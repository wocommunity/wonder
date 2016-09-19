package er.ajax.json.serializer;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.MethodDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.jabsorb.serializer.AbstractSerializer;
import org.jabsorb.serializer.MarshallException;
import org.jabsorb.serializer.ObjectMatch;
import org.jabsorb.serializer.SerializerState;
import org.jabsorb.serializer.UnmarshallException;
import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import er.extensions.foundation.ERXStringUtilities;

/**
 * ERXBeanSerializer is a rip-off of BeanSerializer except that it supports
 * WO-style naming (i.e. missing "get").
 */
public class ERXBeanSerializer extends AbstractSerializer {
	/**
	 * Stores the readable and writable properties for the Bean.
	 */
	protected static class BeanData {
		// TODO: Legacy comment. WTF?
		// in absence of getters and setters, these fields are
		// public to allow subclasses to access.
		/**
		 * The bean info for a certain bean
		 */
		public BeanInfo beanInfo;

		/**
		 * The readable properties of the bean.
		 */
		public Map<String, Method> readableProps;

		/**
		 * The writable properties of the bean.
		 */
		public Map<String, Method> writableProps;
	}

	/**
	 * Unique serialisation id.
	 */
	private final static long serialVersionUID = 2;

	/**
	 * The logger for this class
	 */
	private final static Logger log = LoggerFactory.getLogger(ERXBeanSerializer.class);

	/**
	 * Caches analysed beans
	 */
	private static Map<Class<?>, BeanData> beanCache = new HashMap<Class<?>, BeanData>();

	/**
	 * Classes that this can serialise to.
	 * 
	 * TODO: Yay for bloat!
	 */
	private static Class<?>[] _JSONClasses = new Class[] {};

	private static Set<String> _ignoreMethodNames = new HashSet<>();
	
	static {
		_ignoreMethodNames = new HashSet<>();
		_ignoreMethodNames.add("equals");
		_ignoreMethodNames.add("hashCode");
		_ignoreMethodNames.add("main");
		_ignoreMethodNames.add("declaringClass");
	}
	
	private Class _clazz;
	
	/**
	 * Analyses a bean, returning a BeanData with the data extracted from it.
	 * 
	 * @param clazz The class of the bean to analyse
	 * @return A populated BeanData
	 * @throws IntrospectionException If a problem occurs during getting the bean
	 *           info.
	 */
	public static BeanData analyzeBean(Class<?> clazz) throws IntrospectionException {
		log.info("analyzing " + clazz.getName());
		BeanData bd = new BeanData();
		bd.beanInfo = Introspector.getBeanInfo(clazz, Object.class);
		bd.readableProps = new HashMap<>();
		bd.writableProps = new HashMap<>();
		for (MethodDescriptor md : bd.beanInfo.getMethodDescriptors()) {
			Method method = md.getMethod();
			if (!Modifier.isStatic(method.getModifiers())) {
				String name = method.getName();
				if (!_ignoreMethodNames.contains(name)) {
					Class<?> returnType = method.getReturnType();
					String propertyName = name;
					if (propertyName.startsWith("get") || propertyName.startsWith("set")) {
						propertyName = ERXStringUtilities.uncapitalize(propertyName.substring("set".length()));
					}
					if (returnType == void.class && name.startsWith("set") && method.getParameterTypes().length == 1) {
						bd.writableProps.put(propertyName, method);
					}
					else if (returnType != null && method.getParameterTypes().length == 0 && !name.startsWith("_")) {
						bd.readableProps.put(propertyName, method);
					}
				}
			}
		}
		return bd;
	}

	/**
	 * Gets the bean data from cache if possible, otherwise analyses the bean.
	 * 
	 * @param clazz The class of the bean to analyse
	 * @return A populated BeanData
	 * @throws IntrospectionException If a problem occurs during getting the bean
	 *           info.
	 */
	public static BeanData getBeanData(Class<?> clazz) throws IntrospectionException {
		BeanData bd;
		synchronized (beanCache) {
			bd = beanCache.get(clazz);
			if (bd == null) {
				bd = analyzeBean(clazz);
				beanCache.put(clazz, bd);
			}
		}
		return bd;
	}
	
	public ERXBeanSerializer(Class clazz) {
		_clazz = clazz;
	}

	@Override
	public boolean canSerialize(Class clazz, Class jsonClazz) {
		return (_clazz.isAssignableFrom(clazz) && (jsonClazz == null || jsonClazz == JSONObject.class));
	}

	public Class<?>[] getJSONClasses() {
		return _JSONClasses;
	}

	public Class<?>[] getSerializableClasses() {
		return new Class[] { _clazz };
	}

	public Object marshall(SerializerState state, Object p, Object o) throws MarshallException {
		BeanData bd;
		try {
			bd = getBeanData(o.getClass());
		}
		catch (IntrospectionException e) {
			throw new MarshallException(o.getClass().getName() + " is not a bean", e);
		}

		JSONObject val = new JSONObject();
		if (ser.getMarshallClassHints()) {
			try {
				val.put("javaClass", o.getClass().getName());
			}
			catch (JSONException e) {
				throw new MarshallException("JSONException: " + e.getMessage(), e);
			}
		}
		Object args[] = new Object[0];
		Object result;
		for (Map.Entry<String, Method> ent : bd.readableProps.entrySet()) {
			String prop = ent.getKey();
			Method getMethod = ent.getValue();
			if (log.isDebugEnabled()) {
				log.debug("invoking " + getMethod.getName() + "()");
			}
			try {
				result = getMethod.invoke(o, args);
			}
			catch (Throwable e) {
				if (e instanceof InvocationTargetException) {
					e = ((InvocationTargetException) e).getTargetException();
				}
				throw new MarshallException("bean " + o.getClass().getName() + " can't invoke " + getMethod.getName() + ": " + e.getMessage(), e);
			}
			try {
				if (result != null || ser.getMarshallNullAttributes()) {
					try {
						Object json = ser.marshall(state, o, result, prop);
						val.put(prop, json);
					}
					catch (JSONException e) {
						throw new MarshallException("JSONException: " + e.getMessage(), e);
					}
				}
			}
			catch (MarshallException e) {
				throw new MarshallException("bean " + o.getClass().getName() + " " + e.getMessage(), e);
			}
		}

		return val;
	}

	public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		JSONObject jso = (JSONObject) o;
		BeanData bd;
		try {
			bd = getBeanData(clazz);
		}
		catch (IntrospectionException e) {
			throw new UnmarshallException(clazz.getName() + " is not a bean", e);
		}

		int match = 0;
		int mismatch = 0;
		for (Map.Entry<String, Method> ent : bd.writableProps.entrySet()) {
			String prop = ent.getKey();
			if (jso.has(prop)) {
				match++;
			}
			else {
				mismatch++;
			}
		}
		if (match == 0) {
			throw new UnmarshallException("bean has no matches");
		}

		// create a concrete ObjectMatch that is always returned in order to satisfy circular reference requirements
		ObjectMatch returnValue = new ObjectMatch(-1);
		state.setSerialized(o, returnValue);

		ObjectMatch m = null;
		ObjectMatch tmp;
		Iterator<String> i = jso.keys();
		while (i.hasNext()) {
			String field = i.next();
			Method setMethod = bd.writableProps.get(field);
			if (setMethod != null) {
				try {
					Class<?> param[] = setMethod.getParameterTypes();
					if (param.length != 1) {
						throw new UnmarshallException("bean " + clazz.getName() + " method " + setMethod.getName() + " does not have one arg");
					}
					tmp = ser.tryUnmarshall(state, param[0], jso.get(field));
					if (tmp != null) {
						if (m == null) {
							m = tmp;
						}
						else {
							m = m.max(tmp);
						}
					}
				}
				catch (UnmarshallException e) {
					throw new UnmarshallException("bean " + clazz.getName() + " " + e.getMessage(), e);
				}
				catch (JSONException e) {
					throw new UnmarshallException("bean " + clazz.getName() + " " + e.getMessage(), e);
				}
			}
			else {
				mismatch++;
			}
		}
		if (m != null) {
			returnValue.setMismatch(m.max(new ObjectMatch(mismatch)).getMismatch());
		}
		else {
			returnValue.setMismatch(mismatch);
		}
		return returnValue;
	}

	public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
		JSONObject jso = (JSONObject) o;
		BeanData bd;
		try {
			bd = getBeanData(clazz);
		}
		catch (IntrospectionException e) {
			throw new UnmarshallException(clazz.getName() + " is not a bean", e);
		}
		if (log.isDebugEnabled()) {
			log.debug("instantiating " + clazz.getName());
		}
		Object instance;
		try {
			instance = clazz.newInstance();
		}
		catch (InstantiationException e) {
			throw new UnmarshallException("could not instantiate bean of type " + clazz.getName() + ", make sure it has a no argument " + "constructor and that it is not an interface or " + "abstract class", e);
		}
		catch (IllegalAccessException e) {
			throw new UnmarshallException("could not instantiate bean of type " + clazz.getName(), e);
		}
		catch (RuntimeException e) {
			throw new UnmarshallException("could not instantiate bean of type " + clazz.getName(), e);
		}
		state.setSerialized(o, instance);
		Object invokeArgs[] = new Object[1];
		Object fieldVal;
		Iterator<String> i = jso.keys();
		while (i.hasNext()) {
			String field = i.next();
			Method setMethod = bd.writableProps.get(field);
			if (setMethod != null) {
				try {
					Class<?> param[] = setMethod.getParameterTypes();
					fieldVal = ser.unmarshall(state, param[0], jso.get(field));
				}
				catch (UnmarshallException e) {
					throw new UnmarshallException("could not unmarshall field \"" + field + "\" of bean " + clazz.getName(), e);
				}
				catch (JSONException e) {
					throw new UnmarshallException("could not unmarshall field \"" + field + "\" of bean " + clazz.getName(), e);
				}
				if (log.isDebugEnabled()) {
					log.debug("invoking " + setMethod.getName() + "(" + fieldVal + ")");
				}
				invokeArgs[0] = fieldVal;
				try {
					setMethod.invoke(instance, invokeArgs);
				}
				catch (Throwable e) {
					if (e instanceof InvocationTargetException) {
						e = ((InvocationTargetException) e).getTargetException();
					}
					throw new UnmarshallException("bean " + clazz.getName() + "can't invoke " + setMethod.getName() + ": " + e.getMessage(), e);
				}
			}
		}
		return instance;
	}
}