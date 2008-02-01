AjaxSelectionList = Class.create();
AjaxSelectionList.prototype = {
	list: null,
	container: null,
	selection: null,
	selectionIndex: null,
	fireOnSelection: false,

	onchange: null,
	onselect: null,
	ondelete: null,

	itemType: null,
	lastClick: 0,

	initialize: function(id) {
		this.container = $(id);
		this.list = this.container.down();
		if (this.list.nodeName == 'UL' || this.list.nodeName == 'OL') {
			this.itemType = 'LI';
		}
		else if (this.list.nodeName == 'TABLE') {
			this.itemType = 'TR';
		}
		else {
			var child = this.list.down();
			if (child != null) {
				this.itemType = child.nodeName;
			}
			else {
				alert('Unknown list node type "' + this.list.nodeName + '".');
			}
		}

		Event.observe(this.container, "selectstart", function() { return false; });
		Event.observe(this.container, "focus", this.containerFocused.bindAsEventListener(this));
		Event.observe(this.container, "blur", this.containerBlurred.bindAsEventListener(this));
		Event.observe(this.container, "keypress", this.containerKeyPressed.bindAsEventListener(this));
		Event.observe(this.container, "mousedown", function(e) { Event.stop(e); });
		this.observeItems();
	},

	observeItems: function() {
		var items = [];
		var firstItem = this.list.down(this.itemType);
		if (firstItem != null) {
			items = firstItem.up().immediateDescendants();
		}
		items.each(function(item, index) {
			Event.observe(item, "mousedown", this.itemClicked.bindAsEventListener(this));
		}.bind(this));
	},

	focus: function() {
		this.container.focus();
	},

	setSelectionIndex: function(index, fireChangeEvent) {
		if (index == -1) {
			this.setSelection(null);
		}
		else {
			this.setSelection(this.list.immediateDescendants()[index], fireChangeEvent);
		}
	},

	setSelection: function(item, fireChangeEvent) {
		if (this.selection == item) {
			return;
		}

		if (this.selection != null) {
			this.selection.removeClassName('selected');
			this.selection = null;
			this.selectionIndex = -1;
		}

		this.selection = item;

		if (this.selection != null) {
			this.selection.addClassName('selected');
			this.selectionIndex = this.list.immediateDescendants().indexOf(this.selection);

			var selectionOffset = Position.cumulativeOffset(this.selection);
			var selectionHeight = this.selection.getHeight();

			var containerContainer = this.container.up();
			var containerOffset = Position.cumulativeOffset(containerContainer);
			var scrollBarHeightEstimate = 15;
			var containerHeight = containerContainer.getHeight() - scrollBarHeightEstimate;
			var containerScrollTop = containerContainer.scrollTop;

			if (selectionOffset[1] + selectionHeight >= containerOffset[1] + containerHeight + containerScrollTop) {
				containerContainer.scrollTop = (selectionOffset[1] - Position.cumulativeOffset(this.container)[1]) + selectionHeight - containerHeight; 
			}
			else if (selectionOffset[1] <= containerOffset[1] + containerScrollTop) {
				containerContainer.scrollTop = selectionOffset[1] - Position.cumulativeOffset(this.container)[1]; 
			}
		}

		if (typeof fireChangeEvent == 'undefined' || fireChangeEvent) {
			this.fireChangeAction();
		}
	},

	itemClicked: function(e) {
		var item = e.target;
		var lastSelection = this.selection;
		if (item == null) {
		}
		else if (item.nodeName != this.itemType) {
			item = item.up(this.itemType);
		}
		this.focus();
		this.setSelection(item, true);

		var thisClick = new Date().getTime();
		if (lastSelection == this.selection && thisClick - this.lastClick < 300) {
			this.itemDoubleClicked(e);
		}
		this.lastClick = thisClick;
	},

	itemDoubleClicked: function(e) {
		if (!this.fireOnSelection) {
			this.fireSelectAction();
		}
	},

	containerBlurred: function(e) {
		this.list.removeClassName('focused');
	},

	containerFocused: function(e) {
		this.list.addClassName('focused');
	},

	selectNext: function() {
		if (this.selection == null) {
			this.setSelection(this.list.down(), true);
		}
		else {
			var next = this.selection.next();
			if (next == null) {
				// stop at the bottom
			}
			else {
				this.setSelection(next, true);
			}
		}
	},

	selectPrevious: function() {
		if (this.selection == null) {
			this.setSelection(this.list.down(), true);
		}
		else {
			var previous = this.selection.previous();
			if (previous == null) {
				// stop at the top
			}
			else {
				this.setSelection(previous, true);
			}
		}
	},

	fireChangeAction: function() {
		if (this.onchange) {
			this.onchange(this);
		}

		if (this.fireOnSelection) {
			this.fireSelectAction(this);
		}
	},

	fireSelectAction: function() {
		if (this.onselect) {
			this.onselect(this);
		}
	},

	fireDeleteAction: function() {
		if (this.ondelete) {
			this.ondelete(this);
		}
	},

	containerKeyPressed: function(e) {
		if (e.keyCode == Event.KEY_DOWN) {
			this.selectNext();
			Event.stop(e);
		}
		else if (e.keyCode == Event.KEY_UP) {
			this.selectPrevious();
			Event.stop(e);
		}
		else if (e.keyCode == Event.KEY_RETURN) {
			this.fireSelectAction();
			Event.stop(e);
		}
		else if (e.keyCode == Event.KEY_DELETE || e.keyCode == Event.KEY_BACKSPACE) {
			this.fireDeleteAction();
			Event.stop(e);
		}
	}
}