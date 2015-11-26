package er.corebusinesslogic;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOGlobalID;

import er.extensions.eof.ERXEC;
import er.extensions.eof.ERXEnterpriseObjectCache;

public class ERCHelpText extends _ERCHelpText {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(ERCHelpText.class);

    public final static String ENTITY = "ERCHelpText";
    public static final ERCHelpTextClazz clazz = (ERCHelpTextClazz)new ERCHelpTextClazz().init(ENTITY); 
    
    public static class ERCHelpTextClazz extends _ERCHelpTextClazz {

        ERXEnterpriseObjectCache cache = new ERXEnterpriseObjectCache(ENTITY, Key.KEY) {
            @Override
            protected void handleUnsuccessfullQueryForKey(Object key) {
                EOEditingContext ec = ERXEC.newEditingContext();
                EOGlobalID gid = NO_GID_MARKER;
                ec.lock();
                try {
                    EOEnterpriseObject eo = (EOEnterpriseObject) EOUtilities.objectsMatchingKeyAndValue(ec, ENTITY, Key.KEY, key).lastObject();
                    if(eo != null) {
                        gid = ec.globalIDForObject(eo);
                    }
                } finally {
                    ec.unlock();
                }
                cache().setObjectForKey(createRecord(gid, null), key);
            }
        };
        
        public ERCHelpText helpTextForKey(EOEditingContext ec, String key) {
            return (ERCHelpText) cache.objectForKey(ec, key);
        }
    }

    public interface Key extends _ERCHelpText.Key {
    }


    /**
     * Intitializes the EO. This is called when an EO is created, not when it is 
     * inserted into an EC.
     */
    @Override
    public void init(EOEditingContext ec) {
        super.init(ec);
    }
    
    // more EO methods here
}
