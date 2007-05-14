package er.extensions.rest;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEditingContext;

public class ERXRestContext {
	private WOContext _context;
	private EOEditingContext _editingContext;
	private IERXRestDelegate _delegate;

	public ERXRestContext(WOContext context, EOEditingContext editingContext) {
		_context = context;
		_editingContext = editingContext;
	}

	public WOContext context() {
		return _context;
	}

	public EOEditingContext editingContext() {
		return _editingContext;
	}

	public IERXRestDelegate delegate() {
		return _delegate;
	}

	public void setDelegate(IERXRestDelegate delegate) {
		_delegate = delegate;
	}
}
