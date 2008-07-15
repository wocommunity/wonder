package er.extensions;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSRange;

/**
 * ERXResponse provides a place to override methods of WOResponse. This is
 * returned by default from ERXApplication. Also has support for "partials",
 * i.e. in your render tree, you can define a new "partial", where the content
 * will actually get rendered.
 * 
 * @author mschrag
 * @author ak
 */
public class ERXResponse extends WOResponse {

	public static class Context {

		private LinkedHashMap<String, ERXResponse> partials = new LinkedHashMap<String, ERXResponse>();

		private Stack<ERXResponse> stack = new Stack<ERXResponse>();
	}

	private LinkedHashMap<String, Integer> marks;

	/**
	 * Call this to mark the place where a partial should get rendered.
	 * 
	 * @param key
	 */
	public void mark(String key) {
		if (marks == null) {
			marks = new LinkedHashMap<String, Integer>();
		}
		marks.put(key, _contentLength());
	}

	/**
	 * Overridden to insert the partials in the respective area.
	 */
	@Override
	public void _finalizeInContext(WOContext arg0) {
		super._finalizeInContext(arg0);
		if (marks != null && marks.size() > 0) {
			Context context = currentContext();
			NSMutableData content = new NSMutableData();
			int last = 0;
			for (Map.Entry<String, Integer> entry : marks.entrySet()) {
				String key = entry.getKey();
				Integer offset = entry.getValue();
				NSRange range = new NSRange(last, offset - last);
				NSData data = content().subdataWithRange(range);
				content.appendData(data);
				ERXResponse partial = context.partials.get(key);
				if (partial != null) {
					NSData partialData = partial.content();
					content.appendData(partialData);
				}
				last = offset;
			}
			NSRange range = new NSRange(last, _contentLength() - last);
			NSData data = content().subdataWithRange(range);
			content.appendData(data);
			setContent(content);
		}
	}

	private static Context currentContext() {
		Context context = (Context) ERXThreadStorage.valueForKey("ERXResponse.Context");
		if (context == null) {
			context = new Context();
			ERXThreadStorage.takeValueForKey(context, "ERXResponse.Context");
		}
		return context;
	}

	/**
	 * Returns the associated response for the supplied key. Creates it if
	 * needed.
	 * 
	 * @param key
	 * @return
	 */
	public static ERXResponse pushPartial(String key) {
		Context context = currentContext();
		WOContext wocontext = ERXWOContext.currentContext();
		context.stack.push((ERXResponse) wocontext.response());
		ERXResponse response = context.partials.get(key);
		if (response == null) {
			response = new ERXResponse();
			context.partials.put(key, response);
		}
		wocontext._setResponse(response);
		return response;
	}

	/**
	 * Returns the top-most response after this one has been pulled from the
	 * stack.
	 * 
	 * @return
	 */
	public static ERXResponse popPartial() {
		Context context = currentContext();
		ERXResponse response = context.stack.pop();
		WOContext wocontext = ERXWOContext.currentContext();
		wocontext._setResponse(response);
		return response;
	}

	/**
	 * The original _appendTagAttributeAndValue would skip null values, but not
	 * blank values, which would produce html like &lt;div style = ""&gt;. This
	 * implementation also skips blank values.
	 */
	@Override
	public void _appendTagAttributeAndValue(String name, String value, boolean escape) {
		if (value != null && value.length() > 0) {
			super._appendTagAttributeAndValue(name, value, escape);
		}
	}
}
