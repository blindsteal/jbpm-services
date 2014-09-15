package de.livinglab;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestClientException;
import com.google.common.base.Charsets;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.common.io.Files;

@RestController
public class RcvController {
	
	private final File msgfile = new File(System.getProperty("user.home")+"/msg.xml");

	@Autowired
	MappingEntryRepository mapping;
	
	@Autowired
	JbpmClientService jservice;
	
	@Autowired
	Environment env;
	
	static Logger log = LoggerFactory.getLogger(RcvController.class);
	
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
			log.info("Found mapping: " + mappingsFor2.get(0));
			
			int childId = jservice.findFirstActiveChild(env.getProperty("jbpm.deployment"), mappingsFor2.get(0).getLocalId2());
			SignalResponse sres = jservice.signalProcess(env.getProperty("jbpm.deployment"), childId, env.getProperty("jbpm.signal"));
		}
		else if(msg.getTargetId2() != -1){
			log.info("Message contains targetId2: "+msg.getTargetId2());
			
			int childId = jservice.findFirstActiveChild(env.getProperty("jbpm.deployment"), msg.getTargetId2());
			SignalResponse sres = jservice.signalProcess(env.getProperty("jbpm.deployment"), childId, env.getProperty("jbpm.signal"));
		}
		else {
			log.info("No mapping entry found.");
			
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

			try{
				ProcessInstance started = jservice.startProcess(env.getProperty("jbpm.deployment"), def2Local);
				id2Local = started.getId();
				
				if(!jservice.isVersionEqual(env.getProperty("jbpm.deployment"), def2Local, msg.getV2())){
					
				}
				
				id3Local = jservice.findFirstActiveChild(env.getProperty("jbpm.deployment"), id2Local);
				SignalResponse sres = jservice.signalProcess(env.getProperty("jbpm.deployment"), id3Local, env.getProperty("jbpm.signal"));
			}
			catch(RestClientException e){
				log.info("Local process definition not found.");
			}
		
			MappingEntry me = new MappingEntry( remote, def1Local, id1Local, def2Local, id2Local, def3Local, id3Local,
					msg.getDef1(), msg.getId1(), msg.getDef2(), msg.getId2(), msg.getDef3(), msg.getId3());
			
			log.info(me.toString());
			mapping.save(me);
		}
		
	}

}
