package er.extensions.components;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

/**
 * Simple RSS feed provider. The actual item goes into the component content.
 * 
 * @binding feedTitle the title of the RSS feed
 * @binding feedUrl the URL of the website associated with the RSS feed
 * @binding feedDescription the description of the RSS feed
 * @binding list the list of items to show in the feed
 * @binding item the repetition item binding for the feed items
 * @binding itemGuid the GUID of the current item
 * @binding itemTitle the title of the current item
 * @binding itemLink the link associated with the current item
 * @binding itemPubDate the publish date of the current item
 * @binding itemAuthor the author of the item
 * 
 * @author ak
 */
public class ERXRssPage extends ERXStatelessComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;
	 
	public ERXRssPage(WOContext context) {
		super(context);
	}

	public Object dateFormatter() {
		return new SimpleDateFormat("E, dd MMM yyyy HH:mm:ss Z", Locale.ENGLISH);
	}
	
	public Object itemGuid() {
		Object itemGuid = valueForBinding("itemGuid");
		if (itemGuid == null) {
			itemGuid = valueForBinding("itemLink");
		}
		return itemGuid;
	}
	
	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		context.generateCompleteURLs();
		response.setHeader("text/xml", "content-type");
		super.appendToResponse(response, context);
	}
}