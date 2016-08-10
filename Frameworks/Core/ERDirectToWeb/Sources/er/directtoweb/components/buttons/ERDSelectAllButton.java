package er.directtoweb.components.buttons;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

import er.directtoweb.interfaces.ERDPickPageInterface;

/**
 * Class for DirectToWeb Component ERDSelectAllButton.
 *
 * @binding sample sample binding explanation
 * @d2wKey sample sample d2w key
 *
 * @author ak on Fri Sep 05 2003
 */
public class ERDSelectAllButton extends ERDActionButton {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDSelectAllButton.class);
    
    /**
     * Public constructor
     * @param context the context
     */
    public ERDSelectAllButton(WOContext context) {
        super(context);
    }

    /** Utility to return the enclosing pick page, if there is one. */
    @Override
    protected ERDPickPageInterface parentPickPage() {
        return (ERDPickPageInterface)enclosingPageOfClass(ERDPickPageInterface.class);
    }

    /** Selects all objects. */
    public WOComponent selectAllAction() {
        ERDPickPageInterface parent = parentPickPage();
        if(parent != null) {
            NSMutableArray selectedObjects = new NSMutableArray();
            NSArray list = displayGroup().allObjects();
            if(displayGroup().qualifier() != null) {
                list = EOQualifier.filteredArrayWithQualifier(list, displayGroup().qualifier());
            }
            for (Enumeration e=list.objectEnumerator();e.hasMoreElements();) {
                selectedObjects.addObject(e.nextElement());
            }
            parent.setSelectedObjects(selectedObjects);
        }
        return null;
    }
}
