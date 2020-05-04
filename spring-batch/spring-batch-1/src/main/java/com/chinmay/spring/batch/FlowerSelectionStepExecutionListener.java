package com.chinmay.spring.batch;

import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

public class FlowerSelectionStepExecutionListener implements StepExecutionListener {

	@Override
	public void beforeStep(StepExecution stepExecution) {
		System.out.println("Executing BEFORE step logic.");
		
	}

	@Override
	public ExitStatus afterStep(StepExecution stepExecution) {
		System.out.println("Executing AFTER step logic.");
		String flowerType = stepExecution.getJobParameters().getString("type");
		System.out.println("AFTER step logic received type as "+flowerType);
		return flowerType.equalsIgnoreCase("roses") ? new ExitStatus("TRIM_REQUIRED") : new ExitStatus("NO_TRIM_REQUIRED");
	}

}
