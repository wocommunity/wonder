var WOLipsClickToOpen = {
	url : null,
	
	active : false,
	ignoreClick : true,
	
	oldClickHandler : null,
	oldMoveHandler : null,
	
	start : function() {
		if (!WOLipsClickToOpen.active) {
			WOLipsClickToOpen.hideComponentList();
			$('clickToOpen').innerHTML = '<span style = "color: rgb(150, 150, 150)">Select a Component:</span> <span id = "_componentBreadCrumb" style = "font-weight: bold">&nbsp;</span>';
			WOLipsClickToOpen.oldClickHandler = document.onclick;
			WOLipsClickToOpen.oldMoveHandler = document.onmousemove;
			document.onmousemove = WOLipsClickToOpen.mouseMoved;
			document.onclick = WOLipsClickToOpen.mouseClicked;
			WOLipsClickToOpen.ignoreClick = true;
			WOLipsClickToOpen.active = true;
		}
		else {
			WOLipsClickToOpen.stop();
		}
	},
	
	stop : function() {
		$('clickToOpen').innerHTML = 'Click to Open';
		document.onclick = WOLipsClickToOpen.oldClickHandler;
		document.onmousemove = WOLipsClickToOpen.oldMoveHandler;
		WOLipsClickToOpen.oldClickHandler = null;
		WOLipsClickToOpen.oldMoveHandler = null;
		WOLipsClickToOpen.active = false;
	},
	
	mouseMoved : function(e) {
		var target = e.target;
	  var componentNames = WOLipsClickToOpen.componentNamesForElement(target);
		if (componentNames != null) {
			var componentBreadCrumb = [];
			componentNames.each(function(value, index) {
				var componentParts = value.split('.');
				componentBreadCrumb.push(componentParts[componentParts.length - 1]);
			});
			var componentBreadCrumbElement = $('_componentBreadCrumb');
			if (componentBreadCrumbElement != null) {
				componentBreadCrumbElement.innerHTML = componentBreadCrumb.join(' <span style = "color: rgb(200, 200, 200);">&gt;</span> ');
			}
		}
	},
	
	mouseClicked : function(e) {
		if (WOLipsClickToOpen.ignoreClick) {
			WOLipsClickToOpen.ignoreClick = false;
			return true;
		}
	  var target = e.target;
	  
	  var componentNames = WOLipsClickToOpen.componentNamesForElement(target);
	  if (componentNames == null || componentNames.length == 0) {
	  	alert('The component you selected could not be identifed.  Make sure er.component.clickToOpen=true.');
	  }
	  else if (WOLipsClickToOpen.url == null) {
		  alert('You do not have a click-to-open url set.');
		}
		else if (componentNames.length == 1) {
			WOLipsClickToOpen.openComponentNamed(componentNames[0]);
		}
		else {
			if (e.isMiddleClick && e.isMiddleClick()) {
				WOLipsClickToOpen.showComponentList(componentNames, e.x + document.viewport.getScrollOffsets().left, e.y + document.viewport.getScrollOffsets().top);
			}
			else {
				WOLipsClickToOpen.openComponentNamed(componentNames[0]);
			}
		}
		e.stop && e.stop();
		WOLipsClickToOpen.stop();
	  return false;
	},
	
	openComponentNamed : function(selectedComponentName) {
		WOLips.perform(WOLipsClickToOpen.url.replace('REPLACEME', selectedComponentName));
	},
	
	hideComponentList : function() {
		var componentList = $('_clickToOpenComponentList');
		if (componentList != null) {
			$('_clickToOpenComponentList').remove();
		}
	},
	
	showComponentList : function(componentNames, x, y) {
		componentNames.push(null);
		
  	var componentNamesContainer = document.createElement("div");
  	componentNamesContainer.id = '_clickToOpenComponentList';

  	var componentNamesTitle = document.createElement("h1");
  	componentNamesTitle.innerHTML = 'Select a Component';
		componentNamesContainer.appendChild(componentNamesTitle);
  	  	
  	
  	var componentNamesList = document.createElement("ul");
  	componentNames.each(function(componentName, index) {
  		var componentNameItem = document.createElement("li");
  		componentNameItem._componentName = componentName;
  		if (componentName == null) {
  			componentNameItem.innerHTML = 'Cancel';
  		}
  		else {
  			componentNameItem.innerHTML = componentName.split('.').last();
  		}
  		componentNameItem.onclick = function() {
  			if (componentNameItem._componentName != null) {
  				WOLipsClickToOpen.openComponentNamed(componentNameItem._componentName);
  			}
  			WOLipsClickToOpen.hideComponentList();
  		};
  		componentNamesList.appendChild(componentNameItem);
  	});
		componentNamesContainer.style.left = x + 'px';
		componentNamesContainer.style.top = y + 'px';
		componentNamesContainer.appendChild(componentNamesList);
  	document.getElementsByTagName("body").item(0).appendChild(componentNamesContainer);
	},
	
	componentNamesForElement : function(target) {
		var componentNamesStr = null;
	  while (target != null) {
	  	if (target.getAttribute) {
	    	var componentName = target.getAttribute('_componentName');
	    	if (componentName != null) {
	    		if (componentNamesStr == null) {
		    		componentNamesStr = componentName;
		    	}
		    	else {
		    		componentNamesStr += "," + componentName; 
		    	}
		    }
	    	target = target.up();
	    }
	    else {
	    	target = null;
	    }
	  }
		var componentNames;
		if (componentNamesStr == null) {
			componentNames = null;
		} else {
			componentNames = componentNamesStr.split(',');
		}
		return componentNames;
	}
}

var WOLipsToolBar = {
	initialize : function(e) {
		$('_wolToolBarContainer').onselectstart = function() { return false };
		WOLipsToolBar.update();
	},
	
	open : function(e) {
		$('_wolToolBar').show();
		WOLipsToolBar.update();
	},
	
	hide : function(e) {
		$('_wolToolBar').hide();
		WOLipsToolBar.update();
	},
	
	toggle : function(e) {
		$('_wolToolBar').toggle();
		WOLipsToolBar.update();
		e.stop && e.stop();
	},
	
	update : function() {
		if ($('_wolToolBar').visible()) {
			$('_wolHandle').innerHTML = '&lt;';
			$('_wolHandle').onclick = WOLipsToolBar.toggle;
			$('_wolToolBarContainer').onclick = null;
			$('_wolToolBarContainer').style.cursor = 'inherit';
		}
		else {
			$('_wolToolBarContainer').style.cursor = 'pointer';
			$('_wolToolBarContainer').onclick = WOLipsToolBar.toggle;
			$('_wolHandle').innerHTML = '&gt;';
			$('_wolHandle').onclick = null;
		}
	}
};

var WOLips = {
	controlFrame : null,

	perform : function(url) {
		if (WOLips.controlFrame == null) {
	  	WOLips.controlFrame = document.createElement("iframe");
	  	WOLips.controlFrame.style.display = 'none';
		  document.getElementsByTagName("body").item(0).appendChild(WOLips.controlFrame);
	  }
	  WOLips.controlFrame.src = url;
	  //setTimeout(function () { document.getElementsByTagName("body").item(0).removeChild(iframeTag); }, 1000);
	}
};
