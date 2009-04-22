package er.rest.routes.model;

import java.beans.PropertyDescriptor;

public class BeanInfoAttributeProxy extends BeanInfoPropertyProxy implements IERXAttribute {
	public BeanInfoAttributeProxy(PropertyDescriptor descriptor) {
		super(descriptor);
	}

	public boolean allowsNull() {
		return true;
	}

}
