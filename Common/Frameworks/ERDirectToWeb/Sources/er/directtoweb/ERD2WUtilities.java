package er.directtoweb;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.foundation.NSArray;
import org.apache.log4j.Logger;

/**
 * A class to collect commonly performed functions that really don't belong other places.
 * @author Travis Cripps
 */
public class ERD2WUtilities {

    public static final Logger log = Logger.getLogger(ERD2WUtilities.class);

    /**
     * Determines if there's a validation exception for the provided property key, in the provided d2wContext.
     * @param propertyKey to inquire about
     * @param d2wContext to inquire
     * @return true if there is a validation exception for the named property key
     */
    public static boolean validationExceptionOccurredForPropertyKey(String propertyKey, D2WContext d2wContext) {
        boolean contains = keyPathsWithValidationExceptions(d2wContext).containsObject(propertyKey);
        if (log.isDebugEnabled())
            log.debug("propertyKey="+propertyKey+", keyPathsWithValidationExceptions="+keyPathsWithValidationExceptions(d2wContext));
        return contains;
    }

    /**
     * Gets the key paths with validation exceptions in the provided context.
     * @param d2wContext in which to evaluate
     * @return the key paths with validation exceptions
     */
    public static NSArray keyPathsWithValidationExceptions(D2WContext d2wContext) {
        NSArray exceptions = (NSArray)d2wContext.valueForKey("keyPathsWithValidationExceptions");
        return exceptions != null ? exceptions : NSArray.EmptyArray;
    }

}
