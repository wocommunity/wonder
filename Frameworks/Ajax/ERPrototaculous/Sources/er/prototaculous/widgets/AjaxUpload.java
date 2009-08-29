package er.prototaculous.widgets;

import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxUtils;
import er.extensions.appserver.ajax.ERXAjaxApplication;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Encapsulation of http://valums.com/ajax-upload/
 * @binding			name				@see http://valums.com/ajax-upload/
 * @binding			onChange			@see http://valums.com/ajax-upload/
 * @binding			onSubmit			@see http://valums.com/ajax-upload/
 * @binding			onComplete			@see http://valums.com/ajax-upload/
 * 
 * @property		useUnobtrusively			For Unobtrusive Javascript programming. Default it is off.
 * 
 * @author mendis
 *
 */
public abstract class AjaxUpload extends WOComponent {
	private static boolean useUnobtrusively = ERXProperties.booleanForKeyWithDefault("er.prototaculous.useUnobtrusively", false);

	public AjaxUpload(WOContext aContext) {
		super(aContext);
	}
	
	/*
	 * Bindings/API
	 */
	public static interface Bindings {
		public static final String id = "id";
		public static final String name = "name";
		public static final String onChange = "onChange";
		public static final String onSubmit = "onSubmit";
		public static final String onComplete = "onComplete";
		public static final String filePath = "filePath";
		public static final String data = "data";
		public static final String mimeType = "mimeType";
		public static final String inputStream = "inputStream";
		public static final String outputStream = "outputStream";
		public static final String finalFilePath = "finalFilePath";
		public static final String streamToFilePath = "streamToFilePath";

	}
	
	/*
	 * Headers of multipart form
	 */
	public static interface Headers {
		public static final String filename = "filename";
		public static final String contentType = "content-type";
		public static final String name = "name";
	}
	
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	// accessors	
	public String id() {
		return _id() == null ? "au" + ERXStringUtilities.safeIdentifierName(elementID()) : _id();
	}
	
	private String elementID;
	
	private String elementID() {
		if (elementID == null) elementID = context().elementID();
		return elementID;
	}
	
	private String _id() {
		return (String) valueForBinding(Bindings.id);
	}
	
	private String _script() {
		return "new AjaxUpload('" + id() + "', {" + options() + "});";
	}
	
	public String script() {
		return isAjax() ? _script() : "document.observe('dom:loaded', function() { " + _script() + " });";
	}
	
	private boolean isAjax() {
		return ERXAjaxApplication.isAjaxRequest(context().request());
	}
	
    /*
     * An array of options for Ajax.Updater
     */
    protected NSArray<String> _options() {
    	NSMutableArray _options = new NSMutableArray("action:'" + href() + "'");
    	
    	// add options
    	if (hasBinding(Bindings.name)) _options.add("name:'" + valueForBinding(Bindings.name) + "'");
    	if (hasBinding(Bindings.onChange)) _options.add("onChange:" + valueForBinding(Bindings.onChange));
    	if (hasBinding(Bindings.onComplete)) _options.add("onComplete:" + valueForBinding(Bindings.onComplete));
    	if (hasBinding(Bindings.onSubmit)) _options.add("onChange:" + valueForBinding(Bindings.onSubmit));

    	return _options.immutableClone();
    }
    
    public String options() {
    	return _options().componentsJoinedByString(", ");
    }
	
	private String _uploadName() {
		return (String) valueForBinding(Bindings.name);
	}
	
	public String uploadName() {
		return _uploadName() != null ? _uploadName() : "userfile";	// Default
	}
	
	protected void setFilePath(String aPath) {
		setValueForBinding(aPath, Bindings.filePath);
	}
	
	protected void setData(NSData data) {
		setValueForBinding(data, Bindings.data);
	}
	
	protected void setMimeType(String aType) {
		setValueForBinding(aType, Bindings.mimeType);
	}
	
	/*
	 * NOTE: this is a standard WO component action url.
	 * The file upload won't work as an ajax request.
	 * 
	 */
	private String href() {
		return context().componentActionURL();
	}
	
	// R&R
    @Override
	public void appendToResponse(WOResponse response, WOContext context) {
    	super.appendToResponse(response, context);
    	
    	if (!useUnobtrusively) {
    		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
    		AjaxUtils.addScriptResourceInHead(context, response, "scriptaculous.js");
    		AjaxUtils.addScriptResourceInHead(context, response, "ERPrototaculous", "ajaxupload3.5.js");
    	}
    }
}
