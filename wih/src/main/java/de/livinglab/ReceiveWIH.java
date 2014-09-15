package de.livinglab;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ReceiveWIH implements WorkItemHandler {
    private static final Logger logger = LoggerFactory.getLogger(ReceiveWIH.class);
	private Path path = Paths.get(System.getProperty("user.home")+"/msg.xml");
	
	@Override
	public void abortWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.warn("ReceiveWIH aborted");
	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("ReceiveWIH started");

        Map<String, Object> results = new HashMap<String, Object>();
        
        List<String> lines = null;
		try {
			lines = Files.readAllLines(path, StandardCharsets.UTF_8);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        String msg = "";
        for(String line : lines){
        	msg += (line + "\n");
        }
        results.put("Message", msg);
        
		logger.info("ReceiveWIH finished. Message: "+msg);
		manager.completeWorkItem(workItem.getId(), results);
	}
}
