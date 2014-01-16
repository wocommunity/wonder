//
// jbl: 13 april 2006
//

package er.directtoweb.assignments.delayed;

import org.apache.log4j.Logger;

import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;

import er.directtoweb.assignments.ERDComputingAssignmentInterface;
import er.extensions.foundation.ERXSelectorUtilities;

/**
 * Similar in nature to a key-value assignment, but allows you to construct arbitrary method invocations to
 * resolve rules.  As a somewhat contrived example, assume we're inferring on the componentName rule:
 * <P>
 * <code>entity.name = 'Person' and propertyKey = 'username' -> componentName = (object, componentForKey, propertyKey)</code>
 *
 * <P>
 *
 * Resolving the rule for componentName, would end up invoking the <code>componentForKey(Object)</code> method on the current object
 * from the rule context, passing the current propertyKey through for the argument.  This would boil down to
 * <code>object.componentForKey("username")</code>.
 *
 * <P>
 *
 * The array in the value for this assignment must have two or more objects.  The first object is a key path evaluated
 * on the rule context to find the target of the selector.  The second object is the selector name, it is a constant
 * and is not evaluated on the rule context.  All subsequent objects in the array are treated as key paths to resolve
 * on the rule context to get the arguments for the selector.
 *
 * <P>
 *
 * Assumptions:
 * <ul>
 * <li>The arguments to the invoked method must all be Objects.  This isn't strictly speaking necessary, but the way the
 *     assignment is currently coded, it's required.</li>
 * </ul>
 */
public class ERDDelayedSelectorInvocationAssignment extends ERDDelayedAssignment implements ERDComputingAssignmentInterface {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static Object decodeWithKeyValueUnarchiver(EOKeyValueUnarchiver eokeyvalueunarchiver)  {
        return new ERDDelayedSelectorInvocationAssignment(eokeyvalueunarchiver);
    }

    public ERDDelayedSelectorInvocationAssignment(EOKeyValueUnarchiver u) {
        super(u);
    }

    public ERDDelayedSelectorInvocationAssignment(String key, Object value) {
        super(key,value);
    }

    public NSArray dependentKeys(String keyPath) {
        return DefaultImplementation.dependentKeys(this, keyPath);
    }

    @Override
    public Object fireNow(D2WContext c) {
        return DefaultImplementation.fire(this, c);
    }

    public static class DefaultImplementation {

        private static final Logger _log = Logger.getLogger(ERDDelayedSelectorInvocationAssignment.class);

        // we cache 0 - 5 arguments
        private static Class[][] _parameterTypesArrays = new Class[5 + 1][];

        static {
            for ( int i = 0; i < _parameterTypesArrays.length; i++ ) {
                Class[] types = null;

                if ( i > 0 ) {
                    types = new Class[i];

                    for ( int j = 0; j < types.length; j++ )
                        types[j] = Object.class;
                }

                _parameterTypesArrays[i] = types;
            }
        }

        private static Class[] _parameterTypesForNumberOfArguments(int numberOfArguments) {
            final Class[] result;

            if ( numberOfArguments < _parameterTypesArrays.length ) {
                result = _parameterTypesArrays[numberOfArguments];
            }
            else {
                result = new Class[numberOfArguments];

                for ( int i = 0; i < numberOfArguments; i++ )
                    result[i] = Object.class;
            }

            return result;
        }

        public static NSArray dependentKeys(Assignment assignment, String keyPath) {
            final NSArray value = (NSArray)assignment.value();
            NSArray result = value;

            if ( result != null && result.count() > 1 ) {
                NSMutableArray a = value.mutableClone();

                a.removeObjectAtIndex(1);  // selector name is constant
                result = a;
            }

            return result != null ? result : NSArray.EmptyArray;
        }

        public static Object fire(Assignment assignment, D2WContext c) {
            final NSArray value = (NSArray)assignment.value();
            final int valueCount = value.count();
            final Object target;
            Object result = null;

            if ( valueCount < 2 )
                throw new RuntimeException("Must have at least 2 components in value: " + value);

            target = c.valueForKeyPath((String)value.objectAtIndex(0));
            if ( target != null ) {
                final int numberOfArguments = valueCount - 2;
                final String selectorName = (String)value.objectAtIndex(1);
                final NSSelector selector;
                Object[] arguments = null;

                if ( numberOfArguments > 0 ) {
                    arguments = new Object[numberOfArguments];

                    for ( int i = 2; i < valueCount; i++ )
                        arguments[i-2] = c.valueForKeyPath((String)value.objectAtIndex(i));
                }

                if ( _log.isDebugEnabled() ) {
                    final StringBuilder sb = new StringBuilder('(');

                    if ( arguments != null ) {
                        for ( int i = 0; i < arguments.length; i++ ) {
                            if ( i > 0)
                                sb.append(", ");
                            sb.append(arguments[i]);
                        }
                    }
                    sb.append(')');

                    _log.debug("Going to fire " + selectorName + " on object " + target + " with " + numberOfArguments + " arguments: " + sb);
                }

                selector = new NSSelector(selectorName, _parameterTypesForNumberOfArguments(numberOfArguments));

                result = ERXSelectorUtilities.invoke(selector, target, arguments);
            }

            return result;
        }
    }

}
