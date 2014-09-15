package de.livinglab;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class InstanceLog {

	private List<InstanceLogEntry> result;

	public List<InstanceLogEntry> getResult() {
		return result;
	}

	public void setResult(List<InstanceLogEntry> result) {
		this.result = result;
	}
}
