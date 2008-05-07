package er.attachment.migrations;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;

/**
 * Performs the initial database table creation.
 * 
 * @author mschrag
 */
public class ERAttachment0 extends ERXMigrationDatabase.Migration {
  @Override
  public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    database.existingTableNamed("ERAttachment").drop();
    database.existingTableNamed("ERAttachmentData").drop();
  }

  @Override
  public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    ERXMigrationTable attachmentTable = database.newTableNamed("ERAttachment");
    attachmentTable.newIntegerColumn("height", true);
    attachmentTable.newIntegerColumn("id", false);
    attachmentTable.newStringColumn("mimeType", 100, false);
    attachmentTable.newStringColumn("configurationName", 100, true);
    attachmentTable.newStringColumn("ownerID", 16, true);
    attachmentTable.newStringColumn("originalFileName", 255, false);
    attachmentTable.newIntegerColumn("parentID", true);
    attachmentTable.newStringColumn("proxied", 5, false);
    attachmentTable.newIntegerColumn("size", false);
    attachmentTable.newStringColumn("storageType", 10, true);
    attachmentTable.newBlobColumn("smallData", true);
    attachmentTable.newStringColumn("thumbnail", 10, true);
    attachmentTable.newStringColumn("webPath", 1000, false);
    attachmentTable.newIntegerColumn("width", true);
    attachmentTable.newIntegerColumn("attachmentDataID", true);
    attachmentTable.newStringColumn("filesystemPath", 255, true);
    attachmentTable.newStringColumn("s3Path", 1000, true);
    attachmentTable.create();
    attachmentTable.setPrimaryKey("id");
    attachmentTable.addUniqueIndex("ERAttachmentWebPath", "webPath", 1000);

    ERXMigrationTable attachmentDataTable = database.newTableNamed("ERAttachmentData");
    attachmentDataTable.newBlobColumn("data", true);
    attachmentDataTable.newIntegerColumn("id", false);
    attachmentDataTable.create();
    attachmentDataTable.setPrimaryKey("id");

    attachmentTable.addForeignKey("attachmentDataID", "ERAttachmentData", "id");
  }
}
