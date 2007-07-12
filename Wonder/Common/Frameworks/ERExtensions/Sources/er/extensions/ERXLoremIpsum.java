package er.extensions;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

/**
 * ERXLoremIpsum provides a component wrapper around the ERXLoremIpsumGenerator.
 * 
 * @binding type "word", "sentence", or "paragraph"
 * @binding count the number of words, sentences, or paragraphs to generate
 * 
 * @author mschrag
 */
public class ERXLoremIpsum extends WODynamicElement {
	private WOAssociation _type;
	private WOAssociation _count;

	public ERXLoremIpsum(String name, NSDictionary associations, WOElement template) {
		super(name, associations, template);
		_type = (WOAssociation) associations.objectForKey("type");
		_count = (WOAssociation) associations.objectForKey("count");
	}

	public void appendToResponse(WOResponse response, WOContext context) {
		WOComponent component = context.component();
		String type;
		if (_type == null) {
			type = ERXLoremIpsumGenerator.PARAGRAPH;
		}
		else {
			type = (String) _type.valueInComponent(component);
		}
		int count;
		if (_count == null) {
			count = 1;
		}
		else {
			count = ((Integer) _count.valueInComponent(component)).intValue();
		}
		String loremIpsum = ERXLoremIpsumGenerator.generate(type, count).replaceAll("\n\n", "<p>");
		response.appendContentString(loremIpsum);
		super.appendToResponse(response, context);
	}
}
