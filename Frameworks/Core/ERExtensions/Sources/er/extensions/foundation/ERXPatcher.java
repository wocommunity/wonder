package er.extensions.foundation;

import java.lang.reflect.Constructor;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.List;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WODynamicElement;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOActiveImage;
import com.webobjects.appserver._private.WOBrowser;
import com.webobjects.appserver._private.WOCheckBox;
import com.webobjects.appserver._private.WOCheckBoxList;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WOGenericContainer;
import com.webobjects.appserver._private.WOGenericElement;
import com.webobjects.appserver._private.WOHiddenField;
import com.webobjects.appserver._private.WOImage;
import com.webobjects.appserver._private.WOImageButton;
import com.webobjects.appserver._private.WOInput;
import com.webobjects.appserver._private.WOJavaScript;
import com.webobjects.appserver._private.WOPasswordField;
import com.webobjects.appserver._private.WOPopUpButton;
import com.webobjects.appserver._private.WORadioButton;
import com.webobjects.appserver._private.WORadioButtonList;
import com.webobjects.appserver._private.WOResetButton;
import com.webobjects.appserver._private.WOSubmitButton;
import com.webobjects.appserver._private.WOText;
import com.webobjects.appserver._private.WOTextField;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSMutableArray;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation._NSUtilities;

import er.extensions.appserver.ERXResponse;
import er.extensions.appserver.ERXSession;
import er.extensions.appserver.ERXWOContext;
import er.extensions.components._private.ERXHyperlink;
import er.extensions.components._private.ERXSubmitButton;
import er.extensions.components._private.ERXSwitchComponent;
import er.extensions.components._private.ERXWOFileUpload;
import er.extensions.woextensions.WOToManyRelationship;
import er.extensions.woextensions.WOToOneRelationship;

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
		DynamicElementsPatches.useButtonTag = ERXProperties.booleanForKeyWithDefault("er.extensions.foundation.ERXPatcher.DynamicElementsPatches.SubmitButton.useButtonTag", false);
		DynamicElementsPatches.suppressValueBindingSlow = ERXValueUtilities.booleanValueWithDefault(System.getProperty("er.extensions.ERXPatcher.suppressValueBindingSlow"), false);
		
		if (DynamicElementsPatches.useButtonTag) {
			ERXPatcher.setClassForName(ERXSubmitButton.class, "WOSubmitButton");
		} else ERXPatcher.setClassForName(DynamicElementsPatches.SubmitButton.class, "WOSubmitButton");
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
		if (ERXProperties.booleanForKeyWithDefault("er.extensions.WOSwitchComponent.patch", true)) {
			ERXPatcher.setClassForName(ERXSwitchComponent.class, "WOSwitchComponent");
		}
		
		// RM XHTML strict compliance
		ERXPatcher.setClassForName(DynamicElementsPatches.JavaScript.class, "WOJavaScript");
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
		private static boolean useButtonTag = false;
		private static Boolean appendComponentIdentifier;
		public static boolean suppressValueBindingSlow = false;
		
		private DynamicElementsPatches() {
		}

		public static class SubmitButton extends WOSubmitButton {

			public SubmitButton(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}
			
			protected String _valueStringInContext(WOContext context) {
				String valueString = null;
				Object value = _value.valueInComponent(context.component());
				if (value != null) {
					valueString = value.toString();
				}
				return valueString;
			}

			/**
			 * Appends the attribute "value" to the response. First tries to get a localized version and if that fails,
			 * uses the supplied value as the default
			 */
			@Override
			protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
				if (_value != null) {
					String valueString = _valueStringInContext(context);
					if (valueString != null) {
						// stringValue = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(stringValue);
						response._appendTagAttributeAndValue("value", valueString, escapeHTMLInContext(context));
					}
				}
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}

			/*
			 * logs the action name into session's dictionary with a key = ERXActionLogging
			 */
			@Override
			public WOActionResults invokeAction(WORequest arg0, WOContext arg1) {
				WOActionResults result = super.invokeAction(arg0, arg1);
				if (result != null && _action != null && ERXSession.anySession() != null) {
					ERXSession.anySession().setObjectForKey(toString(), "ERXActionLogging");
				}
				return result;
			}

		}

		public static class ResetButton extends WOResetButton {

			public ResetButton(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			/**
			 * Appends the attribute "value" to the response. First tries to get a localized version and if that fails,
			 * uses the supplied value as the default
			 */
			@Override
			protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
				if (_value != null) {
					Object object = _value.valueInComponent(context.component());
					if (object != null) {
						String string = object.toString();
						// string = ERXLocalizer.currentLocalizer().localizedStringForKeyWithDefault(string);
						response._appendTagAttributeAndValue("value", string, escapeHTMLInContext(context));
					}
				}
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
		}

		public static class GenericContainer extends WOGenericContainer {

			public GenericContainer(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
				super.appendAttributesToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, null);
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
		}

		public static class GenericElement extends WOGenericElement {

			public GenericElement(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
				super.appendAttributesToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, null);
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
		}

		public static class Image extends WOImage {

			public Image(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
				super.appendAttributesToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, null);
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
		}

		public static class ActiveImage extends WOActiveImage {

			public ActiveImage(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
			}

			@Override
			protected void appendConstantAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
				super.appendConstantAttributesToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}

			/*
			 * logs the action name into session's dictionary with a key = ERXActionLogging if log is set to debug.
			 */
			@Override
			public WOActionResults invokeAction(WORequest arg0, WOContext arg1) {
				WOActionResults result = super.invokeAction(arg0, arg1);
				if (result != null && ERXSession.anySession() != null) {
					ERXSession.anySession().setObjectForKey(toString(), "ERXActionLogging");
				}
				return result;
			}

		}

		public static class TextField extends WOTextField {
			protected WOAssociation _readonly;

			public TextField(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_readonly = _associations.removeObjectForKey("readonly");
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
				if (_readonly != null && _readonly.booleanValueInComponent(wocontext.component())) {
					woresponse._appendTagAttributeAndValue("readonly", "readonly", false);
				}
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
			
			/**
			 * If readonly attribute is set to <code>true</code> prevent the takeValuesFromRequest.
			 */
			@Override
			public void takeValuesFromRequest(WORequest aRequest, WOContext wocontext) {
				WOComponent aComponent = wocontext.component();
				Boolean readOnly = false;
				if (_readonly != null) {
					readOnly = _readonly.booleanValueInComponent(aComponent);
				}
				if (!readOnly) {
					super.takeValuesFromRequest(aRequest, wocontext);
				}
			}
		}

		public static class Text extends WOText {
			protected WOAssociation _readonly;

			public Text(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_readonly = _associations.removeObjectForKey("readonly");
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
				if (_readonly != null && _readonly.booleanValueInComponent(wocontext.component())) {
					woresponse._appendTagAttributeAndValue("readonly", "readonly", false);
				}
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
			
			/**
			 * If readonly attribute is set to <code>true</code> prevent the takeValuesFromRequest.
			 */
			@Override
			public void takeValuesFromRequest(WORequest aRequest, WOContext wocontext) {
				WOComponent aComponent = wocontext.component();
				Boolean readOnly = false;
				if (_readonly != null) {
					readOnly = _readonly.booleanValueInComponent(aComponent);
				}
				if (!readOnly) {
					super.takeValuesFromRequest(aRequest, wocontext);
				}
			}
		}

		public static class PopUpButton extends WOPopUpButton {

			public PopUpButton(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_loggedSlow = suppressValueBindingSlow;
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			/* select element shouldn't worry about value attribute */
			@Override
			protected void _appendValueAttributeToResponse(WOResponse response, WOContext context) {
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
			
			/**
			 * Overridden to stop swallowing all exceptions and properly handle
			 * listClassInContext(WOContext) returning an NSArray.
			 * 
			 * This method isn't actually used by WOPopUpButton, but just in case...
			 */
			@Override
			protected void setSelectionListInContext(WOContext context, List selections) {
				if(_selections != null && _selections.isValueSettable()) {
					try {
						Class resultClass = listClassInContext(context);
						Object result = resultClass.newInstance();
						if(result instanceof NSMutableArray) {
							((NSMutableArray)result).addObjects(selections.toArray());
						} else if (result instanceof NSArray) {
							/*
							 * If "result" is an instanceof NSArray, we need to
							 * assign a new NSArray instance containing the
							 * contents of the "selections" parameter instead of
							 * calling addAll(Collection) on the existing
							 * instance because NSArray does not support it.
							 * 
							 * We are using reflection to do the assignment in
							 * case resultClass is actually a subclass of
							 * NSArray.
							 */
							Class nsArrayArgTypes[] = new Class[] {List.class, Boolean.TYPE};
							Constructor nsArrayConstructor = resultClass.getConstructor(nsArrayArgTypes);
							Object nsArrayConstructorArgs[] = new Object[] {selections, Boolean.TRUE};
							result = nsArrayConstructor.newInstance(nsArrayConstructorArgs);
						} else { 
							if(result instanceof List) {
								((List)result).addAll(selections);
							}
						}
						_selections.setValue(result, context.component());
                    } catch(Exception exception) {
                    	/*
                    	 * Don't ignore Exceptions like WOInputList does. Throw.
                    	 */
                    	throw NSForwardException._runtimeExceptionForThrowable(exception);
                    }
				}
			}
			
			/**
			 * Overridden to make the default return {@link Class} a
			 * NSMutableArray instead of NSArray.
			 * 
			 * @return a <b>mutable</b> Class that implements {@link List}
			 */
			@Override
			protected Class<List> listClassInContext(WOContext context) {
				Class aListClass = NSMutableArray.class;
				if (_list != null) {
					Object value = _list.valueInComponent(context.component());
					if (value instanceof NSArray)
						aListClass = NSMutableArray.class;
					else if (value instanceof List)
						aListClass = value.getClass();
				}
				return aListClass;
			}
		}

		public static class Browser extends WOBrowser {

			public Browser(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_loggedSlow = suppressValueBindingSlow;
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
			
			/**
			 * Overridden to stop swallowing all exceptions and properly handle
			 * listClassInContext(WOContext) returning an NSArray.
			 */
			@Override
			protected void setSelectionListInContext(WOContext context, List selections) {
				if(_selections != null && _selections.isValueSettable()) {
					try {
						Class resultClass = listClassInContext(context);
						Object result = resultClass.newInstance();
						if(result instanceof NSMutableArray) {
							((NSMutableArray)result).addObjects(selections.toArray());
						} else if (result instanceof NSArray) {
							/*
							 * If "result" is an instanceof NSArray, we need to
							 * assign a new NSArray instance containing the
							 * contents of the "selections" parameter instead of
							 * calling addAll(Collection) on the existing
							 * instance because NSArray does not support it.
							 * 
							 * We are using reflection to do the assignment in
							 * case resultClass is actually a subclass of
							 * NSArray.
							 */
							Class nsArrayArgTypes[] = new Class[] {List.class, Boolean.TYPE};
							Constructor nsArrayConstructor = resultClass.getConstructor(nsArrayArgTypes);
							Object nsArrayConstructorArgs[] = new Object[] {selections, Boolean.TRUE};
							result = nsArrayConstructor.newInstance(nsArrayConstructorArgs);
						} else { 
							if(result instanceof List) {
								((List)result).addAll(selections);
							}
						}
						_selections.setValue(result, context.component());
                    } catch(Exception exception) {
                    	/*
                    	 * Don't ignore Exceptions like WOInputList does. Throw.
                    	 */
                    	throw NSForwardException._runtimeExceptionForThrowable(exception);
                    }
				}
			}
			
			/**
			 * Overridden to make the default return {@link Class} a
			 * NSMutableArray instead of NSArray.
			 * 
			 * @return a <b>mutable</b> Class that implements {@link List}
			 */
			@Override
			protected Class<List> listClassInContext(WOContext context) {
				Class aListClass = NSMutableArray.class;
				if (_list != null) {
					Object value = _list.valueInComponent(context.component());
					if (value instanceof NSArray)
						aListClass = NSMutableArray.class;
					else if (value instanceof List)
						aListClass = value.getClass();
				}
				return aListClass;
			}

		}

		public static class CheckBox extends WOCheckBox {

			public CheckBox(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
		}

		public static class CheckBoxList extends WOCheckBoxList {

			public CheckBoxList(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
			
			/**
			 * Overridden to stop swallowing all exceptions and properly handle
			 * listClassInContext(WOContext) returning an NSArray.
			 */
			@Override
			protected void setSelectionListInContext(WOContext context, List selections) {
				if(_selections != null && _selections.isValueSettable()) {
					try {
						Class resultClass = listClassInContext(context);
						Object result = resultClass.newInstance();
						if(result instanceof NSMutableArray) {
							((NSMutableArray)result).addObjects(selections.toArray());
						} else if (result instanceof NSArray) {
							/*
							 * If "result" is an instanceof NSArray, we need to
							 * assign a new NSArray instance containing the
							 * contents of the "selections" parameter instead of
							 * calling addAll(Collection) on the existing
							 * instance because NSArray does not support it.
							 * 
							 * We are using reflection to do the assignment in
							 * case resultClass is actually a subclass of
							 * NSArray.
							 */
							Class nsArrayArgTypes[] = new Class[] {List.class, Boolean.TYPE};
							Constructor nsArrayConstructor = resultClass.getConstructor(nsArrayArgTypes);
							Object nsArrayConstructorArgs[] = new Object[] {selections, Boolean.TRUE};
							result = nsArrayConstructor.newInstance(nsArrayConstructorArgs);
						} else { 
							if(result instanceof List) {
								((List)result).addAll(selections);
							}
						}
						_selections.setValue(result, context.component());
                    } catch(Exception exception) {
                    	/*
                    	 * Don't ignore Exceptions like WOInputList does. Throw.
                    	 */
                    	throw NSForwardException._runtimeExceptionForThrowable(exception);
                    }
				}
			}
			
			/**
			 * Overridden to make the default return {@link Class} a
			 * NSMutableArray instead of NSArray.
			 * 
			 * @return a <b>mutable</b> Class that implements {@link List}
			 */
			@Override
			protected Class<List> listClassInContext(WOContext context) {
				Class aListClass = NSMutableArray.class;
				if (_list != null) {
					Object value = _list.valueInComponent(context.component());
					if (value instanceof NSArray)
						aListClass = NSMutableArray.class;
					else if (value instanceof List)
						aListClass = value.getClass();
				}
				return aListClass;
			}

		}

		public static class FileUpload extends ERXWOFileUpload {

			public FileUpload(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
		}

		public static class HiddenField extends WOHiddenField {
			protected WOAssociation _readonly;

			public HiddenField(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_readonly = _associations.removeObjectForKey("readonly");
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
				if (_readonly != null && _readonly.booleanValueInComponent(wocontext.component())) {
					woresponse._appendTagAttributeAndValue("readonly", "readonly", false);
				}
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
			
			/**
			 * If readonly attribute is set to <code>true</code> prevent the takeValuesFromRequest.
			 */
			@Override
			public void takeValuesFromRequest(WORequest aRequest, WOContext wocontext) {
				WOComponent aComponent = wocontext.component();
				Boolean readOnly = false;
				if (_readonly != null) {
					readOnly = _readonly.booleanValueInComponent(aComponent);
				}
				if (!readOnly) {
					super.takeValuesFromRequest(aRequest, wocontext);
				}
			}
		}

		public static class ImageButton extends WOImageButton {

			public ImageButton(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
		}

		public static class PasswordField extends WOPasswordField {
			protected WOAssociation _readonly;

			public PasswordField(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				_readonly = _associations.removeObjectForKey("readonly");
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
				if (_readonly != null && _readonly.booleanValueInComponent(wocontext.component())) {
					woresponse._appendTagAttributeAndValue("readonly", "readonly", false);
				}
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
			
			/**
			 * If readonly attribute is set to <code>true</code> prevent the takeValuesFromRequest.
			 */
			@Override
			public void takeValuesFromRequest(WORequest aRequest, WOContext wocontext) {
				WOComponent aComponent = wocontext.component();
				Boolean readOnly = false;
				if (_readonly != null) {
					readOnly = _readonly.booleanValueInComponent(aComponent);
				}
				if (!readOnly) {
					super.takeValuesFromRequest(aRequest, wocontext);
				}
			}
		}

		public static class RadioButton extends WORadioButton {

			public RadioButton(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
		}

		public static class RadioButtonList extends WORadioButtonList {

			public RadioButtonList(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
			}

			@Override
			protected void _appendNameAttributeToResponse(WOResponse woresponse, WOContext wocontext) {
				super._appendNameAttributeToResponse(woresponse, wocontext);
				appendIdentifierTagAndValue(this, _id, woresponse, wocontext);
			}

			@Override
			public void appendToResponse(WOResponse woresponse, WOContext wocontext) {
				WOResponse newResponse = cleanupXHTML ? new ERXResponse() : woresponse;
				super.appendToResponse(newResponse, wocontext);

				processResponse(this, newResponse, wocontext, 0, nameInContext(wocontext, wocontext.component()));
				if (ERXPatcher.DynamicElementsPatches.cleanupXHTML) {
					woresponse.appendContentString(newResponse.contentString());
				}
			}
			
			/**
			 * Overridden to stop swallowing all exceptions and properly handle
			 * listClassInContext(WOContext) returning an NSArray.
			 */
			@Override
			protected void setSelectionListInContext(WOContext context, List selections) {
				if(_selections != null && _selections.isValueSettable()) {
					try {
						Class resultClass = listClassInContext(context);
						Object result = resultClass.newInstance();
						if(result instanceof NSMutableArray) {
							((NSMutableArray)result).addObjects(selections.toArray());
						} else if (result instanceof NSArray) {
							/*
							 * If "result" is an instanceof NSArray, we need to
							 * assign a new NSArray instance containing the
							 * contents of the "selections" parameter instead of
							 * calling addAll(Collection) on the existing
							 * instance because NSArray does not support it.
							 * 
							 * We are using reflection to do the assignment in
							 * case resultClass is actually a subclass of
							 * NSArray.
							 */
							Class nsArrayArgTypes[] = new Class[] {List.class, Boolean.TYPE};
							Constructor nsArrayConstructor = resultClass.getConstructor(nsArrayArgTypes);
							Object nsArrayConstructorArgs[] = new Object[] {selections, Boolean.TRUE};
							result = nsArrayConstructor.newInstance(nsArrayConstructorArgs);
						} else { 
							if(result instanceof List) {
								((List)result).addAll(selections);
							}
						}
						_selections.setValue(result, context.component());
                    } catch(Exception exception) {
                    	/*
                    	 * Don't ignore Exceptions like WOInputList does. Throw.
                    	 */
                    	throw NSForwardException._runtimeExceptionForThrowable(exception);
                    }
				}
			}
			
			/**
			 * Overridden to make the default return {@link Class} a
			 * NSMutableArray instead of NSArray.
			 * 
			 * @return a <b>mutable</b> Class that implements {@link List}
			 */
			@Override
			protected Class<List> listClassInContext(WOContext context) {
				Class aListClass = NSMutableArray.class;
				if (_list != null) {
					Object value = _list.valueInComponent(context.component());
					if (value instanceof NSArray)
						aListClass = NSMutableArray.class;
					else if (value instanceof List)
						aListClass = value.getClass();
				}
				return aListClass;
			}

		}
		
		public static class JavaScript extends WOJavaScript {
			public static boolean removeLanguageAttribute = ERXProperties.booleanForKeyWithDefault("er.extensions.foundation.ERXPatcher.DynamicElementsPatches.Javascript.removeLanguageAttribute", false);
			private WOAssociation _language;
			
			public JavaScript(String aName, NSDictionary associations, WOElement element) {
				super(aName, associations, element);
				if (_language == null) {
					_language = (WOAssociation) associations.objectForKey("language");
				}
			}
			
			@Override
			protected void setLanguage(String s) {
				super.setLanguage(s);
				if (s != null) {
					_language = new WOConstantValueAssociation(s);
				}
			}
			
			@Override
			public void appendAttributesToResponse(WOResponse woresponse, WOContext wocontext) {
				if (woresponse instanceof ERXResponse && JavaScript.removeLanguageAttribute) {
					// 5.3 + 5.4 hackaround to pop the language attribute off of the script tag 
					ERXResponse response = (ERXResponse)woresponse;
					response.pushContent();
					super.appendAttributesToResponse(woresponse, wocontext);
					String contentString = response.contentString();
					String language = (String)_language.valueInComponent(wocontext.component());
					Pattern pattern = Pattern.compile("\\s*language\\s*=\\s*\"?" + language + "\"?", Pattern.CASE_INSENSITIVE);
					contentString = pattern.matcher(contentString).replaceFirst("");
					response.setContent(contentString);
					response.popContent(true);
				}
				else {
					super.appendAttributesToResponse(woresponse, wocontext);
				}
			}
			
//		    public void _appendTagAttributeAndValueToResponse(WOResponse response, String tagName, String tagValue, boolean escapeHTML) {
//		    	if (!tagName.equals("language")) super._appendTagAttributeAndValueToResponse(response, tagName, tagValue, escapeHTML);	// RM: Hack to void the language attribute
//		    }
		}

		/**
		 * Allows you to set the component ID without actually touching the HTML code, by adding a
		 * <code>componentIdentifier</code> entry in the context's mutableUserInfo. This is useful for setting CSS
		 * entries you don't have to code for.
		 */
		public static void appendIdentifierTagAndValue(WODynamicElement element, WOAssociation id, WOResponse response, WOContext context) {
			if (id == null && appendComponentIdentifier()) {
				NSMutableDictionary dict = ERXWOContext.contextDictionary();
				String componentIdentifier = (String) dict.objectForKey("componentIdentifier");
				if (componentIdentifier != null) {
					response._appendTagAttributeAndValue("id", componentIdentifier, true);
					dict.removeObjectForKey("componentIdentifier");
				}
			}
		}

		public static boolean appendComponentIdentifier() {
			if(appendComponentIdentifier == null) {
				appendComponentIdentifier = Boolean.valueOf(ERXProperties.booleanForKeyWithDefault("er.extensions.foundation.ERXPatcher.DynamicElementsPatches.appendComponentIdentifier", true));
			}
			return appendComponentIdentifier.booleanValue();
		}
		
		/**
		 * Fixing up the response for XHTML and adding the element to the array of generated element IDs, so we can use
		 * JavaScript later on. If the given element is an input element, it adds a dictionary {type=element.class,
		 * name=element.elementID} to ERXWOContext.contextDictionary().objectForKey("elementArray")
		 */
		public static void processResponse(WODynamicElement element, WOResponse response, WOContext context, int priorOffset, String name) {
			// MS: I'm not a fan of the cleanupXHTML impl -- seems really heavy-handed. I'd rather
			// patch busted components to generate XHTML more selectively, but I'm open for a
			// discussion on this one.
			if (cleanupXHTML/* || ERXResponse.isXHTML(response)*/) {
				correctResponse(response, priorOffset);
			}
			if (element instanceof WOInput) {
				NSMutableDictionary dict = ERXWOContext.contextDictionary();
				NSMutableArray elementArray = (NSMutableArray) dict.objectForKey("elementArray");
				if (elementArray == null) {
					elementArray = new NSMutableArray(10);
				}
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
			StringBuilder tagName = new StringBuilder();
			int i = index;
			int length = string.length();

			for (; i < length; i++) {
				char ch = string.charAt(i);

				if ( isWhiteSpace(ch) || ch == '>' ) {
					break;
				}
				tagName.append(ch);
			}

			buf.append(tagName);

			for (; i < length; i++) {
				char ch = string.charAt(i);
				
				if( isWhiteSpace(ch) ) {
					buf.append(ch);
					// Consume white space
				} else if( ch == '=' ) {
					buf.append(ch);
					i = consumeAttributeValue(string, i + 1, buf);
				} else if( ch == '>' ) {
					String t = tagName.toString();

					if ("img".equals(t) || "input".equals(t) || "link".equals(t) ) {
						buf.append(" /");
					}
					
					buf.append(ch);
					
					return i - 1;
				} else if (ch == '/' && i+1 < length && string.charAt(i+1) == '>') {
					continue;
				} else {
					i = consumeAttributeName(string, i, buf);
				}

			}

			return length;
		}

		// FIXME This only works with attribute="value". W3C states one can use attribute='value'. This
		// method won't support quotes, only double-quotes
		private static final int consumeAttributeValue(String string, int index, StringBuffer buf) {
			int length = string.length();
			boolean hasQuotes;
			int i = index;
		
			while( isWhiteSpace( string.charAt(i) ) ) {
				i++; // Consume white spaces
			}

			buf.append('"');

			if (string.charAt(i) != '"') {
				hasQuotes = false;
			}
			else {
				hasQuotes = true;
				i++;
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
					hasQuotes = true;

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
			StringBuilder attName = new StringBuilder();
			int length = string.length();
			boolean afterWhiteSpace = false;
			
			for (int i = index; i < length; i++) {
				char ch = string.charAt(i);

				if( ch == '=' ) {
					buf.append(attName);
					return i - 1;
				} else if( isWhiteSpace(ch) ) {
					afterWhiteSpace = true;
					// Just consume the white space, do nothing
				} else if( ch == '>' ) {
					buf.append(attName).append("=\"").append(attName).append("\"");
					return i - 1;
				} else {
					if( afterWhiteSpace ) {
						buf.append(attName).append("=\"").append(attName).append("\"");
						return i - 2;
					}
					attName.append(ch);
				}
			}

			return length;
		}
		
		/**
		 * Returns true if ch is an white space character, false otherwise.
		 * 
		 * @param ch
		 * @return true if ch is white space character, false otherwise.
		 */
		private static final boolean isWhiteSpace( char ch ) {
			return ch == ' ' || ch == '\n' || ch == '\r' || ch == '\t';
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
			return atts;
		}

		private static final StringBuffer getBuffer(String src, int len, StringBuffer buf) {
			if (buf == null) {
				StringBuffer ret = new StringBuffer();

				for (int j = 0; j <= len; j++) {
					ret.append(src.charAt(j));
				}

				return ret;
			}
			return buf;
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
		private static Entity[] entities = { new Entity("nbsp", 160), new Entity("iexcl", 161), new Entity("cent", 162), new Entity("pound", 163), new Entity("curren", 164), new Entity("yen", 165), new Entity("brvbar", 166), new Entity("sect", 167), new Entity("uml", 168), new Entity("copy", 169), new Entity("ordf", 170), new Entity("laquo", 171), new Entity("not", 172), new Entity("shy", 173), new Entity("reg", 174), new Entity("macr", 175), new Entity("deg", 176), new Entity("plusmn", 177), new Entity("sup2", 178), new Entity("sup3", 179), new Entity("acute", 180), new Entity("micro", 181), new Entity("para", 182), new Entity("middot", 183), new Entity("cedil", 184), new Entity("sup1", 185), new Entity("ordm", 186), new Entity("raquo", 187), new Entity("frac14", 188), new Entity("frac12", 189),
				new Entity("frac34", 190), new Entity("iquest", 191), new Entity("Agrave", 192), new Entity("Aacute", 193), new Entity("Acirc", 194), new Entity("Atilde", 195), new Entity("Auml", 196), new Entity("Aring", 197), new Entity("AElig", 198), new Entity("Ccedil", 199), new Entity("Egrave", 200), new Entity("Eacute", 201), new Entity("Ecirc", 202), new Entity("Euml", 203), new Entity("Igrave", 204), new Entity("Iacute", 205), new Entity("Icirc", 206), new Entity("Iuml", 207), new Entity("ETH", 208), new Entity("Ntilde", 209), new Entity("Ograve", 210), new Entity("Oacute", 211), new Entity("Ocirc", 212), new Entity("Otilde", 213), new Entity("Ouml", 214), new Entity("times", 215), new Entity("Oslash", 216), new Entity("Ugrave", 217), new Entity("Uacute", 218), new Entity("Ucirc", 219),
				new Entity("Uuml", 220), new Entity("Yacute", 221), new Entity("THORN", 222), new Entity("szlig", 223), new Entity("agrave", 224), new Entity("aacute", 225), new Entity("acirc", 226), new Entity("atilde", 227), new Entity("auml", 228), new Entity("aring", 229), new Entity("aelig", 230), new Entity("ccedil", 231), new Entity("egrave", 232), new Entity("eacute", 233), new Entity("ecirc", 234), new Entity("euml", 235), new Entity("igrave", 236), new Entity("iacute", 237), new Entity("icirc", 238), new Entity("iuml", 239), new Entity("eth", 240), new Entity("ntilde", 241), new Entity("ograve", 242), new Entity("oacute", 243), new Entity("ocirc", 244), new Entity("otilde", 245), new Entity("ouml", 246), new Entity("divide", 247), new Entity("oslash", 248), new Entity("ugrave", 249),
				new Entity("uacute", 250), new Entity("ucirc", 251), new Entity("uuml", 252), new Entity("yacute", 253), new Entity("thorn", 254), new Entity("yuml", 255), new Entity("fnof", 402), new Entity("Alpha", 913), new Entity("Beta", 914), new Entity("Gamma", 915), new Entity("Delta", 916), new Entity("Epsilon", 917), new Entity("Zeta", 918), new Entity("Eta", 919), new Entity("Theta", 920), new Entity("Iota", 921), new Entity("Kappa", 922), new Entity("Lambda", 923), new Entity("Mu", 924), new Entity("Nu", 925), new Entity("Xi", 926), new Entity("Omicron", 927), new Entity("Pi", 928), new Entity("Rho", 929), new Entity("Sigma", 931), new Entity("Tau", 932), new Entity("Upsilon", 933), new Entity("Phi", 934), new Entity("Chi", 935), new Entity("Psi", 936), new Entity("Omega", 937),
				new Entity("alpha", 945), new Entity("beta", 946), new Entity("gamma", 947), new Entity("delta", 948), new Entity("epsilon", 949), new Entity("zeta", 950), new Entity("eta", 951), new Entity("theta", 952), new Entity("iota", 953), new Entity("kappa", 954), new Entity("lambda", 955), new Entity("mu", 956), new Entity("nu", 957), new Entity("xi", 958), new Entity("omicron", 959), new Entity("pi", 960), new Entity("rho", 961), new Entity("sigmaf", 962), new Entity("sigma", 963), new Entity("tau", 964), new Entity("upsilon", 965), new Entity("phi", 966), new Entity("chi", 967), new Entity("psi", 968), new Entity("omega", 969), new Entity("thetasym", 977), new Entity("upsih", 978), new Entity("piv", 982), new Entity("bull", 8226), new Entity("hellip", 8230), new Entity("prime", 8242),
				new Entity("Prime", 8243), new Entity("oline", 8254), new Entity("frasl", 8260), new Entity("weierp", 8472), new Entity("image", 8465), new Entity("real", 8476), new Entity("trade", 8482), new Entity("alefsym", 8501), new Entity("larr", 8592), new Entity("uarr", 8593), new Entity("rarr", 8594), new Entity("darr", 8595), new Entity("harr", 8596), new Entity("crarr", 8629), new Entity("lArr", 8656), new Entity("uArr", 8657), new Entity("rArr", 8658), new Entity("dArr", 8659), new Entity("hArr", 8660), new Entity("forall", 8704), new Entity("part", 8706), new Entity("exist", 8707), new Entity("empty", 8709), new Entity("nabla", 8711), new Entity("isin", 8712), new Entity("notin", 8713), new Entity("ni", 8715), new Entity("prod", 8719), new Entity("sum", 8721), new Entity("minus", 8722),
				new Entity("lowast", 8727), new Entity("radic", 8730), new Entity("prop", 8733), new Entity("infin", 8734), new Entity("ang", 8736), new Entity("and", 8743), new Entity("or", 8744), new Entity("cap", 8745), new Entity("cup", 8746), new Entity("int", 8747), new Entity("there4", 8756), new Entity("sim", 8764), new Entity("cong", 8773), new Entity("asymp", 8776), new Entity("ne", 8800), new Entity("equiv", 8801), new Entity("le", 8804), new Entity("ge", 8805), new Entity("sub", 8834), new Entity("sup", 8835), new Entity("nsub", 8836), new Entity("sube", 8838), new Entity("supe", 8839), new Entity("oplus", 8853), new Entity("otimes", 8855), new Entity("perp", 8869), new Entity("sdot", 8901), new Entity("lceil", 8968), new Entity("rceil", 8969), new Entity("lfloor", 8970),
				new Entity("rfloor", 8971), new Entity("lang", 9001), new Entity("rang", 9002), new Entity("loz", 9674), new Entity("spades", 9824), new Entity("clubs", 9827), new Entity("hearts", 9829), new Entity("diams", 9830), new Entity("quot", 34), new Entity("amp", 38), new Entity("lt", 60), new Entity("gt", 62), new Entity("OElig", 338), new Entity("oelig", 339), new Entity("Scaron", 352), new Entity("scaron", 353), new Entity("Yuml", 376), new Entity("circ", 710), new Entity("tilde", 732), new Entity("ensp", 8194), new Entity("emsp", 8195), new Entity("thinsp", 8201), new Entity("zwnj", 8204), new Entity("zwj", 8205), new Entity("lrm", 8206), new Entity("rlm", 8207), new Entity("ndash", 8211), new Entity("mdash", 8212), new Entity("lsquo", 8216), new Entity("rsquo", 8217),
				new Entity("sbquo", 8218), new Entity("ldquo", 8220), new Entity("rdquo", 8221), new Entity("bdquo", 8222), new Entity("dagger", 8224), new Entity("Dagger", 8225), new Entity("permil", 8240), new Entity("lsaquo", 8249), new Entity("rsaquo", 8250), new Entity("euro", 8364) };

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
