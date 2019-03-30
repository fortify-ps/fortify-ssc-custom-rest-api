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

import java.security.Principal;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Test {
	@Autowired(required=true)
	private DataSource dataSource;
	
    @RequestMapping(value="/custom/ds", produces="application/json", method = { RequestMethod.GET })
    public String ds() {
        return dataSource.getClass().getName();
    }
    
    @RequestMapping(value="/custom/q", produces="application/json", method = { RequestMethod.GET })
    public List<Map<String, Object>> q() {
    	return new JdbcTemplate(dataSource).queryForList("select * from configproperty");
    }
    
    @RequestMapping(value="/custom/r", produces="application/json", method = { RequestMethod.GET })
    public Map<String, String> r(@RequestParam Map<String,String> allRequestParams) {
    	return allRequestParams;
    }
    
    @RequestMapping(value="/custom/u", produces="application/json", method = { RequestMethod.GET })
    public Authentication u() {
    	return SecurityContextHolder.getContext().getAuthentication();
    }
    
 
    
}