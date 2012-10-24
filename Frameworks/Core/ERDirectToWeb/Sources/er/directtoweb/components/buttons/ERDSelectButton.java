package er.directtoweb.components.buttons;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.directtoweb.D2WPage;
import com.webobjects.directtoweb.SelectPageInterface;

/**
 * Select button to display in lists.
 *
 * @binding d2wContext the d2wContext
 *
 * @author ak on Mon Sep 01 2003
 */
public class ERDSelectButton extends ERDActionButton {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDSelectButton.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDSelectButton(WOContext context) {
        super(context);
    }

    public WOComponent selectObjectAction() {
        SelectPageInterface parent = parentSelectPage();
        if(parent != null) {
            parent.setSelectedObject(object());
            return nextPageInPage((D2WPage)parent);
        }
        throw new IllegalStateException("This page is not an instance of SelectPageInterface. I can't select here.");
    }
}
