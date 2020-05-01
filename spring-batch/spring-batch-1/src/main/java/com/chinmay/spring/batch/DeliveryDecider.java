package com.chinmay.spring.batch;

import java.time.LocalDateTime;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class DeliveryDecider implements JobExecutionDecider {

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		String result =  (LocalDateTime.now().getHour() < 12) ? "PRESENT" : "NOT_PRESENT";
		return new FlowExecutionStatus(result);
	}

}
