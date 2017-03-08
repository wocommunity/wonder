package er.directtoweb.components.misc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOSession;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;

import er.extensions.components.ERXStatelessComponent;

/**
 * Simple D2WContext inspector component, showing the bound context's local
 * values to help debugging. Uses a shorter representation instead of toString()
 * for WOSession, EOAttribute, EOEntity and EORelationship objects and skips the
 * contextDictionary key.
 * 
 * @author fpeters
 *
 */
public class ERD2WContextInspector extends ERXStatelessComponent {

    private static final long serialVersionUID = 1L;

    public ERD2WContextInspector(WOContext context) {
        super(context);
    }

    @SuppressWarnings({ "unchecked" })
    public String d2wContextString() {
        D2WContext context = (D2WContext) valueForBinding("d2wContext");
        StringBuilder d2wContextString = new StringBuilder();
        List<String> keys = new ArrayList<>(context._localValues().keySet());
        Collections.sort(keys);
        for (String key : keys) {
            Object value = context.valueForKey(key);
            // skip contextDictionary
            if (!"contextDictionary".equals(key)) {
                if (d2wContextString.length() > 0) {
                    d2wContextString.append(";\n");
                }
                if (value instanceof WOSession) {
                    d2wContextString.append(key + ": " + ((WOSession) value).sessionID());
                } else if (value instanceof EOAttribute) {
                    d2wContextString.append(key + ": " + ((EOAttribute) value).name());
                } else if (value instanceof EOEntity) {
                    d2wContextString.append(key + ": " + ((EOEntity) value).name());
                } else if (value instanceof EORelationship) {
                    d2wContextString.append(key + ": " + ((EORelationship) value).name());
                } else {
                    d2wContextString.append(key + ": " + value);
                }
            }
        }
        return d2wContextString.toString();
    }

}