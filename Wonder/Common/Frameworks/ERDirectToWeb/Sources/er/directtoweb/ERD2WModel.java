/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.directtoweb;

import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import com.webobjects.directtoweb.*;
import com.webobjects.directtoweb.ERD2WUtilities;
import java.util.*;
import java.io.*;
import java.lang.*;
import er.extensions.*;
import org.apache.log4j.Category;

// new caching scheme, plus log4j integration.
public class ERD2WModel extends D2WModel {

    /////////////////////////////////////  log4j category  ////////////////////////////////////
    public static final Category cat = Category.getInstance(ERD2WModel.class);

    ///////////////////////////////////// Notification Titles /////////////////////////////////
    // Register for this notification to have the hook in place to load non-d2wmodel based rules
    public static final String WillSortRules = "WillSortRules";
    public static final String ModelWillReset = "ModelWillReset";
    
    private Hashtable _cache=new Hashtable(10000);
    private Hashtable _systemCache=new Hashtable(10000);
    private Hashtable _significantKeysPerKey=new Hashtable(500);

        // put here the keys than can either provided as input or computed
    // FIXME should add API from clients to add to this array
    static NSMutableArray BACKSTOP_KEYS=new NSMutableArray(new Object[] { "pageConfiguration", "entity", "task" });
    static {
        Class c=D2WFastModel.class; // force initialization
        D2WModel.setDefaultModel(new ERD2WModel(ERXConstant.EmptyArray));
    }

    public static ERD2WModel erDefaultModel() { return (ERD2WModel)D2WModel.defaultModel(); }

    private final static EOSortOrdering _prioritySortOrdering=new EOSortOrdering("priority",EOSortOrdering.CompareDescending);
    private final static EOSortOrdering _descriptionSortOrdering=new EOSortOrdering("toString",EOSortOrdering.CompareDescending);
    private static NSArray ruleSortOrderingKeyArray() {
        NSMutableArray result=new NSMutableArray();
        result.addObject(_prioritySortOrdering);
        result.addObject(_descriptionSortOrdering);
        return result;
    }
    private final static NSArray _ruleSortOrderingKeyVector=ruleSortOrderingKeyArray();

    protected ERD2WModel(NSArray rules) {
        super(rules);
    }
    
    protected void sortRules() {
        // This allows other non-d2wmodel file based rules to be loaded.
        cat.debug("posting WillSortRules.");
        NSNotificationCenter.defaultCenter().postNotification(WillSortRules, this);
        cat.debug("posted WillSortRules.");
        // We don't want dynamicly loaded rules to cause rapid-turnaround to not work.
        setDirty(false);
            //uniqueRuleAssignments();
            //uniqueQualifiers(rules());
            super.sortRules();
        cat.debug("called super sortRules.");
        /*
         the following sort call was to attempt to make assistant generated files more CVS compatible
         by preserving the rule order better. Commenting out since it's very memory hungry (calling description on every rule)
         and we are not using the Assistant
        EOSortOrdering.sortArrayUsingKeyOrderArray((NSMutableArray)rules(),
                                                   _ruleSortOrderingKeyVector);
        cat.debug("Finished sorting.");
        */
        if (rules() !=null && rules().count() > 0) prepareDataStructures();
    }
    
    // These are public cover methods for protected methods.
    public NSArray publicRules() { return rules(); }
    public void publicAddRule(Rule rule) { addRule(rule); }
    public void publicAddRules(NSArray rules) { addRules(rules); }
    public void publicRemoveRule(Rule rule) { removeRule(rule); }
    
    private final static Object NULL_VALUE=new Object();

    protected String descriptionForRuleSet(NSArray set) {
        StringBuffer buffer = new StringBuffer();
        for (Enumeration e = set.objectEnumerator(); e.hasMoreElements();)
            buffer.append("\t" + descriptionForRule((Rule)e.nextElement()) + "\n");
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
    protected void addRules(NSArray rules) {
        //uniqueRuleAssignments(rules);
        super.addRules(rules);
        if (!WOApplication.application().isCachingEnabled() && currentFile() != null) {
            NSArray components = NSArray.componentsSeparatedByString(currentFile().getAbsolutePath(), "/");
            String filePath = components.count() > 1 ? (String)components.objectAtIndex(components.count() - 2) : currentFile().getAbsolutePath();
            if (_filePathRuleTraceCache == null)
                _filePathRuleTraceCache = new Hashtable();
            for (Enumeration e = rules.objectEnumerator(); e.hasMoreElements();) {
                _filePathRuleTraceCache.put(e.nextElement(), filePath);
               // System.out.println("Setting for for filePath: " + filePath);
            }
        }
    }
    
    protected static ERD2WModel _defaultModel;
    public static ERD2WModel defaultERModel() { return _defaultModel; }

    public static final Category ruleTraceEnabledCat = Category.getInstance("er.directtoweb.rules.ERD2WTraceRuleFiringEnabled");
    protected Object fireRuleForKeyPathInContext(String keyPath, D2WContext context) {
        String[] significantKeys=(String[])_significantKeysPerKey.get(keyPath);
        if (significantKeys==null) return null;
        short s=(short)significantKeys.length;
        ERXMultiKey k=new ERXMultiKey((short)(s+1));
        Object[] lhsKeys=k.keys();
        for (short i=0; i<s; i++) {
            //lhsKeys[i]=context.valueForKeyPathNoInference(significantKeys[i]);
            lhsKeys[i]=ERD2WUtilities.contextValueForKeyNoInferenceNoException(context, significantKeys[i]);
        }
        lhsKeys[s]=keyPath;
        Object result=_cache.get(k);
        if (result==null) {
            boolean resetTraceRuleFiring = false;
            Category ruleFireCat=null;
            if (ruleTraceEnabledCat.isDebugEnabled()) {
                Category ruleCandidatesCat = Category.getInstance("er.directtoweb.rules." + keyPath + ".candidates");
                ruleFireCat = Category.getInstance("er.directtoweb.rules." + keyPath + ".fire");
                if (ruleFireCat.isDebugEnabled() && !NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupRules)) {
                    NSLog.allowDebugLoggingForGroups(NSLog.DebugGroupRules);
                    NSLog.setAllowedDebugLevel(NSLog.DebugLevelDetailed);
                    resetTraceRuleFiring = true;
                }
                if (ruleCandidatesCat.isDebugEnabled()) {
                    ruleFireCat.debug("CANDIDATES for keyPath: " + keyPath + "\n" +
                                      descriptionForRuleSet(canidateRuleSetForRHSInContext(keyPath, context)));
                }
            }
            result=super.fireRuleForKeyPathInContext(keyPath,context);
            _cache.put(k,result==null ? NULL_VALUE : result);
            if (ruleTraceEnabledCat.isDebugEnabled()) {
                if (ruleFireCat.isDebugEnabled())
                ruleFireCat.debug("FIRE: " + keyPath + " for propertyKey: " + context.propertyKey() + " depends on: " + new NSArray(significantKeys)
                              + " value: " + (result==null ? "<NULL>" : (result instanceof EOEntity ? ((EOEntity)result).name() : result)));
            }
            if (resetTraceRuleFiring) {
                NSLog.refuseDebugLoggingForGroups(NSLog.DebugGroupRules);
            }
        } else {
            if (ruleTraceEnabledCat.isDebugEnabled()) {
                Category ruleCat = Category.getInstance("er.directtoweb.rules." + keyPath + ".cache");
                if (ruleCat.isDebugEnabled())
                    ruleCat.debug("CACHE: " + keyPath +  " for propertyKey: " + context.propertyKey() + " depends on: "
                                  + new NSArray(significantKeys) + " value: " + (result==NULL_VALUE ? "<NULL>" : (result instanceof EOEntity ? ((EOEntity)result).name() : result)));
            }
            if (result==NULL_VALUE)
                result=null;
        }
        if (result != null && result instanceof ERDDelayedAssignment) {
            result=((ERDDelayedAssignment)result).fireNow(context);
        }
        //Object resultPrint=(result instanceof EOEntity)? ((EOEntity)result).name() : result;
        //System.out.println("DONE fireRuleForKeyPathInContext "+keyPath+" = "+resultPrint);
        return result;
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

    synchronized protected Object fireSystemRuleForKeyPathInContext(String keyPath, D2WContext context) {
        // FIXME: optimize me!
        return super.fireSystemRuleForKeyPathInContext(keyPath,context);
    }


    static class _LhsKeysCallback extends er.extensions.ERXQualifierTraversalCallback {
        public NSMutableArray keys=new NSMutableArray();
        public boolean traverseKeyValueQualifier (EOKeyValueQualifier q) {
            if (!keys.containsObject(q.key()))
                keys.addObject(q.key());
            return true;
        }
        public boolean traverseKeyComparisonQualifier (EOKeyComparisonQualifier q) {
            if (!keys.containsObject(q.leftKey()))
                keys.addObject(q.leftKey());
            if (!keys.containsObject(q.rightKey()))
                keys.addObject(q.rightKey());
            return true;
        }
    }

    static void addKeyToVector(String key, Vector vector) {
        if (key.indexOf(".")!=-1) {
            // we only take the first atom, unless it's object or session
            NSArray a=NSArray.componentsSeparatedByString(key,".");
            String firstAtom=(String)a.objectAtIndex(0);
            if (!firstAtom.equals("object") && !firstAtom.equals("session"))
                key=firstAtom;
        }
        if (!vector.contains(key))
            vector.addElement(key);
    }

    public void prepareDataStructures() {
        cat.debug("prepareDataStructures");

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
        for (Enumeration e=publicRules().objectEnumerator(); e.hasMoreElements();) {
            Rule r=(Rule)e.nextElement();
            String rhsKey=r.rhs().keyPath();
            Vector dependendantKeys=(Vector)dependendKeysPerKey.get(rhsKey);
            if (dependendantKeys==null) {
                dependendantKeys=new Vector();
                dependendKeysPerKey.put(rhsKey,dependendantKeys);
            }
            er.extensions.ERXQualifierTraversal.traverseQualifier(r.lhs(),c);
            for (Enumeration e2=c.keys.objectEnumerator(); e2.hasMoreElements(); ) {
                String k=(String)e2.nextElement();
                addKeyToVector(k,dependendantKeys);
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
                for (Enumeration e6=extraKeys.objectEnumerator(); e6.hasMoreElements(); ) {
                    String k=(String)e6.nextElement();
                    addKeyToVector(k, recipientForNewKeys);
                }
            }
            c.keys=new NSMutableArray();
        }
        // we then reduce the graph
        cat.debug("reducing graph");
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
                                    addKeyToVector(s, keys);
                                }
                            }
                            if (keyFromDelayedAssignment!=null) {
                                for (Enumeration e5=keyFromDelayedAssignment.elements(); e5.hasMoreElements();) {
                                    String s=(String)e5.nextElement();
                                    addKeyToVector(s, keys);
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
            if (NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupRules)) System.out.print("Rhs key "+key+" <-- ");
            Vector keys=(Vector)dependendKeysPerKey.get(key);
            if (NSLog.debugLoggingAllowedForGroups(NSLog.DebugGroupRules)) System.out.println(keys);
            String[] a=new String[keys.size()];
            for (int i=0; i<keys.size();i++) a[i]=(String)keys.elementAt(i);
            _significantKeysPerKey.put(key,a);
        }
    }

    protected void invalidateCaches() {
        cat.info("Invalidating cache");
        if (_cache!=null)
            _cache.clear();
        if (_systemCache!=null)
            _systemCache.clear();
        if (_significantKeysPerKey!=null)
            _significantKeysPerKey.clear();
        super.invalidateCaches();
    }

    public void resetModel() {
        cat.info("Resetting Model");
        if (_filePathRuleTraceCache!=null)
            _filePathRuleTraceCache.clear();
        NSNotificationCenter.defaultCenter().postNotification(ModelWillReset, this);
        invalidateCaches();
        sortRules();
    }

    protected File _currentFile;
    protected void setCurrentFile(File currentFile) { _currentFile = currentFile; }
    protected File currentFile() { return _currentFile; }

    protected void mergeFile(File modelFile) {
        if(cat.isDebugEnabled()) cat.debug("model file being merged = "+modelFile);
        setCurrentFile(modelFile);
        // Uncomment this code if rules are not unarchiving correctly
        /*
        try {
            NSDictionary dic = Services.dictionaryFromFile(modelFile);
            cat.info("\n\n Got dictionary for file: " + modelFile);
            for (Enumeration e = ((NSArray)dic.objectForKey("rules")).objectEnumerator(); e.hasMoreElements();) {
                NSDictionary aRule = (NSDictionary)e.nextElement();
                NSMutableDictionary aRuleDictionary = new NSMutableDictionary(aRule, "rule");
                EOKeyValueUnarchiver archiver = new EOKeyValueUnarchiver(aRuleDictionary);
                try {
                    addRule((Rule)archiver.decodeObjectForKey("rule"));
                } catch (Exception ex) {
                    cat.error("Bad rule: " + aRule);
                }
            }
        } catch (IOException except) {
            cat.error("Bad, bad" + except.getMessage());
        } */
        super.mergeFile(modelFile);
        //uniqueRuleAssignments();
        setCurrentFile(null);
        ERXExtensions.forceGC(1);
    }

    protected Hashtable _uniqueAssignments = new Hashtable();
    protected void uniqueRuleAssignments(NSArray rules) {
        if (rules != null && rules.count() > 0) {
            int uniquedRules = 0;
            if (cat.isDebugEnabled()) cat.debug("Starting Assignment uniquing for " + rules.count() + " rules");
            //Vector uniqueAssignments = new Vector();
            //Hashtable _uniqueAssignments=new Hashtable();
            for (int c = 0; c < rules.count() - 1; c++) {
                //if (c % 100 == 0)
                //    cat.debug("Out of : " + c + " rules, duplicates: " + uniquedRules);
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
                    cat.warn("Rule is null: " + r + " or rhs: " + (r != null ? r.rhs() : null));
                }
            }
            //h = null;
            //if (uniquedRules > 0)
            //    ERXExtensions.forceGC(0);
            if (cat.isDebugEnabled()) cat.debug("Finished Assignment uniquing, got rid of " + uniquedRules + " duplicate assignment(s)");
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
            if (cat.isDebugEnabled()) cat.debug("Starting Qualifier uniquing for " + rules.count() + " rules");
            //Vector uniqueAssignments = new Vector();
            //Hashtable _uniqueAssignments=new Hashtable();
            for (int c = 0; c < rules.count() - 1; c++) {
                if (c % 100 == 0)
                    cat.debug("Out of : " + c + " rules, qualifiers: " + totalQualifiers + " duplicates: "
                              + uniquedQualifiers + " replaced: " + replacedQualifiers);
                Rule r = (Rule)rules.objectAtIndex(c);
                if (r != null && r.lhs() != null) {
                    EOQualifierEvaluation q = r.lhs();
                    try {
                        EOQualifier cache = qualifierInCache((EOQualifier)q);
                        if (cache != null && cache != q) {
                            r.setLhs((EOQualifier)cache);
                            //r.setLhs((EOQualifierEvaluation)cache);
                            replacedQualifiers++;
                            //uniquedQualifiers++;
                        }
                    } catch (NullPointerException npe) {
                        cat.warn("Caught NPE for rule: " + r);
                    }
                }
            }
            flushUniqueCache();
            if (uniquedQualifiers > 0)
                ERXExtensions.forceGC(0);
            if (cat.isDebugEnabled()) cat.debug("Finished Qualifier uniquing, for: " + totalQualifiers
                                                + " got rid of " + uniquedQualifiers + " duplicate qualifiers, replaced: " + replacedQualifiers);
        }
    }

    private boolean _hasAddedExtraModelFile=false;
    public Vector modelFilesInBundles () {
        Vector modelFiles = super.modelFilesInBundles();
        if (!_hasAddedExtraModelFile) {
            String extraModelFilePath = System.getProperty("ERExtraD2WModelFile");
            // it appears super cache's the Vector, so only add the extraModelFile if we haven't already done it
            if (extraModelFilePath != null) {
                if (cat.isDebugEnabled()) cat.debug("ERExtraD2WModelFile = \"" + extraModelFilePath + "\"");
                File extraModelFile = new java.io.File(extraModelFilePath);
                if (extraModelFile.exists() && extraModelFile.isFile() && extraModelFile.canRead()) {
                    extraModelFilePath = extraModelFile.getAbsolutePath();
                    if (cat.isDebugEnabled()) cat.debug("ERExtraD2WModelFile (absolute) = \"" + extraModelFilePath + "\"");
                    modelFiles.addElement(extraModelFile);
                    _hasAddedExtraModelFile = true;
                } else
                    cat.warn("Can't read the ERExtraD2WModelFile file.");
            }
        }
        return modelFiles;
    }
    protected EOQualifier qualifierContainedInEnumeration(EOQualifierEvaluation q1, Enumeration e) {
        EOQualifier containedQualifier = null;
        while (e.hasMoreElements()) {
            EOQualifierEvaluation q2 = (EOQualifierEvaluation)e.nextElement();
            if (ERXQualifierUtilities.qualifiersAreEqual(q1, q2)) {
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
                cat.warn("Unknown qualifier type: " + q.getClass().getName());
            }
        } else {
            cat.warn("Asking caceh for a null qualifier.");
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
                EOQualifier q1 = (EOQualifier)q.qualifiers().objectAtIndex(c);
                EOQualifier cache = qualifierInCache(q1);
                if (cache != null) {
                    if (qualifiers == null) {
                        qualifiers = new NSMutableArray();
                        qualifiers.addObjectsFromArray(q.qualifiers());
                    }
                    if (cache == q1)
                        cat.warn("Found sub-qualifier: " + cache + " in cache when parent qualifier is not?!?!");
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
                EOQualifier q1 = (EOQualifier)q.qualifiers().objectAtIndex(c);
                EOQualifier cache = qualifierInCache(q1);
                if (cache != null) {
                    if (qualifiers == null) {
                        qualifiers = new NSMutableArray();
                        qualifiers.addObjectsFromArray(q.qualifiers());
                    }
                    if (cache == q1)
                        cat.warn("Found sub-qualifier: " + cache + " in cache when parent qualifier is not?!?!");
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
                    cat.warn("Found sub-qualifier in cache: " + cache + " when qualifier not in cache?!?! " + q);
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
        ERXUtilities.sortEOsUsingSingleKey(stringObjects, "description");
        return stringObjects.componentsJoinedByString(".");
    }
}