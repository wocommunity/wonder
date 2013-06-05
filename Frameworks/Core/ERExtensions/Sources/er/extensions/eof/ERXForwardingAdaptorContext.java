//
//  ERXForwardingAdaptorContext.java
//
//  Created by Thomas Burkholder on Thu May 10, 2005.
//
package er.extensions.eof;

import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorContext;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSelector;
import com.webobjects.foundation._NSUtilities;

public class ERXForwardingAdaptorContext extends EOAdaptorContext {

	private EOAdaptorContext _forwardedContext;

	public static Object defaultDelegate() {
		return null;
	}

	public EOAdaptorContext forwardedContext() {
		return _forwardedContext;
	}

	public ERXForwardingAdaptorContext(EOAdaptor adaptor, EOAdaptorContext forwardedContext) {
		super(adaptor);
		Object delegate = delegate();
		_forwardedContext = forwardedContext;
		if (delegate != null) {
			_forwardedContext.setDelegate(delegate);
		}
		_registerForAdaptorContextNotifications();
	}

	public void _registerForAdaptorContextNotifications() {
		NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();
		notificationCenter.addObserver(this, ERXForwardingAdaptorContext._beginTransactionSelector, EOAdaptorContext.AdaptorContextBeginTransactionNotification, _forwardedContext);
		notificationCenter.addObserver(this, ERXForwardingAdaptorContext._commitTransactionSelector, EOAdaptorContext.AdaptorContextCommitTransactionNotification, _forwardedContext);
		notificationCenter.addObserver(this, ERXForwardingAdaptorContext._rollbackTransactionSelector, EOAdaptorContext.AdaptorContextRollbackTransactionNotification, _forwardedContext);
	}

	public void _unregisterForAdaptorContextNotifications() {
		NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();
		notificationCenter.removeObserver(this, EOAdaptorContext.AdaptorContextBeginTransactionNotification, _forwardedContext);
		notificationCenter.removeObserver(this, EOAdaptorContext.AdaptorContextCommitTransactionNotification, _forwardedContext);
		notificationCenter.removeObserver(this, EOAdaptorContext.AdaptorContextRollbackTransactionNotification, _forwardedContext);
	}

	@Override
	public void handleDroppedConnection() {
		_forwardedContext.handleDroppedConnection();
		_unregisterForAdaptorContextNotifications();
	}

	private static final NSSelector _beginTransactionSelector = new NSSelector("_beginTransaction", _NSUtilities._NotificationClassArray);
	private static final NSSelector _commitTransactionSelector = new NSSelector("_commitTransaction", _NSUtilities._NotificationClassArray);
	private static final NSSelector _rollbackTransactionSelector = new NSSelector("_rollbackTransaction", _NSUtilities._NotificationClassArray);

	public void _beginTransaction(NSNotification notification) {
		NSNotificationCenter.defaultCenter().postNotification(EOAdaptorContext.AdaptorContextBeginTransactionNotification, this);
	}

	public void _commitTransaction(NSNotification notification) {
		NSNotificationCenter.defaultCenter().postNotification(EOAdaptorContext.AdaptorContextCommitTransactionNotification, this);
	}

	public void _rollbackTransaction(NSNotification notification) {
		NSNotificationCenter.defaultCenter().postNotification(EOAdaptorContext.AdaptorContextRollbackTransactionNotification, this);
	}

	@Override
	public void beginTransaction() {
		_forwardedContext.beginTransaction();
	}

	@Override
	public void commitTransaction() {
		_forwardedContext.commitTransaction();
	}

	@Override
	public void rollbackTransaction() {
		_forwardedContext.rollbackTransaction();
	}

	/**
	 * @deprecated use {@link #hasOpenTransaction()} instead
	 */
	@Override
	@Deprecated
	public int transactionNestingLevel() {
		return _forwardedContext.transactionNestingLevel();
	}

	@Override
	public boolean hasOpenTransaction() {
		return _forwardedContext.hasOpenTransaction();
	}

	@Override
	public void transactionDidBegin() {
		_forwardedContext.transactionDidBegin();
	}

	@Override
	public void transactionDidCommit() {
		_forwardedContext.transactionDidCommit();
	}

	@Override
	public void transactionDidRollback() {
		_forwardedContext.transactionDidRollback();
	}

	@Override
	public boolean hasBusyChannels() {
		return _forwardedContext.hasBusyChannels();
	}

	@Override
	public boolean hasOpenChannels() {
		return _forwardedContext.hasOpenChannels();
	}

	@Override
	public NSArray channels() {
		return _forwardedContext.channels();
	}

	public EOAdaptor originalAdaptor() {
		return super.adaptor();
	}

	@Override
	public EOAdaptor adaptor() {
		return _forwardedContext.adaptor();
	}

	@Override
	public EOAdaptorChannel createAdaptorChannel() {
		return _forwardedContext.createAdaptorChannel();
	}

	@Override
	public Object delegate() {
		return _forwardedContext == null ? super.delegate() : _forwardedContext.delegate();
	}

	@Override
	public void setDelegate(Object delegate) {
		if (_forwardedContext != null) {
			_forwardedContext.setDelegate(delegate);
		}
		else {
			super.setDelegate(delegate);
		}
	}

	@Override
	public void _registerAdaptorChannel(EOAdaptorChannel channel) {
		_forwardedContext._registerAdaptorChannel(channel);
	}

	@Override
	public void _unregisterAdaptorChannel(EOAdaptorChannel channel) {
		_forwardedContext._unregisterAdaptorChannel(channel);
	}

	@Override
	@Deprecated
	public boolean canNestTransactions() {
		return _forwardedContext.canNestTransactions();
	}

	@Override
	public NSDictionary _newPrimaryKey(EOEnterpriseObject object, EOEntity entity) {
		return _forwardedContext._newPrimaryKey(object, entity);
	}

}