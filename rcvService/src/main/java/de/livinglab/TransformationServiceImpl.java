package de.livinglab;

import java.io.IOException;

import nu.xom.Attribute;
import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Elements;
import nu.xom.Node;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;
import nu.xom.XPathContext;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

@Service
public class TransformationServiceImpl implements TransformationService {

	private static final String DROOLS_NS_URI = "http://www.jboss.org/drools";
	private static final String BPMN_NS_URI = "http://www.omg.org/spec/BPMN/20100524/MODEL";
	
	static Logger log = LoggerFactory.getLogger(TransformationServiceImpl.class);
	private XPathContext context;
	
	@Autowired
	Environment env;
	
	@Override
	public String getInverse(String source) {
		String rcvName = env.getProperty("jbpm.rcvProcess");
		String sendName = env.getProperty("jbpm.sendProcess");
		
		Builder xom = new Builder();
		Document doc = null;
		
		try {
			doc = xom.build(source, null);
		} catch (ParsingException | IOException e) {
			log.error("Error parsing BPMN: "+e.getMessage());
		}
		
		Element root = doc.getRootElement();
		context = XPathContext.makeNamespaceContext(root);
		context.addNamespace("drools", DROOLS_NS_URI);
		context.addNamespace("bpmn2", BPMN_NS_URI);

		Element processNode = (Element) root.query("//bpmn2:process", context).get(0);
		updateAttributeValue(processNode, "name", processNode.getAttributeValue("name") + "-inverse", null);
		updateAttributeValue(processNode, "id", processNode.getAttributeValue("id") + "-inverse", null);
		
		Nodes rcvNodes = root.query("//bpmn2:callActivity[@calledElement='"+rcvName+"']", context);
		for (int i = 0; i < rcvNodes.size(); i++){
			Element rcvNode = (Element) rcvNodes.get(i);
			log.info("Found rcvNode: "+rcvNode.getQualifiedName());
			
			updateAttributeValue(rcvNode, "calledElement", sendName, null);
			updateAttributeValue(rcvNode, "name", "Call "+sendName, null);
		}
		
		Nodes sendNodes = root.query("//bpmn2:callActivity[@calledElement='"+sendName+"']", context);
		for (int i = 0; i < sendNodes.size(); i++){
			Element sendNode = (Element) sendNodes.get(i);
			log.info("Found sendNode: "+sendNode.getQualifiedName());
			
			updateAttributeValue(sendNode, "calledElement", rcvName, null);
			updateAttributeValue(sendNode, "name", "Call "+rcvName, null);
		}
		
		return doc.toXML();
	}
	
	private void updateAttributeValue(Element e, String attrName, String newValue, String nsUri){
		Attribute attr = (nsUri != null) ? e.getAttribute(attrName, nsUri) : e.getAttribute(attrName);
		e.removeAttribute(attr);
		e.addAttribute(new Attribute(attrName, newValue));
		
	}

}
