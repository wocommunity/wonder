package er.rest.example.migrations;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;
import er.extensions.migration.IERXPostMigration;
import er.rest.example.model.Animal;
import er.rest.example.model.Company;
import er.rest.example.model.Person;

/**
 * Migrations, though the default example runs with a Memory adaptor.
 * 
 * @author mschrag
 */
public class RestExample0 extends ERXMigrationDatabase.Migration implements IERXPostMigration {
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
		ERXMigrationTable personTable = database.newTableNamed("Person");
		personTable.newIntegerColumn("companyID", false);
		personTable.newIntegerColumn("id", false);
		personTable.newStringColumn("name", 255, false);
		personTable.create();
		personTable.setPrimaryKey("id");

		ERXMigrationTable companyTable = database.newTableNamed("Company");
		companyTable.newIntegerColumn("id", false);
		companyTable.newStringColumn("name", 255, false);
		companyTable.create();
		companyTable.setPrimaryKey("id");

		ERXMigrationTable petTable = database.newTableNamed("Pet");
		petTable.newIntegerColumn("id", false);
		petTable.newStringColumn("name", 255, false);
		petTable.newIntegerColumn("ownerID", false);
		petTable.create();
		petTable.setPrimaryKey("id");

		personTable.addForeignKey("companyID", "Company", "id");
		petTable.addForeignKey("ownerID", "Person", "id");
	}

	public void postUpgrade(EOEditingContext editingContext, EOModel model) throws Throwable {
		Company c1 = Company.createCompany(editingContext, "mDT");
		Company c2 = Company.createCompany(editingContext, "Apple");
		Company c3 = Company.createCompany(editingContext, "Microsoft");
		Person p1 = Person.createPerson(editingContext, "Mike", c1);
		Animal a1 = Animal.createAnimal(editingContext, "Derby", p1);
		Animal a2 = Animal.createAnimal(editingContext, "Sydney", p1);
		Person p2 = Person.createPerson(editingContext, "Adam", c1);
		Animal a3 = Animal.createAnimal(editingContext, "Franny", p2);
	}
}