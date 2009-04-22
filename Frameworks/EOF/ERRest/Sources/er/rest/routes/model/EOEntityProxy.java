package er.rest.routes.model;

import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.extensions.eof.ERXEOControlUtilities;

public class EOEntityProxy implements IERXEntity {
	private EOEntity _entity;

	public EOEntityProxy(EOEntity entity) {
		_entity = entity;
	}
	
	public EOEntity entity() {
	  return _entity;
	}

	public IERXAttribute attributeNamed(String name) {
		EOAttribute attribute = _entity.attributeNamed(name);
		return attribute == null ? null : new EOAttributeProxy(attribute);
	}

	@SuppressWarnings("unchecked")
	public NSArray<IERXAttribute> attributes() {
		NSMutableArray<IERXAttribute> attributes = new NSMutableArray<IERXAttribute>();
		for (EOAttribute attribute : (NSArray<EOAttribute>) _entity.attributes()) {
			attributes.addObject(new EOAttributeProxy(attribute));
		}
		return attributes;
	}

	public Object createInstance(EOEditingContext editingContext) {
		return EOUtilities.createAndInsertInstance(editingContext, _entity.name());
	}

	public String name() {
		return _entity.name();
	}

	public String shortName() {
		return _entity.name();
	}

	public Object primaryKeyValue(Object obj) {
		EOEnterpriseObject eo = (EOEnterpriseObject)obj;
		return ERXEOControlUtilities.primaryKeyObjectForObject(eo);
	}

	public Object objectWithPrimaryKeyValue(EOEditingContext editingContext, Object pkValue) {
		pkValue = ((EOAttribute) _entity.primaryKeyAttributes().objectAtIndex(0)).validateValue(pkValue);
		return ERXEOControlUtilities.objectWithPrimaryKeyValue(editingContext, name(), pkValue, null, false);
	}

	public IERXEntity parentEntity() {
		EOEntity parentEntity = _entity.parentEntity();
		return parentEntity == null ? null : new EOEntityProxy(parentEntity);
	}

	@SuppressWarnings("unchecked")
	public NSArray<IERXAttribute> primaryKeyAttributes() {
		NSMutableArray<IERXAttribute> attributes = new NSMutableArray<IERXAttribute>();
		for (EOAttribute attribute : (NSArray<EOAttribute>) _entity.primaryKeyAttributes()) {
			attributes.addObject(new EOAttributeProxy(attribute));
		}
		return attributes;
	}

	@SuppressWarnings( { "cast", "unchecked" })
	public NSArray<String> propertyNames() {
		return (NSArray<String>) _entity._propertyNames();
	}

	public IERXRelationship relationshipNamed(String name) {
		EORelationship relationship = _entity.relationshipNamed(name);
		return relationship == null ? null : new EORelationshipProxy(relationship);
	}

	@SuppressWarnings("unchecked")
	public NSArray<IERXRelationship> relationships() {
		NSMutableArray<IERXRelationship> relationships = new NSMutableArray<IERXRelationship>();
		for (EORelationship relationship : (NSArray<EORelationship>) _entity.relationships()) {
			relationships.addObject(new EORelationshipProxy(relationship));
		}
		return relationships;
	}

	public IERXEntity siblingEntityNamed(String name) {
		EOEntity siblingEntity = _entity.model().modelGroup().entityNamed(name);
		return siblingEntity == null ? null : new EOEntityProxy(siblingEntity);
	}

	@SuppressWarnings("unchecked")
	public NSArray<IERXEntity> subEntities() {
		NSMutableArray<IERXEntity> subEntities = new NSMutableArray<IERXEntity>();
		for (EOEntity subEntity : (NSArray<EOEntity>) _entity.subEntities()) {
			subEntities.addObject(new EOEntityProxy(subEntity));
		}
		return subEntities;
	}

}
