package com.chinmay.spring.batch;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.NonTransientResourceException;
import org.springframework.batch.item.ParseException;
import org.springframework.batch.item.UnexpectedInputException;

public class MyItemReader implements ItemReader<String>{
	
	private List<String> dataSet = new ArrayList<>();
	private Iterator<String> iterator;
	
	public MyItemReader() {
		for(int i=1; i<=50; i++) {
			this.dataSet.add(i+"");
		}
		//this.dataSet.add("1");
		this.iterator = this.dataSet.iterator();
	}

	@Override
	public String read() throws Exception, UnexpectedInputException, ParseException, NonTransientResourceException {
		return iterator.hasNext() ? iterator.next() : null;
	}

	
	
}
