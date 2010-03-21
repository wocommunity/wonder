package er.rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation._NSUtilities;

public class ERXRestClassDescriptionFactory {
	private static Map<Class<?>, EOClassDescription> _classDescriptionByClass = new ConcurrentHashMap<Class<?>, EOClassDescription>();
	private static Map<String, Class<?>> _classByName = new ConcurrentHashMap<String, Class<?>>();

	public static void registerClassDescription(EOClassDescription classDescription, Class<?> clazz) {
		_classDescriptionByClass.put(clazz, classDescription);
	}

	@SuppressWarnings("unchecked")
	public static EOClassDescription classDescriptionForObject(Object obj) {
		EOClassDescription classDescription;
		if (obj == null) {
			classDescription = null;
		}
		else if (obj instanceof EOEnterpriseObject) {
			classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(((EOEnterpriseObject) obj).entityName());
		}
		else if (obj instanceof Map) {
			classDescription = new MapClassDescription((Map<String, ?>) obj);
		}
		else {
			Class clazz;
			if (obj instanceof Class) {
				clazz = (Class) obj;
			}
			else {
				clazz = obj.getClass();
			}
			classDescription = ERXRestClassDescriptionFactory.classDescriptionForClass(clazz, true);
			if (classDescription == null) {
				classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(clazz.getSimpleName());
			}
			if (classDescription == null) {
				classDescription = new BeanInfoClassDescription(clazz);
			}
		}
		return classDescription;
	}

	public static EOClassDescription classDescriptionForEntityName(String entityName) {
		EOClassDescription classDescription = EOClassDescription.classDescriptionForEntityName(entityName);
		if (classDescription == null) {
			if (entityName == null) {
				throw new NullPointerException("You did not specify an entityName.");
			}
			Class clazz = _classByName.get(entityName);
			if (clazz == null) {
				clazz = _NSUtilities.classWithName(entityName);
				if (clazz == null) {
					try {
						clazz = Class.forName(entityName);
					}
					catch (ClassNotFoundException e) {
						clazz = null;
					}
				}
			}
			if (clazz != null) {
				classDescription = ERXRestClassDescriptionFactory.classDescriptionForClass(clazz, true);
			}
			else {
				classDescription = null;
			}
		}
		return classDescription;
	}

	public static EOClassDescription classDescriptionForClass(Class clazz, boolean forceNonEntity) {
		EOClassDescription classDescription = _classDescriptionByClass.get(clazz);
		if (classDescription == null && !forceNonEntity) {
			classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(clazz.getSimpleName());
		}
		if (classDescription == null) {
			classDescription = new BeanInfoClassDescription(clazz);
		}
		return classDescription;
	}

	public static EOClassDescription registerClass(Class clazz) {
		return ERXRestClassDescriptionFactory.registerClassForEntityNamed(clazz, clazz.getSimpleName());
	}

	public static EOClassDescription registerClassForEntityNamed(Class clazz, String entityName) {
		EOClassDescription classDescription = classDescriptionForClass(clazz, false);
		ERXRestClassDescriptionFactory.registerClassDescription(classDescription, clazz);
		_classByName.put(entityName, clazz);
		return classDescription;
	}
}
