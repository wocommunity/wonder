package er.coolcomponents;

import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.components.ERXDynamicElement;

/**
 * <p>
 * CCGoogle analytics is a very simple compoment (dynamic element) that insert the google analytics code</p>
 * <p>
 * 
 * @binding uaid you goocle analytics id: UA-XXXXXX
 *
 * @author amedeomantica
 */

public class CCGoogleAnalytics extends ERXDynamicElement {

	public CCGoogleAnalytics(String name,
			NSDictionary<String, WOAssociation> associations,
			WOElement children) {
		super(name, associations, children);
	}

	
	@Override
	public void appendToResponse(WOResponse aResponse, WOContext aContext) {
		super.appendToResponse(aResponse, aContext);
		
		String googleId = stringValueForBinding("uaid", aContext.component());
		
		if(googleId!=null) {
		
			ERXResponseRewriter.appendScriptTagOpener(aResponse);			
			aResponse.appendContentString("var _gaq = _gaq || [];");
			aResponse.appendContentString("_gaq.push(['_setAccount', '" + googleId + "']);");
			aResponse.appendContentString("_gaq.push(['_trackPageview']);");
			aResponse.appendContentString("(function() {");
			aResponse.appendContentString("var ga = document.createElement('script'); ga.type = 'text/javascript'; ga.async = true;");
			aResponse.appendContentString("ga.src = ('https:' == document.location.protocol ? 'https://ssl' : 'http://www') + '.google-analytics.com/ga.js';");
			aResponse.appendContentString("var s = document.getElementsByTagName('script')[0]; s.parentNode.insertBefore(ga, s);");
			aResponse.appendContentString("})();");
			ERXResponseRewriter.appendScriptTagCloser(aResponse);
			
		}
		
	}
	

}
