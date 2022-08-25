/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof;

import java.util.Enumeration;
import java.util.concurrent.CopyOnWriteArrayList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.webobjects.eoaccess.EOAccessArrayFaultHandler;
import com.webobjects.eoaccess.EOAdaptor;
import com.webobjects.eoaccess.EOAdaptorChannel;
import com.webobjects.eoaccess.EOAdaptorOperation;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EODatabaseChannel;
import com.webobjects.eoaccess.EODatabaseContext;
import com.webobjects.eoaccess.EODatabaseOperation;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOEntityClassDescription;
import com.webobjects.eoaccess.EOGeneralAdaptorException;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eoaccess.EOObjectNotAvailableException;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eoaccess.EOSQLExpression;
import com.webobjects.eoaccess.EOSQLExpressionFactory;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EOFetchSpecification;
import com.webobjects.eocontrol.EOGlobalID;
import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.eocontrol.EOSharedEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXThreadStorage;
import er.extensions.foundation.ERXUtilities;
import er.extensions.jdbc.ERXJDBCConnectionAnalyzer;
import er.extensions.jdbc.ERXSQLHelper;
import er.extensions.logging.ERXPatternLayout;
import er.extensions.statistics.ERXStats;
import er.extensions.statistics.ERXStats.Group;

/**
 * This delegate implements several methods from the formal interface
 * {@link com.webobjects.eoaccess.EODatabaseContext.Delegate EODatabaseContext.Delegate}. 
 * Of special note this class adds the ability
 * for enterprise objects to generate their own primary keys, correctly throws an
 * exception when a toOne relationship object is not found in the database and adds
 * debugging abilities to tracking down when faults are fired. It also supports a cache for
 * array fault that is checked before they are fetched from the database.
 * 
 * @property er.extensions.ERXDatabaseContextDelegate.Exceptions.regex regular expression to
 *           check the exception test for if database connection should be reopened
 */
public class ERXDatabaseContextDelegate {
	
	public static final String DatabaseContextFailedToFetchObject = "DatabaseContextFailedToFetchObject";
	public static final String ERX_ADAPTOR_EXCEPTIONS_REGEX = "er.extensions.ERXDatabaseContextDelegate.Exceptions.regex";
	public static final String ERX_ADAPTOR_EXCEPTIONS_REGEX_DEFAULT = ".*_obtainOpenChannel.*|.*Closed Connection.*|.*No more data to read from socket.*";
	
    public static class ObjectNotAvailableException extends EOObjectNotAvailableException {
    	/**
    	 * Do I need to update serialVersionUID?
    	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
    	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
    	 */
    	private static final long serialVersionUID = 1L;

    	private EOGlobalID globalID;
    	
		public ObjectNotAvailableException(String message) {
			this(message, null);
		}
    	
		public ObjectNotAvailableException(String message, EOGlobalID gid) {
			super(message);
			globalID = gid;
		}
		
		public EOGlobalID globalID() {
			return globalID;
		}
    	
    }
	
    /** Basic logging support */
    public final static Logger log = LoggerFactory.getLogger(ERXDatabaseContextDelegate.class);
    /** Faulting logging support, logging category: <b>er.transaction.adaptor.FaultFiring</b> */
    public final static Logger dbLog = LoggerFactory.getLogger("er.transaction.adaptor.FaultFiring");
    /** Faulting logging support, logging category: <b>er.transaction.adaptor.Exceptions</b> */
    public final static Logger exLog = LoggerFactory.getLogger("er.transaction.adaptor.Exceptions");
    /** Faulting logging support, logging category: <b>er.transaction.adaptor.Batching</b> */
    public final static Logger batchLog = LoggerFactory.getLogger("er.transaction.adaptor.Batching");

    /** Holds onto the singleton of the default delegate */
    private static ERXDatabaseContextDelegate _defaultDelegate = new ERXDatabaseContextDelegate();
    
    private ERXArrayFaultCache _arrayFaultCache = null;
    private ERXFetchResultCache _fetchResultCache = null;
    
    /** Returns the singleton of the database context delegate */
    public static ERXDatabaseContextDelegate defaultDelegate() {
        return _defaultDelegate;
    }
    
    /**
     * @param delegate - the singleton database context delegate to set
     */
    public static void setDefaultDelegate(ERXDatabaseContextDelegate delegate) {
    	_defaultDelegate = delegate;
	}
    
    public ERXArrayFaultCache arrayFaultCache() {
        return _arrayFaultCache;
    }

    public void setArrayFaultCache(ERXArrayFaultCache value) {
        _arrayFaultCache = value;
    }

    public ERXFetchResultCache fetchResultCache() {
        return _fetchResultCache;
    }
    
    public void setFetchResultCache(ERXFetchResultCache value) {
    	_fetchResultCache = value;
    }
    
    /**
	 * Returns an array of already fetched objects or null if they were not already fetched.
	 * @param dbc
	 * @param fs
	 * @param ec
	 */
	public NSArray databaseContextShouldFetchObjects(EODatabaseContext dbc, EOFetchSpecification fs, EOEditingContext ec) {
		NSArray result = null;
		ERXFetchResultCache fetchResultCache = fetchResultCache();
		if (fetchResultCache != null) {
			result = fetchResultCache.objectsForFetchSpecification(dbc, ec, fs);
		}
		return result;
	}

	/**
	 * Sets the cache entry for the fetched objects and refreshes the timestamps
	 * for fetched objects if batch faulting is enabled.
	 * 
	 * @param dbc
	 * @param eos
	 * @param fs
	 * @param ec
	 */
	public void databaseContextDidFetchObjects(EODatabaseContext dbc, NSArray eos, EOFetchSpecification fs, EOEditingContext ec) {
		ERXFetchResultCache fetchResultCache = fetchResultCache();
		if (fetchResultCache != null) {
			fetchResultCache.setObjectsForFetchSpecification(dbc, ec, eos, fs);
		}
		if(autoBatchFetchSize() > 0 && eos.count() > 0) {
			//log.info("Freshen: " + fs.entityName() +  " " + eos.count());
			freshenFetchTimestamps(eos, ec.fetchTimestamp());
		}
	}

    /**
	 * Provides for a hook to get at the original exceptions from the JDBC
	 * driver, as opposed to the cooked EOGeneralAdaptorException you get from
	 * EOF. To see the exceptions trace, set the logger
	 * er.transaction.adaptor.Exceptions to DEBUG.
	 * 
	 * @param databaseContext the current database context
	 * @param throwable the original exception
	 * @return <code>true</code> if the exception has been handled already
	 */
	public boolean databaseContextShouldHandleDatabaseException(EODatabaseContext databaseContext, Throwable throwable) {
		if(!reportingError.canEnter(databaseContext)) return true;
		try {
			if(exLog.isDebugEnabled()) {
				exLog.debug("Database Exception occured: " + throwable, throwable);
			} else if(exLog.isInfoEnabled()) {
				exLog.info("Database Exception occured: " + throwable);
			}
			boolean handled = false;
			try {
				handled = ERXSQLHelper.newSQLHelper(databaseContext).handleDatabaseException(databaseContext, throwable);
			} catch(RuntimeException e) {
				databaseContext.rollbackChanges();
				throw e;
			}
			String exceptionsRegex = ERXProperties.stringForKeyWithDefault(ERX_ADAPTOR_EXCEPTIONS_REGEX, ERX_ADAPTOR_EXCEPTIONS_REGEX_DEFAULT);
			if(!handled && throwable.getMessage() != null && throwable.getMessage().matches(exceptionsRegex)) {
				NSArray models = databaseContext.database().models();
				for(Enumeration e = models.objectEnumerator(); e.hasMoreElements(); ) {
					EOModel model = (EOModel)e.nextElement();
					NSDictionary connectionDictionary = model.connectionDictionary();
					if (connectionDictionary != null) {
						NSMutableDictionary mutableConnectionDictionary = connectionDictionary.mutableClone();
						mutableConnectionDictionary.setObjectForKey("<password deleted for log>", "password");
						connectionDictionary = mutableConnectionDictionary;
					}
					log.info(model.name() + ": " + (connectionDictionary == null ? "No connection dictionary!" : connectionDictionary.toString()));
				}
				if ("JDBC".equals(databaseContext.adaptorContext().adaptor().name())) {
					new ERXJDBCConnectionAnalyzer(databaseContext.database().adaptor().connectionDictionary());
				}
			}
			//EOEditingContext ec = ERXEC.newEditingContext();
			//log.info(NSPropertyListSerialization.stringFromPropertyList(EOUtilities.modelGroup(ec).models().valueForKey("connectionDictionary")));
			return !handled;
		} finally {
			reportingError.leave(databaseContext);
		}
	}

    /**
     * Provides the ability for new enterprise objects that implement the interface {@link ERXGeneratesPrimaryKeyInterface}
     * to provide their own primary key dictionary. If the enterprise object implements the above interface then the
     * method <code>primaryKeyDictionary(true)</code> will be called on the object. If the object returns null then a
     * primary key will be generated for the object in the usual fashion.
     * @param databaseContext databasecontext
     * @param object the new enterprise object
     * @param entity the entity of the object
     * @return primary key dictionary to be used or null if a primary key should be generated for the object.
     */
    public NSDictionary<String, Object> databaseContextNewPrimaryKey(EODatabaseContext databaseContext, Object object, EOEntity entity) {
        return object instanceof ERXGeneratesPrimaryKeyInterface ? ((ERXGeneratesPrimaryKeyInterface)object).rawPrimaryKeyDictionary(true) : null;
    }

    /**
     * Allows custom handling of dropped connection exceptions. This was needed in WebObjects 4.5 because the
     * OracleEOAdaptor wouldn't correctly handle all exceptions of dropped connections. This may not be needed
     * now.
     * @param dbc current database context
     * @param e throw exception
     * @return if the exception is one of the bad ones that isn't handled then the method <code>handleDroppedConnection</code>
     *         is called directly on the database object of the context and <code>false</code> is returned otherwise <code>true</code>.
     */
    // CHECKME: Is this still needed now?
    public boolean databaseContextShouldHandleDatabaseException(EODatabaseContext dbc, Exception e) throws Throwable {
    	if(!reportingError.canEnter(dbc)) return true;
    	try {
    		EOAdaptor adaptor=dbc.adaptorContext().adaptor();
    		boolean shouldHandleConnection = false;
    		if(e instanceof EOGeneralAdaptorException)
    			log.error(((EOGeneralAdaptorException)e).userInfo().toString());
    		else
    			log.error(e.getMessage(), e);
    		if (adaptor.isDroppedConnectionException(e))
    			shouldHandleConnection = true;
    		// FIXME: Should provide api to extend the list of bad exceptions.
    		else if (e.toString().indexOf("ORA-01041")!=-1) {
    			// just returning true here does not seem to do the trick. why !?!?
    			log.error("ORA-01041 detecting -- forcing reconnect");
    			dbc.database().handleDroppedConnection();
    			shouldHandleConnection = false;
    		} else {
    			if(e instanceof EOGeneralAdaptorException)
    				log.info(((EOGeneralAdaptorException)e).userInfo().toString());
    			throw e;
    		}
        	return shouldHandleConnection;
    	} finally {
    		reportingError.leave(dbc);
    	}
    }

    /**
     * This is Kelly Hawks' fix for the missing to one relationship. 
     * Delegate on EODatabaseContext that gets called when a to-one fault cannot find its data in
     * the database. The object that is returned is a cleared fault.
     * We raise here to restore the functionality that existed prior to WebObjects 4.5.
     * Whenever a fault fails for a globalID (i.e. the object is NOT found in the database), we raise
     * an {@link com.webobjects.eoaccess.EOObjectNotAvailableException EOObjectNotAvailableException}. <br>
     * If you have entities you don't really care about, you can set the system property
     * <code>er.extensions.ERXDatabaseContextDelegate.tolerantEntityPattern</code> to a regular expression
     * that will be tested against the GID entity name. If it matches, then only an error will be logged
     * but no exception will be thrown.
     * 
     * @param context database context
     * @param object object that is firing the fault for a given to-one relationship
     * @param gid global id that wasn't found in the database.
     */
    public boolean databaseContextFailedToFetchObject(EODatabaseContext context, Object object, EOGlobalID gid) {
    	String tolerantEntityPattern = ERXProperties.stringForKey("er.extensions.ERXDatabaseContextDelegate.tolerantEntityPattern");
    	boolean raiseException = true;
    	if(tolerantEntityPattern != null && (gid instanceof EOKeyGlobalID)) {
    		if(((EOKeyGlobalID)gid).entityName().matches(tolerantEntityPattern)) {
    			raiseException = false;
    		}
    	}
        if (object!=null) {
            EOEditingContext ec = ((EOEnterpriseObject)object).editingContext();

            // we need to refault the object before raising, otherwise, if the caller traps
            // the exception, it will be a successful lookup the next time a fault with the
            // same global id fires.  NOTE: refaulting in a sharedEditingContext is illegal,
            // so we specifically check for that special case.

            if (!(ec instanceof EOSharedEditingContext) && raiseException) {
                context.refaultObject((EOEnterpriseObject)object, gid, ec);
            }
        }
        String gidString;
        if(gid instanceof EOKeyGlobalID) {
            // ak: when you use 24 byte PKs, the output is unreadable otherwise 
            EOKeyGlobalID kgid = (EOKeyGlobalID)gid;
            gidString = "<" +  kgid.entityName() + ": [" ;
            EOEntity entity = ERXEOAccessUtilities.entityNamed(null, kgid.entityName());
            NSArray pks = entity.primaryKeyAttributes();
            NSArray values = kgid.keyValuesArray();
            EOSQLExpressionFactory expressionFactory = context.database().adaptor().expressionFactory();
            EOSQLExpression expression = null;
            if (expressionFactory != null) {
            	expression = expressionFactory.expressionForEntity(entity);
            }
            for(int i = 0; i < pks.count(); i++) {
                Object value = values.objectAtIndex(i);
                EOAttribute attribute = (EOAttribute) pks.objectAtIndex(i);
                // ak: only Postgres seems to return reasonable values here...
                String stringValue = "" + value;
                if (expression != null) {
                	stringValue = expression.formatValueForAttribute(value, attribute);
                }
                if("NULL".equals(stringValue)) {
                    stringValue = "" + value;
                }
                gidString += attribute.name() + ": \'" +  stringValue + "\'"
                + (i == pks.count() - 1 ? "" : ", ");
            }
            gidString += "] >";
            
        } else {
            gidString = gid.toString();
        }
        NSNotificationCenter.defaultCenter().postNotification(DatabaseContextFailedToFetchObject, object);
        if(raiseException) {
        	throw new ObjectNotAvailableException("No " + (object!=null ? object.getClass().getName() : "N/A") + " found with globalID: " + gidString, gid); 
        } else if (ERXProperties.booleanForKeyWithDefault("er.extensions.ERXDatabaseContextDelegate.logTolerantEntityNotAvailable", true)) {
        	log.error("No " + (object!=null ? object.getClass().getName() : "N/A") + " found with globalID: " + gidString + "\n" + ERXUtilities.stackTrace()); 
        }
        return false;
    }
    
    /**
     * This delegate method is called every time a fault is fired that needs
     * to go to the database. All we have added is logging statement of the
     * debug priority. This way during runtime a developer can toggle the
     * logger priority settting on and off to see what faults are firing. Also
     * note that when using {@link ERXPatternLayout} one can set the option to
     * see full backtraces to the calling method. With this option specified
     * a developer can see exactly which methods are firing faults.
     * @param dc the databasecontext
     * @param fs the fetchspecification
     * @param channel the databasechannel
     */
    public void databaseContextDidSelectObjects(EODatabaseContext dc,
                                                EOFetchSpecification fs,
                                                EODatabaseChannel channel) {
        if (dbLog.isDebugEnabled()) {
            dbLog.debug("databaseContextDidSelectObjects " + fs, new Exception());
        }
    }

	/**
	 * This delegate method first checks the arrayFaultCache if it is set before
	 * trying to resolve the fault from the DB. It can be a severe performance
	 * optimization depending on your setup. Also, it support batch fetching of
	 * to-many relationships of EOs that were fetched at the "same" time.
	 * 
	 * @param dbc
	 * @param obj
	 */

    public boolean databaseContextShouldFetchArrayFault(EODatabaseContext dbc, Object obj) {
    	if(_arrayFaultCache != null) {
    		_arrayFaultCache.clearFault(obj);
    		if(!EOFaultHandler.isFault(obj)) {
    			return false;
    		}
    	}
    	if(autoBatchFetchSize() > 0) {
    		return batchFetchToManyFault(dbc, obj);
    	}
    	return true;
    }

    /**
     * Batch fetches to one relationships if enabled.
     * @param dbc
     * @param obj
     * @return true if the fault should get fetched
     */
	public boolean databaseContextShouldFetchObjectFault(EODatabaseContext dbc, Object obj) {
		if(autoBatchFetchSize() > 0 && obj instanceof AutoBatchFaultingEnterpriseObject) {
			return batchFetchToOneFault(dbc, (AutoBatchFaultingEnterpriseObject)obj);
		}
		return true;
	}

    /**
     * Overridden to remove inserts and deletes of the "same" row. When you
     * delete from a join table and then re-add the same object, then the
     * order of operations would be insert, then delete and you will get
     * an error because the delete would try to also delete the newly inserted
     * row. Here we just check every insert and see if the deleted contains the same
     * object. If they do, we just skip both operations,
     * @author chello team!
     * @param dbCtxt
     * @param adaptorOps
     * @param adChannel
     */
    public NSArray databaseContextWillPerformAdaptorOperations(EODatabaseContext dbCtxt, 
    		NSArray adaptorOps, EOAdaptorChannel adChannel) {
    	NSMutableArray result = new NSMutableArray();
    	NSDictionary groupedOps = ERXArrayUtilities.arrayGroupedByKeyPath(adaptorOps, "adaptorOperator");	
    	Integer insertKey = ERXConstant.integerForInt(EODatabaseOperation.AdaptorInsertOperator);
    	NSArray insertOps = (NSArray) groupedOps.objectForKey(insertKey);
    	Integer deleteKey = ERXConstant.integerForInt(EODatabaseOperation.AdaptorDeleteOperator);
    	NSArray deleteOps = (NSArray) groupedOps.objectForKey(deleteKey);
    	if (insertOps!=null && deleteOps!=null) {
    		NSMutableSet skippedOps = new NSMutableSet();
    		for(Enumeration e = insertOps.objectEnumerator(); e.hasMoreElements();) {
    			EOAdaptorOperation insertOp = (EOAdaptorOperation)e.nextElement();
    			for(Enumeration e1 = deleteOps.objectEnumerator(); e1.hasMoreElements();) {
    				EOAdaptorOperation deleteOp = (EOAdaptorOperation)e1.nextElement();
    				if(!skippedOps.containsObject(deleteOp)) {
    					if(insertOp.entity() == deleteOp.entity()) {
    						if(deleteOp.qualifier().evaluateWithObject(insertOp.changedValues())) {
    							if(false) {
    								// here we remove both the delete and the 
    								// insert. this might fail if we didn't lock on all rows
    								// FIXME: check the current snapshot in the database and
    								// see if it is the same as the new insert

    								skippedOps.addObject(deleteOp);
    								skippedOps.addObject(insertOp);
    							} else {
    								// here we put the delete up front, this might fail if
    								// we have cascading delete rules in the database
    								result.addObject(deleteOp);
    								skippedOps.addObject(deleteOp);
    							}
    							log.warn("Skipped: " + insertOp + "\n" + deleteOp);
    						}
    					}
    				}
    			}
    		}
        	for(Enumeration e = adaptorOps.objectEnumerator(); e.hasMoreElements();) {
        		EOAdaptorOperation op = (EOAdaptorOperation)e.nextElement();
        		if(!skippedOps.containsObject(op)) {
        			result.addObject(op);
        		}
        	}
    	} else {
    		result.addObjectsFromArray(adaptorOps);
    	}
    	return result;
    }

    /**
     * The delegate is not reentrant, so this marks whether we are already reporting an error (most probably due to a logging pattern)
     */
    private ReentranceProtector reportingError = new ReentranceProtector();

    /**
     * The delegate is not reentrant, so this marks whether we are already batch faulting a to-many relationship.
     */
    private ReentranceProtector fetchingToMany = new ReentranceProtector();

    /**
     * The delegate is not reentrant, so this marks whether we are already batch faulting a to-one relationship.
     */
    private ReentranceProtector fetchingToOne = new ReentranceProtector();
    
    /**
     * Holds the auto batch fetch size.
     */
    public static int autoBatchFetchSize = -1;
    
    /**
     * Batching thread key
     */
    
    public static final String THREAD_KEY = "ERXBatching";

    /**
     * Interface to provide auto-magic batch fetching. For an implementation (and the hack needed for to-one handling), see {@link ERXGenericRecord}.
     */
    
    public interface AutoBatchFaultingEnterpriseObject extends EOEnterpriseObject {
    	/**
    	 * Last time fetched.
    	 * @return millis of last fetch.
    	 */
    	public long batchFaultingTimeStamp();
    	
    	/**
    	 * Updates the last time this object was fetched.
    	 * @param fetchTimestamp
    	 */
    	public void setBatchFaultingTimestamp(long fetchTimestamp);
    	
    	/**
    	 * Marks the object as touched from the specified source with the specified key.
    	 * @param toucher
    	 * @param relationship name of relationship
    	 */
    	public void touchFromBatchFaultingSource(AutoBatchFaultingEnterpriseObject toucher, String relationship);
    	
    	/**
    	 * The GID of the object that last touched us, or null.
    	 * @return gid of touching object.
    	 */
		public EOGlobalID batchFaultingSourceGlobalID();
		
		/**
		 * The key through which we were last accessed.
		 * @return relationship name or null
		 */
    	public String batchFaultingRelationshipName();
    }
    
    private class ReentranceProtector {
    	public ReentranceProtector() {}
    	
    	private CopyOnWriteArrayList<EODatabaseContext> _accessing = new CopyOnWriteArrayList<EODatabaseContext>();
    	
    	public boolean canEnter(EODatabaseContext dbc) {
    		if(_accessing.contains(dbc)) {
    			return false;
    		}
    		_accessing.add(dbc);
    		return true;
    	}
    	
       	public void leave(EODatabaseContext dbc) {
    		_accessing.remove(dbc);
    	}
    }

    public interface BatchHandler {

		/**
		 * Override this to skip fetching for the given ec and relationship. The
		 * default is to skip abstract destination entities of to-many
		 * relationships. You might want to exclude very large destinations or
		 * sources and so on.
		 * 
		 * @param ec
		 * @param rel
		 * @return int batch size (0 for not batch fetch)
		 */
    	public int batchSizeForRelationship(EOEditingContext ec, EORelationship rel);
    }
    
    /**
     * Refreshes the fetch timestamp for the fetched objects.
     * @param eos
     * @param timestamp
     */
	private void freshenFetchTimestamps(NSArray eos, long timestamp) {
		for(Object eo: eos) {
			if (eo instanceof AutoBatchFaultingEnterpriseObject) {
				AutoBatchFaultingEnterpriseObject ft = (AutoBatchFaultingEnterpriseObject) eo;
				ft.setBatchFaultingTimestamp(timestamp);
			}
		}
	}
	
	private void markStart(String type, EOEnterpriseObject eo, String key) {
		if(ERXStats.isTrackingStatistics()) {
			ERXStats.markStart(Group.Batching, type + "." + eo.entityName()+"."+key);
		}
	}
	
	private void markEnd(String type, EOEnterpriseObject eo, String key) {
		if(ERXStats.isTrackingStatistics()) {
			ERXStats.markEnd(Group.Batching,  type + "." + eo.entityName()+"."+key);
		}
	}
    
    /**
     * Returns the batch size for automatic batch faulting from the System property <code>er.extensions.ERXDatabaseContextDelegate.autoBatchFetchSize</code>.
     * Default is 0, meaning it's disabled.
     * @return batch size
     */
    public static int autoBatchFetchSize() {
    	if(autoBatchFetchSize == -1) {
    		autoBatchFetchSize = ERXProperties.intForKeyWithDefault("er.extensions.ERXDatabaseContextDelegate.autoBatchFetchSize", 0);
    	}
    	//if(true) return 50;
    	return autoBatchFetchSize;
    }

	/**
	 * Fetches the to-many fault and the faults of all other objects in the EC
	 * that have the same relationship, the fetch timestamp and the same class
	 * description.<br>
	 * 
	 * @param dbc
	 *            database context
	 * @param obj
	 *            to-many fault
	 * @return true if it's still a fault.
	 */
	private boolean batchFetchToManyFault(EODatabaseContext dbc, Object obj) {
		if (fetchingToMany.canEnter(dbc)) {
			try {
				EOAccessArrayFaultHandler handler = (EOAccessArrayFaultHandler) EOFaultHandler.handlerForFault(obj);
				EOEditingContext ec = handler.editingContext();
				EOEnterpriseObject source = ec.faultForGlobalID(handler.sourceGlobalID(), ec);
				if (source instanceof AutoBatchFaultingEnterpriseObject) {
					String key = handler.relationshipName();
					EOEntityClassDescription cd = (EOEntityClassDescription) source.classDescription();
					EORelationship relationship = cd.entity().relationshipNamed(key);
					if (_handler.batchSizeForRelationship(ec, relationship) > 0) {
						markStart("ToMany.Calculation", source, key);
						NSArray<EOEnterpriseObject> candidates = null;
						NSArray currentObjects = (NSArray) ERXThreadStorage.valueForKey(THREAD_KEY);
						boolean fromThreadStorage = false;
						if (currentObjects != null) {
							NSMutableArray<EOEnterpriseObject> tmpList = new NSMutableArray<>();
							for (Object tmpItem : currentObjects) {
								if (tmpItem instanceof AutoBatchFaultingEnterpriseObject) {
									tmpList.add((EOEnterpriseObject) tmpItem);
								}
							}
							if (tmpList.count() > 0) {
								candidates = tmpList;
								fromThreadStorage = true;
							}
						}
						if (candidates == null) {
							candidates = ec.registeredObjects();
						}
						long timestamp = ((AutoBatchFaultingEnterpriseObject) source).batchFaultingTimeStamp();
						NSMutableArray<EOEnterpriseObject> eos = new NSMutableArray<>();
						NSMutableArray faults = new NSMutableArray();
						for (EOEnterpriseObject current : candidates) {
							if (current instanceof AutoBatchFaultingEnterpriseObject) {
								AutoBatchFaultingEnterpriseObject currentEO = (AutoBatchFaultingEnterpriseObject) current;
								if (currentEO.batchFaultingTimeStamp() == timestamp || fromThreadStorage) {
									if (!EOFaultHandler.isFault(currentEO) && currentEO.classDescription() == source.classDescription()) {
										Object fault = currentEO.storedValueForKey(key);
										if (EOFaultHandler.isFault(fault)) {
											faults.addObject(fault);
											eos.addObject(currentEO);
											if (eos.count() == autoBatchFetchSize()) {
												break;
											}
										}
									}
								}
							}
						}
						markEnd("ToMany.Calculation", source, key);
						if (eos.count() > 1) {
							markStart("ToMany.Fetching", source, key);
							doFetch(dbc, ec, relationship, eos);
							int cnt = 0;
							for(Object fault: faults) {
								if(!EOFaultHandler.isFault(fault)) {
									NSArray array = (NSArray)fault;
									freshenFetchTimestamps(array, timestamp);
									cnt += array.count();
								}
							}
							markEnd("ToMany.Fetching", source, key);
							if(batchLog.isDebugEnabled()) {
								batchLog.debug("Fetched " + cnt + " to-many " + relationship.destinationEntity().name() + " from " + eos.count() +  " " + source.entityName() + " for " + key);
							}
							return EOFaultHandler.isFault(obj);
						}
					}
				}
			}
			finally {
				fetchingToMany.leave(dbc);
			}
		}
		return true;
	}

	/**
	 * Fetches the to-one fault and the faults of all other objects in the EC
	 * that have the same relationship, the fetch timestamp and the same class
	 * description.<br>
	 * 
	 * @param dbc
	 *            database context
	 * @param eo
	 *            to-one fault
	 * @return true if it's still a fault.
	 */

	private synchronized boolean batchFetchToOneFault(EODatabaseContext dbc, AutoBatchFaultingEnterpriseObject eo) {
		if(fetchingToOne.canEnter(dbc)) {
			try {
				EOGlobalID sourceGID = eo.batchFaultingSourceGlobalID();
				String key = eo.batchFaultingRelationshipName();
				if(sourceGID != null && key != null) {
					EOEditingContext ec = eo.editingContext();
					AutoBatchFaultingEnterpriseObject source = (AutoBatchFaultingEnterpriseObject) ec.faultForGlobalID(sourceGID, ec);
					EOEntityClassDescription cd = (EOEntityClassDescription)source.classDescription();
					EORelationship relationship = cd.entity().relationshipNamed(key);
					if(_handler.batchSizeForRelationship(ec, relationship) > 0 && !relationship.isToMany()) {
						markStart("ToOne.Calculation", source, key);
						long timestamp = source.batchFaultingTimeStamp();
						boolean fromThreadStorage = false;
						NSMutableArray<EOEnterpriseObject> eos = new NSMutableArray<>();
						NSMutableSet faults = new NSMutableSet();
						NSArray<EOEnterpriseObject> candidates = null;
						NSArray currentObjects = (NSArray) ERXThreadStorage.valueForKey(THREAD_KEY);
						if (currentObjects != null) {
							NSMutableArray<EOEnterpriseObject> tmpList = new NSMutableArray<>();
							for (Object tmpItem : currentObjects) {
								if (tmpItem instanceof AutoBatchFaultingEnterpriseObject) {
									tmpList.add((EOEnterpriseObject) tmpItem);
								}
							}
							if (tmpList.count() > 0) {
								candidates = tmpList;
								fromThreadStorage = true;
							}
						}
						if (candidates == null) {
							candidates = ec.registeredObjects();
						}
						for (EOEnterpriseObject current : candidates) {
							if (current instanceof AutoBatchFaultingEnterpriseObject) {
								AutoBatchFaultingEnterpriseObject currentEO = (AutoBatchFaultingEnterpriseObject) current;
								if(currentEO.batchFaultingTimeStamp() == timestamp || fromThreadStorage) {
									if(source.classDescription() == currentEO.classDescription()) {
										if(!EOFaultHandler.isFault(currentEO)) {
											Object fault = currentEO.storedValueForKey(key);
											if(EOFaultHandler.isFault(fault)) {
												faults.addObject(fault);
												eos.addObject(currentEO);
												if(eos.count() == autoBatchFetchSize()) {
													break;
												}
											}
										}
									}
								}
							}
						}

						markEnd("ToOne.Calculation", source, key);
						if(eos.count() > 1) {
							markStart("ToOne.Fetching", source, key);
							doFetch(dbc, ec, relationship, eos);
							freshenFetchTimestamps(faults.allObjects(), timestamp);
							markEnd("ToOne.Fetching", source, key);
							if(batchLog.isDebugEnabled()) {
								batchLog.debug("Fetched " + faults.count() + " to-one " + relationship.destinationEntity().name() + " from " + eos.count() +  " " + source.entityName() + " for " + key);
							}
							return EOFaultHandler.isFault(eo);
						}
					}
				}
			} finally {
				fetchingToOne.leave(dbc);
			}
		}
		return true;
	}

	private void doFetch(EODatabaseContext dbc, EOEditingContext ec, EORelationship relationship, NSArray eos) {
		// dbc.batchFetchRelationship(relationship, eos, ec);
		ERXEOAccessUtilities.batchFetchRelationship(dbc, relationship, eos, ec, true);
		//ERXBatchFetchUtilities.batchFetch(eos, relationship.name());
	}
	
	private BatchHandler DEFAULT = new BatchHandler() {
		public int batchSizeForRelationship(EOEditingContext ec, EORelationship relationship) {
			return autoBatchFetchSize();
		}
		
	};
	
	private BatchHandler _handler = DEFAULT;
	
	/**
	 * Sets the batch handler.
	 * @param handler
	 */
	public void setBatchHandler(BatchHandler handler) {
		if(handler == null) {
			handler = DEFAULT;
		}
		_handler = handler;
	}
	
	public static void setCurrentBatchObjects(NSArray arr) {
		if(ERXDatabaseContextDelegate.autoBatchFetchSize() > 0) {
			if(arr == null || arr.lastObject() instanceof EOEnterpriseObject) {
				ERXThreadStorage.takeValueForKey(arr, ERXDatabaseContextDelegate.THREAD_KEY);
			}
		}
	}
}
