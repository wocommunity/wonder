package er.extensions;

import java.io.*;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * @author david@cluster9.com
 * (c) 2005, David Teran
 * 
 * This class is a wrapper for the EOAdaptorOperation class
 * in order to serialized the information from an EOAdaptorOperation to file
 * Its used for example by ERXDatabaseContextDelegate to serialize 
 * operations to disk, useful for cross database replications.
 */
public class ERXAdaptorOperationWrapper implements Serializable {

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
}