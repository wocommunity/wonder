package er.extensions.foundation;

import sun.misc.Signal;
import sun.misc.SignalHandler;

import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;

/**
 * There can only be one handler for any signal, so we have our own handler that
 * maintains a list. Simply register your handlers to this one.
 * 
 * @author ak
 * 
 */
public class ERXSignalHandler implements SignalHandler {
	
	static ERXSignalHandler _handler;
	
	private NSMutableDictionary<String, NSMutableArray<SignalHandler>> signals = new NSMutableDictionary();
	
	/**
	 * Adds your handler with the supplied signal name to the queue.
	 * @param signalName eg HUP. TERM etc.
	 * @param handler SignalHandler object
	 */
	public synchronized static void register(String signalName, SignalHandler handler) {
		signalName = normalize(signalName);
		if(_handler == null) {
			_handler = new ERXSignalHandler();
		}
		NSMutableArray<SignalHandler> listeners = _handler.signals.objectForKey(signalName);
		if(listeners == null) {
			listeners = new NSMutableArray<>();
			
	        Signal signal = new Signal(signalName);
	        Signal.handle(signal, _handler);
	        _handler.signals.setObjectForKey(listeners, signalName);
		}
		listeners.addObject(handler);
	}
	
	/**
	 * Removes your handler with the supplied signal name from the queue.
	 * @param signalName eg HUP. TERM etc.
	 * @param handler SignalHandler object
	 */
	public synchronized static void unregister(String signalName, SignalHandler handler) {
		signalName = normalize(signalName);
		NSMutableArray<SignalHandler> listeners = _handler.signals.objectForKey(signalName);
		if(listeners == null) {
			listeners = new NSMutableArray<>();
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

	private static String normalize(String signalName) {
		return signalName.toUpperCase();
	}
	
}
