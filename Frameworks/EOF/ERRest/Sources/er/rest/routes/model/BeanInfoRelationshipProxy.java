package er.rest.routes.model;

import java.beans.PropertyDescriptor;

import com.sun.tools.javac.util.List;

public class BeanInfoRelationshipProxy extends BeanInfoPropertyProxy implements IERXRelationship {
	public BeanInfoRelationshipProxy(PropertyDescriptor descriptor) {
		super(descriptor);
	}

	public IERXEntity destinationEntity() {
		return new ClassEntityProxy(descriptor().getPropertyType());
	}

	public boolean isMandatory() {
		return false;
	}

	public boolean isToMany() {
		return List.class.isAssignableFrom(descriptor().getPropertyType());
	}

}
