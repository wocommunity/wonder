package er.ajax.mootools;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.foundation.NSDictionary;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxUtils;

public class MTAjaxTabbedPanelTab extends AjaxDynamicElement {

	private WOElement content;
	private WOAssociation name;
	private WOAssociation id;
	private WOAssociation isSelected;
	private WOAssociation refreshOnSelect;
	private WOAssociation onLoad;
	private WOAssociation isVisible;
	private WOAssociation accesskey;

	public MTAjaxTabbedPanelTab(String aName, NSDictionary<String, WOAssociation> associations, WOElement template) {
		super(aName, associations, template);

		content = template;
		name = associations.objectForKey("name");
		id = associations.objectForKey("id");
		isSelected = associations.objectForKey("isSelected");
		refreshOnSelect = associations.objectForKey("refreshOnSelect");
		onLoad = associations.objectForKey("onLoad");
		isVisible = associations.objectForKey("isVisible");
		accesskey = associations.objectForKey("accesskey");

		if (name == null) {
			throw new RuntimeException("name binding is required");
		}

	}

    /**
     * Creates the panes.
     */
    @Override
    public void appendToResponse(WOResponse response, WOContext aContext)
    {
    	WOComponent component = aContext.component();
    	
    	if (isVisble(component)) {
            response.appendContentString("<li id=\"");
            response.appendContentString((String)id().valueInComponent(component) + "_panel");
            response.appendContentString("\" data-updateUrl=\"");
            response.appendContentString(AjaxUtils.ajaxComponentActionUrl(aContext));
    		response.appendContentString("\"");
    		if(isSelected(component)) {
    			appendTagAttributeToResponse(response, "class", "active");
    		}
            if (onLoad != null) {
                appendTagAttributeToResponse(response, "onLoad", onLoad.valueInComponent(component));
            }
            response.appendContentString(">");

            // The selected pane needs to have its content rendered when the page is first rendered.  After that
            // it is controlled by the user clicking tabs
            if (isSelected(component) && content != null) {
            	content.appendToResponse(response, aContext);
            }

            response.appendContentString("</li>\n");

    	}
    
    }
	
	
	/** 
	 * Do nothing if not visible. 
	 */
	@Override
	public void takeValuesFromRequest(WORequest request, WOContext context)
	{
		if (isVisble(context.component()) && (isSelected ==  null || isSelected(context.component())) ) {
			super.takeChildrenValuesFromRequest(request, context);
		}
	}

	/** 
	 * Do nothing if not visible. 
	 */
	@Override
	public WOActionResults invokeAction(WORequest request, WOContext context)
	{
		if (isVisble(context.component())) {
			return super.invokeAction(request, context);
		}
		return null;
	}

	/**
	 *
	 * @param component the component this is being rendered in
	 * @return <code>true</code> if this pane is the selected one
	 */
	public boolean isSelected(WOComponent component) {
		return (isSelected != null) ? isSelected.booleanValueInComponent(component) : false;
	}

	/**
	 * If present and settable, sets the isSelected association to true for this tab when it selected and
	 * to false when it is no longer the selected tab.
	 *
	 * @param component the component this is being rendered in
	 * @param isTabSelected true is this is being rendered as the selected tab
	 */
	public void setIsSelected(WOComponent component, boolean isTabSelected) {
		if (isSelected != null && isSelected.isValueSettableInComponent(component)) {
			isSelected.setValue(Boolean.valueOf(isTabSelected), component);
		}
	}

	@Override
	protected void addRequiredWebResources(WOResponse response, WOContext context) {
	}

	/**
	 * The pane content is rendered when an Ajax request is received.
	 * @return the children rendered as HTML
	 */
	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOResponse response = null;
		String didSelect = request.stringFormValueForKey("didSelect");

		// This is not set when the tab is initially loaded, that is our cue to generate our content
		if ( didSelect == null) {
			response = AjaxUtils.createResponse(request, context);
			AjaxUtils.setPageReplacementCacheKey(context, _containerID(context));

			if (content != null) {
				content.appendToResponse(response, context);
			}
		}
		else setIsSelected(context.component(), didSelect.equals("true"));

		return response;
	}

	/**
	 * @param context WOContext response is being returned in
	 * @return ID to cache this Ajax response under
	 */
	@Override
	protected String _containerID(WOContext context) {
		return (String)id().valueInComponent(context.component()) + "_panel";
	}


	/**
	 * @return association for HTML id attribute
	 */
	public WOAssociation id() {
		return id;
	}

	/**
	 * Sets the id association so that the AjaxTabbedPanel can provide an id if needed.
	 *
	 * @param newID the association to use to get the HTML id attribute
	 */
	public void setId(WOAssociation newID) {
		id = newID;
	}

	/**
	 * @return association for HTML id attribute.
	 */
	public WOAssociation name() {
		return name;
	}

	/**
	 * Returns current component's value for the refreshOnSelect binding.
	 *
	 * @param component the component this is being rendered in
	 * @return Boolean value for refreshOnSelect binding, Boolean.FALSE if unset
	 */
	public Boolean refreshesOnSelect(WOComponent component) {
		return (refreshOnSelect != null) ? (Boolean)refreshOnSelect.valueInComponent(component): Boolean.FALSE;
	}

	/**
	 * Returns current component's value for the isVisible binding.
	 *
	 * @param component the component this is being rendered in
	 * @return Boolean value for isVisible binding, Boolean.TRUE if unset
	 */
	public boolean isVisble(WOComponent component) {
		return (isVisible != null) ? ((Boolean)isVisible.valueInComponent(component)).booleanValue() : true;
	}

	/**
	 * @return WOAssociation for the accesskey binding
	 */
	public WOAssociation accesskey() {
		return accesskey;
	}
}
