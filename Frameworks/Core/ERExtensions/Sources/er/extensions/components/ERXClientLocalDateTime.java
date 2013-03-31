/*
  Klaus Berkling for DynEd Sept 19, 2012
  kiberkli@gmail.com
 
  Usage:
  1) Add to your component
  
          ClientLocalDateTime : ERXClientLocalDateTime {}
          
  2) Add
          ClientLocalDateTimeElement : ERXClientLocalDateTimeElement {
               value = <UTC time in seconds>;
               id = <unique identifier>;
               valueWhenEmpty = <alternate value when value is empty>;
               formatFunction = [ clientLocalDateTime  |clientLocalDateTimeUS | clientLocalTime | clientLocalTimeUS | clientLocalDate | clientLocalDateUS ];
               class = [ <class-string> ];
               style = [ <styl-tags> ];
          }

	Supported formatsfunctions are:
	- clientLocalDateTime: Date and time in european style format
	- clientLocalDateTimeUS: Date and time in a US style format
	- clientLocalTime: Time in 24hr format
	- clientLocalTimeUS: Time in US format (12hr am|pm)
	- clientLocalDate: Date in european style format
	- clientLocalDateUS: Date in US style format

*/

package er.extensions.components;

import com.webobjects.appserver.WOContext;
import er.extensions.components.ERXStatelessComponent;

public class ERXClientLocalDateTime extends ERXStatelessComponent {
	
    public ERXClientLocalDateTime(WOContext context) {
        super(context);
    }
    
}