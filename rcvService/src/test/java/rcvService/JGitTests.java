package rcvService;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

import de.livinglab.Application;
import de.livinglab.JbpmClientService;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringApplicationConfiguration(classes = Application.class)
@WebAppConfiguration
@IntegrationTest
public class JGitTests {

	private static final String REMOTE_URL = "localhost:8001";
	private static final String USER = "admin";
	private static final String PW = "admin";
	private static final String DEP = "com.livinglab:remote:1.0";
	private static final String REPO = "testing";
	private static final String DEF = "testGen";
	private static final String VER = "1.1";

	@Autowired
	JbpmClientService jservice;

	@Test
	public void testClone() throws IOException, InvalidRemoteException,
			TransportException, GitAPIException {

		ClassPathResource cpr = new ClassPathResource("parent1.bpmn");
		String source = new String(Files.readAllBytes(cpr.getFile().toPath()),
				StandardCharsets.UTF_8);

		File localPath = File.createTempFile("TestGitRepository", "");
		localPath.delete();

		// then clone
		System.out.println("Cloning from " + REMOTE_URL + " to " + localPath);

		CredentialsProvider creds = new UsernamePasswordCredentialsProvider(
				USER, PW);
		CloneCommand cc = Git.cloneRepository();
		Git git = cc.setURI("ssh://"+USER+"@"+REMOTE_URL+"/"+REPO).setDirectory(localPath)
				.setCredentialsProvider(creds).call();

		List<String> splitDep = Lists.newArrayList(Splitter.on(":")
				.trimResults().split(DEP));
		List<String> splitDepPrefix = Lists.newArrayList(Splitter.on(".")
				.trimResults().split(splitDep.get(0)));

		String processPath = git.getRepository().getDirectory().getParent()
				+ File.separator + splitDep.get(1) + File.separator + "src"
				+ File.separator + "main" + File.separator + "resources";
		for (String folder : splitDepPrefix) {
			processPath += File.separator + folder;
		}
		processPath += File.separator + splitDep.get(1);
		String filename = DEF + "_" + VER + ".bpmn2";

		File newProcess = new File(processPath + File.separator + filename);
		System.out.println("Writing file: " + newProcess.getAbsolutePath());
		newProcess.createNewFile();
		PrintStream pw = new PrintStream(newProcess);
		pw.print(source);
		pw.close();

		git.add().addFilepattern(".").call();
		git.commit().setMessage("Generated: " + filename).call();
		git.push().setCredentialsProvider(creds).call();

		System.out.println("Having repository: "
				+ git.getRepository().getDirectory());

		git.getRepository().close();
	}

}
