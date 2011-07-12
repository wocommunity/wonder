package er.jrexample.components;

import com.webobjects.appserver.WOContext;

import er.extensions.components.ERXComponent;

public class PageWrapper extends ERXComponent {
    public PageWrapper(WOContext context) {
        super(context);
    }
    
    @Override
	public boolean synchronizesVariablesWithBindings() {
		// makes this component non-synchronizing
		return false;
	}

	@Override
	public boolean isStateless() {
		// makes this component stateless
		return true;
	}

	@Override
	public void reset() {
		// resets ivars at the end or RR phases
		super.reset();
	}


}