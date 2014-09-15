package de.livinglab;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown=true)
public class InstanceLog {
	@JsonProperty("process-instance-log")
	private InstanceLogEntry entry;

	public InstanceLogEntry getEntry() {
		return entry;
	}

	public void setEntry(InstanceLogEntry entry) {
		this.entry = entry;
	}


}
