package er.rest.routes.model;

import com.webobjects.eoaccess.EOProperty;

public abstract class EOPropertyProxy implements IERXProperty {
	private EOProperty _property;

	public EOPropertyProxy(EOProperty property) {
		_property = property;
	}

	protected EOProperty property() {
		return _property;
	}

	public String name() {
		return _property.name();
	}

}
