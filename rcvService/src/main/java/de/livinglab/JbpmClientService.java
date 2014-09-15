package de.livinglab;

import java.util.List;

public interface JbpmClientService {

	int findActiveChild(String dep, int instId);
	SignalResponse signalProcess(String dep, int instId, String signal);
	ProcessInstance startProcess(String dep, String def);
	List<InstanceLogEntry> getActive(String dep, String def);
	InstanceLogEntry getLogEntry(String dep, int inst);
	boolean isDeployed(String dep, String def, String version);
	
}
