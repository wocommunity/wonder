/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import java.lang.*;
import java.util.*;
import java.io.*;
import org.apache.log4j.Category;

public class ERXExtensions {

    ////////////////////////////////////////////////  log4j category  //////////////////////////////////////////
    private static Category _cat;
    public static Category cat() {
        if (_cat == null)
            _cat = Category.getInstance(ERXExtensions.class);
        return _cat;
    }

    // EditngContextDelegate methods
    private static ERXEditingContextDelegate _defaultEditingContextDelegate;
    private static ERXECNoValidationDelegate _defaultECNoValidationDelegate;

    public static class Observer {
        public void didSave(NSNotification n) {
            ERXGenericRecord.didSave(n);
        }
        public void finishedLaunchingApp(NSNotification n) {
            ERXLog4j.configureRapidTurnAround(); // Will only enable if WOCaching is off.
            ERXExtensions.warmUpRuleCache();
            ERXSession.registerNotifications();
            //ERXValidationFactory.defaultFactory().configureFactory();
            //ERWebLint.configureWebLint();
        }
        public void sessionDidTimeOut(NSNotification n) {
            String sessionID=(String)n.object();
            ERXExtensions.sessionDidTimeOut(sessionID);
        }
    }

    // called implicitely because ERXExtensions is the principal class of the framework
    private static boolean _isInitialized=false;
    static {
        if (!_isInitialized) {
            // This will configure the Log4j system.
            // This is OK to call multiple times as it will only be configured the first time.
            try {
            ERXLog4j.configureLogging();
            ERXConfigurationManager.initializeDefaults();
            cat().info("Initializing framework: ERXExtensions");
            // Initing defaultEditingContext delegates
            _defaultEditingContextDelegate = new ERXDefaultEditingContextDelegate();
            _defaultECNoValidationDelegate = new ERXECNoValidationDelegate();
            // has to be retained on the objC side
            ERXRetainer.retain(_defaultEditingContextDelegate);
            ERXRetainer.retain(_defaultECNoValidationDelegate);

            // Super-screwy error caused by da bridge, app hangs when attempting to init the objc side of
            // SimpleHTMLFormatter used in one of our eos.  Very, very strange.  This works around the issue
            //ERXExtensions.configureAdaptorContextRapidTurnAround();
            EODatabaseContext.setDefaultDelegate(ERXDatabaseContextDelegate.defaultDelegate());
            //EREntityClassDescription.registerDescription();
            // This patches shared eo loading so cross model relationships to shared eos work.
            //ERXSharedEOLoader.patchSharedEOLoading();
            Observer observer = new Observer();
            ERXRetainer.retain(observer); // has to be retained on the objC side!!
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("didSave", ERXConstant.NotificationClassArray),
                                                             EOEditingContext.EditingContextDidSaveChangesNotification,
                                                             null);
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("finishedLaunchingApp", ERXConstant.NotificationClassArray),
                                                             WOApplication.ApplicationDidFinishLaunchingNotification,
                                                             null);
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("sessionDidTimeOut", ERXConstant.NotificationClassArray),
                                                             WOSession.SessionDidTimeOutNotification,
                                                             null);
            _isInitialized=true;
            } catch (Exception e) {
                System.out.println("Caught exception: " + e.getMessage() + " stack: ");
                e.printStackTrace();
            }
        }
    }

    public static Category adaptorCategory = Category.getInstance("er.transaction.adaptor.EOAdaptorDebugEnabled");
    public static Category sharedEOAdaptorCategory = Category.getInstance("er.transaction.adaptor.EOSharedEOAdaptorDebugEnabled");
    private static Boolean adaptorEnabled;

    private static boolean _isConfigureAdaptorContextRapidTurnAround = false;
    public static void configureAdaptorContextRapidTurnAround() {
        if (!_isConfigureAdaptorContextRapidTurnAround) {
            // This allows enabling from the log4j system.
            if (adaptorCategory.isDebugEnabled() && NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration))
                NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration);
            adaptorEnabled = NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration) ? Boolean.TRUE : Boolean.FALSE;

            Object observer=new Object() {
                public void configureAdaptorContext(NSNotification n) {
                    ERXExtensions.configureAdaptorContext();
                }
            };
            ERXRetainer.retain(observer); // has to be retained on the objC side!!
                                          // Allows rapid turn-around of adaptor debugging.
            NSNotificationCenter.defaultCenter().addObserver(observer,
                                                             new NSSelector("configureAdaptorContext", ERXConstant.NotificationClassArray),
                                                             ERXLog4j.ConfigurationDidChangeNotification,
                                                             null);
            _isConfigureAdaptorContextRapidTurnAround = true;
        }
    }

    public static void configureAdaptorContext() {
        Boolean targetState = null;
        if (adaptorCategory.isDebugEnabled() && !adaptorEnabled.booleanValue()) {
            targetState = Boolean.TRUE;
        } else if (!adaptorCategory.isDebugEnabled() && adaptorEnabled.booleanValue()) {
            targetState = Boolean.FALSE;
        }
        if (targetState != null) {
            EOEditingContext ec = newEditingContext();
            // We set the default, so all future adaptor contexts are either enabled or disabled.
            if (NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupSQLGeneration) != targetState.booleanValue())
                if (targetState.booleanValue()) {
                    NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration);
                } else {
                    NSLog.refuseDebugLoggingForGroups(NSLog.DebugGroupSQLGeneration);
                }
                    ec.hasChanges();
            if (targetState.booleanValue()) {
                adaptorCategory.info("Adaptor debug on");
            } else {
                adaptorCategory.info("Adaptor debug off");
            }
            adaptorEnabled = targetState;
        }
    }

    // Delegate methods
    public static ERXEditingContextDelegate defaultEditingContextDelegate() { return _defaultEditingContextDelegate; }
    public static ERXECNoValidationDelegate defaultECNoValidationDelegate() { return _defaultECNoValidationDelegate; }

    public final static String objectsWillChangeInEditingContext= "ObjectsWillChangeInEditingContext";

    // EditingContext creation methods
    public static EOEditingContext newEditingContext() {
        return ERXExtensions.newEditingContext(EOEditingContext.defaultParentObjectStore(), true);
    }
    public static EOEditingContext newEditingContext(boolean validation) {
        return ERXExtensions.newEditingContext(EOEditingContext.defaultParentObjectStore(), validation);
    }
    public static EOEditingContext newEditingContext(EOObjectStore objectStore) {
        return ERXExtensions.newEditingContext(objectStore, true);
    }

    public static void evaluateSQLWithEntityNamed(String entityName, String exp, EOEditingContext ec) {
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        EODatabaseContext dbContext = EODatabaseContext.registeredDatabaseContextForModel(entity.model(), ec);
        EOAdaptorChannel adaptorChannel = dbContext.availableChannel().adaptorChannel();
        EOSQLExpressionFactory factory=EOAdaptor.adaptorWithModel(entity.model()).expressionFactory();
        adaptorChannel.evaluateExpression(factory.expressionForString(exp));
    }

    // Retaining the editing contexts explicitly until the session that was active
    // when they were created goes away
    // this hopefully will go some way towards avoiding the 'attempted to send
    // message to EO whose EditingContext is gone
    private static NSMutableDictionary _editingContextsPerSession=new NSMutableDictionary();
    public static EOEditingContext newEditingContext(EOObjectStore objectStore, boolean validation) {
        EOEditingContext ec = new EOEditingContext(objectStore);
        ERXExtensions.setDefaultDelegate(ec, validation);
        WOSession s=session();
        if (s!=null) {
            NSMutableArray a=(NSMutableArray)_editingContextsPerSession.objectForKey(s.sessionID());
            if (a==null) {
                a=new NSMutableArray();
                _editingContextsPerSession.setObjectForKey(a,s.sessionID());
                if (cat().isDebugEnabled()) cat().debug("Creating array for "+s.sessionID());
            }
            a.addObject(ec);
            if (cat().isDebugEnabled()) cat().debug("Added new ec to array for "+s.sessionID());
        } else if (cat().isDebugEnabled()) {
            cat().debug("Editing Context created with null session.");
        }
        return ec;
    }

    public static void sessionDidTimeOut(String sessionID) {
        if (sessionID!=null) {
            if (cat().isDebugEnabled()) {
                NSArray a=(NSArray)_editingContextsPerSession.objectForKey(sessionID);
                cat().debug("Session "+sessionID+" is timing out ");
                cat().debug("Found "+ ((a == null) ? 0 : a.count()) + " editing context(s)");
            }
            _editingContextsPerSession.removeObjectForKey(sessionID);
        }
    }

    public static void setDefaultDelegate(EOEditingContext ec) { ERXExtensions.setDefaultDelegate(ec, true); }
    public static void setDefaultDelegate(EOEditingContext ec, boolean validation) {
        if (ec != null) {
            if (validation)
                ec.setDelegate(ERXExtensions.defaultEditingContextDelegate());
            else
                ec.setDelegate(ERXExtensions.defaultECNoValidationDelegate());
        }
    }

    public static EOArrayDataSource dataSourceForArray(NSArray array) {
        EOArrayDataSource dataSource = null;
        if (array != null && array.count() > 0) {
            EOEnterpriseObject eo = (EOEnterpriseObject)array.objectAtIndex(0);
            dataSource = new EOArrayDataSource(eo.classDescription(), eo.editingContext());
            dataSource.setArray(array);
        }
        return dataSource;
    }

    public static NSArray arrayFromDataSource(EODataSource dataSource) {
        WODisplayGroup dg = new WODisplayGroup();
        dg.setDataSource(dataSource);
        dg.fetch(); // Have to fetch in the array, go figure.
        return dg.allObjects();
    }

    public static EODetailDataSource dataSourceForObjectAndKey(EOEnterpriseObject object, String key) {
        EODetailDataSource eodetaildatasource = new EODetailDataSource(EOClassDescription.classDescriptionForEntityName(object.entityName()), key);
        eodetaildatasource.qualifyWithRelationshipKey(key, object);
        return eodetaildatasource;
    }

    public static String friendlyEOArrayDisplayForKey(NSArray list, String attribute, String nullArrayDisplay) {
        String result=null;
        int count = list!=null ? list.count() : 0;
        if (count==0) {
            result=nullArrayDisplay;
        } else if (count == 1) {
            result= (String) (attribute!= null ? ((EOEnterpriseObject)list.objectAtIndex(0)).valueForKeyPath(attribute) :
                              ((EOEnterpriseObject)list.objectAtIndex(0)).toString());
        } else if (count > 1) {
            StringBuffer buffer = new StringBuffer();
            for(int i = 0; i < count; i++) {
                String attributeValue =  (attribute!= null ? (String) ((EOEnterpriseObject)list.objectAtIndex(i)).valueForKeyPath(attribute) :
                                          ((EOEnterpriseObject)list.objectAtIndex(i)).toString());
                if (i == 0) {
                    buffer.append(attributeValue);
                } else if  (i == (count - 1)) {
                    buffer.append(" and " + attributeValue);
                } else {
                    buffer.append(", " + attributeValue);
                }
            }
            result=buffer.toString();
        }
        return result;
    }


    public static String replaceStringByStringInString(String old, String newString, String s) {
        return ERXSimpleHTMLFormatter.replaceStringByStringInString(old,newString,s);
    }

    public static String emptyStringForNull(String s) {
        return s==null ? "" : s;
    }

    public static String emptyStringForNull(Object o) {
        return o==null ? ""  : emptyStringForNull(o.toString());
    }

    public static String nullForEmptyString(String s) {
        return s==null ? null : (s.length()==0 ? null : s);
    }

    public static String nullForEmptyString(Object o) {
        return o==null ? null : nullForEmptyString(o.toString());
    }

    public static boolean stringIsNullOrEmpty(String s) {
        return ((s == null) || (s.length() == 0));
    }

    public static int numberOfOccurrencesOfCharInString(char c, String s) {
        int result=0;
        if (s!=null) {
            for (int i=0; i<s.length();)
                if (s.charAt(i++)==c) result++;
        }
        return result;
    }

    public static String stringWithNtimesString(int n, String s) {
        StringBuffer sb=new StringBuffer();
        for (int i=0; i<n; i++) sb.append(s);
        return sb.toString();
    }

    public static String removeHTMLTagsFromString(String s) {
        // FIXME this is so simplistic it will break if you sneeze
        StringBuffer result=new StringBuffer();
        if (s.length()>0) {
            int position=0;
            while (position<s.length()) {
                int indexOfOpeningTag=s.indexOf("<",position);
                if (indexOfOpeningTag!=-1) {
                    if (indexOfOpeningTag!=position)
                        result.append(s.substring(position, indexOfOpeningTag));
                    position=indexOfOpeningTag+1;
                } else {
                    result.append(s.substring(position, s.length()));
                    position=s.length();
                }
                int indexOfClosingTag=s.indexOf(">",position);
                if (indexOfClosingTag!=-1) {
                    position= indexOfClosingTag +1;
                } else {
                    result.append(s.substring(position, s.length()));
                    position=s.length();
                }
            }
        }
        return replaceStringByStringInString("&nbsp;"," ",result.toString());
    }

    //---------------------------------------------------------------------------

    /* not needed anymore
        public static void adjustAllModels() {
            // called from ERBusinessLogic
            for (Enumeration e=EOModelGroup.defaultGroup().models().objectEnumerator(); e.hasMoreElements();) {
                adjustModel((EOModel)e.nextElement());
            }
            for (Enumeration e=EOModelGroup.globalModelGroup().models().objectEnumerator(); e.hasMoreElements();) {
                adjustModel((EOModel)e.nextElement());
            }
        }
    */

    private static Object _delegate;
    public static void adjustModel(EOModel model) {
        ERXConfigurationManager.defaultManager().resetConnectionDictionaryInModel(model);

        // Forcing reconnect on ORA-01041, which is what we get every day when we take the DB offline
        // to back it up. I am sure there's a better way to backup the DB, but this'll do for now.
        if (model.adaptorName().indexOf("Oracle")!=-1) {
            EODatabaseContext dbc=EOUtilities.databaseContextForModelNamed(newEditingContext(), model.name());
            if (_delegate==null) {
                _delegate=(new Object() {
                    public boolean databaseContextShouldHandleDatabaseException(EODatabaseContext dbc2, Exception e) throws Throwable
                {
                    EOAdaptor adaptor=dbc2.adaptorContext().adaptor();
                    if (adaptor.isDroppedConnectionException(e))
                        return true;
                    else if (e.toString().indexOf("ORA-01041")!=-1) {
                        // just returning true here does not seem to do the trick. why !?!?
                        WOApplication.application().logString("ORA-01041 detecting -- forcing reconnect");
                        dbc2.database().handleDroppedConnection();
                        return false;
                    } else if (e.toString().indexOf("EOGeneralAdaptorException")!=-1 &&
                               e.toString().indexOf("method failed to update row in database")!=-1) {
                        // on optimistic locking failures -- commit suicide
                        WOApplication.application().refuseNewSessions(true);
                        return false;
                    } else
                        throw e;
                }

                    /*
                     public void databaseContextWillFireObjectFaultForGlobalID(EODatabaseContext context,
                                                                               EOGlobalID gid,
                                                                               EOFetchSpecification fs,
                                                                               EOEditingContext ec) {}



                     public void databaseContextWillFireArrayFaultForGlobalID (EODatabaseContext dbc2,
                                                                               EOGlobalID gid,
                                                                               EORelationship r,
                                                                               EOFetchSpecification fs,
                                                                               EOEditingContext ec) {
                         NSArray prefetchKeyPaths=(NSArray)(r.userInfo()!=null ? r.userInfo().objectForKey("prefetchingRelationshipKeyPaths") : null);
                         if (prefetchKeyPaths!=null) {
                             fs.setPrefetchingRelationshipKeyPaths(prefetchKeyPaths);
                         }
                     }
                     */

                });
                ERXRetainer.retain(_delegate); // delegate HAVE to be retained from the objC side
            }
            // to avoid msg sent to freed objects problems
            dbc.setDelegate(_delegate);
        }
    }

    //--------------------------------------------------------------------------------------

    public static void forceGC(int maxLoop) {
        if (cat().isDebugEnabled()) cat().debug("Forcing full Garbage Collection");
        Runtime runtime = Runtime.getRuntime();
        long isFree = runtime.freeMemory();
        long wasFree;
        int i=0;
        do {
            wasFree = isFree;
            runtime.gc();
            isFree = runtime.freeMemory();
            i++;
        } while (isFree > wasFree && (maxLoop<=0 || i<maxLoop) );
        runtime.runFinalization();
    }
    //--------------------------------------------------------------------------------------
    // Convenience Methods

    public static boolean isNewObject(EOEnterpriseObject eo) {
        return eo.editingContext()==null || eo.editingContext().insertedObjects().containsObject(eo);
    }

    public static String primaryKeyForObject(EOEnterpriseObject eo) {
        Object pk=rawPrimaryKeyForObject(eo);
        return pk!=null ? pk.toString() : null;
    }

    public static Object rawPrimaryKeyFromPrimaryKeyAndEO(NSDictionary primaryKey, EOEnterpriseObject eo) {
        Object result = null;
        if(primaryKey!=null && primaryKey.allValues().count() == 1)
            result=primaryKey.allValues().lastObject();
        else if (primaryKey!=null && primaryKey.allValues().count() > 1)
            cat().warn("Attempting to get a raw primary key from an object with a compound key: " + eo);
        return result;
    }

    public static Object rawPrimaryKeyForObject(EOEnterpriseObject eo) {
        Object result = null;
        if (eo!=null)  {
            NSDictionary d=EOUtilities.primaryKeyForObject(eo.editingContext(),eo);
            result = rawPrimaryKeyFromPrimaryKeyAndEO(d, eo);
        }
        return result;
    }

    public static String capitalize(String s) {
        String result=s;
        if (s!=null && s.length()>0) {
            char c=s.charAt(0);
            if (Character.isLowerCase(c))
                result=Character.toUpperCase(c)+s.substring(1);
        }
        return result;
    }

    public static String plurify(String s, int howMany) {
        return ERXPluralString.plurify(s, howMany);
    }

    public static boolean safeEquals(Object v1, Object v2) {
        return v1==v2 || (v1!=null && v2!=null && v1.equals(v2));
    }

    public static boolean safeDifferent(Object v1, Object v2) {
        return
        v1==v2 ? false :
        v1==null && v2!=null ||
        v1!=null && v2==null ||
        !v1.equals(v2);
    }

    public static boolean stringIsParseableInteger(String s) {
        try {
            int x = Integer.parseInt(s);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    public static int intFromParseableIntegerString(String s) {
        try {
            int x = Integer.parseInt(s);
            return x;
        } catch (NumberFormatException e) {
            return 0;
        }
    }
    public static String substituteStringByStringInString(String s1, String s2, String s) {
        NSArray a=NSArray.componentsSeparatedByString(s,s1);
        return a!=null ? a.componentsJoinedByString(s2) : s;
    }

    // FIXME:  Depricated use singleton method instead.
    public static ERXSimpleHTMLFormatter htmlFormatter() { return ERXSimpleHTMLFormatter.formatter(); }

    public static void addObjectToBothSidesOfPotentialRelationshipFromObjectWithKeyPath(EOEnterpriseObject to,
                                                                                        EOEnterpriseObject from,
                                                                                        String keyPath) {

        /*
         Handles 3 different cases

         1. keyPath is a single key and represents a relationship --> addObjectToBothSidesOfRelationshipWithKey
         2. keyPath is a single key and does NOT represents a relationship
         3. keyPath is a real key path: break it up, navigate to the last atom --> back to 1. or 2.

         */

        if (from!=null) {
            if (keyPath.indexOf('.')!=-1) { // we have a key path
                String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(keyPath);
                from=(EOEnterpriseObject)from.valueForKeyPath(partialKeyPath);
                keyPath=KeyValuePath.lastPropertyKeyInKeyPath(keyPath);
            }
            //if the key is not a keyPath we can check if the key is actually a relationship
            EOEntity e=EOModelGroup.defaultGroup().entityNamed(from.entityName());
            EORelationship r=e.relationshipNamed(keyPath);
            if (r!=null) //if the key correspond to a relationship
                from.addObjectToBothSidesOfRelationshipWithKey(to, keyPath);
            else
                from.takeValueForKeyPath(to,keyPath);
        }
    }

    public static NSArray flatten(NSArray array) {
        NSMutableArray newArray=null;
        for (int i=0; i<array.count(); i++) {
            Object element=array.objectAtIndex(i);
            if (element instanceof NSArray) {
                if (newArray==null) {
                    newArray=new NSMutableArray();
                    for (int j=0; j<i; j++) {
                        if(array.objectAtIndex(j)!=null){
                            newArray.addObject(array.objectAtIndex(j));
                        }
                    }
                }
                NSArray a=flatten((NSArray)element);
                for (int j=0; j<a.count();j++) {
                    if(a.objectAtIndex(j)!=null){
                        newArray.addObject(a.objectAtIndex(j));
                    }
                }
            }
        }
        return (newArray !=null) ? newArray : array;
    }

    public static final String NULL_GROUPING_KEY="**** NULL GROUPING KEY ****";
    public static NSDictionary eosInArrayGroupedByKeyPath(NSArray eos, String keyPath) {
        return eosInArrayGroupedByKeyPath(eos,keyPath,true,null);
    }
    public static NSDictionary eosInArrayGroupedByKeyPath(NSArray eos, String keyPath, boolean includeNulls, String extraKeyPathForValues) {
        NSMutableDictionary result=new NSMutableDictionary();
        for (Enumeration e=eos.objectEnumerator(); e.hasMoreElements();) {
            EOEnterpriseObject eo=(EOEnterpriseObject)e.nextElement();
            Object key=eo.valueForKeyPath(keyPath);
            boolean isNullKey = key==null || key instanceof NSKeyValueCoding.Null;
            if (!isNullKey || includeNulls) {
                if (isNullKey) key=NULL_GROUPING_KEY;
                NSMutableArray existingGroup=(NSMutableArray)result.objectForKey(key);
                if (existingGroup==null) {
                    existingGroup=new NSMutableArray();
                    result.setObjectForKey(existingGroup,key);
                }
                if (extraKeyPathForValues!=null) {
                    Object value=((EOEnterpriseObject)eo).valueForKeyPath(extraKeyPathForValues);
                    if (value!=null) existingGroup.addObject(value);
                } else
                    existingGroup.addObject(eo);
            }
        }
        return result;
    }

    public static boolean arraysAreIdenticalSets(NSArray a1, NSArray a2) {
        boolean result=false;
        for (Enumeration e=a1.objectEnumerator();e.hasMoreElements();) {
            Object i=e.nextElement();
            if (!a2.containsObject(i)) {
                result=false; break;
            }
        }
        if (result) {
            for (Enumeration e=a2.objectEnumerator();e.hasMoreElements();) {
                Object i=e.nextElement();
                if (!a1.containsObject(i)) {
                    result=false; break;
                }
            }
        }
        return result;
    }

    public static NSArray filteredArrayWithQualifierEvaluation(NSArray a, EOQualifierEvaluation q) {
        NSMutableArray result=null;
        if (a!=null) {
            result=new NSMutableArray();
            for (Enumeration e=a.objectEnumerator(); e.hasMoreElements();) {
                Object o=e.nextElement();
                if (q.evaluateWithObject(o)) result.addObject(o);
            }
        }
        return result;
    }

    public static NSDictionary dictionaryByRemovingFromDictionaryKeysInArray(NSDictionary d, NSArray a) {
        NSMutableDictionary result=new NSMutableDictionary();
        if (d!=null && a!=null) {
            for (Enumeration e=d.allKeys().objectEnumerator();e.hasMoreElements();) {
                String key=(String)e.nextElement();
                if (!a.containsObject(key))
                    result.setObjectForKey(d.objectForKey(key),key);
            }
        }
        return result;
    }


    private static final char hex[] = {
        '0', '1', '2', '3', '4', '5', '6', '7',
        '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'
    };

    public static String byteArrayToHexString (final byte data[]) {
        int len = data.length;
        char hexchars[] = new char[2 * len];

        int ix = 0;
        for (int i = 0; i < len; i++) {
            hexchars[ix++] = hex[(data[i] >> 4) & 0xf];
            hexchars[ix++] = hex[data[i] & 0xf];
        }
        return new String(hexchars);
    }

    public static byte[] bytesFromFile(File f) throws IOException {
        if (f == null)
            throw new IOException("null file");
        int size = (int) f.length();
        FileInputStream fis = new FileInputStream(f);
        byte[] data = new byte[size];
        int bytesRead = 0;
        while (bytesRead < size)
            bytesRead += fis.read(data, bytesRead, size - bytesRead);
        fis.close();
        return data;
    }

    public static String stringFromFile(File f) throws IOException {
        return new String(bytesFromFile(f));
    }

    public static long lastModifiedDateForFileInFramework(String fileName, String frameworkName) {
        long lastModified = 0;
        String filePath = WOApplication.application().resourceManager().pathForResourceNamed(fileName,
                                                                                             frameworkName,
                                                                                             null);
        if (filePath!=null) {
            lastModified = new File(filePath).lastModified();
        }
        return lastModified;
    }

    public static Object readPropertyListFromFileinFramework(String fileName, String aFrameWorkName) {
        return readPropertyListFromFileInFramework(fileName, aFrameWorkName, null);
    }

    public static Object readPropertyListFromFileInFramework(String fileName, String aFrameWorkName, NSArray languageList) {
        String filePath = WOApplication.application().resourceManager().pathForResourceNamed(fileName,
                                                                                             aFrameWorkName,
                                                                                             languageList);
        Object result=null;
        if (filePath!=null) {
            File file = new File(filePath);
            try {
                result=NSPropertyListSerialization.propertyListFromString(stringFromFile(file));
            } catch (IOException ioe) {
                cat().error("ConfigurationManager: Error reading "+filePath);
            }
        }
        return result;
    }

    public static String displayNameForPropertyKey(String key, String entityName) {
        D2WContext context = ((ERXApplication)WOApplication.application()).d2wContext();
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        context.setEntity(entity);
        // grosse but efficient hack -- force the computation of pageConfiguration
        // so that caching works correctly -- saves us from having to add entity as a significant key
        // Not needed anymore.
        //context.valueForKey("pageConfiguration");
        context.setPropertyKey(key);
        return context.displayNameForProperty();
    }

    // For this we check two parameters, first if the value is in the user defaults, if not we ask the rule system to resolve.
    public static Object configurationForKey(String key) {
        return System.getProperty(key) != null ?
        System.getProperty(key) : ERXExtensions.d2wContextValueForKey(key);
    }

    public static Object d2wContextValueForKey(String key) {
        return key != null ? ((ERXApplication)WOApplication.application()).d2wContext().valueForKey(key) : null;
    }

    public static Object d2wContextValueForKey(String key, String entityName) {
        D2WContext context = ((ERXApplication)WOApplication.application()).d2wContext();
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        context.setEntity(entity);
        // grosse but efficient hack -- force the computation of pageConfiguration
        // so that caching works correctly -- saves us from having to add entity as a significant key
        //System.out.println("pageConfig="+context.valueForKey("pageConfiguration"));
        return context.valueForKey(key);
    }

    public static String createConfigurationForEntityNamed(String entityName) {
        D2WContext context = ((ERXApplication)WOApplication.application()).d2wContext();
        EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
        context.setEntity(entity);
        // grosse but efficient hack -- force the computation of pageConfiguration
        // so that caching works correctly -- saves us from having to add entity as a significant key
        context.valueForKey("pageConfiguration");
        return (String)context.valueForKey("createConfigurationNameForEntity");
    }


    public static String userInfoUnit(EOEnterpriseObject object, String key) {
        // return the unit stored in the userInfo dictionary of the appropriate EOAttribute
        EOEntity entity=null;
        String lastKey=null;
        String result=null;
        if (key.indexOf(".")==-1) {
            String entityName=object.entityName();
            entity=EOModelGroup.defaultGroup().entityNamed(entityName);
            lastKey=key;
        } else {
            String partialKeyPath=KeyValuePath.keyPathWithoutLastProperty(key);
            EOEnterpriseObject objectForPropertyDisplayed=(EOEnterpriseObject)object.valueForKeyPath(partialKeyPath);
            if (objectForPropertyDisplayed!=null) {
                entity=EOModelGroup.defaultGroup().entityNamed(objectForPropertyDisplayed.entityName());
                lastKey=KeyValuePath.lastPropertyKeyInKeyPath(key);
            }
        }
        if (entity!=null && lastKey!=null) {
            EOAttribute a=entity.attributeNamed(lastKey);
            NSDictionary userInfo=null;
            if (a!=null) userInfo=a.userInfo();
            else {
                EORelationship r=entity.relationshipNamed(lastKey);
                if (r!=null) userInfo=r.userInfo();
            }
            result= (String)(userInfo!=null ? userInfo.valueForKey("unit") : null);
        }
        return result;
    }


    public static String resolveUnit(String userInfoUnitString,
                                     EOEnterpriseObject object,
                                     String prefixKeyPath) {
        // some of our units (stored in the user info take the form of @project.unit
        // this method resolves the @keyPath..
        if(userInfoUnitString!=null && userInfoUnitString.indexOf("@")>-1){
            String keyPath = userInfoUnitString.substring(1);
            String PropertyKeyWithoutLastProperty = KeyValuePath.keyPathWithoutLastProperty(prefixKeyPath);
            EOEnterpriseObject objectForPropertyDisplayed = null;
            if(PropertyKeyWithoutLastProperty!=null){
                objectForPropertyDisplayed = object!=null ? (EOEnterpriseObject)object.valueForKeyPath(PropertyKeyWithoutLastProperty) : null;
            }else{
                objectForPropertyDisplayed = object;
            }
            userInfoUnitString = objectForPropertyDisplayed!=null ? (String)objectForPropertyDisplayed.valueForKeyPath(keyPath) : null;
        }
        return userInfoUnitString;
    }

    public static NSArray arrayWithoutDuplicates(NSArray anArray) {
        NSMutableSet aSet = new NSMutableSet();
        aSet.addObjectsFromArray(anArray);
        return aSet.allObjects();
    }

    public static NSArray arrayWithoutDuplicateKeyValue(NSArray eos, String key){
        NSMutableDictionary dico = new NSMutableDictionary();
        for(Enumeration e = eos.objectEnumerator(); e.hasMoreElements(); ){
            EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
            Object value = eo.valueForKey(key);
            if(value != null){
                dico.setObjectForKey(eo, value);
            }
        }
        return dico.allValues();
    }

    public static NSArray arrayMinusArray(NSArray main, NSArray minus){
        NSSet result = ERXUtilities.setFromArray(main);
        return result.setBySubtractingSet(ERXUtilities.setFromArray(minus)).allObjects();
    }

    public static NSArray arrayByAddingObjectsFromArrayWithoutDuplicates(NSArray a1, NSArray a2) {
        // FIXME this is n2 -- could be made n lg n
        NSArray result=null;
        if (a2.count()==0)
            result=a1;
        else {
            NSMutableArray mutableResult=new NSMutableArray(a1);
            for (Enumeration e=a2.objectEnumerator(); e.hasMoreElements();) {
                Object elt=e.nextElement();
                if (!mutableResult.containsObject(elt)) mutableResult.addObject(elt);
            }
            result=mutableResult;
        }
        return result;
    }
    public static void addObjectsFromArrayWithoutDuplicates(NSMutableArray a1, NSArray a2) {
        for (Enumeration e=a2.objectEnumerator(); e.hasMoreElements();) {
            Object elt=e.nextElement();
            if (!a1.containsObject(elt)) a1.addObject(elt);
        }
    }


    public static String userPresentableEOArray(NSArray array, String attribute) {
        String userPresentable = "";
        if (array != null && array.count() > 0) {
            if (attribute == null)
                attribute = "description";
            if (array.count() > 1) {
                NSArray subArray = array.subarrayWithRange(new NSRange(0, array.count() - 1));
                userPresentable = ((NSArray)subArray.valueForKey(attribute)).componentsJoinedByString(", ") + " and ";
            }
            userPresentable += ((NSKeyValueCoding)array.lastObject()).valueForKey(attribute);
        }
        return userPresentable;
    }

    public static void refreshSharedObjectsWithNames(NSArray names) {
        for (Enumeration e = names.objectEnumerator(); e.hasMoreElements();)
            refreshSharedObjectsWithName((String)e.nextElement());
    }

    public static void refreshSharedObjectsWithName(String entityName) {
        EOEditingContext peer = ERXExtensions.newEditingContext();
        peer.setSharedEditingContext(null);
        EOFetchSpecification fetchAll = EOModelGroup.defaultGroup().entityNamed(entityName).fetchSpecificationNamed("FetchAll");
        if (fetchAll != null) {
            // Need to refault all the shared EOs first.
            for (Enumeration e = EOUtilities.objectsForEntityNamed(peer, entityName).objectEnumerator(); e.hasMoreElements();) {
                EOEnterpriseObject eo = (EOEnterpriseObject)e.nextElement();
                peer.rootObjectStore().refaultObject(eo, peer.globalIDForObject(eo), peer);
            }
            fetchAll.setRefreshesRefetchedObjects(true);
            peer.objectsWithFetchSpecification(fetchAll);
        } else {
            cat().warn("Attempting to refresh a non-shared EO: " + entityName);
        }
    }

    private static Random _random=new Random();
    public static String randomizeDirectActionURL(String daURL) {
        // this method can be used with Direct Action URLs, to make sure the will cause the browser
        // to reload the page..
        int r=_random.nextInt();
        char c=daURL.indexOf('?')==-1 ? '?' : '&';
        return  daURL+c+"r="+r;
    }
    public static void addRandomizeDirectActionURL(StringBuffer daURL) {
        // this method can be used with Direct Action URLs, to make sure the will cause the browser
        // to reload the page..
        int r=_random.nextInt();
        char c='?';
        for (int i=0; i<daURL.length(); i++) {
            if (daURL.charAt(i)=='?') {
                c='&'; break;
            }
        }
        daURL.append(c);
        daURL.append("r=");
        daURL.append(r);
    }
    public static String addWosidFormValue(String url, WOSession s) {
        String result= url;
        if (result!=null && s!=null) {
            result += ( result.indexOf("?") == -1 ? "?" : "&" ) + "wosid=" + s.sessionID();
        } else {
            cat().warn("not adding sid: url="+url+" session="+s);
        }
        return result;
    }
    // -----------------------------------------------------------------------------------------------
    // Rule cache warming up -- useful for deployment

    public static void warmUpRuleCache() {
        if (WOApplication.application() instanceof ERXApplication) {
            ((ERXApplication)WOApplication.application()).resetSignificantKeys();
            ((ERXApplication)WOApplication.application()).warmUpRuleCache();
        }
    }
    // ----------------------------------------------------------------------------------------
    // fuzzy match

    public static String cleanString(String newString, NSArray toBeCleaneds) {
        String result=newString;
        if (newString!=null) {
            for(Enumeration e = toBeCleaneds.objectEnumerator(); e.hasMoreElements();){
                String toBeCleaned = (String)e.nextElement();
                if(newString.toUpperCase().indexOf(toBeCleaned)>-1){
                    result=newString.substring(0, newString.toUpperCase().indexOf(toBeCleaned));
                }
            }
        }
        return result;
    }


    protected static double adjustement = 0.5;
    public static void setAdjustement(double newAdjustement){
        adjustement = newAdjustement;
    }
    public static NSArray fuzzyMatch(String name,
                                     String entityName,
                                     String propertyKey, String synonymsKey,
                                     EOEditingContext ec,
                                     ERXFuzzyMatchCleaner cleaner,
                                     String comparisonString){
        NSMutableArray results = new NSMutableArray();
        NSArray rawRows = EOUtilities.rawRowsMatchingValues( ec, entityName, null);
        if(name == null)
            name = "";
        name = name.toUpperCase();
        String cleanedName = cleaner.cleanStringForFuzzyMatching(name);
        for(Enumeration e = rawRows.objectEnumerator(); e.hasMoreElements(); ){
            NSDictionary dico = (NSDictionary)e.nextElement();
            Object value = dico.valueForKey(propertyKey);
            boolean trySynonyms = true;
            //First try to match with the name of the eo
            if( value!=null && value instanceof String){
                String comparedString = ((String)value).toUpperCase();
                String cleanedComparedString = cleaner.cleanStringForFuzzyMatching(comparedString);
                if( (distance(name, comparedString) <=
                     Math.min((double)name.length(), (double)comparedString.length())*adjustement ) ||
                    (distance(cleanedName, cleanedComparedString) <=
                     Math.min((double)cleanedName.length(), (double)cleanedComparedString.length())*adjustement)){
                    ERXGenericRecord object = (ERXGenericRecord)EOUtilities.objectFromRawRow( ec, entityName, dico);
                    object.takeValueForKey(new Double(distance(name, comparedString)), "distance");
                    results.addObject(object);
                    trySynonyms = false;
                }
            }
            //Then try to match using the synonyms vector
            if(trySynonyms && synonymsKey != null){
                Object synonymsString = dico.valueForKey(synonymsKey);
                if(synonymsString != null && synonymsString instanceof String){
                    Object plist  = NSPropertyListSerialization.propertyListFromString((String)synonymsString);
                    Vector v = (Vector)plist;
                    for(int i = 0; i< v.size(); i++){
                        String comparedString = ((String)v.elementAt(i)).toUpperCase();
                        if((distance(name, comparedString) <=
                            Math.min((double)name.length(), (double)comparedString.length())*adjustement) ||
                           (distance(cleanedName, comparedString) <=
                            Math.min((double)cleanedName.length(), (double)comparedString.length())*adjustement)){
                            ERXGenericRecord object = (ERXGenericRecord)EOUtilities.objectFromRawRow( ec, entityName, dico);
                            object.takeValueForKey(new Double(distance(name, comparedString)), "distance");
                            results.addObject(object);
                            break;
                        }
                    }
                }

            }
        }
        if(comparisonString != null){
            NSArray sortOrderings = new NSArray();
            if(comparisonString.equals("asc")){
                sortOrderings = new NSArray(new Object [] { new EOSortOrdering("distance",
                                                                               EOSortOrdering.CompareAscending) });
            }else if(comparisonString.equals("desc")){
                sortOrderings = new NSArray(new Object [] { new EOSortOrdering("distance",
                                                                               EOSortOrdering.CompareDescending) });
            }
            results = (NSMutableArray)EOSortOrdering.sortedArrayUsingKeyOrderArray((NSArray)results, sortOrderings);
        }
        return results;
    }

    //The code below comes from the following post on http://mail.python.org
    /*Fuzzy string matching
        Magnus L. Hetland mlh@idt.ntnu.no
        27 Aug 1999 15:51:03 +0200

        Previous message: Why do we call python a scripting language?
        Next message: Fuzzy string matching (snippet candidate?)
        Messages sorted by: [ date ] [ thread ] [ subject ] [ author ]

        --------------------------------------------------------------------------------

        Explanation of the distance algorithm...

        (Got a request for this, and thought I might as well post it...)

        The algorithm:

        def distance(a,b):
        c = {}
    n = len(a); m = len(b)

        for i in range(0,n+1):
        c[i,0] = i
        for j in range(0,m+1):
        c[0,j] = j

        for i in range(1,n+1):
        for j in range(1,m+1):
        x = c[i-1,j]+1
        y = c[i,j-1]+1
        if a[i-1] == b[j-1]:
        z = c[i-1,j-1]
else:
        z = c[i-1,j-1]+1
        c[i,j] = min(x,y,z)
        return c[n,m]

        It calculates the following: Given two strings, a and b, and three
        operations, adding, subtracting and exchanging single characters, what
        is the minimal number of steps needed to translate a into b?

        The method is based on the following idea:

        We want to find the distance between a[:x] and b[:y]. To do this, we
        first calculate

        1) the distance between a[:x-1] and b[:y], adding the cost of a
subtract-operation, used to get from a[:x] to a[:z-1];

2) the distance between a[:x] and b[:y-1], adding the cost of an
addition-operation, used to get from b[:y-1] to b[:y];

3) the distance between a[:x-1] and b[:y-1], adding the cost of a
*possible* exchange of the letter b[y] (with a[x]).

The cost of the subtraction and addition operations are 1, while the
exchange operation has a cost of 1 if a[x] and b[y] are different, and
0 otherwise.

After calculating these costs, we choose the least one of them (since
                                                                we want to use the best solution.)

Instead of doing this recursively (i.e. calculating ourselves "back"
                                   from the final value), we build a cost-matrix c containing the optimal
costs, so we can reuse them when calculating the later values. The
costs c[i,0] (from string of length n to empty string) are all i, and
correspondingly all c[0,j] (from empty string to string of length j)
are j.

Finally, the cost of translating between the full strings a and b
(c[n,m]) is returned.

I guess that ought to cover it...

--

    Magnus              Making no sound / Yet smouldering with passion
    Lie          The firefly is still sadder / Than the moaning insect
          Hetland                                       : Minamoto Shigeyuki*/



    public static double distance( String a, String b){
        int n = a.length();
        int m = b.length();
        int c[][] = new int[n+1][m+1];
        for(int i = 0; i<=n; i++){
            c[i][0] = i;
        }
        for(int j = 0; j<=m; j++){
            c[0][j] = j;
        }
        for(int i = 1; i<=n; i++){
            for(int j = 1; j<=m; j++){
                int x = c[i-1][j] + 1;
                int y = c[i][j-1] + 1;
                int z = 0;
                if(a.charAt(i-1) == b.charAt(j-1))
                    z = c[i-1][j-1];
                else
                    z = c[i-1][j-1] + 1;
                int temp = Math.min(x,y);
                c[i][j] = Math.min(z, temp);
            }
        }
        return c[n][m];
    }

    public static void setBooleanFlagOnSessionForKey(WOSession s, String key, boolean newValue) {
        s.setObjectForKey(newValue ? Boolean.TRUE : Boolean.FALSE, key);
    }
    public static boolean booleanFlagOnSessionForKeyWithDefault(WOSession s, String key, boolean defaultValue) {
        //if (s.objectForKey(key) == null)
        //    s.setObjectForKey(defaultValue ? Boolean.TRUE : Boolean.FALSE, key);
        //return ((Boolean)s.objectForKey(key)).booleanValue();
        return s.objectForKey(key) != null ? ERXUtilities.booleanValue(s.objectForKey(key)) : defaultValue;
    }

    // this should be thread-safe
    // even though we don't intend to run with full MT on at this point
    private static NSMutableDictionary _sessionsPerThread=new NSMutableDictionary();
    public synchronized static void setSession(WOSession session) {
        Object key=Thread.currentThread().getName();
        if (session!=null)
            _sessionsPerThread.setObjectForKey(session,key);
        else
            _sessionsPerThread.removeObjectForKey(key);
    }

    public synchronized static WOSession session() {
        Object key=Thread.currentThread().getName();
        return (WOSession)_sessionsPerThread.objectForKey(key);
    }

    // -----------------------------------------------------------------------
    // method used by the preferences mechanism from ERDirectToWeb
    // needs to be in here because shared by ERDirectToWeb and ERCoreBusinessLogic

    public static String userPreferencesKeyFromContext(String key, NSKeyValueCoding context) {
        StringBuffer result=new StringBuffer(key);
        result.append('.');
        String pc=(String)context.valueForKey("pageConfiguration");
        if (pc==null || pc.length()==0) {
            String en="_All_";
            EOEntity e=(EOEntity)context.valueForKey("entity");
            if (e!=null) en=e.name();
            result.append("__");
            result.append(context.valueForKey("task"));
            result.append("_");
            result.append(en);
        } else {
            result.append(pc);
        }
        return result.toString();
    }


    //-------------------------------------------------------------------------

    public static void freeProcessResources(Process p) {
        if (p!=null) {
            try {
                if (p.getInputStream()!=null) p.getInputStream().close();
                if (p.getOutputStream()!=null) p.getOutputStream().close();
                if (p.getErrorStream()!=null) p.getErrorStream().close();
                p.destroy();
            } catch (IOException e) {}
        }
    }

    
}
