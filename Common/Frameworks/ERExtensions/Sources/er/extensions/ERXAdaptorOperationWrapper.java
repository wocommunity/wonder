package er.extensions;

import java.io.*;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * This class is a wrapper for the EOAdaptorOperation class
 * in order to serialized the information from an EOAdaptorOperation to files.
 * It can used for example by ERXDatabaseContextDelegate to serialize 
 * operations to disk, useful for cross database replications.
 * 
 * @author david@cluster9.com
 */
public class ERXAdaptorOperationWrapper implements Serializable {
    public static final ERXLogger log = ERXLogger.getERXLogger(ERXAdaptorOperationWrapper.class);

    public static final NSRecursiveLock adaptorOperationsLock = new NSRecursiveLock();

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
     * @return
     */
    public static NSArray wrappedAdaptorOperations(NSArray adaptorOps) {
        adaptorOperationsLock.lock();
        try {
            ByteArrayOutputStream bous = new ByteArrayOutputStream();
            ObjectOutputStream os;
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
            EODatabaseChannel dchannel = context.availableChannel();
            EOAdaptorChannel achannel = dchannel.adaptorChannel();
            achannel.adaptorContext().beginTransaction();
            try {
                boolean wasOpen = achannel.isOpen();
                if (!wasOpen) {
                    achannel.openChannel();
                }
                for(int i = 0; i < ops.count(); i++) {
                    op = (ERXAdaptorOperationWrapper)ops.objectAtIndex(i);
                    try {
                        achannel.performAdaptorOperation(op.operation());
                    } catch(EOGeneralAdaptorException ex) {
                        log.error("Failed op " + i + ": " + ex + "\n" + op);
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

}