//
//  ERXForwardingAdaptorContext.java
//
//  Created by Thomas Burkholder on Thu May 10, 2005.
//
package er.extensions;

import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;
import com.webobjects.eoaccess.*;
import java.util.*;

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
        _forwardedContext = forwardedContext;
        _registerForAdaptorContextNotifications();
    }

    public void _registerForAdaptorContextNotifications() {
        NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();
        notificationCenter.addObserver(this, _beginTransactionSelector, EOAdaptorContext.AdaptorContextBeginTransactionNotification, _forwardedContext);
        notificationCenter.addObserver(this, _commitTransactionSelector, EOAdaptorContext.AdaptorContextCommitTransactionNotification, _forwardedContext);
        notificationCenter.addObserver(this, _rollbackTransactionSelector, EOAdaptorContext.AdaptorContextRollbackTransactionNotification, _forwardedContext);
    }

    public void _unregisterForAdaptorContextNotifications() {
        NSNotificationCenter notificationCenter = NSNotificationCenter.defaultCenter();
        notificationCenter.removeObserver(this, EOAdaptorContext.AdaptorContextBeginTransactionNotification, _forwardedContext);
        notificationCenter.removeObserver(this, EOAdaptorContext.AdaptorContextCommitTransactionNotification, _forwardedContext);
        notificationCenter.removeObserver(this, EOAdaptorContext.AdaptorContextRollbackTransactionNotification, _forwardedContext);
    }

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

    public void beginTransaction() {
        _forwardedContext.beginTransaction();
    }

    public void commitTransaction() {
        _forwardedContext.commitTransaction();
    }
    
    public void rollbackTransaction() {
        _forwardedContext.rollbackTransaction();
    }
    
    public int transactionNestingLevel() {
        return _forwardedContext.transactionNestingLevel();
    }

    public boolean hasOpenTransaction() {
        return _forwardedContext.hasOpenTransaction();
    }

    public void transactionDidBegin() {
        _forwardedContext.transactionDidBegin();
    }

    public void transactionDidCommit() {
        _forwardedContext.transactionDidCommit();
    }

    public void transactionDidRollback() {
        _forwardedContext.transactionDidRollback();
    }

    public boolean hasBusyChannels() {
        return _forwardedContext.hasBusyChannels();
    }

    public boolean hasOpenChannels() {
        return _forwardedContext.hasOpenChannels();
    }

    public NSArray channels() {
        return _forwardedContext.channels();
    }

    public EOAdaptor originalAdaptor() {
        return super.adaptor();
    }

    public EOAdaptor adaptor() {
        return _forwardedContext.adaptor();
    }

    public EOAdaptorChannel createAdaptorChannel() {
        return _forwardedContext.createAdaptorChannel();
    }

    public Object delegate() {
        return _forwardedContext.delegate();
    }

    public void setDelegate(Object delegate) {
        _forwardedContext.setDelegate(delegate);
    }

    public void _registerAdaptorChannel(EOAdaptorChannel channel) {
        _forwardedContext._registerAdaptorChannel(channel);
    }

    public void _unregisterAdaptorChannel(EOAdaptorChannel channel) {
        _forwardedContext._unregisterAdaptorChannel(channel);
    }

    public boolean canNestTransactions() {
        return _forwardedContext.canNestTransactions();
    }

    public NSDictionary _newPrimaryKey(EOEnterpriseObject object, EOEntity entity) {
        return _forwardedContext._newPrimaryKey(object,entity);
    }

}
