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
            if (arr.count()>1) throw new IllegalStateException("Found "+arr.count()+" rows for key "+value);
            return (ERCStatic) (arr.count() == 1 ? arr.objectAtIndex(0) : null);
        }


        // the STATIC table acts as a dictionary
        private final static EOEditingContext _ec=ERXExtensions.newEditingContext();

        public static String staticStoredValueForKey(EOEditingContext ec, String key) {
            ERCStatic entry = ERCStatic.staticClazz().objectMatchingKey(ec,key);
            return entry!=null ? entry.value() : null;
        }
        public static int staticStoredIntValueForKey(EOEditingContext ec, String key) {
            int result=-1;
            String s= staticStoredValueForKey(ec, key);
            if (s!=null) {
                try {
                    result=Integer.parseInt(s);
                } catch (NumberFormatException e) {}
            }
            return result;
        }

        public static String staticStoredValueForKey(String key) {
            return staticStoredValueForKey(_ec, key);
        }
        public static int staticStoredIntValueForKey(String key) {
            return staticStoredIntValueForKey(_ec, key);
        }

        public static void takeStaticStoredValueForKey(String value,
                                                       String key) {
            takeStaticStoredValueForKey(_ec, value, key);
        }

        public static void takeStaticStoredValueForKey(EOEditingContext editingContext,
                                                       String value,
                                                       String key) {
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
