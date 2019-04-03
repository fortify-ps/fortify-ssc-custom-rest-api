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
package com.fortify.server.platform.endpoints.rest.custom.filesystem;

import java.io.IOException;
import java.nio.file.LinkOption;
import java.nio.file.Path;
import java.nio.file.Paths;

import com.fortify.server.platform.endpoints.rest.custom.AbstractCustomApiExecutor;
import com.fortify.server.platform.endpoints.rest.custom.RequestParamsArgs;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

public abstract class AbstractFileSystemContentsExecutor extends AbstractCustomApiExecutor<RequestParamsArgs> {
	private TemplateExpression parentPathExpression;
	
	@Override
	protected final Object _execute(RequestParamsArgs args) throws IOException {
		String parentPathName = parentPathExpression==null ? null : SpringExpressionUtil.evaluateExpression(args, parentPathExpression, String.class);
		if ( parentPathName == null ) {
			throw new IllegalArgumentException("Parent path may not be null");
		}

		Path parentPath = Paths.get(parentPathName).toRealPath(LinkOption.NOFOLLOW_LINKS);
		
		return _execute(args, parentPath);
	}
	
	protected abstract Object _execute(RequestParamsArgs args, Path parentPath) throws IOException;

	public TemplateExpression getParentPathExpression() {
		return parentPathExpression;
	}

	public void setParentPathExpression(TemplateExpression parentPathExpression) {
		this.parentPathExpression = parentPathExpression;
	}
}
