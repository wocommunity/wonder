package er.example.erxpartials.migration;

import com.webobjects.eoaccess.EOModel;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.foundation.NSArray;

import er.example.erxpartials.model.GenderType;
import er.example.erxpartials.model.Person;
import er.extensions.migration.ERXMigrationDatabase;
import er.extensions.migration.ERXMigrationTable;
import er.extensions.migration.ERXModelVersion;
import er.extensions.migration.IERXPostMigration;

public class ERXPartialBase0 extends ERXMigrationDatabase.Migration implements IERXPostMigration {
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
		ERXMigrationTable genderTypeTable = database.newTableNamed("GenderType");
		genderTypeTable.newIntegerColumn("id", false);
		genderTypeTable.newStringColumn("name", 255, false);
		genderTypeTable.create();
	 	genderTypeTable.setPrimaryKey("id");

		ERXMigrationTable personTable = database.newTableNamed("Person");
		personTable.newStringColumn("firstName", 255, false);
		personTable.newIntegerColumn("genderTypeID", false);
		personTable.newIntegerColumn("id", false);
		personTable.newStringColumn("lastName", 255, false);
		personTable.create();
	 	personTable.setPrimaryKey("id");

		personTable.addForeignKey("genderTypeID", "GenderType", "id");
  }

  public void postUpgrade(EOEditingContext ec, EOModel model) throws Throwable {
	  GenderType female = GenderType.createGenderType(ec, "Female");
	  GenderType male = GenderType.createGenderType(ec, "Male");
	  ec.saveChanges();
	  
	  NSArray<String> maleFirstName = new NSArray<String>(new String[] {"David", "Stephen", "Frank", "John", "Edward"});
	  NSArray<String> femaleFirstName = new NSArray<String>(new String[] {"Sally", "Susan", "Linda", "Jane", "Ellen"});
	  
	  NSArray<String> lastNames = new NSArray<String>(new String[] {"Smith", "Jones", "Parsons", "Hand", "Best", "Jobs", "Cooke", "White", "Brown", "Crowe"});

	  for ( String name : maleFirstName) {
		  for ( String surname : lastNames) {
			  Person.createPerson(ec, name, surname, male);
		  }
	  }
	  ec.saveChanges();

	  for ( String name : femaleFirstName) {
		  for ( String surname : lastNames) {
			  Person.createPerson(ec, name, surname, female);
		  }
	  }
	  ec.saveChanges();
}
  
}
