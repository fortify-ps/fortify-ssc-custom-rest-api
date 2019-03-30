# fortify-ssc-custom-rest-api

# Do not use in production SSC instances; this is just a prototype

For now, this is just a prototype for adding custom REST API endpoints to SSC. The prototype adds the following API endpoints:

* /api/v1/custom/ds: Shows the SSC data source class name injected by Spring
* /api/v1/custom/q: Shows how to query the SSC database using the injected data source and Spring JdbcTemplate
* /api/v1/custom/r: Shows how to access random request parameters
* /api/v1/custom/u: Shows how to access the user details for the user currently logged in to SSC

The idea is to add one or more endpoints that allow for accessing data from the SSC database that is not (or not efficiently) available through the standard SSC REST API's, for example using a (Spring bean) configuration as follows:

```xml
<property name="requiredPermissions">PERM_xxx,PERM_yyy</property>
<property name="queryParameters><map>
   <entry key="param1" value="${reqParam1=='xxx'?'yyy':'zzz'}"/>
</map></property>
<property name="query" value="SELECT x,y,z FROM a WHERE b=:param1"/>
```

Some advantages compared to direct database access from 3rd-party integrations:

* Uses data source/database connections provided by SSC; no need to share SSC database credentials with 3rd-party systems
* SSC administrator defines which database queries are available through the custom API
* Shares access control with SSC

## Build & install

To build, simply run `mvn clean package`. To install, simply copy the resulting jar-file to the SSC WEB-INF/lib directory, and restart SSC.
