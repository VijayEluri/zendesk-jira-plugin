package org.agilos.jira.zendesk.notifications;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

public class ChangeMessage {
	private static DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
	private MessageParts message;	
	private Node ticket;
	public static boolean publicComments = true;
	private String author;
	private String issueString;
	private StringBuffer changeString = new StringBuffer();
	private String jiraComment = null;

	public ChangeMessage(String author, String jiraIssue) {
		this.author = author;
		this.issueString = jiraIssue;
		message = new MessageParts();		
	}

	/**
	 * Adds a change of a Zendesk mapped attribute to the change message
	 * @param zendeskFieldID The Zendesk attribute the JIRA field should be mapped to. If null the no attribute update element is added to the message, 
	 * eg. corresponds to the {@link #addChange(String, String)} method
	 * @param jiraFieldID
	 * @param newValue
	 */
	public void addChange(String zendeskFieldID, String jiraFieldID, String newValue, String oldValue) {
		if (message.ticketChanges == null && zendeskFieldID != null ) {
			try {
				message.ticketChanges = factory.newDocumentBuilder().getDOMImplementation().createDocument(null,null,null);
			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			}

			ticket = message.ticketChanges.createElement("ticket");
			message.ticketChanges.appendChild(ticket);
		}
		if (zendeskFieldID != null) {
			Element element = message.ticketChanges.createElement(zendeskFieldID);
			element.setTextContent(newValue);
			ticket.appendChild(element);	
		}

		addChange(jiraFieldID, newValue, oldValue);
	}

	public void addChange(String jiraFieldID, String newValue, String oldValue) {
		changeString.append("\n"+capitalizeFirstLetter(jiraFieldID)+ ": "+newValue);
		if (oldValue != null) {
			changeString.append(" (was: "+oldValue+")");
		}
	}

	public void addComment(String comment) {
		this.jiraComment = comment;
	}

	private void createComment(String commentString) throws DOMException, ParserConfigurationException {
		message.comment = factory.newDocumentBuilder().getDOMImplementation().createDocument(null,null,null);
		Element comment = message.comment.createElement("comment");

		Element isPublic = message.comment.createElement("is-public");
		if (publicComments) {
			isPublic.setTextContent("true");
		} else {
			isPublic.setTextContent("false");
		}
		comment.appendChild(isPublic);

		Element value = message.comment.createElement("value");		
		value.setTextContent(commentString);		
		comment.appendChild(value);   	

		message.comment.appendChild(comment);
	}	

	public boolean isEmpty() {
		return (changeString.length() == 0 &&
				jiraComment == null);
	}

	public MessageParts createMessageParts() throws DOMException, ParserConfigurationException {
		StringBuffer comment = new StringBuffer();
		comment.append(author+" has updated JIRA issue "+issueString+" with:");

		if (changeString.length() != 0) {
			comment.append(changeString+"\n");
		}

		if (jiraComment != null) {
			comment.append("\nComment: "+jiraComment+"\n");
		}
		createComment(comment.toString());
		return message;
	}

	private String capitalizeFirstLetter(String inputWord) {
		String firstLetter = inputWord.substring(0,1);  // Get first letter
		String remainder   = inputWord.substring(1);    // Get remainder of word.
		return firstLetter.toUpperCase() + remainder.toLowerCase();
	}

	/**
	 * Zendesk is not able to handle a message containing both comments and attribute changes, so this needs to be separated into 2 parts.
	 */
	public static class MessageParts {
		Document ticketChanges;
		Document comment;

		/**
		 * Returns the document containing the changes which should be made to the Zendesk ticket. Return null if no changes are present.
		 * @return
		 */
		public Document getTicketChanges() {
			return ticketChanges;
		}

		/**
		 * Returns the document containing the comment which should be made added to the Zendesk ticket.
		 * @return
		 */
		public Document getComment() {
			return comment;
		}
	}
}