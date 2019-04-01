# Fortify custom REST API endpoints

This project provides custom Fortify SSC REST API endpoints that allow for performing
arbitrary SQL queries on the SSC database, and also serves as a generic framework for 
adding other custom REST API endpoints.

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
  
## Installation

At the moment, no binary distribution is provided for the custom Fortify REST API endpoints.

Installation instructions:

* Install Maven 3.x and Java JDK 1.8+
* Download or clone the source code in this repository
* Run `mvn clean package`
* Copy the target/custom-rest-api-[version].jar file to the SSC's WEB-INF/lib folder
* Create a custom-api.xml file in the SSC's WEB-INF/classes folder; see [examples](https://github.com/fortify-ps/fortify-ssc-custom-rest-api/tree/master/examples)
and the configuration section below. Note that you will need to rename any example 
configuration files to the exact filename custom-api.xml.  
* Restart SSC

## Configuration

To use the custom query API, you will need to create a WEB-INF/classes/custom-api.xml
file containing the custom queries that you want to make available through the custom
SSC REST API.

This file will need to contain Spring bean definitions with an id (used to reference
the query definition through the custom API) and class `com.fortify.server.platform.endpoints.rest.custom.query.QueryExecutor`.
This class supports the following properties:

* requiresAnyRole (default: none): User will only be able to execute this query if user
  has any of the roles defined by this property.
* requiresAllPermissions (default: none): User will only be able to execute this query if
  user has all of the permissions defined by this property.
* embedResultInDataObject (default: true): Embed any query results into a 'data' object,
  similar to most standard SSC API's.
* queryParams (default: none): Map containing parameter names as keys, and Spring template 
  expressions (identified by `${SpEL-expression}`) as values. These parameters can be used to
  build the query statement, and as statement parameters (see below). The Spring template
  expressions can access request parameters by name.
* query (required): Defines the query to be executed. This query can reference named query 
  parameters (both queryParams defined above, and request parameters) using the `:paramName`
  notation, and also use SpEL templates to build customizable queries using the `${SpEL-expression}`
  notation. Note that especially the latter should be used with care, as this can result in SQL Injection 
  and other vulnerabilities.
* output (default: list of query columns and values): Map containing output properties as keys,
  and Spring template expressions (identified by `${SpEL-expression}`) as values. The Spring
  template expressions can access column values using the `${columns.columnName}` expression,
  and use `${params.paramName}` to access any request parameters or parameters defined through
  the `queryParams` property. 
  
Any queries defined in custom-api.xml can then be referenced by their bean id using the 
`/api/v1/custom/query/{name}` endpoint.


## Usage

The custom Fortify REST API supports the following endpoints:

* `/api/v1/custom/reloadConfig`: Reloads the custom-api.xml configuration file,
  allowing you to make changes to the configuration file without requiring an SSC restart.
  Note that any code changes to custom-rest-api-[version].jar will require an SSC restart.
* `/api/v1/custom/query/{name}`: Executes the query defined by `{name}` in the 
  custom-api.xml configuration file. 
  