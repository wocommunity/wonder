package er.attachment.migrations;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;

public class ERAttachment3 extends ERXMigrationDatabase.Migration {

	private static final String ER_ATTACHMENT_TABLE_NAME = "ERAttachment";
	private static final String CF_PATH_COLUMN_NAME = "cfPath";

	@Override
	public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
		//TODO maybe require a property set here or crash so no one accidentally upgrades to data loss?
		ERXMigrationTable attachmentTable = database.existingTableNamed(ERAttachment3.ER_ATTACHMENT_TABLE_NAME);
		attachmentTable.existingColumnNamed(CF_PATH_COLUMN_NAME).delete();

	}

	@Override
	public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
		ERXMigrationTable attachmentTable = database.existingTableNamed(ERAttachment3.ER_ATTACHMENT_TABLE_NAME);
		attachmentTable.newStringColumn(CF_PATH_COLUMN_NAME, 1000, true);

	}

}
