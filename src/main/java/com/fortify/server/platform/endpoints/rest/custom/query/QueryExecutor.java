/*******************************************************************************
 * (c) Copyright 2017 EntIT Software LLC
 *
 * Permission is hereby granted, free of charge, to any person obtaining a 
 * copy of this software and associated documentation files (the 
 * "Software"), to deal in the Software without restriction, including without 
 * limitation the rights to use, copy, modify, merge, publish, distribute, 
 * sublicense, and/or sell copies of the Software, and to permit persons to 
 * whom the Software is furnished to do so, subject to the following 
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be included 
 * in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY 
 * KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE 
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR 
 * PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE 
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, 
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF 
 * CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN 
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS 
 * IN THE SOFTWARE.
 ******************************************************************************/
package com.fortify.server.platform.endpoints.rest.custom.query;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.jdbc.core.ColumnMapRowMapper;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

/**
 * <p>This {@link AbstractQueryExecutor} implementation allows for executing arbitrary
 * SQL queries. Most configuration properties on this class support the
 * Spring Expression Language through template expressions; where indicated you can
 * use ${SpEL-expression}. This class supports the following functionality:</p>
 *  
 * <ol>
 *  <li>Build a parameters map, containing all request parameters, and optionally all parameters
 *      defined through the {@link #queryParams} map property. The values in the {@link #queryParams}
 *      map property can use Spring Expression Language to refer to request parameters, for example
 *      using ${requestParamName}.</li>
 *  <li>Evaluate the query configured through the {@link #query} property. Again the {@link #query} 
 *      property can use Spring Expression Language to refer to any parameters defined in the parameters
 *      map described above (i.e. all query parameters, and all parameters defined through the 
 *      {@link #queryParams} map property). Note that you should be very careful with building a
 *      query based on these parameters, as this can easily result in SQL Injection and other 
 *      vulnerabilities. Whenever possible, you should use query parameter substitution instead, 
 *      based on named parameters from the parameters map, using the ':paramName' notation.</li>
 *  <li>Execute the query resulting from the previous step.</li>
 *  <li>Return the query result. By default, this will return a list of maps, containing the column
 *      names and values for each result set row. This can optionally be overridden through the
 *      {@link #output} map property, with each entry describing a property name to be returned,
 *      together with a Spring Template Expression to calculate the value for that property.</li>
 * </ol>  
 * 
 * <p>As an example, consider the following bean definition:</p>
 * <pre>{@code 
 *  <bean id="myQuery" class="com.fortify.server.platform.endpoints.rest.custom.query.QueryExecutor">
 *		<property name="requiresAnyRole" value="Administrator"/>
 *		
 *		<property name="queryParamExpressions"><map>
 *			<entry key="param1" value="SomeText ${requestParam1}"/>
 *			<entry key="param2" value="COL_${requestParam2}"/>
 *		</map></property>
 *		<property name="queryExpression"><value><![CDATA[
 *			SELECT col1, col2, col3 FROM ${param2} WHERE col1=:param1 AND col3=:requestParam3
 *		]]></value></property>
 *		<property name="outputExpressions"><map>
 *			<entry key="prop1" value="${params.param1}"/>
 *			<entry key="prop2" value="${columns.col1} - ${columns.col2}"/>
 *		</map></property>
 *	</bean>
 * }</pre>
 * 
 * <p>Given requestParam1=A, requestParam2=B and requestParam3=C, this would result in the following 
 * prepared statement to be executed, with 'SomeText A' and 'C' as the prepared statement parameters:
 * {@code SELECT col1, col2, col3 FROM COL_B WHERE col1=? AND col3=?}. Of course, the use of '${param2}'
 * in the query template is unsafe, as this can easily result in SQL injection. You should use proper
 * validation to make sure that the given request parameter results in a proper column name.</p>
 * 
 * <p>Given the 'output' property, this definition would result in a JSON array being returned, with each
 * array entry representing a single query result row, containing prop1 with value 'SomeText A', and prop2 
 * containing the concatenated values of col1 and col2.</p>
 * 
 * 
 * @author Ruud Senden
 *
 */
public class QueryExecutor extends AbstractQueryExecutor {
	private Map<String, TemplateExpression> queryParamExpressions;
	private TemplateExpression queryExpression;
	private Map<String, TemplateExpression> outputExpressions;
	

	@Override
	protected Object _execute(DataSource dataSource, Map<String, String> requestParams) {
		Map<String, Object> evaluatedQueryParams = evaluateQueryParams(requestParams);
		String evaluatedQuery = evaluateQuery(evaluatedQueryParams);
		RowMapper<Map<String, Object>> rowMapper = getRowMapper(evaluatedQueryParams);
		return new NamedParameterJdbcTemplate(dataSource).query(evaluatedQuery, evaluatedQueryParams, rowMapper);
	}
	
	private Map<String, Object> evaluateQueryParams(Map <String, String> requestParams) {
		Map<String, Object> result = new HashMap<>(requestParams);
		if ( queryParamExpressions!=null && !queryParamExpressions.isEmpty() ) {
			queryParamExpressions.forEach((k, v) -> result.put(k, SpringExpressionUtil.evaluateExpression(requestParams, v, Object.class)));
		}
		return result;
	}
	
	private String evaluateQuery(Map <String, Object> params) {
		return SpringExpressionUtil.evaluateExpression(params, queryExpression, String.class);
	}
	
	private RowMapper<Map<String, Object>> getRowMapper(Map<String, Object> params) {
		return ( outputExpressions==null || outputExpressions.isEmpty() ) ? new ColumnMapRowMapper() : new SpELRowMapper(params, outputExpressions);
	}
	
	public Map<String, TemplateExpression> getQueryParamExpressions() {
		return queryParamExpressions;
	}

	public void setQueryParamExpressions(Map<String, TemplateExpression> queryParamExpressions) {
		this.queryParamExpressions = queryParamExpressions;
	}

	public TemplateExpression getQueryExpression() {
		return queryExpression;
	}

	public void setQueryExpression(TemplateExpression queryExpression) {
		this.queryExpression = queryExpression;
	}

	public Map<String, TemplateExpression> getOutputExpressions() {
		return outputExpressions;
	}

	public void setOutputExpressions(Map<String, TemplateExpression> outputExpressions) {
		this.outputExpressions = outputExpressions;
	}



	private static final class SpELRowMapper implements RowMapper<Map<String, Object>> {
		private final Map<String, Object> params;
		private final Map<String, TemplateExpression> outputExpressions;
		private final ColumnMapRowMapper columnMapRowMapper = new ColumnMapRowMapper(); 
		
		public SpELRowMapper(Map<String, Object> params, Map<String, TemplateExpression> outputExpressions) {
			this.params = params;
			this.outputExpressions = outputExpressions;
		}

		@Override
		public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
			RowData rowData = new RowData(this.params, columnMapRowMapper.mapRow(rs, rowNum));
			Map<String, Object> result = new LinkedHashMap<>(outputExpressions.size());
			outputExpressions.forEach((k, v) -> result.put(k, SpringExpressionUtil.evaluateExpression(rowData, v, Object.class)));
			return result;
		}
		
		@SuppressWarnings("unused")
		private static final class RowData {
			private final Map<String, Object> params;
			private final Map<String, Object> columns;
			
			public RowData(Map<String, Object> params, Map<String, Object> columns) {
				super();
				this.params = params;
				this.columns = columns;
			}
			public Map<String, Object> getColumns() {
				return columns;
			}
			public Map<String, Object> getParams() {
				return params;
			}
		}
	}
}
