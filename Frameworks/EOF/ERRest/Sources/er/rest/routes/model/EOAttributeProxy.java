package er.rest.routes.model;

import com.webobjects.eoaccess.EOAttribute;

public class EOAttributeProxy extends EOPropertyProxy implements IERXAttribute {
	public EOAttributeProxy(EOAttribute attribute) {
		super(attribute);
	}

	public EOAttribute attribute() {
		return (EOAttribute) property();
	}

	public boolean allowsNull() {
		return attribute().allowsNull();
	}

	public boolean isClassProperty() {
		return attribute().entity().classProperties().containsObject(attribute());
	}
}
