package er.rest;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;

import er.extensions.eof.ERXKeyFilter;

/**
 * An enum for performing operations on the common response types.
 * 
 * @author mschrag
 */
public enum ERXRestFormat {
	XML(ERXXmlRestResponseWriter.class, ERXXmlRestRequestParser.class), JSON(ERXJSONRestResponseWriter.class, ERXJSONRestRequestParser.class), PLIST(ERXPListRestResponseWriter.class, null);

	private Class<? extends IERXRestResponseWriter> _writerClass;
	private Class<? extends IERXRestRequestParser> _parserClass;

	private ERXRestFormat(Class<? extends IERXRestResponseWriter> writerClass, Class<? extends IERXRestRequestParser> parserClass) {
		_writerClass = writerClass;
		_parserClass = parserClass;
	}

	public IERXRestRequestParser parser() {
		try {
			if (_parserClass == null) {
				throw new IllegalArgumentException("There is no parser for the format '" + name() + "'.");
			}
			return _parserClass.newInstance();
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	/**
	 * Returns a rest response writer using the "true, true" constructor.
	 * 
	 * @return a rest response writer
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public IERXRestResponseWriter writer() {
		try {
			return _writerClass.getConstructor(boolean.class, boolean.class).newInstance(Boolean.TRUE, Boolean.TRUE);
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	/**
	 * Returns a rest response writer using the "true, true" constructor.
	 * 
	 * @param filter
	 *            a key filter
	 * @return a rest response writer
	 * @throws IllegalArgumentException
	 * @throws SecurityException
	 * @throws InstantiationException
	 * @throws IllegalAccessException
	 * @throws InvocationTargetException
	 * @throws NoSuchMethodException
	 */
	public IERXRestResponseWriter writer(ERXKeyFilter filter) {
		try {
			return _writerClass.getConstructor(ERXKeyFilter.class).newInstance(filter);
		}
		catch (Exception e) {
			throw NSForwardException._runtimeExceptionForThrowable(e);
		}
	}

	/**
	 * Returns a String form of the given object using the unsafe delegate.
	 * 
	 * @param value
	 *            the value to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public String toString(EOEnterpriseObject value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestUtils.toString(writer(), value);
	}

	/**
	 * Returns a String form of the given objects using the unsafe delegate.
	 * 
	 * @param values
	 *            the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public String toString(EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestUtils.toString(writer(), entity, values);
	}
}
