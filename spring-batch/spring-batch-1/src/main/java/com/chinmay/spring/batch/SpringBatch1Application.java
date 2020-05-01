package com.chinmay.spring.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
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
	public JobExecutionDecider decider() {
		return new DeliveryDecider();
	}
	
	@Bean
	public Step leaveAtDoorStep() {
		return this.stepBuilderFactory.get("leave_at_door_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Leaving the parcel at the door.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step giveParcelToCustomerStep() {
		return this.stepBuilderFactory.get("give_parcel_to_customer_step.").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("STEP-4:Parcel successfully delivered to customer.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Step storePackageStep() {
		return this.stepBuilderFactory.get("store_packae_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("STEP-3: Storing the package while customer address is being located");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step driveToAddressStep() {
		
		boolean GOT_LOST=false; //Setting this flag to "true" causes this step to fail by throwing a RuntimeException
		return this.stepBuilderFactory.get("drive_to_address_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				if (GOT_LOST) throw new RuntimeException("Got lost while drive to teh address!!!");
				
				System.out.println("STEP-2: Successfully arrived at the address.");
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
				System.out.println(String.format("STEP-1: The %s has been packed on %s.", item, date));
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
		 * The powershell script "rerun_delivery_job.ps1" adds current time to the job parameter making it a new job and it gets always called.
		 * 
		 * If you want to run without any parameter change invoke "run_delivery_job.ps1", which does nto add any current time param.
		 * This script "run_delivery_job.ps1" starts from the failed step onwards provided the job execution shows failed status.
		 *      and job will remain in failed state only if we have not handled failed condition (i.e. on("FAILED") is nto there in the code). 
		 * 
		 */
		
	}
	
	@Bean
	public Job deliveryParcelJob() {
		return this.jobBuilderFactory.get("delivery_parcel_job")
				.start(parcelPackingStep())
				.next(driveToAddressStep())
					.on("FAILED").to(storePackageStep())  //To reach this failure condition chaange the flag in "driveToAddressStep() to throw exception and fail there.
				.from(driveToAddressStep())
					.on("*").to(decider())
						.on("PRESENT").to(giveParcelToCustomerStep())
					.from(decider())
						.on("NOT_PRESENT").to(leaveAtDoorStep())
				.end()
				.build();
		/*
		 * ##//SEQUENTIAL FLOW OF STEPS TO VERiFY RESTARTABLE JOBS
		return this.jobBuilderFactory.get("delivery_parcel_job")
				.start(parcelPackingStep())
				.next(driveToAddressStep())
				.next(giveParcelToCustomerStep()).build();
		*/
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBatch1Application.class, args);
	}

}
