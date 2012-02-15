package er.ajax.mootools;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;

import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXProperties;

public class MTAjaxUtils extends AjaxUtils {
	
	public static final String MOOTOOLS_MORE_JS = "scripts/core/mootools-more-1.4.0.1.js";
	public static final String MOOTOOLS_CORE_JS = "scripts/core/mootools-core-1.4.4.js";
	public static final String MOOTOOLS_WONDER_JS = "scripts/core/MTWonder.js";

	public static void addScriptResourceInHead(WOContext context, WOResponse response, String framework, String fileName) {
	
		String processedFileName = fileName;

		if(ERXProperties.booleanForKey("er.mootools.compressed") && MOOTOOLS_CORE_JS.equals(fileName)) {
			processedFileName = "scripts/core/mootools-core-1.4.4-yc.js";
		}
		
		if(ERXProperties.booleanForKey("er.mootools.compressed") && MOOTOOLS_MORE_JS.equals(fileName)) {
			processedFileName = "scripts/core/mootools-more-1.4.0.1-yc.js";
		}

		ERXResponseRewriter.addScriptResourceInHead(response, context, framework, processedFileName);
	
	}
	
}
