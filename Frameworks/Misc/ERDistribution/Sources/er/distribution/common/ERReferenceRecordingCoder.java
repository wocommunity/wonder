package er.distribution.common;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eodistribution.common.ERDistributionUtils;
import com.webobjects.eodistribution.common._EOReferenceRecordingCoder;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSSelector;

/**
 * Add DEBUG logging for messages that fail to be decoded 
 * Fix _classForNumber slightly
 * Fix deserialization of EOKeyValueQualifier to create an _EOKnownSelector when appropriate; otherwise, in-memory evaluation of the qualifier does not work.
 *
 */
public class ERReferenceRecordingCoder extends _EOReferenceRecordingCoder {
	
	/**
	 * Declared to gain access to the protected method: operatorSelectorForSelectorNamed
	 */
	private static class FriendlyQualifier extends EOKeyValueQualifier {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		public FriendlyQualifier() {
			super("fake", EOQualifier.QualifierOperatorEqual, "qual");
		}
		
		public static NSSelector operatorSelectorForSelectorNamed(String name) {
			return EOQualifier.operatorSelectorForSelectorNamed(name);
		}
	}
	
 	private static final Logger log = LoggerFactory.getLogger(ERReferenceRecordingCoder.class);
	
	private NSData sourceMessage;
	private int sourceSize;
	
	public ERReferenceRecordingCoder(boolean parity) {
		super(parity);
	}

	/**
	 * Fix to return null if 'number' is less than zero
	 */
	@Override
	protected Class _classForNumber(short number) {
		if (number < 0) {
			return null;
		} else {
			return super._classForNumber(number);
		}
	}
	
	/**
	 * Save a copy of the whole original message so we can log it later if needed
	 */
	@Override
	public void prepareForReading(InputStream stream) {
		sourceSize = -1;
		if ((stream instanceof ByteArrayInputStream)) {
			try {
				sourceSize = stream.available();
			} catch (IOException e) {}
		}
		
		if (log.isDebugEnabled()) {
			saveSourceMessage(stream);
		}
		
		super.prepareForReading(stream);
	}

	/**
	 * Add DEBUG logging for messages that fail to be decoded
	 */
	@Override
	public Object decodeObject() {
		try {
			Object result = super.decodeObject();
			if (result != null && result.getClass().equals(EOKeyValueQualifier.class)) {
				EOKeyValueQualifier qual = (EOKeyValueQualifier) result;
				NSSelector<?> selector = qual.selector();
				NSSelector<?> newSelector = FriendlyQualifier.operatorSelectorForSelectorNamed(selector.name());
				EOKeyValueQualifier newResult = new EOKeyValueQualifier(qual.key(), newSelector, qual.value());
				return newResult;
			}
			return result;
		} catch (RuntimeException e) {
			if (log.isDebugEnabled()) {
				try {
					String filename = File.createTempFile("decodeError", "").getCanonicalPath();
					FileOutputStream fileOutputStream = new FileOutputStream(filename);
					sourceMessage.writeToStream(fileOutputStream);
					fileOutputStream.close();
					log.warn("Wrote message that caused exception to file: " + filename);
				} catch (java.io.IOException exception) {
					log.warn("Unable to save message that caused exception: " + exception);
				}
			}
			
			log.error(e.getMessage() + ". Response was " + sourceSize + " bytes long.");
			
			if (ERDistributionUtils.isTemporaryLockingFailure(e)) {
				throw e;
			} else {
				throw new MalformedResponseException("The response from the server was invalid" + 
						(sourceSize == 0 ? " (empty)" : "") + ".");
			}
		}
	}
	
	private void saveSourceMessage(InputStream stream) {
		if (!(stream instanceof ByteArrayInputStream)) {
			sourceMessage = new NSData();
			return;
		}
		
		ByteArrayInputStream byteStream = (ByteArrayInputStream)stream;
		byte[] bytes = new byte[byteStream.available()];
		try {
			byteStream.read(bytes);
			sourceMessage = new NSData(bytes);
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
		
		try {
			stream.reset();
		} catch (IOException e) {
			log.error(e.getMessage(), e);
		}
	}
	
}