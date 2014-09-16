package de.livinglab;

import java.io.File;

public interface JbpmClientService {

	int findFirstActiveChild(String dep, int instId);
	SignalResponse signalProcess(String dep, int instId, String signal);
	ProcessInstance startProcess(String dep, String def);
	boolean isVersionEqual(String dep, String def, String v2);
	File getProcessFile(String dep, String def, String ver, String basedir);
	String raiseDeploymentVersion(String dep, String basedir);
	void deploy(String repo, String project);
	void undeploy(String dep);
	
}
