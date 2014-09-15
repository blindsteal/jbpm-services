package de.livinglab;

import java.util.List;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;

@Service
public class JbpmClientServiceImpl implements JbpmClientService {
	private final String startUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/process/%2$s/start";
	private final String logUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/history/process/%2$s";
	private final String instLogUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/history/instance/%2$s";
	private final String signalUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/process/instance/%2$s/signal?signal=%3$s";
	private final String childUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/history/instance/%2$s/child";
	private final String definitionHistoryUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/history/process/%2$s";

	@Autowired
	RestTemplate client;

	@Autowired
	Environment env;

	static Logger log = LoggerFactory.getLogger(JbpmClientServiceImpl.class);

	@Override
	public int findActiveChild(String dep, int instId) {
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<InstanceLog> res = client.exchange(
				String.format(childUrl, dep, instId), HttpMethod.GET, req,
				InstanceLog.class);

		List<InstanceLogEntry> active = Lists.newArrayList();
		for (InstanceLogEntry entry : res.getBody().getResult()) {
			if (entry.getStatus() == 1) {
				active.add(entry);
			}
		}

		log.info("Found child: " + instId + ": " + active.get(0).getId());
		return active.get(0).getId();
	}

	@Override
	public SignalResponse signalProcess(String dep, int instId, String signal) {
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<SignalResponse> res = client.exchange(
				String.format(signalUrl, dep, instId, signal), HttpMethod.POST,
				req, SignalResponse.class);

		log.info("Signalling of " + instId + ": " + res.getBody().getStatus());
		return res.getBody();
	}

	@Override
	public ProcessInstance startProcess(String dep, String def) {
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<ProcessInstance> res = client.exchange(
				String.format(startUrl, dep, def), HttpMethod.POST, req,
				ProcessInstance.class);

		log.info("Started process " + res.getBody().getProcessId() + " (id: "
				+ res.getBody().getId() + ")");
		return res.getBody();
	}

	@Override
	public List<InstanceLogEntry> getActive(String dep, String def) {
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<InstanceLog> res = client.exchange(
				String.format(logUrl, dep, def), HttpMethod.GET, req,
				InstanceLog.class);

		List<InstanceLogEntry> active = Lists.newArrayList();
		for (InstanceLogEntry entry : res.getBody().getResult()) {
			if (entry.getStatus() == 1) {
				active.add(entry);
			}
		}
		log.info("Found " + active.size() + " active instances of " + def);
		return active;
	}

	@Override
	public InstanceLogEntry getLogEntry(String dep, int inst) {
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<InstanceLog> res = client.exchange(
				String.format(instLogUrl, dep, inst), HttpMethod.GET, req,
				InstanceLog.class);
		return res.getBody().getResult().get(0);
	}

	private HttpEntity<String> newEntityWithHeaders() {
		HttpHeaders headers = new HttpHeaders();
		headers.add(
				"Authorization",
				"Basic "
						+ encodeCreds(env.getProperty("jbpm.user"),
								env.getProperty("jbpm.pw")));
		headers.add("Accept", "application/json");
		HttpEntity<String> req = new HttpEntity<String>(headers);
		return req;
	}

	private String encodeCreds(String user, String pw) {
		String plainCreds = user + ":" + pw;
		byte[] base64CredsBytes = Base64.encodeBase64(plainCreds.getBytes());
		String base64Creds = new String(base64CredsBytes);
		return base64Creds;
	}

	@Override
	public boolean isDeployed(String dep, String def, String version) {
		HttpEntity<String> req = newEntityWithHeaders();
		log.info("requesting "+String.format(definitionHistoryUrl, dep, def));
		ResponseEntity<InstanceLogList> res = client.exchange(
				String.format(definitionHistoryUrl, dep, def), HttpMethod.GET, req,
				InstanceLogList.class);

		int latestEntry = res.getBody().getInstanceLogs().size()-1;
		if(latestEntry != -1){
			String instanceVersion = res.getBody().getInstanceLogs().get(latestEntry).getProcessVersion();
			if(instanceVersion.equals(version)){
				log.info(def+" "+version+" is present in deployment "+dep);
				return true;
			}
			log.info("Version "+version+" of "+def+" was not found (present: "+instanceVersion+")");
		}
		log.info(def+" "+version+" was not found in deployment "+dep);
		return false;
	}
}
