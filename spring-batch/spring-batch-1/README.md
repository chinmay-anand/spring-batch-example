# spring-example/spring-batch/spring-batch-1

This is a simple example where a job is invoked with one single step which terminates after first execution, and the step simply prints a statement.

Steps:
1. Create a Spring Boot application with H2 and Batch dependency. H2 in-memory database will be used by Spring to use for JobRepository to store the state of Job and Steps.
2. StepBuilderFactory is used to get an instance of a step with a tasklet implemented to simply print a statement.
3. JobBuilderFactory is used to get a Job instance which starts execution of the Step defined in previous stage.
4. To add support for a different database such as MySQL:
  * * we need to replace h2 dependency with mysql dependency in pom.xml, 
  * * we need to provide mysql connection parameters in application.propetrties file
  * * we also need to set "spring.batch.initialize-schema=always" in application.properties to assk Spring to create the job repository tables in the schema being used (this is not needed for H2 in-memory database)<br/>

Refer:  [Spring Database Configuration Settings](../../assets/spring_database_configurations.md)
