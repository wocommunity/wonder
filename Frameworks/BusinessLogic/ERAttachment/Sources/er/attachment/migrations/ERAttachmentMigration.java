package er.attachment.migrations;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXMigrationDatabase.Migration;

/**
 * Provides a base class for a migration that adds an attachment to an
 * existing table.
 * 
 * @author mschrag
 */
public abstract class ERAttachmentMigration extends Migration {
  private String _tableName;
  private String _columnName;
  private boolean _allowsNull;

  /**
   * Construct an ERAttachmentMigration.
   * 
   * @param tableName the name of the table to add an attachment to
   * @param columnName the name of the attachment foreign key column 
   * @param allowsNull whether or not the attachment is allowed to be null
   */
  public ERAttachmentMigration(String tableName, String columnName, boolean allowsNull) {
    _tableName = tableName;
    _columnName = columnName;
    _allowsNull = allowsNull;
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
