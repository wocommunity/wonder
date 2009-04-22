package er.rest.routes.model;

import java.beans.PropertyDescriptor;

public class BeanInfoPropertyProxy implements IERXProperty {
	private PropertyDescriptor _descriptor;

	public BeanInfoPropertyProxy(PropertyDescriptor descriptor) {
		_descriptor = descriptor;
	}

	protected PropertyDescriptor descriptor() {
		return _descriptor;
	}

	public boolean isClassProperty() {
		return true;
	}

	public String name() {
		return _descriptor.getName();
	}

}
