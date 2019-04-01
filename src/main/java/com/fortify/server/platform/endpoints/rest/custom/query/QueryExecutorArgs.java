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

import java.util.Map;

import javax.sql.DataSource;

/**
 * This class holds all of the data required for the {@link AbstractQueryExecutor#_execute(QueryExecutorArgs)} method,
 * which are the SSC data source and a {@link Map} containing all current request parameters.
 *  
 * @author Ruud Senden
 *
 */
public class QueryExecutorArgs {
	private final DataSource dataSource;
	private final Map<String,String> requestParams;
	public QueryExecutorArgs(DataSource dataSource, Map<String, String> requestParams) {
		this.dataSource = dataSource;
		this.requestParams = requestParams;
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	public Map<String, String> getRequestParams() {
		return requestParams;
	}
}