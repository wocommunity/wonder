package er.directtoweb.components.strings;

import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSArray;

import er.directtoweb.components.ERDCustomEditComponent;
import er.extensions.appserver.ERXResponseRewriter;
import er.extensions.foundation.ERXProperties;
import er.extensions.foundation.ERXStringUtilities;

/**
 * Very, very basic version of a TinyMCE integration. As it doesn't make much sense to load the the JS files
 * it's excepted that you put them somewhere on your server and specify the location with
 * the property <code>er.directtoweb.ERDEditHTML.tinyMceSourceUrl=http://somewhere/.../tiny_mce.js</code>.
 * The default is the TinyMce server which would be very unfriendly and slow to use in deployment...
 * <p>
 * You can also use <code>er.directtoweb.ERDEditHTML.tinyMceSourceFileName</code> and 
 * <code>er.directtoweb.ERDEditHTML.tinyMceSourceFrameworkName</code> properties to specify file name and framework name ("app" by default)
 * of TinyMCE if you want to store the files in WebServerResources of your application of framework. For example:
 * <pre>
 * er.directtoweb.ERDEditHTML.tinyMceSourceFileName = tiny_mce/tiny_mce.js
 * </pre>
 * 
 * @property er.directtoweb.ERDEditHTML.tinyMceSourceFileName
 * @property er.directtoweb.ERDEditHTML.tinyMceSourceFrameworkName
 * @property er.directtoweb.ERDEditHTML.tinyMceSourceUrl (default http://tinymce.moxiecode.com/js/tinymce/jscripts/tiny_mce/tiny_mce.js)
 *
 * @author ak
 */
public class ERDEditHTML extends ERDCustomEditComponent {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

	public static final String DEFAULT_URL = "http://tinymce.moxiecode.com/js/tinymce/jscripts/tiny_mce/tiny_mce.js";
	public static final String SOURCE_URL_PROPERTY = "er.directtoweb.ERDEditHTML.tinyMceSourceUrl";
	public static final String FRAMEWORK_NAME_PROPERTY = "er.directtoweb.ERDEditHTML.tinyMceSourceFrameworkName";
	public static final String FILE_NAME_PROPERTY = "er.directtoweb.ERDEditHTML.tinyMceSourceFileName";

	public ERDEditHTML(WOContext context) {
		super(context);
	}

	@Override
	public boolean isStateless() {
		return true;
	}

	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}

	@Override
	public void appendToResponse(WOResponse response, WOContext context) {
		super.appendToResponse(response, context);
		String url = ERXProperties.stringForKeyWithDefault(SOURCE_URL_PROPERTY, DEFAULT_URL);
		String fileName = ERXProperties.stringForKeyWithDefault(FILE_NAME_PROPERTY, url);
		String framework = ERXProperties.stringForKeyWithDefault(FRAMEWORK_NAME_PROPERTY, "app");
		ERXResponseRewriter.addScriptResourceInHead(response, context, framework, fileName);
	}

	public String initScript() {
		return String.format("tinyMCE.init({%s});", richTextMode());
	}
	
	public String textAreaClass() {
		return stringValueForBinding("textareaClass", defaultTextAreaClass());
	}
	
	public String defaultTextAreaClass() {
		String defaultClass = "";
		if (key() != null) {
			String temp = "";
			if (key().indexOf(".") != -1) {
				NSArray<String> components = NSArray.componentsSeparatedByString(key(), ".");
				for (String string : components) {
					string = ERXStringUtilities.capitalize(string);
					temp = temp + string;
				}
			} else {
				temp = ERXStringUtilities.capitalize(key());
			}
			defaultClass = temp + "RichTextArea";
		}
		return defaultClass;
	}

	private String richTextMode() {
		return stringValueForBinding("richTextMode") + ", editor_selector : '" + textAreaClass() + "', setup : function(ed) { ed.onChange.add(function(ed) { tinyMCE.triggerSave(); } );}";
	}

}
