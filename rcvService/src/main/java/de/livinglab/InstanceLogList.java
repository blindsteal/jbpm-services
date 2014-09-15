package de.livinglab;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class InstanceLogList {
	@JsonProperty("log-instance-list")
	private List<InstanceLogEntry> instanceLogs;

	public List<InstanceLogEntry> getInstanceLogs() {
		return instanceLogs;
	}

	public void setInstanceLogs(List<InstanceLogEntry> instanceLogs) {
		this.instanceLogs = instanceLogs;
	}
	

}
