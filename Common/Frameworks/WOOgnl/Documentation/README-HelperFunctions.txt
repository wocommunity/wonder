=== Overview ===
It is often the case in a WOD file that you want to apply formatters to various
bindings, but only certain specific components actually support the concept of 
a formatter.

There are several approaches to solving this problem.  One is to create formatter
methods on your models (i.e, person.displayName() that returns firstName() + " " + lastName()).
This suffers a design flaw in that you don't want what is essentially view code in your 
models.  Another approach is to define these methods in a WOComponent, but then you have the 
problem that you can't reuse the value very easily (you need it in every component).  You could
create a new WOComponent that just renders in this format, but then you can't pass it as the
value to another binding.

Rails addresses this with the concept of a helper function.  Helper functions are easily reusable
formatting methods for use in your views.  WOHelperFunctionHTMLParser provides a similar type of 
functionality for use in WOD bindings.


=== Example Usage ===
As an example, say you have a Person class and you want a common display name (like in the example
above).  With WOHelperFunctionHTMLParser, you would do the following:

1) Create a class like the following (in any package):
	public class PersonHelper {
		public String displayName(Person person) {
			return person.firstName() + " " + person.lastName();
		}
	}

2) In a WOD file:
	PersonName : WOString {
		value = currentPerson|displayName;
	}

(that is a pipe between currentPerson and displayName)

3) Set ognl.helperFunctions=true in your Properties file.

4) Profit.

Likewise you can make StringHelper, BooleanHelper, etc.  You want to use "yes" or "no" when
you display booleans?

in Java:
	public class BooleanHelper {
		public String yesNo(Boolean value) {
			String yesNoValue;
			if (yesNoValue == null || !yesNoValue.booleanValue()) {
				yesNoValue = "no";
			}
			else {
				yesNoValue = "yes";
			}
		}
	}

in your WOD:
	RandomValue : WOString {
		value = currentPerson.isAdmin|yesNo;
	}

The above examples use WOString, so /technically/ you could use formatters for these.  Here's
an example where that wouldn't work out:
	
	HeaderFooter : HeaderFooterWrapper {
		title = currentPerson|displayName;
	}

The examples here only define a single method, but you can define as many helper methods inside 
of the Helper classes as you'd like.


=== How Does it Work? ===
In the parser stage of loading your WOD file, helper function bindings are replaced with the
much larger WOOGNL expression that is able to resolve your request, so behind the scenes this
is using the same OGNL that you know and love.

The helper class to use for a particular value is determined by the type of object that comes 
immediately before the pipe symbol.  For instance, if getClass().getName() returns 
"com.mdimension.Person", by default WOHelperFunction will look for a "PersonHelper" class
using the same bundle discovery that components use.


=== Helper Function Parameters ===
Because the method calls turn into WOOGNL behind the scenes, parameters should mostly just work.  As
an example, you could make a truncate helper function:

in Java:
	public class StringHelper {
		public String truncate(String value, int atIndex) {
			String truncatedValue = value;
			if (value != null && value.length() > atIndex) {
				truncatedValue = value.substring(0, atIndex) + " ...";
			}
			return truncatedValue;
		}
	}

and in your WOD:
	HeaderFooter : HeaderFooterWrapper {
		title = pageMetadata.description|truncate(10);
	}

You are not limited to just a single parameter.


=== Remapping Helper Instances ===
It is sometimes helper to remap a particular type of object to a custom helper class.  In this
case, you can call:

setHelperInstanceForClassInFrameworkNamed(Object helperInstance, Class targetObjectClass, String frameworkName);

Let's say you want Person.class to use MyPersonHelper as its helper instance for the entire app, you would call:

WOHelperFunctionRegistry.setHelperInstanceForClassInFrameworkNamed(new MyPersonHelper(), Person.class, "app");


=== Per-Framework Helper Functions ===
Unqualified, helper functions look in the "app" frameworkName to resolve.  However, this can cause conflicts if
you have a framework that wants to register its own helpers (you wouldn't want to replace the top-level
StringHelper, for instance).  Take Ajax framework as an example:

WOHelperFunctionRegistry.setHelperInstanceForClassInFrameworkNamed(new AjaxPersonHelper(), String.class, "Ajax");

To use a qualified helper function, you use the syntax:

HeaderFooter : HeaderFooterWrapper {
	title = currentPerson|Ajax.displayName;
}

Note the "Ajax." qualifier in front of the helper function name.


=== Subclassing Helper Instances ===
Subclassing with helper instances is legal.  So if you have a StringHelper in your core framework, you can
make a MyProjectStringHelper extends StringHelper and register MyProjectStringHelper as "app" helper.


=== Gotchas ===
If you use WOLips, you should go to Windows=>Preferences=>WOLips=>WOD Editor and add pipe, open 
paren, and close paren to the list of valid wod binding characters.