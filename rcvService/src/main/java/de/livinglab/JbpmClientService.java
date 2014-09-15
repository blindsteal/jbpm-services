package de.livinglab;

public interface JbpmClientService {

	int findFirstActiveChild(String dep, int instId);
	SignalResponse signalProcess(String dep, int instId, String signal);
	ProcessInstance startProcess(String dep, String def);
	boolean isVersionEqual(String dep, String def, String v2);
	
}
