// Functions for AjaxTabbedPanel

// Changes which tab appears in the selected state
function selectTab(tabControlID, selectedTabID) {
  var selectedTab = $(selectedTabID);
  var tablist = $(tabControlID).getElementsByTagName('li');
  var nodes = $A(tablist);

  selectedTab.className = 'ajaxTabbedPanelTab-selected';
  nodes.each(function(node){
    if (node.id != selectedTab.id) {
      node.className = 'ajaxTabbedPanelTab-unselected';
    };
  });
}


// Changes which panel appears
function selectPanel(paneControlID, selectedPaneID) {
  var selectedPane = $(selectedPaneID);
  var panelist = $(paneControlID).getElementsByTagName('li');
  var nodes = $A(panelist);
  
  selectedPane.className='ajaxTabbedPanelPane-selected';
  nodes.each(function(node){
    if (node.id != selectedPane.id) {
      node.className='ajaxTabbedPanelPane-unselected';
    };
  });
}


// Loads the panel contents if not already loaded
function loadPanel(paneID, busyDivID) {

  // Determine what to show if the panel takes a while to  load
  var busyContent = 'Loading, please wait...';
  if (busyDivID != '') {
  	busyContent = $('busydiv').innerHTML;
  }
  
  var pane = $(paneID);
  if (pane.innerHTML=='' || pane.innerHTML==busyContent) {
     new Ajax.Updater(pane, pane.getAttribute('updateUrl'), {asynchronous:1, evalScripts:true, onLoading:function(request){pane.innerHTML=busyContent;}})
  }
}

