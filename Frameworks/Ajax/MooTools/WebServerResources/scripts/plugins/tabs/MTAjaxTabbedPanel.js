var MTAjaxTabbedPanel = new Class({

	Implements: [Options, Events],

	options : {
	
		busyDiv: 'busyDiv',
		selectedTabClassName: 'active',
		selectedPanelClassName: 'active',
		tabbedPanelTabsContainer: '',
		tabbedPanelPanesContainer: ''
		
	},

	initialize: function(options) {
		this.setOptions(options);
		if(this.options.elementID) {
			this.runOnLoad($(this.options.elementID));
		}
		this.initializeTabs();
	},
	
	initializeTabs : function() {

		var tabbedPanelTabsContainer = $(this.options.tabbedPanelTabsContainer);

		if(tabbedPanelTabsContainer == null) {
			alert("Unable to find tabbed panel with id: " + this.options.tabbedPanelContainer);
		}

		tabbedPanelTabsContainer.getElements('a').each(function(el) {

			if(el.get('rel') != null) {

				el.addEvent('click', function(e) {
					e.preventDefault();
					var element = e.target;
					var panel = $(element.get('rel'));
					this.loadPanel(panel);
					this.selectPanel(panel);
					this.selectTab(element);
				}.bind(this));
			}	
		}.bind(this)); 

	},
	
		
	selectTab : function(selectedTab) {

		var nodes = $(this.options.tabbedPanelTabsContainer).getElements("a");

		var dict = {
			node: selectedTab,
			selectedTabClassName: this.options.selectedTabClassName
		}

		nodes.each(function(node) {
			if(node != this.node) {
				node.getParent().removeClass(this.selectedTabClassName);
			}
		}, dict);

		selectedTab.getParent().addClass(this.options.selectedTabClassName);

		var pane = $(selectedTab.get('rel'));

		if(pane.get('html') == '' && pane.get('html') != this.busyContent(this.options.busyDiv)) {
			this.runOnSelect($(this.options.tabbedPanelTabsContainer));
		}

	}, 
	
	loadPanel : function(panel) {
	
		if(panel.get('html') == '' || panel.get('html') == this.busyContent(this.options.busyDiv)) {

		var req = new Request.HTML({
			delay: 0.25,
			initialDelay: 0.25,
			update : panel,
			url : panel.get('data-updateUrl'),
			async: true,
			evalScripts: true,
			onSuccess: function() {
				req.stopTimer();
				this.runOnLoad(panel);
				this.runOnSelect($(this.options.tabbedPanelPanesContainer));
			}.bind(this)

		}).startTimer();

		}
	
	},
  
	selectPanel : function(selectedPanelID) {

		var selectedPane = $(selectedPanelID);
		
		var formInPanel = selectedPane.getFirst('form');
		if(formInPanel) {
			MTASB.request(formInPanel, null, {async: false, evalScripts:false, _asbn:'dummy'});
		} else {
			var formAroundPanel = selectedPane.getParent();
			if(formAroundPanel.tagName == 'FORM') {
				MTASB.request(formAroundPanel, null, {async: false, evalScripts:false, _asbn:'dummy'});							
			}
		}
		
		var nodes = $(this.options.tabbedPanelPanesContainer).getChildren('li');
		var dict = {
			selectedPane: selectedPane,
			selectedPanelClassName: this.options.selectedPanelClassName
		};
		
		nodes.each(function(node) {

			if(node != this.selectedPane) {
				if(node.hasClass(this.selectedPanelClassName)) {
					new Request({ 
						url: node.get('data-updateUrl'), 
						async: true,
						evalScripts: false,
						didSelect: false
					}).send();				
				}
				node.removeClass(this.selectedPanelClassName);
			}

		}, dict);

		selectedPane.addClass(this.options.selectedPanelClassName);
		new Request({
			url: selectedPane.get('data-updateUrl'),
			async: true,
			evalScripts: false,
			didSelect: true,
		}).send();
	
	},
	
	runOnLoad : function(element) {
		var onLoadScript = element.get('onLoad');
		if(onLoadScript) {
			eval(onLoadScript);
		}	
	},
	
	runOnSelect : function(element) {
		var onSelectScript = element.get('onSelect');
		if(onSelectScript) {
			eval(onSelectScript);
		}
	},
	
	busyContent : function(busyDivID) {
		var busyContent = 'Loading, please wait...';
		if(busyDivID != '') {
			busyContent = $(busyDivID).get('html');
		}
		return busyContent;
	}

});