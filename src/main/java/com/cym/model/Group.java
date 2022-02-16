package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.InitValue;
import com.cym.sqlhelper.config.Table;

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
