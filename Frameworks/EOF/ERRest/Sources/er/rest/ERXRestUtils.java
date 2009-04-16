package er.rest;

import java.lang.reflect.InvocationTargetException;
import java.text.ParseException;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;

import er.extensions.eof.ERXEOAccessUtilities;
import er.extensions.eof.ERXKeyFilter;

/**
 * Miscellaneous rest-related utility methods.
 * 
 * @author mschrag
 */
public class ERXRestUtils {
	/**
	 * An enum for performing operations on the common response types.
	 * 
	 * @author mschrag
	 */
	public enum ResponseType {
		XML(ERXXmlRestResponseWriter.class), JSON(ERXJSONRestResponseWriter.class), PLIST(ERXPListRestResponseWriter.class);

		private Class<? extends IERXRestResponseWriter> _writerClass;

		private ResponseType(Class<? extends IERXRestResponseWriter> writerClass) {
			_writerClass = writerClass;
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
		public IERXRestResponseWriter defaultWriter() throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			return _writerClass.getConstructor(boolean.class, boolean.class).newInstance(Boolean.TRUE, Boolean.TRUE);
		}

		/**
		 * Returns a rest response writer using the "true, true" constructor.
		 * 
		 * @param filter a key filter
		 * @return a rest response writer
		 * @throws IllegalArgumentException
		 * @throws SecurityException
		 * @throws InstantiationException
		 * @throws IllegalAccessException
		 * @throws InvocationTargetException
		 * @throws NoSuchMethodException
		 */
		public IERXRestResponseWriter defaultWriter(ERXKeyFilter filter) throws IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			return _writerClass.getConstructor(ERXKeyFilter.class).newInstance(filter);
		}

		/**
		 * Returns a String form of the given object using the unsafe delegate.
		 * 
		 * @param value the value to write
		 * @return a string form of the value using the given writer
		 * @throws ERXRestException
		 * @throws ERXRestSecurityException
		 * @throws ERXRestNotFoundException
		 * @throws ParseException
		 */
		public String toString(EOEnterpriseObject value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			return ERXRestUtils.toString(defaultWriter(), value);
		}

		/**
		 * Returns a String form of the given objects using the unsafe delegate.
		 * 
		 * @param values the values to write
		 * @return a string form of the value using the given writer
		 * @throws ERXRestException
		 * @throws ERXRestSecurityException
		 * @throws ERXRestNotFoundException
		 * @throws ParseException
		 */
		public String toString(EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException, IllegalArgumentException, SecurityException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			return ERXRestUtils.toString(defaultWriter(), entity, values);
		}
	}

	// MS: Yes, this is wrong, but I'll fix it later ...
	public static EOEntity getEntityNamed(ERXRestContext context, String name) {
		EOEntity e = ERXEOAccessUtilities.entityNamed(context.editingContext(), name);
		if (e == null) {
			throw new RuntimeException("Could not find entity named '" + name + "'");
		}
		return e;
	}

	/**
	 * Returns a String form of the given object using the given delegate.
	 * 
	 * @param context the context to write within
	 * @param writer the writer to write with
	 * @param value the value to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(ERXRestContext context, IERXRestResponseWriter writer, EOEnterpriseObject value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		ERXStringBufferResponseWriter responseWriter = new ERXStringBufferResponseWriter();
		writer.appendToResponse(context, responseWriter, new ERXRestKey(context, ERXEOAccessUtilities.entityForEo(value), null, value));
		return responseWriter.toString();
	}

	/**
	 * Returns a String form of the given objects using the given delegate.
	 * 
	 * @param context the context to write within
	 * @param writer the writer to write with
	 * @param values the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(ERXRestContext context, IERXRestResponseWriter writer, EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		ERXStringBufferResponseWriter responseWriter = new ERXStringBufferResponseWriter();
		writer.appendToResponse(context, responseWriter, new ERXRestKey(context, entity, null, values));
		return responseWriter.toString();
	}

	/**
	 * Returns a String form of the given object using the unsafe delegate.
	 * 
	 * @param writer the writer to write with
	 * @param value the value to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(IERXRestResponseWriter writer, EOEnterpriseObject value) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), writer, value);
	}

	/**
	 * Returns a String form of the given objects using the unsafe delegate.
	 * 
	 * @param writer the writer to write with
	 * @param values the values to write
	 * @return a string form of the value using the given writer
	 * @throws ERXRestException
	 * @throws ERXRestSecurityException
	 * @throws ERXRestNotFoundException
	 * @throws ParseException
	 */
	public static String toString(IERXRestResponseWriter writer, EOEntity entity, NSArray values) throws ERXRestException, ERXRestSecurityException, ERXRestNotFoundException, ParseException {
		return ERXRestUtils.toString(new ERXRestContext(new ERXUnsafeRestEntityDelegate(true)), writer, entity, values);
	}
}
