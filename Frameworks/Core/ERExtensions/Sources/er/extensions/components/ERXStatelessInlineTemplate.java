package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * Stateless variant of {@link ERXInlineTemplate}.
 * @author th
 */
public class ERXStatelessInlineTemplate extends ERXInlineTemplate {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public ERXStatelessInlineTemplate(WOContext context) {
        super(context);
    }
    
    /** component is stateless */
    @Override
    public boolean isStateless() { return true; }

    @Override
    public void reset() {
        super.reset();
        _dynamicBindings = null;
        _deferredError = null;
    }
}
