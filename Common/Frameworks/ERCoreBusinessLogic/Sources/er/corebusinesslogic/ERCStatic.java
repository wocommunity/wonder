// ERCStatic.java
// (c) by Anjo Krank (ak@kcmedia.ag)
package er.corebusinesslogic;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import er.extensions.*;

public class ERCStatic extends _ERCStatic {
    static final ERXLogger log = ERXLogger.getLogger(ERCStatic.class);

    public String toString() {
        return entityName()+": "+key()+"="+value();
    }
    public String description() {
        return toString();
    }
    public String userPresentableDescription() {
        return toString();
    }
    
    
    // Class methods go here
    
    public static class ERCStaticClazz extends _ERCStaticClazz {

        public ERCStatic objectMatchingKey(EOEditingContext ec, String value) {
            NSArray arr = preferencesWithKey(ec, value);
            return (ERCStatic) (arr.count() == 1 ? arr.objectAtIndex(0) : null);
        }


        // the STATIC table acts as a dictionary
        private final static EOEditingContext _ec=ERXExtensions.newEditingContext();
        public static String staticStoredValueForKey(String key) {
            ERCStatic entry = ERCStatic.staticClazz().objectMatchingKey(_ec,key);
            return entry!=null ? entry.value() : null;
        }
        public static int staticStoredIntValueForKey(String key) {
            int result=-1;
            String s= staticStoredValueForKey(key);
            if (s!=null) {
                try {
                    result=Integer.parseInt(s);
                } catch (NumberFormatException e) {}
            }
            return result;
        }

        public static void takeStaticStoredValueForKey(String value,
                                                       String key,
                                                       EOEditingContext editingContext) {
            ERCStatic entry = ERCStatic.staticClazz().objectMatchingKey(editingContext,key);
            if (entry==null) {
                entry=(ERCStatic)ERXUtilities.createEO("ERCStatic", editingContext);
                entry.setKey(key);
            }
            entry.setValue(value);
        }
            

        
    }

    public static ERCStaticClazz staticClazz() {
        return (ERCStaticClazz)EOGenericRecordClazz.clazzForEntityNamed("ERCStatic");
    }

}
