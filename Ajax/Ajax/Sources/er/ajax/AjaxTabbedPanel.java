package er.ajax;

import com.webobjects.appserver.*;
import com.webobjects.appserver._private.*;
import com.webobjects.foundation.*;



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
 * 
 * @author Chuck Hill
 */
public class AjaxTabbedPanel extends AjaxDynamicElement {
    
    private WOElement content;
    private NSMutableArray tabs = new NSMutableArray();
    private WOAssociation id;
    private WOAssociation busyDiv;
    
    
    public AjaxTabbedPanel(String name, NSDictionary associations, WOElement template) {
        super(name, associations, template);
        content = template;
        id = (WOAssociation) associations.objectForKey("id");
        busyDiv = (WOAssociation) associations.objectForKey("busyDiv");
        findTabs((WODynamicGroup)template);
        
        if (id == null)
        {
        	throw new RuntimeException("id binding is required");
        }
    }

    
    /**
     * Looks through the child components to local the AjaxTabbedPanelTabs that are controlled by this panel.
     * Tabs without an explicit id attributed are assigned a caculated one.
     * 
     * @param template the graph of elements passed to the constructor.
     */
    private void findTabs(WODynamicGroup template)  {
    	if (template == null) return;
    	
        NSArray children = template.childrenElements();
        for (int i = 0; i < children.count(); i++) {
            WOElement child = (WOElement)children.objectAtIndex(i);
            if (child instanceof AjaxTabbedPanelTab) {
            	AjaxTabbedPanelTab childTab = (AjaxTabbedPanelTab)child;
            	
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

    
    /**
     * Creates the tabs and pane control.
     */
    public void appendToResponse(WOResponse response, WOContext context) {
        WOComponent component = context.component();
        String idString = (String) id.valueInComponent(component);
        if (idString == null)
        {
        	throw new RuntimeException("id binding evaluated to null");
        }

        // UL for tabs
        response.appendContentString("<ul class=\"ajaxTabbedPanel\"");
        appendTagAttributeToResponse(response, "id", idString);
        response.appendContentString(">\n");

        String paneControlID = idString + "_panecontrol";
        
        for (int i = 0; i < tabs.count(); i++) {
            String index = new Integer(i).toString();
            String tabID = idString + "_tab_" + index;
            AjaxTabbedPanelTab tab = (AjaxTabbedPanelTab)tabs.objectAtIndex(i);
            boolean isSelectedTab = tab.isSelected(context.component());
            String panelTabID = (String) tab.id().valueInComponent(component);
            String panelID = panelTabID + "_panel";
            response.appendContentString("  <li class=\"ajaxTabbedPanelTab-");
            response.appendContentString(isSelectedTab ? "selected" : "unselected");
            response.appendContentString("\" ");
            appendTagAttributeToResponse(response, "id", tabID);
            response.appendContentString(">\n");
            response.appendContentString("<a ");
            appendTagAttributeToResponse(response, "id", panelTabID);
            response.appendContentString(" href=\"javascript:void(0)\" onclick=\"");
            
            if ( ! isSelectedTab) {
                response.appendContentString("AjaxTabbedPanel.loadPanel('");
                response.appendContentString(panelID);
                response.appendContentString("', '");
                response.appendContentString((busyDiv != null) ? (String)busyDiv.valueInComponent(component) : "");
                response.appendContentString("'); ");
            }
            
            response.appendContentString("AjaxTabbedPanel.selectTab('");
            response.appendContentString(idString);
            response.appendContentString("', '");
            response.appendContentString(tabID);
            response.appendContentString("'); AjaxTabbedPanel.selectPanel('");
            response.appendContentString(paneControlID);
            response.appendContentString("', '");
            response.appendContentString(panelID);
            response.appendContentString("');\">");
            response.appendContentString((String) tab.name().valueInComponent(component));
            response.appendContentString("</a>\n");
            response.appendContentString("</li>\n");
        }
        
        response.appendContentString("</ul>\n");

        // UL for panes
        response.appendContentString("<ul class=\"ajaxTabbedPanelPanes\" ");
        appendTagAttributeToResponse(response, "id", paneControlID);
        response.appendContentString(">\n");
        // The tabs render themself as pane
        if (content !=  null)
        {
        	content.appendToResponse(response, context);
        }
        response.appendContentString("</ul>\n");
        super.appendToResponse(response, context);
    }


	protected void addRequiredWebResources(WOResponse response, WOContext context) {
		AjaxUtils.addScriptResourceInHead(context, response, "prototype.js");
		AjaxUtils.addScriptResourceInHead(context, response, "switchtabs.js");
	}


	public WOActionResults handleRequest(WORequest request, WOContext context) {
		return null;
	}
}
