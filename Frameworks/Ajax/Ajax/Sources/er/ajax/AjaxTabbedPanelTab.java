package er.ajax;

import com.webobjects.appserver.*;
import com.webobjects.foundation.*;



/**
 * This implements the tab contents of a tabbed panel display as an unordered
 * list (UL and LI elements). This is to be used inside of a AjaxTabbedPanel.
 * The contents can be anything.
 *
 * @see er.ajax.AjaxTabbedPanel for details on CSS and usage
 *
 * @binding name required, String the text shown in the tab that is clicked on
 *          to select the tab
 * @binding isSelected optional, boolean true if this tab is initially selected
 *          when the page is first rendered, defaults to false.  If present and
            can be set, is set to true when this tab is selected and to false
            when it is no longer the selected tab
 * @binding id optional, String the id attribute of the A element selecting
 *          this panel, [id]_panel is the id of LI element implementing this tab
 * @binding refreshOnSelect optional, if true the tab content will reload each
 *          time the tab is selected.  Defaults to false
 * @binding onLoad optional, String JavaScript to execute after the tab loads
 *
 * @author Chuck Hill
 */
public class AjaxTabbedPanelTab extends AjaxDynamicElement {

    private WOElement content;
    private WOAssociation name;
    private WOAssociation id;
    private WOAssociation isSelected;
    private WOAssociation refreshOnSelect;
    private WOAssociation onLoad;


    public AjaxTabbedPanelTab(String aName, NSDictionary associations, WOElement template) {
        super(aName, associations, template);

        content = template;
        name = (WOAssociation) associations.objectForKey("name");
        id = (WOAssociation) associations.objectForKey("id");
        isSelected = (WOAssociation) associations.objectForKey("isSelected");
        refreshOnSelect = (WOAssociation) associations.objectForKey("refreshOnSelect");
        onLoad = (WOAssociation) associations.objectForKey("onLoad");

        if (name == null)
        {
        	throw new RuntimeException("name binding is required");
        }
    }


    /**
     * Creates the panes.
     */
    public void appendToResponse(WOResponse aResponse, WOContext aContext)
    {
    	WOComponent component = aContext.component();
        aResponse.appendContentString("<li id=\"");
        aResponse.appendContentString((String)id().valueInComponent(component) + "_panel");
        aResponse.appendContentString("\" updateUrl=\"");
        aResponse.appendContentString(AjaxUtils.ajaxComponentActionUrl(aContext));
		aResponse.appendContentString("\" class=\"");
        aResponse.appendContentString(isSelected(component) ? "ajaxTabbedPanelPane-selected" : "ajaxTabbedPanelPane-unselected");
        aResponse.appendContentString("\"");
        if (onLoad != null) {
            appendTagAttributeToResponse(aResponse, "onLoad", onLoad.valueInComponent(component));
        }
        aResponse.appendContentString(">");

        // The selected pane needs to have its content rendered when the page is first rendered.  After that
        // it is controlled by the user clicking tabs
        if (isSelected(component) && content != null)
        {
        	content.appendToResponse(aResponse, aContext);
        }

        aResponse.appendContentString("</li>\n");

        // The selected pane needs to have its onLoad fired when the page is first rendered.  After that
        // it is fired by the user clicking tabs
        if (isSelected(component) && content != null)
        {
        	aResponse.appendContentString("<script>AjaxTabbedPanel.onLoad('");
        	aResponse.appendContentString((String)id().valueInComponent(component) + "_panel");
        	aResponse.appendContentString("');</script>\n");
        }
    }


    /**
     *
     * @param component the component this is being rendered in
     * @return <code>true</code> if this pane is the selected one
     */
    public boolean isSelected(WOComponent component)
    {
        return (isSelected != null) ? ((Boolean)isSelected.valueInComponent(component)).booleanValue() : false;
    }



    /**
     * If present and settable, sets the isSelected association to true for this tab when it selected and
     * to false when it is no longer the selected tab.
     *
     * @param component the component this is being rendered in
     */
    public void setIsSelected(WOComponent component, boolean isTabSelected)
    {
    	if (isSelected != null && isSelected.isValueSettableInComponent(component)) {
    		isSelected.setValue(new Boolean(isTabSelected), component);
    	}
    }



	protected void addRequiredWebResources(WOResponse response, WOContext context) {
	}


	/**
	 * The pane content is rendered when an Ajax request is received.
	 * @return the children rendered as HTML
	 */
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		WOResponse response = null;
		String didSelect = request.stringFormValueForKey("didSelect");

		// This is not set when the tab is initially loaded, that is our cue to generate our content
		if ( didSelect == null)
		{
			response = AjaxUtils.createResponse(request, context);
			AjaxUtils.setPageReplacementCacheKey(context, _containerID(context));

			if (content != null)
			{
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
    public void setId(WOAssociation newID)
    {
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
    public Boolean refreshesOnSelect(WOComponent component)
    {
       return (refreshOnSelect != null) ? (Boolean)refreshOnSelect.valueInComponent(component): Boolean.FALSE;
    }

}
