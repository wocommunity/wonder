/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import java.util.Enumeration;

import org.apache.log4j.Logger;

import java.io.*;
import java.sql.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import java.util.Enumeration;

/**
 * This delegate implements several methods from the formal interface
 * {@link com.webobjects.eoaccess.EODatabaseContext.Delegate EODatabaseContext.Delegate}. 
 * Of special note this class adds the ability
 * for enterpiseobjects to generate their own primary keys, correctly throws an
 * exception when a toOne relationship object is not found in the database and adds
 * debugging abilities to tracking down when faults are fired.
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

    /** Holds onto the singleton of the default delegate */
    private static ERXDatabaseContextDelegate _defaultDelegate = new ERXDatabaseContextDelegate();

	/** Returns the singleton of the database context delegate */
    public static ERXDatabaseContextDelegate defaultDelegate() {
        return _defaultDelegate;
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
     * an {@link com.webobjects.eoaccess.EOObjectNotAvailableException EOObjectNotAvailableException}.
     * @param context database context
     * @param object object that is firing the fault for a given to-one relationship
     * @param gid global id that wasn't found in the database.
     */
    public boolean databaseContextFailedToFetchObject(EODatabaseContext context, Object object, EOGlobalID gid) {
        if (object!=null) {
            EOEditingContext ec = ((EOEnterpriseObject)object).editingContext();

            // we need to refault the object before raising, otherwise, if the caller traps
            // the exception, it will be a successful lookup the next time a fault with the
            // same global id fires.  NOTE: refaulting in a sharedEditingContext is illegal,
            // so we specifically check for that special case.

            if (!(ec instanceof EOSharedEditingContext)) {
                context.refaultObject((EOEnterpriseObject)object, gid, ec);
            }
        }
        throw new EOObjectNotAvailableException("No " + (object!=null ? object.getClass().getName() : "N/A") + " found with globalID: " + gid);            
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
            dbLog.debug("databaseContextDidSelectObjects " + fs+ ERXUtilities.stackTrace());
        }
    }

    /**
        Delegate method. Will switch the connection to read write.
     **/
    public NSArray databaseContextWillPerformAdaptorOperations(EODatabaseContext dbCtxt,NSArray adaptorOps,EOAdaptorChannel adChannel) {
        log.debug("databaseContextWillPerformAdaptorOperations.. Setting it to ReadWrite");
        if (adaptorOps.count() != 0) {
            setReadWriteForConnectionInDatabaseContext(true, dbCtxt);
        }
        return adaptorOps;
    }

    /**
        Delegate method. Will switch the connection to read only.
     **/
    public boolean databaseContextShouldFetchObjectFault(EODatabaseContext dbCtxt, Object obj) {
        log.debug("databaseContextShouldFetchObjectFault.. Setting it to ReadOnly");
        setReadWriteForConnectionInDatabaseContext(false, dbCtxt);
        return true;
    }

    /**
        Delegate method. Will switch the connection to read only.
     **/
    public  boolean databaseContextShouldFetchArrayFault(EODatabaseContext eodatabasecontext, Object obj) {
        log.debug("databaseContextShouldFetchArrayFault.. Setting it to ReadOnly");
        setReadWriteForConnectionInDatabaseContext(false, eodatabasecontext);
        return true;
    }

    /**
        Delegate method. Will switch the connection to read only.
     **/
    public  NSArray databaseContextShouldFetchObjects(EODatabaseContext eodatabasecontext, EOFetchSpecification
                                                      eofetchspecification, EOEditingContext eoeditingcontext) {
        log.debug("databaseContextShouldFetchObjects.. Setting it to ReadOnly");
        setReadWriteForConnectionInDatabaseContext(false, eodatabasecontext);
        return null;
    }

    /**
        Switch the connection to read/write mode.

     @param isReadWrite true if the connection should be set to read/write, false if it should be set to read only
     @param dbc the EODatabaseContext to use to get the java.sql.Connection object

     FIXME: should support per-model sql statements in order to support different databases
     **/
    public void setReadWriteForConnectionInDatabaseContext(boolean isReadWrite, EODatabaseContext dbc) {
        if (_readOnlySessionProperties().length() > 0 && _readWriteSessionProperties().length() > 0) {
            log.debug("ReadOnly and ReadWrite Transactions enabled, trying to change");
            try {

                Connection connection = _getConnection(dbc);
                if (connection != null) {
                    if (isReadWrite) {
                        _configureReadWrite(connection);
                    } else {
                        _configureReadOnly(connection);
                    }
                } else {
                    log.warn("Cannot change readoonly/readwrite level since the connection for the editing context is null!!");
                }

            } catch (java.sql.SQLException e) {
                log.error("Cannot change transaction level for databse, received " + e.getMessage(), e);
            } 
        }
    }
    
    public Connection _getConnection(EODatabaseContext dbc) {
        return ((com.webobjects.jdbcadaptor.JDBCContext) dbc.adaptorContext()).connection();
    }

    
    public void _configureReadWrite(Connection aConnection) throws SQLException {
        if (log.isDebugEnabled())
            log.debug("Setting the JDBC connection "+aConnection+" to read / write, current state:"+
                      " isReadOnly="+aConnection.isReadOnly()+
                      ", isolation level="+aConnection.getTransactionIsolation());
        aConnection.commit();
        aConnection.createStatement().executeUpdate(_readWriteSessionProperties());
        aConnection.commit();
    }

    public void _configureReadOnly(Connection aConnection) throws SQLException {
        if (log.isDebugEnabled())
            log.debug("Setting the JDBC connection "+aConnection+" to read only, current state:"+
                      " isReadOnly="+aConnection.isReadOnly()+
                      ", isolation level="+aConnection.getTransactionIsolation());
        aConnection.commit();
        aConnection.createStatement().executeUpdate(_readOnlySessionProperties());
        aConnection.commit();
    }

    private String _readOnlySessionProperties;
    public String _readOnlySessionProperties() {
        if (_readOnlySessionProperties == null) {
            _readOnlySessionProperties = ERXProperties.stringForKeyWithDefault("er.extensions.ERXDatabaseContextDelegate.readOnlySessionProperties","");
        }
        return _readOnlySessionProperties;
    }

    private String _readWriteSessionProperties;
    public String _readWriteSessionProperties() {
        if (_readWriteSessionProperties == null) {
            _readWriteSessionProperties = ERXProperties.stringForKeyWithDefault("er.extensions.ERXDatabaseContextDelegate.readWriteSessionProperties","");
        }
        return _readWriteSessionProperties;
    }
}
