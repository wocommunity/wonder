/**
 * SegmentedSlider provides a picker for enumerated type values in the style of the iPhone on/off button.
 */
var SegmentedSlider = Class.create({
	/**
	 * Creates a new SegmentedSlider.
	 *
	 * @param sliderElement the div that will contain the slider
	 * @param name the form name of the radio button tags
	 */
	initialize: function(sliderElement, name, options) {
		this._sliderElement = $(sliderElement);
		this._initialSelection = !(options && options['initialSelection'] === false);
		this._toggleSelection = options && options['toggleSelection'];
		this._name = name;
		this._applyStyles();
		this._resetSlider();

		SegmentedSlider.sliders.set(this._sliderElement.identify(), this);
		
		if (options && options['enableDragSupport'] === true) {
			this.enableDragSupport();
		}
	},
	
	/**
	 * Adds an item to this segmented slider.
	 * 
	 * @param value the value for the radio button on this item
	 * @param displayName the display name for this item to show in its segment
	 * @param selected whether or not this item should be selected
	 */
	addItem: function(value, displayName, selected) {
		var sliderListElement = this._sliderElement.down('ol.items');
		var sliderItemElement = document.createElement('li');
		sliderItemElement.update('<input type="radio" name="' + this._name + '" value="' + value + '" ' + (selected ? 'checked' : '') + '/>' + (displayName || value));
		sliderListElement.insert(sliderItemElement);
		this._addItem(sliderItemElement, selected);
		return sliderItemElement;
	},
	
	/**
	 * Select an item.
	 *
	 * @param item the value of the item to select, or the corresponding li element
	 * @param animate whether or not to animate the selection
	 */
	select: function(item, animate) {
		return this._select(item, animate, false);
	},
		
	_select: function(item, animate, initialSelection) {
		var itemElement = item;
		
		if (typeof item == 'string') {
			var radioButton = this._sliderElement.down('input[value="' + item + '"]');
			if (radioButton) {
				itemElement = radioButton.up('li');
			}
			else {
				itemElement = null;
			}
		}

		var selector = this._selectorElement();
		var previousSelectedItemElement = this.selectedItemElement();
		
		this._sliderElement.select('input').each(function(radioButton) {
			radioButton.checked = false;
		});
		
		if (!initialSelection && previousSelectedItemElement == itemElement && this._toggleSelection) {
			itemElement = null;
		}

		if (itemElement) {
			itemElement.down('input').checked = true;
			var itemElementOffset = itemElement.positionedOffset();
			selector.style.width = itemElement.getWidth();
			if (animate) {
				new Effect.Morph(selector, { 
					style: 'left: ' + itemElementOffset.left + 'px',
					duration: 0.1,
					transition: Effect.Transitions.sinoidal,
					afterFinish: function() {
						if (this.isDragSupportEnabled()) {
							var selectorDragElement = this._selectorDragElement();
							selectorDragElement.style.left = itemElementOffset.left + 'px';
							selectorDragElement.style.width = itemElement.getWidth();
							this._currentDropTargetEl = itemElement;
						}
						this._beforeSelectStart(previousSelectedItemElement, itemElement, animate); 
						this._afterSelectFinish(previousSelectedItemElement, itemElement, animate); 
					}.bind(this)
				});
			}
			else {
				selector.style.left = itemElementOffset.left + 'px';
				if (this.isDragSupportEnabled()) {
					var selectorDragElement = this._selectorDragElement();
					selectorDragElement.style.left = itemElementOffset.left + 'px';
					selectorDragElement.style.width = itemElement.getWidth();
					this._currentDropTargetEl = itemElement;
				}
				this._beforeSelectStart(previousSelectedItemElement, itemElement, animate);
				this._afterSelectFinish(previousSelectedItemElement, itemElement, animate);
			}
		}
		else {
			// selector.style.left = '0px';
			this._beforeSelectStart(previousSelectedItemElement, itemElement, animate);
			this._afterSelectFinish(previousSelectedItemElement, itemElement, animate);
		}
	},

	/**
	 * Returns the top level slider div.
	 */
	sliderElement: function() {
		return this._sliderElement;
	},

	/**
	 * Returns an array of li elements corresponding to the options in this slider.
	 */
	sliderItemElements: function() {
		return this._sliderElement.select('ol.items li');
	},

	/**
	 * Returns the selected li element
	 */
	selectedItemElement: function() {
		return this._sliderElement.down('ol.items li.selected');
	},
	
	/**
	 * Returns the selected item value string.
	 */
	selectedItemValue: function() {
		return this._itemValue(this.selectedItemElement());
	},

	/**
	 * Returns the drag element.
	 */
	_selectorDragElement: function() {
		return this._sliderElement.down('div.selectorDrag');
	},

	/**
	 * Forces itemSelected to fire based on the current selection.
	 */
	fireItemSelected: function() {
		this._fireItemSelected(this.selectedItemElement());
	},

	_addItem: function(sliderItemElement, selected) {
		this._attachListener(sliderItemElement);
		this._addSeparator(sliderItemElement);
		if ((this._initialSelection && !this.selectedItemElement()) || selected) {
			this.select(sliderItemElement);
		}
	},
	
	_beforeSelectStart: function(previousSelectedItemElement, selectedItemElement, animate) {
		if (previousSelectedItemElement) {
			previousSelectedItemElement.removeClassName('selected');
			this._sliderElement.fire('slider:itemDeselected', { slider: this, sliderElement: this._sliderElement, itemElement: previousSelectedItemElement, name: this._itemValue(previousSelectedItemElement) });
		}
	},
	
	_afterSelectFinish: function(previousSelectedItemElement, selectedItemElement, animate) {
		if (!previousSelectedItemElement) {
			if (animate) {
				this._selectorElement().appear({ duration: 0.10 });
			}
			else {
				this._selectorElement().show();
			}
		}
		if (selectedItemElement) {
			selectedItemElement.addClassName('selected');
		}
		this._fireItemSelected(selectedItemElement);
		if (!selectedItemElement) {
			if (animate) {
				this._selectorElement().fade({ duration: 0.10 });
			}
			else {
				this._selectorElement().hide();
			}
		}
	},
	
	_itemValue: function(itemElement) {
		var itemValue;
		if (itemElement) {
			var radioButton = itemElement.down('input[type="radio"]');
			itemValue = radioButton.value;
		}
		return itemValue;
	},
	
	_applyStyles: function() {
		if (!this._sliderElement.hasClassName('slider')) {
			this._sliderElement.addClassName('slider');
		}
		
		var itemListElement = this._sliderElement.down('ol.items');
		if (!itemListElement) {
			var listElements = this._sliderElement.select('ol');
			if (listElements.length == 1) {
				itemListElement = listElements[0];
				itemListElement.addClassName('items');
			}
		}

		if (!this._sliderElement.down('div.selector')) {
			var selectorElement = document.createElement('div');
			selectorElement.addClassName('selector');
			this._sliderElement.insertBefore(selectorElement, itemListElement);
		}

		if (!this._sliderElement.down('ol.separators')) {
			var separatorListElement = document.createElement('ol');
			separatorListElement.addClassName('separators');
			this._sliderElement.insertBefore(separatorListElement, itemListElement);
		}

		if (!itemListElement) {
			itemListElement = document.createElement('ol');
			itemListElement.addClassName('items');
			this._sliderElement.appendChild(itemListElement);
		}
		
		var thisSegmentedSlider = this;
		var sliderItemElements = this.sliderItemElements().each(function(sliderItemElement) {
			var radioButton = sliderItemElement.down('input');
			if (!radioButton) {
				radioButton = document.createElement('input');
				radioButton.type = 'radio';
				radioButton.value = sliderItemElement.getAttribute('name') || sliderItemElement.id || sliderItemElement.innerHTML.trim();
				sliderItemElement.insertBefore(radioButton, sliderItemElement.firstChild);
			}
			thisSegmentedSlider._addItem(sliderItemElement, sliderItemElement.hasClassName('selected') || radioButton.checked);
		});
	},
	
	_attachListener: function(sliderItemElement) {
		var sliderController = this;
		sliderItemElement.observe('click', function(event) { sliderController.select(sliderItemElement, true); });
		if (this.isDragSupportEnabled()) {
			Droppables.add(sliderItemElement, { 
				accept: 'selectorDrag', 
				overlap: 'horizontal',
				onHover: this._dragHover.bind(this)
			});
		}
	},
	
	_resetSlider: function() {
		this._select(this.selectedItemElement(), false, true);
	},

	_selectorElement: function() {
		return this._sliderElement.down('div.selector');
	},

	_fireItemSelected: function(itemElement) {
		this._sliderElement.fire('slider:itemSelected', { slider: this, sliderElement: this._sliderElement, itemElement: itemElement, name: this._itemValue(itemElement) });
	},
		
	_addSeparator: function(sliderItemElement) {
		var separatorListElement = this._sliderElement.down('ol.separators');
		var separatorElement = document.createElement('li');
		separatorElement.update('&nbsp;');
		separatorElement.style.width = sliderItemElement.getWidth();
		separatorElement.style.paddingLeft = '0px';
		separatorElement.style.paddingRight = '0px';

		separatorListElement.insert(separatorElement);
	},
	
	isDragSupportEnabled: function() {
		return this._isDragSupportEnabled || false;
	},
	
	/**
	 * Enables drag support for the slider.
	 */
	enableDragSupport: function() {
		if (!this.isDragSupportEnabled()) {
			var sliderItemElements = this.sliderItemElements();
			for (var i = 0; i < sliderItemElements.length; i++) {
				var sliderItemElement = sliderItemElements[i];
				Droppables.add(sliderItemElement, { 
					accept: 'selectorDrag', 
					overlap: 'horizontal',
					onHover: this._dragHover.bind(this)
				});
			}
		
			var selectedItemElement = this.selectedItemElement()
			var selectorDragElement = document.createElement('div');
			selectorDragElement.id = 'selectorDragElement';
			selectorDragElement.addClassName('selectorDrag');
			this._sliderElement.appendChild(selectorDragElement);
			
			if (selectedItemElement !== undefined) {
				var offset = selectedItemElement.positionedOffset();
				selectorDragElement.style.left = offset.left + 'px';
				selectorDragElement.style.width = selectedItemElement.getWidth();
				this._currentDropTargetEl = selectedItemElement;
			}
			
			new Draggable(selectorDragElement, { 
				constraint: 'horizontal', 
				onDrag: this._drag.bind(this), 
				onEnd: this._dragEnd.bind(this), 
				snap: function(x, y, eventInfo) {
					var posX = eventInfo.element.positionedOffset().left;
					var targetEl = this._currentDropTargetEl || this._findItemElementWithCoordinates(posX, y);
					if (targetEl !== undefined) {
						return targetEl.positionedOffset();
					} else {
						return [x, y];
					}
				}.bind(this)
			});
		
			selectorDragElement.observe('mousedown', function() {
				this._selectorElement().addClassName("selector_active");
			}.bind(this));
		
			selectorDragElement.observe('mouseup', function() {
				this._selectorElement().removeClassName("selector_active");
				
				// Toggle selection support.
				var currentTarget = this._currentDropTargetEl;
				var selectedItemElement = this.selectedItemElement();
				if (selectedItemElement === undefined || (currentTarget !== undefined && currentTarget == this.selectedItemElement())) {
					this.select(currentTarget, true);
				}
			}.bind(this));
		
			this._isDragSupportEnabled = true;
		}
	},
	
	/**
	 * Removes drag support from the slider.
	 */
	removeDragSupport: function() {
		if (this.isDragSupportEnabled()) {
			this._isDragSupportEnabled = false;
		
			var sliderItemElements = this.sliderItemElements();
			for (var i = 0; i < sliderItemElements.length; i++) {
				var sliderItemElement = sliderItemElements[i];
				Droppables.remove(sliderItemElement);
			}
		
			var selectorDragElement = this._selectorDragElement();
			var drags = $A(Draggables.drags);
			var selectorDrag = drags.detect(function(n) { return n.element == selectorDragElement; });
			if (selectorDrag !== undefined) {
				selectorDrag.destroy();
			}
			this.sliderElement().removeChild(selectorDragElement);
		}
	},
	
	_findItemElementWithCoordinates: function(x, y) {
		return this.sliderItemElements().detect(function(el) {
			var offset = el.positionedOffset();
			return (x >= offset.left && x <= offset.left + el.getWidth() - 2);
		});
	},
	
	_dragHover: function(draggable, droppable, percentOverlap) {
		this._currentDropTargetEl = droppable;
	},
	
	_drag: function(eventInfo) {
		var draggedEl = eventInfo.element;
		var offset = draggedEl.positionedOffset();
		var selectorElement = this._selectorElement();
		
		// Keep the drag element and the selector elements' positions in sync.
		draggedEl.style.left = offset.left + 'px';
		selectorElement.style.left = offset.left + 'px';
		
		// Adjust the width of the slider to match the width of the item over which the drag element is currently held.
		var currentlyHoveredOverElement = this._currentDropTargetEl || this._findItemElementWithCoordinates(offset.left, 0);
		if (currentlyHoveredOverElement !== undefined) {
			var width = currentlyHoveredOverElement.getWidth();
			draggedEl.style.width = width + 'px';
			selectorElement.style.width = width + 'px';
		}
	},
	
	_dragEnd: function(evt) {
		this._selectorElement().removeClassName("selector_active");
		this.select(this._currentDropTargetEl, false);
	}
});

SegmentedSlider.sliders = new Hash(); // It's pretty useful to hang onto the sliders for later reference.
