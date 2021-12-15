package com.cym.model;

import cn.craccd.sqlHelper.bean.BaseModel;
import cn.craccd.sqlHelper.config.Table;

/**
 * 小组
 * 
 * @author 陈钇蒙
 *
 */
@Table
public class Group extends BaseModel {
	// 小组名
	String name;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	
}
