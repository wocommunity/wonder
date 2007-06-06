package er.extensions;

import java.util.Enumeration;
import java.util.Hashtable;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOActiveImage;
import com.webobjects.appserver._private.WOBrowser;
import com.webobjects.appserver._private.WOCheckBox;
import com.webobjects.appserver._private.WOCheckBoxList;
import com.webobjects.appserver._private.WOGenericContainer;
import com.webobjects.appserver._private.WOGenericElement;
import com.webobjects.appserver._private.WOHiddenField;
import com.webobjects.appserver._private.WOImage;
import com.webobjects.appserver._private.WOImageButton;
import com.webobjects.appserver._private.WOInput;
import com.webobjects.appserver._private.WOPasswordField;
import com.webobjects.appserver._private.WOPopUpButton;
import com.webobjects.appserver._private.WORadioButton;
import com.webobjects.appserver._private.WORadioButtonList;
import com.webobjects.appserver._private.WOResetButton;
import com.webobjects.appserver._private.WOSubmitButton;
import com.webobjects.appserver._private.WOText;
import com.webobjects.appserver._private.WOTextField;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;

/**
 * Wrapper around the WO-private NSUtilities which allows for some Objective-C-Style poseAs. Using these methods may or
 * may not break in the future.
 */
public class ERXPatcher {

	/** logging support */
	public final static Logger log = Logger.getLogger(ERXPatcher.class);

	public ERXPatcher() {
	}

	/**
	 * Returns the class registered for the name <code>className</code>.<br/> Uses the private WebObjects class
	 * cache.
	 * 
	 * @param className
	 *            class name
	 * @return class for the registered name or null
	 */
	public static Class classForName(String className) {
		return _NSUtilities.classWithName(className);
	}

	/**
	 * Sets the class registered for the name <code>className</code> to the given class.<br/> Changes the private
	 * WebObjects class cache.
	 * 
	 * @param clazz
	 *            class object
	 * @param className
	 *            name for the class - normally clazz.getName()
	 */
	public static void setClassForName(Class clazz, String className) {
		_NSUtilities.setClassForName(clazz, className);
	}

	public static synchronized void installPatches() {
		DynamicElementsPatches.cleanupXHTML = ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXPatcher.cleanupXHTML"), false);

		ERXPatcher.setClassForName(DynamicElementsPatches.SubmitButton.class, "WOSubmitButton");
		ERXPatcher.setClassForName(DynamicElementsPatches.ResetButton.class, "WOResetButton");
		ERXPatcher.setClassForName(DynamicElementsPatches.TextField.class, "WOTextField");
		ERXPatcher.setClassForName(DynamicElementsPatches.GenericElement.class, "WOGenericElement");
		// ERXPatcher.setClassForName(DynamicElementsPatches.GenericContainer.class, "WOGenericContainer");
		ERXPatcher.setClassForName(DynamicElementsPatches.Image.class, "WOImage");
		ERXPatcher.setClassForName(DynamicElementsPatches.ActiveImage.class, "WOActiveImage");
		ERXPatcher.setClassForName(DynamicElementsPatches.Text.class, "WOText");
		ERXPatcher.setClassForName(DynamicElementsPatches.PopUpButton.class, "WOPopUpButton");
		ERXPatcher.setClassForName(DynamicElementsPatches.Browser.class, "WOBrowser");
		ERXPatcher.setClassForName(DynamicElementsPatches.CheckBox.class, "WOCheckBox");
		ERXPatcher.setClassForName(DynamicElementsPatches.CheckBoxList.class, "WOCheckBoxList");
		ERXPatcher.setClassForName(DynamicElementsPatches.FileUpload.class, "WOFileUpload");
		ERXPatcher.setClassForName(DynamicElementsPatches.HiddenField.class, "WOHiddenField");
		ERXPatcher.setClassForName(DynamicElementsPatches.ImageButton.class, "WOImageButton");
		ERXPatcher.setClassForName(DynamicElementsPatches.PasswordField.class, "WOPasswordField");
		ERXPatcher.setClassForName(DynamicElementsPatches.RadioButton.class, "WORadioButton");
		ERXPatcher.setClassForName(DynamicElementsPatches.RadioButtonList.class, "WORadioButtonList");

		// AK This is needed so we get our versions of the WOToXXRelationships installed even if the
		// ones from WOExtensions are before us in the classpath
		ERXPatcher.setClassForName(WOToManyRelationship.class, "WOToManyRelationship");
		ERXPatcher.setClassForName(WOToOneRelationship.class, "WOToOneRelationship");

		ERXPatcher.setClassForName(ERXHyperlink.class, "WOHyperlink");
		ERXPatcher.setClassForName(ERXWOConditional.class, "WOConditional");
	}

	/**
	 * This class holds patches for WebObjects dynamic elements, which have always a closing tag and all attribute
	 * values are enclosed in quotes. The patches are automatically registered if this framework gets loaded.<br/>
	 * <b>Note</b>: <code>WOForm</code> is not replaced, because it is ok if you don't use <code>?</code>-bindings.
	 * If you need additional parameters, just insert <code>WOHiddenField</code>s.<br/> Also
	 * <code>WOJavaScript</code> is not replaced, even if it is not XHTML-conform.
	 */
	public static class DynamicElementsPatches {
		public static boolean cleanupXHTML = false;

		private DynamicElementsPatches() {
		}

		public static class SubmitButton extends WOSubmitButton {
			protected WOAssociation _id;

			public SubmitButton(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			/**
			 * Appends the attribute "value" to the response. First tries to get a localized version and if that fails,
			 * uses the supplied value as the default
			 */
			protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
				if (_value != null) {
					Object value = _value.valueInComponent(context.component());
					if (value != null) {
						String stringValue = value.toString();
						// stringValue = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(stringValue);
						response._appendTagAttributeAndValue("value", stringValue, true);
					}
				}
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}

			/*
			 * logs the action name into session's dictionary with a key = ERXActionLogging
			 */
			public WOActionResults invokeAction(WORequest arg0, WOContext arg1) {
				WOActionResults result = super.invokeAction(arg0, arg1);
				if (result != null && _action != null && ERXSession.session() != null) {
					ERXSession.session().setObjectForKey(this.toString(), "ERXActionLogging");
				}
				return result;
			}

		}

		public static class ResetButton extends WOResetButton {
			protected WOAssociation _id;

			public ResetButton(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			/**
			 * Appends the attribute "value" to the response. First tries to get a localized version and if that fails,
			 * uses the supplied value as the default
			 */
			protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
				if (_value != null) {
					Object object = _value.valueInComponent(context.component());
					Object object1 = null;
					if (object != null) {
						String string = object.toString();
						// string = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(string);
						response._appendTagAttributeAndValue("value", string, true);
					}
				}
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class GenericContainer extends WOGenericContainer {
			protected WOAssociation _id;

			public GenericContainer(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
				super.appendAttributesToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, null);
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class GenericElement extends WOGenericElement {
			protected WOAssociation _id;

			public GenericElement(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
				super.appendAttributesToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, null);
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class Image extends WOImage {
			protected WOAssociation _id;

			public Image(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
				super.appendAttributesToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, null);
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class ActiveImage extends WOActiveImage {
			protected WOAssociation _id;

			public ActiveImage(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				int offset = woresponse.contentString().length();
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}

			/*
			 * logs the action name into session's dictionary with a key = ERXActionLogging if log is set to debug.
			 */
			public WOActionResults invokeAction(WORequest arg0, WOContext arg1) {
				WOActionResults result = super.invokeAction(arg0, arg1);
				if (result != null && ERXSession.session() != null) {
					ERXSession.session().setObjectForKey(this.toString(), "ERXActionLogging");
				}
				return result;
			}

		}

		public static class TextField extends WOTextField {
			protected WOAssociation _id;

			public TextField(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class Text extends WOText {
			protected WOAssociation _id;

			public Text(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class PopUpButton extends WOPopUpButton {
			protected WOAssociation _id;

			public PopUpButton(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class Browser extends WOBrowser {
			protected WOAssociation _id;

			public Browser(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class CheckBox extends WOCheckBox {
			protected WOAssociation _id;

			public CheckBox(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class CheckBoxList extends WOCheckBoxList {
			protected WOAssociation _id;

			public CheckBoxList(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class FileUpload extends ERXWOFileUpload {
			protected WOAssociation _id;

			public FileUpload(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class HiddenField extends WOHiddenField {
			protected WOAssociation _id;

			public HiddenField(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class ImageButton extends WOImageButton {
			protected WOAssociation _id;

			public ImageButton(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class PasswordField extends WOPasswordField {
			protected WOAssociation _id;

			public PasswordField(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class RadioButton extends WORadioButton {
			protected WOAssociation _id;

			public RadioButton(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		public static class RadioButtonList extends WORadioButtonList {
			protected WOAssociation _id;

			public RadioButtonList(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_id = (WOAssociation) super._associations.removeObjectForKey("id");
			}

			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = new WOResponse();
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				woresponse.appendContentString(newResponse.contentString());
			}
		}

		/**
		 * Allows you to set the component ID without actually touching the HTML code, by adding a
		 * <code>componentIdentifier</code> entry in the context's mutableUserInfo. This is useful for setting CSS
		 * entries you don't have to code for.
		 */
		public static void appendIdentifierTagAndValue(WODynamicElement element, WOAssociation id, WOResponse response, WOContext context) {
			if (id != null) {
				Object idValue = id.valueInComponent(context.component());
				if (idValue != null)
					response._appendTagAttributeAndValue("id", idValue.toString(), true);
			}
			else {
				NSMutableDictionary dict = ERXWOContext.contextDictionary();
				String componentIdentifier = (String) dict.objectForKey("componentIdentifier");
				if (componentIdentifier != null) {
					response._appendTagAttributeAndValue("id", componentIdentifier, true);
				}
			}
		}

		/**
		 * Fixing up the response for XHTML and adding the element to the array of generated element IDs, so we can use
		 * JavaScript later on. If the given element is an input element, it adds a dictionary {type=element.class,
		 * name=element.elementID} to ERXWOContext.contextDictionary().objectForKey("elementArray")
		 */
		public static void processResponse(WODynamicElement element, WOResponse response, WOContext context, int priorOffset, String name) {
			if (cleanupXHTML)
				correctResponse(response, priorOffset);
			if (element instanceof WOInput) {
				NSMutableDictionary dict = ERXWOContext.contextDictionary();
				NSMutableArray elementArray = (NSMutableArray) dict.objectForKey("elementArray");
				if (elementArray == null)
					elementArray = new NSMutableArray(10);
				elementArray.addObject(new NSDictionary(new Object[] { element.getClass().getName(), name == null ? "NULL" : name }, new String[] { "type", "name" }));
				dict.setObjectForKey(elementArray, "elementArray");
			}
		}

		/**
		 * Corrects the response of dynamic elements to be XHTML-conformant. <code>input</code>- and <code>img</code>-tags
		 * will be closed correctly, all attribute values will be quoted and attributes without a value like
		 * <code>disabled</code> will get a quoted value. All attribute-values with uncorrectly escaped ampersands
		 * (&amp;) will be corrected. E.g. <code>&quot;w&amp;amp;auml;hlen&quot;</code> will become
		 * <code>&quot;w&amp;auml;hlen&quot;</code>.<br/> This method would normally be called in the following way:
		 * 
		 * <pre>
		 * public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
		 * 	String pre = woresponse.contentString();
		 * 	super.appendToResponse(woresponse, wocontext);
		 * 	correctResponse(woresponse, pre.length(), pre);
		 * }
		 * </pre>
		 * 
		 * @param response
		 *            the response to be corrected.
		 * @param start
		 *            the offset to start from.
		 * @param pre
		 *            the string which should be inserted at the begin of the response.
		 */
		public static final void correctResponse(WOResponse response, int start) {
			String string = response.contentString();
			int length = string.length();
			StringBuffer buf = new StringBuffer(length);
			// buf.append(string.substring(0, start));
			char[] characters = new char[start];
			string.getChars(0, start, characters, 0);
			buf.append(characters);

			for (int i = start; i < length; i++) {
				char ch = string.charAt(i);

				switch (ch) {

				case '<':
					buf.append('<');
					i = consumeTag(string, i + 1, buf);
					break;

				case '>':
					break;

				default:
					buf.append(ch);
					break;
				}
			}

			response.setContent(buf.toString());
		}

		private static final int consumeTag(String string, int index, StringBuffer buf) {
			StringBuffer tagName = new StringBuffer();
			int i = index;
			int length = string.length();

			for (; i < length; i++) {
				char ch = string.charAt(i);

				if (ch == ' ') {
					break;

				}
				else {
					tagName.append(ch);
				}
			}

			buf.append(tagName);

			for (; i < length; i++) {
				char ch = string.charAt(i);
				buf.append(ch);

				switch (ch) {

				case ' ':
					i = consumeAttributeName(string, i + 1, buf);
					break;

				case '=':
					i = consumeAttributeValue(string, i + 1, buf);
					break;

				case '>':
					String t = tagName.toString();

					if ("img".equals(t) || "input".equals(t)) {
						buf.append("</").append(tagName).append(">");
					}

					return i - 1;
				}

			}

			return length;
		}

		private static final int consumeAttributeValue(String string, int index, StringBuffer buf) {
			int length = string.length();
			boolean hasQuotes;
			int i;

			buf.append('"');

			if (string.charAt(index) != '"') {
				hasQuotes = false;

				i = index;

			}
			else {
				hasQuotes = true;
				i = index + 1;
			}

			for (; i < length; i++) {
				char ch = string.charAt(i);
				buf.append(ch);

				switch (ch) {

				case '&':
					// check if there is &amp; when it should not
					if (i + 5 < length && string.substring(i + 1, i + 4).equals("amp")) {
						int semi = string.indexOf(';', i + 5);
						if (semi != -1) {
							String ent = string.substring(i + 5, semi);
							if (EntityTable.getDefaultEntityTable().entityCode("&" + ent) != 0) {
								// we have a valid entity. thank you, Apple!
								buf.append(ent).append(';');
								i = semi;
							}
						}
					}
					break;
				case '"':

					if (hasQuotes) {
						return i;

					}
					else {
						hasQuotes = true;
					}

				case ' ':

					if (!hasQuotes) {
						buf.deleteCharAt(buf.length() - 1);
						buf.append("\"");
						return i - 1;
					}

				case '>':

					if (!hasQuotes) {
						buf.deleteCharAt(buf.length() - 1);
						buf.append("\"");
						return i - 1;
					}

				default:
				}
			}

			return length;
		}

		private static final int consumeAttributeName(String string, int index, StringBuffer buf) {
			StringBuffer attName = new StringBuffer();
			int length = string.length();

			for (int i = index; i < length; i++) {
				char ch = string.charAt(i);

				switch (ch) {

				case '=':
					buf.append(attName);
					return i - 1;

				case ' ':

				case '>':
					buf.append(attName).append("=\"").append(attName).append("\"");
					return i - 1;

				default:
					attName.append(ch);
				}
			}

			return length;
		}

		/**
		 * This method adds missing quotes to the given attribute string. E.g.
		 * <code>type=text name=&quot;mytext&quot;</code> will be corrected to
		 * <code>type=&quot;text&quot; name=&quot;mytext&quot;</code>
		 * 
		 * @param atts
		 *            a string of attributes.
		 * 
		 * @return the corrected string.
		 */
		public static final String addQuotes(String atts) {
			int len = atts.length();
			boolean attVal = false;
			boolean addQuote = false;
			StringBuffer buf = null;
			int i;

			for (i = 0; i < len; i++) {
				char ch = atts.charAt(i);

				if (buf != null) {
					buf.append(ch);
				}

				if (ch == '=' && !attVal) {
					attVal = true;

					if (atts.charAt(i + 1) != '"') {
						buf = getBuffer(atts, i, buf);
						buf.append('"');
						addQuote = true;
					}

				}
				else if (ch == ' ') {
					if (addQuote && attVal) {
						buf = getBuffer(atts, i, buf);
						buf.deleteCharAt(buf.length() - 1);
						buf.append("\" ");
						addQuote = false;
						attVal = false;
					}

				}
				else if (ch == '"') {
					attVal = false;
					addQuote = false;
				}
			}

			if (addQuote && attVal) {
				buf = getBuffer(atts, i, buf);
				buf.append('"');
				addQuote = false;
				attVal = false;
			}

			if (buf != null) {
				return buf.toString();

			}
			else {
				return atts;
			}
		}

		private static final StringBuffer getBuffer(String src, int len, StringBuffer buf) {
			if (buf == null) {
				StringBuffer ret = new StringBuffer();

				for (int j = 0; j <= len; j++) {
					ret.append(src.charAt(j));
				}

				return ret;

			}
			else {
				return buf;
			}
		}

	}

	/*
	 * the rest is shamelessly copied over from jtidy, but I put it here to avoid having to add it to ERJars for the
	 * moment
	 */
	public static class Entity {
		public String name;
		public short code;

		public Entity(String string, int i) {
			name = string;
			code = (short) i;
		}

		public Entity(String string, short i) {
			name = string;
			code = i;
		}
	}

	public static class EntityTable {
		private Hashtable entityHashtable = new Hashtable();
		private static EntityTable defaultEntityTable = null;
		private static Entity[] entities = { new Entity("nbsp", (int) 160), new Entity("iexcl", (int) 161), new Entity("cent", (int) 162), new Entity("pound", (int) 163), new Entity("curren", (int) 164), new Entity("yen", (int) 165), new Entity("brvbar", (int) 166), new Entity("sect", (int) 167), new Entity("uml", (int) 168), new Entity("copy", (int) 169), new Entity("ordf", (int) 170), new Entity("laquo", (int) 171), new Entity("not", (int) 172), new Entity("shy", (int) 173), new Entity("reg", (int) 174), new Entity("macr", (int) 175), new Entity("deg", (int) 176), new Entity("plusmn", (int) 177), new Entity("sup2", (int) 178), new Entity("sup3", (int) 179), new Entity("acute", (int) 180), new Entity("micro", (int) 181), new Entity("para", (int) 182), new Entity("middot", (int) 183), new Entity("cedil", (int) 184), new Entity("sup1", (int) 185), new Entity("ordm", (int) 186), new Entity("raquo", (int) 187), new Entity("frac14", (int) 188), new Entity("frac12", (int) 189),
				new Entity("frac34", (int) 190), new Entity("iquest", (int) 191), new Entity("Agrave", (int) 192), new Entity("Aacute", (int) 193), new Entity("Acirc", (int) 194), new Entity("Atilde", (int) 195), new Entity("Auml", (int) 196), new Entity("Aring", (int) 197), new Entity("AElig", (int) 198), new Entity("Ccedil", (int) 199), new Entity("Egrave", (int) 200), new Entity("Eacute", (int) 201), new Entity("Ecirc", (int) 202), new Entity("Euml", (int) 203), new Entity("Igrave", (int) 204), new Entity("Iacute", (int) 205), new Entity("Icirc", (int) 206), new Entity("Iuml", (int) 207), new Entity("ETH", (int) 208), new Entity("Ntilde", (int) 209), new Entity("Ograve", (int) 210), new Entity("Oacute", (int) 211), new Entity("Ocirc", (int) 212), new Entity("Otilde", (int) 213), new Entity("Ouml", (int) 214), new Entity("times", (int) 215), new Entity("Oslash", (int) 216), new Entity("Ugrave", (int) 217), new Entity("Uacute", (int) 218), new Entity("Ucirc", (int) 219),
				new Entity("Uuml", (int) 220), new Entity("Yacute", (int) 221), new Entity("THORN", (int) 222), new Entity("szlig", (int) 223), new Entity("agrave", (int) 224), new Entity("aacute", (int) 225), new Entity("acirc", (int) 226), new Entity("atilde", (int) 227), new Entity("auml", (int) 228), new Entity("aring", (int) 229), new Entity("aelig", (int) 230), new Entity("ccedil", (int) 231), new Entity("egrave", (int) 232), new Entity("eacute", (int) 233), new Entity("ecirc", (int) 234), new Entity("euml", (int) 235), new Entity("igrave", (int) 236), new Entity("iacute", (int) 237), new Entity("icirc", (int) 238), new Entity("iuml", (int) 239), new Entity("eth", (int) 240), new Entity("ntilde", (int) 241), new Entity("ograve", (int) 242), new Entity("oacute", (int) 243), new Entity("ocirc", (int) 244), new Entity("otilde", (int) 245), new Entity("ouml", (int) 246), new Entity("divide", (int) 247), new Entity("oslash", (int) 248), new Entity("ugrave", (int) 249),
				new Entity("uacute", (int) 250), new Entity("ucirc", (int) 251), new Entity("uuml", (int) 252), new Entity("yacute", (int) 253), new Entity("thorn", (int) 254), new Entity("yuml", (int) 255), new Entity("fnof", (int) 402), new Entity("Alpha", (int) 913), new Entity("Beta", (int) 914), new Entity("Gamma", (int) 915), new Entity("Delta", (int) 916), new Entity("Epsilon", (int) 917), new Entity("Zeta", (int) 918), new Entity("Eta", (int) 919), new Entity("Theta", (int) 920), new Entity("Iota", (int) 921), new Entity("Kappa", (int) 922), new Entity("Lambda", (int) 923), new Entity("Mu", (int) 924), new Entity("Nu", (int) 925), new Entity("Xi", (int) 926), new Entity("Omicron", (int) 927), new Entity("Pi", (int) 928), new Entity("Rho", (int) 929), new Entity("Sigma", (int) 931), new Entity("Tau", (int) 932), new Entity("Upsilon", (int) 933), new Entity("Phi", (int) 934), new Entity("Chi", (int) 935), new Entity("Psi", (int) 936), new Entity("Omega", (int) 937),
				new Entity("alpha", (int) 945), new Entity("beta", (int) 946), new Entity("gamma", (int) 947), new Entity("delta", (int) 948), new Entity("epsilon", (int) 949), new Entity("zeta", (int) 950), new Entity("eta", (int) 951), new Entity("theta", (int) 952), new Entity("iota", (int) 953), new Entity("kappa", (int) 954), new Entity("lambda", (int) 955), new Entity("mu", (int) 956), new Entity("nu", (int) 957), new Entity("xi", (int) 958), new Entity("omicron", (int) 959), new Entity("pi", (int) 960), new Entity("rho", (int) 961), new Entity("sigmaf", (int) 962), new Entity("sigma", (int) 963), new Entity("tau", (int) 964), new Entity("upsilon", (int) 965), new Entity("phi", (int) 966), new Entity("chi", (int) 967), new Entity("psi", (int) 968), new Entity("omega", (int) 969), new Entity("thetasym", (int) 977), new Entity("upsih", (int) 978), new Entity("piv", (int) 982), new Entity("bull", (int) 8226), new Entity("hellip", (int) 8230), new Entity("prime", (int) 8242),
				new Entity("Prime", (int) 8243), new Entity("oline", (int) 8254), new Entity("frasl", (int) 8260), new Entity("weierp", (int) 8472), new Entity("image", (int) 8465), new Entity("real", (int) 8476), new Entity("trade", (int) 8482), new Entity("alefsym", (int) 8501), new Entity("larr", (int) 8592), new Entity("uarr", (int) 8593), new Entity("rarr", (int) 8594), new Entity("darr", (int) 8595), new Entity("harr", (int) 8596), new Entity("crarr", (int) 8629), new Entity("lArr", (int) 8656), new Entity("uArr", (int) 8657), new Entity("rArr", (int) 8658), new Entity("dArr", (int) 8659), new Entity("hArr", (int) 8660), new Entity("forall", (int) 8704), new Entity("part", (int) 8706), new Entity("exist", (int) 8707), new Entity("empty", (int) 8709), new Entity("nabla", (int) 8711), new Entity("isin", (int) 8712), new Entity("notin", (int) 8713), new Entity("ni", (int) 8715), new Entity("prod", (int) 8719), new Entity("sum", (int) 8721), new Entity("minus", (int) 8722),
				new Entity("lowast", (int) 8727), new Entity("radic", (int) 8730), new Entity("prop", (int) 8733), new Entity("infin", (int) 8734), new Entity("ang", (int) 8736), new Entity("and", (int) 8743), new Entity("or", (int) 8744), new Entity("cap", (int) 8745), new Entity("cup", (int) 8746), new Entity("int", (int) 8747), new Entity("there4", (int) 8756), new Entity("sim", (int) 8764), new Entity("cong", (int) 8773), new Entity("asymp", (int) 8776), new Entity("ne", (int) 8800), new Entity("equiv", (int) 8801), new Entity("le", (int) 8804), new Entity("ge", (int) 8805), new Entity("sub", (int) 8834), new Entity("sup", (int) 8835), new Entity("nsub", (int) 8836), new Entity("sube", (int) 8838), new Entity("supe", (int) 8839), new Entity("oplus", (int) 8853), new Entity("otimes", (int) 8855), new Entity("perp", (int) 8869), new Entity("sdot", (int) 8901), new Entity("lceil", (int) 8968), new Entity("rceil", (int) 8969), new Entity("lfloor", (int) 8970),
				new Entity("rfloor", (int) 8971), new Entity("lang", (int) 9001), new Entity("rang", (int) 9002), new Entity("loz", (int) 9674), new Entity("spades", (int) 9824), new Entity("clubs", (int) 9827), new Entity("hearts", (int) 9829), new Entity("diams", (int) 9830), new Entity("quot", (int) 34), new Entity("amp", (int) 38), new Entity("lt", (int) 60), new Entity("gt", (int) 62), new Entity("OElig", (int) 338), new Entity("oelig", (int) 339), new Entity("Scaron", (int) 352), new Entity("scaron", (int) 353), new Entity("Yuml", (int) 376), new Entity("circ", (int) 710), new Entity("tilde", (int) 732), new Entity("ensp", (int) 8194), new Entity("emsp", (int) 8195), new Entity("thinsp", (int) 8201), new Entity("zwnj", (int) 8204), new Entity("zwj", (int) 8205), new Entity("lrm", (int) 8206), new Entity("rlm", (int) 8207), new Entity("ndash", (int) 8211), new Entity("mdash", (int) 8212), new Entity("lsquo", (int) 8216), new Entity("rsquo", (int) 8217),
				new Entity("sbquo", (int) 8218), new Entity("ldquo", (int) 8220), new Entity("rdquo", (int) 8221), new Entity("bdquo", (int) 8222), new Entity("dagger", (int) 8224), new Entity("Dagger", (int) 8225), new Entity("permil", (int) 8240), new Entity("lsaquo", (int) 8249), new Entity("rsaquo", (int) 8250), new Entity("euro", (int) 8364) };

		public short entityCode(String string) {
			if (string.length() <= 1)
				return (short) 0;
			if (string.charAt(1) == '#') {
				int i = 0;
				try {
					if (string.length() >= 4 && string.charAt(2) == 'x')
						i = Integer.parseInt(string.substring(3), 16);
					else if (string.length() >= 3)
						i = Integer.parseInt(string.substring(2));
				}
				catch (NumberFormatException numberformatexception) {
					/* empty */
				}
				return (short) i;
			}
			Entity entity = lookup(string.substring(1));
			if (entity != null)
				return entity.code;
			return (short) 0;
		}

		public String entityName(short i) {
			String string = null;
			Enumeration enumeration = entityHashtable.elements();
			while (enumeration.hasMoreElements()) {
				Entity entity = (Entity) enumeration.nextElement();
				if (entity.code == i) {
					string = entity.name;
					break;
				}
			}
			return string;
		}

		public static EntityTable getDefaultEntityTable() {
			if (defaultEntityTable == null) {
				defaultEntityTable = new EntityTable();
				for (int i = 0; i < entities.length; i++)
					defaultEntityTable.install(entities[i]);
			}
			return defaultEntityTable;
		}

		public Entity install(String string, short i) {
			Entity entity = lookup(string);
			if (entity == null) {
				entity = new Entity(string, i);
				entityHashtable.put(string, entity);
			}
			else
				entity.code = i;
			return entity;
		}

		public Entity install(Entity entity) {
			return (Entity) entityHashtable.put(entity.name, entity);
		}

		public Entity lookup(String string) {
			return (Entity) entityHashtable.get(string);
		}
	}
}
