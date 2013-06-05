package er.corebusinesslogic.migrations;

import java.sql.Types;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.foundation.ERXProperties;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

/**
 *
 * @property ERCoreBusinessLogic1.languages
 */
public class ERCoreBusinessLogic2 extends ERXMigrationDatabase.Migration {
    
    public ERCoreBusinessLogic2() {
       super(ERXProperties.arrayForKey("ERCoreBusinessLogic1.languages"));
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
        ERXMigrationTable ercMailMessageTable = database.existingTableNamed("ERCMAIL_MESSAG");
        ercMailMessageTable.existingColumnNamed("BCC_ADDR").setWidthType(Types.VARCHAR, 10000000, null);
        ercMailMessageTable.existingColumnNamed("CC_ADDR").setWidthType(Types.VARCHAR, 10000000, null);
        ercMailMessageTable.existingColumnNamed("TO_ADDR").setWidthType(Types.VARCHAR, 10000000, null);

        ERXMigrationTable ercMailMessageArchiveTable = database.existingTableNamed("ERCMAIL_MESSAG_ARCHIVE");
        ercMailMessageArchiveTable.existingColumnNamed("BCC_ADDR").setWidthType(Types.VARCHAR, 10000000, null);
        ercMailMessageArchiveTable.existingColumnNamed("CC_ADDR").setWidthType(Types.VARCHAR, 10000000, null);
        ercMailMessageArchiveTable.existingColumnNamed("TO_ADDR").setWidthType(Types.VARCHAR, 10000000, null);
    }

}