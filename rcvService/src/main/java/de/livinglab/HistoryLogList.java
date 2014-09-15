package de.livinglab;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown=true)
public class HistoryLogList {

	private List<InstanceLog> historyLogList;

	public List<InstanceLog> getHistoryLogList() {
		return historyLogList;
	}

	public void setHistoryLogList(List<InstanceLog> historyLogList) {
		this.historyLogList = historyLogList;
	}

}
