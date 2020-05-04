package com.chinmay.spring.batch;

import java.sql.ResultSet;
import java.sql.SQLException;

import org.springframework.jdbc.core.RowMapper;

public class OrderRowMapper implements RowMapper<Order>{

	//public static String ORDER_SQL = "SELECT order_id, first_name, last_name, email, cost, item_id, item_name, ship_date\n" + 
	//          "FROM shipped_order ORDER BY order_id";

	//These tokens / column headers are defined in the main class

	@Override
	public Order mapRow(ResultSet rs, int rowNum) throws SQLException {
		Order order = new Order();
		order.setOrderId(rs.getLong("order_id")); // These strings are the columns of table SHIPPED_ORDER.
		order.setCost(rs.getBigDecimal("cost"));
		order.setFirstName(rs.getString("first_name"));
		order.setLastName(rs.getString("last_name"));
		order.setEmail(rs.getString("email"));
		order.setItemId(rs.getString("item_id"));
		order.setItemName(rs.getString("item_name"));
		order.setShipDate(rs.getDate("ship_date"));
		
		return order;
	}
}
