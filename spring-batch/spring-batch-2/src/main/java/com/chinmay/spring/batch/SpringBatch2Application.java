package com.chinmay.spring.batch;

import java.util.List;

import javax.sql.DataSource;

import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.database.builder.JdbcCursorItemReaderBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.mapping.DefaultLineMapper;
import org.springframework.batch.item.file.transform.DelimitedLineTokenizer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.FileSystemResource;

@SpringBootApplication
@EnableBatchProcessing
public class SpringBatch2Application {

	public static String[] order_columns = new String[] {"order_id", "first_name", "last_name", "email", "cost", "item_id", "item_name", "ship_date"};
	public static String ORDER_SQL = "SELECT order_id, first_name, last_name, email, cost, item_id, item_name, ship_date\n" + 
			"FROM shipped_order ORDER BY order_id";
	
	@Autowired
	public JobBuilderFactory jobBuilderFactory;
	
	@Autowired
	public StepBuilderFactory stepBuilderFactory;
	
	@Autowired
	public DataSource myDataSource;
	
	@Bean
	public ItemReader<Order> myDatabaseItemReader() {
		
		return new JdbcCursorItemReaderBuilder<Order>()
				.dataSource(myDataSource)
				.name("jdbc_cursor_item_reader")
				.sql(ORDER_SQL)
				.rowMapper(new OrderRowMapper())
				.build();
	}
	
	@Bean
	public ItemReader<Order> myCsvItemReader() {
		
		//......................
		//Define the tokenizer, (a)set column names (same as the column names from the csv file)
		DelimitedLineTokenizer tokenizer = new DelimitedLineTokenizer(); //default delimiter comma is applied
		tokenizer.setNames(order_columns); //Tell the tokenizer the columns found in the csv file

		//......................
		//Define the line mapper (a)set the tokenizer and (b)set the field set mapper to get the Order object from the parsed data from csv
		DefaultLineMapper<Order> lineMapper = new DefaultLineMapper<Order>();
		lineMapper.setLineTokenizer(tokenizer);
		lineMapper.setFieldSetMapper(new OrderFieldSetMapper());
		/**
		 * FlatFIleItemREader uses lineMapper to parse the lines form csv file.
		 * ... parses the line using the tokenizer
		 * ... takes parsed tokens and map them to our Order POJO object.
		 * The tokenizer and fieldsetmapper combination gets an Order object from a row in csv.
		 */
		
		//......................
		//Define FlatFileItemReader, (a)set the resource the csv file to read from, (b)set lines to skip and (c)set the line mapper.
		FlatFileItemReader<Order> itemReader = new FlatFileItemReader<Order>();
		itemReader.setResource(new FileSystemResource("data/shipped_orders.csv")); //location is "src\main\resources\data\shipped_orders.csv"
		itemReader.setLinesToSkip(1); //Skip the first lien from the csv file
		itemReader.setLineMapper(lineMapper);
		//......................
		
		return itemReader;
	}

	@Bean
	public Step chunkBasedStep() {
		return this.stepBuilderFactory.get("chunk_based_step")
				.<Order, Order>chunk(3)
				.reader(myDatabaseItemReader())   //.reader(myCsvItemReader())
				.writer(new ItemWriter<Order>() {

					@Override
					public void write(List<? extends Order> items) throws Exception {
						System.out.println(String.format("Received list has %s elements", items.size()));
						items.forEach(System.out::println);
					}
				})
				//.taskExecutor(new SimpleAsyncTaskExecutor())  //chunks will be executed in parallel (faster) if this line is uncommented
				// //.throttleLimit(6) // Maximum these many threads can run in parallel. Default is 4.
				.build();
	}
	
	@Bean
	public ItemReader<String> simpleitemReader() {
		return new MyItemReader();
	}
	
	@Bean
	public Step chunkBasedStaticDataStep() {
		return this.stepBuilderFactory.get("chunk_based_step")
				.<String, String>chunk(6)
				.reader(simpleitemReader())
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
		//return this.jobBuilderFactory.get("chunk_based_job").start(chunkBasedStaticDataStep()).build();
		return this.jobBuilderFactory.get("chunk_based_job").start(chunkBasedStep()).build();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBatch2Application.class, args);
	}

}
