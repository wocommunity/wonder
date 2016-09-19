package er.rest;

import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;

/**
 * The delegate interface used to convert objects to and from request nodes.
 * 
 * @author mschrag
 */
public interface IERXRestDelegate {
	/**
	 * Returns the primary key for the specified object.
	 * 
	 * @param obj
	 *            the object to return a pk for
	 * @return the primary key of the object
	 */
	public Object primaryKeyForObject(Object obj, ERXRestContext context);

	/**
	 * Creates a new instance of the entity.
	 * 
	 * @param entity
	 *            the entity
	 * @return a new instance of the entity
	 */
	public Object createObjectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context);

	/**
	 * Returns the object with the given entity and ID.
	 * 
	 * @param entity
	 *            the entity
	 * @param id
	 *            the ID of the object
	 * @return the object with the given entity and ID
	 */
	public Object objectOfEntityWithID(EOClassDescription entity, Object id, ERXRestContext context);

	/**
	 * This API will likely change. Override if you have to for now, but I'm not
	 * sure if it makes more sense to return an array of pk classes, a map of
	 * pk to pk class, this boolean, or an array of pk attribute names. If we
	 * return pk names, we could probably get rid of primaryKeyForObject, or
	 * at least fully implement it in ERXAbstractRestDelegate, but I don't
	 * want to fully commit to this API yet. In the meantime, this at least
	 * provides a stapgap for automatic registration.
	 * 
	 * @param classDescription
	 *            the class description in question
	 * @return whether or not the given class description has numeric pks
	 */
	public boolean __hasNumericPrimaryKeys(EOClassDescription classDescription);

	/**
	 * A Factory for creating IERXRestDelegates. Right now it's just hard-coded, but this is being added for a later
	 * extension point.
	 * 
	 * @author mschrag
	 */
	public static class Factory {
		private static NSMutableDictionary<String, IERXRestDelegate> _delegates = new NSMutableDictionary<>();
		private static IERXRestDelegate _defaultDelegate = new ERXEORestDelegate();
		private static IERXRestDelegate _defaultBeanDelegate = new ERXNoOpRestDelegate();

		/**
		 * Sets the default rest delegate to use for EO's when no other can be found. The default is ERXEORestDelegate.
		 * 
		 * @param defaultDelegate
		 *            the default delegate to use
		 */
		public static void setDefaultDelegate(IERXRestDelegate defaultDelegate) {
			IERXRestDelegate.Factory._defaultDelegate = defaultDelegate;
		}

		/**
		 * Sets the default rest delegate to use for non-EO's when no other can be found. The default is
		 * ERXNoOpRestDelegate.
		 * 
		 * @param defaultDelegate
		 *            the default delegate to use
		 */
		public static void setDefaultBeanDelegate(IERXRestDelegate defaultDelegate) {
			IERXRestDelegate.Factory._defaultBeanDelegate = defaultDelegate;
		}

		/**
		 * Registers a rest delegate for the given entity name.
		 * 
		 * @param delegate
		 *            the delegate to register
		 * @param entityName
		 *            the entity name to register for
		 */
		public static void setDelegateForEntityNamed(IERXRestDelegate delegate, String entityName) {
			_delegates.setObjectForKey(delegate, entityName);
		}

		/**
		 * Registers a rest delegate for the given entity name.
		 * 
		 * @param delegate
		 *            the delegate class to register
		 * @param entityName
		 *            the entity name to register for
		 */
		public static void setDelegateForEntityNamed(IERXRestDelegate delegate, String entityName, Class<?> clazz) {
			_delegates.setObjectForKey(delegate, entityName);
			ERXRestClassDescriptionFactory.registerClass(clazz);
		}

		/**
		 * Returns a rest delegate for the given entity name.
		 * 
		 * @param entityName
		 *            the name o the entity to lookup
		 * @return a rest delegate
		 */
		public static IERXRestDelegate delegateForEntityNamed(String entityName) {
			return IERXRestDelegate.Factory.delegateForClassDescription(ERXRestClassDescriptionFactory.classDescriptionForEntityName(entityName));
		}

		public static IERXRestDelegate delegateForObject(Object object) {
			IERXRestDelegate delegate = null;
			if (object instanceof EOEnterpriseObject) {
				delegate = IERXRestDelegate.Factory.delegateForClassDescription(((EOEnterpriseObject) object).classDescription());
			}
			else if (object != null) {
				delegate = IERXRestDelegate.Factory.delegateForClassDescription(ERXRestClassDescriptionFactory.classDescriptionForObject(object, false));
			}
			return delegate;
		}

		public static IERXRestDelegate delegateForClassDescription(EOClassDescription classDescription) {
			String entityName = classDescription.entityName();
			IERXRestDelegate delegate = _delegates.objectForKey(entityName);
			if (delegate == null) {
				Class<?> possibleDelegateClass = _NSUtilities.classWithName(entityName + "RestDelegate");
				if (possibleDelegateClass != null) {
					try {
						delegate = possibleDelegateClass.asSubclass(IERXRestDelegate.class).newInstance();
						setDelegateForEntityNamed(delegate, entityName);
					}
					catch (Throwable t) {
						throw new RuntimeException("Failed to create a delegate for the entity '" + entityName + "'.", t);
					}
				}
			}

			if (delegate == null) {
				try {
					if (classDescription instanceof EOEntityClassDescription) {
						delegate = IERXRestDelegate.Factory._defaultDelegate;
					}
					else {
						delegate = IERXRestDelegate.Factory._defaultBeanDelegate;
					}
					setDelegateForEntityNamed(delegate, entityName);
				}
				catch (Exception e) {
					throw new RuntimeException("Failed to create the rest delegate '" + _defaultDelegate + ".", e);
				}
			}

			return delegate;
		}
	}
}