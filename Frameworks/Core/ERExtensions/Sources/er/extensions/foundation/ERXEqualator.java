package er.extensions.foundation;

import java.util.Objects;

import com.webobjects.eocontrol.EOEnterpriseObject;

import er.extensions.eof.ERXEOControlUtilities;

/**
 * This class is an abstract class that defines an API for determining if two objects are equal.
 * This is useful if you'd like to have a custom definition of what it means for two objects to
 * be equal in one context (within an array, for example) while not changing the meaning of
 * equality in another (by overriding equals()).  This is especially useful for doing
 * non-editing-context-sensitive equality checks between EOs.
 * 
 * @author Jonathan B. Leffert
 */
public abstract class ERXEqualator {

    //
    // some useful equalators
    //
    
    /**
     * Provides a safe equality check that won't throw if one or both of the objects is null.
     * 
     * @see Objects#equals(Object, Object)
     */
    public static final ERXEqualator SafeEqualsEqualator = new _SafeEqualsEqualator();
    
    /**
     * Provides EO equality checks regardless of the editing context the objects are registered in.
     * 
     * @see er.extensions.eof.ERXEOControlUtilities#eoEquals(EOEnterpriseObject, EOEnterpriseObject)
     */
    public static final ERXEqualator EOEqualsEqualator = new _EOEqualsEqualator();
    
    //
    // methods
    //
    
    /**
     * Performs the equality check between o1 and o2.  What the equality check means between the
     * objects and when either object is null or when objects are of different classes is
     * the subclass's responsibility.
     * 
     * @param o1 first object to compare.
     * @param o2 second object to compare.
     * @return true if o1 and o2 are to be considered equal, false otherwise.
     */ 
    public abstract boolean objectIsEqualToObject(Object o1, Object o2);
    
    //
    // private classes
    //
    
    private static class _SafeEqualsEqualator extends ERXEqualator {
        public _SafeEqualsEqualator() {}

        @Override
        public boolean objectIsEqualToObject(Object o1, Object o2) {
            return Objects.equals(o1, o2);
        }
    }
    
    private static class _EOEqualsEqualator extends ERXEqualator {
        public _EOEqualsEqualator() {}

        @Override
        public boolean objectIsEqualToObject(Object o1, Object o2) {
            if ( (o1 != null && ! (o1 instanceof EOEnterpriseObject)) || (o2 != null && ! (o2 instanceof EOEnterpriseObject)) ) {
                throw new RuntimeException("Unable to compare objects because both objects need to be EOEnterpriseObjects.  " +
                                           "o1: " + o1 + " (class: " + o1.getClass() + "), o2: " + o2 + " (class: " + o2.getClass() + ").");
            }
            final EOEnterpriseObject eo1 = (EOEnterpriseObject)o1;
            final EOEnterpriseObject eo2 = (EOEnterpriseObject)o2;
            
            return ERXEOControlUtilities.eoEquals(eo1, eo2);
        }
    }
}
