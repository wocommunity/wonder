//
//  ERXForwardingAdaptorChannel.java
//
//  Created by Thomas Burkholder on Thu May 10, 2005.
//
package er.extensions.eof;

import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOAdaptorOperation;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOStoredProcedure;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

public class ERXForwardingAdaptorChannel extends EOAdaptorChannel {
	private EOAdaptorChannel _forwardedChannel;

	public EOAdaptorChannel forwardedChannel() {
		return _forwardedChannel;
	}

	public ERXForwardingAdaptorChannel(EOAdaptorContext context, EOAdaptorChannel channel) {
		super(context);
		_forwardedChannel = channel;
	}

	@Override
	public boolean isOpen() {
		return _forwardedChannel.isOpen();
	}

	@Override
	public void openChannel() {
		_forwardedChannel.openChannel();
	}

	@Override
	public void closeChannel() {
		_forwardedChannel.closeChannel();
	}

	public EOAdaptorContext originalAdaptorContext() {
		return super.adaptorContext();
	}

	@Override
	public EOAdaptorContext adaptorContext() {
		return _forwardedChannel.adaptorContext();
	}

	@Override
	public void insertRow(NSDictionary row, EOEntity entity) {
		_forwardedChannel.insertRow(row, entity);
	}

	@Override
	public void updateValuesInRowDescribedByQualifier(NSDictionary row, EOQualifier qualifier, EOEntity entity) {
		_forwardedChannel.updateValuesInRowDescribedByQualifier(row, qualifier, entity);
	}

	@Override
	public int updateValuesInRowsDescribedByQualifier(NSDictionary row, EOQualifier qualifier, EOEntity entity) {
		return _forwardedChannel.updateValuesInRowsDescribedByQualifier(row, qualifier, entity);
	}

	@Override
	public void deleteRowDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
		_forwardedChannel.deleteRowDescribedByQualifier(qualifier, entity);
	}

	@Override
	public int deleteRowsDescribedByQualifier(EOQualifier qualifier, EOEntity entity) {
		return _forwardedChannel.deleteRowsDescribedByQualifier(qualifier, entity);
	}

	@Override
	public void selectAttributes(NSArray attributes, EOFetchSpecification fetchSpecification, boolean yn, EOEntity entity) {
		_forwardedChannel.selectAttributes(attributes, fetchSpecification, yn, entity);
	}

	@Override
	public void lockRowComparingAttributes(NSArray atts, EOEntity entity, EOQualifier qualifier, NSDictionary snapshot) {
		_forwardedChannel.lockRowComparingAttributes(atts, entity, qualifier, snapshot);
	}

	@Override
	public void evaluateExpression(EOSQLExpression expression) {
		_forwardedChannel.evaluateExpression(expression);
	}

	@Override
	public boolean isFetchInProgress() {
		return _forwardedChannel.isFetchInProgress();
	}

	@Override
	public NSArray describeResults() {
		return _forwardedChannel.describeResults();
	}

	@Override
	public NSMutableDictionary fetchRow() {
		return _forwardedChannel.fetchRow();
	}

	@Override
	public void setAttributesToFetch(NSArray attributes) {
		_forwardedChannel.setAttributesToFetch(attributes);
	}

	@Override
	public NSArray attributesToFetch() {
		return _forwardedChannel.attributesToFetch();
	}

	@Override
	public void cancelFetch() {
		_forwardedChannel.cancelFetch();
	}

	@Override
	public NSDictionary primaryKeyForNewRowWithEntity(EOEntity entity) {
		return _forwardedChannel.primaryKeysForNewRowsWithEntity(1, entity).objectAtIndex(0);
	}

	@Override
	public NSArray primaryKeysForNewRowsWithEntity(int count, EOEntity entity) {
		return _forwardedChannel.primaryKeysForNewRowsWithEntity(count, entity);
	}

	@Override
	public NSArray describeStoredProcedureNames() {
		return _forwardedChannel.describeStoredProcedureNames();
	}

	@Override
	public void addStoredProceduresNamed(NSArray storedProcedureNames, EOModel model) {
		_forwardedChannel.addStoredProceduresNamed(storedProcedureNames, model);
	}

	@Override
	public NSArray describeTableNames() {
		return _forwardedChannel.describeTableNames();
	}

	@Override
	public EOModel describeModelWithTableNames(NSArray tableNames) {
		return _forwardedChannel.describeModelWithTableNames(tableNames);
	}

	@Override
	public Object delegate() {
		return _forwardedChannel.delegate();
	}

	@Override
	public void setDelegate(Object delegate) {
		if (_forwardedChannel != null) {
			// It is null during initialization, so ignore
			_forwardedChannel.setDelegate(delegate);
		}
	}

	@Override
	public NSMutableDictionary dictionaryWithObjectsForAttributes(Object[] objects, NSArray attributes) {
		return _forwardedChannel.dictionaryWithObjectsForAttributes(objects, attributes);
	}

	@Override
	public void executeStoredProcedure(EOStoredProcedure storedProcedure, NSDictionary values) {
		_forwardedChannel.executeStoredProcedure(storedProcedure, values);
	}

	@Override
	public NSDictionary returnValuesForLastStoredProcedureInvocation() {
		return _forwardedChannel.returnValuesForLastStoredProcedureInvocation();
	}

	@Override
	public void performAdaptorOperation(EOAdaptorOperation adaptorOp) {
		_forwardedChannel.performAdaptorOperation(adaptorOp);
	}

	@Override
	public void performAdaptorOperations(NSArray adaptorOps) {
		_forwardedChannel.performAdaptorOperations(adaptorOps);
	}
}