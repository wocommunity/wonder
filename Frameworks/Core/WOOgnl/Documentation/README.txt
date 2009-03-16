Hi,
        I thought those on this list might be interested in a little  
framework that I put together a few weekends ago that allows for the  
use of OGNL syntax in wod bindings and D2W custom assignments.  OGNL  
is basicly key-value coding on steroids. Pulled straight from the
wod file of the downloadable example WOOgnlTest (note the '~'
character is just like the '^', ie it flags that the binding should  
be resolved via OGNL.  The '~' character is configurable so for
example you could set "OGNL:" to be the flag).

// Calling static methods or accessing static ivars
String1: WOString {
        value = "~@ognl.webobjects.WOOgnl@OgnlSpecialCharacters";
}

// Use of conditionals, note that every previous value of the . is
pushed into the ivar #this
String2: WOString {
        value = "~name.length().(#this > 100? 2*#this : 20+#this)";
}

// String concat
String3: WOString {
        value = "~\"Hello Max \" + name";
}

// Use of set operator in.  can also use in against NSArray and
NSSet objects
String4: WOString {
        value = "~name in {\"Main\", \"Something\"} ? \"Yes\" : \"No\"";
}

// Variable declaration.  Note that commas allow multiple actions
per expression.
String5: WOString {
        value = "~#A=new com.webobjects.foundation.NSMutableArray(),  
#A.addObject(name), #A.addObjectsFromArray(session.languages), #A";
}

The WOOgnl dev page is at http://www.netstruxr.com/developer/woognl.html.

More info on OGNL is at http://www.ognl.org

Comments, bugs, feedback or questions feel free to contact me.

Regards,
        Max
