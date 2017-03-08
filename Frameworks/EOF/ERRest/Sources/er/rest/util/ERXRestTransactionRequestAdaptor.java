package er.rest.util;

import com.webobjects.appserver.WOApplication;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver.WOSession;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.appserver.ERXSession;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXProperties;
import er.rest.routes.ERXRouteRequestHandler;

/**
 * EXPERIMENTAL.
 * 
 * @property ERXRest.transactionsEnabled (default 'false')
 * @property ERXRestTransaction.transactionManager (default '50')
 * 
 * @author mschrag
 */
public class ERXRestTransactionRequestAdaptor {
	private static final String CLIENT_ID_HEADER_KEY = "Client-Id";
	private static final String SEQUENCE_ID_HEADER_KEY = "Seq-Id";
	private static final String TRANSACTION_HEADER_KEY = "Transaction";
	private static final String OPEN_TRANSACTION_HEADER_VALUE = "open";
	private static final String COMMIT_TRANSACTION_HEADER_VALUE = "commit";

	private static final String EXECUTING_TRANSACTION_KEY = "er.rest.ERXRestTransaction.transaction";
	private static final String TRANSACTION_MANAGER_KEY = "er.rest.ERXRestTransaction.transactionManager";
	private static ERXRestTransactionRequestAdaptor _defaultAdaptor;

	private boolean _transactionsEnabled;
	private int _maxEventsPerTransaction;
	
	public static synchronized ERXRestTransactionRequestAdaptor defaultAdaptor() {
		if (_defaultAdaptor == null) {
			_defaultAdaptor = new ERXRestTransactionRequestAdaptor();
		}
		return _defaultAdaptor;
	}

	public ERXRestTransactionRequestAdaptor() {
		_transactionsEnabled = ERXProperties.booleanForKeyWithDefault("ERXRest.transactionsEnabled", false);
		_maxEventsPerTransaction = ERXProperties.intForKeyWithDefault("ERXRest.maxEventsPerTransaction", 50);
	}

	protected EOEditingContext newEditingContext() {
		return ERXEC.newEditingContext();
	}

	public boolean transactionsEnabled() {
		return _transactionsEnabled;
	}
	
	public boolean hasSequence(WOContext context, WORequest request) {
		return request.headerForKey(ERXRestTransactionRequestAdaptor.SEQUENCE_ID_HEADER_KEY) != null;
	}

	public boolean hasTransaction(WOContext context, WORequest request) {
		return request.headerForKey(ERXRestTransactionRequestAdaptor.TRANSACTION_HEADER_KEY) != null;
	}

	public boolean isExecutingTransaction(WOContext context, WORequest request) {
		return executingTransaction(context, request) != null;
	}
	
	public ERXRestTransaction executingTransaction(WOContext context, WORequest request) {
		ERXRestTransaction transaction = null;
		NSDictionary<String, Object> userInfo = request.userInfo();
		if (userInfo != null) {
			transaction = (ERXRestTransaction)userInfo.objectForKey(ERXRestTransactionRequestAdaptor.EXECUTING_TRANSACTION_KEY);
		}
		return transaction;
	}
	
	protected void setExecutingTransaction(ERXRestTransaction transaction, WOContext context, WORequest request) {
		NSDictionary<String, Object> immutableUserInfo = request.userInfo();
		NSMutableDictionary<String, Object> userInfo = (immutableUserInfo == null) ? new NSMutableDictionary<>() : immutableUserInfo.mutableClone();
		userInfo.setObjectForKey(transaction, ERXRestTransactionRequestAdaptor.EXECUTING_TRANSACTION_KEY);
		request.setUserInfo(userInfo);
	}
	
	public ERXRestTransaction transaction(WOContext context, WORequest request) {
		ERXRestTransaction transaction = null;
		if (transaction == null) {
			ERXRestTransactionManager transactionManager = transactionManager(context, request);
			transaction = transaction(context, request, transactionManager);
		}
		return transaction;
	}

	public boolean willHandleRequest(WOContext context, WORequest request) {
		boolean shouldDispatchRequest = true;
		Integer sequenceIDInteger = sequenceID(request);
		if (sequenceIDInteger != null) {
			int sequenceID = sequenceIDInteger.intValue();
			ERXRestTransactionManager transactionManager = transactionManager(context, request);
			transactionManager.addSequenceID(sequenceID);

			ERXRestTransaction.State state = state(request);
			if (state != null) {
				ERXRestTransaction transaction = transaction(context, request, transactionManager);
				transaction.addEvent(sequenceID, state, request);
				shouldDispatchRequest = false;

				if (transaction.size() > _maxEventsPerTransaction) {
					transactionManager.removeTransaction(transaction);
					throw new IllegalArgumentException("You exceeded the maximum number of events for a single transaction.");
				}
			}
		}
		return shouldDispatchRequest;
	}
	
	public boolean didHandleRequest(WOContext context, WORequest request) {
		boolean shouldHandleRequest = true;
		ERXRestTransactionManager transactionManager = transactionManager(context, request);
		ERXRestTransaction transaction = transaction(context, request, transactionManager);
		if (transactionManager.isTransactionReady(transaction)) {
			shouldHandleRequest = false;

			// MS: This is sketchy -- basically we're about to execute a pile of requests on the same thread and we can't
			// check out the session again on the same thread, so we're going to forcefully check it back in
			if (context._session() != null) {
				// MS: Should we sleep it? Should we just not do this at all?
				// wosession._sleepInContext(null);
				WOApplication.application().sessionStore().checkInSessionForContext(context);
				context._setSession(null);
				ERXSession.setSession(null);
			}
			try {
				EOEditingContext editingContext = transaction.editingContext();
				try {
					for (Object record : transaction.records()) {
						WORequest recordRequest = (WORequest)record;
						setExecutingTransaction(transaction, context, recordRequest);
						recordRequest.removeHeadersForKey(ERXRestTransactionRequestAdaptor.CLIENT_ID_HEADER_KEY);
						recordRequest.removeHeadersForKey(ERXRestTransactionRequestAdaptor.SEQUENCE_ID_HEADER_KEY);
						recordRequest.removeHeadersForKey(ERXRestTransactionRequestAdaptor.TRANSACTION_HEADER_KEY);
						
						ERXRouteRequestHandler requestHandler = (ERXRouteRequestHandler)WOApplication.application().handlerForRequest(recordRequest);
						WOResponse response = requestHandler.handleRequest(recordRequest);
						if (response.status() < 200 || response.status() > 299) {
							throw new RuntimeException("Transaction failed: " + response.contentString());
						}
					}
					editingContext.saveChanges();
				}
				finally {
					transactionManager.removeTransaction(transaction);
					editingContext.dispose();
				}
			}
			finally {
				context.session();
			}
		}
		return shouldHandleRequest;
	}

	protected String clientID(WORequest request) {
		return request.headerForKey(ERXRestTransactionRequestAdaptor.CLIENT_ID_HEADER_KEY);
	}

	protected Integer sequenceID(WORequest request) {
		Integer sequenceID = null;
		String sequenceIDStr = request.headerForKey(ERXRestTransactionRequestAdaptor.SEQUENCE_ID_HEADER_KEY);
		if (sequenceIDStr != null) {
			sequenceID = Integer.parseInt(sequenceIDStr);
		}
		return sequenceID;
	}

	protected ERXRestTransaction.State state(WORequest request) {
		String stateStr = request.headerForKey(ERXRestTransactionRequestAdaptor.TRANSACTION_HEADER_KEY);
		ERXRestTransaction.State state;
		if (stateStr == null) {
			state = null;
		}
		else if (ERXRestTransactionRequestAdaptor.COMMIT_TRANSACTION_HEADER_VALUE.equals(stateStr)) {
			state = ERXRestTransaction.State.Commit;
		}
		else if (ERXRestTransactionRequestAdaptor.OPEN_TRANSACTION_HEADER_VALUE.equals(stateStr)) {
			state = ERXRestTransaction.State.Open;
		}
		else {
			throw new IllegalArgumentException("Unknown transaction state: " + stateStr);
		}
		return state;
	}

	protected ERXRestTransactionManager transactionManager(WOContext context, WORequest request) {
		WOSession session = context.session();
		ERXRestTransactionManager transactionManager = (ERXRestTransactionManager) session.objectForKey(ERXRestTransactionRequestAdaptor.TRANSACTION_MANAGER_KEY);
		if (transactionManager == null) {
			transactionManager = new ERXRestTransactionManager();
			session.setObjectForKey(transactionManager, ERXRestTransactionRequestAdaptor.TRANSACTION_MANAGER_KEY);
		}
		return transactionManager;
	}

	protected ERXRestTransaction transaction(WOContext context, WORequest request, ERXRestTransactionManager transactionManager) {
		WOSession session = context.session();

		String clientID = clientID(request);
		if (clientID == null) {
			clientID = session.sessionID();
		}

		return transactionManager.transactionForID(clientID);
	}
}
