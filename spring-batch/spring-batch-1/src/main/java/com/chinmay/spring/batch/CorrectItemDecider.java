package com.chinmay.spring.batch;

import java.util.Random;

import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.job.flow.FlowExecutionStatus;
import org.springframework.batch.core.job.flow.JobExecutionDecider;

public class CorrectItemDecider implements JobExecutionDecider {

	@Override
	public FlowExecutionStatus decide(JobExecution jobExecution, StepExecution stepExecution) {
		String result = (new Random().nextFloat() < 0.70f) ? "CORRECT" : "INCORRECT";
		System.out.print("CorrectItemDecider: The item delivered is " + result +"\n");
		return new FlowExecutionStatus(result);
	}

}
