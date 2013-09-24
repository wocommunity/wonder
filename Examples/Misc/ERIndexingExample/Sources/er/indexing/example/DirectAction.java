package er.indexing.example;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WORequest;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOKeyValueQualifier;
import com.webobjects.eocontrol.EOQualifier;
import com.webobjects.eocontrol._EOMutableKnownKeyDictionary;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSNotification;

import er.directtoweb.ERD2WDirectAction;
import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXRemoteNotificationCenter;
import er.extensions.foundation.ERXSelectorUtilities;
import er.indexing.ERIndex;
import er.indexing.example.eof.Asset;
import er.indexing.example.eof.AssetGroup;
import er.indexing.example.eof.Tag;

public class DirectAction extends ERD2WDirectAction {

    public DirectAction(WORequest aRequest) {
        super(aRequest);
    }
   
    /**
     * Checks if a page configuration is allowed to render.
     * Provide a more intelligent access scheme as the default just returns false. And
     * be sure to read the javadoc to the super class.
     * @param pageConfiguration
     * @return
     */
    @Override
    protected boolean allowPageConfiguration(String pageConfiguration) {
        return true;
    }

    @Override
    public WOActionResults defaultAction() {
        //testIndexing();
        NSDictionary dict = new NSDictionary("TestValue", "TestKey");
        NSArray keys = new NSArray(new String[]{"test1", "test2"});
        _EOMutableKnownKeyDictionary vals;
        _EOMutableKnownKeyDictionary.Initializer initializer = new _EOMutableKnownKeyDictionary.Initializer(keys);
        vals = new _EOMutableKnownKeyDictionary(initializer);
        log.info(vals);
        vals.setObjectForKey("t1", "test1");
        log.info(vals);
        vals.setObjectForKey("t2", "test2");
        log.info(vals);
        vals.setObjectForKey("t3", "test3");
        log.info(vals);
        vals = new _EOMutableKnownKeyDictionary(initializer, new Object[]{"1", "2"});
        log.info(vals);
//        ERXRemoteNotificationCenter.defaultCenter().postNotification("All", null, dict);
        return pageWithName(Main.class.getName());
    }

    static {
        ERXRemoteNotificationCenter.defaultCenter().addObserver(DirectAction.class, ERXSelectorUtilities.notificationSelector("receiveNotification"), "All", null);
    }
    
    public static void receiveNotification(NSNotification n) {
        log.info("Received: " + n);
    }
    
    private void testIndexing() {
        EOEditingContext ec = ERXEC.newEditingContext();
        ec.lock();
        try {
            Tag tag = Tag.clazz.allObjects(ec).lastObject();
            Asset asset = Asset.clazz.allObjects(ec).lastObject();
            AssetGroup assetGroup = AssetGroup.clazz.allObjects(ec).lastObject();
            // new DataCreator().createDummyData();
            ERIndex eofStore = ERIndex.indexNamed("AssetInEOFStore");
            ERIndex fileStore = ERIndex.indexNamed("AssetInFileStore");
            EOQualifier tagQualifier = new EOKeyValueQualifier("tags.name", EOQualifier.QualifierOperatorEqual, tag.name());
            EOQualifier groupQualifier = new EOKeyValueQualifier("assetGroup.name", EOQualifier.QualifierOperatorEqual, tag.name());
            log.info("fileStore: " + fileStore.findGlobalIDs(tagQualifier).count());
            log.info("eofStore: " + eofStore.findGlobalIDs(tagQualifier).count());
            log.info("fileStore: " + fileStore.findGlobalIDs(groupQualifier).count());
            log.info("eofStore: " + eofStore.findGlobalIDs(groupQualifier).count());
            
            String newName = "cooltest";
            tagQualifier = new EOKeyValueQualifier("tags.name", com.webobjects.eocontrol.EOQualifier.QualifierOperatorEqual, newName);
            tag.setName(newName + " " + System.currentTimeMillis());
            ec.saveChanges();
            
            assetGroup.setName(newName + "  " + System.currentTimeMillis());
            ec.saveChanges();
            log.info("fileStore 1: " + fileStore.findGlobalIDs(tagQualifier).count());
            log.info("eofStore 1: " + eofStore.findGlobalIDs(tagQualifier).count());
            try {
                if(true) {
                    Thread.sleep(2000);
                }
                log.info("fileStore 2: " + fileStore.findGlobalIDs(tagQualifier).count());
                log.info("eofStore 2: " + eofStore.findGlobalIDs(tagQualifier).count());
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } finally {
            ec.unlock();
        }
    }
}
