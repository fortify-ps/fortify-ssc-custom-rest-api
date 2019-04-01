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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.fortify.server.platform.endpoints.rest.custom.AbstractCustomApi;

/**
 * This concrete {@link AbstractCustomApi} implementation provides the '/api/v1/custom/query/{name}' endpoint,
 * which allows for executing an {@link IQueryExecutor} instance with the given name from the custom API
 * configuration file. 
 * 
 * @author Ruud Senden
 *
 */
@RestController
public class CustomAPIQueryDb extends AbstractCustomApi {
	@Autowired(required = true)
	private DataSource dataSource;

	@RequestMapping(value = "/custom/query/{name}", produces = "application/json", method = { RequestMethod.GET })
	public Object query(@PathVariable String name, @RequestParam Map<String, String> requestParams) {
		return execute(name, IQueryExecutor.class, new QueryExecutorArgs(dataSource, requestParams));
	}
}