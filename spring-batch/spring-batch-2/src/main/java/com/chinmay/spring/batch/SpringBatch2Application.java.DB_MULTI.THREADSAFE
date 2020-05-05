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
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

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
	public PagingQueryProvider myPagingQueryProvider() throws Exception {
		SqlPagingQueryProviderFactoryBean factory = new SqlPagingQueryProviderFactoryBean();
		factory.setSelectClause("SELECT order_id, first_name, last_name, email, cost, item_id, item_name, ship_date");
		factory.setFromClause("FROM shipped_order");
		factory.setSortKey("order_id");
		factory.setDataSource(myDataSource);
		return factory.getObject();
	}
	
	@Bean
	public ItemReader<Order> itemReader() throws Exception {
		
		return new JdbcPagingItemReaderBuilder<Order>()
				.dataSource(myDataSource)
				.name("jdbc_paging_item_reader")
				.queryProvider(myPagingQueryProvider())
				.rowMapper(new OrderRowMapper())
				.pageSize(10)
				.build();
	}
	
	@Bean
	public Step chunkBasedStep() throws Exception {
		return this.stepBuilderFactory.get("chunk_based_db_multi_step")
				.<Order, Order>chunk(10)
				.reader(itemReader())
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
	
	@Bean Job chunkBasedJob() throws Exception {
		return this.jobBuilderFactory.get("chunk_based_job").start(chunkBasedStep()).build();
	}
	
	public static void main(String[] args) {
		SpringApplication.run(SpringBatch2Application.class, args);
	}

}
