package er.extensions.eof;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;

/**
 * Experimental class to have quasi-GUIDs that fit into a long. Used as an
 * alternative to the ERXLongPrimaryKeyFactory or the 24 byte built-in keys.
 * To have it installed, set your pk prototype to "longId".
 * @author ak
 * 
 */
public class ERXTemporaryGlobalID extends EOGlobalID {

	private static int _cnt;
	private static int _lastTime;

	private static int _identifier;
	
	private long _value;
   
	public ERXTemporaryGlobalID() {
		synchronized (ERXTemporaryGlobalID.class) {
			if(_identifier == 0) {
				setDefaultAppIdentifier();
			}
			_value = 0L;
			int current = (int) System.currentTimeMillis();
			if (_lastTime < current) {
				_cnt = 0;
				_lastTime = current;
			}
			_value |= ((identifier() << 40) & 0xffffff0000000000L);
			_value |= ((_cnt << 32)         & 0x000000ff00000000L);
			_value |= ((_lastTime )         & 0x00000000ffffffffL);
			if (++_cnt == Byte.MAX_VALUE) {
				_lastTime++;
				_cnt = 0;
			}
		}
	}

	private static void setDefaultAppIdentifier() {
		int result = hostIdentifier();
		result |= appIdentifier();
		_identifier = result;
	}

	private static int hostIdentifier() {
		try {
			byte[] address = InetAddress.getLocalHost().getAddress();
			int result = address[3] << 16;
			result |= address[4] << 8;
			return result;
		}
		catch (UnknownHostException e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	private static int appIdentifier() {
		return WOApplication.application().port().intValue() & 0xff;
	}

	public static void setIdentifier(int value) {
		_identifier = value;
	}

	private int identifier() {
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
}
