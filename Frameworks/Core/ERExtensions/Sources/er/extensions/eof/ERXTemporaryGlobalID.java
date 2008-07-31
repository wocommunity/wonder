package er.extensions.eof;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.webobjects.appserver.WOApplication;
import com.webobjects.eocontrol.EOTemporaryGlobalID;
import com.webobjects.foundation.NSForwardException;

/**
 * Experimental class to have quasi-GUIDs that fit into a long.
 * @author ak
 *
 */
public class ERXTemporaryGlobalID extends EOTemporaryGlobalID {
	
	private static int _cnt;
	private static int _lastTime;
	
	private long _value;

	public static interface Delegate {
		public int identifier();
	}
	
	private static Delegate _delegate;
			
	public ERXTemporaryGlobalID() {
		synchronized(ERXTemporaryGlobalID.class) {
			_value = 0L;
			int current = (int) System.currentTimeMillis();
			if(_lastTime < current) {
				_cnt = 0;
			}
			_value |= delegate().identifier() << 40;
			_value |= (_cnt++) << 32;
			_value |= current;
		}
	}
	
	public static class DefaultDelegate implements Delegate {
		
		private int _identifier;

		DefaultDelegate() {
			int result = getHostIdentifier();
			result |= getAppIdentifier();
			_identifier = result;
		}

		protected int getHostIdentifier() {
			try {
				byte[] address = InetAddress.getLocalHost().getAddress();
				int result = address[3] << 16;
				result |= address[4] << 8;
				return result;
			} catch (UnknownHostException e) {
				throw NSForwardException._runtimeExceptionForThrowable(e);
			}
		}

		protected int getAppIdentifier() {
			return WOApplication.application().port().intValue() & 0xff;
		}

		public int identifier() {
			return _identifier;
		}
	}
	
	private Delegate delegate() {
		if(_delegate == null) {
			_delegate = new DefaultDelegate();
		}
		return _delegate;
	}
	
	public static void setDelegate(Delegate delegate) {
		_delegate = delegate;
	}
}
