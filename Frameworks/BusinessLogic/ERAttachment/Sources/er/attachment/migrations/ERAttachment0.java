package er.attachment.migrations;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.foundation.ERXProperties;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;

/**
 * <span class="en">
 * Performs the initial database table creation.
 * </span>
 * 
 * <span class="ja">
 * データベース・テーブル作成の実行
 * </span>
 * 
 * @property er.extensions.ERXModelGroup.ERAttachment.size.columnName
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
    attachmentTable.newIntegerColumn(ERXProperties.stringForKeyWithDefault("er.extensions.ERXModelGroup.ERAttachment.size.columnName", "size"), false);
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
