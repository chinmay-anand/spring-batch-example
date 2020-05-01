package com.chinmay.spring.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatch1Application {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;

	@Bean
	public Step giveParcelToCustomerStep() {
		return this.stepBuilderFactory.get("give_parcel_to_customer_step.").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Parcel successfully delivered to customer.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step driveToAddressStep() {
		
		boolean GOT_LOST=false; //Setting this flag causes this step to fail by throwing a RuntimeException
		return this.stepBuilderFactory.get("drive_to_address_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				if (GOT_LOST) throw new RuntimeException("Got lost while drive to teh address!!!");
				
				System.out.println("Successfully arrived at the address.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step parcelPackingStep() {
		return this.stepBuilderFactory.get("parcel_packing_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				String item = chunkContext.getStepContext().getJobParameters().get("item").toString();
				String date = chunkContext.getStepContext().getJobParameters().get("packing_date").toString();
				System.out.println(String.format("The %s has been packed on %s.", item, date));
				return RepeatStatus.FINISHED;
			}
		}).build();
		//If we pass a wrong parameter say "packing_time" instead of "packing_date" we will get NullPointerException as one of the getJobParameters().get methods will fail.
		/**
		 * Syntax for stand-alone batch command invocaton:
		 * 		java -jar target\spring-batch-1-1.0.0.jar "item=box" "packing_date=2020-04-30"
		 * Next time if we want to test again we need to pass a differrent value for any of the parameters so that a new job will get launched.
		 * We can specify the data type of a parameter at runtime, for example:
		 * 		java -jar target\spring-batch-1-1.0.0.jar "item=box" "packing_date(date)=2020/04/30"
		 * 		As per my machine settings the exception thrown on my machine asked for "yyyy/MM/dd" format of date when I passed "2020-04-30".
		 * 
		 * Below three SQLs can be used to validate the status of the steps and jobs 
		 * 
		 * SELECT * FROM batch_repo.BATCH_JOB_INSTANCE ORDER BY jOB_INSTANCE_ID desc;
		 * SELECT * FROM batch_repo.batch_job_execution ORDER BY JOB_EXECUTION_ID desc;
		 * SELECT * FROM batch_repo.batch_step_execution ORDER BY STEP_EXECUTION_ID desc;
		 * 
		 * 
		 */
		
	}
	
	@Bean
	public Job deliveryParcelJob() {
		return this.jobBuilderFactory.get("delivery_parcel_job")
				.start(parcelPackingStep())
				.next(driveToAddressStep())
				.next(giveParcelToCustomerStep())
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBatch1Application.class, args);
	}

}
