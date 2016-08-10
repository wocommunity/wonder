//
//  ERDObjectCreationDelegate.java
//  ERDirectToWeb
//
//  Created by Max Muller on Wed Nov 20 2002.
//
package er.directtoweb.assignments.delayed;

import java.lang.reflect.Constructor;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;

/**
 * Assignment used to create objects on the fly. This assignment
 * can be used in two different manner. The first is by just
 * specifing the class name as a string, ie "foo.bar.MyClass". This
 * will create an instance of the MyClass object. The second form
 * allows one to specify the object to be created in a dictionary format:<pre><code>
 * {
 * 	className = "foo.bar.MyClass";
 *	arguments = ( {
 *			className = "com.webobjects.appserver.WOSession";
 *			contextKey = "session";
 *    		}, {
 *			className = "java.lang.String";
 *			contextKey = "propertyKey";
 * 		});
 * }</code></pre>
 *
 * This will create an object of type MyClass using the constructor:
 * MyClass(WOSession session, String key), using the arguments found
 * by resolving the contextKey off of the current {@link D2WContext context}.
 */
public class ERDDelayedObjectCreationAssignment extends ERDDelayedAssignment {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    //	===========================================================================
    //	Class variable(s)
    //	---------------------------------------------------------------------------
    
    /** logging support */
    public static final Logger log = Logger.getLogger(ERDDelayedObjectCreationAssignment.class);

    //	===========================================================================
    //	Class method(s)
    //	---------------------------------------------------------------------------    

    /**
    * Static constructor required by the EOKeyValueUnarchiver
     * interface. If this isn't implemented then the default
     * behavior is to construct the first super class that does
     * implement this method. Very lame.
     * @param eokeyvalueunarchiver to be unarchived
     * @return decoded assignment of this class
     */    
    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedObjectCreationAssignment(eokeyvalueunarchiver);
    }

    //	===========================================================================
    //	Constructor(s)
    //	---------------------------------------------------------------------------    

    /**
     * Public constructor
     * @param u key-value unarchiver used when unarchiving
     *		from rule files.
     */    
    public ERDDelayedObjectCreationAssignment(EOKeyValueUnarchiver u) { super(u); }

    /**
     * Public constructor
     * @param key context key
     * @param value of the assignment
     */    
    public ERDDelayedObjectCreationAssignment(String key, Object value) { super(key,value); }

    //	===========================================================================
    //	Instance method(s)
    //	---------------------------------------------------------------------------    

    /**
     * Delayed firing of assignment. Creates an object
     * for the specified class. See description of the
     * class for the correct format.
     * @param context current context
     * @return newly created object
     */
    @Override
    public Object fireNow(D2WContext context) {
        Object createdObject = null;
        try {
            if (log.isDebugEnabled())
                log.debug("Creating object for value: " + value());
            if (value() instanceof String) {
                Class c = Class.forName((String)value());
                createdObject = c.newInstance();
            } else if (value() instanceof NSDictionary) {
                String mainClassName = (String)((NSDictionary)value()).objectForKey("className");
                Class mainClass = Class.forName(mainClassName);
                NSArray arguments = (NSArray)((NSDictionary)value()).objectForKey("arguments");
                if (arguments != null && arguments.count() > 0) {
                    Class argumentClasses[] = new Class[arguments.count()];
                    Object argumentValues[] = new Object[arguments.count()];
                    int count = 0;
                    while (count < arguments.count()) {
                        NSDictionary anArgument = (NSDictionary)arguments.objectAtIndex(count);
                        String argumentClassName = (String)anArgument.objectForKey("className");
                        String argumentContextKey = (String)anArgument.objectForKey("contextKey");
                        argumentValues[count] = context.valueForKeyPath(argumentContextKey);
                        argumentClasses[count] = Class.forName(argumentClassName);
                        count++;
                    }
                    Constructor constructor = mainClass.getConstructor(argumentClasses);
                    if (constructor != null) {
                        createdObject = constructor.newInstance(argumentValues);
                    } else {
                        log.warn("Unable to find constructor on class: " + mainClass.getName()
                                 + " for argument classes: " +   argumentClasses);
                    }
                } else {
                    createdObject = mainClass.newInstance();           
                }
            } else {
                log.warn("Unsupported value: " + value());
            }
        } catch (Exception e) {
            log.warn("Exception happened when attempting to create object for value: " + value() + " exception: " + e);
        }
        return createdObject;
    }
}
