package er.extensions.eof;

import java.io.Serializable;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorOperation;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EOUtilities;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.foundation.ERXProperties;

/**
 * This class is a wrapper for the EOAdaptorOperation class
 * in order to serialized the information from an EOAdaptorOperation to files.
 * It can used for example by ERXDatabaseContextDelegate to serialize 
 * operations to disk, useful for cross database replications.
 * 
 * @author david@cluster9.com
 * @author ak moved stuff from ERXEOAccessUtilities to here
 */
public class ERXAdaptorOperationWrapper implements Serializable {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    private static final Logger log = LoggerFactory.getLogger(ERXAdaptorOperationWrapper.class);

    public static final ReentrantLock adaptorOperationsLock = new ReentrantLock();
    
    public static final String AdaptorOperationsDidPerformNotification = "AdaptorOperationsDidPerform";
    
    private static Boolean postAdaptorOperationNotifications = null;

    transient EOAdaptorOperation operation;
    String                       entityName;
    private int                  operator;
    private NSArray              attributes;
    private NSDictionary         changedValues;
    private EOQualifier          qualifier;

    public ERXAdaptorOperationWrapper(EOAdaptorOperation aop) {
        super();
        operation = aop;
        entityName = aop.entity().name();
        operator = aop.adaptorOperator();
        attributes = aop.attributes();
        changedValues = aop.changedValues();
        qualifier = aop.qualifier();
    }

    public EOAdaptorOperation operation() {
        if (operation == null) {
            EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
            operation = new EOAdaptorOperation(entity);
            operation.setAdaptorOperator(operator);
            operation.setAttributes(attributes);
            operation.setChangedValues(changedValues);
            operation.setQualifier(qualifier);
        }
        return operation;
    }
 
    /**
     * Returns an array of ERXAdaptorOperations that can be serialzed to the transport of your choice.
     * @param adaptorOps
     */
    public static NSArray wrappedAdaptorOperations(NSArray adaptorOps) {
        adaptorOperationsLock.lock();
        try {
            NSMutableArray ops = new NSMutableArray();
            if(adaptorOps.count() > 0) {
                for (int i = 0; i < adaptorOps.count(); i++) {
                    EOAdaptorOperation a = (EOAdaptorOperation) adaptorOps.objectAtIndex(i);
                    ERXAdaptorOperationWrapper wrapper = new ERXAdaptorOperationWrapper(a);
                    ops.addObject(wrapper);
                }
            }
            return ops;
        } finally {
            adaptorOperationsLock.unlock();
        }
    }
    
    /**
     * Unwraps and performs the supplied adaptor operations.
     * @param ops
     */
    public static void performWrappedAdaptorOperations(NSArray ops) {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            // FIXME use the entityName information from each EOAdaptorOperation to get the correct
            // database context, this implementation here only works if all EOModels use the same database
            ERXAdaptorOperationWrapper op = (ERXAdaptorOperationWrapper) ops.lastObject();
            EODatabaseContext context = EOUtilities.databaseContextForModelNamed(ec, op.operation().entity().model().name());
            context.lock();
            adaptorOperationsLock.lock();
            try {
                EODatabaseChannel dchannel = context.availableChannel();
                EOAdaptorChannel achannel = dchannel.adaptorChannel();
                achannel.adaptorContext().beginTransaction();
                boolean wasOpen = achannel.isOpen();
                if (!wasOpen) {
                    achannel.openChannel();
                }
                for(int i = 0; i < ops.count(); i++) {
                    op = (ERXAdaptorOperationWrapper)ops.objectAtIndex(i);
                    try {
                        achannel.performAdaptorOperation(op.operation());
                    } catch(EOGeneralAdaptorException ex) {
                        log.error("Failed op {}: {}\n{}", i, ex, op);
                        throw ex;
                    }
                }
                achannel.adaptorContext().commitTransaction();
                if (!wasOpen) {
                    achannel.closeChannel();
                }
            } finally {
                adaptorOperationsLock.unlock();
                context.unlock();
            }
        } finally {
            ec.unlock();
        }
    }

    public static void adaptorOperationsDidPerform(NSArray ops) {
        if (postAdaptorOperationNotifications() && ops.count() > 0) {
            NSNotificationCenter.defaultCenter().postNotification(AdaptorOperationsDidPerformNotification, ops);
        }

    }

    /**
     * @return <code>true</code> if the system property
     *         <code>er.extensions.ERXDatabaseContextDelegate.postAdaptorOperationNotifications</code>
     *         is set to true and if ERXThreadStorage.valueForKey(disabledForThreadKey) returns false or null, false otherwise.
     */
    private static boolean postAdaptorOperationNotifications() {
        if (postAdaptorOperationNotifications == null) {
            postAdaptorOperationNotifications = ERXProperties.booleanForKeyWithDefault(
                    "er.extensions.ERXAdaptorationWrapper.postAdaptorOperationNotifications", false) ? Boolean.TRUE : Boolean.FALSE;
        }
        return postAdaptorOperationNotifications.booleanValue();
    }

}
