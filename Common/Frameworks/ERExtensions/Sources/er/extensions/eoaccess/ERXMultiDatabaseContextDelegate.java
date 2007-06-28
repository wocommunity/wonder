package er.extensions.eoaccess;

import java.lang.reflect.Method;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;

import er.extensions.foundation.ERXDelegateMulticast;


/**
 * Subclass of <code>er.extensions.foundation.ERXDelegateMulticast</code> that implements
 * <code>com.webobjects.eoaccess.EODatabaseContext.Delegate</code>. Use this to aggregate multiple delegate objects
 * for <code>EODatabaseContext.Delegate</code>
 *
 * @see er.extensions.foundation.ERXDelegateMulticast
 * @see com.webobjects.eoaccess.EODatabaseContext.Delegate
 * @author chill
 */
public class ERXMultiDatabaseContextDelegate extends ERXDelegateMulticast implements EODatabaseContext.Delegate {

    private Method orderAdaptorOperations;



    public ERXMultiDatabaseContextDelegate() {
        super();
        try {
            orderAdaptorOperations = EODatabaseContext.class.getDeclaredMethod("orderAdaptorOperations", new Class[] {});
            orderAdaptorOperations.setAccessible(true);
        }
        catch (Exception e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }


    /**
     * <p>Convenience method to add <code>newDelegate</code> as the last delegate called for
     * <code>EODatabaseContext.defaultDelegate()</code>.  There are three cases to handle:</p>
     * <ol>
     * <li>If there is no default delegate defined, an <code>ERXMultiDatabaseContextDelegate</code>
     * is created as the default delegate, and <code>newDelegate</code> added.</li>
     *
     * <li>If there is a default delegate defined, and it is a <code>ERXMultiDatabaseContextDelegate</code>,
     * <code>newDelegate</code> is added at the end of the delegate chain.</li>
     *
     * <li>If there is a default delegate defined, and it is not a <code>ERXMultiDatabaseContextDelegate</code>,
     * an <code>ERXMultiDatabaseContextDelegate</code> is created as the default delegate, the existing delegate is
     * added, then <code>newDelegate</code> is added at the end of the delegate chain.</li>
     * </ol>
     *
     * @param newDelegate
     */
    public static void addDefaultDelegate(Object newDelegate) {
        ERXMultiDatabaseContextDelegate multiDelegate;
         if (EODatabaseContext.defaultDelegate() == null) {
             multiDelegate = new ERXMultiDatabaseContextDelegate();
         }
         else {
             if (EODatabaseContext.defaultDelegate() instanceof ERXMultiDatabaseContextDelegate) {
                    multiDelegate = (ERXMultiDatabaseContextDelegate)EODatabaseContext.defaultDelegate();
             }
             else {
                 multiDelegate = new ERXMultiDatabaseContextDelegate();
                 multiDelegate.addDelegate(EODatabaseContext.defaultDelegate());
             }
         }
         multiDelegate.addDelegate(newDelegate);
         EODatabaseContext.setDefaultDelegate(multiDelegate);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextDidFetchObjects(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.foundation.NSArray, com.webobjects.eocontrol.EOFetchSpecification, com.webobjects.eocontrol.EOEditingContext)
     */
    public void databaseContextDidFetchObjects(EODatabaseContext dbCtxt, NSArray array, EOFetchSpecification fetchSpec, EOEditingContext ec) {
        perform("databaseContextDidFetchObjects", new Object[] {dbCtxt, array, fetchSpec, ec}, null);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextDidSelectObjects(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.eocontrol.EOFetchSpecification, com.webobjects.eoaccess.EODatabaseChannel)
     */
    public void databaseContextDidSelectObjects(EODatabaseContext dbCtxt, EOFetchSpecification fetchSpec, EODatabaseChannel dbChannel) {
        perform("databaseContextDidSelectObjects", new Object[] {dbCtxt, fetchSpec, fetchSpec, dbChannel}, null);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextFailedToFetchObject(com.webobjects.eoaccess.EODatabaseContext, java.lang.Object, com.webobjects.eocontrol.EOGlobalID)
     */
    public boolean databaseContextFailedToFetchObject(EODatabaseContext dbCtxt, Object object, EOGlobalID gid) {
        return booleanPerform("databaseContextFailedToFetchObject", new Object[] {dbCtxt, object, gid}, false);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextNewPrimaryKey(com.webobjects.eoaccess.EODatabaseContext, java.lang.Object, com.webobjects.eoaccess.EOEntity)
     */
    public NSDictionary databaseContextNewPrimaryKey(EODatabaseContext dbCtxt, Object object, EOEntity entity) {
        return (NSDictionary)perform("databaseContextNewPrimaryKey", new Object[] {dbCtxt, object, entity}, null);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldFetchArrayFault(com.webobjects.eoaccess.EODatabaseContext, java.lang.Object)
     */
    public boolean databaseContextShouldFetchArrayFault(EODatabaseContext dbCtxt, Object object) {
        return booleanPerform("databaseContextShouldFetchArrayFault", new Object[] {dbCtxt, object}, true);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldFetchObjectFault(com.webobjects.eoaccess.EODatabaseContext, java.lang.Object)
     */
    public boolean databaseContextShouldFetchObjectFault(EODatabaseContext dbCtxt, Object object) {
        return booleanPerform("databaseContextShouldFetchObjectFault", new Object[] {dbCtxt, object}, true);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldFetchObjects(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.eocontrol.EOFetchSpecification, com.webobjects.eocontrol.EOEditingContext)
     */
    public NSArray databaseContextShouldFetchObjects(EODatabaseContext dbCtxt, EOFetchSpecification fetchSpec, EOEditingContext ec) {
        return (NSArray)perform("databaseContextShouldFetchObjects", new Object[] {dbCtxt, fetchSpec, ec}, null);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldHandleDatabaseException(com.webobjects.eoaccess.EODatabaseContext, java.lang.Throwable)
     */
    public boolean databaseContextShouldHandleDatabaseException(EODatabaseContext dbCtxt, Throwable exception) {
        return booleanPerform("databaseContextShouldHandleDatabaseException", new Object[] {dbCtxt, exception}, true);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldInvalidateObjectWithGlobalID(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.eocontrol.EOGlobalID, com.webobjects.foundation.NSDictionary)
     */
    public boolean databaseContextShouldInvalidateObjectWithGlobalID(EODatabaseContext dbCtxt, EOGlobalID gid, NSDictionary dic) {
        return booleanPerform("databaseContextShouldInvalidateObjectWithGlobalID", new Object[] {dbCtxt, gid, dic}, true);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldLockObjectWithGlobalID(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.eocontrol.EOGlobalID, com.webobjects.foundation.NSDictionary)
     */
    public boolean databaseContextShouldLockObjectWithGlobalID(EODatabaseContext dbCtxt, EOGlobalID gid, NSDictionary dic) {
        return booleanPerform("databaseContextShouldLockObjectWithGlobalID", new Object[] {dbCtxt, gid, dic}, true);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldRaiseExceptionForLockFailure(com.webobjects.eoaccess.EODatabaseContext, java.lang.Throwable)
     */
    public boolean databaseContextShouldRaiseExceptionForLockFailure(EODatabaseContext dbCtxt, Throwable exception) {
        return booleanPerform("databaseContextShouldRaiseExceptionForLockFailure", new Object[] {dbCtxt, exception}, true);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldSelectObjects(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.eocontrol.EOFetchSpecification, com.webobjects.eoaccess.EODatabaseChannel)
     */
    public boolean databaseContextShouldSelectObjects(EODatabaseContext dbCtxt, EOFetchSpecification fetchSpec, EODatabaseChannel dbChannel) {
        return booleanPerform("databaseContextShouldSelectObjects", new Object[] {dbCtxt, fetchSpec, dbChannel}, true);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldUpdateCurrentSnapshot(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.foundation.NSDictionary, com.webobjects.foundation.NSDictionary, com.webobjects.eocontrol.EOGlobalID, com.webobjects.eoaccess.EODatabaseChannel)
     */
    public NSDictionary databaseContextShouldUpdateCurrentSnapshot(EODatabaseContext dbCtxt, NSDictionary dic, NSDictionary dic2, EOGlobalID gid, EODatabaseChannel dbChannel) {
        return (NSDictionary)perform("databaseContextShouldUpdateCurrentSnapshot", new Object[] {dbCtxt, dic, dic2, gid, dbChannel}, null);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextShouldUsePessimisticLock(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.eocontrol.EOFetchSpecification, com.webobjects.eoaccess.EODatabaseChannel)
     */
    public boolean databaseContextShouldUsePessimisticLock(EODatabaseContext dbCtxt, EOFetchSpecification fetchSpec, EODatabaseChannel dbChannel) {
        return booleanPerform("databaseContextShouldUsePessimisticLock", new Object[] {dbCtxt, fetchSpec, dbChannel}, false);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextWillFireArrayFaultForGlobalID(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.eocontrol.EOGlobalID, com.webobjects.eoaccess.EORelationship, com.webobjects.eocontrol.EOFetchSpecification, com.webobjects.eocontrol.EOEditingContext)
     */
    public void databaseContextWillFireArrayFaultForGlobalID(EODatabaseContext dbCtxt, EOGlobalID gid, EORelationship rel, EOFetchSpecification fetchSpec, EOEditingContext ec) {
        perform("databaseContextWillFireArrayFaultForGlobalID", new Object[] {dbCtxt, gid, rel, fetchSpec, ec}, null);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextWillFireObjectFaultForGlobalID(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.eocontrol.EOGlobalID, com.webobjects.eocontrol.EOFetchSpecification, com.webobjects.eocontrol.EOEditingContext)
     */
    public void databaseContextWillFireObjectFaultForGlobalID(EODatabaseContext dbCtxt, EOGlobalID gid, EOFetchSpecification fetchSpec, EOEditingContext ec) {
        perform("databaseContextWillFireObjectFaultForGlobalID", new Object[] {dbCtxt, gid, fetchSpec, ec}, null);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextWillOrderAdaptorOperations(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.foundation.NSArray)
     */
   public NSArray databaseContextWillOrderAdaptorOperations(EODatabaseContext dbCtxt, NSArray databaseOps) {
       NSArray result = (NSArray)perform("databaseContextWillOrderAdaptorOperations", new Object[] {dbCtxt, databaseOps}, null);

       /* OK, this is really quite brutal.  This delegate method has no way of returning an "I did not handle this so do the default thing"
        * response.  The default thing is private so it can't be called directly.  Rather than re-implement it, we use reflection to
        * gain access to the private default implementation private NSArray orderAdaptorOperations().
        */
       if (result == null)
       {
           try {
               result = (NSArray) orderAdaptorOperations.invoke(dbCtxt, new Object[] {});
           }
           catch (Exception e) {
               throw NSForwardException._runtimeExceptionForThrowable(e);
           }
       }
       return result;
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextWillPerformAdaptorOperations(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.foundation.NSArray, com.webobjects.eoaccess.EOAdaptorChannel)
     */
    public NSArray databaseContextWillPerformAdaptorOperations(EODatabaseContext dbCtxt, NSArray adaptorOps, EOAdaptorChannel adChannel) {
        return (NSArray)perform("databaseContextWillPerformAdaptorOperations", new Object[] {dbCtxt, adaptorOps, adChannel}, adaptorOps);
    }


    /**
     * @see com.webobjects.eoaccess.EODatabaseContext.Delegate#databaseContextWillRunLoginPanelToOpenDatabaseChannel(com.webobjects.eoaccess.EODatabaseContext, com.webobjects.eoaccess.EODatabaseChannel)
     */
    public boolean databaseContextWillRunLoginPanelToOpenDatabaseChannel(EODatabaseContext dbCtxt, EODatabaseChannel dbChannel) {
        return booleanPerform("databaseContextWillRunLoginPanelToOpenDatabaseChannel", new Object[] {dbCtxt, dbChannel}, true);
    }

}
