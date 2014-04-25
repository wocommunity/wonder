package er.examples.erjaxws.components;

import com.webobjects.appserver.WOContext;

import er.examples.erjaxws.Application;
import er.examples.erjaxws.Session;
import er.extensions.components.ERXComponent;

@SuppressWarnings("serial")
public class BaseComponent extends ERXComponent {
	public BaseComponent(WOContext context) {
		super(context);
	}
	
	@Override
	public Application application() {
		return (Application)super.application();
	}
	
	@Override
	public Session session() {
		return (Session)super.session();
	}
}
