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


        private NSMutableDictionary _staticsPerKey=new NSMutableDictionary();

        
        public ERCStatic objectMatchingKey(EOEditingContext ec, String key) {
            Object result=_staticsPerKey.objectForKey(key);
            if (result==null) {
                NSArray arr = preferencesWithKey(ec, key);
                if (arr.count()>1) throw new IllegalStateException("Found "+arr.count()+" rows for key "+key);
                result=arr.count() == 1 ? arr.objectAtIndex(0) : NSKeyValueCoding.NullValue;
                _staticsPerKey.setObjectForKey(result,key);
                result= result == NSKeyValueCoding.NullValue ? null : result;
            }
            result= result!=null ? EOUtilities.localInstanceOfObject(ec, (ERCStatic)result) : null;
            return (ERCStatic)result;
        }

        public void invalidateCache() { _staticsPerKey.removeAllObjects(); }


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
