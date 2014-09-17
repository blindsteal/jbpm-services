package com.sample;

import org.jbpm.test.JbpmJUnitBaseTestCase;
import org.junit.Test;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.manager.RuntimeEngine;
import org.kie.api.runtime.manager.RuntimeManager;
import org.kie.api.runtime.process.ProcessInstance;

import de.livinglab.ReceiveWIH;
import de.livinglab.SendWIH;

/**
 * This is a sample file to test a process.
 */
public class ProcessTest extends JbpmJUnitBaseTestCase {

	@Test
	public void testProcess() {
		RuntimeManager manager = createRuntimeManager("sample.bpmn", "parent1.bpmn", "parent2.bpmn");
		//RuntimeManager manager = createRuntimeManager("sample2.bpmn");
		RuntimeEngine engine = getRuntimeEngine(null);
		KieSession ksession = engine.getKieSession();
		ksession.getWorkItemManager().registerWorkItemHandler("SendMessage", new SendWIH(ksession));
		ksession.getWorkItemManager().registerWorkItemHandler("ReceiveMessage", new ReceiveWIH());
		
		//ProcessInstance processInstance = ksession.startProcess("com.sample.bpmn.hello");
		ProcessInstance pi = ksession.startProcess("com.sample.bpmn.parent1");
		
		manager.disposeRuntimeEngine(engine);
		manager.close();
	}

}