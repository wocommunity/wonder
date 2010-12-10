package er.directtoweb.components.repetitions;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOContext;
import com.webobjects.eocontrol.EOEnterpriseObject;

/**
 * Class for DirectToWeb Component ERDInspectPageRepetition.
 *
 * @author ak on Mon Sep 01 2003
 * @project ERDirectToWeb
 * @d2wKey componentName
 * @d2wKey colSpan
 * @d2wKey rowSpan
 * @d2wKey maxColumns
 * @d2wKey matrixLayoutVertical
 * @d2wKey useMatrixLayout
 * @d2wKey useTableLayout
 * @d2wKey propertyNameComponentName
 * @d2wKey sectionComponentName
 * @d2wKey useHorizontalLayoutTables
 */
public class ERDInspectPageRepetition extends ERDAttributeRepetition {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERDInspectPageRepetition.class);
	
    /**
     * Public constructor
     * @param context the context
     */
    public ERDInspectPageRepetition(WOContext context) {
        super(context);
    }

    /** component does not synchronize it's variables */
    public boolean synchronizesVariablesWithBindings() { return false; }

    public EOEnterpriseObject object() {
        return (EOEnterpriseObject)valueForBinding("object");
    }
}
