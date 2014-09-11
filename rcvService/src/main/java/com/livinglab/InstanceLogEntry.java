package com.livinglab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class InstanceLogEntry {
	
	private int id, status, parentProcessInstanceId;
	private String processId, identity, externalId;
	
	public int getId() {
		return id;
	}
	public void setId(int id) {
		this.id = id;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public int getParentProcessInstanceId() {
		return parentProcessInstanceId;
	}
	public void setParentProcessInstanceId(int parentProcessInstanceId) {
		this.parentProcessInstanceId = parentProcessInstanceId;
	}
	public String getProcessId() {
		return processId;
	}
	public void setProcessId(String processId) {
		this.processId = processId;
	}
	public String getIdentity() {
		return identity;
	}
	public void setIdentity(String identity) {
		this.identity = identity;
	}
	public String getExternalId() {
		return externalId;
	}
	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

}
