package de.livinglab;

import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.EntityEnclosingMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.jbpm.bpmn2.handler.WorkItemHandlerRuntimeException;
import org.kie.api.runtime.KieSession;
import org.kie.api.runtime.process.WorkItem;
import org.kie.api.runtime.process.WorkItemHandler;
import org.kie.api.runtime.process.WorkItemManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("restriction")
public class SendWIH implements WorkItemHandler {
	private static final Logger logger = LoggerFactory.getLogger(SendWIH.class);
	private boolean logThrownException = false;

	private int connectTimeout = 20000;
	private int readTimeout = 20000;
	private String urlString = "http://localhost:8010/send?host=%1$s";
	private String defaultReceiver = "localhost:8010";
	private KieSession ksession;

	private ObjectSerializerService oss = new ObjectSerializerServiceImpl();

	public SendWIH(KieSession ksession) {
		this.ksession = ksession;
	}

	@Override
	public void abortWorkItem(WorkItem arg0, WorkItemManager arg1) {
		logger.warn("SendWIH aborted");

	}

	@Override
	public void executeWorkItem(WorkItem workItem, WorkItemManager manager) {
		logger.info("SendWIH started");

		Map<String, Object> params = workItem.getParameters();
		long id3 = workItem.getProcessInstanceId();
		String def3 = ksession.getProcessInstance(id3).getProcessId();
		String id2 = (String) params.get("id2");
		String def2 = ksession.getProcessInstance(Long.parseLong(id2))
				.getProcess().getId();
		String v2 = ksession.getProcessInstance(Long.parseLong(id2))
				.getProcess().getVersion();
		String id1 = (String) params.get("id1");
		String def1 = "";
		int id1i = -1;
		if(params.get("id1") != null){
			def1  = ksession.getProcessInstance(Long.parseLong(id1))
					.getProcess().getId();
			id1i = Integer.parseInt(id1);
		}
		String host = (params.containsKey("Host")) ? (String) params
				.get("Host") : defaultReceiver;

		logger.info("Send called by " + def3 + " " + id3 + " / " + def2 + " "
				+ id2 + " / " + def1 + " " + id1);

		String source2 = getProcessSource(Long.parseLong(id2));

		Message msg = new Message(def1, def2, def3, id1i,
				Integer.parseInt(id2), new Long(id3).intValue(), v2,
				(String) params.get("Message"), -1, source2);
		String serializedMsg = oss.objectToString(msg);

		HttpClient httpclient = new HttpClient();
		httpclient.getHttpConnectionManager().getParams()
				.setConnectionTimeout(connectTimeout);
		httpclient.getHttpConnectionManager().getParams()
				.setSoTimeout(readTimeout);

		String targetUrl = String.format(urlString, host);
		logger.info("Sending to " + targetUrl + " msg: "
				+ (String) params.get("Message"));
		HttpMethod method = new PostMethod(targetUrl);
		setBody(method, serializedMsg);

		try {
			int responseCode = httpclient.executeMethod(method);
			Map<String, Object> results = new HashMap<String, Object>();
			if (responseCode >= 200 && responseCode < 300) {
				method.getResponseBody();
				postProcessResult(method.getResponseBodyAsString(), results);
				results.put("StatusMsg", "request to endpoint " + urlString
						+ " successfully completed " + method.getStatusText());
			} else {
				logger.warn(
						"Unsuccessful response from REST server (status {}, endpoint {}, response {}",
						responseCode, urlString,
						method.getResponseBodyAsString());
				results.put("StatusMsg",
						"endpoint " + urlString + " could not be reached: "
								+ method.getResponseBodyAsString());
			}
			results.put("Status", responseCode);

			// notify manager that work item has been completed
			manager.completeWorkItem(workItem.getId(), results);
		} catch (Exception e) {
			handleException(e);
		} finally {
			method.releaseConnection();
			logger.info("SendWIH finished");
		}
	}

	protected void setBody(HttpMethod theMethod, String msg) {
		try {
			((EntityEnclosingMethod) theMethod)
					.setRequestEntity(new StringRequestEntity(msg,
							"text/plain", null));
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("Cannot set body for REST request "
					+ theMethod, e);
		}
	}

	protected void postProcessResult(String result, Map<String, Object> results) {
		results.put("Result", result);
		logger.info(result);
	}

	protected void handleException(Throwable cause,
			Map<String, Object> handlerInfoMap) {
		String service = (String) handlerInfoMap.get("Interface");
		String operation = (String) handlerInfoMap.get("Operation");

		if (this.logThrownException) {
			String message;
			if (service != null) {
				message = this.getClass().getSimpleName()
						+ " failed when calling " + service + "." + operation;
			} else {
				message = this.getClass().getSimpleName()
						+ " failed while trying to complete the task.";
			}
			logger.error(message, cause);

		} else {
			WorkItemHandlerRuntimeException wihRe = new WorkItemHandlerRuntimeException(
					cause);
			for (String key : handlerInfoMap.keySet()) {
				wihRe.setInformation(key, handlerInfoMap.get(key));
			}
			wihRe.setInformation(
					WorkItemHandlerRuntimeException.WORKITEMHANDLERTYPE, this
							.getClass().getSimpleName());
			throw wihRe;
		}
	}

	private String getProcessSource(long id) {
		String content = "";
		if(ksession.getProcessInstance(id).getProcess()
					.getResource() == null){
			logger.info("Could not get process resource for: "+ ksession.getProcessInstance(id).getProcess());
			return content;
		}
		try {
			Reader r = ksession.getProcessInstance(id).getProcess()
					.getResource().getReader();
			int buf;
			while ((buf = r.read()) != -1) {
				content += (char) buf;
			}
		} catch (IOException e1) {
			logger.error(e1.getMessage());
		}
		return content;
	}

	protected void handleException(Throwable cause) {
		handleException(cause, new HashMap<String, Object>());
	}

}
