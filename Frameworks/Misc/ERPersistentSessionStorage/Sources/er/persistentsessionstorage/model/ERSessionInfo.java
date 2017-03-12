package er.persistentsessionstorage.model;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSForwardException;

public class ERSessionInfo extends er.persistentsessionstorage.model.eogen._ERSessionInfo {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = LoggerFactory.getLogger(ERSessionInfo.class);

	public static final ERSessionInfoClazz<ERSessionInfo> clazz = new ERSessionInfoClazz<>();

	public static class ERSessionInfoClazz<T extends ERSessionInfo> extends
			er.persistentsessionstorage.model.eogen._ERSessionInfo._ERSessionInfoClazz<T> {
		/* more clazz methods here */
	}

	/**
	 * Initializes the EO. This is called when an EO is created, not when it is
	 * inserted into an EC.
	 */
	@Override
	public void init(EOEditingContext ec) {
		super.init(ec);
		setIntLock(Integer.valueOf(0));
	}
	
	@Override
	public void willUpdate() {
		super.willUpdate();
		Integer lock = Integer.valueOf(intLock().intValue() + 1);
		setIntLock(lock);
	}

	public WOSession session() {
		return sessionFromArchivedData(sessionData());
	}

	public void archiveDataFromSession(WOSession session) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		ObjectOutputStream oos = null;
		NSData data = null;

		try {
			oos = new ObjectOutputStream(baos);
			oos.writeObject(session);
			oos.flush();
			byte[] bytes = baos.toByteArray();
			data = new NSData(bytes);
		} catch (IOException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		} finally {
			if (oos != null) {
				try {
					oos.close();
				} catch (IOException e) {
					log.warn("Could not close stream.", e);
				}
			}
		}

		setSessionData(data);
	}

	public WOSession sessionFromArchivedData(NSData data) {
		Object object = null;
		byte[] bytes = data.bytes();
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new ByteArrayInputStream(bytes));
			object = ois.readObject();
		} catch (IOException e) {
			log.warn("Failed to deserialize session", e);
		} catch (ClassNotFoundException e) {
			log.warn("Failed to deserialize session", e);
		} catch (RuntimeException e) {
			log.warn("Failed to deserialize session", e);
		} finally {
			if (ois != null) {
				try {
					ois.close();
				} catch (IOException e) {
					log.warn("Could not close stream.", e);
				}
			}
		}
		return (WOSession) object;
	}
}
