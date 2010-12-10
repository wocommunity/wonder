package er.modern.look.pages;

import com.webobjects.appserver.WOContext;

import er.directtoweb.pages.templates.ERD2WPickTypePageTemplate;

/**
 * Useful for picking the type of something.  A type being a string description and either radio buttons or checkboxes  displayed to the left.<br />
 * @d2wKey formEncoding
 * @d2wKey uiStyle
 * @d2wKey explanationComponentName
 * @d2wKey explanationString
 * @d2wKey noSelectionString
 * @d2wKey pageWrapperName
 */
//DELETEME This looks like a select page? Except for the popup, which I can't imagine makes sense?
public class ERMODPickTypePage extends ERD2WPickTypePageTemplate {
	
	public ERMODPickTypePage(WOContext wocontext) {
		super(wocontext);
	}

}
