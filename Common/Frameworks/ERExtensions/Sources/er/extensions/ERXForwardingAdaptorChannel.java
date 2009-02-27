//
//  ERXForwardingAdaptorChannel.java
//
//  Created by Thomas Burkholder on Thu May 10, 2005.
//
package er.extensions;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;

import java.util.*;

public class ERXForwardingAdaptorChannel extends EOAdaptorChannel {
    private EOAdaptorChannel _forwardedChannel;
    
    public EOAdaptorChannel forwardedChannel() {
        return _forwardedChannel;
    }

    public ERXForwardingAdaptorChannel(EOAdaptorContext context, EOAdaptorChannel channel) {
	super(context);
        _forwardedChannel = channel;
    }

    public boolean isOpen() {
        return _forwardedChannel.isOpen();
    }

    public void openChannel() {
        _forwardedChannel.openChannel();
    }

    public void closeChannel() {
        _forwardedChannel.closeChannel();
    }

    public EOAdaptorContext originalAdaptorContext() {
        return super.adaptorContext();
    }

    public EOAdaptorContext adaptorContext() {
        return _forwardedChannel.adaptorContext();
    }

    public void insertRow(NSDictionary row, EOEntity entity) {
        _forwardedChannel.insertRow(row,entity);
    }

    public void updateValuesInRowDescribedByQualifier(NSDictionary row, EOQualifier qualifier, EOEntity entity) {
        _forwardedChannel.updateValuesInRowDescribedByQualifier(row,qualifier,entity);
    }

    public int updateValuesInRowsDescribedByQualifier(NSDictionary row, EOQualifier qualifier, EOEntity entity) {
        return _forwardedChannel.updateValuesInRowsDescribedByQualifier(row,qualifier,entity);
    }

    public void deleteRowDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
        _forwardedChannel.deleteRowDescribedByQualifier(qualifier,entity);
    }
    
    public int deleteRowsDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
        return _forwardedChannel.deleteRowsDescribedByQualifier(qualifier,entity);
    }
    
    @Override
    public void selectAttributes(NSArray<EOAttribute> attributes, EOFetchSpecification fetchSpecification, boolean yn, EOEntity entity) {
        _forwardedChannel.selectAttributes(attributes,fetchSpecification, yn, entity);
    }

    public void lockRowComparingAttributes(NSArray atts, EOEntity entity, EOQualifier qualifier, NSDictionary snapshot) {
        _forwardedChannel.lockRowComparingAttributes(atts,entity,qualifier,snapshot);
    }

    public void evaluateExpression(EOSQLExpression expression) {
        _forwardedChannel.evaluateExpression(expression);
    }

    public boolean isFetchInProgress() {
        return _forwardedChannel.isFetchInProgress();
    }
    
    public NSArray describeResults() {
        return _forwardedChannel.describeResults();
    }

    public NSMutableDictionary fetchRow() {
        return _forwardedChannel.fetchRow();
    }

    public void setAttributesToFetch(NSArray attributes) {
        _forwardedChannel.setAttributesToFetch(attributes);
    }

    public NSArray attributesToFetch() {
        return _forwardedChannel.attributesToFetch();
    }

    public void cancelFetch() {
        _forwardedChannel.cancelFetch();
    }

    public NSDictionary primaryKeyForNewRowWithEntity(EOEntity entity) {
        return _forwardedChannel.primaryKeyForNewRowWithEntity(entity);
    }

    public NSArray primaryKeysForNewRowsWithEntity(int count, EOEntity entity) {
        return _forwardedChannel.primaryKeysForNewRowsWithEntity(count,entity);
    }
    
    public NSArray describeStoredProcedureNames() {
        return _forwardedChannel.describeStoredProcedureNames();
    }

    public void addStoredProceduresNamed(NSArray storedProcedureNames, EOModel model) {
        _forwardedChannel.addStoredProceduresNamed(storedProcedureNames,model);
    }

    public NSArray describeTableNames() {
        return _forwardedChannel.describeTableNames();
    }

    public EOModel describeModelWithTableNames(NSArray tableNames) {
        return _forwardedChannel.describeModelWithTableNames(tableNames);
    }

    public Object delegate() {
        return _forwardedChannel.delegate();
    }
     
    public void setDelegate(Object delegate) {
        if (_forwardedChannel != null) { // It is null during initialization, so ignore
            _forwardedChannel.setDelegate(delegate);
        }
    }

    public NSMutableDictionary dictionaryWithObjectsForAttributes(Object[] objects, NSArray attributes) {
        return _forwardedChannel.dictionaryWithObjectsForAttributes(objects,attributes);
    }
    
    public void executeStoredProcedure(EOStoredProcedure storedProcedure, NSDictionary values) {
        _forwardedChannel.executeStoredProcedure(storedProcedure, values);
    }
    
    public NSDictionary returnValuesForLastStoredProcedureInvocation() {
        return _forwardedChannel.returnValuesForLastStoredProcedureInvocation();
    }
    
    public void performAdaptorOperation(EOAdaptorOperation adaptorOp) {
        _forwardedChannel.performAdaptorOperation(adaptorOp);
    }

    public void performAdaptorOperations(NSArray adaptorOps) {
        _forwardedChannel.performAdaptorOperations(adaptorOps);
    }
}
