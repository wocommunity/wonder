package er.bugtracker.mail;

import java.io.IOException;
import java.util.Properties;

import javax.mail.AuthenticationFailedException;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Store;

import org.apache.log4j.Logger;

public class MailReader extends Thread {
    
    private boolean running = true;

    private static final Logger log = Logger.getLogger(MailReader.class);

    private String server;

    private String user;

    private String password;

    private String queueName;

    public MailReader(String queue) {
        queueName = queue;
        server = getProperty(queue, "server");
        user = getProperty(queue, "user");
        password = getProperty(queue, "password");
    }

    private String getProperty(String queue, String key, String defaultValue) {
        String propertyKey = "BugTracker." + queue + "." + key;
        if(queue == null) {
            propertyKey = "BugTracker." + key;
        }
        return System.getProperty(propertyKey, defaultValue);
    }

    private String getProperty(String queue, String key) {
        return getProperty(queue, key, null);
    }

    public String queueName() {
        return queueName;
    }
    
    public void stopReader() {
        running = false;
    }
    
    public void startReader() {
        start();
    }
    
    private Message readMessages(String server, String user, String password) throws MessagingException, IOException {
        Store store = null;
        Folder folder = null;

        try {
            Properties props = System.getProperties();
            Session session = Session.getDefaultInstance(props, null);
            String storeName = getProperty(null, "mailStore", "imap");
            store = session.getStore(storeName);
            store.connect(server, user, password);

            folder = store.getDefaultFolder();
            if (folder == null) {
                throw new IllegalStateException("No default folder");
            }
            folder = folder.getFolder("INBOX");
            if (folder == null) {
                throw new IllegalStateException("No " + storeName + " INBOX");
            }
            folder.open(Folder.READ_WRITE);

            Message[] messages = folder.getMessages();
            log.debug("Checking INBOX: " + messages.length);
            for (int i = 0; i < messages.length; i++) {
                Message m = messages[i];
                Message result = processMessage(m);

                if (result != null) {
                    return result;
                }
            }
        } catch (AuthenticationFailedException e) {
            log.error("Mail setup wrong: " + server + ", " + user + ", " + password, e);
            throw e;
        } finally {
            if (folder != null && folder.isOpen()) {
                folder.close(true);
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
        return null;
    }

    private Message processMessage(Message message) throws MessagingException, IOException {
        log.info("Processing mail: " + message.getSubject());
        if (false) {
            Object content = message.getContent();
            if (content instanceof Multipart) {
                handleMultipart((Multipart) content);
            } else {
                handlePart(message);
            }
            // message.setFlag(Flags.Flag.DELETED, true);
        }
        return message;
    }

    private void handlePart(Message message) {
        // TODO Auto-generated method stub
        
    }

    private void handleMultipart(Multipart multipart) {
        // TODO Auto-generated method stub
        
    }

    public void run() {
        while (running) {
            try {
                readMessages(server, user, password);
                Thread.sleep(5*1000L);
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (MessagingException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (InterruptedException e) {
                running = false;
            }
        }
    }
}
