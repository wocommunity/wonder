package er.extensions;

import java.text.SimpleDateFormat;
import java.util.Locale;

import com.webobjects.appserver.*;

/**
 * Simple RSS feed provider.
 * @binding feedTitle
 * @binding feedUrl
 * @binding feedDescription
 * @binding list
 * @binding item
 * @binding itemGuid
 * @binding itemTitle
 * @binding itemLink
 * @binding itemPubDate
 * @author ak
 */

public class ERXRssPage extends ERXStatelessComponent {
	 
	public ERXRssPage(WOContext context) {
		super(context);
	}

	public Object dateFormatter() {
		return new SimpleDateFormat("E, dd MMM yyyy hh:mm:ss Z", Locale.ENGLISH);
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
		context._generateCompleteURLs();
		response.setHeader("text/xml", "content-type");
		super.appendToResponse(response, context);
	}
}