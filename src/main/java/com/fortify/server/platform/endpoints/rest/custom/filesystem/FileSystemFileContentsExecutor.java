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
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.nio.file.Path;

import org.springframework.security.access.AccessDeniedException;

import com.fortify.server.platform.endpoints.rest.custom.RequestParamsArgs;
import com.fortify.util.spring.SpringExpressionUtil;
import com.fortify.util.spring.expression.TemplateExpression;

public class FileSystemFileContentsExecutor extends AbstractFileSystemContentsExecutor {
	private TemplateExpression fileNameExpression;
	
	public FileSystemFileContentsExecutor() {
		setPostProcessExpression(SpringExpressionUtil.parseSimpleExpression("args.requestParams.plainText!='false' ? data : {'data':{'contents': args.requestParams.splitLines=='true' ? data.split('\\n') : data}}"));
	}
	
	@Override
	protected Object _execute(RequestParamsArgs args, Path parentPath) throws IOException {
		String fileName = fileNameExpression==null ? null : SpringExpressionUtil.evaluateExpression(args, fileNameExpression, String.class);
		
		if ( fileName == null ) {
			throw new IllegalArgumentException("File name may not be null");
		}

		Path file = parentPath.resolve(fileName).toRealPath(LinkOption.NOFOLLOW_LINKS);
		if ( !file.startsWith(parentPath) ) {
			throw new AccessDeniedException("File "+file+" is outside of parent path "+parentPath);
		}
		
		return new String(Files.readAllBytes(file));
	}

	public TemplateExpression getFileNameExpression() {
		return fileNameExpression;
	}

	public void setFileNameExpression(TemplateExpression fileNameExpression) {
		this.fileNameExpression = fileNameExpression;
	}
}
