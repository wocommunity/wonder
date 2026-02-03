package er.r2d2w.components.repetitions;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODisplayGroup;
import com.webobjects.foundation.NSDictionary;

import er.directtoweb.components.ERDCustomComponent;

public class R2DListInspectRepetition extends ERDCustomComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 2L;

	public R2DListInspectRepetition(WOContext context) {
        super(context);
    }
	
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
    
    public WODisplayGroup displayGroup() {
    	return (WODisplayGroup) valueForBinding("displayGroup");
    }

	public String listElement() {
		return displayGroup().sortOrderings().isEmpty()?"ul":"ol";
	}

	public String startValue() {
		if(displayGroup().sortOrderings().isEmpty()) {
			return null;
		}
		return String.valueOf(displayGroup().indexOfFirstDisplayedObject());
	}

	public NSDictionary<String,String> settings() {
        String pc = d2wContext().dynamicPage();
        if(pc != null) {
            return new NSDictionary<String,String>(pc, "parentPageConfiguration");
        }
        return null;
    }
}