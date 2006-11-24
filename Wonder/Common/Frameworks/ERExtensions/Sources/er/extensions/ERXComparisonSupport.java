package er.extensions;

import java.text.Collator;
import java.util.Locale;

import org.apache.log4j.Logger;

import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSSelector;

/**
 * Comparison support that sorts strings in a locale-savvy manner and adds
 * support for regex in-memory matching when using qualifier strings.
 * 
 * @created ak on Fri Apr 11 2003
 */

public class ERXComparisonSupport {

    /** logging support */
    private static final Logger log = Logger.getLogger(ERXComparisonSupport.class);
    
    private static Class stringClass = String.class;

    private static final int MAGIC = -42;

    private static boolean fixAnyway;
    
    public static void initialize() {
        EOSortOrdering.ComparisonSupport.setSupportForClass(new StringSortSupport(), String.class);
        EOQualifier.ComparisonSupport.setSupportForClass(new StringQualifierSupport(), String.class);
        fixAnyway = ERXProperties.booleanForKeyWithDefault("er.extensions.ERXComparisonSupport.fixAnyway", true);
    }

    /**
     * Support class that adds regex matching, causing it to use the correct selector <code>String.matches(String)</code>
     * instead of <code>String.matches(Object)</code>.
     * We would not need this if there was a way to set the selector by name in EOQualifier... 
     */
    protected static class StringQualifierSupport extends EOQualifier.ComparisonSupport {

        public boolean _compareWithArbitrarySelector(Object aLeft, Object aRight, NSSelector aSelector) {
            if(aSelector.name().equals(ERXRegExQualifier.MatchesSelectorName)) {
                aSelector = ERXRegExQualifier.MatchesSelector;
            }
            return super._compareWithArbitrarySelector(aLeft, aRight, aSelector);
        }
        
    }
    
    protected static class StringSortSupport extends EOSortOrdering.ComparisonSupport {

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
        	// AK: unfortunately, the is no combination that allows us to keep
        	// sorting by case, but disregard the Umlaut characters. So
        	// we just use the default, unless we come up with a better idea...
        	if(fixAnyway) {
        		int i = _handleNulls(object1, object2);
        		if (i != MAGIC)
        			return i;
        		Class clazz = object1.getClass();
        		if (clazz == stringClass) {
        			return collator().compare(object1.toString(),object2.toString());
        		}
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
}
