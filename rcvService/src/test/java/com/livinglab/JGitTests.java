package com.livinglab;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

import org.eclipse.jgit.api.CloneCommand;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.api.errors.InvalidRemoteException;
import org.eclipse.jgit.api.errors.TransportException;
import org.eclipse.jgit.transport.CredentialsProvider;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.IntegrationTest;
import org.springframework.boot.test.SpringApplicationConfiguration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.web.WebAppConfiguration;

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
	private static final String DEP = "com.livinglab:remote:1.2";
	private static final String REPO = "testing";
	private static final String DEF = "testGen";
	private static final String VER = "1.1";
	private static final String PROJECT = "remote";

	@Autowired
	JbpmClientService jservice;

	@Ignore
	@Test
	public void testClone() throws IOException {

		ClassPathResource cpr = new ClassPathResource("parent1.bpmn");
		String source = new String(Files.readAllBytes(cpr.getFile().toPath()),
				StandardCharsets.UTF_8);

		File localPath = File.createTempFile("TestGitRepository", "");
		localPath.delete();
		
		Git git = jservice.cloneRepo(USER, PW, REPO, localPath);

		File newProcess = jservice.getProcessFile(DEP, DEF, VER, git.getRepository().getDirectory().getParent());
		
		jservice.write(source, newProcess);
		
		jservice.raiseDeploymentVersion(DEP, git.getRepository().getDirectory().getParent());

		jservice.addAllAndPush(USER, PW, git);
		
		jservice.deploy(REPO, PROJECT);
		
		git.getRepository().close();
	}

}
