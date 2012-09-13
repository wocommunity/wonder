# ERXPartials Test / Example

## Notes about ERXPartials

### Design Overview

1. `ERXPartialBaseModel` contains a very basic model containing the Root entity for the partials named **Person**.
	- contains one standard relationship to **GenderType**
	- contains a standard migration to create the initial tables for the **Person** and **GenderType**
	- the migration also preloads some default values for gender and 100 semi-random Person records.

2. `ERXPartialExampleModel` contains additional extensions to the **Person** entity.  The design uses two partial entities.
	- one partial entity **Partial_AuthenticatedPerson** stores the basic authenticated user details *username*, *password* and *lastLoginDate*
	- the second entity **Partial_EmployeePerson** stores common employee details like *department*, *employeeType*, *salary* and *employeeNumber*
	- **Company**, **Department** and **EmployeeType** provide additional relationships for the **Partial_EmployeePerson**
	- the migration in this frameworks creates the supporting tables in a normal fashion, and adds the additional attributes and constraints to the existing **Person** table.
 
2. `ERXPartialExampleApp` is a simple D2W demo application to demonstrate and test the Partials in a live environment.




### Generated Partial entity Templates

By default I was using the `_WonderEntity.java` template, however there was a small issue with the generated content.  When the template created a to-Many accessor method, it was using the relationship *actual destination* entity to generate the appropriate key path.  However when the destination is a ERXPartial entity then the *actual destination* is the Root Entity.  In this example the Department relationship to the **Partial_EmployeePerson** was generated as

#### Bad
	  public NSArray<er.example.erxpartials.model.Person> partial_EmployeePersons(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
	…
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.example.erxpartials.model.Person.DEPARTMENT_KEY, EOQualifier.QualifierOperatorEqual, this);

#### Should be
	  public NSArray<er.example.erxpartials.model.Person> partial_EmployeePersons(EOQualifier qualifier, NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
	…
      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.example.erxpartials.model.Partial_EmployeePerson.DEPARTMENT_KEY, EOQualifier.QualifierOperatorEqual, this);

A patched `_PartialWonderEntity.java` is included int the Resources of the `ERXPartialExampleModel` project.




### Validation

The original behaviour for validation forwards ***all*** messages from the ERXPartial object to ***all*** contained partials.  The problem is that only ***one*** of this partials will correctly validate the specified key path, the other partials will throw exceptions for unbound keys.

Additionally when the `ERXPartialInitializer` merges the partial entities into the Root, the `EOEntities` for the partials are removed from the `EOModel`, so it is not possible to query the original partial entity to determine if the key path is valid for that partial object.

My solution is currently to generate a static array for the attributes and the relationships and have the `ERXPartialGenericRecord` query the partial to determine the validity of a given key path before forwarding the invocation.

Patches for this are included in the `ERXPartial`, `ERXPartialGenericObject` classes and support is also added to the `_PartialWonderEntity.java` template.

## Getting Started

1. create the database
	- Change into the ERXPartialBaseModel directory

	> _GVCDev:ERXPartialBaseModel daspinall$ ant fb.recreate_
	>
	> Buildfile: /Volumes/Data/Development/GitHub/ERXPartials/wonder/Tests/ERXPartials/ERXPartialBaseModel/build.xml
	> 
	> init.properties:
	> 
	>    fb.recreate:
	>      [exec] connect to ERXPartials user _system;  
	>      [exec] Cannot connect to ERXPartials@localhost  
	>      [exec] Database is not running  
	>      [exec] stop database;  
	>      [exec] No current session.  
	>      [exec] delete database ERXPartials;  
	>      [exec] Cannot delete database ERXPartials@localhost;  
	>      [exec] Reason: Database is unknown  
	>      [exec] create database ERXPartials;  
	>      [exec] connect to ERXPartials user _system;  
	>      [exec] Auto committing is on: SET COMMIT TRUE;  
	>      [exec] create user erxpartial;  
	>      [exec] set password test user erxpartial;  
	>      [exec] create schema erxpartial authorization erxpartial;  
	>      [exec] disconnect all;  
	>      [exec]  
	>
	> BUILD SUCCESSFUL
	>
	> Total time: 1 second
	>
	> GVCDev:ERXPartialBaseModel daspinall$

2. compile and run. The main page is a login page, but there is no login logic, just click the button :)

----
David Aspinall  
Senior IT Consultant  
[Global Village Consulting Inc.][gvc]



[gvc]: http://www.global-village.net
