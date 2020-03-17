/*******************************************************************************
 * (c) Copyright 2020 Micro Focus or one of its affiliates
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

import com.fortify.server.platform.endpoints.rest.custom.AbstractCustomApiExecutor;

/**
 * This abstract base class provides a default implementation for the {@link AbstractCustomApiExecutor#_execute(Object)}
 * method, by simply calling the abstract {@link #_execute(DataSource, Map)} method based on the corresponding properties
 * provided in the given {@link QueryExecutorArgs} object.
 * 
 * @author Ruud Senden
 *
 */
public abstract class AbstractQueryExecutor extends AbstractCustomApiExecutor<QueryExecutorArgs> implements IQueryExecutor {
	/**
	 * This method simply calls the {@link #_execute(DataSource, Map)} method based on the corresponding properties
	 * provided in the given {@link QueryExecutorArgs} object.
	 */
	@Override
	public final Object _execute(QueryExecutorArgs args) {
		return _execute(args.getDataSource(), args.getRequestParams());
	}
	
	/**
	 * This method is to be implemented by concrete subclasses to perform an actual database query.
	 * @param dataSource
	 * @param requestParams
	 * @return
	 */
	protected abstract Object _execute(DataSource dataSource, Map<String, String> requestParams);
}
