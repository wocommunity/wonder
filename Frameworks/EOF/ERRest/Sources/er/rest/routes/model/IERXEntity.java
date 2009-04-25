package er.rest.routes.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation._NSUtilities;

import er.extensions.eof.ERXEOAccessUtilities;

public interface IERXEntity {

	public static class Factory {
		private static Map<String, IERXEntity> _entityRegistry = new ConcurrentHashMap<String, IERXEntity>();

		public static void registerEntityNamed(IERXEntity entity, String name) {
			_entityRegistry.put(name, entity);
		}

		public static IERXEntity registeredEntityNamed(String name) {
			return _entityRegistry.get(name);
		}

		public static IERXEntity entityNamed(EOEditingContext editingContext, String name) {
			IERXEntity entity = IERXEntity.Factory.registeredEntityNamed(name);
			if (entity == null) {
				EOEntity eoEntity = null;
				if (editingContext != null) {
					eoEntity = ERXEOAccessUtilities.entityNamed(editingContext, name);
				}
				if (eoEntity != null) {
					entity = new EOEntityProxy(eoEntity);
				}
				else {
					Class clazz = _NSUtilities.classWithName(name);
					if (clazz == null) {
						try {
							clazz = Class.forName(name);
						}
						catch (ClassNotFoundException e) {
							clazz = null;
						}
					}
					if (clazz != null) {
						entity = new ClassEntityProxy(clazz);
					}
					else {
						entity = null;
					}
				}
			}
			return entity;
		}

		public static IERXEntity entityNamed(IERXEntity siblingEntity, String name) {
			IERXEntity entity = IERXEntity.Factory.registeredEntityNamed(name);
			if (entity == null) {
				entity = siblingEntity.siblingEntityNamed(name);
				if (entity == null) {
					entity = IERXEntity.Factory.entityNamed((EOEditingContext) null, name);
				}
			}
			return entity;
		}

		public static IERXEntity entityForObject(Object obj) {
			IERXEntity entity;
			if (obj == null) {
				entity = null;
			}
			else if (obj instanceof EOEnterpriseObject) {
				entity = new EOEntityProxy(ERXEOAccessUtilities.entityForEo((EOEnterpriseObject) obj));
			}
			else {
				Class clazz;
				if (obj instanceof Class) {
					clazz = (Class) obj;
				}
				else {
					clazz = obj.getClass();
				}
				entity = IERXEntity.Factory.registeredEntityNamed(clazz.getName());
				if (entity == null) {
					entity = IERXEntity.Factory.registeredEntityNamed(clazz.getSimpleName());
				}
				if (entity == null) {
					entity = new ClassEntityProxy(clazz);
				}
			}
			return entity;
		}
	}

	public String name();

	public String shortName();

	public IERXEntity parentEntity();

	public IERXRelationship relationshipNamed(String name);

	public IERXAttribute attributeNamed(String name);

	public NSArray<IERXAttribute> primaryKeyAttributes();

	public NSArray<String> propertyNames();

	public NSArray<IERXEntity> subEntities();

	public NSArray<IERXAttribute> attributes();

	public NSArray<IERXRelationship> relationships();

	public IERXEntity siblingEntityNamed(String name);

	public Object primaryKeyValue(Object obj);

	public Object createInstance(EOEditingContext editingContext);

	public Object objectWithPrimaryKeyValue(EOEditingContext editingContext, Object pkValue);
}
