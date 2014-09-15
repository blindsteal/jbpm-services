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
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.google.common.collect.Lists;

@Service
public class JbpmClientServiceImpl implements JbpmClientService {
	private final String startUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/process/%2$s/start";
	private final String signalUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/process/instance/%2$s/signal?signal=%3$s";
	private final String childUrl = "http://localhost:8080/jbpm-console/rest/history/instance/%1$s/child";
	private final String definitionHistoryUrl = "http://localhost:8080/jbpm-console/rest/history/process/%1$s";

	@Autowired
	RestTemplate client;

	@Autowired
	Environment env;

	static Logger log = LoggerFactory.getLogger(JbpmClientServiceImpl.class);

	@Override
	public int findFirstActiveChild(String dep, int instId) throws RestClientException{
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<HistoryLogList> res = client.exchange(
				String.format(childUrl, instId), HttpMethod.GET, req,
				HistoryLogList.class);

		List<InstanceLogEntry> active = Lists.newArrayList();
		for (InstanceLog ilog : res.getBody().getHistoryLogList()) {
			log.info(ilog.getEntry().getId()+ " = "+ilog.getEntry().getStatus());
			if (ilog.getEntry().getStatus() == 1) {
				active.add(ilog.getEntry());
			}
		}

		log.info("Found child: " + instId + ": " + active.get(0).getId());
		return active.get(0).getId();
	}

	@Override
	public SignalResponse signalProcess(String dep, int instId, String signal) throws RestClientException{
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<SignalResponse> res = client.exchange(
				String.format(signalUrl, dep, instId, signal), HttpMethod.POST,
				req, SignalResponse.class);

		log.info("Signalling of " + instId + ": " + res.getBody().getStatus());
		return res.getBody();
	}

	@Override
	public ProcessInstance startProcess(String dep, String def) throws RestClientException{
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<ProcessInstance> res = client.exchange(
				String.format(startUrl, dep, def), HttpMethod.POST, req,
				ProcessInstance.class);

		log.info("Started process " + res.getBody().getProcessId() + " (id: "
				+ res.getBody().getId() + ")");
		return res.getBody();
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
	public boolean isVersionEqual(String dep, String def, String v2) {
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<HistoryLogList> res = client.exchange(
				String.format(definitionHistoryUrl, def), HttpMethod.GET, req,
				HistoryLogList.class);
		String localVer = res.getBody().getHistoryLogList().get(res.getBody().getHistoryLogList().size()-1).getEntry().getProcessVersion();
		if(localVer.equals(v2)){
			log.info("Process "+def+" present in matching version: "+v2);
			return true;
		}
		log.info("Process "+def+" has incorrect version: "+localVer+" (sent: "+v2+")");
		return false;
	}
}
