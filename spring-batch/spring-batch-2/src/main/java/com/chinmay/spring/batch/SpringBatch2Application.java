package com.chinmay.spring.batch;

import java.util.List;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.SimpleAsyncTaskExecutor;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatch2Application {

	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Bean
	public ItemReader<String> itemReader() {
		return new MyItemReader();
	}
	
	@Bean
	public Step chunkBasedStep() {
		return this.stepBuilderFactory.get("chunk_based_step")
				.<String, String>chunk(6)
				.reader(itemReader())
				.writer(new ItemWriter<String>() {

					@Override
					public void write(List<? extends String> items) throws Exception {
						System.out.println(String.format("Received list has %s elements", items.size()));
						items.forEach(System.out::println);
					}
				})
				//.taskExecutor(new SimpleAsyncTaskExecutor())  //chunks will be executed in parallel (faster) if this line is uncommented
				// //.throttleLimit(6) // Maximum these many threads can run in parallel. Default is 4.
				.build();
	}
	
	@Bean Job chunkBasedJob() {
		return this.jobBuilderFactory.get("chunk_based_job").start(chunkBasedStep()).build();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBatch2Application.class, args);
	}

}
