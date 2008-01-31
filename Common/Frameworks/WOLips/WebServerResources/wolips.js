var WOLips = {
	clickToOpenActive : false,
	ignoreClick : true,
	clickToOpenUrl : null,
	oldClickHandler : null,
	
	controlFrame : null,

	toggleToolBar : function() {
		$('_wolToolBar').toggle();
		if ($('_wolToolBar').visible()) {
			$('_wolHandle').innerHTML = '&lt;';
		}
		else {
			$('_wolHandle').innerHTML = '&gt;';
		}
	},
		
	startClickToOpen : function() {
		if (!WOLips.clickToOpenActive) {
			$('clickToOpen').innerHTML = '<span style = "color: rgb(150, 150, 150)">Select a Component:</span> <span id = "_componentBreadCrumb" style = "font-weight: bold">&nbsp;</span>';
			WOLips.oldClickHandler = document.onclick;
			document.onmousemove = WOLips.clickToOpenHover;
			document.onclick = WOLips.clickToOpen;
			WOLips.ignoreClick = true;
			WOLips.clickToOpenActive = true;
		}
		else {
			WOLips.stopClickToOpen();
		}
	},
	
	stopClickToOpen : function() {
		$('clickToOpen').innerHTML = 'Click to Open';
		document.onclick = WOLips.oldClickHandler;
		WOLips.oldClickHandler = null;
		WOLips.clickToOpenActive = false;
	},
	
	clickToOpenHover : function(e) {
		var target = e.target;
	  var componentNames = WOLips.componentNames(target);
		if (componentNames != null) {
			var componentBreadCrumb = [];
			componentNames.each(function(value, index) {
				var componentParts = value.split('.');
				componentBreadCrumb.push(componentParts[componentParts.length - 1]);
			});
			$('_componentBreadCrumb').innerHTML = componentBreadCrumb.join(' <span style = "color: rgb(200, 200, 200);">&gt;</span> ');
		}
	},
	
	clickToOpen : function(e) {
		if (WOLips.ignoreClick) {
			WOLips.ignoreClick = false;
			return true;
		}
	  var target = e.target;
	  
	  var componentNames = WOLips.componentNames(target);
	  if (componentNames == null) {
	  	alert('The component you selected could not be identifed.  Make sure er.component.clickToOpen=true.');
	  }
	  else {
		  if (WOLips.clickToOpenUrl == null) {
		  	alert('You do not have a clickToOpenURL set.');
		  }
		  else {
		  	var openComponentUrl = WOLips.clickToOpenUrl.replace('REPLACEME', componentNames[0]);
		  	WOLips.perform(openComponentUrl);
		  } 
		}
		e.stop();
		WOLips.stopClickToOpen();
	  return false;
	},
	
	componentNames : function(target) {
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
	},

	perform : function(url) {
		if (WOLips.controlFrame == null) {
	  	WOLips.controlFrame = document.createElement("iframe");
	  	WOLips.controlFrame.src = url;
	  	WOLips.controlFrame.style.display = 'none';
		  document.getElementsByTagName("body").item(0).appendChild(WOLips.controlFrame);
	  }
	  else {
	  	WOLips.controlFrame.src = url;
	  }
	  //setTimeout(function () { document.getElementsByTagName("body").item(0).removeChild(iframeTag); }, 1000);
	}
};
