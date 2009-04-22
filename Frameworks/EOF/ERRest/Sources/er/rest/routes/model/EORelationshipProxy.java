package er.rest.routes.model;

import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;

public class EORelationshipProxy extends EOPropertyProxy implements IERXRelationship {

	public EORelationshipProxy(EORelationship relationship) {
		super(relationship);
	}

	protected EORelationship relationship() {
		return (EORelationship) property();
	}

	public IERXEntity destinationEntity() {
		EOEntity destinationEntity = relationship().destinationEntity();
		return destinationEntity == null ? null : new EOEntityProxy(destinationEntity);
	}

	public boolean isMandatory() {
		return relationship().isMandatory();
	}

	public boolean isToMany() {
		return relationship().isToMany();
	}

	public boolean isClassProperty() {
		return relationship().entity().classProperties().containsObject(relationship());
	}
}
