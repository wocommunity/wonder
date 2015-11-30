/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.URL;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOApplication;
import com.webobjects.directtoweb.Assignment;
import com.webobjects.directtoweb.D2WContext;
import com.webobjects.directtoweb.D2WFastModel;
import com.webobjects.directtoweb.D2WModel;
import com.webobjects.directtoweb.DefaultAssignment;
import com.webobjects.directtoweb.ERD2WUtilities;
import com.webobjects.directtoweb.Rule;
import com.webobjects.directtoweb.Services;
import com.webobjects.eoaccess.EOAttribute;
import com.webobjects.eoaccess.EOEntity;
import com.webobjects.eoaccess.EOModelGroup;
import com.webobjects.eoaccess.EORelationship;
import com.webobjects.eocontrol.EOAndQualifier;
import com.webobjects.eocontrol.EOKeyComparisonQualifier;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOKeyValueUnarchiver;
import com.webobjects.eocontrol.EONotQualifier;
import com.webobjects.eocontrol.EOOrQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol.EOQualifierEvaluation;
import com.webobjects.eocontrol.EOSortOrdering;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSBundle;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSLog;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSMutableSet;
import com.webobjects.foundation.NSNotification;
import com.webobjects.foundation.NSNotificationCenter;
import com.webobjects.foundation.NSSet;

import er.directtoweb.assignments.ERDComputingAssignmentInterface;
import er.directtoweb.assignments.ERDLocalizableAssignmentInterface;
import er.directtoweb.assignments.delayed.ERDDelayedAssignment;
import er.directtoweb.qualifiers.ERDQualifierTraversal;
import er.directtoweb.qualifiers.ERDQualifierTraversalCallback;
import er.extensions.ERXExtensions;
import er.extensions.foundation.ERXArrayUtilities;
import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXMappingObjectStream;
import er.extensions.foundation.ERXMultiKey;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXSelectorUtilities;
import er.extensions.localization.ERXLocalizer;

/**
 * Overhaul of the caching system.
 */
public class ERD2WModel extends D2WModel {

    /** logging support */
    public static final Logger log = Logger.getLogger(ERD2WModel.class);

    /** logs rules being decoded */
    public static final Logger ruleDecodeLog = Logger.getLogger("er.directtoweb.rules.decode");

    /** main category for enabling or disabling tracing of rules */
    public static final Logger ruleTraceEnabledLog = Logger.getLogger("er.directtoweb.rules.ERD2WTraceRuleFiringEnabled");

    //	===========================================================================
    //	Notification Title(s)
    //	---------------------------------------------------------------------------

    // Register for this notification to have the hook in place to load non-d2wmodel based rules
    public static final String WillSortRules = "WillSortRules";
    public static final String ModelWillReset = "ModelWillReset";

    /** null referenced used to represent null in the caching system */
    private final static Object NULL_VALUE="<NULL>";

    private Hashtable _cache=new Hashtable(10000);
    private Hashtable _systemCache=new Hashtable(10000);
    private Hashtable _significantKeysPerKey=new Hashtable(500);

    private static D2WModel _defaultModel;

    // put here the keys than can either provided as input or computed
    // FIXME should add API from clients to add to this array
    static NSMutableArray BACKSTOP_KEYS=new NSMutableArray(new Object[] { "pageConfiguration", "entity", "task" });
    static {
        Class c=D2WFastModel.class; // force initialization
        _defaultModel = new ERD2WModel(NSArray.EmptyArray);
        D2WModel.setDefaultModel(_defaultModel);
    }

    /**
     * Gets the default D2W model cast as an ERD2WModel.
     * @return the default ERD2Model
     */
    public static ERD2WModel erDefaultModel() {
        if(!(D2WModel.defaultModel() instanceof ERD2WModel)) {
            D2WModel.setDefaultModel(_defaultModel);
            log.warn("erDefaultModel had wrong class, fixing to ERD2WModel");
        }
        return (ERD2WModel)D2WModel.defaultModel();
    }

    private final static EOSortOrdering _prioritySortOrdering=new EOSortOrdering("priority",EOSortOrdering.CompareDescending);
    private final static EOSortOrdering _descriptionSortOrdering=new EOSortOrdering("toString",EOSortOrdering.CompareDescending);
    private static NSArray ruleSortOrderingKeyArray() {
        NSMutableArray result=new NSMutableArray();
        result.addObject(_prioritySortOrdering);
        result.addObject(_descriptionSortOrdering);

        return result;
    }

    /**
     * Main constructor. Builds a model for a given
     * set of rules.
     * @param rules array of rules
     */
    protected ERD2WModel(NSArray rules) {
    	super(rules);
    	NSNotificationCenter.defaultCenter().addObserver(this,
    			ERXSelectorUtilities.notificationSelector("applicationDidFinishLaunching"),
    			WOApplication.ApplicationDidFinishLaunchingNotification, null);
    }

    protected ERD2WModel(File file) {
        super(file);
    }
    public ERD2WModel(URL url) {
        super(url);
    }
    protected ERD2WModel(EOKeyValueUnarchiver unarchiver) {
        super(unarchiver);
    }
    /**/
    public void clearD2WRuleCache() {
        invalidateCaches();
        sortRules();
    }

    public NSArray availableTasks() {
    	return new NSArray(taskVector().toArray());
    }

    public NSArray availablePageConfigurations() {
    	return new NSArray(dynamicPages().toArray());
    }

    @Override
    protected void sortRules() {
        // This allows other non-d2wmodel file based rules to be loaded.
        // but we only post for the main model
        if(D2WModel.defaultModel() == this) {
          if(log.isDebugEnabled())
            log.debug("posting WillSortRules.");

            NSNotificationCenter.defaultCenter().postNotification(WillSortRules, this);
          
          if(log.isDebugEnabled())
            log.debug("posted WillSortRules.");
        }
        // We don't want dynamically loaded rules to cause rapid-turnaround to not work.
        setDirty(false);
        super.sortRules();
        if(log.isDebugEnabled())
        log.debug("called super sortRules.");
        /*
         the following sort call was to attempt to make assistant generated files more CVS compatible
         by preserving the rule order better. Commenting out since it's very memory hungry (calling description on every rule)
         and we are not using the Assistant
        EOSortOrdering.sortArrayUsingKeyOrderArray((NSMutableArray)rules(),
                                                   _ruleSortOrderingKeyVector);
        log.debug("Finished sorting.");
        */
        if (rules() !=null && rules().count() > 0) prepareDataStructures();
    }

    public void applicationWillDispatchRequest(NSNotification n) {
    	checkRules();
    }

    public void applicationDidFinishLaunching(NSNotification n) {
    	if(!WOApplication.application().isCachingEnabled()) {
    		NSNotificationCenter.defaultCenter().addObserver(this,
    				ERXSelectorUtilities.notificationSelector("applicationWillDispatchRequest"),
    				WOApplication.ApplicationWillDispatchRequestNotification, null);
        NSNotificationCenter.defaultCenter().addObserver(this,
            ERXSelectorUtilities.notificationSelector("clearD2WRuleCache"),
            "clearD2WRuleCache", null);
    	}
    }

    public void clearD2WRuleCache(NSNotification n) {
      clearD2WRuleCache();
    }

    @Override
    public NSArray rules() {
        return super.rules();
    }

    @Override
    public void addRule(Rule rule) {
        super.addRule(rule);
    }

    @Override
    public void removeRule(Rule rule) {
        super.removeRule(rule);
    }

    protected String descriptionForRuleSet(NSArray set) {
        StringBuilder buffer = new StringBuilder();
        for (Enumeration e = set.objectEnumerator(); e.hasMoreElements();) {
            buffer.append('\t');
            buffer.append(descriptionForRule((Rule)e.nextElement()));
            buffer.append('\n');
        }
        return buffer.toString();
    }

    protected String descriptionForRule(Rule r) {
        String suffix = null;
        if (_filePathRuleTraceCache != null) {
            suffix = (String)_filePathRuleTraceCache.get(r);
            if (suffix == null) suffix = "Dynamic";
        }
        return r.toString() + (suffix != null ? " From: " + suffix : "");
    }

    protected Hashtable _filePathRuleTraceCache;
    @Override
    public void addRules(NSArray rules) {
        super.addRules(rules);
        if (!WOApplication.application().isCachingEnabled() && currentFile() != null) {
            String path=currentFile().getAbsolutePath();
            NSArray components = NSArray.componentsSeparatedByString(path, "/");
            int count=components.count();
            String filePath = count > 2 ?
                (String)components.objectAtIndex(count-3)+"/"+ (String)components.objectAtIndex(count-2) :
                path;
            if (_filePathRuleTraceCache == null)
                _filePathRuleTraceCache = new Hashtable();
            for (Enumeration e = rules.objectEnumerator(); e.hasMoreElements();) {
                _filePathRuleTraceCache.put(e.nextElement(), filePath);
            }
        }
    }

    @Override
    protected Object fireSystemRuleForKeyPathInContext(String keyPath, D2WContext context) {
        return fireRuleForKeyPathInContext(_systemCache, keyPath,context);
    }

    @Override
    protected Object fireRuleForKeyPathInContext(String keyPath, D2WContext context) {
        return fireRuleForKeyPathInContext(_cache, keyPath, context);
    }

    protected boolean _shouldUseCacheForFiringRuleForKeyPathInContext(final String keyPath, final D2WContext context) {
        return true;
    }

    private Object fireRuleForKeyPathInContext(Map cache, String keyPath, D2WContext context) {
        final boolean useCache = _shouldUseCacheForFiringRuleForKeyPathInContext(keyPath, context);

        if ( ! useCache && ruleTraceEnabledLog.isDebugEnabled() )
            ruleTraceEnabledLog.debug("CACHE DISABLED for keyPath: " + keyPath);

        String[] significantKeys=(String[])_significantKeysPerKey.get(keyPath);
        if (significantKeys==null) return null;
        short s=(short)significantKeys.length;
        Object[] lhsKeys=new Object[(short)(s+1)];
        for (short i=0; i<s; i++) {
            //lhsKeys[i]=context.valueForKeyPathNoInference(significantKeys[i]);
            lhsKeys[i]=ERD2WUtilities.contextValueForKeyNoInferenceNoException(context, significantKeys[i]);
        }
        lhsKeys[s]=keyPath;
        ERXMultiKey k=new ERXMultiKey(lhsKeys);

        Object result=useCache ? cache.get(k) : null;
        if (result==null) {
            boolean resetTraceRuleFiring = false;
            Logger ruleFireLog=null;
            if (ruleTraceEnabledLog.isDebugEnabled()) {
                Logger ruleCandidatesLog = Logger.getLogger("er.directtoweb.rules." + keyPath + ".candidates");
                ruleFireLog = Logger.getLogger("er.directtoweb.rules." + keyPath + ".fire");
                if (ruleFireLog.isDebugEnabled() && !NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupRules)) {
                    NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupRules);
                    //NSLog.setAllowedDebugLevel(NSLog.DebugLevelDetailed);
                    resetTraceRuleFiring = true;
                }
                if (ruleCandidatesLog.isDebugEnabled()) {
                    ruleFireLog.debug("CANDIDATES for keyPath: " + keyPath + "\n" +
                                      descriptionForRuleSet(canidateRuleSetForRHSInContext(keyPath, context)));
                }
            }
            try {
                if (cache == _systemCache) {
                    result = super.fireSystemRuleForKeyPathInContext(keyPath, context);
                } else {
                    result = super.fireRuleForKeyPathInContext(keyPath, context);
                }
            } catch (StackOverflowError ex) {
                log.error("Problem with this key: " + keyPath + " depends: " + new NSArray(significantKeys) + " values: " + k + " context: " + context + " values: " + context._localValues() + " map: " + cache);
                throw NSForwardException._runtimeExceptionForThrowable(ex);
            }
            if ( useCache )
                cache.put(k,result==null ? NULL_VALUE : result);
            if (ruleTraceEnabledLog.isDebugEnabled()) {
                if (ruleFireLog.isDebugEnabled())
                	ruleFireLog.debug("FIRE: " +keyPath + " for propertyKey: " + context.propertyKey() + " depends on: "  + new NSArray(significantKeys) + " = " + k
                              + " value: " + (result==null ? "<NULL>" : (result instanceof EOEntity ? ((EOEntity)result).name() : result)));
            }
            if (resetTraceRuleFiring) {
                NSLog.refuseDebugLoggingForGroups(NSLog.DebugGroupRules);
            }
        } else {
            if (ruleTraceEnabledLog.isDebugEnabled()) {
                Logger ruleLog = Logger.getLogger("er.directtoweb.rules." + keyPath + ".cache");
                if (ruleLog.isDebugEnabled())
                	ruleLog.debug("CACHE: " + keyPath + " for propertyKey: " + context.propertyKey() + " depends on: "  + new NSArray(significantKeys) + " = " + k
                                  + " value: " + (result==NULL_VALUE ? "<NULL>" : (result instanceof EOEntity ? ((EOEntity)result).name() : result)));
            }
            if (result==NULL_VALUE)
                result=null;
        }
        if (result != null && result instanceof ERDDelayedAssignment) {
            result=((ERDDelayedAssignment)result).fireNow(context);
        }
        return result;
    }

    /** Means to dump the cache. You shouldn't use this unless you know what you are doing. */
    public void dumpCache(String fileName) {
        fileName = fileName == null ? "dmp.cache": fileName;
        synchronized(this) {
            try {
                ERXFileUtilities.writeInputStreamToFile(new ByteArrayInputStream(cacheToBytes(_cache)), new File(fileName));
            } catch(IOException ex) {
                log.error(ex);
            }
        }
    }

    /** Means to restore the cache. You shouldn't use this unless you know what you are doing. */
    public void restoreCache(String fileName) {
        fileName = fileName == null ? "dmp.cache": fileName;
        synchronized(this) {
            try {
                _cache = cacheFromBytes(ERXFileUtilities.bytesFromFile(new File(fileName)));
            } catch(IOException ex) {
                log.error(ex);
            }
        }
    }

    public NSArray canidateRuleSetForRHSInContext(String rhs, D2WContext context) {
        NSMutableSet canidateSet = new NSMutableSet();
        for (Enumeration e = rules().objectEnumerator(); e.hasMoreElements();) {
            Rule r = (Rule)e.nextElement();
            if (r.rhsKeyPath().equals(rhs) && r.canFireInContext(context))
                canidateSet.addObject(r);
        }
        return canidateSet.count() == 0 ? canidateSet.allObjects() :
            EOSortOrdering.sortedArrayUsingKeyOrderArray(canidateSet.allObjects(), ruleSortOrderingKeyArray());
    }


    static class _LhsKeysCallback extends ERDQualifierTraversalCallback {
        public NSMutableArray keys=new NSMutableArray();
        @Override
        public boolean traverseKeyValueQualifier (EOKeyValueQualifier q) {
            if (!keys.containsObject(q.key()))
                keys.addObject(q.key());
            return true;
        }
        @Override
        public boolean traverseKeyComparisonQualifier (EOKeyComparisonQualifier q) {
            if (!keys.containsObject(q.leftKey()))
                keys.addObject(q.leftKey());
            if (!keys.containsObject(q.rightKey()))
                keys.addObject(q.rightKey());
            return true;
        }
    }

    private static final NSSet _addKeyToVectorDefaultKeysToTakeLiterally = new NSSet(new Object[] { "object", "session" });

    protected NSSet _addKeyToVectorKeysToTakeLiterally() {
        return _addKeyToVectorDefaultKeysToTakeLiterally;
    }

    protected void _addKeyToVector(String key, Vector vector) {
        if (key.indexOf(".")!=-1) {
            // we only take the first atom, unless it's object or session
            NSArray a=NSArray.componentsSeparatedByString(key,".");
            String firstAtom=(String)a.objectAtIndex(0);
            if ( ! _addKeyToVectorKeysToTakeLiterally().containsObject(firstAtom) )
                key = firstAtom;
        }
        if (!vector.contains(key))
            vector.addElement(key);
    }

    public void prepareDataStructures() {
      if(log.isDebugEnabled())
        log.debug("prepareDataStructures");
      
        boolean localizationEnabled = ERXLocalizer.isLocalizationEnabled();
        // is a dictionary which will contain for each rhs key, which other keys it depends
        // on, for single rule hops
        Hashtable dependendKeysPerKey=new Hashtable();
        // here we put per key the keys which this key depends on, when computed by a delayed
        // assignment. When this is the case, we only need to add those keys to the main dictionary
        // when the rhs key itself shows up on the lhs
        Hashtable delayedDependendKeysPerKey=new Hashtable();
        _LhsKeysCallback c=new _LhsKeysCallback();

        // we first put all those implicit depedencies introduced by the computedKey business
        Vector v=new Vector();
        v.addElement("propertyKey");
        dependendKeysPerKey.put(D2WModel.PropertyIsKeyPathKey,v.clone());
        v.addElement("entity");
        dependendKeysPerKey.put(D2WModel.RelationshipKey,v.clone());
        dependendKeysPerKey.put(D2WModel.AttributeKey,v.clone());
        dependendKeysPerKey.put(D2WModel.PropertyTypeKey,v.clone());
        dependendKeysPerKey.put(D2WModel.PropertyKeyPortionInModelKey,v.clone());

        // then enumerate through all the rules; h
        for (Enumeration e=rules().objectEnumerator(); e.hasMoreElements();) {
            Rule r=(Rule)e.nextElement();
            String rhsKey=r.rhs().keyPath();
            Vector dependendantKeys=(Vector)dependendKeysPerKey.get(rhsKey);
            if (dependendantKeys==null) {
                dependendantKeys=new Vector();
                dependendKeysPerKey.put(rhsKey,dependendantKeys);
            }
            ERDQualifierTraversal.traverseQualifier(r.lhs(),c);
            for (Enumeration e2=c.keys.objectEnumerator(); e2.hasMoreElements(); ) {
                String k=(String)e2.nextElement();
                _addKeyToVector(k,dependendantKeys);
            }
            // also add those from the assignment
            // if the assignment is delayed, do not add them here;
            // they only need to be added if the key that the assignment computes is itself used
            // on the left hand side
            if (r.rhs() instanceof ERDComputingAssignmentInterface) {
                Vector recipientForNewKeys=dependendantKeys;
                if (r.rhs() instanceof ERDDelayedAssignment) {
                    // put those keys away, needed when reducing the graph and
                    recipientForNewKeys=(Vector)delayedDependendKeysPerKey.get(rhsKey);
                    if (recipientForNewKeys ==null) {
                        recipientForNewKeys =new Vector();
                        delayedDependendKeysPerKey.put(rhsKey, recipientForNewKeys);
                    }
                }
                NSArray extraKeys=((ERDComputingAssignmentInterface)r.rhs()).dependentKeys(rhsKey);
                if (extraKeys!=null) {
                    for (Enumeration e6=extraKeys.objectEnumerator(); e6.hasMoreElements(); ) {
                        String k=(String)e6.nextElement();
                        _addKeyToVector(k, recipientForNewKeys);
                    }
                }
            } else if (r.rhs() instanceof DefaultAssignment) {
                // special treatment for the only custom assignment coming for the D2W default rule set
                // since it does not implement ERDComputingAssignmentInterface, we add the required keys explicitely here
                // another way to do this would be to introduce a rule with the required keys in their LHS, but that is
                // quite a few rules and this is a bit more self contained
                _addKeyToVector("task", dependendantKeys);
                _addKeyToVector("entity", dependendantKeys);
                _addKeyToVector("propertyKey", dependendantKeys);
            }
            if(localizationEnabled && r.rhs() instanceof ERDLocalizableAssignmentInterface) {
                _addKeyToVector("session.language", dependendantKeys);
            }
            c.keys=new NSMutableArray();
        }
        // we then reduce the graph
        if(log.isDebugEnabled())
        log.debug("reducing graph");
        
        boolean touched=true;
        while (touched) {
            touched=false;
            for (Enumeration e3=dependendKeysPerKey.keys(); e3.hasMoreElements();) {
                String rk=(String)e3.nextElement();
                Vector keys=(Vector)dependendKeysPerKey.get(rk);
                for (Enumeration e4=keys.elements(); e4.hasMoreElements();) {
                    String k=(String)e4.nextElement();
                    if (!BACKSTOP_KEYS.containsObject(k)) {
                        Vector newKeys=(Vector)dependendKeysPerKey.get(k);
                        Vector keyFromDelayedAssignment=(Vector)delayedDependendKeysPerKey.get(k);
                        if (newKeys!=null || keyFromDelayedAssignment!=null) {
                            keys.removeElement(k);
                            touched=true;
                            if (newKeys!=null) {
                                for (Enumeration e5=newKeys.elements(); e5.hasMoreElements();) {
                                    String s=(String)e5.nextElement();
                                    _addKeyToVector(s, keys);
                                }
                            }
                            if (keyFromDelayedAssignment!=null) {
                                for (Enumeration e5=keyFromDelayedAssignment.elements(); e5.hasMoreElements();) {
                                    String s=(String)e5.nextElement();
                                    _addKeyToVector(s, keys);
                                }
                            }
                        }
                    }
                }
            }
        }
        // transfer all this into
        for (Enumeration e7=dependendKeysPerKey.keys(); e7.hasMoreElements(); ) {
            String key=(String)e7.nextElement();
            Vector keys=(Vector)dependendKeysPerKey.get(key);
            if(log.isDebugEnabled())
              log.debug("Rhs key "+key+" <-- " + keys);
            
            String[] a=new String[keys.size()];
            for (int i=0; i<keys.size();i++) a[i]=(String)keys.elementAt(i);
            if(_significantKeysPerKey != null)
                _significantKeysPerKey.put(key,a);
        }
    }

    @Override
    protected void invalidateCaches() {
      if(log.isDebugEnabled())
        log.debug("Invalidating cache");
      
        if (_cache!=null)
            _cache.clear();
        if (_systemCache!=null)
            _systemCache.clear();
        if (_significantKeysPerKey!=null)
            _significantKeysPerKey.clear();
        super.invalidateCaches();
    }

    /**
     * <span class="ja">
     * モデル・リセット
     * 
     * 開発中：ローカライズ・ファイルが変更されるとモデルもリセットされます
     * </span>
     */
    public void resetModel() {
      if(log.isInfoEnabled())
        log.info("Resetting Model");
      
      if (_filePathRuleTraceCache!=null)
        _filePathRuleTraceCache.clear();
      
      NSNotificationCenter.defaultCenter().postNotification(ModelWillReset, this);
      setRules(new NSArray());
      initializeClientConfiguration();
      loadRules();
      //invalidateCaches();
      //sortRules();
    }

    protected File _currentFile;
    protected void setCurrentFile(File currentFile) { _currentFile = currentFile; }
    protected File currentFile() { return _currentFile; }

    protected static NSDictionary dictionaryFromPathUrl(URL url) {
        NSDictionary model = null;
        try {
          if(log.isDebugEnabled())
            log.debug("Loading url: " + url);
          
            if(url != null) {
                model = Services.dictionaryFromPathURL(url);
                NSArray rules = (NSArray)model.objectForKey("rules");
                Enumeration e = rules.objectEnumerator();
                boolean patchRules = ERXProperties.booleanForKeyWithDefault("er.directtoweb.ERXD2WModel.patchRules", true);
                while(e.hasMoreElements()) {
                    NSMutableDictionary dict = (NSMutableDictionary)e.nextElement();
                    if(patchRules) {
                        if(Rule.class.getName().equals(dict.objectForKey("class"))) {
                            dict.setObjectForKey(ERD2WRule.class.getName(), "class");
                        }
                    }
                }
            }
        } catch (Throwable throwable) {
            NSLog.err.appendln("****** DirectToWeb: Problem reading file "
                               + url + " reason:" + throwable);
            if (NSLog.debugLoggingAllowedForLevelAndGroups(1, 40L)) {
                NSLog.err.appendln("STACKTRACE:");
                NSLog.err.appendln(throwable);
            }
            throw NSForwardException._runtimeExceptionForThrowable(throwable);
        }
        return model;
    }

    @Override
    protected void mergePathURL(URL modelURL) {
        if(modelURL != null) {

            File modelFile = new File(modelURL.getFile());
            if(log.isDebugEnabled())
            log.debug("Merging rule file \"" + modelFile.getPath() + "\"");
            
            setCurrentFile(modelFile);
            NSDictionary dic = dictionaryFromPathUrl(modelURL);
            if(dic != null) {
                if (ruleDecodeLog.isDebugEnabled()) {
                    ruleDecodeLog.debug("Got dictionary for file: " + modelFile + "\n\n");
                    for (Enumeration e = ((NSArray)dic.objectForKey("rules")).objectEnumerator(); e.hasMoreElements();) {
                        NSDictionary aRule = (NSDictionary)e.nextElement();
                        NSMutableDictionary aRuleDictionary = new NSMutableDictionary(aRule, "rule");
                        EOKeyValueUnarchiver archiver = new EOKeyValueUnarchiver(aRuleDictionary);
                        try {
                            addRule((Rule)archiver.decodeObjectForKey("rule"));
                        } catch (Exception ex) {
                            ruleDecodeLog.error("Bad rule: " + aRule, ex);
                            ruleDecodeLog.error("Decoded rule: " + archiver.decodeObjectForKey("rule"));
                        }
                    }
                } else {
                    NSArray rules = (NSArray) new EOKeyValueUnarchiver(dic).decodeObjectForKey("rules");
                    if(rules != null) {
                        ERD2WModel model = new ERD2WModel(rules);
                        addRules(model.rules());
                    }
                }
            }
            setDirty(false);
        }
        setCurrentFile(null);
    }

    @Override
    protected void mergeFile(File modelFile) {
        mergePathURL(ERXFileUtilities.URLFromFile(modelFile));
    }

    protected Hashtable _uniqueAssignments = new Hashtable();
    protected void uniqueRuleAssignments(NSArray rules) {
        if (rules != null && rules.count() > 0) {
            int uniquedRules = 0;
            if(log.isDebugEnabled())
              log.debug("Starting Assignment uniquing for " + rules.count() + " rules");
            
            //Vector uniqueAssignments = new Vector();
            //Hashtable _uniqueAssignments=new Hashtable();
            for (int c = 0; c < rules.count() - 1; c++) {
                //if (c % 100 == 0)
                //    log.debug("Out of : " + c + " rules, duplicates: " + uniquedRules);
                Rule r = (Rule)rules.objectAtIndex(c);
                if (r != null && r.rhs() != null) {
                    Vector v = (Vector)_uniqueAssignments.get(r.rhs().keyPath());
                    if (v != null) {
                        Assignment unique = assignmentContainedInVector(r.rhs(), v);
                        if (unique == null) {
                            v.addElement(r.rhs());
                        } else if (!(unique == r.rhs())) {
                            r.setRhs(unique);
                            uniquedRules++;
                        }
                    } else {
                        Vector m = new Vector();
                        m.addElement(r.rhs());
                        _uniqueAssignments.put(r.rhs().keyPath(), m);
                    }
                } else {
                    log.warn("Rule is null: " + r + " or rhs: " + (r != null ? r.rhs() : null));
                }
            }
            //h = null;
            //if (uniquedRules > 0)
            //    ERXExtensions.forceGC(0);
            if(log.isDebugEnabled())
                log.debug("Finished Assignment uniquing, got rid of " + uniquedRules + " duplicate assignment(s)");
        }
    }

    protected Assignment assignmentContainedInVector(Assignment a1, Vector v) {
        Assignment containedAssignment = null;
        for (Enumeration e = v.elements(); e.hasMoreElements();) {
            Assignment a2 = (Assignment)e.nextElement();
            if (ERD2WUtilities.assignmentsAreEqual(a1, a2)) {
                containedAssignment = a2; break;
            }
        }
        return containedAssignment;
    }

    protected int uniquedQualifiers = 0;
    protected int totalQualifiers = 0;
    protected void uniqueQualifiers(NSArray rules) {
        if (rules != null && rules.count() > 0) {
            uniquedQualifiers = 0;
            totalQualifiers = 0;
            int replacedQualifiers = 0;
            if(log.isDebugEnabled())
              log.debug("Starting Qualifier uniquing for " + rules.count() + " rules");
            
            //Vector uniqueAssignments = new Vector();
            //Hashtable _uniqueAssignments=new Hashtable();
            for (int c = 0; c < rules.count() - 1; c++) {
                if (c % 100 == 0)
                  if(log.isDebugEnabled())
                    log.debug("Out of : " + c + " rules, qualifiers: " + totalQualifiers + " duplicates: "
                              + uniquedQualifiers + " replaced: " + replacedQualifiers);
                
                Rule r = (Rule)rules.objectAtIndex(c);
                if (r != null && r.lhs() != null) {
                    EOQualifierEvaluation q = r.lhs();
                    try {
                        EOQualifier cache = qualifierInCache((EOQualifier)q);
                        if (cache != null && cache != q) {
                            r.setLhs(cache);
                            //r.setLhs((EOQualifierEvaluation)cache);
                            replacedQualifiers++;
                            //uniquedQualifiers++;
                        }
                    } catch (NullPointerException npe) {
                        log.warn("Caught NPE for rule: " + r);
                    }
                }
            }
            flushUniqueCache();
            if (uniquedQualifiers > 0)
                ERXExtensions.forceGC(0);
            if (log.isDebugEnabled()) 
              log.debug("Finished Qualifier uniquing, for: " + totalQualifiers
                                                + " got rid of " + uniquedQualifiers + " duplicate qualifiers, replaced: " + replacedQualifiers);
        }
    }

    private boolean _hasAddedExtraModelFiles=false;

    /**
     * Overridden to support additional d2wmodel files.
     * Provide an array of filenames (including extension) in the property
     * 'er.directtoweb.ERD2WModel.additionalModelNames', these files should get
     * loaded and added to the rules set during application startup.
     */
	@Override
	public Vector modelFilesPathURLsInBundles() {
		Vector modelFilePaths = super.modelFilesPathURLsInBundles();
		if (!_hasAddedExtraModelFiles) {
			NSArray additionalModelURLs = additionalModelURLs();
			modelFilePaths.addAll(additionalModelURLs);
			_hasAddedExtraModelFiles = true;
		}
		return modelFilePaths;
	}

	private NSArray<URL> additionalModelURLs() {
		NSMutableArray<URL> result = new NSMutableArray();
		if(log.isDebugEnabled())
		  log.debug("Adding additional rule files");
		
		NSArray<String> modelNames = additionalModelNames();
		NSMutableArray<NSBundle> bundles = NSBundle.frameworkBundles().mutableClone();
		bundles.addObject(NSBundle.mainBundle());
		if (modelNames.count() > 0 && bundles.count() > 0) {
			for (NSBundle bundle : bundles) {
				String name = bundle.name();
				if (name != null) {
					for (String modelName : modelNames) {
						URL path = bundle.pathURLForResourcePath(modelName);
						if (path != null) {
						  if(log.isDebugEnabled())
						    log.debug("Adding file '" + path + "' from framework '" + name + "'");
							
							result.addObject(path);
						}
					}
				}
			}
		}
		return result;
	}

	private NSArray<String> additionalModelNames() {
		NSArray<String> modelNames = ERXProperties.arrayForKeyWithDefault("er.directtoweb.ERD2WModel.additionalModelNames", NSArray.EmptyArray);
		NSMutableArray<String> result = new NSMutableArray<String>();
		result.addObjectsFromArray(modelNames);
		String extraModelFilePath = System.getProperty("ERExtraD2WModelFile");
		// it appears super cache's the Vector, so only add the extraModelFile if we haven't already done it
		if (extraModelFilePath != null) {
			result.addObject(extraModelFilePath);
		}
		return result;
	}

    protected EOQualifier qualifierContainedInEnumeration(EOQualifierEvaluation q1, Enumeration e) {
        EOQualifier containedQualifier = null;
        while (e.hasMoreElements()) {
            EOQualifierEvaluation q2 = (EOQualifierEvaluation)e.nextElement();
            if (q1.equals(q2)) {
                containedQualifier = (EOQualifier)q2; break;
            }
        }
        if (containedQualifier != null && q1 != containedQualifier)
            uniquedQualifiers++;
        return containedQualifier;
    }

    protected EOQualifier qualifierInCache(EOQualifier q) {
        EOQualifier cacheQualifier = null;
        totalQualifiers++;
        if (q != null) {
            if (q instanceof EOKeyValueQualifier) {
                cacheQualifier = keyValueQualifierInCache((EOKeyValueQualifier)q);
            } else if (q instanceof EONotQualifier) {
                cacheQualifier = notQualifierInCache((EONotQualifier)q);
            } else if (q instanceof EOAndQualifier) {
                cacheQualifier = andQualifierInCache((EOAndQualifier)q);
            } else if (q instanceof EOOrQualifier) {
                cacheQualifier = orQualifierInCache((EOOrQualifier)q);
            } else {
                log.warn("Unknown qualifier type: " + q.getClass().getName());
            }
        } else {
            log.warn("Asking cache for a null qualifier.");
        }
        return cacheQualifier;
    }

    protected Hashtable _uniqueAndQualifiers = new Hashtable();
    protected EOAndQualifier andQualifierInCache(EOAndQualifier q) {
        EOAndQualifier cachedQualifier = null;
        String hashEntryName = nameForSet(q.allQualifierKeys());
        Vector v = (Vector)_uniqueAndQualifiers.get(hashEntryName);
        if (v != null) {
            EOQualifier cache = qualifierContainedInEnumeration(q, v.elements());
            if (cache != null)
                cachedQualifier = (EOAndQualifier)cache;
        } else {
            v = new Vector();
            _uniqueAndQualifiers.put(hashEntryName, v);
        }
        if (cachedQualifier == null) {
            NSMutableArray qualifiers = null;
            for (int c = 0; c < q.qualifiers().count(); c++) {
                EOQualifier q1 = q.qualifiers().objectAtIndex(c);
                EOQualifier cache = qualifierInCache(q1);
                if (cache != null) {
                    if (qualifiers == null) {
                        qualifiers = new NSMutableArray();
                        qualifiers.addObjectsFromArray(q.qualifiers());
                    }
                    if (cache == q1)
                        log.warn("Found sub-qualifier: " + cache + " in cache when parent qualifier is not?!?!");
                    else
                        qualifiers.replaceObjectAtIndex(cache, c);
                }
            }
            if (qualifiers != null) {
                // Need to reconstruct
                cachedQualifier = new EOAndQualifier(qualifiers);
                v.addElement(cachedQualifier);
            } else {
                v.addElement(q);
            }
        }
        return cachedQualifier;
    }

    protected Hashtable _uniqueOrQualifiers = new Hashtable();
    protected EOOrQualifier orQualifierInCache(EOOrQualifier q) {
        EOOrQualifier cachedQualifier = null;
        String hashEntryName = nameForSet(q.allQualifierKeys());
        Vector v = (Vector)_uniqueOrQualifiers.get(hashEntryName);
        if (v != null) {
            EOQualifier cache = qualifierContainedInEnumeration(q, v.elements());
            if (cache != null)
                cachedQualifier = (EOOrQualifier)cache;
        } else {
            v = new Vector();
            _uniqueOrQualifiers.put(hashEntryName, v);
        }
        if (cachedQualifier == null) {
            NSMutableArray qualifiers = null;
            for (int c = 0; c < q.qualifiers().count(); c++) {
                EOQualifier q1 = q.qualifiers().objectAtIndex(c);
                EOQualifier cache = qualifierInCache(q1);
                if (cache != null) {
                    if (qualifiers == null) {
                        qualifiers = new NSMutableArray();
                        qualifiers.addObjectsFromArray(q.qualifiers());
                    }
                    if (cache == q1)
                        log.warn("Found sub-qualifier: " + cache + " in cache when parent qualifier is not?!?!");
                    else
                        qualifiers.replaceObjectAtIndex(cache, c);
                }
            }
            if (qualifiers != null) {
                // Need to reconstruct
                cachedQualifier = new EOOrQualifier(qualifiers);
                v.addElement(cachedQualifier);
            } else {
                v.addElement(q);
            }
        }
        return cachedQualifier;
    }

    protected Hashtable _uniqueNotQualifiers = new Hashtable();
    protected EONotQualifier notQualifierInCache(EONotQualifier q) {
        EONotQualifier cachedQualifier = null;
        String hashEntryName = nameForSet(q.allQualifierKeys());
        Vector v = (Vector)_uniqueNotQualifiers.get(hashEntryName);
        if (v != null) {
            EOQualifier cache = qualifierContainedInEnumeration(q, v.elements());
            if (cache != null)
                cachedQualifier = (EONotQualifier)cache;
        } else {
            v = new Vector();
            _uniqueNotQualifiers.put(hashEntryName, v);
        }
        if (cachedQualifier == null) {
            EOQualifier cache = qualifierInCache(q.qualifier());
            if (cache != null) {
                if (cache == q.qualifier()) {
                    log.warn("Found sub-qualifier in cache: " + cache + " when qualifier not in cache?!?! " + q);
                    v.addElement(q);
                } else {
                    // Need to construct a new EONotQualifier with the cached value..
                    cachedQualifier = new EONotQualifier(cache);
                    v.addElement(cachedQualifier);
                }
            } else {
                v.addElement(q);
            }
        }
        return cachedQualifier;
    }

    protected Hashtable _uniqueKeyValueQualifiers = new Hashtable();
    protected EOKeyValueQualifier keyValueQualifierInCache(EOKeyValueQualifier q) {
        EOKeyValueQualifier cachedQualifier = null;
        Vector v = (Vector)_uniqueKeyValueQualifiers.get(q.key());
        if (v != null) {
            EOQualifier cache = qualifierContainedInEnumeration(q, v.elements());
            if (cache != null) {
                cachedQualifier = (EOKeyValueQualifier)cache;
            }
        } else {
            v = new Vector();
            _uniqueKeyValueQualifiers.put(q.key(), v);
        }
        if (cachedQualifier == null)
            v.addElement(q);
        return cachedQualifier;
    }

    protected void flushUniqueCache() {
        _uniqueKeyValueQualifiers = new Hashtable();
        _uniqueNotQualifiers = new Hashtable();
        _uniqueOrQualifiers = new Hashtable();
        _uniqueNotQualifiers = new Hashtable();
    }

    // FIXME: Must be a better way of doing
    public String nameForSet(NSSet set) {
        NSMutableArray stringObjects = new NSMutableArray();
        stringObjects.addObjectsFromArray(set.allObjects());
        ERXArrayUtilities.sortArrayWithKey(stringObjects, "description");
        return stringObjects.componentsJoinedByString(".");
    }


    // stuff to dump and restore the cache. This should increase the experience
    // for the first folks after an app needed to be restarted and the rules have not been fired yet
    // the schema is very simplicistic, though, and needs improvement

    protected static final String ENTITY_PREFIX = "::ENTITY::";
    protected static final String RELATIONSHIP_PREFIX = "::RELATIONSHIP::";
    protected static final String ATTRIBUTE_PREFIX = "::ATTRIBUTE::";

    protected Object encodeObject(Object o) {
        if(o instanceof EOEntity) {
            o = ENTITY_PREFIX + ((EOEntity)o).name();
        } else if(o instanceof EORelationship) {
            o = RELATIONSHIP_PREFIX + ((EORelationship)o).name() + ENTITY_PREFIX + ((EORelationship)o).entity().name();
        } else if(o instanceof EOAttribute) {
            o = ATTRIBUTE_PREFIX + ((EOAttribute)o).name() + ENTITY_PREFIX + ((EOAttribute)o).entity().name();
        }
        return o;
    }
    protected Object decodeObject(Object o) {
        if(o instanceof String) {
            String s = (String)o;
            if(s.indexOf(ENTITY_PREFIX) == 0) {
                String entityName = s.substring(ENTITY_PREFIX.length());
                o = EOModelGroup.defaultGroup().entityNamed(entityName);
            } else if(s.indexOf(RELATIONSHIP_PREFIX) == 0) {
                int entityOffset = s.indexOf(ENTITY_PREFIX);
                String entityName = s.substring(entityOffset + ENTITY_PREFIX.length());
                String relationshipName = s.substring(RELATIONSHIP_PREFIX.length(), entityOffset);
                EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
                o = entity.relationshipNamed(relationshipName);
            } else if(s.indexOf(ATTRIBUTE_PREFIX) == 0) {
                int entityOffset = s.indexOf(ENTITY_PREFIX);
                String entityName = s.substring(entityOffset + ENTITY_PREFIX.length());
                String attributeName = s.substring(ATTRIBUTE_PREFIX.length(), entityOffset);
                EOEntity entity = EOModelGroup.defaultGroup().entityNamed(entityName);
                o = entity.attributeNamed(attributeName);
            }
        }
        return o;
    }

    protected boolean writeEntry(ERXMultiKey key, Object value, ObjectOutputStream out)  throws IOException {
        value=encodeObject(value);
        if((value != null) && !(value instanceof Serializable)) {
            return false;
        }
        Object keyKeys[] = key.keys();
        Object keys[]=new Object[keyKeys.length];
        for (short i=0; i<keys.length; i++) {
            Object o=keyKeys[i];
            o=encodeObject(o);
            if((o != null) && !(o instanceof Serializable)) {
                return false;
            }
            keys[i]=o;
        }
        out.writeObject(keys);
        out.writeObject(value);
        return true;
    }

    protected ERXMultiKey readEntry(Hashtable cache, ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object keys[]=(Object[])in.readObject();
        Object value = decodeObject(in.readObject());
        for (short i=0; i<keys.length; i++) {
            Object o=decodeObject(keys[i]);
            keys[i]=o;
        }
        ERXMultiKey key = new ERXMultiKey(keys);
        cache.put(key,value);
        return key;
    }

    protected byte[] cacheToBytes(Hashtable cache) {
        try {
            ByteArrayOutputStream ostream = new ByteArrayOutputStream();
            ObjectOutputStream out = new ObjectOutputStream(ostream);
            for(Enumeration keys = cache.keys(); keys.hasMoreElements();) {
                ERXMultiKey key = (ERXMultiKey)keys.nextElement();
                Object o = cache.get(key);
                if(writeEntry(key,o,out)) {
                  if(log.isDebugEnabled())
                        log.debug("Wrote: " + key + " -- " + o);
                } else {
                  if(log.isInfoEnabled())
                    log.info("Can't write: " + key + " -- " + o);
                }
            }
            out.flush();
            ostream.close();
            return ostream.toByteArray();
        } catch(Exception ex) {
            log.error(ex,ex);
        }
        return null;
    }

    protected Hashtable cacheFromBytes(byte[] bytes) {
        try {
            ByteArrayInputStream istream = new ByteArrayInputStream(bytes);
			ObjectInputStream in = new ERXMappingObjectStream(istream);
            Hashtable newCache = new Hashtable(10000);
            try {
                //FIXME ak how do I do without the EOFException?
                for(;;) {
                    ERXMultiKey key = readEntry(newCache,in);
                    Object o = newCache.get(key);
                    if(log.isDebugEnabled()) {
                        log.debug("Read: " + key + " -- " + o);
                    }
                }
            } catch(EOFException ex) {
            }
            istream.close();
            return newCache;
        } catch(Exception ex) {
            log.error(ex,ex);
        }
        return null;
    }

    public void _diagnoseCache() {
        final Map cache = _cache;
        final Set keySet = cache.keySet();
        final Iterator keySetIterator = keySet.iterator();

        System.out.println("Cache size is: " + cache.size());

        while ( keySetIterator.hasNext() ) {
            final Object theKey = keySetIterator.next();
            final Object theValue = cache.get(theKey);

            System.out.println("\t" + theKey + " -> " + theValue);
        }
    }

}
