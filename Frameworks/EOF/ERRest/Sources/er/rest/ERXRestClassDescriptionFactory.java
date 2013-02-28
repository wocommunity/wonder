package er.rest;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation._NSUtilities;

public class ERXRestClassDescriptionFactory {
	private static Map<Class<?>, EOClassDescription> _classDescriptionByClass = new ConcurrentHashMap<Class<?>, EOClassDescription>();
	private static Map<String, Class<?>> _classByName = new ConcurrentHashMap<String, Class<?>>();

	public static String _guessMismatchedCaseEntityName(String mismatchedCaseEntityName) {
		String guessedEntityName = null;
		for (EOModel model : EOModelGroup.defaultGroup().models()) {
			for (EOEntity entity : model.entities()) {
				if (entity.name().equalsIgnoreCase(mismatchedCaseEntityName)) {
					guessedEntityName = entity.name();
					break;
				}
			}
			if (guessedEntityName != null) {
				break;
			}
		}
		if (guessedEntityName != null) {
			for (String entityName : _classByName.keySet()) {
				if (entityName.equalsIgnoreCase(mismatchedCaseEntityName)) {
					guessedEntityName = entityName;
				}
			}
		}
		return guessedEntityName;
	}

	public static void registerClassDescription(EOClassDescription classDescription, Class<?> clazz) {
		_classDescriptionByClass.put(clazz, classDescription);
	}

	@SuppressWarnings("unchecked")
	public static EOClassDescription classDescriptionForObject(Object obj) {
		return ERXRestClassDescriptionFactory.classDescriptionForObject(obj, false);
	}

	@SuppressWarnings("unchecked")
	public static EOClassDescription classDescriptionForObject(Object obj, boolean forceNonEntity) {
		EOClassDescription classDescription;
		if (obj == null) {
			classDescription = null;
		}
		else if (obj instanceof EOEnterpriseObject && !forceNonEntity) {
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
			classDescription = null; 
			if (classDescription == null && !forceNonEntity) {
				classDescription = ERXRestClassDescriptionFactory.classDescriptionForClass(clazz, true);
			}
			if (classDescription == null && !forceNonEntity) {
				classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(clazz.getSimpleName());
			}
			if (classDescription == null) {
				classDescription = new BeanInfoClassDescription(clazz);
			}
		}
		return classDescription;
	}

	/**
	 * Returns the entity name for the given object.
	 * 
	 * @param obj
	 *            the object to return an entity name for
	 * @return the entity name for the given object
	 */
	public static String entityNameForObject(Object obj) {
		String entityName;
		if (obj instanceof EOEnterpriseObject) {
			entityName = ((EOEnterpriseObject) obj).entityName();
		}
		else {
			entityName = obj.getClass().getSimpleName();
		}
		return entityName;
	}

	public static EOClassDescription classDescriptionForEntityName(String entityName) {
		if (entityName == null) {
			throw new NullPointerException("You did not specify an entityName.");
		}
		EOClassDescription classDescription = EOClassDescription.classDescriptionForEntityName(entityName);
		if (classDescription == null) {
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
				classDescription = EOClassDescription.classDescriptionForEntityName(ERXRestClassDescriptionFactory._guessMismatchedCaseEntityName(entityName));
			}
		}
		return classDescription;
	}

	public static EOClassDescription classDescriptionForClass(Class clazz, boolean forceNonEntity) {
		EOClassDescription classDescription = _classDescriptionByClass.get(clazz);
		if (classDescription == null && !forceNonEntity && EOEnterpriseObject.class.isAssignableFrom(clazz)) {
			classDescription = ERXRestClassDescriptionFactory.classDescriptionForEntityName(clazz.getSimpleName());
		}
		if (classDescription == null) {
			if (NSDictionary.class.isAssignableFrom(clazz)) {
				classDescription = new NSDictionaryClassDescription();
			}
			else if (Map.class.isAssignableFrom(clazz)) {
				classDescription = new MapClassDescription();
			}
			else {
				classDescription = new BeanInfoClassDescription(clazz);
			}
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
