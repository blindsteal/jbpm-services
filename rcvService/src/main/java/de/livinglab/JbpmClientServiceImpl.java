package de.livinglab;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.ParsingException;
import nu.xom.Text;
import nu.xom.XPathContext;

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
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

@Service
public class JbpmClientServiceImpl implements JbpmClientService {
	private final String startUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/process/%2$s/start";
	private final String signalUrl = "http://localhost:8080/jbpm-console/rest/runtime/%1$s/process/instance/%2$s/signal?signal=%3$s";
	private final String childUrl = "http://localhost:8080/jbpm-console/rest/history/instance/%1$s/child";
	private final String definitionHistoryUrl = "http://localhost:8080/jbpm-console/rest/history/process/%1$s";
	private final String undeployUrl = "http://localhost:8080/jbpm-console/rest/deployment/%1$s/undeploy";
	private final String mvnDeployUrl = "http://localhost:8080/jbpm-console/rest/repositories/%1$s/projects/%2$s/maven/deploy";

	@Autowired
	RestTemplate client;

	@Autowired
	Environment env;

	static Logger log = LoggerFactory.getLogger(JbpmClientServiceImpl.class);

	@Override
	public int findFirstActiveChild(String dep, int instId)
			throws RestClientException {
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<HistoryLogList> res = client.exchange(
				String.format(childUrl, instId), HttpMethod.GET, req,
				HistoryLogList.class);

		List<InstanceLogEntry> active = Lists.newArrayList();
		for (InstanceLog ilog : res.getBody().getHistoryLogList()) {
			log.info(ilog.getEntry().getId() + " = "
					+ ilog.getEntry().getStatus());
			if (ilog.getEntry().getStatus() == 1) {
				active.add(ilog.getEntry());
			}
		}

		log.info("Found child: " + instId + ": " + active.get(0).getId());
		return active.get(0).getId();
	}

	@Override
	public SignalResponse signalProcess(String dep, int instId, String signal)
			throws RestClientException {
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<SignalResponse> res = client.exchange(
				String.format(signalUrl, dep, instId, signal), HttpMethod.POST,
				req, SignalResponse.class);

		log.info("Signalling of " + instId + ": " + res.getBody().getStatus());
		return res.getBody();
	}

	@Override
	public ProcessInstance startProcess(String dep, String def)
			throws RestClientException {
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
		String localVer = res.getBody().getHistoryLogList()
				.get(res.getBody().getHistoryLogList().size() - 1).getEntry()
				.getProcessVersion();
		if (localVer.equals(v2)) {
			log.info("Process " + def + " present in matching version: " + v2);
			return true;
		}
		log.info("Process " + def + " has incorrect version: " + localVer
				+ " (sent: " + v2 + ")");
		return false;
	}

	@Override
	public File getProcessFile(String dep, String def, String ver,
			String basedir) {
		List<String> splitDep = Lists.newArrayList(Splitter.on(":")
				.trimResults().split(dep));
		List<String> splitDepPrefix = Lists.newArrayList(Splitter.on(".")
				.trimResults().split(splitDep.get(0)));

		String processPath = basedir + File.separator + splitDep.get(1)
				+ File.separator + "src" + File.separator + "main"
				+ File.separator + "resources";
		for (String folder : splitDepPrefix) {
			processPath += File.separator + folder;
		}
		processPath += File.separator + splitDep.get(1);
		String filename = def + "_" + ver + ".bpmn2";
		File f = new File(processPath + File.separator + filename);
		log.info("Saving process to file: " + f.getAbsolutePath());
		return f;
	}

	@Override
	public String raiseDeploymentVersion(String dep, String basedir) {
		List<String> splitDep = Lists.newArrayList(Splitter.on(":")
				.trimResults().split(dep));

		String pomPath = basedir + File.separator + splitDep.get(1)
				+ File.separator + "pom.xml";
		File pom = new File(pomPath);
		
		log.info("Parsing POM at "+pomPath);

		Builder xom = new Builder();
		Document doc = null;
		try {
			doc = xom.build(pom);
		} catch (ParsingException | IOException e) {
			log.error("Parsing error: " + e.getMessage());
		}

		Element root = doc.getRootElement();
		XPathContext context = XPathContext.makeNamespaceContext(root);
		context.addNamespace("mv", "http://maven.apache.org/POM/4.0.0");
		Element version = (Element) (root.query("mv:version", context )).get(0);
		String old = ((Text) version.getChild(0)).getValue();
		
		List<String> splitVer = Lists.newArrayList(Splitter.on(".").split(old));
		int raised = Integer.parseInt(splitVer.get(1)) + 1;
		splitVer.set(1, raised + "");
		String newVer = Joiner.on(".").join(splitVer);
		version.removeChild(0);
		version.appendChild(newVer);
		log.info("Raised version from " + old + " to " + newVer);
		
		pom.delete();
		try {
			pom.createNewFile();
		PrintStream pw = new PrintStream(pom);
		pw.print(doc.toXML());
		pw.close();
		} catch (IOException e) {
			log.error("POM write error: "+e.getMessage());
		}

		return newVer;
	}

	@Override
	public void deploy(String repo, String project) {
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<String> res = client.exchange(
				String.format(mvnDeployUrl, repo, project), HttpMethod.POST,
				req, String.class);
		log.info("Deployed " + repo + "/" + project);
	}

	@Override
	public void undeploy(String dep) {
		HttpEntity<String> req = newEntityWithHeaders();
		ResponseEntity<String> res = client.exchange(
				String.format(undeployUrl, dep), HttpMethod.POST, req,
				String.class);
		log.info("Undeployed " + dep);

	}
}
