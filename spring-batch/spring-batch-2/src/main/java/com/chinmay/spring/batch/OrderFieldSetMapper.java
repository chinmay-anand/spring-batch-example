package com.chinmay.spring.batch;

import org.springframework.batch.item.file.mapping.FieldSetMapper;
import org.springframework.batch.item.file.transform.FieldSet;
import org.springframework.validation.BindException;

public class OrderFieldSetMapper implements FieldSetMapper<Order>{

	//String[] order_columns = new String[] {"order_id", "first_name", "last_name", "email", "cost", "item_id", "item_name", "ship_date"};
	//These tokens / column headers are defined in the main class

	@Override
	public Order mapFieldSet(FieldSet fieldSet) throws BindException {
		Order order = new Order();
		order.setOrderId(fieldSet.readLong("order_id")); // These strings are the tokens set as column names in the tokenizer that was set to line mapper.
		order.setCost(fieldSet.readBigDecimal("cost"));
		order.setFirstName(fieldSet.readString("first_name"));
		order.setLastName(fieldSet.readString("last_name"));
		order.setEmail(fieldSet.readString("email"));
		order.setItemId(fieldSet.readString("item_id"));
		order.setItemName(fieldSet.readString("item_name"));
		order.setShipDate(fieldSet.readDate("ship_date"));
		
		return order;
	}
}
