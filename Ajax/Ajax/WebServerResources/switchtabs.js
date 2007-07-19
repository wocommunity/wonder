// Functions for AjaxTabbedPanel
var AjaxTabbedPanel = {

    // Change which tab appears in selected state
    selectTab : function(tabControlID, selectedTabID) {
      var selectedTab = $(selectedTabID);
      var tablist = this.getChildrenByTagName($(tabControlID), 'li');
      var nodes = $A(tablist);

      selectedTab.className = 'ajaxTabbedPanelTab-selected';
      nodes.each(function(node){
        if (node.id != selectedTab.id) {
          node.className = 'ajaxTabbedPanelTab-unselected';
        };
      });
    },

    // Change which panel appears
    selectPanel : function(paneControlID, selectedPaneID) {
      var selectedPane = $(selectedPaneID);
      var panelist = this.getChildrenByTagName($(paneControlID), 'li');
      var nodes = $A(panelist);

      selectedPane.className='ajaxTabbedPanelPane-selected';
      nodes.each(function(node){
        if (node.id != selectedPane.id) {
          node.className='ajaxTabbedPanelPane-unselected';
        };
      });
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
         new Ajax.Updater(pane, pane.getAttribute('updateUrl'), {asynchronous:1, evalScripts:true})
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
