package com.cym.model;

import com.cym.sqlhelper.bean.BaseModel;
import com.cym.sqlhelper.config.Table;

/**
 * 嵌套小组用户
 * 
 * @author 陈钇蒙
 *
 */
@Table
public class GroupGroup extends BaseModel {

	String masterGroupId;
	String slaveGroupId;

	public String getMasterGroupId() {
		return masterGroupId;
	}

	public void setMasterGroupId(String masterGroupId) {
		this.masterGroupId = masterGroupId;
	}

	public String getSlaveGroupId() {
		return slaveGroupId;
	}

	public void setSlaveGroupId(String slaveGroupId) {
		this.slaveGroupId = slaveGroupId;
	}

}
