/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions.eof;

import java.util.Enumeration;

import org.apache.log4j.Logger;

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
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotificationCenter;

import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXUtilities;
import er.extensions.jdbc.ERXJDBCConnectionAnalyzer;
import er.extensions.logging.ERXPatternLayout;
import er.extensions.statistics.ERXStats;
import er.extensions.statistics.ERXStats.Group;

/**
 * This delegate implements several methods from the formal interface
 * {@link com.webobjects.eoaccess.EODatabaseContext.Delegate EODatabaseContext.Delegate}. 
 * Of special note this class adds the ability
 * for enterpriseobjects to generate their own primary keys, correctly throws an
 * exception when a toOne relationship object is not found in the database and adds
 * debugging abilities to tracking down when faults are fired. It also supports a cache for
 * array fault that is checked before they are fetched from the database.
 */
public class ERXDatabaseContextDelegate {
	
	public static final String DatabaseContextFailedToFetchObject = "DatabaseContextFailedToFetchObject";
	
    public static class ObjectNotAvailableException extends EOObjectNotAvailableException {
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
    public final static Logger log = Logger.getLogger(ERXDatabaseContextDelegate.class);
    /** Faulting logging support, logging category: <b>er.transaction.adaptor.FaultFiring</b> */
    public final static Logger dbLog = Logger.getLogger("er.transaction.adaptor.FaultFiring");
    /** Faulting logging support, logging category: <b>er.transaction.adaptor.Exceptions</b> */
    public final static Logger exLog = Logger.getLogger("er.transaction.adaptor.Exceptions");
    /** Faulting logging support, logging category: <b>er.transaction.adaptor.Batching</b> */
    public final static Logger batchLog = Logger.getLogger("er.transaction.adaptor.Batching");

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
	 * @param databaseContext
	 * @param throwable
	 */
    public boolean databaseContextShouldHandleDatabaseException(EODatabaseContext databaseContext, Throwable throwable) {
    	if(exLog.isDebugEnabled()) {
    		exLog.debug("Database Exception occured: " + throwable, throwable);
    	} else if(exLog.isInfoEnabled()) {
    		exLog.info("Database Exception occured: " + throwable);
    	}
    	if(throwable.getMessage() != null && throwable.getMessage().indexOf("_obtainOpenChannel") != -1) {
    		NSArray models = databaseContext.database().models();
    		for(Enumeration e = models.objectEnumerator(); e.hasMoreElements(); ) {
    			EOModel model = (EOModel)e.nextElement();
    			NSDictionary dict = model.connectionDictionary();
    			log.info(model.name() + ": " + (dict == null ? "No connection dictionary!" : dict.toString()));
    		}
    		if ("JDBC".equals(databaseContext.adaptorContext().adaptor().name())) {
    			new ERXJDBCConnectionAnalyzer(databaseContext.database().adaptor().connectionDictionary());
    		}
    	}
    	//EOEditingContext ec = ERXEC.newEditingContext();
    	//log.info(NSPropertyListSerialization.stringFromPropertyList(EOUtilities.modelGroup(ec).models().valueForKey("connectionDictionary")));
    	return true;
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
    public NSDictionary databaseContextNewPrimaryKey(EODatabaseContext databaseContext, Object object, EOEntity entity) {
        return object instanceof ERXGeneratesPrimaryKeyInterface ? ((ERXGeneratesPrimaryKeyInterface)object).primaryKeyDictionary(true) : null;
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
        EOAdaptor adaptor=dbc.adaptorContext().adaptor();
        boolean shouldHandleConnection = false;
        if(e instanceof EOGeneralAdaptorException)
            log.error(((EOGeneralAdaptorException)e).userInfo());
        else
            log.error(e);
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
                log.info(((EOGeneralAdaptorException)e).userInfo());
            throw e;
        }
        return shouldHandleConnection;
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
     * The delegate is not reentrant, so this marks whether we are already batch faulting a to-many relationship.
     */
    private boolean fetchingToMany = false;

    /**
     * The delegate is not reentrant, so this marks whether we are already batch faulting a to-one relationship.
     */
    private boolean fetchingToOne = false;
    
    /**
     * Holds the auto batch fetch size.
     */
    public static int autoBatchFetchSize = -1;
    
    /**
     * Returns the batch size for automatic batch faulting from the System property <code>er.extensions.ERXDatabaseContextDelegate.autoBatchFetchSize</code>.
     * Default is 0, meaning it's disabled.
     * @return batch size
     */
    public static int autoBatchFetchSize() {
    	if(autoBatchFetchSize == -1) {
    		autoBatchFetchSize = ERXProperties.intForKeyWithDefault("er.extensions.ERXDatabaseContextDelegate.autoBatchFetchSize", 0);
    	}
    	return autoBatchFetchSize;
    }

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

    public interface BatchHandler {

		/**
		 * Override this to skip fetching for the given ec and relationship. The
		 * default is to skip abstract destination entities of to-many
		 * relationships. You might want to exclude very large destinations or
		 * sources and so on.
		 * 
		 * @param ec
		 * @param relationship
		 * @return int batch size (0 for not batch fetch)
		 */
    	public int batchSizeForRelationship(EOEditingContext ec, EORelationship rel);
    }
    
    /**
     * Refreshes the fetch timestamp for the fetched objects.
     * @param eos
     * @param ec
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
	private synchronized boolean batchFetchToManyFault(EODatabaseContext dbc, Object obj) {
		if (!fetchingToMany) {
			fetchingToMany = true;
			try {
				EOAccessArrayFaultHandler handler = (EOAccessArrayFaultHandler) EOFaultHandler.handlerForFault(obj);
				EOEditingContext ec = handler.editingContext();
				EOEnterpriseObject source = ec.faultForGlobalID(handler.sourceGlobalID(), ec);
				if (source instanceof AutoBatchFaultingEnterpriseObject) {
					String key = handler.relationshipName();
					EOEntityClassDescription cd = (EOEntityClassDescription) source.classDescription();
					EORelationship relationship = cd.entity().relationshipNamed(key);
					if (_handler.batchSizeForRelationship(ec, relationship) > 0) {
						long timestamp = ((AutoBatchFaultingEnterpriseObject) source).batchFaultingTimeStamp();
						NSMutableArray<EOEnterpriseObject> eos = new NSMutableArray<EOEnterpriseObject>();
						markStart("ToMany.Calculation", source, key);
						NSMutableArray faults = new NSMutableArray();
						for (EOEnterpriseObject eo : (NSArray<EOEnterpriseObject>) ec.registeredObjects()) {
							if (eo instanceof AutoBatchFaultingEnterpriseObject) {
								if (((AutoBatchFaultingEnterpriseObject) eo).batchFaultingTimeStamp() == timestamp) {
									if (!EOFaultHandler.isFault(eo) && eo.classDescription() == source.classDescription()) {
										Object fault = eo.storedValueForKey(key);
										if (EOFaultHandler.isFault(fault)) {
											faults.addObject(fault);
											eos.addObject(eo);
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
							// dbc.batchFetchRelationship(relationship, eos, ec);
							ERXEOAccessUtilities.batchFetchRelationship(dbc, relationship, eos, ec, true);
							// ERXBatchFetchUtilities.batchFetch(eos, relationship.name());
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
				fetchingToMany = false;
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
	 * @param obj
	 *            to-one fault
	 * @return true if it's still a fault.
	 */

	private synchronized boolean batchFetchToOneFault(EODatabaseContext dbc, AutoBatchFaultingEnterpriseObject eo) {
		if(!fetchingToOne) {
			fetchingToOne = true;
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
						NSMutableArray<EOEnterpriseObject> eos = new NSMutableArray<EOEnterpriseObject>();
						NSMutableSet faults = new NSMutableSet();
						for (EOEnterpriseObject current : (NSArray<EOEnterpriseObject>)ec.registeredObjects()) {
							if (current instanceof AutoBatchFaultingEnterpriseObject) {
								AutoBatchFaultingEnterpriseObject currentEO = (AutoBatchFaultingEnterpriseObject) current;
								if(currentEO.batchFaultingTimeStamp() == timestamp) {
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
							// dbc.batchFetchRelationship(relationship, eos, ec);
							ERXEOAccessUtilities.batchFetchRelationship(dbc, relationship, eos, ec, true);
							// ERXBatchFetchUtilities.batchFetch(eos, relationship.name());
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
				fetchingToOne = false;
			}
		}
		return true;
	}

	private BatchHandler DEFAULT = new BatchHandler() {
		public int batchSizeForRelationship(EOEditingContext ec, EORelationship relationship) {
			// AK: this looks like a bug in EOF: when we have a flattened toMany (probably also to-one) to an abstract,
			// the fetch doesn't also fetch the qualifiers for the destination, so the GID ends up without the correct sub-entity.
			// So when the fault is fired, we get an exception.
			// In the single-table case, we COULD "fix" this by overriding faultForGlobalID, catching and then trying with the right entity.
			// but we'd probably end up with a totally messed EOF state...
			return(relationship.isToMany() && relationship.isFlattened() && relationship.destinationEntity().isAbstractEntity()) ? 0 : autoBatchFetchSize();
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
}
