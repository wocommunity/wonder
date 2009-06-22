package er.extensions.components;

import com.webobjects.appserver.WOContext;

/**
 * Stateless variant of {@link ERXInlineTemplate}.
 * @author th
 *
 */
public class ERXStatelessInlineTemplate extends ERXInlineTemplate {
    public ERXStatelessInlineTemplate(WOContext context) {
        super(context);
    }
    
    /** component is stateless */
    public boolean isStateless() { return true; }

    /* (non-Javadoc)
     * @see com.webobjects.appserver.WOComponent#reset()
     */
    public void reset() {
        super.reset();
        _dynamicBindings = null;
        _deferredError = null;
    }

}