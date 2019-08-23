package com.jimi.uw_server.constant.sql;

public class FAQSQL {

	public final static String GET_FAQ_BY_PROBLEM_NAME = "SELECT * FROM faq WHERE problem_name = ?";

	public final static String GET_ALL_FAQ = "SELECT * FROM faq";

	public final static String GET_FAQ_BY_KEYWORD = "SELECT * FROM faq WHERE problem_name like ? OR content like ? ";

}
