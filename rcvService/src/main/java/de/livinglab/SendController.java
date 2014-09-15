package de.livinglab;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

@RestController
public class SendController {
	
	static Logger log = LoggerFactory.getLogger(SendController.class);
	//private final String targetUrl = "http://%1$s/rcv?def1=%2$s&id1=%3$s&def2=%4$s&id2=%5$s&def3=%6$s&id3=%7$s";
	private final String targetUrl = "http://%1$s/rcv";

	
	@Autowired
	Environment env;
	
	private RestTemplate client = new RestTemplate();

	@Autowired
	MappingEntryRepository mapping;
	
	@Autowired
	ObjectSerializerService oss;
	
	@RequestMapping(value = "/send", method = RequestMethod.POST)
	public void receiveMessage(
			@RequestBody String body,
			@RequestParam(value="host") String host){
		
		Message msg = oss.stringToObject(body);
		
		String rcvUrl = String.format(targetUrl, host);
		
		List<MappingEntry> mappingsFor2 = mapping.findByLocalDef2AndLocalId2(msg.getDef2(), msg.getId2());
		if(!mappingsFor2.isEmpty()){
			msg.setTargetId2(mappingsFor2.get(0).getRemoteId2());
		}
		
		log.info("Sending to: " + rcvUrl + ", Msg: " + msg);
		
		client.postForLocation(rcvUrl, msg);
		
	}
}
