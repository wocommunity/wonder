package er.taggable.migrations;

import com.webobjects.eocontrol.EOEditingContext;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationDatabase.Migration;
import er.extensions.migration.ERXMigrationTable;

/**
 * ERTaggable0 creates the ERTag table.
 * 
 * @author mschrag
 */
public class ERTaggable0 extends Migration {

  @Override
  public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    database.existingTableNamed("ERTag").drop();
  }

  @Override
  public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    ERXMigrationTable tagTable = database.newTableNamed("ERTag");
    tagTable.newIntegerColumn("id", false);
    tagTable.newStringColumn("name", 255, false);
    tagTable.create();
    tagTable.setPrimaryKey("id");
    tagTable.addUniqueIndex("uniqueTagName", "name", 255);
  }
}
