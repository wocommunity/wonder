/**
 * <code>ERIMAP</code> is a utilities class that let you obtain connections and messages from an IMAP server.
 * 
 * @author <a href="mailto:probert@macti.ca">Pascal Robert</a>
 */

package er.javamail;

import javax.mail.AuthenticationFailedException;
import javax.mail.FolderNotFoundException;
import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import com.sun.mail.imap.IMAPFolder;
import com.sun.mail.imap.IMAPStore;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class ERIMAP {

	private IMAPStore imapStore;
	
	public ERIMAP() {
		super();
	}
	
	/*
	 * Simply return an IMAP (non-SSL) connection, obtained with a user and password.  
	 * The IMAP server host name is obtained from the er.javamail.imapHost property.
	 * 
	 * @param The user and password for the user to connect with.
	 * 
	 */
	public void openConnection(String imapUser, String imapPassword) throws AuthenticationFailedException, MessagingException, IllegalStateException  {
		imapStore = (IMAPStore)ERJavaMail.sharedInstance().defaultSession().getStore("imap");
		imapStore.connect(imapHost(), imapUser, imapPassword); 
	}
	
	/*
	 * Close the IMAP store and connection.
	 */
	public void closeConnection() throws MessagingException {
		imapStore.close();
	}	
	
	/*
	 * This method will open a IMAP folder.  Check com.sun.mail.imap.IMAPFolder constants for the mode parameter.
	 * 
	 * @param The folder name to open, and how to open it (read only or read write)
	 * @return An IMAP folder that you can manipulate.
	 */
	// mode = Folder.READ_WRITE
	public IMAPFolder openFolder(String folderName, int mode) throws MessagingException, FolderNotFoundException  {
		IMAPFolder folder = (IMAPFolder)imapStore.getFolder(folderName);
		folder.open(mode);	
		return folder;
	}
	
	/*
	 * Close a IMAP folder and optionally expunge messages with the DELETE flag.
	 * 
	 * @param An IMAP folder, obtained with openFolder, and a boolean to tell if you want to expunge deleted messages.
	 */
	public void closeFolder(IMAPFolder folder, boolean expunge) throws MessagingException {
		folder.close(expunge);	
	}
	
	/*
	 * Get all messages from an IMAP folder.  Simply call getMessages with 1 as the first message index, and folder.getMessageCount as the last message index.
	 * 
	 * @param An IMAP folder, obtained with openFolder.
	 * @return A array of ERMessage.
	 */
	public NSArray<ERMessage> getMessages(IMAPFolder folder) throws MessagingException {
		return getMessages(folder, 1, folder.getMessageCount());
	}	
	
	/*
	 * Get specific messages from an IMAP folder, with a starting and an end index.
	 * 
	 * @param An IMAP folder, obtained with openFolder, the index of the first message to fetch, and the index of the last message.
	 * @return A array of ERMessage.
	 */
	public NSArray<ERMessage> getMessages(IMAPFolder folder, int firstMsg, int lastMsg) throws MessagingException {
		NSMutableArray<ERMessage> emails = new NSMutableArray<>();
		if (folder.isOpen()) {
			MimeMessage[] messages = (MimeMessage[])folder.getMessages(firstMsg, lastMsg);
			for (int index = 0; index < messages.length; index++) {
				MimeMessage mimeMessage = messages[index];
				ERMessage message = new ERMessage();
				message.setMimeMessage(mimeMessage);
				emails.add(message);
			}
		}
		return emails;
	}
	
	/*
	 * Change the er.javamail.imapHost property, useful if you want to change IMAP server from your code.
	 */
	public void setImapHost(String _imapHost) {
		System.setProperty("er.javamail.imapHost", _imapHost);
	}
	
	/*
	 * Fetch the IMAP server hostname from the er.javamail.imapHost property.
	 */
	public String imapHost() {
		return System.getProperty("er.javamail.imapHost");
	}

}
