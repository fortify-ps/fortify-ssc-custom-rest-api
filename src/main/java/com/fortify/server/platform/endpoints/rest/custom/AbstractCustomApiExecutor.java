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

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fortify.util.spring.SpringExpressionUtil;

/**
 * This abstract base implementation of {@link ICustomApiExecutor} provides the following
 * functionality:
 * <ul>
 *  <li>Perform access checks based on configurable properties, before invoking the actual operation</li>
 *  <li>Disable embedding the results into a 'data' object if the {@link #embedResultInDataObject} property 
 *      is set to 'false'</li>
 * </ul>
 * @author Ruud Senden
 *
 * @param <A>
 */
public abstract class AbstractCustomApiExecutor<A> implements ICustomApiExecutor<A> {
	private String[] requiresAnyRole = null;
	private String[] requiresAllPermissions = null;
	private boolean embedResultInDataObject = true;
	
	/**
	 * This {@link ICustomApiExecutor#execute(Object)} implementation does the following:
	 * <ul>
	 *  <li>Check access permissions by calling the {@link #checkAccess()} method</li>
	 *  <li>Execute the actual operation by calling the abstract {@link #_execute(Object)} method</li>
	 *  <li>Embed the execution result into a 'data' object if the {@link #embedResultInDataObject} 
	 *      property is set to 'true'</li>
	 * </ul>
	 */
	@Override
	public final Object execute(A args) {
		checkAccess();
		Object result = _execute(args);
		if ( isEmbedResultInDataObject() ) {
			result = Collections.singletonMap("data", result);
		}
		return result;
	}
	
	/**
	 * This abstract method needs to be implemented by subclasses to actually perform
	 * some operation.
	 * @param args
	 * @return
	 */
	protected abstract Object _execute(A args);

	/**
	 * This method calls various check*() methods to verify that the current user
	 * is allowed to perform the current operation.
	 */
	protected void checkAccess() {
		checkRoles();
		checkPermissions();
		// TODO Add support for checking application version access (use SpEL to get application version id's from request parameters)
		// TODO Add support for custom access checks?
		// TODO How to have SSC properly display an 'unauthorized' message, instead of 'an unexpected error has occurred'
	}
	
	/**
	 * This method checks whether the current user has any of the roles defined in the
	 * {@link #requiresAnyRole} property.
	 */
	protected void checkRoles() {
		if ( requiresAnyRole!=null && requiresAnyRole.length > 0 ) {
			List<String> requiresAnyRoleList = Arrays.asList(requiresAnyRole);
			Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
			Collection<?> userRoles = SpringExpressionUtil.evaluateExpression(principal, "permissionTemplates.![name]", Collection.class);
			userRoles.retainAll(requiresAnyRoleList);
			if ( userRoles.isEmpty() ) throw new AccessDeniedException("User doesn't have any of the roles "+requiresAnyRoleList);
		}
	}

	/**
	 * This method checks whether the current user has all of the permissions defined in the
	 * {@link #requiresAllPermissions} property.
	 */
	protected void checkPermissions() {
		if ( requiresAllPermissions!=null && requiresAllPermissions.length > 0 ) {
			List<String> requiresAllPermissionsList = Arrays.asList(requiresAllPermissions);
			Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
			Collection<?> userPermissions = SpringExpressionUtil.evaluateExpression(authentication, "authorities.![authority]", Collection.class);
			if ( !userPermissions.containsAll(requiresAllPermissionsList) ) {
				requiresAllPermissionsList.removeAll(userPermissions);
				throw new AccessDeniedException("User doesn't have the permission(s) "+requiresAllPermissionsList); }
		}
	}
	
	public String[] getRequiresAnyRole() {
		return requiresAnyRole;
	}

	public void setRequiresAnyRole(String[] requiresAnyRole) {
		this.requiresAnyRole = requiresAnyRole;
	}

	public String[] getRequiresAllPermissions() {
		return requiresAllPermissions;
	}

	public void setRequiresAllPermissions(String[] requiresAllPermissions) {
		this.requiresAllPermissions = requiresAllPermissions;
	}

	public boolean isEmbedResultInDataObject() {
		return embedResultInDataObject;
	}

	public void setEmbedResultInDataObject(boolean embedResultInDataObject) {
		this.embedResultInDataObject = embedResultInDataObject;
	}
}
