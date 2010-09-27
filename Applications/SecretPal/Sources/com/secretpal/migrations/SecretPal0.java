package com.secretpal.migrations;

import com.secretpal.model.SPEvent;
import com.secretpal.model.SPGroup;
import com.secretpal.model.SPMembership;
import com.secretpal.model.SPPerson;
import com.secretpal.model.SPSecretPal;
import com.secretpal.model.SPWish;
import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;
import er.extensions.migration.IERXPostMigration;

public class SecretPal0 extends ERXMigrationDatabase.Migration implements IERXPostMigration {
	@Override
	public NSArray<ERXModelVersion> modelDependencies() {
		return null;
	}

	@Override
	public void downgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
		// DO NOTHING
	}

	@Override
	public void upgrade(EOEditingContext editingContext, ERXMigrationDatabase database) throws Throwable {
		ERXMigrationTable sPGroupTable = database.newTableNamed("SPGroup");
		sPGroupTable.newStringColumn("description", 1000, true);
		sPGroupTable.newStringColumn("groupPassword", 100, true);
		sPGroupTable.newIntegerColumn("id", false);
		sPGroupTable.newStringColumn("name", 255, false);
		sPGroupTable.newIntegerColumn("ownerID", false);
		sPGroupTable.create();
	 	sPGroupTable.setPrimaryKey("id");

		ERXMigrationTable sPPersonTable = database.newTableNamed("SPPerson");
		sPPersonTable.newBooleanColumn("admin", false);
		sPPersonTable.newIntegerColumn("avatarID", true);
		sPPersonTable.newStringColumn("emailAddress", 255, false);
		sPPersonTable.newBooleanColumn("emailDeliveryFailure", false);
		sPPersonTable.newIntegerColumn("id", false);
		sPPersonTable.newStringColumn("name", 255, false);
		sPPersonTable.newStringColumn("password", 100, true);
		sPPersonTable.create();
	 	sPPersonTable.setPrimaryKey("id");

		ERXMigrationTable sPSecretPalTable = database.newTableNamed("SPSecretPal");
		sPSecretPalTable.newIntegerColumn("eventID", false);
		sPSecretPalTable.newIntegerColumn("giverID", false);
		sPSecretPalTable.newIntegerColumn("id", false);
		sPSecretPalTable.newIntegerColumn("receiverID", false);
		sPSecretPalTable.create();
	 	sPSecretPalTable.setPrimaryKey("id");

		ERXMigrationTable sPWishTable = database.newTableNamed("SPWish");
		sPWishTable.newBigDecimalColumn("cost", 38, 4, true);
		sPWishTable.newStringColumn("description", 1000, true);
		sPWishTable.newIntegerColumn("id", false);
		sPWishTable.newBooleanColumn("purchased", false);
		sPWishTable.newIntegerColumn("suggestedByID", false);
		sPWishTable.newIntegerColumn("suggestedForID", false);
		sPWishTable.create();
	 	sPWishTable.setPrimaryKey("id");

		ERXMigrationTable sPMembershipTable = database.newTableNamed("SPMembership");
		sPMembershipTable.newBooleanColumn("admin", false);
		sPMembershipTable.newIntegerColumn("groupID", false);
		sPMembershipTable.newIntegerColumn("personID", false);
		sPMembershipTable.newStringColumn("confirmationCode", 100, true);
		sPMembershipTable.newBooleanColumn("confirmed", false);
		sPMembershipTable.create();
	 	sPMembershipTable.setPrimaryKey("groupID", "personID");

		ERXMigrationTable sPEventTable = database.newTableNamed("SPEvent");
		sPEventTable.newBooleanColumn("active", false);
		sPEventTable.newStringColumn("description", 1000, true);
		sPEventTable.newIntegerColumn("groupID", false);
		sPEventTable.newIntegerColumn("id", false);
		sPEventTable.newStringColumn("name", 255, false);
		sPEventTable.create();
	 	sPEventTable.setPrimaryKey("id");

		sPGroupTable.addForeignKey("ownerID", "SPPerson", "id");
		sPPersonTable.addForeignKey("avatarID", "ERAttachment", "id");
		sPSecretPalTable.addForeignKey("eventID", "SPEvent", "id");
		sPSecretPalTable.addForeignKey("giverID", "SPPerson", "id");
		sPSecretPalTable.addForeignKey("receiverID", "SPPerson", "id");
		sPWishTable.addForeignKey("suggestedByID", "SPPerson", "id");
		sPWishTable.addForeignKey("suggestedForID", "SPPerson", "id");
		sPMembershipTable.addForeignKey("groupID", "SPGroup", "id");
		sPMembershipTable.addForeignKey("personID", "SPPerson", "id");
		sPEventTable.addForeignKey("groupID", "SPGroup", "id");
		
		sPPersonTable.addUniqueIndex("uniqueEmailAddress", "emailAddress");
	
		sPSecretPalTable.addUniqueIndex("uniqueSecretPal", sPSecretPalTable.existingColumnNamed("eventID"), sPSecretPalTable.existingColumnNamed("giverID"), sPSecretPalTable.existingColumnNamed("receiverID"));
	}

	public void postUpgrade(EOEditingContext editingContext, EOModel model) throws Throwable {
		SPPerson admin = SPPerson.createSPPerson(editingContext, Boolean.TRUE, "admin@secretpal.com", Boolean.FALSE, "John Administrator");
		admin.setPlainTextPassword("adminadmin");
		
		SPPerson gary = SPPerson.createSPPerson(editingContext, Boolean.FALSE, "test1@secretpal.com", Boolean.FALSE, "Robert Test");
		gary.setPlainTextPassword("test1");
		
		SPPerson mary = SPPerson.createSPPerson(editingContext, Boolean.FALSE, "test2@secretpal.com", Boolean.FALSE, "Mary Example");
		mary.setPassword(null);
		
		SPGroup testGroup = SPGroup.createSPGroup(editingContext, "The Example Crew", admin);
		testGroup.setDescription("This is the example group for testing Secret Pal.");
		SPMembership.createSPMembership(editingContext, Boolean.TRUE, Boolean.TRUE, testGroup, admin);
		SPMembership.createSPMembership(editingContext, Boolean.FALSE, Boolean.TRUE, testGroup, gary);
		SPMembership.createSPMembership(editingContext, Boolean.FALSE, Boolean.FALSE, testGroup, mary);
		SPEvent testEvent = SPEvent.createSPEvent(editingContext, Boolean.TRUE, "Christmas 2010", testGroup);
		testEvent.setDescription("It's christmas time! Get your secret pal something nice!");
		SPSecretPal.createSPSecretPal(editingContext, testEvent, admin, gary);
		SPSecretPal.createSPSecretPal(editingContext, testEvent, mary, admin);
		//SPSecretPal.createSPSecretPal(editingContext, testEvent, gary, mary);
		SPWish.createSPWish(editingContext, Boolean.TRUE, admin, admin).setDescription("a pony for riding");
		SPWish.createSPWish(editingContext, Boolean.FALSE, admin, admin).setDescription("a race car that goes really fast");
		SPWish.createSPWish(editingContext, Boolean.FALSE, mary, admin).setDescription("he wants lots of food");
		SPWish.createSPWish(editingContext, Boolean.FALSE, mary, admin).setDescription("a dog!");
		SPWish.createSPWish(editingContext, Boolean.FALSE, mary, mary).setDescription("things for making food");
		SPWish.createSPWish(editingContext, Boolean.TRUE, mary, gary).setDescription("a pony");
		SPWish.createSPWish(editingContext, Boolean.FALSE, admin, admin).setDescription("a pony");
		SPWish.createSPWish(editingContext, Boolean.FALSE, gary, gary).setDescription("more stuff");
		SPWish.createSPWish(editingContext, Boolean.TRUE, gary, gary).setDescription("a magazine to put in the trash");
		SPWish.createSPWish(editingContext, Boolean.FALSE, admin, gary).setDescription("televisions made of gold");
		SPWish.createSPWish(editingContext, Boolean.FALSE, gary, admin).setDescription("the world's biggest ball of twine");
	}
}