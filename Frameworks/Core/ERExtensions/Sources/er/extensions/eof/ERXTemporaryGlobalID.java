package er.extensions.eof;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;

/**
 * Experimental class to have quasi-GUIDs that fit into a long. Used as an
 * alternative to the ERXLongPrimaryKeyFactory or the 24 byte built-in keys. To
 * have it installed, set your pk prototype to "longId". <br>
 * Note: by default, the key is partitioned by the lower 2 bytes host address,
 * the lower byte of the port number and the current time in seconds. Then you
 * have a byte left for inserts in one second. <br>
 * This means, if you you are inserting - say - one thousand objects and your
 * app crashes and restarts in 4 seconds and directly inserts new objects, then
 * you might end up with duplicate keys. So this might only be for apps that stay at a
 * low volume.<br>
 * Also you need to be sure that your app is in one class B subnet and your
 * instances port numbers are partitionable over the lower byte - which means
 * less than 256 instances on one host.<br>
 * Given these restrictions, you might want to stay with sequences...
 * 
 * @author ak
 * 
 */
public class ERXTemporaryGlobalID extends EOGlobalID {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	private static long _cnt;
	
	private static long _lastTime;

	private static long _identifier;
	
	private long _value;
   
	public ERXTemporaryGlobalID() {
		synchronized (ERXTemporaryGlobalID.class) {
			if(_identifier == 0) {
				setDefaultAppIdentifier();
			}
			_value = 0L;
			long current = (System.currentTimeMillis()/1000);
			if (_lastTime < current) {
				_cnt = 0;
				_lastTime = current;
			}
			_value |= ((identifier() << 40) & 0xffffff0000000000L);
			_value |= ((_lastTime << 8)     & 0x000000ffffffff00L);
			_value |= ((_cnt << 0)          & 0x00000000000000ffL);
			_cnt = _cnt+1;
			if (_cnt == 256) {
				_lastTime++;
				_cnt = 0;
			}
		}
	}

	private static void setDefaultAppIdentifier() {
		long result = hostIdentifier();
		result |= appIdentifier();
		_identifier = result;
	}

	private static long hostIdentifier() {
		try {
			byte[] address = InetAddress.getLocalHost().getAddress();
			long result = address[2] << 16;
			result |= address[3] << 8;
			return result;
		}
		catch (UnknownHostException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	private static long appIdentifier() {
		return WOApplication.application().port().intValue() & 0xff;
	}

	public static void setIdentifier(int value) {
		_identifier = value;
	}

	private long identifier() {
		return _identifier;
	}

	@Override
	public boolean equals(Object obj) {
		if (obj instanceof ERXTemporaryGlobalID) {
			ERXTemporaryGlobalID gid = (ERXTemporaryGlobalID) obj;
			return gid._value == _value;
		}
		return false;
	}
	
	@Override
	public boolean isTemporary() {
		return true;
	}

	@Override
	public int hashCode() {
		return (int)_value;
	}
	
	public long value() {
		return _value;
	}
	
	/**
	 * Returns a pk-ready dictionary with the supplied key.
	 * @param key
	 */
	public NSDictionary dictionary(String key) {
		return new NSDictionary(value(), key);
	}

	private static String hex = "0123456789abcdef";

	@Override
	public String toString() {
		StringBuilder s = new StringBuilder();
		for(int i = 0; i < 16; i++) {
			int index = (int) (_value >> ((15 - i) * 4));
			s.append(hex.charAt(index & 0xf));
		}
		return "0x" + s;
	}
}
