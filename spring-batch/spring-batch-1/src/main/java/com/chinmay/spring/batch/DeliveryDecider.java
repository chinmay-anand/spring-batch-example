package com.chinmay.spring.batch;

import java.time.LocalDateTime;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class DeliveryDecider implements JobExecutionDecider {

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		int currentHour = LocalDateTime.now().getHour();
		String result = (currentHour > 9 && currentHour < 23) ? "PRESENT" : "NOT_PRESENT";
		System.out.println("DeliveryDecider: The result is:  " + result +". Current hour is "+ currentHour);
		return new FlowExecutionStatus(result);
	}

}
