package com.metaparadigm.jsonrpc;

//Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
//Jad home page: http://www.kpdus.com/jad.html
//Decompiler options: packimports(3) radix(10) lradix(10) 
//Source File Name:   JSONRPCBridge.java

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import org.json.JSONArray;
import org.json.JSONObject;

public class JSONBridge implements Serializable {

	public static class ReferenceSerializer extends AbstractSerializer {

		public Class[] getSerializableClasses() {
			return _serializableClasses;
		}

		public Class[] getJSONClasses() {
			return _JSONClasses;
		}

		public ReferenceSerializer(JSONBridge bridge) {
			this.bridge = bridge;
		}

		public boolean canSerialize(Class clazz, Class jsonClazz) {
			return !clazz.isArray() && !clazz.isPrimitive() && !clazz.isInterface() && (bridge.isReference(clazz) || bridge.isCallableReference(clazz)) && (jsonClazz == null || jsonClazz == (org.json.JSONObject.class));
		}

		public ObjectMatch tryUnmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
			return ObjectMatch.OKAY;
		}

		public Object unmarshall(SerializerState state, Class clazz, Object o) throws UnmarshallException {
			JSONObject jso = (JSONObject) o;
			Object ref = null;
			String json_type = jso.getString("JSONRPCType");
			int object_id = jso.getInt("objectID");
			if (json_type != null && json_type.equals("Reference"))
				synchronized (bridge.referenceMap) {
					ref = bridge.referenceMap.get(new Integer(object_id));
				}
			return ref;
		}

		public Object marshall(SerializerState state, Object o) throws MarshallException {
			Class clazz = o.getClass();
			Integer identity = new Integer(System.identityHashCode(o));
			if (bridge.isReference(clazz)) {
				if (ser.isDebug())
					log.fine("marshalling reference to object " + identity + " of class " + clazz.getName());
				synchronized (bridge.referenceMap) {
					bridge.referenceMap.put(identity, o);
				}
				JSONObject jso = new JSONObject();
				jso.put("JSONRPCType", "Reference");
				jso.put("javaClass", clazz.getName());
				jso.put("objectID", identity);
				return jso;
			}
			if (bridge.isCallableReference(clazz)) {
				if (ser.isDebug())
					log.fine("marshalling callable reference to object " + identity + " of class " + clazz.getName());
				bridge.registerObject(identity, o);
				JSONObject jso = new JSONObject();
				jso.put("JSONRPCType", "CallableReference");
				jso.put("javaClass", clazz.getName());
				jso.put("objectID", identity);
				return jso;
			}
			else {
				return null;
			}
		}

		private static final long serialVersionUID = 1L;
		private JSONBridge bridge;
		private static final Class _serializableClasses[] = new Class[0];
		private static final Class _JSONClasses[] = new Class[0];
	}

	protected static class MethodCandidate {

		public ObjectMatch getMatch() {
			int mismatch = -1;
			for (int i = 0; i < match.length; i++)
				mismatch = Math.max(mismatch, match[i].mismatch);

			if (mismatch == -1)
				return ObjectMatch.OKAY;
			else
				return new ObjectMatch(mismatch);
		}

		private Method method;
		private ObjectMatch match[];

		public MethodCandidate(Method method) {
			this.method = method;
			match = new ObjectMatch[method.getParameterTypes().length];
		}
	}

	protected static class MethodKey {

		public int hashCode() {
			return methodName.hashCode() * numArgs;
		}

		public boolean equals(Object o) {
			if (!(o instanceof MethodKey))
				return false;
			else
				return methodName.equals(((MethodKey) o).methodName) && numArgs == ((MethodKey) o).numArgs;
		}

		private String methodName;
		private int numArgs;

		public MethodKey(String methodName, int numArgs) {
			this.methodName = methodName;
			this.numArgs = numArgs;
		}
	}

	protected static class ObjectInstance implements Serializable {

		public ClassData classData() {
			return JSONBridge.getClassData(clazz);
		}

		private Object o;
		private Class clazz;

		public ObjectInstance(Object o) {
			this.o = o;
			clazz = o.getClass();
		}
	}

	protected static class LocalArgResolverData {

		public boolean understands(Object context) {
			return contextInterface.isAssignableFrom(context.getClass());
		}

		public int hashCode() {
			return argResolver.hashCode() * argClazz.hashCode() * contextInterface.hashCode();
		}

		public boolean equals(Object o) {
			LocalArgResolverData cmp = (LocalArgResolverData) o;
			return argResolver.equals(cmp.argResolver) && argClazz.equals(cmp.argClazz) && contextInterface.equals(cmp.contextInterface);
		}

		private LocalArgResolver argResolver;
		private Class argClazz;
		private Class contextInterface;

		public LocalArgResolverData(LocalArgResolver argResolver, Class argClazz, Class contextInterface) {
			this.argResolver = argResolver;
			this.argClazz = argClazz;
			this.contextInterface = contextInterface;
		}
	}

	protected static class CallbackData implements Serializable {

		public boolean understands(Object context) {
			return contextInterface.isAssignableFrom(context.getClass());
		}

		public int hashCode() {
			return cb.hashCode() * contextInterface.hashCode();
		}

		public boolean equals(Object o) {
			CallbackData cmp = (CallbackData) o;
			return cb.equals(cmp.cb) && contextInterface.equals(cmp.contextInterface);
		}

		private InvocationCallback cb;
		private Class contextInterface;

		public CallbackData(InvocationCallback cb, Class contextInterface) {
			this.cb = cb;
			this.contextInterface = contextInterface;
		}
	}

	protected static class ClassData {

		private Class clazz;
		private HashMap methodMap;
		private HashMap staticMethodMap;

		protected ClassData() {
		}
	}

	public void setDebug(boolean debug) {
		this.debug = debug;
		ser.setDebug(debug);
	}

	protected boolean isDebug() {
		return debug;
	}

	public static JSONBridge getGlobalBridge() {
		return globalBridge;
	}

	public JSONBridge() {
		this(true);
	}

	public JSONBridge(boolean useDefaultSerializers) {
		debug = false;
		ser = new JSONSerializer();
		callbackSet = new HashSet();
		classMap = new HashMap();
		objectMap = new HashMap();
		referenceMap = new HashMap();
		referenceSer = null;
		referenceSet = new HashSet();
		callableReferenceSet = new HashSet();
		if (useDefaultSerializers)
			try {
				ser.registerDefaultSerializers();
			}
			catch (Exception e) {
				e.printStackTrace();
			}
	}

	private static ClassData analyzeClass(Class clazz) {
		log.info("analyzing " + clazz.getName());
		Method methods[] = clazz.getMethods();
		ClassData cd = new ClassData();
		cd.clazz = clazz;
		HashMap staticMethodMap = new HashMap();
		HashMap methodMap = new HashMap();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];
			if (method.getDeclaringClass() != (java.lang.Object.class)) {
				int mod = methods[i].getModifiers();
				if (Modifier.isPublic(mod)) {
					Class param[] = method.getParameterTypes();
					int argCount = 0;
					synchronized (localArgResolverMap) {
						for (int n = 0; n < param.length; n++) {
							HashSet resolvers = (HashSet) localArgResolverMap.get(param[n]);
							if (resolvers == null)
								argCount++;
						}

					}
					MethodKey mk = new MethodKey(method.getName(), argCount);
					ArrayList marr = (ArrayList) methodMap.get(mk);
					if (marr == null) {
						marr = new ArrayList();
						methodMap.put(mk, marr);
					}
					marr.add(method);
					if (Modifier.isStatic(mod)) {
						marr = (ArrayList) staticMethodMap.get(mk);
						if (marr == null) {
							marr = new ArrayList();
							staticMethodMap.put(mk, marr);
						}
						marr.add(method);
					}
				}
			}
		}

		cd.methodMap = new HashMap();
		cd.staticMethodMap = new HashMap();
		for (Iterator i = methodMap.entrySet().iterator(); i.hasNext();) {
			java.util.Map.Entry entry = (java.util.Map.Entry) i.next();
			MethodKey mk = (MethodKey) entry.getKey();
			ArrayList marr = (ArrayList) entry.getValue();
			if (marr.size() == 1)
				cd.methodMap.put(mk, marr.get(0));
			else
				cd.methodMap.put(mk, ((Object) (marr.toArray(new Method[0]))));
		}

		for (Iterator i = staticMethodMap.entrySet().iterator(); i.hasNext();) {
			java.util.Map.Entry entry = (java.util.Map.Entry) i.next();
			MethodKey mk = (MethodKey) entry.getKey();
			ArrayList marr = (ArrayList) entry.getValue();
			if (marr.size() == 1)
				cd.staticMethodMap.put(mk, marr.get(0));
			else
				cd.staticMethodMap.put(mk, ((Object) (marr.toArray(new Method[0]))));
		}

		return cd;
	}

	private static ClassData getClassData(Class clazz) {
		ClassData cd;
		synchronized (classCache) {
			cd = (ClassData) classCache.get(clazz);
			if (cd == null) {
				cd = analyzeClass(clazz);
				classCache.put(clazz, cd);
			}
		}
		return cd;
	}

	public void registerSerializer(Serializer s) throws Exception {
		ser.registerSerializer(s);
	}

	public void enableReferences() throws Exception {
		if (referenceSer == null) {
			referenceSer = new ReferenceSerializer(this);
			ser.registerSerializer(referenceSer);
		}
	}

	public void registerReference(Class clazz) throws Exception {
		synchronized (referenceSet) {
			referenceSet.add(clazz);
		}
		if (debug)
			log.info("registered reference " + clazz.getName());
	}

	protected boolean isReference(Class clazz) {
		if (referenceSet.contains(clazz))
			return true;
		if (this == globalBridge)
			return false;
		else
			return globalBridge.isReference(clazz);
	}

	public void registerCallableReference(Class clazz) throws Exception {
		synchronized (callableReferenceSet) {
			callableReferenceSet.add(clazz);
		}
		if (debug)
			log.info("registered callable reference " + clazz.getName());
	}

	protected boolean isCallableReference(Class clazz) {
		if (callableReferenceSet.contains(clazz))
			return true;
		if (this == globalBridge)
			return false;
		else
			return globalBridge.isCallableReference(clazz);
	}

	public void registerClass(String name, Class clazz) throws Exception {
		synchronized (classMap) {
			Class exists = (Class) classMap.get(name);
			if (exists != null && exists != clazz)
				throw new Exception("different class registered as " + name);
			if (exists == null)
				classMap.put(name, clazz);
		}
		if (debug)
			log.info("registered class " + clazz.getName() + " as " + name);
	}

	private ClassData resolveClass(String className) {
		Class clazz = null;
		ClassData cd = null;
		synchronized (classMap) {
			clazz = (Class) classMap.get(className);
		}
		if (clazz != null)
			cd = getClassData(clazz);
		if (cd != null) {
			if (debug)
				log.fine("found class " + cd.clazz.getName() + " named " + className);
			return cd;
		}
		if (this != globalBridge)
			return globalBridge.resolveClass(className);
		else
			return null;
	}

	public void registerObject(Object key, Object o) {
		Class clazz = o.getClass();
		ObjectInstance inst = new ObjectInstance(o);
		synchronized (objectMap) {
			objectMap.put(key, inst);
		}
		if (debug)
			log.info("registered object " + o.hashCode() + " of class " + clazz.getName() + " as " + key);
	}

	private ObjectInstance resolveObject(Object key) {
		ObjectInstance oi;
		synchronized (objectMap) {
			oi = (ObjectInstance) objectMap.get(key);
		}
		if (debug && oi != null)
			log.fine("found object " + oi.o.hashCode() + " of class " + oi.classData().clazz.getName() + " with key " + key);
		if (oi == null && this != globalBridge)
			return globalBridge.resolveObject(key);
		else
			return oi;
	}

	public void registerCallback(InvocationCallback callback, Class contextInterface) {
		synchronized (callbackSet) {
			callbackSet.add(new CallbackData(callback, contextInterface));
		}
		if (debug)
			log.info("registered callback " + callback.getClass().getName() + " with context interface " + contextInterface.getName());
	}

	public void unregisterCallback(InvocationCallback callback, Class contextInterface) {
		synchronized (callbackSet) {
			callbackSet.remove(new CallbackData(callback, contextInterface));
		}
		if (debug)
			log.info("unregistered callback " + callback.getClass().getName() + " with context " + contextInterface.getName());
	}

	public static void registerLocalArgResolver(Class argClazz, Class contextInterface, LocalArgResolver argResolver) {
		synchronized (localArgResolverMap) {
			HashSet resolverSet = (HashSet) localArgResolverMap.get(argClazz);
			if (resolverSet == null) {
				resolverSet = new HashSet();
				localArgResolverMap.put(argClazz, resolverSet);
			}
			resolverSet.add(new LocalArgResolverData(argResolver, argClazz, contextInterface));
			classCache = new HashMap();
		}
		log.info("registered local arg resolver " + argResolver.getClass().getName() + " for local class " + argClazz.getName() + " with context " + contextInterface.getName());
	}

	public static void unregisterLocalArgResolver(Class argClazz, Class contextInterface, LocalArgResolver argResolver) {
		synchronized (localArgResolverMap) {
			HashSet resolverSet = (HashSet) localArgResolverMap.get(argClazz);
			if (resolverSet != null && resolverSet.remove(new LocalArgResolverData(argResolver, argClazz, contextInterface))) {
				if (resolverSet.isEmpty())
					localArgResolverMap.remove(argClazz);
				classCache = new HashMap();
				log.info("unregistered local arg resolver " + argResolver.getClass().getName() + " for local class " + argClazz.getName() + " with context " + contextInterface.getName());
			}
			else {
				log.warning("local arg resolver " + argResolver.getClass().getName() + " not registered for local class " + argClazz.getName() + " with context " + contextInterface.getName());
			}
		}
	}

	private Method resolveMethod(HashMap methodMap, String methodName, JSONArray arguments) {
		Method method[] = null;
		MethodKey mk = new MethodKey(methodName, arguments.length());
		Object o = methodMap.get(mk);
		if (o instanceof Method) {
			Method m = (Method) o;
			if (debug)
				log.fine("found method " + methodName + "(" + argSignature(m) + ")");
			return m;
		}
		if (o instanceof Method[])
			method = (Method[]) o;
		else
			return null;
		ArrayList candidate = new ArrayList();
		if (debug)
			log.fine("looking for method " + methodName + "(" + argSignature(arguments) + ")");
		for (int i = 0; i < method.length; i++)
			try {
				candidate.add(tryUnmarshallArgs(method[i], arguments));
				if (debug)
					log.fine("+++ possible match with method " + methodName + "(" + argSignature(method[i]) + ")");
			}
			catch (Exception e) {
				if (debug)
					log.fine("xxx " + e.getMessage() + " in " + methodName + "(" + argSignature(method[i]) + ")");
			}

		MethodCandidate best = null;
		for (int i = 0; i < candidate.size(); i++) {
			MethodCandidate c = (MethodCandidate) candidate.get(i);
			if (best == null) {
				best = c;
			}
			else {
				ObjectMatch bestMatch = best.getMatch();
				ObjectMatch cMatch = c.getMatch();
				if (bestMatch.mismatch > cMatch.mismatch)
					best = c;
				else if (bestMatch.mismatch == cMatch.mismatch)
					best = betterSignature(best, c);
			}
		}

		if (best != null) {
			Method m = best.method;
			if (debug)
				log.fine("found method " + methodName + "(" + argSignature(m) + ")");
			return m;
		}
		else {
			return null;
		}
	}

	private MethodCandidate betterSignature(MethodCandidate methodCandidate, MethodCandidate methodCandidate1) {
		Method method = methodCandidate.method;
		Method method1 = methodCandidate1.method;
		Class parameters[] = method.getParameterTypes();
		Class parameters1[] = method1.getParameterTypes();
		int c = 0;
		int c1 = 0;
		for (int i = 0; i < parameters.length; i++) {
			Class parameterClass = parameters[i];
			Class parameterClass1 = parameters1[i];
			if (parameterClass != parameterClass1)
				if (parameterClass.isAssignableFrom(parameterClass1))
					c1++;
				else
					c++;
		}

		if (c1 > c)
			return methodCandidate1;
		else
			return methodCandidate;
	}

	private static String argSignature(Method method) {
		Class param[] = method.getParameterTypes();
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < param.length; i++) {
			if (i > 0)
				buf.append(",");
			buf.append(param[i].getName());
		}

		return buf.toString();
	}

	private static String argSignature(JSONArray arguments) {
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < arguments.length(); i++) {
			if (i > 0)
				buf.append(",");
			Object jso = arguments.get(i);
			if (jso == null)
				buf.append("java.lang.Object");
			else if (jso instanceof String)
				buf.append("java.lang.String");
			else if (jso instanceof Number)
				buf.append("java.lang.Number");
			else if (jso instanceof JSONArray)
				buf.append("java.lang.Object[]");
			else
				buf.append("java.lang.Object");
		}

		return buf.toString();
	}

	private MethodCandidate tryUnmarshallArgs(Method method, JSONArray arguments) throws UnmarshallException {
		MethodCandidate candidate = new MethodCandidate(method);
		Class param[] = method.getParameterTypes();
		int i = 0;
		int j = 0;
		try {
			for (; i < param.length; i++) {
				SerializerState state = new SerializerState();
				HashSet resolverSet;
				synchronized (localArgResolverMap) {
					resolverSet = (HashSet) localArgResolverMap.get(param[i]);
				}
				if (resolverSet != null)
					candidate.match[i] = ObjectMatch.OKAY;
				else
					candidate.match[i] = ser.tryUnmarshall(state, param[i], arguments.get(j++));
			}

		}
		catch (UnmarshallException e) {
			throw new UnmarshallException("arg " + (i + 1) + " " + e.getMessage());
		}
		return candidate;
	}

	private Object resolveLocalArg(Object context[], HashSet resolverSet) throws UnmarshallException {
		for (Iterator i = resolverSet.iterator(); i.hasNext();) {
			LocalArgResolverData resolverData = (LocalArgResolverData) i.next();
			for (int j = 0; j < context.length; j++)
				if (resolverData.understands(context[j]))
					try {
						return resolverData.argResolver.resolveArg(context[j]);
					}
					catch (LocalArgResolveException e) {
						throw new UnmarshallException("error resolving local argument: " + e);
					}

		}

		throw new UnmarshallException("couldn't find local arg resolver");
	}

	private Object[] unmarshallArgs(Object context[], Method method, JSONArray arguments) throws UnmarshallException {
		Class param[] = method.getParameterTypes();
		Object javaArgs[] = new Object[param.length];
		int i = 0;
		int j = 0;
		try {
			for (; i < param.length; i++) {
				SerializerState state = new SerializerState();
				HashSet resolverSet;
				synchronized (localArgResolverMap) {
					resolverSet = (HashSet) localArgResolverMap.get(param[i]);
				}
				if (resolverSet != null)
					javaArgs[i] = resolveLocalArg(context, resolverSet);
				else
					javaArgs[i] = ser.unmarshall(state, param[i], arguments.get(j++));
			}

		}
		catch (UnmarshallException e) {
			throw new UnmarshallException("arg " + (i + 1) + " " + e.getMessage());
		}
		return javaArgs;
	}

	private void preInvokeCallback(Object context, Object instance, Method m, Object arguments[]) throws Exception {
		synchronized (callbackSet) {
			for (Iterator i = callbackSet.iterator(); i.hasNext();) {
				CallbackData cbdata = (CallbackData) i.next();
				if (cbdata.understands(context))
					cbdata.cb.preInvoke(context, instance, m, arguments);
			}

		}
	}

	private void postInvokeCallback(Object context, Object instance, Method m, Object result) throws Exception {
		synchronized (callbackSet) {
			for (Iterator i = callbackSet.iterator(); i.hasNext();) {
				CallbackData cbdata = (CallbackData) i.next();
				if (cbdata.understands(context))
					cbdata.cb.postInvoke(context, instance, m, result);
			}

		}
	}

	public JSONRPCResult call(Object context[], JSONObject jsonReq) {
		JSONRPCResult result = null;
		String encodedMethod = null;
		Object requestId = null;
		JSONArray arguments = null;
		try {
			encodedMethod = jsonReq.getString("method");
			arguments = jsonReq.getJSONArray("params");
			requestId = jsonReq.opt("id");
		}
		catch (NoSuchElementException e) {
			log.severe("no method or parameters in request");
			return new JSONRPCResult(591, null, "method not found (session may have timed out)");
		}
		if (isDebug())
			log.fine("call " + encodedMethod + "(" + arguments + ")" + ", requestId=" + requestId);
		String className = null;
		String methodName = null;
		int objectID = 0;
		StringTokenizer t = new StringTokenizer(encodedMethod, ".");
		if (t.hasMoreElements())
			className = t.nextToken();
		if (t.hasMoreElements())
			methodName = t.nextToken();
		if (encodedMethod.startsWith(".obj#")) {
			t = new StringTokenizer(className, "#");
			t.nextToken();
			objectID = Integer.parseInt(t.nextToken());
		}
		ObjectInstance oi = null;
		ClassData cd = null;
		HashMap methodMap = null;
		Method method = null;
		Object itsThis = null;
		if (objectID == 0) {
			if (encodedMethod.equals("system.listMethods")) {
				HashSet m = new HashSet();
				globalBridge.allInstanceMethods(m);
				if (globalBridge != this) {
					globalBridge.allStaticMethods(m);
					globalBridge.allInstanceMethods(m);
				}
				allStaticMethods(m);
				allInstanceMethods(m);
				JSONArray methods = new JSONArray();
				for (Iterator i = m.iterator(); i.hasNext(); methods.put((String) i.next()))
					;
				return new JSONRPCResult(0, requestId, methods);
			}
			if (className == null || methodName == null || (oi = resolveObject(className)) == null && (cd = resolveClass(className)) == null)
				return new JSONRPCResult(591, requestId, "method not found (session may have timed out)");
			if (oi != null) {
				itsThis = oi.o;
				methodMap = oi.classData().methodMap;
			}
			else {
				methodMap = cd.staticMethodMap;
			}
		}
		else {
			if ((oi = resolveObject(new Integer(objectID))) == null)
				return new JSONRPCResult(591, requestId, "method not found (session may have timed out)");
			itsThis = oi.o;
			methodMap = oi.classData().methodMap;
			if (methodName.equals("listMethods")) {
				HashSet m = new HashSet();
				uniqueMethods(m, "", oi.classData().staticMethodMap);
				uniqueMethods(m, "", oi.classData().methodMap);
				JSONArray methods = new JSONArray();
				for (Iterator i = m.iterator(); i.hasNext(); methods.put((String) i.next()))
					;
				return new JSONRPCResult(0, requestId, methods);
			}
		}
		if ((method = resolveMethod(methodMap, methodName, arguments)) == null)
			return new JSONRPCResult(591, requestId, "method not found (session may have timed out)");
		try {
			if (debug)
				log.fine("invoking " + method.getReturnType().getName() + " " + method.getName() + "(" + argSignature(method) + ")");
			Object javaArgs[] = unmarshallArgs(context, method, arguments);
			for (int i = 0; i < context.length; i++)
				preInvokeCallback(context[i], itsThis, method, javaArgs);

			Object o = method.invoke(itsThis, javaArgs);
			for (int i = 0; i < context.length; i++)
				postInvokeCallback(context[i], itsThis, method, o);

			SerializerState state = new SerializerState();
			result = new JSONRPCResult(0, requestId, ser.marshall(state, o));
		}
		catch (UnmarshallException e) {
			for (int i = 0; i < context.length; i++)
				errorCallback(context[i], itsThis, method, e);

			result = new JSONRPCResult(592, requestId, e.getMessage());
		}
		catch (MarshallException e) {
			for (int i = 0; i < context.length; i++)
				errorCallback(context[i], itsThis, method, e);

			result = new JSONRPCResult(593, requestId, e.getMessage());
		}
		catch (Throwable e) {
			if (e instanceof InvocationTargetException)
				e = ((InvocationTargetException) e).getTargetException();
			for (int i = 0; i < context.length; i++)
				errorCallback(context[i], itsThis, method, e);

			result = new JSONRPCResult(490, requestId, e);
		}
		return result;
	}

	private void errorCallback(Object context, Object instance, Method method, Throwable error) {
		synchronized (callbackSet) {
			for (Iterator i = callbackSet.iterator(); i.hasNext();) {
				CallbackData cbdata = (CallbackData) i.next();
				if (cbdata.understands(context) && (cbdata.cb instanceof ErrorInvocationCallback)) {
					ErrorInvocationCallback ecb = (ErrorInvocationCallback) cbdata.cb;
					try {
						ecb.invocationError(context, instance, method, error);
					}
					catch (Throwable th) {
					}
				}
			}

		}
	}

	private void uniqueMethods(HashSet m, String prefix, HashMap methodMap) {
		MethodKey mk;
		for (Iterator i = methodMap.entrySet().iterator(); i.hasNext(); m.add(prefix + mk.methodName)) {
			java.util.Map.Entry mentry = (java.util.Map.Entry) i.next();
			mk = (MethodKey) mentry.getKey();
		}

	}

	private void allStaticMethods(HashSet m) {
		synchronized (classMap) {
			String name;
			ClassData cd;
			for (Iterator i = classMap.entrySet().iterator(); i.hasNext(); uniqueMethods(m, name + ".", cd.staticMethodMap)) {
				java.util.Map.Entry cdentry = (java.util.Map.Entry) i.next();
				name = (String) cdentry.getKey();
				Class clazz = (Class) cdentry.getValue();
				cd = getClassData(clazz);
			}

		}
	}

	private void allInstanceMethods(HashSet m) {
		synchronized (objectMap) {
			for (Iterator i = objectMap.entrySet().iterator(); i.hasNext();) {
				java.util.Map.Entry oientry = (java.util.Map.Entry) i.next();
				Object key = oientry.getKey();
				if (key instanceof String) {
					String name = (String) key;
					ObjectInstance oi = (ObjectInstance) oientry.getValue();
					uniqueMethods(m, name + ".", oi.classData().methodMap);
					uniqueMethods(m, name + ".", oi.classData().staticMethodMap);
				}
			}

		}
	}

	public JSONSerializer getSerializer() {
		return ser;
	}

	public void setSerializer(JSONSerializer ser) {
		this.ser = ser;
	}

	private static final Logger log;
	private boolean debug;
	private static JSONBridge globalBridge = new JSONBridge();
	private static HashMap classCache = new HashMap();
	private static HashMap localArgResolverMap = new HashMap();
	private JSONSerializer ser;
	private HashSet callbackSet;
	private HashMap classMap;
	private HashMap objectMap;
	protected HashMap referenceMap;
	protected Serializer referenceSer;
	protected HashSet referenceSet;
	protected HashSet callableReferenceSet;

	static {
		log = Logger.getLogger((com.metaparadigm.jsonrpc.JSONRPCBridge.class).getName());
	}
}
