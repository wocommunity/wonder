package er.extensions;

import java.util.*;

import com.webobjects.eoaccess.*;
import com.webobjects.eocontrol.*;
import com.webobjects.foundation.*;

/**
 * 
 * @author Lenny Marks (lenny@aps.org)
 *  
 */
public class ERXRecursiveBatchFetching {

    /**
     * Batch fetch relationships specified by <i>keypaths </i> for
     * <i>sourceObjects </i>.
     * <p>
     * This method will use EODatabaseContext.batchFetchRelationship to
     * efficiently fetch like relationships for many objects in as few as one
     * database round trip per relationship.
     * 
     * For example, if fetching from Movie entities, you might specify paths of
     * the form: ("directors","roles.talent", "plotSummary"). This works much
     * like prefetching with fetch specifications, however this implementation
     * is able to work around inheritance where prefetching fails.
     * 
     * @param sourceObjects
     * @param keypaths
     */
    public static void batchFetch(NSArray sourceObjects, NSArray keypaths) {
        EOEnterpriseObject sample = (EOEnterpriseObject) sourceObjects.objectAtIndex(0);

        EOEditingContext ec = sample.editingContext();

        EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) ec.rootObjectStore();

        osc.lock();
        try {

            NSArray rootKeyPathObjects = KeyPath.parseKeyPathStrings(keypaths);

            Enumeration keyPathObjectsEnum = rootKeyPathObjects.objectEnumerator();
            while (keyPathObjectsEnum.hasMoreElements()) {
                KeyPath kp = (KeyPath) keyPathObjectsEnum.nextElement();
                kp.traverseForObjects(sourceObjects);
            }

        } finally {
            osc.unlock();
        }
    }

    /**
     * This class represent a keypath as a hierarchical tree structure(path and
     * subPaths similar to directory and subDirectory).
     * 
     * @author lenny
     *  
     */
    static class KeyPath {

        private String  path;
        private NSArray subPaths; //NSArray of KeyPath objects

        public KeyPath(String path, NSArray subPaths) {
            this.path = path;
            this.subPaths = subPaths;
        }

        /**
         * Traverse through all relationships represented by this keypath for
         * <i>sourceObjects </i> using batchFetching to optimize roundtrips to
         * database.
         * 
         * <p>
         * This method assumes the EOObjectStoreCoordinator has been externally
         * locked.
         * 
         * @see EOFUtils#batchFetch
         * 
         * @param sourceObjects
         */
        public void traverseForObjects(NSArray sourceObjects) {
            if (sourceObjects == null || sourceObjects.count() < 1) return;

            NSDictionary objectsByEntity = splitObjectsByEntity(sourceObjects);
            Enumeration e = objectsByEntity.allValues().objectEnumerator();
            while (e.hasMoreElements()) {
                NSArray homogeniousObjects = (NSArray) e.nextElement();
                traverseForHomogeniousObjects(homogeniousObjects);
            }
        }

        public String path() {
            return path;
        }

        public NSArray subPaths() {
            return subPaths;
        }

        /**
         * Take a list of keypath strings and return a list or KeyPath objects.
         * 
         * <pre>
         * 
         *  ex. from
         *  manuscriptEvents.userAction.enteredBy
         *  manuscriptEvents.userAction.changedBy
         *  manuscriptEvents.correspondence.enteredBy
         *  deniedIndividuals
         *  
         *  to: 
         *  
         *  1. manuscriptEvents.
         *  					userAction.
         *  							   enteredBy
         *                              changedBy
         *  2. manuscriptEvents.
         *  				    correspondence.
         *  								   enteredBy
         *  3. deniedIndividuals
         *  
         * </pre>
         * 
         * @param keypathStrings
         * @return
         */
        public static NSArray parseKeyPathStrings(NSArray keypathStrings) {
            //keyed by top level so we can combine like root paths
            NSDictionary subPathsKeyedByTopLevel = subPathsKeyedByTopLevel(keypathStrings);

            NSMutableArray keyPathObjects = new NSMutableArray();

            Enumeration e = subPathsKeyedByTopLevel.keyEnumerator();
            while (e.hasMoreElements()) {
                String path = (String) e.nextElement();
                NSArray subPaths = (NSArray) subPathsKeyedByTopLevel.valueForKey(path);

                KeyPath kp = new KeyPath(path, KeyPath.parseKeyPathStrings(subPaths));

                keyPathObjects.addObject(kp);
            }

            return keyPathObjects;
        }

        private void traverseForHomogeniousObjects(NSArray sourceObjects) {
            if (sourceObjects == null || sourceObjects.count() < 1) return;

            EOEnterpriseObject eo = (EOEnterpriseObject) sourceObjects.objectAtIndex(0);
            EOEditingContext ec = eo.editingContext();
            EOEntity entity = EOUtilities.entityForObject(ec, eo);

            EORelationship relationship = entity.relationshipNamed(path);

            if (relationship == null) return;

            batchFetchRelationshipOnSourceObjects(relationship, sourceObjects);

            Enumeration subPathsEnum = subPaths.objectEnumerator();
            while (subPathsEnum.hasMoreElements()) {
                KeyPath subPath = (KeyPath) subPathsEnum.nextElement();

                NSArray destinationObjects = destinationObjectsForRelationship(relationship, sourceObjects);

                subPath.traverseForObjects(destinationObjects);
            }
        }

        private static NSDictionary subPathsKeyedByTopLevel(NSArray keypathStrings) {
            NSMutableDictionary subPathsKeyedByTopLevel = new NSMutableDictionary();

            Enumeration e = keypathStrings.objectEnumerator();
            while (e.hasMoreElements()) {
                String keypath = (String) e.nextElement();

                String path = KeyPath.directPathFromKeyPath(keypath);

                NSMutableArray subPaths = (NSMutableArray) subPathsKeyedByTopLevel.valueForKey(path);

                if (subPaths == null) {
                    subPaths = new NSMutableArray();
                    subPathsKeyedByTopLevel.takeValueForKey(subPaths, path);
                }

                String subPath = KeyPath.indirectPathFromKeyPath(keypath);

                if (subPath != null) subPaths.addObject(subPath);
            }

            return subPathsKeyedByTopLevel;
        }

        private NSArray destinationObjectsForRelationship(EORelationship relationship, NSArray sourceObjects) {

            NSMutableArray destinationObjects = new NSMutableArray();

            Enumeration e = sourceObjects.objectEnumerator();
            while (e.hasMoreElements()) {
                EOEnterpriseObject nextEO = (EOEnterpriseObject) e.nextElement();

                if (relationship.isToMany()) {
                    destinationObjects.addObjectsFromArray((NSArray) nextEO.valueForKey(path));
                } else {
                    Object o = nextEO.valueForKey(path);
                    if (o != null) destinationObjects.addObject(o);

                }
            }

            return destinationObjects;
        }

        private void batchFetchRelationshipOnSourceObjects(EORelationship relationship, NSArray sourceObjects) {

            EOEnterpriseObject eo = (EOEnterpriseObject) sourceObjects.objectAtIndex(0);
            EOEditingContext ec = eo.editingContext();

            NSLog.debug.appendln("batch fetching " + path + " on " + eo.entityName());

            EODatabaseContext dbContext = dbContextForEnterpriseObject(eo);

            dbContext.batchFetchRelationship(relationship, sourceObjects, ec);

        }

        private EODatabaseContext dbContextForEnterpriseObject(EOEnterpriseObject eo) {
            EOEditingContext ec = eo.editingContext();

            EOObjectStoreCoordinator osc = (EOObjectStoreCoordinator) ec.rootObjectStore();

            EODatabaseContext dbContext = (EODatabaseContext) osc.objectStoreForObject(eo);

            return dbContext;
        }

        private NSDictionary splitObjectsByEntity(NSArray objects) {
            NSMutableDictionary objectsByEntityName = new NSMutableDictionary();

            Enumeration e = objects.objectEnumerator();
            while (e.hasMoreElements()) {
                EOEnterpriseObject eo = (EOEnterpriseObject) e.nextElement();
                NSMutableArray objectsForEntity = (NSMutableArray) objectsByEntityName.valueForKey(eo.entityName());
                if (objectsForEntity == null) {
                    objectsForEntity = new NSMutableArray();
                    objectsByEntityName.takeValueForKey(objectsForEntity, eo.entityName());
                }

                objectsForEntity.addObject(eo);
            }

            return objectsByEntityName;
        }

        private static String directPathFromKeyPath(String keypath) {
            String path = keypath;

            int indexOfDot = keypath.indexOf(".");
            if (indexOfDot >= 0) {
                path = keypath.substring(0, indexOfDot);
            }

            return path;
        }

        private static String indirectPathFromKeyPath(String keypath) {
            String indirectPath = null;

            int indexOfDot = keypath.indexOf(".");
            if (indexOfDot >= 0) {
                indirectPath = keypath.substring(indexOfDot + 1);
            }

            return indirectPath;
        }
    }

}