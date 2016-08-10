package er.attachment.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationDatabase.Migration;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;

/**
 * <span class="en">
 * Provides a base class for a migration that adds an attachment to an
 * existing table.
 * </span>
 * 
 * <span class="ja">
 * マイグレションの為のベース・クラスを提供しています。
 * 存在テーブルにアタッチメントを追加します。
 * </span>　
 * 
 * @author mschrag
 */
public abstract class ERAttachmentMigration extends Migration {
  private String _tableName;
  private String _columnName;
  private boolean _allowsNull;

  /**
   * <span class="en">
   * Construct an ERAttachmentMigration.
   * 
   * @param tableName the name of the table to add an attachment to
   * @param columnName the name of the attachment foreign key column 
   * @param allowsNull whether or not the attachment is allowed to be null
   * </span>
   * 
   * <span class="ja">
   * @param tableName - アタッチメントを追加するテーブル名
   * @param columnName - アタッチメント外部キーの名前
   * @param allowsNull - アタッチメントが null 可能かどうか
   * </span>
   */
  public ERAttachmentMigration(String tableName, String columnName, boolean allowsNull) {
    _tableName = tableName;
    _columnName = columnName;
    _allowsNull = allowsNull;
  }
  
  @Override
  public NSArray<ERXModelVersion> modelDependencies() {
  	return new NSArray<ERXModelVersion>(new ERXModelVersion("ERAttachment", 1));
  }

  @Override
  public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    database.existingTableNamed(_tableName).existingColumnNamed(_columnName).delete();
  }

  @Override
  public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    ERXMigrationTable table = database.existingTableNamed(_tableName);
    table.newIntegerColumn(_columnName, _allowsNull);
    table.addForeignKey(_columnName, database.existingTableNamed("ERAttachment").existingColumnNamed("id"));
  }

}
