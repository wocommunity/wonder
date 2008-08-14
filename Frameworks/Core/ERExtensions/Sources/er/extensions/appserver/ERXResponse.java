package er.extensions.appserver;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOMessage;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableData;
import com.webobjects.foundation.NSRange;

import er.extensions.foundation.ERXThreadStorage;

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
		protected LinkedHashMap<String, ERXResponse> partials = new LinkedHashMap<String, ERXResponse>();

		protected Stack<ERXResponse> stack = new Stack<ERXResponse>();
	}

	private LinkedHashMap<String, Integer> marks;
	private Stack<Object> _contentStack;
	
	protected void __setContent(Object appendable) {
		try {
			WOMessage.class.getDeclaredField("_content").set(this, appendable);
		}
		catch (Throwable e) {
			throw new NSForwardException(e);
		}
	}
	
	/**
	 * Pushes a new _content onto the stack, so you can write to this response and capture the 
	 * output.
	 */
	public void pushContent() {
		if (_contentStack == null) {
			_contentStack = new Stack<Object>();
		}
		_contentStack.push(_content);
		Object newContent;
		try {
			newContent = _content.getClass().newInstance();
		}
		catch (Throwable e) {
			throw new NSForwardException(e);
		}
		__setContent(newContent);
	}

	/**
	 * Pops the last _content off the stack, optionally appending the current content to it.
	 * 
	 * @param append
	 */
	public void popContent(boolean append) {
		if (_contentStack == null || _contentStack.size() == 0) {
			throw new IllegalStateException("You attempted to popContent off of an empty stack.");
		}
		Object oldAppendable = _content;
		Object appendable = _contentStack.pop();
		__setContent(appendable);
		if (append) {
			appendContentString(oldAppendable.toString());
		}
	}

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
	 * @param key the key to push the partial as
	 * @return the new ERXResponse to write to
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
	 * @return the previous partial
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
