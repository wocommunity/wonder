package er.javamail;

import java.util.LinkedList;
import java.util.List;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.URLName;
import javax.mail.internet.AddressException;

import junit.framework.TestCase;
import er.extensions.foundation.ERXProperties;

public class ERMailSenderTest extends TestCase {
	public List<RelayInfo> relays() {
		List<RelayInfo> relays = new LinkedList<>();
		relays.add(new RelayInfo(null, "mail.pobox.com", "mschrag@pobox.com", "mschrag@pobox.com"));
		relays.add(new RelayInfo("other", "mailother.pobox.com", "mschrag@pobox.com", "mschrag@pobox.com"));
		return relays;
	}

	public RelayInfo defaultRelay() {
		return relays().get(0);
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
		for (RelayInfo relay : relays()) {
			if (relay.contextString == null) {
				System.setProperty("er.javamail.smtpHost", relay.host);
			} else {
				System.setProperty("er.javamail.smtpHost." + relay.contextString, relay.host);
			}
		}
		ERXProperties.setStringForKey("smtptest", "mail.smtp.protocol");
		ERJavaMail.sharedInstance().finishInitialization();
		TestSMTPTransport.clearSentMessages();
	}

	protected void sendTestMessage(RelayInfo relay, boolean sendNow) throws AddressException, MessagingException {
		ERMailDeliveryPlainText mailDelivery = new ERMailDeliveryPlainText();
		mailDelivery.setSubject("Test Subject");
		mailDelivery.setToAddress(relay.fromAddress);
		mailDelivery.setFromAddress(relay.toAddress);
		mailDelivery.setTextContent("Test Message");
		mailDelivery.setContextString(relay.contextString);
		mailDelivery.sendMail(sendNow);
	}

	protected void testSendMessageNowWithRelay(RelayInfo relay) throws MessagingException {
		sendTestMessage(relay, true);

		List<SentMessage> sentMessages = TestSMTPTransport.sentMessages();
		assertEquals(1, sentMessages.size());
		assertEquals(relay.host, sentMessages.get(0).transport.getSession().getProperty("mail.smtptest.host"));
	}

	public void testSendMessageNowDefaultRelay() throws MessagingException {
		testSendMessageNowWithRelay(defaultRelay());
	}

	public void testSendMessageNowOtherRelay() throws MessagingException {
		testSendMessageNowWithRelay(relays().get(1));
	}

	protected void waitForMessages(int count) {
		for (long startTime = System.currentTimeMillis(); TestSMTPTransport.sentMessages().size() < count && System.currentTimeMillis() - startTime < 10000;) {
			// DO NOTHING
		}
	}

	protected void testSendMessageDeferredWithRelay(RelayInfo relay) throws MessagingException {
		sendTestMessage(relay, false);

		waitForMessages(1);

		List<SentMessage> sentMessages = TestSMTPTransport.sentMessages();
		assertEquals(1, sentMessages.size());
		assertEquals(relay.host, sentMessages.get(0).transport.getSession().getProperty("mail.smtptest.host"));
	}

	public void testSendMessageDeferredDefaultRelay() throws MessagingException {
		testSendMessageDeferredWithRelay(defaultRelay());
	}

	public void testSendMessageDeferredOtherRelay() throws MessagingException {
		testSendMessageDeferredWithRelay(relays().get(1));
	}

	public void testSendTwoMessagesDeferredMixedRelays() throws AddressException, MessagingException {
		sendTestMessage(relays().get(0), false);
		sendTestMessage(relays().get(1), false);

		waitForMessages(2);

		List<SentMessage> sentMessages = TestSMTPTransport.sentMessages();
		assertEquals(2, sentMessages.size());
		assertEquals(relays().get(0).host, sentMessages.get(0).transport.getSession().getProperty("mail.smtptest.host"));
		assertEquals(relays().get(1).host, sentMessages.get(1).transport.getSession().getProperty("mail.smtptest.host"));
	}

	public static class SentMessage {
		public TestSMTPTransport	transport;

		public Message				message;

		public Address[]			addresses;

		public SentMessage(TestSMTPTransport transport, Message message, Address[] addresses) {
			this.transport = transport;
			this.message = message;
			this.addresses = addresses;
		}
	}

	public static class TestSMTPTransport extends Transport {
		private static List<SentMessage>	_sentMessages	= new LinkedList<>();

		public TestSMTPTransport(Session session, URLName urlname) {
			super(session, urlname);
		}

		public Session getSession() {
			return session;
		}

		@Override
		public void sendMessage(Message message, Address[] addresses) throws MessagingException {
			synchronized (_sentMessages) {
				_sentMessages.add(new SentMessage(this, message, addresses));
			}
		}

		@Override
		public void connect() throws MessagingException {
		// DO NOTHING
		}

		@Override
		public void connect(String host, int port, String user, String password) throws MessagingException {
		// DO NOTHING
		}

		@Override
		public void connect(String user, String password) throws MessagingException {
		// DO NOTHING
		}

		@Override
		public void connect(String host, String user, String password) throws MessagingException {
		// DO NOTHING
		}

		public static void clearSentMessages() {
			synchronized (_sentMessages) {
				_sentMessages.clear();
			}
		}

		public static List<SentMessage> sentMessages() {
			synchronized (_sentMessages) {
				return new LinkedList<>(_sentMessages);
			}
		}
	}

	public static class RelayInfo {
		public String	contextString;

		public String	host;

		public String	fromAddress;

		public String	toAddress;

		public RelayInfo(String contextString, String host, String fromAddress, String toAddress) {
			this.contextString = contextString;
			this.host = host;
			this.fromAddress = fromAddress;
			this.toAddress = toAddress;
		}
	}
}
