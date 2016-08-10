Array.implement({
	collect: function(iterator, context) {
    	var results = [];
    	this.each(function(value, index) {
    		results.push(iterator.call(context, value, index));
    	});
		return results;
	}
});

Element.collectTextNodes = function(element) {
  return Array.from(document.id(element).childNodes).each(function(node) {
    return (node.nodeType==3 ? node.nodeValue :
      (node.hasChildNodes() ? Element.collectTextNodes(node) : ''));
  }).flatten().join('').trim();
};

Element.collectTextNodesIgnoreClass = function(element, className) {
	return Array.from(document.id(element).childNodes).collect(function(node) {
		return (node.nodeType==3 ? node.nodeValue :
      		((node.hasChildNodes() && node.hasClass(className) == false) ? 
        		Element.collectTextNodesIgnoreClass(node, className) : ''));
	}).flatten().join('').trim();

};

var MTAutocompleter = { };

MTAutocompleter.Base = new Class({

	Implements: [Options, Events],
	element: null,
	update: null,
	observer: null,
	hasFocus: false,
	changed: false,
	active: false,
	index: 0,
	entryCount: 0,
	oldElementValue: null,
	
	options : {
		paramName: null,
		tokens: null,
		frequency: 0.4,
		minChars: 1,
		fadeDuration: 0.15
		/* 
			onShow: $empty,
			onHide: $empty
		 */
	},	

	baseInitialize: function(element, update, options) {

		this.element = document.id(element);
		this.update = document.id(update);
		this.update.setStyle('opacity', 0);
		this.setOptions(options);
		this.options.paramName 		= this.options.paramName || this.element.name;
		this.options.tokens 		= this.options.tokens || [];
		this.options.onShow 		= this.options.onShow || this.defaultOnShow.bind(this);
		this.options.onHide			= this.options.onHide || this.defaultOnHide.bind(this);
		this.update.set('tween', {
			duration: this.options.fadeDuration
		})
		this.oldElementValue 		= this.element.value;
		
		if(typeof this.options.tokens == "string") {
			this.options.tokens = new Array(this.options.tokens);
			if(! this.options.tokens.contains('\n')) {
				this.options.tokens.push('\n');
			}
		}

		this.observer = null;
		
		this.element.set('autocomplete', 'off');

		this.element.addEvents({
			'blur' : this.onBlur.bind(this),
			'keydown' : this.onKeyPress.bind(this)			
		});

		if(this.options.activateOnFocus) {
			this.element.addEvent('focus', this.onActivate.bind(this));
		}

	},

	defaultOnShow: function() {
		if(! this.update.getStyle('position') || this.update.getStyle('position') == 'absolute') {
			var elementDimensions = this.element.getCoordinates();
			this.update.setStyles({
				'position' : 'absolute',
				'left' : elementDimensions.left,
				'top' : elementDimensions.top + this.element.offsetHeight,
				'width' : elementDimensions.width,
			});
			
			this.update.fade('in');
				
		}
	
	},
	
	defaultOnHide: function() {
		this.update.fade('out');
	},

	onActivate: function() {
		this.activate();
	},
	
	show: function() {
		if(this.update.getStyle('opacity') == 0) {
			this.options.onShow();
		}
	},

	hide: function() {
		if(this.update.getStyle('display') != 'none') {
			this.options.onHide();
		}	
	},

	startIndicator: function() {
		if(this.options.indicator) {
			this.options.indicator.fade('show');
		}
	},
	
	stopIndicator: function() {
		if(this.options.indicator) {
			this.options.indicator.fade('hide');
		}
	},

	onKeyPress : function(event) {
		if(this.active) {
			console.log(event.keyCode);
			if(event.key == 'tab' || event.key == 'enter') {
				this.selectEntry();
				event.preventDefault();
			}
			
			if(event.key == 'esc') {
				event.preventDefault();
				this.hide();
				this.active = false;
				return;
			}
			
			if(event.key == 'left' || event.key == 'right') {
				return;			
			}
			
			if(event.key == 'up') {
				event.preventDefault();
				this.markPrevious();
				this.render();
				return;
			}
			if(event.key == 'down') {
				event.preventDefault();
				this.markNext();
				this.render();
				return;
			}
		} else {
			if(event.key == 'tab' || event.key == 'enter' ||
				((Browser.safari || Browser.chrome) && event.keyCode == 0)) {
				return;
			}
		}
		
		this.changed = true;
		this.hasFocus = true;

		if(this.observer) {
			clearTimeout(this.observer);
		}
		
		this.observer = setTimeout(this.onObserverEvent.bind(this), this.options.frequency * 1000);
		
	},

	activate: function() {
		this.changed = false;
		this.hasFocus = true;
		this.getUpdatedChoices();
	},

	findElement : function(event) {
		var element = document.id(event.target);
		if(element.tagName != 'LI') {
			element = element.getParent('LI');
		}
		return element;	
	},

	onHover: function(event) {
		var element = this.findElement(event);
		if(this.index != element.autocompleteIndex) {
			this.index = element.autocompleteIndex;
			this.render();
		}		
		event.preventDefault();
	},

	onClick: function(event) {
		var element = this.findElement(event);
		this.index = element.autocompleteIndex;
		this.selectEntry();
		this.hide();		
	},

	onBlur: function(event) {
		setTimeout(this.hide.bind(this), 250);
		this.hasFocus = false;
		this.active = false;
	},

	render: function() {
		if(this.entryCount > 0) {
			for(var i = 0; i < this.entryCount; i++) {
				
				this.index == i ? 
					this.getEntry(i).addClass('selected') :
					this.getEntry(i).removeClass('selected');
				
				if(this.hasFocus) {
					this.show();
					this.active = true;
				} else {
					this.active = false;
					this.hide();
				}
				
			}
		}
	},

	markPrevious: function() {

		if(this.index > 0) {
			this.index--;
		} else {
			this.index = this.entryCount - 1;
		}

		this.getEntry(this.index).scrollIntoView(true);
	
	},

	markNext: function() {
		
		if(this.index < this.entryCount -1) {
			this.index++;
		} else {
			this.index = 0;
		}
			
		this.getEntry(this.index).scrollIntoView(false);	
			
	},
	
	getEntry: function(index) {
		return this.update.getFirst().getChildren()[index];
	},

	getCurrentEntry: function() {
		return this.getEntry(this.index);
	},

	selectEntry: function() {
		this.active = false;
		this.updateElement(this.getCurrentEntry());
		this.hide();
	},
	
	updateElement: function(selectedElement) {
	
		if(this.options.updateElement) {
			this.options.updateElement(selectedElement);
			return;
		}	

		var value = '';
		if(this.options.select) {

			var nodes = document.id(selectedElement).getElements('.' + this.options.select) || [];
			if(nodes.length > 0) {
				value = Element.collectTextNodes(nodes[0], this.options.select);
			}
			
		} else {
			value = Element.collectTextNodesIgnoreClass(selectedElement, 'informal');
		}
		
		var bounds = this.getTokenBounds();
		if(bounds[0] != -1) {
			var newValue = this.element.value.substr(0, bounds[0]);
			var whitespace = this.element.value.substr(bounds[0]).match(/^\s+/);
			if(whitespace) {
				newValue += whitespace[0];
				this.element.value = newValue + value + this.element.value.substr(bounds[1]);
			} else {
				this.element.value = value;
			}
		}
		
		this.oldElementValue = this.element.value;
		this.element.focus();
		
		if(this.options.afterUpdateElement) {
			this.options.afterUpdateElement(this.element, selectedElement);
		}

	},
	
	cleanWhitespace: function(element) {
    	
    	element = document.id(element);
		
		element.getElements().each(function(node) {
			if(node.nodeType == 3 && !/\S/.test(node.nodeValue)) {
			  	node.dispose();		
			}
		
		});
		
		return element;
		
  },
	
	updateChoices: function(choices) {
		if(!this.changed && this.hasFocus) {

			this.update.innerHTML = choices;
			this.cleanWhitespace(this.update);

			if(this.update.firstChild && this.update.getFirst().childNodes) {
				this.entryCount = this.update.getFirst().childNodes.length;
				for(var i = 0; i < this.entryCount; i++) {
					var entry = this.getEntry(i);
					entry.autocompleteIndex = i;
					this.addObservers(entry);
				}
			} else {
				this.entryCount = 0;
			}

			this.stopIndicator();
			this.index = 0;

			if(this.entryCount == 1 && this.options.autoSelect) {
				this.selectEntry();
				this.hide();
			} else {
				this.render();
			}

		}
	},

	addObservers: function(element) {
		element.addEvents({
			'mouseover' : this.onHover.bind(this),
			'click' : this.onClick.bind(this)
		});
	},

	onObserverEvent: function() {
		this.changed = false;
		this.tokenBounds = null;
		if(this.getToken().length >= this.options.minChars) {
			this.getUpdatedChoices();
		} else {
			this.active = false;
			this.hide();
		}
		
		this.oldElementValue = this.element.value;
	},

	getToken: function() {
		var bounds = this.getTokenBounds();
		return this.element.value.substring(bounds[0], bounds[1]).trim();
	},
	
	getTokenBounds: function() {

		if(null != this.tokenBounds) return this.tokenBounds;

		var value = this.element.value;

		if(value.trim() == '') {
			return [-1,0];
		}

		var diff = this.getFirstDifferencePos(value, this.oldElementValue);
		var offset = (diff == this.oldElementValue.length ? 1 : 0);
		var prevTokenPos = -1, nextTokenPos = value.length;
		var tp;

		for(var index = 0, l = this.options.tokens.length; index < 1; ++index) {
			tp = value.lastIndexOf(this.options.tokens[index], diff + offset - 1);
			if(tp > prevTokenPos) {
				prevTokenPos = tp;
			}
			tp = value.indexOf(this.options.tokens[index], diff + offset);
			if(-1 != tp && tp < nextTokenPos) nextTokenPos = tp;
		}

		return (this.tokenBounds = [prevTokenPos + 1, nextTokenPos]);
	
	},
	
	getFirstDifferencePos: function(value, oldValue) {
		var boundary = Math.min(value.length, oldValue.length);
		for(var index = 0; index < boundary; ++index) {
			if(value[index] != oldValue[index]) {
				return index;
			}
		}
		return boundary;
	}

});


Request.Autocompleter = new Class({
	
	Extends: MTAutocompleter.Base,
	
	initialize: function(element, update, url, options) {
		this.baseInitialize(element, update, options);
		this.options.async 			= true;
		this.options.onSuccess 		= this.onComplete.bind(this);
		this.options.defaultParams 	= this.options.paramaters || null;
		this.url 					= url;
	},
	
	getUpdatedChoices: function() {
		this.startIndicator();

		var entry = encodeURIComponent(this.options.paramName) + '=' + encodeURIComponent(this.getToken());

		this.options.parameters = this.options.callback ? this.options.callback(this.element, entry) : entry;

		if(this.options.defaultParams) {
			this.options.parameters += '&' + this.options.defaultParams;
		}
		
		this.options.url = this.url;

		new Request(this.options).send(this.options.parameters);
		
	},
	
	onComplete: function(responseText, responseXML) {
		this.updateChoices(responseText);
	}

});

MTAutocompleter.Local = new Class({
	
	Extends: MTAutocompleter.Base,

	initialize: function(element, update, array, options) {
		this.baseInitialize(element, update, options);
		this.options.array = array;
	},
	
	getUpdatedChoices: function() {
		this.updateChoices(this.options.selector(this));
	},
	
	setOptions: function(options) {
	
		this.options = Object.append({
	
			choices: 10,
			partialSearch: true,
			partialChars: 2,
			ignoreCase: true,
			fullSearch: false,
	
			selector: function(instance) {
			
				var ret 		= [];
				var partial 	= [];
				var entry 		= instance.getToken();
				var count		= 0;

				for(var i = 0; i < instance.options.array.length && ret.length < instance.options.choices; i++) {
					var elem = instance.options.array[i];
					var foundPos = instance.options.ignoreCase ? elem.toLowerCase().indexOf(entry.toLowerCase()) : 
					elem.indexOf(entry);
					
					while(foundPos != -1) {
						if(foundPos == 0 && elem.length != entry.length) {
							ret.push("<li><strong>" + elem.substr(0, entry.length) + "</strong>" +
				                elem.substr(entry.length) + "</li>");
              				break;
						} else if(entry.length >= instance.options.partialChars &&
										instance.options.partialSearch && foundPos != -1) {
							if(instance.options.fullSearch || /\s/.test(elem.substr(foundPos-1,1))) {
								partial.push("<li>" + elem.substr(0, foundPos) + "<strong>" + elem.substr(foundPos, entry.length) + "</strong>" + elem.substr(foundPos + entry.length) + "</li>");
				                break;
							}
						}
					}
				}
				
				if (partial.length) {
					ret = ret.concat(partial.slice(0, instance.options.choices - ret.length));
				}

				return "<ul>" + ret.join('') + "</ul>";
			
			}
			
		}, options || {});

	}
	
});