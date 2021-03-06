package org.agilos.zendesk_jira_plugin.testframework;

import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;

import javax.xml.rpc.ServiceException;

import org.agilos.jira.soapclient.AgilosSoapService;
import org.agilos.jira.soapclient.AgilosSoapServiceService;
import org.agilos.jira.soapclient.AgilosSoapServiceServiceLocator;
import org.agilos.jira.soapclient.RemoteUser;
import org.apache.log4j.Logger;

public class ZendeskWSClient {
	private static AgilosSoapService agilosSoapService;
	private static String agilosSoapToken;

	public static AgilosSoapService getSoapService() {
		return agilosSoapService;
	}

	public static String getSoapToken() {
		return agilosSoapToken;
	}

	private Logger log = Logger.getLogger(ZendeskWSClient.class.getName());

	public ZendeskWSClient() throws ServiceException, RemoteException, MalformedURLException {	
		AgilosSoapServiceService agilosSoapServiceGetter = new AgilosSoapServiceServiceLocator();
		URL agilosSOAPServiceUrl = new URL(JIRA.URL+"/rpc/soap/agilossoapservice-v1");
		log.debug("Retriving jira soap service from "+agilosSOAPServiceUrl);
		agilosSoapService = agilosSoapServiceGetter.getAgilossoapserviceV1(agilosSOAPServiceUrl);
		log.debug("Logging in with user: " + JIRA.LOGIN_NAME+" and password: " + JIRA.LOGIN_PASSWORD);
		agilosSoapToken = agilosSoapService.login(JIRA.LOGIN_NAME, JIRA.LOGIN_PASSWORD);
	}
	
	public RemoteUser[] assignableUsers(String projectKey) throws Exception {
		return agilosSoapService.getAssignableUsers(agilosSoapToken, projectKey);
	}
}
