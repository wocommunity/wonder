/*
ModalBox - The pop-up window thingie with AJAX, based on prototype and script.aculo.us.

Copyright Andrey Okonetchnikov (andrej.okonetschnikow@gmail.com), 2006-2007
All rights reserved.
 
VERSION 1.6.1
Last Modified: 01/11/2010 
*/

if (Object.isUndefined(Prototype.Browser.IE6)) {
	Prototype.Browser.IE6 = (navigator.appName.indexOf("Microsoft Internet Explorer") != -1 && navigator.appVersion.indexOf("MSIE 6.0") != -1 && !window.XMLHttpRequest);
}

if (!window.Modalbox)
	var Modalbox = {};

Modalbox.Methods = {
	overrideAlert: false, // Override standard browser alert message with ModalBox
	focusableElements: [],
	currFocused: 0,
	initialized: false, // Modalbox is visible
	active: true, // Modalbox is visible and active
	options: {
		title: "ModalBox Window", // Title of the ModalBox window
		overlayClose: true, // Close modal box by clicking on overlay
		width: 500, // Default width in px
		height: 90, // Default height in px
		minWidth: 500, // TC add minWidth option
		centerVertically: false, // True if should be centered vertically on page
		overlayOpacity: 0.65, // Default overlay opacity
		overlayDuration: 0.25, // Default overlay fade in/out duration in seconds
		slideDownDuration: 0.5, // Default Modalbox appear slide down effect in seconds
		slideUpDuration: 0.5, // Default Modalbox hiding slide up effect in seconds
		resizeDuration: 0.25, // Default resize duration seconds
		inactiveFade: true, // Fades MB window on inactive state
		transitions: true, // Toggles transition effects. Transitions are enabled by default
		loadingString: "Please wait. Loading...", // Default loading string message
		closeString: "Close window", // Default title attribute for close window link
		closeValue: "&times;", // Default string for close link in the header
		locked: false, // Use true to supress close window link, prevent Esc key and overlay from closing dialog
		params: {},
		method: "get", // Default Ajax request method
		autoFocusing: true, // Toggles auto-focusing for form elements. Disable for long text pages.
		aspnet: false // Should be true when using with ASP.NET controls. When true Modalbox window will be injected into the first form element.
	},
	_options: {},
	_initOptions: {}, // TC Added - Keep the initialization options around.
	
	setOptions: function(options) {
		// locked implies the overlay is also deactivated
		if (options.locked) {
			options.overlayClose = false;
		}
		Object.extend(this.options, options || {});
	},
	
	_init: function(options) {
		// TC Added - Save the initial options.
		Object.extend(this._initOptions, options || {});
		
		// Setting up original options with default options
		Object.extend(this._options, this.options);
		this.setOptions(options);
		
		// Creating the overlay
		this.MBoverlay = new Element("div", {id: "MB_overlay", style: "opacity: 0"});
		
		// Creating the modal window
		this.MBwindow = new Element("div", {id: "MB_window", style: "display: none"}).update(
			this.MBframe = new Element("div", {id: "MB_frame"}).update(
				this.MBheader = new Element("div", {id: "MB_header"}).update(
					this.MBcaption = new Element("div", {id: "MB_caption"})
				)
			)
		);
		if ( ! this.options.locked) {
			this.MBclose = new Element("a", {id: "MB_close", title: this.options.closeString, href: "#"}).update("<span>" + this.options.closeValue + "</span>");
			this.MBheader.insert({'bottom':this.MBclose});
		}
		this.MBcontent = new Element("div", {id: "MB_content"}).update(
			this.MBloading = new Element("div", {id: "MB_loading"}).update(this.options.loadingString)
		);
		this.MBframe.insert({'bottom':this.MBcontent});

		// Inserting into DOM. If parameter set and form element have been found will inject into it. Otherwise will inject into body as topmost element.
		// Be sure to set padding and marging to null via CSS for both body and (in case of asp.net) form elements.
		var injectToEl = this.options.aspnet ? $(document.body).down('form') : $(document.body);
		injectToEl.insert({'top':this.MBwindow});
		injectToEl.insert({'top':this.MBoverlay});

		// Initial scrolling position of the window. To be used for remove scrolling effect during ModalBox appearing
		this.initScrollX = window.pageXOffset || document.body.scrollLeft || document.documentElement.scrollLeft;
		this.initScrollY = window.pageYOffset || document.body.scrollTop || document.documentElement.scrollTop;

		//Adding event observers
		this.hideObserver = this._hide.bindAsEventListener(this);
		this.kbdObserver = this._kbdHandler.bindAsEventListener(this);
		this.resizeObserver = this._setWidthAndPosition.bindAsEventListener(this);
		this._initObservers();

		this.initialized = true; // Mark as initialized
	},

	show: function(content, options) {
		if (!this.initialized) this._init(options); // Check if MB is already initialized

		this.content = content;
		this.setOptions(options);

		if (this.options.title) { // Updating title of the MB
			this.MBcaption.update(this.options.title);
		} else { // If title isn't given, the header will not displayed
			this.MBheader.hide();
			this.MBcaption.hide();
		}

		if (this.MBwindow.style.display == "none") { // First MB appearing
			this._appear();
			this.event("onShow"); // Passing onShow callback
		} else { // If MB already on the screen, update it
			this._update();
			this.event("onUpdate"); // Passing onUpdate callback
		}
	},

	hide: function(options) { // External hide method to use from external HTML and JS
		if(this.initialized) {
			// Reading for options/callbacks except if event given as a parameter
			if (options && !Object.isFunction(options.element))
				Object.extend(this.options, options);
			this.event("beforeHide"); // Passing beforeHide callback
			if (this.options.transitions) {
				Effect.SlideUp(this.MBwindow, { duration: this.options.slideUpDuration, transition: Effect.Transitions.sinoidal, afterFinish: this._deinit.bind(this) });
			} else {
				this.MBwindow.hide();
				this._deinit();
			}
		} else {
			throw("Modalbox is not initialized.");
		}
	},
	
	_hide: function(event) { // Internal hide method to use with overlay and close link
		event.stop(); // Stop event propagation for link elements
		// When clicked on overlay we'll check the option and in case of overlayClose == false we'll break hiding execution [Fix for #139]
		if (event.element().id == 'MB_overlay' && !this.options.overlayClose) return false;
		this.hide();
	},
	
	alert: function(message){
		var html = '<div class="MB_alert"><p>' + message + '</p><input type="button" onclick="Modalbox.hide()" value="OK" /></div>';
		Modalbox.show(html, {title: 'Alert: ' + document.title, width: 300});
	},
		
	_appear: function() { // First appearing of MB
		if (Prototype.Browser.IE6) { // Preparing IE 6 for showing modalbox
			window.scrollTo(0,0);
			this._prepareIEHtml("100%", "hidden");
			this._prepareIESelects("hidden");
		}
		this._setSize();
		this._setPosition();
		if(this.options.transitions) {
			this.MBoverlay.setOpacity(0);
			new Effect.Fade(this.MBoverlay, {
					from: 0, 
					to: this.options.overlayOpacity, 
					duration: this.options.overlayDuration, 
				afterFinish: (function() {
						new Effect.SlideDown(this.MBwindow, {
							duration: this.options.slideDownDuration, 
							transition: Effect.Transitions.sinoidal, 
						afterFinish: (function() {
								this._setPosition(); 
								this.loadContent();
						}).bind(this)
						});
				}).bind(this)
			});
		} else {
			this.MBoverlay.setOpacity(this.options.overlayOpacity);
			this.MBwindow.show();
			this._setPosition(); 
			this.loadContent();
		}
		Event.observe(window, "resize", this.resizeObserver);
	},

	_update: function() { // Updating MB in case of wizards
		this.MBcontent.update($(this.MBloading).update(this.options.loadingString));
		this.loadContent();
	},
	
	resizeTo: function(newWidth, newHeight, options) { // Change size of MB without content reloading
		var o = this.MBoverlay.getDimensions();		
		// SWK: Check for window height > viewport
		var minMargin = (this.options.centerVertically === true) ? 40 : 20;
		if (newHeight >= (o.height - minMargin) ) {
			var h = this.MBheader.getDimensions();
			var pageHeight = o.height - minMargin;
			var ch = pageHeight - h.height - 13;
			this.MBcontent.setStyle({height: ch + 'px', 'overflow-y': 'scroll'});
			// Try to widen the content area to allow for a vertical scrollbar.
			var scrollbarWidth = 16;
			if ((this.MBwindow.getWidth() + scrollbarWidth) < (document.viewport.getWidth() - 40)) {
				this.MBwindow.setStyle({width: (this.MBwindow.getWidth() + scrollbarWidth) + 'px'});
			}
			newHeight = pageHeight;
		}	
		
		var newStyle = {width: newWidth + "px", height: newHeight + "px", left: (o.width - newWidth)/2 + "px"};
		this.options.width = newWidth;
		if (options) this.setOptions(options); // Passing callbacks
		if (this.options.transitions) {
			new Effect.Morph(this.MBwindow, {
				style: newStyle,
				duration: this.options.resizeDuration, 
				beforeStart: function(fx){
					fx.element.setStyle({overflow: "hidden"}); // Fix for MSIE 6 to resize correctly
				},
				afterFinish: (function(fx) {
					fx.element.setStyle({overflow: "visible"});
					this.event("_afterResize"); // Passing internal callback
					this.event("afterResize"); // Passing callback
				}).bind(this)
			});
		} else {
			// MS: Modified to support height = -1 converts to height "auto"
			var heightStyle = (this.options.height == -1) ? "auto" : (newHeight + "px");
			newStyle["height"] = heightStyle;
			this.MBwindow.setStyle(newStyle);
			(function() {
				this.event("_afterResize"); // Passing internal callback
				this.event("afterResize"); // Passing callback
			}).bind(this).defer();
		}
	},
		
	resize: function(byWidth, byHeight, options) { // Change size of MB without loading content
		var w = this.MBwindow.getDimensions(), hHeight = this.MBheader.getHeight(), cHeight = this.MBcontent.getHeight();
		this.resizeTo((w.width + byWidth), Math.max(hHeight + cHeight, w.height + byHeight), options);
	},
	
	resizeToContent: function(options){
		// Resizes the modalbox window to the actual content height.
		// This might be useful to resize modalbox after some content modifications which were changed content height.
		var byHeight = this.options.height - this.MBwindow.getHeight();
		if (byHeight != 0) {
			this.resize(0, byHeight, options);
		}
	},
	
	resizeToInclude: function(element, options){
		// Resizes the modalbox window to the cumulative height of element. Calculations are using CSS properties for margins and border.
		// This method might be useful to resize modalbox before including or updating content.
		
		element = $(element);
		var styles = ['margin-top','margin-bottom','border-top-width','border-bottom-width'];
		var elHeight = styles.inject(element.getHeight(), function(acc, n) {
			var x = parseInt(element.getStyle(n), 10);
			acc += (isNaN(x) ? 0 : x);
			return acc;
		});
		if (elHeight > 0) {
			Modalbox.resize(0, elHeight, options);
		}
	},
	
	loadContent: function() {
		if (this.event("beforeLoad")) { // If callback returned false, skip loading of the content
			if (typeof this.content == 'string') {
				var htmlRegExp = new RegExp(/<\/?[^>]+>/gi), evalScript = function(script) {
							return eval(script.replace("<!--", "").replace("// -->", ""));
				};
				if (htmlRegExp.test(this.content)) { // Plain HTML given as a parameter
					this._insertContent(this.content.stripScripts(), (function() {
						this.content.extractScripts().map(evalScript, window);
					}).bind(this));
				} else { // URL given as a parameter. We'll request it via Ajax
					new Ajax.Request(this.content, {
						method: this.options.method.toLowerCase(),
						parameters: this.options.params,
						onSuccess: (function(transport) {
							var response = new String(transport.responseText);
							this._insertContent(transport.responseText.stripScripts(), function(){
								response.extractScripts().map(evalScript, window);
							});
						}).bind(this),
						onException: function(instance, exception){
							Modalbox.hide();
							throw('Modalbox Loading Error: ' + exception);
						}
					});
				}
			} else if (typeof this.content == 'object') { // HTML Object is given
				this._insertContent(this.content);
			} else {
				this.hide();
				throw('Modalbox Parameters Error: Please specify correct URL or HTML element (plain HTML or object)');
			}
		}
	},
	
	_insertContent: function(content, callback) {
		this.MBcontent.hide().update();

		if (typeof content == 'string') { // Plain HTML is given
			this.MBcontent.insert(new Element("div", { style: "display: none" }).update(content)).down().show();
		} else if (typeof content == 'object') { // HTML Object is given
			var _htmlObj = content.cloneNode(true); // If node already a part of DOM we'll clone it
			// If cloneable element has ID attribute defined, modifying it to prevent duplicates
			if (content.id) content.id = "MB_" + content.id;
			// Add prefix for IDs on all elements inside the DOM node
			$(content).select('*[id]').each(function(el) { el.id = "MB_" + el.id; });
			this.MBcontent.insert(_htmlObj).down().show();
			if (Prototype.Browser.IE6) { // Toggle back visibility for hidden selects in IE
				this._prepareIESelects("", "#MB_content ");
			}
		}
		
		// Prepare and resize modal box for content
		// MS: check for a -1 height ... I'm only doing this because the "else" tries to make it scrollable
		if(this.options.height == this._options.height || this.options.height == -1) {
			this.resizeTo(this.__computeWidth(), this.__computeHeight(), { // TC - Changed to use __computeWidth and __computeHeight.
				'afterResize': (function() {
					this._putContent.bind(this, callback).defer(); // MSIE fix
					this._setWidthAndPosition.bind(this, callback).defer(); // CH: Set position (and width) after the content loads so that dialog is centered when width = - 1
				}).bind(this)
			});
		} else { // Height is defined. Creating a scrollable window
			this._setSize();
			this.MBcontent.setStyle({
				overflow: 'auto',
				height: this.options.height - this.MBheader.getHeight() - 13 + 'px'
			});
			this._putContent.bind(this, callback).defer(); // MSIE fix
		}
	},
	
	_putContent: function(callback) {
		this.MBcontent.show();
		this._findFocusableElements();
		this._setFocus(); // Setting focus on first 'focusable' element in content (input, select, textarea, link or button)
		if (Object.isFunction(callback))
			callback(); // Executing internal JS from loaded content
		this.event("afterLoad"); // Passing callback
		// CH move _setFocus to after timeout so elements with onFocus binding aren't focused too early
		//this._setFocus(); // Setting focus on first 'focusable' element in content (input, select, textarea, link or button)
	},
	
	activate: function(options) {
		this.setOptions(options);
		this.active = true;
		if ( ! this.options.locked) 
			this.MBclose.observe("click", this.hideObserver);
		if (this.options.overlayClose)
			this.MBoverlay.observe("click", this.hideObserver);
		if ( ! this.options.locked) 
			this.MBclose.observe("click", this.hideObserver).show();
		if (this.options.transitions && this.options.inactiveFade)
			new Effect.Appear(this.MBwindow, {duration: this.options.slideUpDuration});
	},
	
	deactivate: function(options) {
		this.setOptions(options);
		this.active = false;
		if ( ! this.options.locked) 
			this.MBclose.stopObserving("click", this.hideObserver).hide();
		if (this.options.overlayClose)
			this.MBoverlay.stopObserving("click", this.hideObserver);
		if ( ! this.options.locked) 
			this.MBclose.hide();
		if (this.options.transitions && this.options.inactiveFade)
			new Effect.Fade(this.MBwindow, {duration: this.options.slideUpDuration, to: 0.75});
	},
	
	_initObservers: function(){
		if ( ! this.options.locked) 
			this.MBclose.observe("click", this.hideObserver);
		if (this.options.overlayClose)
			this.MBoverlay.observe("click", this.hideObserver);
		// Gecko and Opera are moving focus a way too fast, all other browsers are okay with keydown
		var kbdEvent = (Prototype.Browser.Gecko || Prototype.Browser.Opera) ? "keypress" : "keydown";
		Event.observe(document, kbdEvent, this.kbdObserver);
	},
	
	_removeObservers: function(){
		if ( ! this.options.locked) 
			this.MBclose.stopObserving("click", this.hideObserver);
		if (this.options.overlayClose)
			this.MBoverlay.stopObserving("click", this.hideObserver);
		var kbdEvent = (Prototype.Browser.Gecko || Prototype.Browser.Opera) ? "keypress" : "keydown";
		Event.stopObserving(document, kbdEvent, this.kbdObserver);
	},
	
	_setFocus: function() { 
		// Setting focus to the first 'focusable' element which is one with tabindex = 1 or the first in the form loaded.
		if (this.options.autoFocusing === true && this.focusableElements.length > 0) {

			// MS: don't steal focus if there is already an element inside the AMD that is focused
			var focusedElement = $$('*:focus').first();
			var alreadyFocused = focusedElement && this.focusableElements.indexOf(focusedElement) != -1;
			if (alreadyFocused) {
				return;
			}
			// MS: done
			
			var firstEl = this.focusableElements.find(function (el){
				return el.tabIndex == 1;
			}) || this.focusableElements.first();
			
			// MS: try to focus on form field rather than a link ...
			var inputTagNames = ['input', 'select', 'textarea'];
			if (firstEl && !inputTagNames.include(firstEl.tagName.toLowerCase())) {
				var firstInputEl = this.focusableElements.find(function(element) {
					return inputTagNames.include(element.tagName.toLowerCase());
				});
				if (firstInputEl) {
					firstEl = firstInputEl;
				}
			}

			this.currFocused = this.focusableElements.toArray().indexOf(firstEl);
			firstEl.focus(); // Focus on first focusable element except close button
		} else if(! this.options.locked && this.MBclose.visible()) {
			$(this.MBclose).focus(); // If no focusable elements exist focus on close button
		}
	},
	
	_findFocusableElements: function(){ // Collect form elements or links from MB content, elements with class MB_notFocusable are excluded
		if (this.options.autoFocusing === true) {
			// TODO maybe add :enabled to select and textarea elements
			this.MBcontent.select('input:not([type~=hidden]):enabled, select, textarea, button, a[href]').invoke('addClassName', 'MB_focusable');
			this.focusableElements = this.MBcontent.select('.MB_focusable').reject(function(e) { return e.hasClassName('MB_notFocusable'); });
		}
	},
	
	_kbdHandler: function(event) {
		var node = event.element();
		switch(event.keyCode) {
			case Event.KEY_TAB:
				event.stop();
				
				// Switching currFocused to the element which was focused by mouse instead of TAB-key. Fix for #134
				if (node != this.focusableElements[this.currFocused])
					this.currFocused = this.focusableElements.indexOf(node);
				
				if (!event.shiftKey) { // Focusing in direct order
					if(this.currFocused >= this.focusableElements.length - 1) {
						this.currFocused = 0;
					} else {
						this.currFocused++;
					}
				} else { // Shift key is pressed. Focusing in reverse order
					if(this.currFocused <= 0) {
						this.currFocused = this.focusableElements.length - 1;
					} else {
						this.currFocused--;
					}
				}

				var focusedElement = this.focusableElements[this.currFocused];
				if(focusedElement) focusedElement.focus();

				break;			
			case Event.KEY_ESC:
				// CH: Add Esc key handling start
				if (this.options.clickOnEscId) {
					var target = $(this.options.clickOnEscId);
					if (target && this._isClickable(target)) {
						target.click();
						event.stop();
					}
				}
				// CH: done
				if(this.active && ! this.options.locked) this._hide(event);
				break;
			case 32:
				this._preventScroll(event);
				break;
			case 0: // For Gecko browsers compatibility
				if (event.which == 32) this._preventScroll(event);
				break;
			case Event.KEY_UP:
			case Event.KEY_DOWN:
				// Allow up and down arrow keys in text boxes in WebKit browsers,
				// because these keys can move the cursor.
				if(Prototype.Browser.WebKit && (["textarea","select"].include(node.tagName.toLowerCase()) ||
					(node.tagName.toLowerCase() == "input" && ["text", "password"].include(node.type)))) {
					break;
				}
			case Event.KEY_PAGEDOWN:
			case Event.KEY_PAGEUP:
			case Event.KEY_HOME:
			case Event.KEY_END:
				// Safari operates in slightly different way. This realization is still buggy in Safari.
				if(Prototype.Browser.WebKit && !["textarea", "select"].include(node.tagName.toLowerCase()))
					event.stop();
				else if( this._isClickable(node) )  // CH: change to use _isClickable
					event.stop();
				break;
			// CH: Add Return key handling start
			case Event.KEY_RETURN:
				if (this.options.clickOnReturnId) {
					var target = $(this.options.clickOnReturnId);
					// Don't trigger this for clickable elements or text areas
					if (target && this._isClickable(target)) {
						// Only click the target if node is not clickable or if node is not in the dialog box
						if (this.MBcontent.select('input:not([type~=hidden]), select, textarea, button, a[href]').indexOf(node) == -1 ||
							! (this._isClickable(node) || ["textarea"].include(node.type)) ) {
							target.click();
							event.stop();
						}
					}
				}
				break;
			// CH: done
		}
	},
	
	// CH: add _isClickable
	_isClickable: function(element) {
		return (["input", "button"].include(element.tagName.toLowerCase()) && ["submit", "button"].include(element.type)) || (element.tagName.toLowerCase() == "a")
	},
	// CH: done
	
	_preventScroll: function(event) { // Disabling scrolling by "space" key
		var el = event.element();
		if (!["input", "textarea", "select", "button"].include(el.tagName.toLowerCase())
				&& !(el.contentEditable == 'true' || el.contentEditable == ''))
			event.stop();
	},
	
	_deinit: function() {
		this._removeObservers();
		Event.stopObserving(window, "resize", this.resizeObserver);
		if (this.options.transitions) {
			Effect.toggle(this.MBoverlay, 'appear', {duration: this.options.overlayDuration, afterFinish: this._removeElements.bind(this) });
		} else {
			this.MBoverlay.hide();
			this._removeElements();
		}
		this.MBcontent.setStyle({overflow: '', height: ''});
	},
	
	_removeElements: function() {
		if (Prototype.Browser.Opera) { // Remove overlay after-effects in Opera
			window.scrollBy(0, 0);
		}
		this.MBoverlay.remove();
		this.MBwindow.remove();
		if (Prototype.Browser.IE6) {
			this._prepareIEHtml("", ""); // If set to auto MSIE will show horizontal scrolling
			this._prepareIESelects("");
			window.scrollTo(this.initScrollX, this.initScrollY);
		}
		
		// Replacing prefixes 'MB_' in IDs for the original content
		if (typeof this.content == 'object') {
			if(this.content.id && this.content.id.match(/MB_/)) {
				this.content.id = this.content.id.replace(/MB_/, "");
			}
			this.content.select('*[id]').each(function(el) { el.id = el.id.replace(/MB_/, ""); });
		}
		// Initialized will be set to false
		this.initialized = false;
		this.event("afterHide"); // Passing afterHide callback
		this.setOptions(this._options); // Settings options object into initial state
	},
	
	_setSize: function() { // Set size
		// MS: Add support for -1 width/height
		var width = this.__computeWidth(); // TC - Changed to use __computeWidth.
		var heightStyle = (this.options.height == -1) ? "auto" : (this.__computeHeight() + "px");
		this.MBwindow.setStyle({width: width + "px", height: heightStyle});
		this.__adjustContentHeightIfNecessary(); // TC added
	},
	
	_setPosition: function() {
		this.MBwindow.setStyle({left: ((this.MBoverlay.getWidth() - this.MBwindow.getWidth()) / 2 ) + "px"});

		// CH: Add vertical centering
		if (this.options.centerVertically) {
			var elem = $(this.MBwindow);
			var docElem = document.documentElement;
			pageHeight = self.innerHeight || (docElem&&docElem.clientHeight) || document.body.clientHeight;
			elemHeight = elem.getHeight();
			var y = Math.round(pageHeight/2) - (elemHeight/2);	
			elem.style.top = y+'px';
		}
		// CH: Done adding vertical centering
		
		this.__adjustContentHeightIfNecessary(); // TC added
	},
	
	_setWidthAndPosition: function() {
		var wWidth = this.__computeWidth(); // TC - Changed to use __computeWidth.
		this.MBwindow.setStyle({
			width: wWidth + "px",
			left: ((this.MBoverlay.getWidth() - this.options.width) / 2 ) + "px"
		});
		this._setPosition();
	},
	
	/**
	 * Checks the content height is greater than the dialog height, and adjusts the content area dimensions as necessary.
	 */
	// Added by TC.
	__adjustContentHeightIfNecessary: function() {
		// Check for content height > MBwindow height
		if (this.options.height != -1 && this.MBcontent.getHeight() > this.MBwindow.getHeight()) {
			this.MBcontent.setStyle({height: this.__computeContentHeight() + 'px', 'overflow-y': 'scroll'});
			// Try to widen the content area to allow for a vertical scrollbar.
			var scrollbarWidth = 16;
			if ((this.MBwindow.getWidth() + scrollbarWidth) < (document.viewport.getWidth() - 40)) {
				this.MBwindow.setStyle({width: (this.MBwindow.getWidth() + scrollbarWidth) + 'px'});
			}
		}
	},
	
	/**
	 * Computes the necessary width of the dialog, optionally making sure the dialog width fits within the viewport.
	 * @return the width in px for the MBwindow element
	 */
	// Added by TC.
	__computeWidth: function() {
		var newWidth;
		if (this._initOptions.width && this._initOptions.width != -1) { // If there's an explicit width set, respect the value.
			newWidth = this.options.width;
		} else { // If there's no explicit width, calculate it.
			var cWidth = this.MBcontent.getWidth();
			var pageWidth = document.viewport.getWidth();
			if (cWidth < (pageWidth - 40)) { // Allow at least 20px margin on either side of the dialog.
				newWidth = (cWidth < this.options.width) ? this.options.width : cWidth;
			} else { // Too big to fit in window.
				newWidth = pageWidth - 40;
			}
			newWidth = (newWidth > 200) ? newWidth : 200; // Enforce a minimum width of at least 200px.
		}
		return newWidth;
	},

	/**
	 * Computes the necessary height of the dialog, optionally making sure the dialog height fits within the viewport.
	 * @return the height in px for the MBwindow element
	 */
	// Added by TC.
	__computeHeight: function() {
		var newHeight;
		if (this._initOptions.height && this._initOptions.height != -1) { // If there's an explicit height set, respect the value.
			newHeight = this.options.height;
		} else { // If there's no explicit height, calculate it.
			var cHeight = this.MBheader.getHeight() + this.MBcontent.getHeight();
			var pageHeight = document.viewport.getHeight();
			var minMargin = (this.options.centerVertically === true) ? 40 : 20;
			if (cHeight < (pageHeight - minMargin)) { // Allow at least 20px margin on the bottom of the dialog.
				newHeight = (cHeight < this.options.height) ? this.options.height : cHeight;
			} else { // Too big to fit in window.
				newHeight = pageHeight - 20;
			}
			newHeight = (newHeight > 90) ? newHeight : 90; // Enforce a minimum height of at least 90px.
		}
		return newHeight;
	},
	
	/**
	 * Computes the necessary height of the dialog's content area, making sure the content height fits within the dialog.
	 * @return the height in px for the MBcontent element
	 */
	// Added by TC.
	__computeContentHeight: function() {
		var newHeight = -1;
		var wHeight = this.__computeHeight();
		var cHeight = this.MBcontent.getHeight();
		var wHeight = this.MBwindow.getHeight();
		if (cHeight > (wHeight - this.MBheader.getHeight())) { // Too big to fit in window.
			newHeight = wHeight - this.MBheader.getHeight() - 13;
		}
		return newHeight;
	},

	_prepareIEHtml: function(height, overflow) { // IE requires width and height set to 100% and overflow hidden
		$$('html, body').invoke('setStyle', {width: height, height: height, overflow: overflow});
	},

	_prepareIESelects: function(overflow, prefix) { // Toggle visibility for select elements
		$$((prefix || "") + "select").invoke('setStyle', {'visibility': overflow});
	},
	
	event: function(eventName) {
		var r = true;
		if (this.options[eventName]) {
			var returnValue = this.options[eventName](); // Executing callback
			//this.options[eventName] = null; // Removing callback after execution
			if (!Object.isUndefined(returnValue))
				r = returnValue;
		}
		Event.fire(document, 'Modalbox:' + eventName);
		return r;
	}
};

Object.extend(Modalbox, Modalbox.Methods);

if (Modalbox.overrideAlert) window.alert = Modalbox.alert;
