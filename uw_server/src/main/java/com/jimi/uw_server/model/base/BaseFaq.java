package com.jimi.uw_server.model.base;

import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.IBean;

/**
 * Generated by JFinal, do not modify this file.
 */
@SuppressWarnings({"serial", "unchecked"})
public abstract class BaseFaq<M extends BaseFaq<M>> extends Model<M> implements IBean {

	public M setId(java.lang.Integer id) {
		set("id", id);
		return (M)this;
	}
	
	public java.lang.Integer getId() {
		return getInt("id");
	}

	public M setProblemName(java.lang.String problemName) {
		set("problem_name", problemName);
		return (M)this;
	}
	
	public java.lang.String getProblemName() {
		return getStr("problem_name");
	}

	public M setResultHtml(java.lang.String resultHtml) {
		set("result_html", resultHtml);
		return (M)this;
	}
	
	public java.lang.String getResultHtml() {
		return getStr("result_html");
	}

	public M setContent(java.lang.String content) {
		set("content", content);
		return (M)this;
	}
	
	public java.lang.String getContent() {
		return getStr("content");
	}

}