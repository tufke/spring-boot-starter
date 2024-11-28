# Spring boot starter implementation instructions

## Default auto configuration
- [x] ApplicationStartupListener is enabled, can be disabled setting property <b>service.starter.application.listener.enabled = false</b>
- [x] web security is enabled, can be disabled setting property <b>service.starter.security.enabled = false</b>
- [x] Jackson's configuration is enabled, can be disabled by setting property <b>service.starter.jackson.enabled = false</b>
- [x] Request and response logging is disabled, can be enabled setting property <b>service.starter.logging.enabled = true</b>
    - [x] Log headers with <b>service.starter.logging.includeHeaders = true</b>
    - [x] Log query string (endpoint url) with <b>service.starter.logging.includeQueryString = true</b>
    - [x] Log Json Payload with <b>service.starter.logging.includePayload = true</b>
- [x] Exception handling with Problem object is enabled, can be disabled by setting property <b>service.starter.problem.enabled = false</b>
- [x] Service platform banner is enabled when starting spring boot (replaces the Spring banner) can not be disabled

Jackson's configuration and Generic exception handling configuration should be enabled in a Rest service with Json payload. 
Request response logging can be enabled during development to debug the requests.

## Generic exception handling
Generic exception handling is available for use in rest services with a json payload. the use of openapi is not required the
exception handling is generic for all rest services with json payload. when switched on all exceptions
thrown inside a rest controller will get caught in a generic class named: ProblemEntityExceptionHandler. In this handler
the exception is logged and transformed to a Problem object and then returned in a ResponseEntity with a http status code.
The client will always receive a Problem json object holding information about the exception. What is exposed to the
client in this Problem object depends on the exception and how we configured to handle it.


```text
{
  "type" : "constraint-violation",
  "title" : "Bad Request",
  "status" : 400,
  "detail" : "Validation failed, see violations property for more details",
  "instance" : "/quotes-service/api/quotes/-1",
  "violations" : [ {
    "field" : "getQuoteById.quoteId",
    "message" : "must be greater than or equal to 1"
  } ]
}
```
<i><b>Example Problem response caused by a ConstraintValidation exception</b></i>

Below a description of how all exceptions are handled,

- ServiceException:\
  Traceable runtime exception which a developer can throw for instance after catching a checked exception. This exception has a unique uuid,
  and it can have an error code if the developer adds one. both code and uuid can be used to trace the problem in the log files.
  by default the exception message is only exposed to the client in case the http status code is in the 400 range (client error).
  in all other cases the exception message is logged.
    - log: In case exposeDetails is false, log "Unexposed exception details - uuid = 123-456-789 :" + exception message
    - return Problem object
        - type: traceable-problem
        - status: status code from exception otherwise 500
        - title: Description of the http status code (Internal Server Error for status 500)
        - detail: in case exposeDetails is true or the http status code in 400 range the exception message
        - instance: request URI
        - id: uuid
        - code: error code if provided in exception


- UnauthorizedException:\
  Exception to be thrown when an identity is not authenticated sufficiently to access a rest controller (service) at all
    - log: "*** ACCESS DENIED *** " + exception message
    - return Problem object
        - type: problem
        - status: 401
        - title: Unauthorized
        - detail: "Access Denied"
        - instance: request URI


- ForbiddenException:\
  Exception to be thrown when an identity has insufficient access rights to a resource method in the rest controller
    - log: "*** ACCESS DENIED *** " + exception message
    - return Problem object
        - type: problem
        - status: 403
        - title: Forbidden
        - detail: "Access Denied"
        - instance: request URI


- IllegalArgumentException:\
  Exception to be thrown to indicate that a method has been passed an illegal or inappropriate argument
    - log: "Illegal argument: {}" + exception message + the complete stack trace
    - return Problem object
        - type: problem
        - status: 400
        - title: Bad Request
        - detail: <empty>
        - instance: request URI


- ResponseStatusException:\
  Base class for exceptions used for applying a status code to an HTTP response. Developers can make custom exceptions
  extending this class to wrap a checked exception and set a desired http status code in the exception.
    - log: "Response status: " + http status code + exception message
    - return Problem object
        - type: problem
        - status: as provided in the exception
        - title: description of the set http status code
        - detail: <empty>
        - instance: request URI


- MethodArgumentTypeMismatchException:\
  A TypeMismatchException raised while resolving a rest controller method argument (input object of the operation).
  This exception is thrown when validating a Request Body and a validation fails in the model object of the operation.
  The model object has annotated properties @NotNull, @Pattern, etc to validate properties.
    - log: nothing
    - return Problem object
        - type: problem
        - status: 400
        - title: Bad Request
        - detail: "Invalid value '%s' for parameter '%s'"
        - instance: request URI


- ValidationException:\
  A failed validation (@NotNull @min, etc) will trigger a ConstraintViolationException instead of a MethodArgumentNotValidException
  in case it didn't occur in a request body model object. for instance in these cases:
    - Validating Path Variables and Request Parameters (Url parameter annotated with @Min, @Max, etc), an id in the url for instance
    - Validating Input to a Spring Service Method (@valid on objects as method parameters within a @Component or @Service class)
    - Validating JPA Entities
        - log: nothing
        - return Problem object
            - type: problem
            - status: 400
            - title: Bad Request
            - detail: exception message
            - instance: request URI


- ConstraintViolationException:\
  Same as ValidationException but now there are multiple validation messages
    - log: nothing
    - return Problem object
        - type: constraint_violations
        - status: 400
        - title: Bad Request
        - detail: "Validation failed, see violations property for more details"
        - instance: request URI
        - violations: list with all validation messages


- all other exceptions:
    - log: "Uncaught exception" + the complete stack trace
    - return Problem object
        - type: problem
        - status: 500
        - title: Internal Server Error
        - detail: <empty>
        - instance: request URI

## Request response logging
By enabling this feature all incoming requests and their responses will get logged. This only works in rest services
with a json payload. You can add the request and response headers and json payload to the logs if you switch it on in the
configuration.

```text
2024-11-27 14:59:58,986 INFO  [http-nio-12345-exec-10] nl.kabisa.spring.boot.starter.service.logging.RequestResponseLoggingFilter: 1 > REQUEST POST /quotes-service/api/quotes content-type=application/json
1 > thread:[http-nio-12345-exec-10]
1 > content-type=application/json
{
  "text": "To quote or not to quote"
  "author": "mark"
}

2024-11-27 14:59:59,156 INFO  [http-nio-12345-exec-10] nl.kabisa.spring.boot.starter.service.logging.RequestResponseLoggingFilter: 1 < RESPONSE 201 (Created) [180 ms] POST /quotes-service/api/quotes content-type=application/json
1 < thread:[http-nio-12345-exec-10]
1 < content-type=application/json
{
  "id" : 1,
  "text" : "To quote or not to quote",
  "author" : "mark",
  "active" : true
}
```
<i><b>Example request response logging with payload and without headers</i></b>

## Jackson configuration
By enabling this feature the Jackson parser will get configured. This is useful in rest services with a json payload.
it will register a bean named JacksonCustomizer in the spring context which holds configuration for jackson to
map json to objects and vice versa.
- LocalDate will be formatted as "yyyy-MM-dd"
- LocalDateTime will be formatted as "yyyy-MM-dd HH:mm:ss"
- ZonedDateTime will be formatted as "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
- json will be indented
- only map non empty values
- don't fail when encountering a property that is not available
- Use a getter to map to a collection if no setter is found
- don't write dates as numeric timestamp but as textual representation (Date and Calendar classes)

## Service banner
Integrated in the Spring boot starter is a banner that will show when you start up your server.
This banner replaces the Spring banner to make clear it is a platform service we are running.
This will make the service feel like a product of the Platform.

```text
#
#  .----------------.  .----------------.  .----------------.  .----------------.  .----------------.  .----------------.
# | .--------------. || .--------------. || .--------------. || .--------------. || .--------------. || .--------------. |
# | |  ___  ____   | || |      __      | || |   ______     | || |     _____    | || |    _______   | || |      __      | |
# | | |_  ||_  _|  | || |     /  \     | || |  |_   _ \    | || |    |_   _|   | || |   /  ___  |  | || |     /  \     | |
# | |   | |_/ /    | || |    / /\ \    | || |    | |_) |   | || |      | |     | || |  |  (__ \_|  | || |    / /\ \    | |
# | |   |  __'.    | || |   / ____ \   | || |    |  __'.   | || |      | |     | || |   '.___`-.   | || |   / ____ \   | |
# | |  _| |  \ \_  | || | _/ /    \ \_ | || |   _| |__) |  | || |     _| |_    | || |  |`\____) |  | || | _/ /    \ \_ | |
# | | |____||____| | || ||____|  |____|| || |  |_______/   | || |    |_____|   | || |  |_______.'  | || ||____|  |____|| |
# | |              | || |              | || |              | || |              | || |              | || |              | |
# | '--------------' || '--------------' || '--------------' || '--------------' || '--------------' || '--------------' |
#  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'  '----------------'
#
   ::  Kabisa - Service Platform  ::
```


## Adding new auto configuration:
- Implement a configuration class.
- Add conditional annotations so this class can be disabled/overridden.
- add the class with package name as prefix in AutoConfiguration.imports file under src/main/resources/META-INF/spring.


# Spring boot starters

## starters
Starter projects work as spring boot starters using autoconfiguration and enables the implementation
if they are found within the classpath.

## Starters developed
- service-spring-boot-starter – Spring Boot Starter, Spring Cloud, Spring Security, Spring Eureka are in this starter
- database-spring-boot-starter – dependencies regarding database are moved within this starter

## service-spring-bom project

The Service Bill of Material (BOM) is included in each starter project for versioning management of dependencies.
___
## Using the starters

service-spring-boot-starter

This starter holds a common implementation for all Service components.
Any new improvement that is common for all Service components should be done within service-spring-boot-starter.

# Adding service-spring-boot-starter in your project

1. **Update build.gradle file:**
    - add a dependency by service-spring-boot-starter within the build.gradle
      `implementation 'nl.kabisa.spring.boot.starter:service-spring-boot-starter'`
    - remove all dependencies found within service-spring-boot-starter from your project.
      - add service-spring-bom as BOM

    ``` gradle 
        dependencyManagement {
            imports {
                mavenBom "nl.kabisa.spring.boot.starter:service-spring-bom:${serviceSpringBomVersion}"
            }
        }
    ```
2. **Code updates:**
    - Remove ApplicationListener from your application, implemented within service-spring-boot-starter
    - Remove WebSecurityConfig from your application, implemented within service-spring-boot-starter
    - Remove any ExceptionHandlers from your application, implemented within service-spring-boot-starter
    - Remove any Jackson serialization and deserialization from your application, implemented within service-spring-boot-starter
    - Remove any request response logging, implemented within service-spring-boot-starter
3. **Disabling or enabling starter default configuration:**
    - service.starter.application.listener.enabled - setting to false will disable default ApplicationListener . Missing property is treated as true setup value.
    - service.starter.security.enabled – setting to false will disable WebSecurityConfig adapter. Missing property is treated as true setup value.
    - service.starter.jackson.enabled setting to false will disable Jackson configuration. By default, it is switched on.
    - service.starter.logging.enabled setting to true will enable request and response logging. By default, it is switched off.
        - service.starter.logging.includeHeaders setting to true will log request headers. By default, it is switched off.
        - service.starter.logging.includeQueryString setting to true will log the query string (request uri path). By default, it is switched on.
        - service.starter.logging.includePayload setting to true will log the Json request payload. By default, it is switched off.
    - service.starter.problem.enabled setting to false will stop catching all exceptions, log them and return a Problem object with a Http error status code

# Adding database-spring-boot-starter in your project

1. **Update build.gradle file:**
    - Add dependency by database-spring-boot-starter within build.gradle
      `implementation 'nl.kabisa.spring.boot.starter:database-spring-boot-starter'`
    - Remove all dependencies found within database-boot-starter from your project. 
