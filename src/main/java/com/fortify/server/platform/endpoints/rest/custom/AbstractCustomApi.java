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
package com.fortify.server.platform.endpoints.rest.custom;

import java.util.Collections;

import org.springframework.context.ApplicationContext;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.fortify.util.spring.SpringContextUtil;

/**
 * <p>This abstract base class for custom REST API implementations allows for loading
 * and invoking {@link ICustomApiExecutor} instances from a Spring configuration file 
 * named custom-api.xml located on the class path. It also provides the 
 * '/api/v1/custom/reloadConfig' REST endpoint for on-demand reloading of this 
 * configuration file.</p>
 * 
 * <p>Concrete subclasses will need to provide the actual REST controller implementation,
 * taking into account the following requirements:</p>
 * <ul>
 *  <li>Located in a sub-package of com.fortify.server.platform.endpoints.rest.custom</li>
 *  <li>Annotated with {@link RestController}</li>
 *  <li>Providing one or more handler methods annotated with {@link RequestMapping}</li>
 *  <li>Each handler method needs to call the {@link #execute(String, Class, Object)} method
 *      provided by this base class, passing the name and type of the {@link ICustomApiExecutor}
 *      to be used, together with a corresponding executor arguments object</li>
 * </ul>
 * 
 * @author Ruud Senden
 */
public abstract class AbstractCustomApi {
	private ApplicationContext applicationContext = null; 
	
	/**
	 * This method retrieves the {@link ICustomApiExecutor} instance with the given name
	 * and type from the Spring application context, invokes the {@link ICustomApiExecutor#execute(Object)}
	 * method with the given arguments object, and returns the results. 
	 * @param name Name of the {@link ICustomApiExecutor} to be executed
	 * @param executorType Type of the {@link ICustomApiExecutor} to be executed
	 * @param args Arguments object to be passed to the {@link ICustomApiExecutor#execute(Object)} method
	 * @return Return value of the {@link ICustomApiExecutor#execute(Object)} method
	 */
	protected final <A, E extends ICustomApiExecutor<A>> Object execute(String name, Class<E> executorType, A args) {
		return getApplicationContext().getBean(name, executorType).execute(args);
	}
	
	@RequestMapping(value = "/custom/reloadConfig", produces = "application/json", method = { RequestMethod.GET })
	public synchronized Object reloadConfig() {
		this.applicationContext = null; getApplicationContext();
		return Collections.singletonMap("result", "OK");
	}
	
	private synchronized ApplicationContext getApplicationContext() {
		if ( applicationContext == null ) {
			applicationContext = SpringContextUtil.loadApplicationContextFromResources(true, "classpath:custom-api.xml");
		}
		return applicationContext;
	}
}
