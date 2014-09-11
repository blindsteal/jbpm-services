package com.livinglab;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.tomcat.util.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

@RestController
public class RcvController {
	
	static Logger log = LoggerFactory.getLogger(RcvController.class);
	private final File msgfile = new File(System.getProperty("user.home")+"/msg.xml");
	private final String startUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/process/%2$s/start";
	private final String logUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/history/process/%2$s";
	private final String instLogUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/history/instance/%2$s";
	private final String signalUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/process/instance/%2$s/signal?signal=%3$s";
	private final String childUrl ="http://localhost:8080/jbpm-console/rest/runtime/%1$s/history/instance/%2$s/child";
	
	@Autowired
	Environment env;

	@Autowired
	MappingEntryRepository mapping;
	
	private RestTemplate client = new RestTemplate();
	
	@RequestMapping(value = "/rcv", method = RequestMethod.POST)
	public void receiveMessage(HttpServletRequest req,
			@RequestBody Message msg){
		
		try {
			Files.write(msg.getMsg(), msgfile, Charsets.UTF_8);
		} catch (IOException e) {
			log.error(e.getMessage());
		}
		
		List<MappingEntry> mappingsFor2 = mapping.findByRemoteDef2AndRemoteId2(msg.getDef2(), msg.getId2());
		if(!mappingsFor2.isEmpty()) {
			int childId = findActiveChild(env.getProperty("jbpm.deployment"), mappingsFor2.get(0).getLocalId2());
			SignalResponse sres = signalProcess(env.getProperty("jbpm.deployment"), childId, env.getProperty("jbpm.signal"));
		}
		else if(msg.getTargetId2() != -1){
			int childId = findActiveChild(env.getProperty("jbpm.deployment"), msg.getTargetId2());
			SignalResponse sres = signalProcess(env.getProperty("jbpm.deployment"), childId, env.getProperty("jbpm.signal"));
		}
		else {
			String remote = req.getRemoteAddr();

			String def1Local = "", def2Local = "", def3Local = "";
			int id1Local = -1, id2Local = -1, id3Local = -1;
			
			List<String> split = Lists.newArrayList(Splitter.on("-").trimResults().split(msg.getDef2()));
			if(!split.contains("inverse")) {
				def2Local = msg.getDef2() + "-inverse";
			}
			else{
				def2Local = split.get(0);
			}

			ProcessInstance started = startProcess(env.getProperty("jbpm.deployment"), def2Local);
			id2Local = started.getId();
			
			id3Local = findActiveChild(env.getProperty("jbpm.deployment"), id2Local);
			SignalResponse sres = signalProcess(env.getProperty("jbpm.deployment"), id3Local, env.getProperty("jbpm.signal"));
		
			MappingEntry me = new MappingEntry( remote, def1Local, id1Local, def2Local, id2Local, def3Local, id3Local,
					msg.getDef1(), msg.getId1(), msg.getDef2(), msg.getId2(), msg.getDef3(), msg.getId3());
			
			log.info(me.toString());
			mapping.save(me);
		}
		
	}
	
	private int findActiveChild(String dep, int instId) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + encodeCreds(env.getProperty("jbpm.user"), env.getProperty("jbpm.pw")));
		headers.add("Accept", "application/json");
		HttpEntity<String> req = new HttpEntity<String>(headers);
		ResponseEntity<InstanceLog> res = client.exchange(String.format(childUrl, dep, instId), HttpMethod.GET, req, InstanceLog.class);
		
		List<InstanceLogEntry> active = Lists.newArrayList();
		for(InstanceLogEntry entry : res.getBody().getResult()) {
			if(entry.getStatus() == 1) {
				active.add(entry);
			}
		}
		
		log.info("Found child: " + instId + ": " + active.get(0).getId());
		return active.get(0).getId();
	}

	private SignalResponse signalProcess(String dep, int instId, String signal) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + encodeCreds(env.getProperty("jbpm.user"), env.getProperty("jbpm.pw")));
		headers.add("Accept", "application/json");
		HttpEntity<String> req = new HttpEntity<String>(headers);
		ResponseEntity<SignalResponse> res = client.exchange(String.format(signalUrl, dep, instId, signal), HttpMethod.POST, req, SignalResponse.class);
		
		log.info("Signalling of " + instId + ": " + res.getBody().getStatus());
		return res.getBody();
	}
	
	private ProcessInstance startProcess(String dep, String def) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + encodeCreds(env.getProperty("jbpm.user"), env.getProperty("jbpm.pw")));
		headers.add("Accept", "application/json");
		HttpEntity<String> req = new HttpEntity<String>(headers);
		ResponseEntity<ProcessInstance> res = client.exchange(String.format(startUrl, dep, def), HttpMethod.POST, req, ProcessInstance.class);
		
		log.info("Started process "+res.getBody().getProcessId()+" (id: "+res.getBody().getId()+")");
		return res.getBody();
	}

	private List<InstanceLogEntry> getActive(String dep, String def) {
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + encodeCreds(env.getProperty("jbpm.user"), env.getProperty("jbpm.pw")));
		headers.add("Accept", "application/json");
		HttpEntity<String> req = new HttpEntity<String>(headers);
		ResponseEntity<InstanceLog> res = client.exchange(String.format(logUrl, dep, def), HttpMethod.GET, req, InstanceLog.class);
		
		List<InstanceLogEntry> active = Lists.newArrayList();
		for(InstanceLogEntry entry : res.getBody().getResult()) {
			if(entry.getStatus() == 1) {
				active.add(entry);
			}
		}
		log.info("Found " + active.size() + " active instances of " + def);
		return active;
	}
	
	private InstanceLogEntry getLogEntry(String dep, int inst){
		HttpHeaders headers = new HttpHeaders();
		headers.add("Authorization", "Basic " + encodeCreds(env.getProperty("jbpm.user"), env.getProperty("jbpm.pw")));
		headers.add("Accept", "application/json");
		HttpEntity<String> req = new HttpEntity<String>(headers);
		ResponseEntity<InstanceLog> res = client.exchange(String.format(instLogUrl, dep, inst), HttpMethod.GET, req, InstanceLog.class);
		return res.getBody().getResult().get(0);
	}

	private String encodeCreds(String user, String pw){
		String plainCreds = user+":"+pw;
		byte[] base64CredsBytes = Base64.encodeBase64(plainCreds.getBytes());
		String base64Creds = new String(base64CredsBytes);
		return base64Creds;
	}

}
