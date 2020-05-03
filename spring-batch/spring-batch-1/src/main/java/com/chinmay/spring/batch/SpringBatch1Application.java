package com.chinmay.spring.batch;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.core.job.builder.FlowBuilder;
import org.springframework.batch.core.job.flow.Flow;
import org.springframework.batch.core.job.flow.JobExecutionDecider;
import org.springframework.batch.core.job.flow.support.SimpleFlow;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties.Sentinel;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatch1Application {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public JobExecutionDecider correctItemDecider() {
		return new CorrectItemDecider();
	}
	
	@Bean
	public JobExecutionDecider deliveryDecider() {
		return new DeliveryDecider();
	}
	
	@Bean
	public Flow deliveryFlow() {
		/**
		 * Externalize the delivery portion of "delivery_package_job" to reuse in "prepare_flower_job"
		 * Prune the desired sections from delivery job and use it here in the flow  
		 */
		return new FlowBuilder<SimpleFlow>("delivery_flow")
			.start(driveToAddressStep())
				.on("FAILED").fail() //stop() //to(storePackageStep())  //To reach this failure condition chaange the flag in "driveToAddressStep() to throw exception and fail there.
			.from(driveToAddressStep())
				.on("*").to(deliveryDecider())
					.on("NOT_PRESENT").to(leaveAtDoorStep())
				.from(deliveryDecider())
					.on("PRESENT").to(giveParcelToCustomerStep())
						.next(correctItemDecider()).on("CORRECT").to(thankCustomerStep())
						.from(correctItemDecider()).on("INCORRECT").to(giveRefundStep())
			.build();
	}
	
	/**
	 * Nested Job:
	 * 1. Define a new step "sendInvoiceStep() and Define a new job "billingJob()" using this step.
	 * 2. Create a new step out of the billingJob() which can be used inside another preexisting job to introduce the billing funtionality.
	 * Job in a another Job - hence nested job
	 * @return
	 */
	@Bean
	public Step sendInvoiceStep() {
		return this.stepBuilderFactory.get("send_invoice_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Sending the invoice to customer.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Job billingJob() {
		/**
		 * For our example this is a single step job
		 * In realtime scenario it can be a complex job with multiple steps and transitions
		 */
		return this.jobBuilderFactory.get("billing_job").start(sendInvoiceStep()).build();
	}
	
	@Bean
	public Step billingJobStep() {
		/**
		 * This is an example of step being prepared from a probably complex job
		 */
		return this.stepBuilderFactory.get("billing_job_step").job(billingJob()).build();
	}
	
	@Bean
	public StepExecutionListener selectFlowerStepListener() {
		return new FlowerSelectionStepExecutionListener();
	}
	
	@Bean
	public Step selectFlowersStep() {
		return this.stepBuilderFactory.get("select_flowers_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Gathering flowers for order.");
				return RepeatStatus.FINISHED;
			}
		}).listener(selectFlowerStepListener()).build();
	}
	
	@Bean
	public Step removeThornsStep() {
		return this.stepBuilderFactory.get("remove_thorns_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Remove thorns form roses.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step arrangeFlowersStep() {
		return this.stepBuilderFactory.get("arrange_flowers_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Arrange the flowers.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}

	@Bean
	public Job prepareFlowersJob() {
		return this.jobBuilderFactory.get("prepare_flowers_job")
					.start(selectFlowersStep())
						.on("TRIM_REQUIRED").to(removeThornsStep()).next(arrangeFlowersStep())
					.from(selectFlowersStep())
						.on("NO_TRIM_REQUIRED").to(arrangeFlowersStep())
					.from(arrangeFlowersStep()).on("*").to(deliveryFlow())
					.next(billingJobStep())
					.end()
					.build();
	}
	
	@Bean
	public Step giveRefundStep() {
		return this.stepBuilderFactory.get("give_refund_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("PRocessing refund to the customer as correct item was not delivered.");
				return RepeatStatus.FINISHED;
			}
		}).build();
	}
	
	@Bean
	public Step thankCustomerStep() {
		return this.stepBuilderFactory.get("thank_customer_step").tasklet(new Tasklet() {
			
			@Override
			public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
				System.out.println("Thanking customer as corrent item has been delivered.");
				return RepeatStatus.FINISHED;
			}
		}).build();
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
				if (GOT_LOST) throw new RuntimeException("Got lost while drive to the address!!!");
				
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
		 * The powershell script "run_delivery_job.ps1" allows us to restart a FAILED / STOPPED job as it starts with same parameters with in a day, 
		 * 	but "rerun_delivery_job.ps1" always starts a new job as it adds a current time parameter which is always different from invocation to invocation. 
		 *
		 *	Upon failure
		 * 		.on("FAILED").to(storePackageStep()) // exits with execution status of Step as ABANDONED and Job as COMPLETED, so that job can not be restarted.
		 * 		.on("FAILED").stop()) // exits with execution status of Step as FAILED and Job as STOPPED, this job can be restarted.
		 * 		.on("FAILED").fail()) // exits with execution status of Step as FAILED and Job as FAILED, this job can be restarted. This is a better status indicator
		 */
		
	}
	
	@Bean
	public Job deliveryParcelJob() {
		return this.jobBuilderFactory.get("delivery_parcel_job")
				.start(parcelPackingStep())
				.on("*").to(deliveryFlow())
				.next(billingJobStep())
				.end()
				.build();
	}

	public static void main(String[] args) {
		SpringApplication.run(SpringBatch1Application.class, args);
	}

}
