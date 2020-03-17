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
package com.fortify.server.platform.endpoints.rest.custom.filesystem;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;

import com.fortify.server.platform.endpoints.rest.custom.RequestParamsArgs;
import com.fortify.util.spring.SpringExpressionUtil;

public class FileSystemDirContentsExecutor extends AbstractFileSystemContentsExecutor {
	public FileSystemDirContentsExecutor() {
		setPostProcessExpression(SpringExpressionUtil.parseSimpleExpression("args.requestParams.plainText=='true'?T(String).join('\n',data):{'data':{'directoryContents':data}}"));
	}
	
	@Override
	protected Object _execute(RequestParamsArgs args, Path parentPath) throws IOException {
		// TODO Instead of returning a plain directory contents list, add an href property that points to the corresponding API to get file contents 
		return Arrays.asList(parentPath.toFile().list());
	}
}
