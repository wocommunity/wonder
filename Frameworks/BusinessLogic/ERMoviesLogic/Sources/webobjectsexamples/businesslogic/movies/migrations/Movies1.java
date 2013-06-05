package webobjectsexamples.businesslogic.movies.migrations;

import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.attachment.migrations.ERAttachmentMigration;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXModelVersion;

public class Movies1 extends ERAttachmentMigration {
	
	public Movies1() {
		super("movie", "poster_AttachmentID", true);
	}

	@Override
	public NSArray<ERXModelVersion> modelDependencies() {
		return null;
	}
  
	@Override
	public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
		// DO NOTHING
	}

}
