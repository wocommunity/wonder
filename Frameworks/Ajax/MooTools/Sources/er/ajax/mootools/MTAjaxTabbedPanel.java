package er.ajax.mootools;

import com.webobjects.appserver.WOActionResults;
import com.webobjects.appserver.WOAssociation;
import com.webobjects.appserver.WOComponent;
import com.webobjects.appserver.WOContext;
import com.webobjects.appserver.WOElement;
import com.webobjects.appserver.WORequest;
import com.webobjects.appserver.WOResponse;
import com.webobjects.appserver._private.WOConstantValueAssociation;
import com.webobjects.appserver._private.WODynamicGroup;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSMutableArray;

import er.ajax.AjaxDynamicElement;
import er.ajax.AjaxUtils;
import er.extensions.appserver.ERXWOContext;

/**
 * This implements the tabs and the main control of a tabbed panel display as an
 * unordered list (UL and LI elements). The tab contents go in AjaxTabbedPanel
 * components contained within this component. The tab contents are loaded on
 * demand so only the selected tab is rendered when the page is displayed. If
 * tabs can take a while to load, a hidden div containing a "working" message
 * can temporarily replace the panel content while it loads.
 *
 * <h3>CSS Classes Used by AjaxTabbedPanel and AjaxTabbedPanelTab</h3>
 * <table>
 * <tr>
 * <th>Class Name</th>
 * <th>Used For</th>
 * </tr>
 * <tr>
 * <td>ajaxTabbedPanel</td>
 * <td>The UL containing the tabs.</td>
 * </tr>
 * <tr>
 * <td>ajaxTabbedPanelTab-selected</td>
 * <td>The LI representing the selected tab and the A element that is the clickable title.</td>
 * </tr>
 * <tr>
 * <td>ajaxTabbedPanelTab-unselected</td>
 * <td>The LI representing the selected tab(s) and the A element that is the clickable title.</td>
 * </tr>
 * <tr>
 * <td>ajaxTabbedPanelPanes</td>
 * <td>The UL containing the panels (panes).</td>
 * </tr>
 * <tr>
 * <td>ajaxTabbedPanelPane-selected</td>
 * <td>The LI representing the selected panel.</td>
 * </tr>
 * <tr>
 * <td>ajaxTabbedPanelPane-unselected</td>
 * <td>The LI representing the unselected panel(s).</td>
 * </tr>
 * </table
 *
 * @binding id required, String the id of the UL that wraps the tabs
 * @binding busyDiv optional, String the id of a div that should be shown when a
 *          tab is loading
 * @binding onLoad optional, String JavaScript to execute after the whole tabbed panel loads
 * @binding onSelect optional, String JavaScript to execute after a different tab is selected.
 * 			This will <b>not</b> get called when this is first rendered.  Use onLoad if you need that.
 * @binding class css classes to add to the ul.
 * @author Chuck Hill
 */

public class MTAjaxTabbedPanel extends AjaxDynamicElement {

    private WOElement content;
    private NSMutableArray<MTAjaxTabbedPanelTab> tabs = new NSMutableArray<MTAjaxTabbedPanelTab>();
    private WOAssociation id;
    private WOAssociation busyDiv;
    private WOAssociation onLoad;
    private WOAssociation onSelect;
	
	public MTAjaxTabbedPanel(String name, NSDictionary<String, WOAssociation> associations, WOElement template) {
        super(name, associations, template);
        content = template;
        id = associations.objectForKey("id");
        busyDiv = associations.objectForKey("busyDiv");
        onLoad = associations.objectForKey("onLoad");
        onSelect = associations.objectForKey("onSelect");
        findTabs((WODynamicGroup)template);

        if (id == null)
        {
        	throw new RuntimeException("id binding is required");
        }
    }

    /**
     * Looks through the child components to locate the AjaxTabbedPanelTabs that are controlled by this panel.
     * Tabs without an explicit id attributed are assigned a calculated one.
     *
     * @param template the graph of elements passed to the constructor.
     */
    private void findTabs(WODynamicGroup template)  {
    	if (template == null || template.childrenElements() == null) return;

        NSArray<WOElement> children = template.childrenElements();
        for (int i = 0; i < children.count(); i++) {
            WOElement child = children.objectAtIndex(i);
            if (child instanceof MTAjaxTabbedPanelTab) {
            	MTAjaxTabbedPanelTab childTab = (MTAjaxTabbedPanelTab)child;

            	// The tabs need to have an id attribute so we assign one if needed
                if (childTab.id() == null) {
                    childTab.setId(new WOConstantValueAssociation(id.valueInComponent(null) + "_pane_" + tabs.count()));
                }

                tabs.addObject(childTab);
            }
            else if (child instanceof WODynamicGroup) {
                findTabs((WODynamicGroup)child);
            }
        }
    }
	
    @Override
    public void appendToResponse(WOResponse response, WOContext context) {
    	
        WOComponent component = context.component();
        String idString = (String) id.valueInComponent(component);
        if (idString == null) {
        	throw new RuntimeException("id binding evaluated to null");
        }

        // UL for tabs
        response.appendContentString("<ul");
        appendTagAttributeToResponse(response, "class", valueForBinding("class", component));
        appendTagAttributeToResponse(response, "id", idString);
        
        // Optional JavaScriplets
        if (onLoad != null) {
            appendTagAttributeToResponse(response, "onLoad", onLoad.valueInComponent(component));
        }
        if (onSelect != null) {
            appendTagAttributeToResponse(response, "onSelect", onSelect.valueInComponent(component));
        }
        
        response.appendContentString(">\n");

        String paneControlID = idString + "_panecontrol";
        String selectedTabClassName = stringValueForBinding("selectedPanelClassName", component);
        if(selectedTabClassName == null) {
        	selectedTabClassName = "active";
        }
        for(MTAjaxTabbedPanelTab tab : tabs) {
        	
            if (tab.isVisble(component)) {
	            boolean isSelectedTab = tab.isSelected(context.component());
	            String panelTabID = (String) tab.id().valueInComponent(component);
	            String panelID = panelTabID + "_panel";
	            response.appendContentString("  <li");
	            if(isSelectedTab) {
	            	response.appendContentString(" class=\"");
	            	response.appendContentString(selectedTabClassName);
	            	response.appendContentString("\"");
	            }
	            response.appendContentString(">\n");
	            response.appendContentString("<a ");
	            
	            //add the accesskey
	            if( tab.accesskey() != null ){
	            	String accessKeyStr = tab.accesskey().valueInComponent(component).toString();
	            	appendTagAttributeToResponse(response, "accesskey", accessKeyStr );
	            }
	            
	            appendTagAttributeToResponse(response, "href", "javascript:void(0)");
	            appendTagAttributeToResponse(response, "rel", panelID);	
	            response.appendContentString("\">");
	            response.appendContentString((String) tab.name().valueInComponent(component));
	            response.appendContentString("</a>\n");
	            response.appendContentString("</li>\n");
            }
        }

        response.appendContentString("</ul>\n");

        // UL for panes
        response.appendContentString("<ul class=\"ajaxTabbedPanelPanes\" ");
        appendTagAttributeToResponse(response, "id", paneControlID);
        response.appendContentString(">\n");
        // The tabs render themselves as panes
        if (content !=  null) {
        	content.appendToResponse(response, context);
        }
        response.appendContentString("</ul>\n");
        super.appendToResponse(response, context);

        AjaxUtils.appendScriptHeader(response);
        response.appendContentString("window.addEvent('domready', function() {");
        response.appendContentString("\n\tvar ");
        response.appendContentString(safeID(context));
        response.appendContentString(" = new MTAjaxTabbedPanel({");
        response.appendContentString("tabbedPanelTabsContainer : ");
        response.appendContentString("'");
        response.appendContentString(idString);
        response.appendContentString("', ");
        response.appendContentString("tabbedPanelPanesContainer : ");
        response.appendContentString("'");
        response.appendContentString(paneControlID);
        response.appendContentString("'");
        if(! selectedTabClassName.equals("active")) {
            response.appendContentString(", ");
            response.appendContentString("selectedTabClassName : ");
            response.appendContentString("'");
            response.appendContentString(selectedTabClassName);
            response.appendContentString("'");
        }
        
        response.appendContentString("});");
        response.appendContentString("\n});");
        AjaxUtils.appendScriptFooter(response);
        
    }
    
    public String safeID(WOContext context) {
    	return "mtTabbedPanel" + ERXWOContext.safeIdentifierName(context, true);
    }
    
	@Override
	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_CORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_MORE_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", MTAjaxUtils.MOOTOOLS_WONDER_JS);
		MTAjaxUtils.addScriptResourceInHead(context, context.response(), "MooTools", "scripts/plugins/tabs/MTAjaxTabbedPanel.js");
	}

	@Override
	public WOActionResults handleRequest(WORequest request, WOContext context) {
		// TODO Auto-generated method stub
		return null;
	}


}