# spring-example/spring-batch/spring-batch-1

This is a simple example where a job is invoked with one single step which terminates after first execution, and the step simply prints a statement.

Steps:
1. Create a Spring Boot application with H2 and Batch dependency. H2 in-memory database will be used by Spring to uss for JobRepository to store the state of JOb and Steps.
2. StepBuilderFactory is used to get an instance of a step with a tasklet implemented to simply print a statement.
3. JobBuilderFactory is used to get a Job instance which starts execution of the Step defined in previous stage.
