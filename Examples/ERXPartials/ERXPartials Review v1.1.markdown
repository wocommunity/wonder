# ERXPartials
## Enhancement Review and Plan
Work on this package was partly funded by [Logic Squad][logicsquad].
## Document Purpose

The purpose of this document is to review the current state of the `ERXPartial` code as it exists in the current Wonder trunk and to propose a plan for enhancement and verification of features.
## Review of ERXPartials

The concept for `ERXPartial`s allows for designing a single entity whose definition spans across multiple models.  Each model defines a part of the overall structure and is combined into one logical record by the `ERXPartialInitializer`.  The design supports the following features and limitations:

## Name Collisions

Partial objects may include attributes from the Root entity, especially where they are useful in defining relationships to other entities.  The `ERXPartialInitializer` will log a message but otherwise skip the duplicate definition so the consolidated EOEntity will only contain a single attribute or relationship for the indicated name.

There are several potential issues with name collisions

- Name collisions by 2 partial objects where the name is not declared on the Root will be accepted and integrated on a first loaded basis.  The assumption that 2 attributes or relationships with the same name in different partial entities/models will have the same implementation.

- A name collision where the underlying value type is different is also ignored.

- If the partial entity defines an attribute, then the initializer only checks for a collision in the consolidated entity attribute list.  However, no check is made for a name collision among the relationships.  This could lead to runtime errors depending on the types and order loaded.

- Similarly named relationships are not checked against attributes for name collisions.

## Inheritance

The `ERXPartial`s design does not specifically exclude the classical WO use of inheritance.  Generally I would consider the partial design to be an alternate design pattern using composition rather than as sub-classing.  Mixing these two forms is possible but clearly is not a technique I would recommend. Additionally one of the guiding design principals for this composition pattern is that the Root entity and all the Partial Entity(s) data fields are retrieved in a single fetch from a single table.  This philosophy would not be maintained for all types of WO inheritance.

In my opinion I do not think mixing inheritance with partials is a desirable design pattern. I think a good designer would use Composition with Partials or Inheritance, but not both in the same project.  Maybe it should be a property for

	er.extensions.partials.ERXPartialInitializer.allowsInheritance = false

When false, any hint of inheritance involving Partials is an error.

Nonetheless, the general limitations using the current design would require:

- The Root of the inheritance tree must subclass `ERXPartialGenericRecord`

- `ERXPartial`s specifically do not support inheritance.  Parent classes identified in the model for Partial Entities are ignored as the velocity engine extends

Example:
    public abstract class _TaskPerson extends ERXPartial<Person>

The remainder of this discussion will focus on Root Partial entities as used in a traditional WO/EOF inheritance design.

### Horizontal Inheritance

In simple terms, horizontal inheritance places each sub-classed entity into it own unique table.  The standard issues for horizontal inheritance remain for the Partial design, but with the following additional issues.

- If the Partial Root Entity is sub-classed, the initializer will not propagate the additional attributes and relationships to the subclass entities.

- If the Partial Root Entity is sub-classed, the migrations as defined for the Partial Entities will only target the Root Entity table, missing the subclass tables.

- If the Partial Root Entity is a leaf class in a Horizontal Inheritance tree, then the design should work (pending verification).

### Single Table Inheritance

Single table inheritance is very similar in design to the Partial design.  Each subclass will incrementally append additional columns to the root table.  Partial Entity migrations should work as expected, other issues include

- If the Partial Root Entity is sub-classed, the initializer will not propagate the additional attributes and relationships to the subclass entities.

- If the Partial Root Entity is a leaf class, then the design should work (pending verification).

### Vertical Inheritance

This design pattern only places the distinct attributes and relationships of the sub-classes in distinct tables.  I have only seen this pattern successful used against database systems that support deferred constraints like Oracle, Frontbase or Postgres.  The use of Vertical Inheritance for the Partial design should have the following characteristics.

- If the Partial Root Entity is sub-classed, the initializer will not propagate the additional attributes and relationships to the subclass entities, and in theory they should not be propagated but should instead be flattened into the subclass.

- If the Partial Root Entity is sub-classed, the migrations as defined for the Partial Entities will only target the Root Entity table.  This may not be an issue (for migrations)

- If the Partial Root Entity is a leaf class in a Vertical Inheritance tree, then the design should work although the migration should target the vertical data table not the Root Entity table (pending verification),

Given the complexity involved in these scenarios, I would recommend that inheritance from a Partial Root entity be limited to leaf nodes.

## Validation

The validation process for partial objects calls the validateValueForKey on the Root entity, which then forwards the message to each partial.  The implications of this include

- Validation must pass without exception being raised in any of the partials or the root.

- The root and the partials all have an opportunity to adjust the validated object.  For example, each of the partials has an opportunity to truncate a date-time value, or round a decimal value.

> _Note: Sept 14, 2012 - the test application uses a simple technique to track which Partial Entities support specific key bindings.  The Validation logic has been altered to query before forwarding the validation method.  Please see the Readme.markdown file for more information._

## Key Path

Unlike validation where the validateValueForKey message is propagated to all partials, the valueForKey requires the partial to be identified in the keypath.  The documentation provides the example:

	DayStartTime : WOString {
        value = person.@CalendarPerson.dayStartTime;
        dateformat = "%m/%d/%y";
	}

However, given that attribute and relationship names are unique in the Root Entity it should be possible for the Person entity in this example to automatically resolve dayStartTime without the @CalendarPerson notation.

## Direct To Web

The D2W engine utilizes the Model (and the adjustments to the model made by the initializer) along with the rule engine to present meaningful user interfaces.  There may be an issue creating D2W components for Partial Entities.  Until a thorough testing is performed I am unsure if:

- List, query, edit and other D2W tasks/subtasks/pages will perform properly?

> _Note: Sept 14, 2012 - YES they do using the Root entity._

- Can a page configuration be created for a Partial Entity?

> _Note: Sept 14, 2012 - NO, the Partial Entity is merged during initialization with the Root and only the Root remains as an EOEntity within the model at runtime._

- Can a Partial Entity be correctly identified and evaluated in the rule engine?

> _Note: Sept 14, 2012 - NO, the Partial Entity is merged during initialization with the Root and only the Root remains as an EOEntity within the model at runtime._

- Can the Root entity attributes that are not specified in the Partial still be accessed and evaluated in the rule engine.

> _Note: Sept 14, 2012 - YES they do using the Root entity._

## REST

Similar to the D2W engine, ERRest utilizes the Model to automatically perform basic REST actions.  My questions include:

- Can a Partial Entity be specified in a REST url request and be operated on using the default handlers?

Example:
	http://localhost/cgi-bin/WebObjects/ERPartialExample.woa/ra/CalendarPerson.json

> _Note: Sept 14, 2012 - NO, the Partial Entity is merged during initialization with the Root and only the Root remains as an EOEntity within the model at runtime._

- Do Partials generate a response with all attributes of the Partial and the Root entity?

> _Note: Sept 14, 2012 - YES, the Partial Entity is merged during initialization with the Root and only the Root remains as an EOEntity within the model at runtime._

- Do Root entities generate a response with all the attributes for both the Root entity and all partials?

> _Note: Sept 14, 2012 - NO, the Partial Entity is merged during initialization with the Root and only the Root remains as an EOEntity within the model at runtime._

- Should the attributes/relationships from the partials be identified distinctly?



# Plan for Enhancement

In order for `ERXPartial` to gain mindshare and traction several projects need to be implemented.

## New Features

- Enhance the WOLips EOModel editor to allow the selection of appropriate default super-classes

	- Currently this is only used to select classes within the included model and referenced models for the purpose of entity-inheritance.  Including a section with EOGenericObject, ERXGenericObject and `ERXPartialGenericObject` would improve the template generation and the ability for new users of the design to adopt it.

- `ERXPartialInitializer` should evaluate additional meta-data about Root Partials and Partial Entities for the following purposes:

- Storing original source entity information for attributes and relationship and allowing runtime query of this data.

- Evaluating namespace collisions with more stringent type checking to ensure conformance between Root concepts and Partials.

- `ERXPartialInitializer` should evaluate all entities and throw an exception for any that try to subclass a Root Partial Entity

- `ERXPartialInitializer` should evaluate all Partial Entities and generate a warning for any that declare a parent entity, as this data is not used and is silently ignored.

- Potential updates to support Partials in both ERRest and D2W are not included at this time, as those suggestions will be discovered during the building and testing of the demo and test projects.

## Sample (Demo) Application

The sample application should illustrate the core features of the `ERXPartial` implementation and the most common use case.  This will entail

- Documentation for demo application, detailing
- Overall design overview
- Connecting information between How-To concepts to sample demo code
- Discussion and comparison of the Partial Composition pattern and the Inheritance pattern as used in WebObjects.
- Configuration and setup instructions

It would be nice to support multiple data storage mechanisms in the demo.  Specifically the MemoryAdaptor  or H2Adaptor are perfect for a build and run demo, but a persistent store like Frontbase would provide additional instruction and the opportunity to review the effects of the transactions on the raw data and schema.

### Core Model Framework

A simple framework containing

- A control group of entities
- One root entity that will be extended with Partials
- A small inheritance tree with Leaf nodes that will also act as Root Partial Entities
- Initialize the base model using migration tools### Additional features framework

A simple framework that will contain

- A control group of entities
- One or more simple Partial entities that will extend the root concept entity
- One or more simple Partial entities that will extend the inheritance tree.
- Demonstrate the integration of partials using migration tools.

### Demonstration Application

- Validation - demonstrate validation in both Root entities and Partial entities.
- D2W - demonstrate the partial design using common D2W components and workflows.
- Custom Component - demonstrate the use of partials in a WOComponent display and processing page.
- ERRest - demonstrate the use of the Partials using the REST protocol (similar to the ERRestRoutesExample).

## Test Case

Simple test units to verify the core features of `ERXPartial`s are functioning with the current Wonder build.


----
David Aspinall  
Senior IT Consultant  
[Global Village Consulting Inc.][gvc]



[gvc]: http://www.global-village.net
[logicsquad]: http://logicsquad.net/
