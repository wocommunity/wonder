package er.corebusinesslogic.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

public class ERCoreBusinessLogic0 extends ERXMigrationDatabase.Migration {
    
    public ERCoreBusinessLogic0() {
        // FIXME ak: dynamic
       super(new NSArray("en"));
    }
    
    @Override
    public NSArray<ERXModelVersion> modelDependencies() {
        return null;
    }

    @Override
    public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
        // DO NOTHING
    }

    @Override
    public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
        
        ERXMigrationTable eRCAuditBlobTable = database.newTableNamed("ERCAuditBlob");
        eRCAuditBlobTable.newBlobColumn("BLOB_VALUE", false);
        eRCAuditBlobTable.newIntegerColumn("ID", false);
        eRCAuditBlobTable.create();
        eRCAuditBlobTable.setPrimaryKey("ID");

        ERXMigrationTable eRCAuditTrailTable = database.newTableNamed("ERCAuditTrail");
        eRCAuditTrailTable.newStringColumn("GID", 255, true);
        eRCAuditTrailTable.newIntegerColumn("ID", false);
        eRCAuditTrailTable.newIntBooleanColumn("IS_DELETED", false);
        eRCAuditTrailTable.create();
        eRCAuditTrailTable.setPrimaryKey("ID");

        ERXMigrationTable eRCAuditTrailEntryTable = database.newTableNamed("ERCAuditTrailEntry");
        eRCAuditTrailEntryTable.newTimestampColumn("CREATED", false);
        eRCAuditTrailEntryTable.newIntegerColumn("ID", false);
        eRCAuditTrailEntryTable.newStringColumn("KEY_PATH", 100, true);
        eRCAuditTrailEntryTable.newIntegerColumn("NEW_BLOB_VALUE_ID", true);
        eRCAuditTrailEntryTable.newStringColumn("NEW_VALUES", 1000, true);
        eRCAuditTrailEntryTable.newIntegerColumn("OLD_BLOB_VALUE_ID", true);
        eRCAuditTrailEntryTable.newStringColumn("OLD_VALUES", 1000, true);
        eRCAuditTrailEntryTable.newIntegerColumn("TRAIL_ID", false);
        eRCAuditTrailEntryTable.newIntegerColumn("TYPE", true);
        eRCAuditTrailEntryTable.newStringColumn("USER_GLOBAL_ID", 255, true);
        eRCAuditTrailEntryTable.newBlobColumn("USER_INFO", true);
        eRCAuditTrailEntryTable.create();
        eRCAuditTrailEntryTable.setPrimaryKey("ID");
        eRCAuditTrailEntryTable.addForeignKey("NEW_BLOB_VALUE_ID", "ERCAuditBlob", "id");
        eRCAuditTrailEntryTable.addForeignKey("OLD_BLOB_VALUE_ID", "ERCAuditBlob", "id");
        eRCAuditTrailEntryTable.addForeignKey("TRAIL_ID", "ERCAuditTrail", "id");

        ERXMigrationTable eRCHelpTextTable = database.newTableNamed("ERCHELP_TEXT");
        eRCHelpTextTable.newIntegerColumn("ID", false);
        eRCHelpTextTable.newStringColumn("KEY_", 100, false);
        eRCHelpTextTable.newLocalizedClobColumns("VALUE_", true);
        eRCHelpTextTable.create();
        eRCHelpTextTable.setPrimaryKey("ID");

        ERXMigrationTable eRCLogEntryTable = database.newTableNamed("ERCLOG_ENTRY");
        eRCLogEntryTable.newTimestampColumn("CREATED", false);
        eRCLogEntryTable.newIntegerColumn("ID", false);
        eRCLogEntryTable.newStringColumn("TEXT_", 10000000, false);
        eRCLogEntryTable.newIntegerColumn("USER_ID", false);
        eRCLogEntryTable.create();
        eRCLogEntryTable.setPrimaryKey("ID");

        ERXMigrationTable eRCPreferenceTable = database.newTableNamed("ERCPREFER");
        eRCPreferenceTable.newIntegerColumn("ID", false);
        eRCPreferenceTable.newStringColumn("KEY_", 100, false);
        eRCPreferenceTable.newIntegerColumn("USER_ID", true);
        eRCPreferenceTable.newStringColumn("VALUE_", 10000000, true);
        eRCPreferenceTable.create();
        eRCPreferenceTable.setPrimaryKey("ID");

        ERXMigrationTable eRCStaticTable = database.newTableNamed("ERCSTATIC");
        eRCStaticTable.newIntegerColumn("ID", false);
        eRCStaticTable.newStringColumn("KEY_", 100, false);
        eRCStaticTable.newStringColumn("VALUE_", 1000, true);
        eRCStaticTable.create();
        eRCStaticTable.setPrimaryKey("ID");
    }
}