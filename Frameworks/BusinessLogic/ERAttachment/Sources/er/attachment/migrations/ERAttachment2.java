
package er.attachment.migrations;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;

public class ERAttachment2 extends
                          ERXMigrationDatabase.Migration {

	private static final String ER_ATTACHMENT_TABLE_NAME = "ERAttachment";
	private static final String CF_PATH_COLUMN_NAME = "cfPath";


	@Override
    public void downgrade(EOEditingContext editingContext,
                          ERXMigrationDatabase database) throws Throwable {
		ERXMigrationTable attachmentTable = database.existingTableNamed(ERAttachment2.ER_ATTACHMENT_TABLE_NAME);
		attachmentTable.existingColumnNamed(CF_PATH_COLUMN_NAME).delete();
	    
    }


	@Override
	public void upgrade(EOEditingContext editingContext,
	                    ERXMigrationDatabase database) throws Throwable {
		ERXMigrationTable attachmentTable = database.existingTableNamed(ERAttachment2.ER_ATTACHMENT_TABLE_NAME);
		attachmentTable.newStringColumn(CF_PATH_COLUMN_NAME, 1000, true);

	}

}
