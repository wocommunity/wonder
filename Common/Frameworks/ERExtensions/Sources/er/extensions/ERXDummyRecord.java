package er.extensions;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Enumeration;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOArrayDataSource;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOCustomObject;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableArray;

/**
 * Put POJOs into EOF (sort of). This class is mainly usefull when used with
 * D2W, when you don't want to create components for non-persistent objects.
 * Should be regarded as experimental :) 
 * Thing to watch out for: <ul>
 * <li> you can't create new objects
 * <li> reverting the EC doesn't revert the objects
 * <li> you should use a new EC with these objects
 * <li> auto-discovery of attributes is very lame
 * </ul>
 * Here's a usage example, showing how to
 * call up an edit page for a single "object" and a list page for an array. Note 
 * that the list-inspect-edit workflow and sorting, batching etc work out of the box.
 * <pre><code>
 * public class Main extends WOComponent {
 * 
 * 	public static class Test {
 * 		
 * 		public String string;
 * 		public Number number;
 * 		public Boolean flag;
 * 		
 * 		public Test(String string, Number number, Boolean flag) {
 * 			this.string = string;
 * 			this.number = number;
 * 			this.flag = flag;
 * 		}
 * 	}
 * 	
 * 	public Test object;
 * 	public NSArray list;
 * 
 * 	public Main(WOContext context) {
 * 		super(context);
 * 		ERXDummyRecord.registerDescriptionForClass(Test.class, null);
 * 		NSMutableArray l = new NSMutableArray();
 * 		for (int i = 0; i &gt; 5; i++) {
 * 			Test o = new Test("Foo "+ i, new Integer(i^i % (i+1)), i % 2 == 0? Boolean.TRUE : Boolean.FALSE);
 * 			l.addObject(o);
 * 		}
 * 		object = (Test) l.lastObject();
 * 		list = l.immutableClone();
 * 	}
 * 
 * 	public WOComponent editObject() {
 * 		EOEnterpriseObject eo = ERXDummyRecord.recordForObject(session().defaultEditingContext(), object);
 * 		WOComponent result = D2W.factory().pageForTaskAndEntityNamed("edit", eo.entityName(), session());
 * 		result.takeValueForKey(eo, "object");
 * 		result.takeValueForKey(context().page(), "nextPage");
 * 		return result;
 * 	}
 * 
 * 	public WOComponent showList() {
 * 		EOArrayDataSource ds = ERXDummyRecord.dataSourceForObjects(session().defaultEditingContext(), list);
 * 		ds.setArray(objects);
 * 		WOComponent result = D2W.factory().pageForTaskAndEntityNamed("list", ds.classDescriptionForObjects().entityName(), session());
 * 		result.takeValueForKey(ds, "dataSource");
 * 		result.takeValueForKey(context().page(), "nextPage");
 * 		return result;
 * 	}
 * }</code></pre>
 * @author ak
 */

public class ERXDummyRecord extends EOCustomObject {

	private Object object;

	protected ERXDummyRecord(Object o) {
		object = o;
		EOClassDescription classDescription = classDescriptionForObject(object);
		__setClassDescription(classDescription);
	}

	public Object object() {
		return object;
	}
	
	public static class GlobalID extends EOGlobalID {

		private Object object;

		public GlobalID(Object o) {
			object = o;
		}

		public boolean equals(Object obj) {
			if (obj instanceof GlobalID) {
				GlobalID gid = (GlobalID) obj;
				return gid.object == object;
			}
			return false;
		}

		public int hashCode() {
			return System.identityHashCode(object);
		}

		public boolean isTemporary() {
			return true;
		}
	}

	public static class ProxyBinding extends NSKeyValueCoding._KeyBinding {

		public ProxyBinding(String key) {
			super(null, key);
		}

		public Object valueInObject(Object object) {
			ERXDummyRecord eo = (ERXDummyRecord) object;
			return NSKeyValueCoding.Utility.valueForKey(eo.object(), _key);
		}

		public void setValueInObject(Object value, Object object) {
			ERXDummyRecord eo = (ERXDummyRecord) object;
			NSKeyValueCoding.Utility.takeValueForKey(eo.object(), value, _key);
		}
	}

	public NSKeyValueCoding._KeyBinding _otherStorageBinding(String key) {
		NSKeyValueCoding._KeyBinding result = new ProxyBinding(key);
		return result;
	}

	private static EOModel pojoModel;

	public static EOClassDescription classDescriptionForObject(Object object) {
		return EOClassDescription.classDescriptionForClass(object.getClass());
	}
	
	private static NSArray fieldNamesFromClass(Class clazz) {
		NSMutableArray fieldNames = new NSMutableArray();
		Field f[] = clazz.getDeclaredFields();
		for (int i = 0; i < f.length; i++) {
			Field field = f[i];
			fieldNames.addObject(field.getName().replaceFirst("^_", ""));
		}
		return fieldNames;
	}

	private static Field _fieldForName(Class clazz, String name) {
		try {
			return clazz.getDeclaredField(name);
		}
		catch (SecurityException e) {
		}
		catch (NoSuchFieldException e) {
		}
		return null;
	}

	private static Field fieldForName(Class clazz, String name) {
		Field result = _fieldForName(clazz, name);
		if(result == null) {
			result = _fieldForName(clazz, "_" + name);
		}
		return result;
	}

	private static Method _methodForName(Class clazz, String name) {
		try {
			return clazz.getDeclaredMethod(name, null);
		}
		catch (SecurityException e) {
		}
		catch (NoSuchMethodException e) {
		}
		return null;
	}

	private static Method methodForName(Class clazz, String name) {
		Method result = _methodForName(clazz, "get" + ERXStringUtilities.capitalize(name));
		if(result == null) {
			result = _methodForName(clazz, name);
		}
		if(result == null) {
			result = _methodForName(clazz, "_" + name);
		}
		return result;
	}

	public static synchronized void registerDescriptionForClass(Class clazz, NSArray keys) {
		String entityName = "Pojo" + ERXStringUtilities.lastPropertyKeyInKeyPath(clazz.getName().replaceAll("\\$", ""));
		if (pojoModel == null) {
			pojoModel = new EOModel();
			pojoModel.setName("PojoModel");
			EOModelGroup.defaultGroup().addModel(pojoModel);
		}
		EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
		/*if (entity != null) {
			pojoModel.removeEntity(entity);
			entity = null;
		}*/
		EOClassDescription classDescription;
		if (entity == null) {
			entity = new EOEntity();
			entity.setName(entityName);
			keys = (keys == null ? fieldNamesFromClass(clazz) : keys);
			for (Enumeration iter = keys.objectEnumerator(); iter.hasMoreElements();) {
				String name = (String) iter.nextElement();
				EOAttribute attribute = new EOAttribute();
				attribute.setName(name);
				Method m = methodForName(clazz, name);
				if(m != null) {
					attribute.setClassName(m.getReturnType().getName());
				} else {
					Field f = fieldForName(clazz, name);
					if(f != null) {
						String type = f.getType().getName();
						if("boolean".equals(type)) {
							type = "java.lang.Boolean";
						} else if("int".equals(type)) {
							type = "java.lang.Number";
						} else if("long".equals(type)) {
							type = "java.lang.Number";
						} else if("short".equals(type)) {
							type = "java.lang.Number";
						}

						attribute.setClassName(type);
					}
				}
				entity.addAttribute(attribute);
			}
			classDescription = new EOEntityClassDescription(entity);
			NSKeyValueCoding.Utility.takeValueForKey(entity, classDescription, "classDescription");
			EOClassDescription.registerClassDescription(classDescription, clazz);
			pojoModel.addEntity(entity);
		} else {
			// classDescription = entity.classDescriptionForInstances();
		}
	}

	public static EOArrayDataSource dataSourceForObjects(EOEditingContext ec, NSArray list) {
		EOClassDescription cd = null;
		NSMutableArray objects = new NSMutableArray();
		for (Enumeration iter = list.objectEnumerator(); iter.hasMoreElements();) {
			Object o = (Object) iter.nextElement();

			EOEnterpriseObject eo = recordForObject(ec, o);
			ec = eo.editingContext();
			cd = eo.classDescription();
			objects.addObject(eo);
		}
		EOArrayDataSource ds = new EOArrayDataSource(cd, ec);
		ds.setArray(objects);
		return ds;
	}

	public static EOEnterpriseObject recordForObject(EOEditingContext ec, Object o) {
		EOGlobalID gid = new GlobalID(o);

		EOEnterpriseObject eo = ec.objectForGlobalID(gid);
		if(eo == null) {
			eo = new ERXDummyRecord(o);
			ec.recordObject(eo, gid);
		}
		return eo;
	}
}
