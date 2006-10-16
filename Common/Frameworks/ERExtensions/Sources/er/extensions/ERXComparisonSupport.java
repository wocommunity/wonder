package er.extensions;

import java.text.*;
import java.util.*;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * Comparison support that sorts strings in a locale-savvy manner.
 * 
 * @created ak on Fri Apr 11 2003
 */

public class ERXComparisonSupport extends EOSortOrdering.ComparisonSupport {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERXComparisonSupport.class);
    
    private static Class stringClass = String.class;

    private static final int MAGIC = -42;

    public static void initialize() {
        EOSortOrdering.ComparisonSupport.setSupportForClass(new ERXComparisonSupport(), String.class);
    }
    
    private Collator collator() {
        Locale locale = ERXLocalizer.currentLocalizer().locale();
        if(locale != null) {
            locale = Locale.getDefault();
        }
        return Collator.getInstance(locale);
    }
    
    private static int _handleNulls(Object object1, Object object2) {
        if (object1 == null || object1 == NSKeyValueCoding.NullValue) {
            if (object2 == null || object2 == NSKeyValueCoding.NullValue)
                return 0;
            return -1;
        }
        if (object2 == null || object2 == NSKeyValueCoding.NullValue)
            return 1;
        return MAGIC;
    }

    protected int _genericCompareTo(Object object1, Object object2) {
        int i = _handleNulls(object1, object2);
        if (i != MAGIC)
            return i;
        Class clazz = object1.getClass();
        if (clazz == stringClass) {
            return collator().compare(object1.toString(),object2.toString());
        }
        return super._genericCompareTo(object1, object2);
    }

    protected int _genericCaseInsensitiveCompareTo(Object object1, Object object2) {
        int i = _handleNulls(object1, object2);
        if (i != MAGIC)
            return i;
        Class clazz = object1.getClass();
        if (clazz == stringClass)  {
            return collator().compare(object1.toString().toUpperCase(),object2.toString().toUpperCase());
        }
        return super._genericCaseInsensitiveCompareTo(object1, object2);
    }
}
