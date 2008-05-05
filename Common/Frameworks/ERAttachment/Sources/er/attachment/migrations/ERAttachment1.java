package er.attachment.migrations;

import java.util.TimeZone;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSTimestamp;

import er.extensions.migration.ERXMigrationDatabase;

/**
 * Add creation date and available boolean.
 * 
 * @author mschrag
 */
public class ERAttachment1 extends ERXMigrationDatabase.Migration {
  @Override
  public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    database.existingTableNamed("ERAttachment").existingColumnNamed("available").delete();
    database.existingTableNamed("ERAttachment").existingColumnNamed("creationDate").delete();
  }

  @Override
  public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
    database.existingTableNamed("ERAttachment").newBooleanColumn("available", false, Boolean.TRUE);
    database.existingTableNamed("ERAttachment").newTimestampColumn("creationDate", false, new NSTimestamp(2007, 8, 1, 0, 0, 0, TimeZone.getDefault()));
  }
}
