package er.extensions.foundation;

import java.util.concurrent.CopyOnWriteArrayList;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation._NSDelegate;


/**
 * <p>By design, WebObjects' classes that accept a delegate only accept a single object.  ERXMulticastingDelegate allows multiple
 * delegate objects to be aggregated and presented as a single delegate object.</p>
 *
 * <p>Delegates are called in the order they are added.  Methods are called until one of the delegates handles the message.
 * Delegate methods that have a Object or boolean return type are called until one returns a non-null response that is not the default value.
 * Delegate methods that have a void return type are always called.</p>
 *
 * <p>Here is an example if you have multiple delegates that you want to set up.</p>
 * <pre>
 * ERXDatabaseContextMulticastingDelegate multiDelegate = new ERXDatabaseContextMulticastingDelegate();
 * multiDeletegate.addDelegate(new ERXDatabaseContextDelegate());
 * multiDeletegate.addDelegate(new ERXEntityDependencyOrderingDelegate());
 * EODatabaseContext.setDefaultDelegate(multiDelegate);
 * </pre>
 *
 * <p>Here is a usage example to handle the case where a deletegate may already be set</p>
 * <pre>
 * Object newDelegate = new ERXEntityDependencyOrderingDelegate();
 * ERXDatabaseContextMulticastingDelegate multiDelegate;
 * if (EODatabaseContext.defaultDelegate() == null) {
 *     multiDelegate = new ERXDatabaseContextMulticastingDelegate();
 * }
 * else {
 *     if (EODatabaseContext.defaultDelegate() instanceof ERXDatabaseContextMulticastingDelegate) {
 *            multiDelegate = (ERXDatabaseContextMulticastingDelegate)EODatabaseContext.defaultDelegate();
 *     }
 *     else {
 *         multiDelegate = new ERXDatabaseContextMulticastingDelegate();
 *         multiDelegate.addDelegate(EODatabaseContext.defaultDelegate());
 *     }
 * }
 * multiDelegate.addDelegate(newDelegate);
 * EODatabaseContext.setDefaultDelegate(multiDelegate);
 * </pre>
 *
 * <p>This class needs to be implemented for each delegate interface.  All methods on the interface should be implemented
 * and should call one of the perform... or booleanPerform... methods on this class.  See ERXDatabaseContextMulticastingDelegate
 * for example usage. One result of this implementation is that delegates can be added and removed at any time.</p>
 *
 * @author chill
 */
public abstract class ERXMulticastingDelegate {

    private CopyOnWriteArrayList delegates = new CopyOnWriteArrayList();


    /**
     * Adds <code>delegate</code> at the end of the chain.  It becomes the last delegate called.
     *
     * @param delegate Object to add as one of the delegates called
     */
    public void addDelegate(Object delegate) {
    	if (hasDelegate(delegate)) {
    		throw new IllegalArgumentException("Delegate is already included");
    	}

    	_NSDelegate delegateObject = new _NSDelegate(getClass(), delegate);
    	delegates.add(delegateObject);
    }
    

    /**
     * Adds <code>delegate</code> at the start of the chain.  It becomes the first delegate called.
     *
     * @param delegate Object to add as one of the delegates called
     */
    public void addDelegateAtStart(Object delegate) {
    	if (hasDelegate(delegate)) {
    		throw new IllegalArgumentException("Delegate is already included");
    	}

    	_NSDelegate delegateObject = new _NSDelegate(getClass(), delegate);
    	delegates.add(0, delegateObject);
    }
    
    
    /**
     * Removes <code>delegate</code> from the delegates called.
     *
     * @param delegate Object to remove as one of the delegates called
     */
    public void removeDelegate(Object delegate) {
    	if ( ! hasDelegate(delegate)) {
    		throw new IllegalArgumentException("Delegate is not present");
    	}

    	for (int i = 0; i < delegates.size(); i++) {
    		_NSDelegate delegateObject = (_NSDelegate)delegates.get(i);
    		if (delegateObject.delegate().equals(delegate))
    		{
    			delegates.remove(i);
    			break;
    		}
    	}
    }
    

    /**
     * Returns <code>true</code> if delegate is represented in delegates().
     *
     * @param delegate Object to test for membership in delegates()
     * @return <code>true</code> if delegate is represented in delegates()
     */
    public boolean hasDelegate(Object delegate) {
    	for (int i = 0; i < delegates.size(); i++) {
    		_NSDelegate delegateObject = (_NSDelegate)delegates.get(i);
    		if (delegateObject.delegate().equals(delegate))
    		{
    			return true;
    		}
    	}
    	
    	return false;
    }
    
    
    /**
     * This method returns an array of <code>com.webobjects.foundation._NSDelegate</code>, <b>not</b>
     * the delegate objects originally added by calling addDelegate...  Call <code>delegate()</code>
     * on the elements in this list to examine the delegate objects originally added by calling addDelegate...
     *
     * @return the delegates in the order they will be called
     */
    public NSArray delegates() {
    	return new NSArray(delegates);
    }
    

    /**
     * Use this to set the delegate order if the addDelegate... methods are not sufficient.
     * <code>orderedDelegates</code> should be a re-arrangement of the list returned by <code>delegates()</code>.
     *
     * @param orderedDelegates array of <code>com.webobjects.foundation._NSDelegate</code> in the order in which delegates should be called
     */
    public void setDelegateOrder(NSArray orderedDelegates) {
    	for (int i = 0; i < orderedDelegates.count(); i++) {
    		if ( ! (orderedDelegates.objectAtIndex(i) instanceof _NSDelegate)) {
    			throw new IllegalArgumentException("Object of class " + orderedDelegates.objectAtIndex(i).getClass().getName() +
    					" must be instanceof _NSDelegate");
    		}
    	}
    	delegates.clear();
    	delegates.addAll(orderedDelegates);

    }


    /**
     * This is the central method for dispatching messages to the delegates aggregated by this object.
     * The other perform... and booleanPerform... methods simply call this method.
     *
     * @param methodName the name of the delegate method to call
     * @param args 0 or more arguments to pass to the delegate method
     * @param defaultResult the value to return if none of the delegates implement this method
     * @return value returned by the last delegate called
     */
    protected Object perform(String methodName, Object args[], Object defaultResult) {
        Object result = null;
            for (int i = 0; (result == null || result.equals(defaultResult)) && i < delegates.size(); i++) {
                _NSDelegate delegate = (_NSDelegate) delegates.get(i);
                if (delegate.respondsTo(methodName)) {
                    result = delegate.perform(methodName, args);
                }
            }

        return result == null ? defaultResult : result;
    }


    protected Object perform(String methodName, Object defaultResult)
    {
        return perform(methodName, new Object[0], defaultResult);
    }


    protected Object perform(String methodName, Object arg, Object defaultResult)
    {
        return perform(methodName, new Object[] {arg}, defaultResult);
    }


    protected Object perform(String methodName, Object arg1, Object arg2, Object defaultResult)
    {
        return perform(methodName, new Object[] {arg1, arg2}, defaultResult);
    }


    protected Object perform(String methodName, Object arg1, Object arg2, Object arg3, Object defaultResult)
    {
        return perform(methodName, new Object[] {arg1, arg2, arg3}, defaultResult);
    }



    protected Object perform(String methodName, Object arg1, Object arg2, Object arg3, Object arg4, Object arg5, Object defaultResult)
    {
        return perform(methodName, new Object[] {arg1, arg2, arg3, arg4, arg5}, defaultResult);
    }
    

    protected boolean booleanPerform(String methodName, boolean defaultResult)
    {
        return booleanPerform(methodName, new Object[0], defaultResult);
    }


    protected boolean booleanPerform(String methodName, Object arg, boolean defaultResult)
    {
        return booleanPerform(methodName, new Object[] {arg}, defaultResult);
    }


    protected boolean booleanPerform(String methodName, Object arg1, Object arg2, boolean defaultResult)
    {

        return booleanPerform(methodName, new Object[] {arg1, arg2}, defaultResult);
    }


    protected boolean booleanPerform(String methodName, Object arg1, Object arg2, Object arg3, boolean defaultResult)
    {
        return booleanPerform(methodName, new Object[] {arg1, arg2, arg3}, defaultResult);
    }


    protected boolean booleanPerform(String methodName, Object args[], boolean defaultResult)
    {
        Boolean result = (Boolean) perform(methodName, args, Boolean.valueOf(defaultResult));
        return result != null ? result.booleanValue() : false;
    }

}
