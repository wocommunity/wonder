/*
 * Copyright (C) NetStruxr, Inc. All rights reserved.
 *
 * This software is published under the terms of the NetStruxr 
 * Public Software License version 0.5, a copy of which has been
 * included with this distribution in the LICENSE.NPL file.  */
package er.extensions;

import com.webobjects.directtoweb.*;
import com.webobjects.foundation.*;
import com.webobjects.eocontrol.*;
import com.webobjects.eoaccess.*;
import com.webobjects.appserver.*;
import org.apache.log4j.Category;

/**
 * This class is pulled directly out of David Neumann's
 * ChangeNotification framework.  The only changes made are
 * to use log4j instead of System.out.println.<br/>
 * <br/>
 * The tolerant saver provides a way to save an editing context in
 * a tolerant fashion. By tolerant we mean that you can have the
 * option to save an editing context and have the exception ignored,
 * hvae the changes merged from the database or stomp all the changes
 * the database regardless of locking. The entry point for using this
 * class is the <code>save</code> method.
 */
// MOVEME: All of these methods could move to something like ERXEOFUtilities
public class ERXTolerantSaver {

    /** logging support */
    public final static Category cat = Category.getInstance(ERXTolerantSaver.class);

    /**
     * Filters a list of relationships for only the ones that
     * have a given EOAttribute as a source attribute. This
     * implementation igonores relationships that have compound
     * keys.
     * @param attrib EOAttribute to filter source attributes of
     *		relationships.
     * @param rels array of EORelationship objects.
     * @return filtered array of EORelationship objects that have
     * 		the given attribute as the source attribute.
     */
    // MOVEME: ERXEOFUtilities
    public static NSArray relationshipsForAttribute(EOAttribute attrib, NSArray rels) {
        int i;
        NSMutableArray arr = new NSMutableArray();
        int cnt = rels.count();
        for(i=0; i<cnt; i++){
            EORelationship rel = (EORelationship)rels.objectAtIndex(i);
            NSArray attribs = rel.sourceAttributes();
            if(attribs != null){
                int cnt2 = attribs.count();
                // I'm ignoring compound key relationships for now
                if(cnt2 < 2 && attribs.containsObject(attrib)){
                    arr.addObject(rel);
                }
            }
        }
        return arr;
    }

    /**
     * Constructs a primary key dictionary given a
     * value and an entity. This implementation ignores
     * entities that have multiple primary keys.
     * @param val primary key to be the value in the dictionary.
     * @param destEnt entity to construct the primary key dictionary
     *		for.
     * @return primary key dictionary for the given value and entity
     */
    // MOVEME: ERXEOFUtilities
    public static NSDictionary primaryKeyFor(Object val, EOEntity destEnt){
        NSArray keys = destEnt.primaryKeyAttributes();
        int cnt = keys.count();
        if(cnt < 2){
            EOAttribute pkAttrib = (EOAttribute)keys.objectAtIndex(0);
            String key = pkAttrib.name();
            NSDictionary d = new NSDictionary (val, key);
            return d;
        }
        return null;
    }

    public static void applyEOChangesForValue(Object val, EOAttribute attrib, NSArray rels, EOEnterpriseObject eo){
        int i;
        int cnt = rels.count();
        EOEditingContext ec = eo.editingContext();
        for(i=0; i<cnt; i++){
            EORelationship rel = (EORelationship)rels.objectAtIndex(i);
            String relKey = rel.name();
            EOEntity destEnt = rel.destinationEntity();
            NSDictionary pkDict = primaryKeyFor(val, destEnt);
            if(pkDict != null){
                EOGlobalID gid = destEnt.globalIDForRow(pkDict);
                EOEnterpriseObject destEO = ec.faultForGlobalID(gid, ec);
                eo.takeValueForKey(destEO, relKey);
            }
        }
    }

    public static void applyChangesToEO(NSDictionary changedValues, EOEnterpriseObject failedEO, EOEntity ent){
        int i;
        NSArray keys = changedValues.allKeys();
        int cnt = keys.count();
        NSArray cps = ent.classProperties ();
        NSArray rels = ent.relationships ();
        for(i=0; i<cnt; i++){
            String key = (String)keys.objectAtIndex(i);
            EOAttribute attrib = ent.attributeNamed(key);
            if(attrib != null){
                Object val = changedValues.objectForKey(key);
                NSArray relsUsingAttrib = relationshipsForAttribute(attrib, rels);
                int cnt2 = relsUsingAttrib.count();
                if(cps.containsObject(attrib)){
                    failedEO.takeValueForKey(val, key);
                }
                if(cnt2 > 0){
                    applyEOChangesForValue(val, attrib, relsUsingAttrib, failedEO);
                }
            }
        }
    }

    public static EOQualifier qualifierWithSnapshotAndPks(NSArray pkAttribs, NSDictionary snapshot){
        int i;
        int cnt = pkAttribs.count();
        NSMutableArray qualArray = new NSMutableArray();
        for(i=0;i<cnt;i++){
            EOAttribute attrib = (EOAttribute)pkAttribs.objectAtIndex(i);
            String key = attrib.name();
            EOKeyValueQualifier qual = new EOKeyValueQualifier(key, EOQualifier.QualifierOperatorEqual, snapshot.objectForKey(key));
            qualArray.addObject(qual);
        }
        return (EOQualifier)new EOAndQualifier(qualArray);
    }


    public static String save(EOEditingContext ec, boolean writeAnyWay) {
        return save(ec, writeAnyWay, true);
    }
    public static String saveMerge(EOEditingContext ec, boolean writeAnyWay, boolean merge) {
        return save(ec, writeAnyWay, merge);
    }

    // FIXME: returning those strings for error conditions is not very good
    // we should probably return ints for status and re-throw the original exception
    // in some cases
    public static String save(EOEditingContext ec, boolean writeAnyWay, boolean merge) {
        if (cat.isDebugEnabled()) cat.debug("TolerantSaver: save...");
        try{
            //if (cat.isDebugEnabled()) cat.debug("about to save changes...");
            ec.saveChanges();
        } catch(NSValidation.ValidationException eov) {
            cat.info("TolerantSaver: Caught EOValidationException: " + eov.getMessage());
        } catch(EOGeneralAdaptorException e) {
            EOEnterpriseObject failedEO;
            NSDictionary userInfo = (NSDictionary)e.userInfo();
            //if (cat.isDebugEnabled()) cat.debug("TolerantSaver: Exception occurred name: "+ eName);
            //if (cat.isDebugEnabled()) cat.debug("Exception occurred e: -------------------------");
            //if (cat.isDebugEnabled()) cat.debug("Exception occurred e: "+e);
            //if (cat.isDebugEnabled()) cat.debug("Exception occurred userInfo: "+ userInfo);
            //if (cat.isDebugEnabled()) cat.debug("Exception occurred e: ^^^^^^^^^^^^^^^^^^^^^^^^^");
            if(!(userInfo == null)){
                String eType = (String)userInfo.objectForKey("EOAdaptorFailureKey");
                if(!(eType == null)){
                    if(eType.equals("EOAdaptorOptimisticLockingFailure")){
                        //if (cat.isDebugEnabled()) cat.debug("about to get EOFailedAdaptorOperationKey");
                        EOAdaptorOperation op = (EOAdaptorOperation) userInfo.objectForKey("EOFailedAdaptorOperationKey");
                        EODatabaseOperation dbop = (EODatabaseOperation) userInfo.objectForKey("EOFailedDatabaseOperationKey");
                        //if (cat.isDebugEnabled()) cat.debug("about to get _changedValues");
                        if (op!=null && dbop!=null) {
                            NSDictionary changedValues =  op.changedValues();
                            //if (cat.isDebugEnabled()) cat.debug("about to get _entity: _changedValues"+ changedValues);
                            NSDictionary snapshot = dbop.dbSnapshot();
                            if (cat.isDebugEnabled()) cat.debug("snapshot"+ snapshot);
                            EOEntity ent = op.entity();
                            String entName = ent.name();
                            if (cat.isDebugEnabled()) cat.debug("entName"+ entName);
                            NSArray pkAttribs = ent.primaryKeyAttributes();
                            EOQualifier qual = ERXTolerantSaver.qualifierWithSnapshotAndPks(pkAttribs, snapshot);
                            EOFetchSpecification fs = new EOFetchSpecification(entName, qual, null);
                            fs.setRefreshesRefetchedObjects(true);
                            NSArray objs = ec.objectsWithFetchSpecification(fs);
                            if(objs.count() > 0){
                                failedEO = (EOEnterpriseObject) objs.objectAtIndex(0);
                                if (cat.isDebugEnabled()) cat.debug("failedEO"+ failedEO);
                                if(merge){
                                    //EODatabaseContext dbcontext = ChangeCatcher.databaseContextForEntityNamed (entName, ec);
                                    //EODatabase db = dbcontext.database();
                                    //EOGlobalID gid = ec.globalIDForObject(failedEO);
                                    //NSMutableDictionary ss = new NSMutableDictionary(snapshot);
                                    //ss. addEntriesFromDictionary(changedValues);
                                    //db.forgetSnapshotForGlobalID(gid);
                                    //db.recordSnapshotForGlobalID(ss, gid);
                                    applyChangesToEO(changedValues, failedEO, ent);
                                }
                            }else{
                                if (cat.isDebugEnabled()) cat.debug("TolerantSaver: EO was NOT there anymore!");
                                failedEO = null;
                            }
                            if(writeAnyWay){
                                if (cat.isDebugEnabled()) cat.debug("TolerantSaver: about to save changes again");
                                ec.saveChanges();
                            }
                            return "EOAdaptorOptimisticLockingFailure";
                        } else {
                            cat.error("Missing EOFailedAdaptorOperationKey or EOFailedDatabaseOperationKey. "+e+"\n\n"+e.userInfo());
                        }
                    }                    
                } else {
                    cat.error("TolerantSaver: No EOAdaptorFailureKey Exception:" + e);
                    return "Error: No EOAdaptorFailureKey";
                }
            } else {
                cat.error("TolerantSaver: No UserInfo: "+e);
                return "Error: No UserInfo";
            }
        }
        if (cat.isDebugEnabled()) cat.debug("TolerantSaver: save... done");
        return null;
    }
}
