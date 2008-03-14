// Functions for AjaxTabbedPanel
var AjaxTabbedPanel = {

    // Change which tab appears in selected state
    selectTab : function(tabControlID, selectedTabID) {
      var selectedTab = $(selectedTabID);
      var tablist = this.getChildrenByTagName($(tabControlID), 'li');
      var nodes = $A(tablist);

      nodes.each(function(node){
          if (node.id != selectedTab.id) {
              node.removeClassName('ajaxTabbedPanelTab-selected').addClassName('ajaxTabbedPanelTab-unselected');
          }
      });
      selectedTab.removeClassName('ajaxTabbedPanelTab-unselected').addClassName('ajaxTabbedPanelTab-selected');
    },

    // Change which panel appears
    selectPanel : function(paneControlID, selectedPaneID) {
      var selectedPane = $(selectedPaneID);
      var panelist = this.getChildrenByTagName($(paneControlID), 'li');
      var nodes = $A(panelist);

	   // Find the currently seleted tab, de-select it and notify the application  
      nodes.each(function(node){
        if (node.id != selectedPane.id) {
          if (node.hasClassName('ajaxTabbedPanelPane-selected')) {
            new Ajax.Request(node.getAttribute('updateUrl') + "?didSelect=false",  {asynchronous:1, evalScripts:false})
            node.removeClassName('ajaxTabbedPanelPane-selected').addClassName('ajaxTabbedPanelPane-unselected');
          }
        };
      });

      // Select the new tab and ntify the app of the selected tab
      selectedPane.removeClassName('ajaxTabbedPanelPane-unselected').addClassName('ajaxTabbedPanelPane-selected');
       new Ajax.Request(selectedPane.getAttribute('updateUrl') + "?didSelect=true",  {asynchronous:1, evalScripts:false})
    },

    // Loads the panel contents if not already loaded
    loadPanel : function(paneID, busyDivID) {

      // Determine what to show if the panel takes a while to  load
      var busyContent = 'Loading, please wait...';
      if (busyDivID != '') {
          busyContent = $('busydiv').innerHTML;
      }

      var pane = $(paneID);
      if (pane.innerHTML=='' || pane.innerHTML==busyContent) {
         new PeriodicalExecuter(function(pe) { if (pane.innerHTML=='' || pane.innerHTML==busyContent) {pane.innerHTML=busyContent}; pe.stop()}, 0.5);
         new Ajax.Updater(pane, pane.getAttribute('updateUrl'), {asynchronous:1, evalScripts:true});
      }
    },


    // Returns an element's children that have a specific tag name as an array
    getChildrenByTagName : function(element, tag_name) {
        child_array      = new Array();
        tag_name         = tag_name.toLowerCase();
        element_children = element.childNodes;

        for (i = 0; i < element_children.length; i++) {
            if (element_children[i].tagName != undefined) {
                if (element_children[i].tagName.toLowerCase() == tag_name) {
                    child_array.push(element_children[i]);
                }
            }
        }

        return child_array;
    }
}
