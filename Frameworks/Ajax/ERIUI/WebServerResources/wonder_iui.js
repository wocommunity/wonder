function createCSS(selector, declaration) {
	// test for IE
  var ua = navigator.userAgent.toLowerCase();
  var isIE = (/msie/.test(ua)) && !(/opera/.test(ua)) && (/win/.test(ua));
  
  // create the style node for all browsers
  var style_node = document.createElement("style");
  style_node.setAttribute("type", "text/css");
  style_node.setAttribute("media", "screen"); 
  
  // append a rule for good browsers
  if (!isIE) style_node.appendChild(document.createTextNode(selector + " {" + declaration + "}"));
  
  // append the style node
  document.getElementsByTagName("head")[0].appendChild(style_node);
  
  // use alternative methods for IE
 	if (isIE && document.styleSheets && document.styleSheets.length > 0) {
  	var last_style_node = document.styleSheets[document.styleSheets.length - 1];
  	if (typeof(last_style_node.addRule) == "object") last_style_node.addRule(selector, declaration);
  }
};
  
var currentWidth = 0;
var currentHash = '';
function checkOrientAndLocation() {
    if (window.innerWidth != currentWidth) {   
        currentWidth = window.innerWidth;
        var orient = currentWidth == 320 ? "profile" : "landscape";
        document.body.setAttribute("orient", orient);
        setTimeout(scrollTo, 100, 0, 1);
    }

    if (location.hash != currentHash) {
    	var newLocation;
    	if (location.hash.startsWith('#_')) {
    		var hashMatch = location.hash.match(/^#_([^_]+)_(.*)/);
    		var offset = hashMatch[1];
    		var hashmod = hashMatch[2];
    		newLocation = location.href.substr(0, location.href.length - location.hash.length - hashmod.length + parseInt(offset)) + hashmod;
    	}
    	else {
    		newLocation = document.location.href.replace('#', '/');
    	}
    	location.replace(newLocation);
    	currentHash = location.hash;
    }
}

function screenWidth() {
	return window.innerWidth;
}

Event.observe(window, "load", checkOrientAndLocation);

function toggleButtonToggled(id) {
	var toggleField = $(id);
	var toggleDiv = $(toggleField.id + '_div');
	if (toggleField.value == 'on') {
		toggleField.value = 'off';
	}
	else {
		toggleField.value = 'on';
	}
	toggleDiv.setAttribute('toggled', toggleDiv.getAttribute('toggled') != 'true');
	if (toggleField.onchange) {
		toggleField.onchange();
	}
}

var Rotator = Class.create({
	newElement: null,
	animationComplete: null,
	onContainerUpdated: null,
	onContainerUpdating: null,
	
	rotatorClass: null,

	current: null,
	previous: null,
	next: null,
	
	fade: false,
	slowOut: false,
	
	initialize: function(rotatorClass, fade, slowOut) {
		this.fade = fade;
		this.slowOut = slowOut;
		
		this.rotatorClass = rotatorClass;
		this.current = $$('.' + rotatorClass).first();
		
		this.previous = document.createElement('div');
		this.previous.addClassName(rotatorClass);
		this.current.parentNode.insertBefore(this.previous, this.current);
		
		this.next = document.createElement('div');
		this.next.addClassName(rotatorClass);

		if (this.next.nextSibling) {
			this.current.parentNode.insertBefore(this.next, this.current.nextSibling);
		}
		else {
			this.current.parentNode.appendChild(this.next);
		}

		this._containerUpdated(this.current);
	},

	_slideStart: function(direction) {
		if (direction == -1) {
			this.newElement = this.next;
			this.animationComplete = this._slideNextComplete.bind(this);
		}
		else {
			this.newElement = this.previous;
			this.animationComplete = this._slidePreviousComplete.bind(this);
		}
		if (this.onContainerUpdating) {
			this.onContainerUpdating(this.rotatorClass, this.newElement);
		}
		
		this.newElement.style.visibility = 'visible';
		
		if (this.fade) {
			this.current.style.opacity = 1.0;
			this.newElement.style.opacity = 0.0;
		}

		if (this.slowOut) {
			this.current.addClassName("slideSlow");
			this.newElement.addClassName("slideSlow");
		}
		else {
			this.current.addClassName("slide");
			this.newElement.addClassName("slide");
		} 
		
		if (direction == -1) {
			this.current.style.left = -screenWidth() + 'px';
			this.newElement.style.left = '0px';
		}
		else {
			this.current.style.left = screenWidth() + 'px';
			this.newElement.style.left = '0px';
		}
		
		if (this.fade) {
			this.current.style.opacity = 0.0;
			this.newElement.style.opacity = 1.0;
		}
	},
	
	_slidePreviousComplete: function() {
		var previous = this.previous;
		var current = this.current;
		var next = this.next;

		this.previous = next;
		this.current = previous;
		this.next = current;

		this._containerUpdated(current);
	},

	_slideNextComplete: function() {
		var previous = this.previous;
		var current = this.current;
		var next = this.next;
		
		this.previous = current;
		this.current = next;
		this.next = previous;
		
		this._containerUpdated(current);
	},
	
	_containerUpdated: function(previousCurrent) {
		var currentID = previousCurrent.id;

		this.current.style.left = '0px';
		this.current.removeClassName("slide");
		this.current.removeClassName("slideSlow");
		this.current.removeClassName("fade");

		this.previous.style.left = '-' + screenWidth() + 'px';
		this.previous.removeClassName("slide");
		this.previous.removeClassName("slideSlow");
		this.previous.removeClassName("fade");
		this.previous.style.visibility = 'hidden';
		this.previous.innerHTML = 'An error occurred.';

		this.current.style.left = '0px';
		/* this.current.style.opacity = 1.0; */
		
		this.next.style.left = screenWidth() + 'px';
		this.next.removeClassName("slide");
		this.next.removeClassName("slideSlow");
		this.next.removeClassName("fade");
		this.next.style.visibility = 'hidden';
		this.next.innerHTML = 'An error occurred.';
		
		if (this.onContainerUpdated) {
			this.onContainerUpdated();
		}
	}
	
});

var iPhoneSlider = Class.create({
	rotators: [],
	pending: null,
	animating: false,

  initialize: function(allOptions) {
		this.pending = document.createElement('div');
		this.pending.id = '_pending';
		this.pending.style.display = 'none';
		document.body.appendChild(this.pending);

  	allOptions.each(function(options) {
			var rotator = new Rotator(options.rotatorClass, options.fade || false, options.slowOut || false);
			rotator.onContainerUpdating = this._containerUpdating.bind(this);
			rotator.onContainerUpdated = this._containerUpdated.bind(this);
			this.rotators.push(rotator);
  	}.bind(this));
		
		this._replaceAllLinks(document.body);
	},
	
	_animationStart: function(direction) {
		this.animating = true;
		
		for (i = 0; i < this.rotators.length; i ++) {
			var rotator = this.rotators[i];
			rotator._slideStart(direction);
		}
		
		setTimeout(this._animationComplete.bind(this), 400);
	},
	
	_animationComplete: function() {
		for (var i = 0; i < this.rotators.length; i ++) {
			var rotator = this.rotators[i];
			rotator.animationComplete();
		}

		this.pending.immediateDescendants().each(function(elem) { elem.remove() });
				
		this.animating = false;
	},

	_containerUpdating: function(rotatorClass, destinationElement) {
		var newElement = this.pending.select('.' + rotatorClass).first();
		if (newElement) {
			var replacementHTML = newElement.innerHTML;
			destinationElement.innerHTML = replacementHTML;
			newElement.remove();
			this._replaceAllLinks(destinationElement);
		}
	},
	
	_containerUpdated: function() {
		checkOrientAndLocation();
	},
	
	updateAndSlide: function(direction, url) {
		new Effect.Event({
			queue: 'end',
			afterFinish: function() {
				new Ajax.Updater(this.pending, url, {
					method: 'get',
					insertion: function(receiver, response) {
						receiver.update(response);
						this._animationStart(direction);
					}.bind(this),
					asynchronous: true,
					evalScripts: true
				});
			}.bind(this)
		});
	},
	
	_replaceAllLinks: function(containerElement) {
		$A(containerElement.getElementsByTagName('a')).each(function(linkElement, index) {
			if (linkElement.hasClassName('transitionPrevious')) {
				this._replaceLink(linkElement, this.updateAndSlide.bind(this, 1));
			}
			else if (linkElement.hasClassName('transitionNext')) {
				this._replaceLink(linkElement, this.updateAndSlide.bind(this, -1));
			}
		}.bind(this));
		$A(containerElement.getElementsByTagName('input')).each(function(inputElement, index) {
			if (inputElement.type == 'submit') {
			/* 
				if (inputElement.hasClassName('transitionPrevious')) {
					this._replaceSubmit(inputElement, this.submitAndSlide.bind(this, -));
				}
				else if (inputElement.hasClassName('transitionNext')) {
					this._replaceSubmit(inputElement, this.submitAndSlideNext.bind(this));
				}
			*/
			}
		}.bind(this));
	},
	
	_setLocationHash: function(href) {
		var hash;
		var location = document.location.href;
		location = location.replace(/#.*/, '');
		if (href.startsWith(location)) {
			hash = href.substr(location.length + 1);
		}
		else {
			var i;
			for (i = 0; i < location.length && i < href.length; i ++) {
				if (location[i] != href[i]) {
					break;
				}
			}
			
			hash = '_' + (href.length - location.length) + '_' + href.substr(i);
		}
		
		document.location.hash=hash;
		currentHash=document.location.hash; 
	},
	
	_replaceLink: function(linkElement, linkFunction) {
		if (linkElement) {
			var linkHRef = linkElement.href;
			//alert(linkElement.innerHTML + ': ' + linkHRef);
			if (!linkHRef.startsWith('javascript:')) {
				linkElement.href = 'javascript:void(0)';
				Event.observe(linkElement, 'click', function() {
					if (!this.animating) {
						linkElement.setAttribute('selected', 'progress');
						this._setLocationHash(linkHRef);
						linkFunction(linkHRef);
					}
				}.bindAsEventListener(this));
			}
		}
	}
});