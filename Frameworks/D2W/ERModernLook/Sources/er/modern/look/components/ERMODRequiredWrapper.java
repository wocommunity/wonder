package er.modern.look.components;

import com.webobjects.appserver.WOContext;

import er.extensions.foundation.ERXStringUtilities;

/**
 * Wrapper for most look pages.
 * 
 * @author davidleber
 *
 */
public class ERMODRequiredWrapper extends ERMODComponent {
	
    private String _wrapperClass;
    private String _wrapperId;
	private String _watchedContainerID;
	private String _formName;
	private Boolean _showForm;
	private Boolean _showHelp;

	public ERMODRequiredWrapper(WOContext context) {
        super(context);
    }
	
	@Override
	public boolean synchronizesVariablesWithBindings() {
		return false;
	}
    
	/**
	 * CSS class for the main wrapper.
	 */
	public String wrapperClass() {
		if (_wrapperClass == null) {
			_wrapperClass = stringValueForBinding("class");
		}
		return _wrapperClass;
	}

	public void setWrapperClass(String c) {
		_wrapperClass = c;
	}

	/**
	 * CSS ID fro the main wrapper.
	 */
	public String wrapperId() {
		if (_wrapperId == null) {
			_wrapperId = stringValueForBinding("id");
		}
		return _wrapperId;
	}

	public void setWrapperId(String id) {
		_wrapperId = id;
	}
	
	/**
	 * ID of the watchedContainer for the page's global busy indicator.
	 */
	public String watchedContainerID() {
		if (_watchedContainerID == null) {
			_watchedContainerID = stringValueForBinding("watchedContainerID");
		}
		return _watchedContainerID;
	}

	public void setWatchedContainerID(String id) {
		this._watchedContainerID = id;
	}
	
	/**
	 * Name for the page form. If not supplied this will be the current task with 'Form' appended.
	 * i.e: 'editForm'
	 */
	public String formName() {
		if (_formName == null) {
			_formName = ERXStringUtilities.capitalize(d2wContext().task()) + "Form";	
		}
		return _formName;
	}

	public void setFormName(String n) {
		_formName = n;
	}
	
	/**
	 * Show the form for only those pages that need it.
	 */
	public Boolean showForm() {
		if (_showForm == null) {
			Integer temp = (Integer)d2wContext().valueForKey("hasForm");
			boolean hideForm = booleanValueForBinding("hideForm");
			boolean result = (!hideForm && temp != null && temp.intValue() > 0);
			_showForm = Boolean.valueOf(result);
		}
		return _showForm;
	}

	/**
	 * Only show help for those pages that need it.
	 */
	public Boolean showHelp() {
		if (_showHelp == null) {
			_showHelp = new Boolean(d2wContext().valueForKey("parentConfigurationName") != null);
		}
		return _showHelp;
	}

}
