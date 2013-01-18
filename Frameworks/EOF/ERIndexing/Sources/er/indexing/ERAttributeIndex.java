package er.indexing;

import java.io.File;
import java.net.MalformedURLException;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSNotification;

import er.extensions.eof.ERXEC;
import er.extensions.foundation.ERXArrayUtilities;

public class ERAttributeIndex extends ERIndex {

    protected class AttributeTransactionHandler extends TransactionHandler {
        
        @Override
        public void _handleChanges(NSNotification n) {
            EOEditingContext ec = (EOEditingContext) n.object();
            if (ec.parentObjectStore() == ec.rootObjectStore()) {

                String notificationName = n.name();
                if (notificationName.equals(ERXEC.EditingContextWillSaveChangesNotification)) {
                    ec.processRecentChanges();
                    NSArray inserted = ec.insertedObjects();
                    NSArray updated = ec.updatedObjects();
                    updated = ERXArrayUtilities.arrayMinusArray(updated, inserted);
                    NSArray deleted = ec.deletedObjects();

                    Transaction transaction = new Transaction(ec);

                    activeChanges.put(ec, transaction);

                } else if (notificationName.equals(ERXEC.EditingContextDidSaveChangesNotification)) {
                    Transaction transaction = activeChanges.get(ec);
                    if (transaction != null) {
                        activeChanges.remove(ec);
                    }
                    submit(transaction);

                } else if (notificationName.equals(ERXEC.EditingContextDidRevertChanges) || notificationName.equals(ERXEC.EditingContextFailedToSaveChanges)) {
                    activeChanges.remove(ec);
                }
            }
        }
    }
    
    public ERAttributeIndex(String name, String store) {
        super(name);
        setStore(store);
        setTransactionHandler(new AttributeTransactionHandler());
    }
    
    public ERAttributeIndex(String name, File store) {
        this(name, toUrl(store));
    }

    private static String toUrl(File store) {
        try {
            return store.toURI().toURL().toString();
        } catch (MalformedURLException e) {
            throw NSForwardException._runtimeExceptionForThrowable(e);
        }
    }

    public static synchronized ERAttributeIndex indexNamed(String key) {
        ERAttributeIndex index = (ERAttributeIndex) ERIndex.indexNamed(key);
        if(index == null) {
            index = new ERAttributeIndex(key, key);
        }
        return index;
    }
}
