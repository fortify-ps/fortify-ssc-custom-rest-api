# Fortify custom REST API endpoints

This project provides custom Fortify SSC REST API endpoints that allow for the following:

* Perform configurable SQL queries on the SSC database
* Query directory and file contents like SSC log files

This project also serves as a generic framework for adding other custom REST API endpoints.

Note that this should be used with care, taking into account the following warnings:

* SSC is not designed to support custom API endpoints. Although this API extension has been 
  tested with SSC 18.20, there is no guarantee that this will ever work with any other SSC versions. 
  Even though there is no direct dependency on SSC-specific libraries, this custom API extension
  heavily depends on the fact that SSC uses the Spring framework to automatically discover endpoint 
  definitions, and to inject SSC resources like the SSC data source into the custom endpoint 
  implementations. In order to manage access to custom API endpoints, this API extension also depends 
  on the fact that SSC uses Spring Security for access decisions.  
* Direct SSC database access could result in a range of security vulnerabilities like SQL Injection
  and Access Control issues. You should carefully review any custom query definitions to prevent
  such vulnerabilities.
* The custom query API should only be used for querying data from SSC; you should never invoke
  any SQL statements that perform updates on the SSC database. SSC may not pick up such updates
  until after an SSC restart, and in the worst case the SSC database may become damaged.
* These custom API endpoints have not been tested in production SSC instances; although unlikely
  the use of these custom endpoints may have a negative impact on regular SSC operations.
* Configuration file contents are not yet stable; you may need to update the configuration
  file whenever you build a new version of the code in this repository.
  
In general, you should first try to implement any required functionality by utilizing the existing
SSC REST API's. This custom API should only be used if the standard API does not fulfill your 
requirements, for example because the standard API doesn't provide access to some data, or if
using a custom query would be (much) more efficient. 

In such cases, you should consider submitting an enhancement request through Fortify Support to
get the required functionality added in a future SSC version. This custom API should then only be
used as a temporary solution, until the required functionality has been added to the standard API.


## Advantages

In case you have a need to access SSC data through database queries because the standard SSC REST API
doesn't provide the functionality to (efficiently) retrieve such data, this custom API provides the
following advantages compared to accessing the SSC database directly from 3<sup>rd</sup>-party systems:

* SSC administrators have full control over the custom queries that are being exposed through
  the custom API.
* Credentials for accessing the SSC database do not need to be shared with 3<sup>rd</sup>-party systems;
  the custom API re-uses the data source provided by SSC.
* Custom token definitions can be defined in SSC's serviceContext.xml to allow token-based access to the
  custom API.
* Access to individual queries can be controlled based on SSC roles and permissions. 

  
## Installation

At the moment, no binary distribution is provided for the custom Fortify REST API endpoints.

Installation instructions:

* Install Maven 3.x and Java JDK 1.8+
* Download or clone the source code in this repository
* Run `mvn clean package`
* Copy the target/custom-rest-api-[version].jar file to the SSC's WEB-INF/lib folder
* Create a custom-api.xml file in the SSC's WEB-INF/classes folder; see [examples](https://github.com/fortify-ps/fortify-ssc-custom-rest-api/tree/master/examples)
and the configuration section below.  
* Restart SSC

## Configuration

To use the custom SSC REST API, you will need to create a WEB-INF/classes/custom-api.xml
file containing information about the custom actions that you want to make available 
through the custom SSC REST API. 

This configuration file must follow the rules for a Spring XML configuration file, i.e. start
and end with beans tags, containing bean elements. See the example configuration files
mentioned in the previous section.

Each action that you want to make available through the custom REST API is defined as an 
Executor bean in this configuration file. Different types of actions use different Executor
classes, each with their own configuration properties. 

The following sections describe the various Executor implementations that are available, as
well as their configuration properties.

### Common Executor configuration properties

All Executor beans defined in the configuration file support the following configuration
properties:

* `requiresAnyRole`: User will only be able to execute this action if user has any of the roles 
  defined by this property. This is an optional property; by default any authenticated user can 
  execute an action.
* `requiresAllPermissions`: User will only be able to execute this action if user has all of 
  the permissions defined by this property. This is an optional property; by default any authenticated
  user can execute an action
* `postProcessExpression`: Allows for arbitrary processing/reformatting the action execution
  results. The default behavior (unless overridden by a specific executor implementation) is to embed
  the results inside a 'data' object, similar to most standard SSC REST API's.


### Reload custom API configuration file

To enable reloading the custom API configuration file through the `/api/v1/custom/reloadConfig`
endpoint, your configuration file will need to contain the following bean definition:

```xml
	<bean class="com.fortify.server.platform.endpoints.rest.custom.reloadconfig.ReloadConfigExecutor">
		<property name="requiresAnyRole" value="Administrator"/>
	</bean>
```

Apart from the common Executor configuration properties described in the previous section,
the ReloadConfigExecutor does not support any other configuration properties.

Note that there can be at most one bean definition with this class in the configuration file.


### Custom queries

Custom queries are defined in the configuration file using bean definitions like
the following:

```xml
	<bean name="queryName" class="com.fortify.server.platform.endpoints.rest.custom.query.QueryExecutor">
		...
	</bean>
```

Apart from the common Executor configuration properties, this bean type supports the following
configuration properties:

* `queryParamExpressions` (default: none): Map containing parameter names as keys, and Spring template 
  expressions (identified by `${SpEL-expression}`) as values. These parameters can be used to
  build the query statement, and as statement parameters (see below). The Spring template
  expressions can access request parameters using `${requestParamName}`.
* `queryExpression` (required): Defines the query to be executed. This query can reference named query 
  parameters (both queryParams defined above, and request parameters) using the `:paramName`
  notation, and also use SpEL templates to build customizable queries using the `${SpEL-expression}`
  notation. Note that especially the latter should be used with care, as this can result in SQL Injection 
  and other vulnerabilities.
* `outputExpressions` (default: list of query columns and values): Map containing output properties as keys,
  and Spring template expressions (identified by `${SpEL-expression}`) as values. The Spring
  template expressions can access column values using the `${columns.columnName}` expression,
  and use `${params.paramName}` to access any request parameters or parameters defined through
  the `queryParams` property. 
  
Any queries defined in custom-api.xml can be referenced by their bean name using the 
`/api/v1/custom/query/{name}` endpoint.

### Directory listings

Actions for listing server directories are defined in the configuration file using bean 
definitions like the following:

```xml
	<bean name="dirListName" class="com.fortify.server.platform.endpoints.rest.custom.filesystem.FileSystemDirContentsExecutor">
		...
	</bean>
```

Apart from the common Executor configuration properties, this bean type supports the following
configuration properties:

* `parentPathExpression` (required): Expression that specifies the directory for which 
  the contents should be listed. This property can use Spring template expressions 
  (identified by `${SpEL-expression}`), for example to look up the Fortify home directory
  (using `${T(System).properties['fortify.home']}`) or request parameters (using
  `${requestParams.requestParamName}`). The latter should be used with extreme care
  to avoid Path Manipulation vulnerabilities.
  
This Executor overrides the default `postProcessExpression` property with 
`args.requestParams.plainText=='true'?T(String).join('\n',data):{'data':{'directoryContents':data}}`.
Basically this allows the API user to specify whether the listing should be returned
as plain text or not, using the plainText=true or plainText=false request parameter.
  
Any directory listing actions defined in custom-api.xml can be referenced by their bean name 
using the `/api/v1/custom/fs/dir/{name}` endpoint.


### File contents

Actions for retrieving server file contents are defined in the configuration file using bean 
definitions like the following:

```xml
	<bean name="fileContentsName" class="com.fortify.server.platform.endpoints.rest.custom.filesystem.FileSystemFileContentsExecutor">
		...
	</bean>
```

Apart from the common Executor configuration properties, this bean type supports the following
configuration properties:

* `parentPathExpression` (required): Expression that specifies the directory from which
  the files are being loaded. This property can use Spring template expressions 
  (identified by `${SpEL-expression}`), for example to look up the Fortify home directory
  (using `${T(System).properties['fortify.home']}`) or request parameters (using
  `${requestParams.requestParamName}`). The latter should be used with extreme care
  to avoid Path Manipulation vulnerabilities.
* `fileNameExpression` (required): Expression that specifies for which file to load the
  contents. This expression can use the same template expressions as `parentPathExpression`.
  
This Executor overrides the default `postProcessExpression` property with 
`args.requestParams.plainText!='false' ? data : {'data':{'contents': args.requestParams.splitLines=='true' ? data.split('\\n') : data}}`.
Basically this allows the API user to specify whether the listing should be returned
as plain text or not, using the plainText=true or plainText=false request parameter. If plainText==false,
the user can use the optional splitLines request parameter to specify whether the file contents
should be returned as a single JSON property, or as a JSON array containing individual lines.
  
Any file contents actions defined in custom-api.xml can be referenced by their bean name 
using the `/api/v1/custom/fs/file/{name}` endpoint.


## Usage

The custom Fortify REST API supports the following endpoints:

* `/api/v1/custom/reloadConfig`: Reloads the custom-api.xml configuration file,
  allowing you to make changes to the configuration file without requiring an SSC restart.
  Note that any code changes to custom-rest-api-[version].jar will require an SSC restart.
  Note that this endpoint is only enabled if the corresponding ReloadConfigExecutor bean
  is configured in the configuration file.
* `/api/v1/custom/query/{name}`: Executes the query defined by `{name}` in 
  the custom-api.xml configuration file. 
* `/api/v1/custom/fs/dir/{name}`: Executes the directory listing operation defined
  by `{name}` in the custom-api.xml configuration file.
* `/api/v1/custom/fs/file/{name}`: Executes the file contents operation defined
  by `{name}` in the custom-api.xml configuration file.
  
  
For example, with the sample configuration file, you can use the following endpoints:

* `/api/v1/custom/query/configPropertyNames`: Retrieves a list of all 
  SSC configuration property names.
* `/api/v1/custom/query/permissionNames`: Retrieves a list of all 
  SSC permission names.
* `/api/v1/custom/query/scanIssuesForCategory?projectVersionId=5&category=SQL*`: 
  Retrieves a list of all scan issues for the given project version, for any issue category starting with 'SQL'. 
* `/api/v1/custom/fs/file/ssc.log?plainText=false&splitLines=true`: Retrieves the contents of the ssc.log file
* `/api/v1/custom/fs/file/getSSCLogFile?fileName=ssc_metrics.log&plainText=true`: Retrieves the contents of the ssc_metrics.log file
* `/api/v1/custom/fs/dir/listSSCLogFiles?plainText=false`: Retrieves the list of files in the SSC log files directory
  
  
# Licensing

See [LICENSE.TXT](LICENSE.TXT)

