package webobjectsexamples.businesslogic.movies.migrations;

import com.webobjects.eocontrol.EOEditingContext;

import er.attachment.migrations.ERAttachmentMigration;
import er.extensions.migration.ERXMigrationDatabase;

public class Movies1 extends ERAttachmentMigration {
	
	public Movies1() {
		super("movie", "poster_AttachmentID", true);
	}

	@Override
	public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
		// DO NOTHING
	}

}
