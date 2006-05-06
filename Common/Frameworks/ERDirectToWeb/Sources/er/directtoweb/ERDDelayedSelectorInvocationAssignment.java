//
// jbl: 13 april 2006
//

package er.directtoweb;

import er.extensions.ERXSelectorUtilities;
import er.extensions.ERXValueUtilities;
import er.extensions.ERXLogger;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation.NSArray;

/**
 * Similar in nature to a key-value assignment, but allows you to construct arbitrary method invocations to
 * resolve rules.  As a somewhat contrived example, assume we're inferring on the componentName rule:
 * <P>
 * <code>entity.name = 'Person' and propertyKey = 'username' -> componentName = 'componentForKey'</code> (selector invocation assigment)
 * <BR>
 * <code>entity.name = 'Person' and propertyKey = 'username' -> target = 'object'</code> (delayed key-value association>
 * <BR>
 * <code>entity.name = 'Person' and propertyKey = 'username' -> numberOfSelectorArguments = '1'</code> (assignment)
 * <BR>
 * <code>entity.name = 'Person' and propertyKey = 'username' -> selectorArgument0 = 'propertyKey'</code> (delayed key-value association)
 *
 * <P>
 *
 * The default setup provides for a selector with a single argument which is the propertyKey invoked on object.
 *
 * <P>
 *
 * Resolving the rule for componentName, would end up invoking the <code>componentForKey(Object)</code> method on the current object
 * from the rule context, passing the current propertyKey through for the argument.  This would boil down to
 * <code>object.componentForKey("username")</code>.
 *
 * <P>
 *
 * Assumptions:
 * <ul>
 * <li>The arguments to the invoked method must all be Objects.  This isn't strictly speaking necessary, but the way the
 *     assignment is currently coded, it's required.</li>
 * <li>The maximum number of arguments to a selector is defined at compile time and is currently set to 5.  The compile-time
 *     definition is needed in order to get the dependent keys generated properly.</li>
 * </ul>
 *
 */
public class ERDDelayedSelectorInvocationAssignment extends ERDDelayedAssignment implements ERDComputingAssignmentInterface {

    private static final ERXLogger _log = ERXLogger.getERXLogger(ERDDelayedSelectorInvocationAssignment.class);

    private static final String NUMBER_OF_SELECTOR_ARGUMENTS_KEY = "numberOfSelectorArguments";
    private static final String SELECTOR_ARGUMENT_PREFIX_KEY = "selectorArgument";
    private static final String SELECTOR_TARGET_KEY = "selectorTarget";

    private static final int _maximumNumberOfSelectorArguments = 5;  // totally arbitrary

    private static NSArray _argumentKeys;
    private static NSArray _dependentKeys;
    private static Class[][] _argumentArrays = new Class[_maximumNumberOfSelectorArguments + 1][];

    static {
        NSMutableArray a = new NSMutableArray();

        for ( int i = 0; i < _maximumNumberOfSelectorArguments; i++ )
            a.addObject(SELECTOR_ARGUMENT_PREFIX_KEY + i);

        _argumentKeys = a.immutableClone();
        _dependentKeys = (new NSArray(new Object[] { SELECTOR_TARGET_KEY, NUMBER_OF_SELECTOR_ARGUMENTS_KEY })).arrayByAddingObjectsFromArray(_argumentKeys);

        for ( int i = 0; i < _argumentArrays.length; i++ ) {
            Class[] types = null;

            if ( i > 0 ) {
                types = new Class[i];

                for ( int j = 0; j < types.length; j++ )
                    types[j] = Object.class;
            }

            _argumentArrays[i] = types;
        }
    }

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
        return _dependentKeys;
    }

    private int _numberOfSelectorArgumentsInContext(D2WContext c) {
        return ERXValueUtilities.intValueWithDefault(c.valueForKey(NUMBER_OF_SELECTOR_ARGUMENTS_KEY), 1);
    }

    private Object _selectorTargetInContext(D2WContext c) {
        Object target = c.valueForKey(SELECTOR_TARGET_KEY);

        return target != null ? target : c.valueForKey("object");
    }

    private Object _defaultSelectorArgumentZeroInContext(D2WContext c) {
        return c.valueForKey(D2WModel.PropertyKeyKey);
    }

    public Object fireNow(D2WContext c) {
        final String selectorName = (String)value();
        final int numberOfSelectorArguments = _numberOfSelectorArgumentsInContext(c);
        final Object target = _selectorTargetInContext(c);
        NSSelector selector = null;
        NSMutableArray arguments = null;

        if ( selectorName == null )
            throw new RuntimeException("selectorName is null");
        if ( target == null )
            throw new RuntimeException(SELECTOR_TARGET_KEY + " is null");
        if ( numberOfSelectorArguments > _maximumNumberOfSelectorArguments )
            throw new RuntimeException(NUMBER_OF_SELECTOR_ARGUMENTS_KEY + " " + numberOfSelectorArguments + " > _maximumNumberOfSelectorArguments " + _maximumNumberOfSelectorArguments);

        for ( int i = 0; i < numberOfSelectorArguments; i++ ) {
            final String ruleKey = (String)_argumentKeys.objectAtIndex(i);
            Object ruleValue = c.valueForKey(ruleKey);

            if ( i == 0 && ruleValue == null )
                ruleValue = _defaultSelectorArgumentZeroInContext(c);

            if ( ruleValue != null )
                (arguments != null ? arguments : (arguments = new NSMutableArray())).addObject(ruleValue);
            else
                break;
        }

        if ( _log.isDebugEnabled() )
            _log.debug("Going to fire " + selectorName + " on object " + target + " with " + numberOfSelectorArguments + " arguments: " + arguments);

        selector = new NSSelector(selectorName, _argumentArrays[numberOfSelectorArguments]);

        return ERXSelectorUtilities.invoke(selector, target, arguments != null ? arguments.objects() : null);
    }
}
