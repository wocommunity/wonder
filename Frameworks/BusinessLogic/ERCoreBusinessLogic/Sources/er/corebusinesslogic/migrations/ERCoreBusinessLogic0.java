package er.corebusinesslogic.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXProperties;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

/**
 *
 * @property ERCoreBusinessLogic0.languages
 */
public class ERCoreBusinessLogic0 extends ERXMigrationDatabase.Migration {
    
    public ERCoreBusinessLogic0() {
       super(ERXProperties.arrayForKey("ERCoreBusinessLogic0.languages"));
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
        eRCAuditBlobTable.newBlobColumn("BLOB_VALUE", NOT_NULL);
        eRCAuditBlobTable.newIntegerColumn("ID", NOT_NULL);
        eRCAuditBlobTable.create();
        eRCAuditBlobTable.setPrimaryKey("ID");

        ERXMigrationTable eRCAuditTrailTable = database.newTableNamed("ERCAuditTrail");
        eRCAuditTrailTable.newStringColumn("GID", 255, ALLOWS_NULL);
        eRCAuditTrailTable.newIntegerColumn("ID", NOT_NULL);
        eRCAuditTrailTable.newIntBooleanColumn("IS_DELETED", NOT_NULL);
        eRCAuditTrailTable.create();
        eRCAuditTrailTable.setPrimaryKey("ID");

        ERXMigrationTable eRCAuditTrailEntryTable = database.newTableNamed("ERCAuditTrailEntry");
        eRCAuditTrailEntryTable.newTimestampColumn("CREATED", NOT_NULL);
        eRCAuditTrailEntryTable.newIntegerColumn("ID", NOT_NULL);
        eRCAuditTrailEntryTable.newStringColumn("KEY_PATH", 100, ALLOWS_NULL);
        eRCAuditTrailEntryTable.newIntegerColumn("NEW_BLOB_VALUE_ID", ALLOWS_NULL);
        eRCAuditTrailEntryTable.newStringColumn("NEW_VALUES", 1000, ALLOWS_NULL);
        eRCAuditTrailEntryTable.newIntegerColumn("OLD_BLOB_VALUE_ID", ALLOWS_NULL);
        eRCAuditTrailEntryTable.newStringColumn("OLD_VALUES", 1000, ALLOWS_NULL);
        eRCAuditTrailEntryTable.newIntegerColumn("TRAIL_ID", NOT_NULL);
        eRCAuditTrailEntryTable.newStringColumn("TYPE", 50, NOT_NULL);
        eRCAuditTrailEntryTable.newStringColumn("USER_GLOBAL_ID", 255, ALLOWS_NULL);
        eRCAuditTrailEntryTable.newBlobColumn("USER_INFO", ALLOWS_NULL);
        eRCAuditTrailEntryTable.create();
        eRCAuditTrailEntryTable.setPrimaryKey("ID");
        eRCAuditTrailEntryTable.addForeignKey("NEW_BLOB_VALUE_ID", "ERCAuditBlob", "id");
        eRCAuditTrailEntryTable.addForeignKey("OLD_BLOB_VALUE_ID", "ERCAuditBlob", "id");
        eRCAuditTrailEntryTable.addForeignKey("TRAIL_ID", "ERCAuditTrail", "id");

        ERXMigrationTable eRCHelpTextTable = database.newTableNamed("ERCHELP_TEXT");
        eRCHelpTextTable.newIntegerColumn("ID", NOT_NULL);
        eRCHelpTextTable.newStringColumn("KEY_", 100, NOT_NULL);
        eRCHelpTextTable.newLocalizedClobColumns("VALUE_", ALLOWS_NULL);
        eRCHelpTextTable.create();
        eRCHelpTextTable.setPrimaryKey("ID");

        ERXMigrationTable eRCLogEntryTable = database.newTableNamed("ERCLOG_ENTRY");
        eRCLogEntryTable.newTimestampColumn("CREATED", NOT_NULL);
        eRCLogEntryTable.newIntegerColumn("ID", NOT_NULL);
        eRCLogEntryTable.newClobColumn("TEXT_", NOT_NULL);
        eRCLogEntryTable.newIntegerColumn("USER_ID", NOT_NULL);
        eRCLogEntryTable.create();
        eRCLogEntryTable.setPrimaryKey("ID");

        ERXMigrationTable eRCPreferenceTable = database.newTableNamed("ERCPREFER");
        eRCPreferenceTable.newIntegerColumn("ID", NOT_NULL);
        eRCPreferenceTable.newStringColumn("KEY_", 100, NOT_NULL);
        eRCPreferenceTable.newIntegerColumn("USER_ID", ALLOWS_NULL);
        eRCPreferenceTable.newClobColumn("VALUE_", ALLOWS_NULL);
        eRCPreferenceTable.create();
        eRCPreferenceTable.setPrimaryKey("ID");

        ERXMigrationTable eRCStaticTable = database.newTableNamed("ERCSTATIC");
        eRCStaticTable.newIntegerColumn("ID", NOT_NULL);
        eRCStaticTable.newStringColumn("KEY_", 100, NOT_NULL);
        eRCStaticTable.newStringColumn("VALUE_", 1000, ALLOWS_NULL);
        eRCStaticTable.create();
        eRCStaticTable.setPrimaryKey("ID");
    }
}
