package er.modern.look.components;

import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WContext;

import er.extensions.components.ERXComponent;

public class ERMODComponent extends ERXComponent{

	private D2WContext _d2wContext;
	
	public ERMODComponent(WOContext context) {
		super(context);
	}
	
    public D2WContext d2wContext() {
    	if (_d2wContext == null) {
			_d2wContext = (D2WContext)objectValueForBinding("d2wContext");
		}
		return _d2wContext;
    }
    
    public void setD2wContext(D2WContext c) {
    	_d2wContext = c;
    }
	
	
}
