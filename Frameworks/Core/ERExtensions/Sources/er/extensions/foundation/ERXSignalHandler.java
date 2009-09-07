package er.extensions.foundation;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * There can only be one handler for any signal, so we define a catch-all
 * handler that makes a NSNotification from it. To use it, you need to define
 * the property <code>er.foundation.ERXSignalHandler.signals=HUP, TERM</code>
 * and listen to the notifications <code>HandleSignalHUP</code> and
 * <code>HandleSignalTERM</code>.
 * 
 * @author ak
 * 
 */
public class ERXSignalHandler implements SignalHandler {
	
	static ERXSignalHandler _handler;
	
	public static final String HANDLE = "HandleSignal";
	
	private NSMutableDictionary<String, NSMutableArray<SignalHandler>> signals = new NSMutableDictionary();
	
	/**
	 * Convenience to listen to a signal.
	 * @param signalName
	 * @param selectorName
	 * @param listener
	 */
	public synchronized static void register(String signalName, SignalHandler handler) {
		signalName = normalize(signalName);
		if(_handler == null) {
			_handler = new ERXSignalHandler();
		}
		NSMutableArray<SignalHandler> listeners = _handler.signals.objectForKey(signalName);
		if(listeners == null) {
			listeners = new NSMutableArray<SignalHandler>();
			
	        Signal signal = new Signal(signalName);
	        Signal.handle(signal, _handler);
	        _handler.signals.setObjectForKey(listeners, signalName);
		}
		listeners.addObject(handler);
	}

	private static String normalize(String signalName) {
		return signalName.toUpperCase();
	}
	
	public synchronized static void unregister(String signalName, SignalHandler handler) {
		signalName = normalize(signalName);
		NSMutableArray<SignalHandler> listeners = _handler.signals.objectForKey(signalName);
		if(listeners == null) {
			listeners = new NSMutableArray<SignalHandler>();
		}
		listeners.removeObject(handler);
	}
	
	/**
	 * Implementation of the SignalHandler interface.
	 */
	public void handle(Signal signal) {
		NSMutableArray<SignalHandler> listeners = signals.objectForKey(normalize(signal.getName()));
		if(listeners != null) {
			for (SignalHandler handler : listeners) {
				handler.handle(signal);
			}
		}
	}
	
}
