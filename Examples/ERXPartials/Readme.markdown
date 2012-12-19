# ERXPartials Test / Example

## Notes about ERXPartials

### Sponsored By

Work on this package was partly funded by [Logic Squad][logicsquad].


### Design Overview

1. `ERXPartialBaseModel` contains a very basic model containing the Root entity
for the partials named **Person**.
	- contains one standard relationship to **GenderType**
	- contains a standard migration to create the initial tables for the
	**Person** and **GenderType**
	- the migration also preloads some default values for gender and 100
	semi-random Person records.

2. `ERXPartialExampleModel` contains additional extensions to the **Person**
entity.  The design uses two partial entities.
	- one partial entity **Partial_AuthenticatedPerson** stores the basic
	authenticated user details *username*, *password* and *lastLoginDate*
	- the second entity **Partial_EmployeePerson** stores common employee
	details like *department*, *employeeType*, *salary* and *employeeNumber*
	- **Company**, **Department** and **EmployeeType** provide additional
	relationships for the **Partial_EmployeePerson**
	- the migration in this frameworks creates the supporting tables in a normal
	fashion, and adds the additional attributes and constraints to the existing
	**Person** table.
 
3. `ERXPartialExampleApp` is a simple D2W demo application to demonstrate and
test the Partials in a live environment.


### Generated Partial entity Templates

By default I was using the `_WonderEntity.java` template, however there was a
small issue with the generated content.  When the template created a to-Many
accessor method, it was using the relationship *actual destination* entity to
generate the appropriate key path.  However when the destination is a ERXPartial
entity then the *actual destination* is the Root Entity.  In this example the
Department relationship to the **Partial_EmployeePerson** was generated as

#### Bad
	  public NSArray<er.example.erxpartials.model.Person> partial_EmployeePersons(EOQualifier qualifier,
	    NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
		  ...
          EOQualifier inverseQualifier = new EOKeyValueQualifier(er.example.erxpartials.model.Person.DEPARTMENT_KEY,
            EOQualifier.QualifierOperatorEqual, this);

#### Should be
	  public NSArray<er.example.erxpartials.model.Person> partial_EmployeePersons(EOQualifier qualifier,
	    NSArray<EOSortOrdering> sortOrderings, boolean fetch) {
	      ...
	      EOQualifier inverseQualifier = new EOKeyValueQualifier(er.example.erxpartials.model.Partial_EmployeePerson.DEPARTMENT_KEY,
	        EOQualifier.QualifierOperatorEqual, this);

A patched `_PartialWonderEntity.java` is included int the Resources of the
`ERXPartialExampleModel` project.


### Validation

The original behaviour for validation forwards ***all*** messages from the
ERXPartial object to ***all*** contained partials.  The problem is that only
***one*** of this partials will correctly validate the specified key path, the
other partials will throw exceptions for unbound keys.

Additionally when the `ERXPartialInitializer` merges the partial entities into
the Root, the `EOEntities` for the partials are removed from the `EOModel`, so
it is not possible to query the original partial entity to determine if the key
path is valid for that partial object.

My solution is currently to generate a static array for the attributes and the
relationships and have the `ERXPartialGenericRecord` query the partial to
determine the validity of a given key path before forwarding the invocation.

Patches for this are included in the `ERXPartial`, `ERXPartialGenericObject`
classes and support is also added to the `_PartialWonderEntity.java` template.


## Getting Started

The example application (`ERXPartialsExampleApp`) can be run against any
database. As shipped, it runs on an in-memory H2 instance. It has also been
confirmed to work using FrontBase and PostgreSQL.

### [H2][h2]

Out of the box, `ERXPartialsExampleApp` runs on a private, in-memory H2
instance. This is for simplicity, and just to prove that it works—you can run
the application immediately, log in, and browse the entities. The interesting
part, though, is examining the entities that are created, and the `Person` table
in particular. For this, you need to persist the database, or at least allow
multiple simultaneous connections to the in-memory database. There are a couple
of options for doing this with H2.

#### Using a shared in-memory database

You can use a named in-memory database in server mode, but you need to start it
up _before_ you start the application. Using a connection URL like:

    jdbc:h2:tcp://localhost/mem:ERXPartials

is fine, but you can't start `ERXPartialsExampleApp` first. Use the built-in H2
viewer application from wherever you have `H2PlugIn.framework` installed:

    java -jar H2PlugIn.framework/Resources/Java/h2-*.jar

Give it the connection URL, and the username `h2user`. This will start up the
in-memory database and the TCP-based server in the right order. You can then
start `ERXPartialsExampleApp`, refresh the viewer, and view the tables created.

#### Using a file-based database

Alternatively, you can use a file-based database, with a connection URL like
this:

    jdbc:h2:file:~/ERXPartials
    
Run `ERXPartialsExampleApp`, wait until migrations have run, quit the
application and then use the H2 viewer application as above with the file URL.

### [FrontBase][frontbase]

Create the database using the Ant target to create the empty database and set
the ownership permissions. Change into the `ERXPartialBaseModel` directory, and
run `ant fb.recreate`:

<pre><code>$ ant fb.recreate
Buildfile: /Volumes/Data/Development/GitHub/ERXPartials/wonder/Tests/ERXPartials/ERXPartialBaseModel/build.xml

init.properties:

fb.recreate:

  [exec] connect to ERXPartials user _system;  
  [exec] Cannot connect to ERXPartials@localhost  
  [exec] Database is not running  
  [exec] stop database;  
  [exec] No current session.  
  [exec] delete database ERXPartials;  
  [exec] Cannot delete database ERXPartials@localhost;  
  [exec] Reason: Database is unknown  
  [exec] create database ERXPartials;  
  [exec] connect to ERXPartials user _system;  
  [exec] Auto committing is on: SET COMMIT TRUE;  
  [exec] create user erxpartial;  
  [exec] set password test user erxpartial;  
  [exec] create schema erxpartial authorization erxpartial;  
  [exec] disconnect all;  
  [exec]  

BUILD SUCCESSFUL

Total time: 1 second</code></pre>

Alter the connection properties in `ERXPartialsExampleApp/Resources/Properties`
as appropriate:

    dbConnectURLGLOBAL=jdbc:FrontBase://localhost/ERXPartials
    dbConnectUserGLOBAL=erxpartial
    dbConnectPasswordGLOBAL=test

Add the `FrontBasePlugIn.framework` to the application's build path
(right-click on application, Build Path > Configure Build Path... > Libraries >
Add Library... > WebObjects Frameworks > Next, then check FrontBasePlugIn and
click Finish.

Compile and run the `ERXPartialsExampleApp`. The main page is a login page, but
there is no login logic, just click the Login button.


### [PostgreSQL][pgsql]

Create the database.

    $ createdb -U postgres -E UTF8 ERXPartials

Alter the connection properties in `ERXPartialsExampleApp/Resources/Properties`
as appropriate:

    dbConnectURLGLOBAL=jdbc:postgresql://localhost/ERXPartials
    dbConnectUserGLOBAL=postgres
    dbConnectPasswordGLOBAL=
    dbConnectPluginGLOBAL=PostgresqlPlugIn
    dbConnectDriverGLOBAL=org.postgresql.Driver

Add the `PostgresqlPlugIn.framework` to the application's build path
(right-click on application, Build Path > Configure Build Path... > Libraries >
Add Library... > WebObjects Frameworks > Next, then check PostgresqlPlugIn and
click Finish.

Compile and run the `ERXPartialsExampleApp`. The main page is a login page, but
there is no login logic, just click the Login button.


----
David Aspinall  
Senior IT Consultant  
[Global Village Consulting Inc.][gvc]

Paul Hoadley  
[Logic Squad][logicsquad]

[gvc]: http://www.global-village.net
[logicsquad]: http://logicsquad.net/
[h2]: http://www.h2database.com/
[frontbase]: http://www.frontbase.com/
[pgsql]: http://postgresql.org/
