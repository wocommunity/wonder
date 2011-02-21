/**
 * iBox 2.2 (Build 1612)
 * For more info & download: http://www.ibegin.com/labs/ibox/
 * Created as a part of the iBegin Labs Project - http://www.ibegin.com/labs/
 * For licensing please see readme.html (MIT Open Source License)
*/
var iBox = function() {
	var _pub = {
		// label for the close link
		close_label: 'Close',

		// AK: added message
		error_message_loading: 'There was an error loading the document.',

		// AK: added message
		loading_message: 'Loading...',
    
		// show iframed content in the parent window
		// this *does not* work with #containers
		inherit_frames: false,

		// how fast to fade in the overlay/ibox (this is each step in ms)
		// MS Fade looks cool :)
		fade_in_speed: 17,

		// our attribute identifier for our iBox elements
		attribute_name: 'rel',
		
		// tags to hide when we show our box
		tags_to_hide: ['select', 'embed', 'object'],

		// default width of the box (when displaying html only)
		// height is calculated automatically
		default_width: 450,

		// public version number
		version_number: '2.2',
		// internal build number
		build_number: '1612',

		// browser checks		
		is_opera: navigator.userAgent.indexOf('Opera/9') != -1,
		is_ie: navigator.userAgent.indexOf("MSIE ") != -1,
		is_ie6: false /*@cc_on || @_jscript_version < 5.7 @*/,
		is_firefox: navigator.appName == "Netscape" && navigator.userAgent.indexOf("Gecko") != -1 && navigator.userAgent.indexOf("Netscape") == -1,
		is_mac: navigator.userAgent.indexOf('Macintosh') != -1,

		// url for including images/external files
		base_url: '',
		
		// MS added params to _pub
		params: {},
		
		/**
		 * Updates the base_url variable.
		 * @param {String} path Relative or absolute path to this file.
		 */
		setPath: function(path) {
			_pub.base_url = path;
		},
		
		/**
		 * Checks a container for specified tags containing rel="ibox"
		 * @param {Object} container
		 * @param {String} tag_name
		 */
		checkTags: function(container, tag_name) {
		// MS: added setTimeout to checkTags at the end of the render pass
		setTimeout(function() {
			if (!container) var container = document.body;
			if (!tag_name) var tag_name = 'a';
			var els = container.getElementsByTagName(tag_name);
			for (var i=0; i<els.length; i++) {
				if (els[i].getAttribute(_pub.attribute_name)) {
					var t = els[i].getAttribute(_pub.attribute_name);
					if ((t.indexOf("ibox") != -1) || t.toLowerCase() == "ibox") { // check if this element is an iBox element
						els[i].onclick = _pub.handleTag;
					}
				}
			}
			}, 0);
		},
		
		/**
		 * Binds arguments to a callback function
		 */
		bind: function(fn) {
				var args = [];
				for (var n=1; n<arguments.length; n++) args.push(arguments[n]);
				return function(e) { return fn.apply(this, [e].concat(args)); };
		},

		/**
		 * Sets the content of the ibox
		 * @param {String} content HTML content
		 * @param {Object} params
		 */
		// MS: added resizeOnly param
		html: function(content, params, resizeOnly) {
		  // MS: added resizeOnly check
		  if (resizeOnly === undefined) var resizeOnly = false;
			if (content === undefined) return els.content;
			if (params === undefined) var params = {};
			if (!active.is_loaded) return;

			// MS: added resizeOnly check
			if (!resizeOnly) {
				_pub.clear();
				_pub.updateObject(els.wrapper.style, {display: 'block', visibility: 'hidden', left: 0, top: 0, height: '', width: ''});
				// MS Run HTML updates through prototype's .update() method so evalScripts works as expected 
				//if (typeof(content) == 'string') els.content.innerHTML = content;
				if (typeof(content) == 'string') $(els.content).update(content);
				else els.content.appendChild(content);
			}
			// MS: added else block
			else {
				_pub.updateObject(els.wrapper.style, {display: 'block', visibility: 'hidden', left: 0, top: 0, height: '', width: ''});
			}
			
			var pagesize = _pub.getPageSize();

			if (params.can_resize === undefined) params.can_resize = true;
			if (params.fade_in === undefined) params.use_fade = true;

			if (params.fullscreen) {
				params.width = '100%';
				params.height = '100%';
			}
			
			// reset offsets
			offset.container = [els.wrapper.offsetLeft*2, els.wrapper.offsetTop*2];
			offset.wrapper = [els.wrapper.offsetWidth-els.content.offsetWidth, els.wrapper.offsetHeight-els.content.offsetHeight];

			// TODO: remove the +4 when issue is solved with calculations
			offset.wrapper[1] += 4;

			if (params.width) var width = params.width;
			else var width = _pub.default_width;

			if (params.height) var height = params.height;
			else {
				els.content.style.height = '100%';
				// MS: changed to 21 -- a scrollbar appeared otherwise
				var height = els.content.offsetHeight + 21;
				els.content.style.height = '';
			}
			active.dimensions = [width, height];
			active.params = params;
			_pub.reposition();
			
			// XXX: Fix for inline containers which had elements that were hidden
			for (var i=0; i<_pub.tags_to_hide.length; i++) {
				showTags(_pub.tags_to_hide[i], els.content);
			}

			els.wrapper.style.visibility = 'visible';
		},
		
		/**
		 * Empties the content of the iBox (also hides the loading indicator)
		 */
		clear: function() {
			els.loading.style.display = "none";
			while (els.content.firstChild) els.content.removeChild(els.content.firstChild);
		},
		
		/**
		 * Loads text into the ibox
		 * @param {String} text
		 * @param {String} title
		 * @param {Object} params
		 */
		show: function(text, title, params) {
			showInit(title, params, function() {
				_pub.html(text, active.params);
			});
		},
		/**
		 * Loads a url into the ibox
		 * @param {String} url
		 * @param {String} title
		 * @param {Object} params
		 */
		showURL: function(url, title, params) {
			showInit(title, params, function() {
				for (var i=0; i<_pub.plugins.list.length; i++) {
					var plugin = _pub.plugins.list[i];
					if (plugin.match(url)) {
						active.plugin = plugin;
						plugin.render(url, active.params);
						break;
					}
				}
			});
		},

		/**
		 * Hides the iBox
		 */
		// MS added "cancel" flag
		hide: function(cancel) {
			// MS Added "locked" support
			if (cancel && _pub.params.locked) {
				return;
			}
			if (active.plugin) {
				// call the plugins unload method
				if (active.plugin.unload) active.plugin.unload();
			}
			active = {}
			_pub.clear();
			// restore elements that were hidden
			for (var i=0; i<_pub.tags_to_hide.length; i++) showTags(_pub.tags_to_hide[i]);

			els.loading.style.display = 'none';
			els.wrapper.style.display = 'none';
			_pub.fade(els.overlay, _pub.getOpacity(null, els.overlay), 0, _pub.fade_out_speed, function() { els.overlay.style.display = 'none';});
			_pub.fireEvent('hide');
		},

		// MS: Added contentChanged function
		contentChanged: function() {
			_pub.html(els.content.innerHTML, active.params, true);
		},
		
		/**
		 * Repositions the iBox wrapper based on the params set originally.
		 */
		reposition: function() {
			if (!active.is_loaded) return;

			// center loading box
			if (els.loading.style.display != 'none') _pub.center(els.loading);
			
			// update ibox width/height/position
			if (active.dimensions) {
				var pagesize = _pub.getPageSize();

				var width = active.dimensions[0];
				var height = active.dimensions[1];
				
				if (height.toString().indexOf('%') != -1) {
					els.wrapper.style.height = (Math.max(document.documentElement.clientHeight, document.body.clientHeight, pagesize.height) - offset.container[0])*(parseInt(height)/100) + 'px';
				}
				else if (height) {
					els.content.style.height = height + 'px';
					// TODO: if we dont set wrapper height, it doesnt restrict the height and the box is fine
					// so offset.wrapper[1] must not be correct
					els.wrapper.style.height = els.content.offsetHeight + offset.wrapper[1] + 'px';
				}
				else {
					els.wrapper.style.height = els.content.offsetHeight + offset.wrapper[1] + 'px';
				}
				var container_offset = (els.content.offsetHeight - els.content.firstChild.offsetHeight);
				if (width.toString().indexOf('%') != -1) {
					els.wrapper.style.width = (Math.max(document.documentElement.clientWidth, document.body.clientWidth, pagesize.width) - offset.container[1])*(parseInt(width)/100) + 'px';
					var container_offset = 0;
				}
				else {
					els.content.style.width = width + 'px';
					els.wrapper.style.width = els.content.offsetWidth + offset.wrapper[0] + 'px';
				}

				_pub.updateObject(els.content.style, {width: '', height: ''});

				var width = parseInt(els.wrapper.style.width);
				var height = parseInt(els.wrapper.style.height);

				// if we can resize this, make sure it fits in our page bounds
				if (active.params.can_resize) {
					var x = pagesize.width;
					var y = pagesize.height;
					
					x -= offset.container[0];
					y -= offset.container[1];
					if (width > x) {
						if (active.params.constrain) height = height * (x/width);
						width = x;
					}
					if (height > y) {
						if (active.params.constrain) width = width * (y/height);
						height = y;
					}
					_pub.updateObject(els.wrapper.style, {width: width + 'px', height: height + 'px'});
				}

				//els.content.style.width = width - offset.wrapper[0] + 'px';
				// TODO: this isn't adjusting to the right height for containers that are smaller than the page height
				// resize the wrappers height based on the content boxes height
				// this needs to be height - ibox_content[margin+padding+border]
				els.content.style.height = height - offset.wrapper[1] + 'px';
				if (active.dimensions != ['100%', '100%']) _pub.center(els.wrapper);
			}
			
			// fix overlay width/height (cant use css fixed on ie6 or fx or any
			// browser really due to issues)
			els.overlay.style.height = Math.max(document.body.clientHeight, document.documentElement.clientHeight) + 'px';
			// AK: added
			var width = Math.max(document.documentElement.clientWidth, document.body.clientWidth);
			els.overlay.style.width = width + 'px';
		},

		updateObject: function(obj, params) {
			for (var i in params) obj[i] = params[i];
		},

		/**
		 * Centers an object
		 * @param {Object} obj
		 */
		center: function(obj) {
			var pageSize = _pub.getPageSize();
			var scrollPos = _pub.getScrollPos();
			var emSize = _pub.getElementSize(obj);
			var x = Math.round((pageSize.width - emSize.width) / 2 + scrollPos.scrollX);
			var y = Math.round((pageSize.height - emSize.height) / 2 + scrollPos.scrollY);
			if (obj.offsetLeft) x -= obj.offsetLeft;
			if (obj.offsetTop) y -= obj.offsetTop;
			if (obj.style.left) x += parseInt(obj.style.left);
			if (obj.style.top) y += parseInt(obj.style.top);
			// this nearly centers it due to scrollbars
			x -= 10;
			_pub.updateObject(obj.style, {top: y + 'px', left: x + 'px'});
		},
		
		getStyle: function(obj, styleProp) {
			if (obj.currentStyle)
				return obj.currentStyle[styleProp];
			else if (window.getComputedStyle)
				return document.defaultView.getComputedStyle(obj,null).getPropertyValue(styleProp);
		},

		/**
		 * Gets the scroll positions
		 */
		getScrollPos: function() {
			var docElem = document.documentElement;
			return {
				scrollX: document.body.scrollLeft || window.pageXOffset || (docElem && docElem.scrollLeft),
				scrollY: document.body.scrollTop || window.pageYOffset || (docElem && docElem.scrollTop)
			};
		},

		/**
		 * Gets the page constraints
		 */
		getPageSize: function() {
			return {
				width: window.innerWidth || (document.documentElement && document.documentElement.clientWidth) || document.body.clientWidth,
				height: window.innerHeight || (document.documentElement && document.documentElement.clientHeight) || document.body.clientHeight
			};
		},

		/**
		 * Gets an objects offsets
		 * @param {Object} obj
		 */
		getElementSize: function(obj) {
			return {
				width: obj.offsetWidth || obj.style.pixelWidth,
				height: obj.offsetHeight || obj.style.pixelHeight
			};
		},
		
		fade: function(obj, start, end, speed, callback) {
			if (start === undefined || !(start >= 0) || !(start <= 100)) var start = 0;
			if (end === undefined || !(end >= 0) || !(end <= 100)) var end = 100;
			if (speed === undefined) var speed = 0;

			if (obj.fader) clearInterval(obj.fader);

			if (!speed) {
				_pub.setOpacity(null, obj, end);
				if (callback) callback();
			}
			
			var opacity_difference = end - start; 
			var time_total = speed; // time is speed (jQuery compat)
			var step_size = 25; // step size in ms
			var steps = time_total / step_size; // total number of steps
			var increment = Math.ceil(opacity_difference / steps); // how much to incr per step
			
			obj.fader = setInterval(_pub.bind(function(e, obj, increment, end, callback) {
				var opacity = _pub.getOpacity(e, obj) + increment;
				_pub.setOpacity(e, obj, opacity);
				if ((increment < 0 && opacity <= end) || (increment > 0 && opacity >= end)) {
					_pub.setOpacity(e, obj, end);
					clearInterval(obj.fader);
					if (callback) callback();
				}
			}, obj, increment, end, callback), step_size);
		},

		/**
		 * Sets the opacity of an element
		 * @param {Object} obj
		 * @param {Integer} value
		 */
		setOpacity: function(e, obj, value) {
			value = Math.round(value);
			obj.style.opacity = value/100;
			obj.style.filter = 'alpha(opacity=' + value + ')';
		},
		
		/**
		 * Gets the opacity of an element
		 * @param {Object} obj
		 * @return {Integer} value
		 */
		getOpacity: function(e, obj) {
			return _pub.getStyle(obj, 'opacity')*100;
		},
		
		/**
		 * Creates a new XMLHttpRequest object based on browser
		 */
		createXMLHttpRequest: function() {
			var http;
			if (window.XMLHttpRequest) { // Mozilla, Safari,...
				http = new XMLHttpRequest();
				if (http.overrideMimeType) {
					// set type accordingly to anticipated content type
					http.overrideMimeType('text/html');
				}
			}
			else if (window.ActiveXObject) { // IE
				try {
					http = new ActiveXObject("Msxml2.XMLHTTP");
				} catch (e) {
					try {
						http = new ActiveXObject("Microsoft.XMLHTTP");
					} catch (e) {}
				}
			}
			if (!http) {
				alert('Cannot create XMLHTTP instance');
				return false;
			}
			return http;
		},
		
		addEvent: function(obj, evType, fn) {
			if (obj.addEventListener) {
				obj.addEventListener(evType, fn, false);
				return true;
			}
			else if (obj.attachEvent) {
				var r = obj.attachEvent("on"+evType, fn);
				return r;
			}
			else {
				return false;
			}
		},
		
		addEventListener: function(name, callback) {
			if (!events[name]) events[name] = new Array();
			events[name].push(callback);
		},

		// MS: Added removeEventListener
		removeEventListener: function(name, callback) {
			if (events[name]) {
				events[name] = events[name].without(callback);
			}
		},
    
		
		/**
		 * Causes all event listeners attached to `name` event to
		 * execute.
		 * @param {String} name Event name
		 */
		fireEvent: function(name) {
				if (events[name] && events[name].length) {
					for (var i=0; i<events[name].length; i++) {
						var args = [];
						for (var n=1; n<arguments.length; n++) args.push(arguments[n]);
						// Events returning false stop propagation
						if (events[name][i](args) === false) break;
					}
				}
		},
		
		/**
		 * Parses the arguments in the rel attribute
		 * @param {String} query
		 */
		parseQuery: function(query) {
			 var params = new Object();
			 if (!query) return params; 
			 var pairs = query.split(/[;&]/);
			 var end_token;
			 for (var i=0; i<pairs.length; i++) {
					var keyval = pairs[i].split('=');
					if (!keyval || keyval.length != 2) continue;
					var key = decodeURIComponent(keyval[0]);
					var val = decodeURIComponent(keyval[1]);
					val = val.replace(/\+/g, ' ');
					if (val[0] == '"') var token = '"';
					else if (val[0] == "'") var token = "'";
					else var token = null;
					if (token) {
						if (val[val.length-1] != token) {
							do {
								i += 1;
								val += '&'+pairs[i];
							}
							while ((end_token = pairs[i][pairs[i].length-1]) != token)
						}
						val = val.substr(1, val.length-2);
					}
					if (val == 'true') val = true;
					else if (val == 'false') val = false;
					else if (val == 'null') val = null;
					params[key] = val;
			 }
			 return params;
		},
		/**
		 * Handles the onclick event for iBox anchors.
		 * @param {Event} e
		 */
		handleTag: function(e) {
			var t = this.getAttribute('rel');
			var params = _pub.parseQuery(t.substr(5,999));
			if (params.target) var url = params.target;
			else if (this.target && !params.ignore_target) var url = this.target;
			else var url = this.href;
			var title = this.title;
			if (_pub.inherit_frames && window.parent) window.parent.iBox.showURL(url, title, params);
			else _pub.showURL(url, title, params);
			return false;
		},
		
		plugins: {
			list: new Array(),
			register: function(func, last) {
				if (last === undefined) var last = false;
				if (!last) {
					_pub.plugins.list = [func].concat(_pub.plugins.list);
				}
				else {
					_pub.plugins.list.push(func);
				}
			}
		},
    
		// MS Added public method to allow for re-initialization
		init: function() {
			initialize();
		}
	};
	
	// private methods and variables
	var active = {};
	
	// events
	var events = {};

	// some containers
	// we store these in memory instead of finding them each time
	var els = {};
	
	var offset = {};
	
	/**
	 * Creates the iBox container and appends it to an element
	 * @param {HTMLObject} elem Container to attach to
	 * @return {HTMLObject} iBox element
	 */
	var create = function(elem) {
		pagesize = _pub.getPageSize();
		
		// TODO: why isnt this using DOM tools
		// a trick on just creating an ibox wrapper then doing an innerHTML on our root ibox element
		els.container = document.createElement('div');
		els.container.id = 'ibox';

		els.overlay = document.createElement('div');
		els.overlay.style.display = 'none';
		_pub.setOpacity(null, els.overlay, 0);
		// firefox mac has issues with opacity and flash
		if (!_pub.is_firefox) els.overlay.style.background = '#000000';
		else els.overlay.style.backgroundImage = "url('" + _pub.base_url + "images/bg.png')";
		els.overlay.id = 'ibox_overlay';
		params = {position: 'absolute', top: 0, left: 0, width: '100%'};
		_pub.updateObject(els.overlay.style, params);
		// MS: Add 'cancel' flag to hide
		els.overlay.onclick = function() { _pub.hide(true) };
		els.container.appendChild(els.overlay);

		els.loading = document.createElement('div');
		els.loading.id = 'ibox_loading';
		// AK: Use the loading message from _pub
		els.loading.innerHTML = _pub.loading_message;
		els.loading.style.display = 'none';
		els.loading.onclick = function() {
			// MS: Add 'cancel' flag to hide
			_pub.hide(true);
			cancelled = true;
		}
		els.container.appendChild(els.loading);

		els.wrapper = document.createElement('div')
		els.wrapper.id = 'ibox_wrapper';
		_pub.updateObject(els.wrapper.style, {position: 'absolute', top: 0, left: 0, display: 'none'});

		els.content = document.createElement('div');
		els.content.id = 'ibox_content';
		_pub.updateObject(els.content.style, {overflow: 'auto'})
		els.wrapper.appendChild(els.content);
	
		var child = document.createElement('div');
		child.id = 'ibox_footer_wrapper';
	
		var child2 = document.createElement('a');
		child2.innerHTML = _pub.close_label;
		child2.href = 'javascript:void(0)';
		//AK: added id
		child2.id = 'ibox_close_link';
		// MS: Add 'cancel' flag to hide
		child2.onclick = function() { _pub.hide(false) };
		child.appendChild(child2);
	
		els.footer = document.createElement('div');
		els.footer.id = 'ibox_footer';
		els.footer.innerHTML = '&nbsp;';
		child.appendChild(els.footer);
		els.wrapper.appendChild(child);

		els.container.appendChild(els.wrapper);

		elem.appendChild(els.container);
				
		_pub.updateObject(els.wrapper.style, {right: '', bottom: ''});
		
		return els.container;
	};
	
	/**
	 * Hides tags within the container
	 * @param {String} tag The name of the tag (e.g. 'a')
	 * @param {HTMLObject} container The container to restore tags within (defaults to document)
	 */
	var hideTags = function(tag, container) {
		if (container === undefined) var container = document.body;
		var list = container.getElementsByTagName(tag);
		for (var i=0; i<list.length; i++) {
			if (_pub.getStyle(list[i], 'visibility') != 'hidden' && list[i].style.display != 'none') {
				list[i].style.visibility = 'hidden';
				list[i].wasHidden = true;
			}
		}
	};
	
	/**
	 * Shows all previously hidden tags in a container.
	 * @param {String} tag The name of the tag (e.g. 'a')
	 * @param {HTMLObject} container The container to restore tags within (defaults to document)
	 */
	var showTags = function(tag, container) {
		if (container === undefined) var container = document.body;
		var list = container.getElementsByTagName(tag);
		for (var i=0; i<list.length; i++) {
			if (list[i].wasHidden) {
				list[i].style.visibility = 'visible';
				list[i].wasHidden = null;
			}
		}
	};
	
	var showInit = function(title, params, callback) {
		if (!_initialized) initialize();
		if (params === undefined) var params = {};
		if (active.plugin) _pub.hide();

		active.is_loaded = true;
		active.params = params;
		
		// MS added params to _pub
		_pub.params = params;
		els.loading.style.display = "block";
		
		_pub.center(els.loading);
		_pub.reposition();

		// hide tags
		for (var i=0; i<_pub.tags_to_hide.length; i++) {
			hideTags(_pub.tags_to_hide[i]);
		}

		// set title here
		els.footer.innerHTML = title || "&nbsp;";

		// setup background
		els.overlay.style.display = "block";
		
		// AK added
		// alert(document.getElementById('ibox_footer_wrapper').firstChild);
		params.closeLabel = params.closeLabel ? params.closeLabel : _pub.close_label;
		document.getElementById('ibox_footer_wrapper').firstChild.innerHTML = params.closeLabel;
		// MS added locked support
		if (params.locked) {
			document.getElementById('ibox_wrapper').className = 'locked';
		}
		else {
			document.getElementById('ibox_wrapper').className = '';
		}
		
		if (!_pub.is_firefox) var amount = 70;
		else var amount = 100;
		_pub.fade(els.overlay, _pub.getOpacity(null, els.overlay), amount, _pub.fade_in_speed, callback);
		
		_pub.fireEvent('show');
	};
	
	var drawCSS = function() {
		// Core CSS (positioning/etc)
		var core_styles = "#ibox {z-index:1000;text-align:left;} #ibox_overlay {z-index:1000;} #ibox_loading {position:absolute;z-index:1001;} #ibox_wrapper {margin:30px;position:absolute;top:0;left:0;z-index:1001;} #ibox_content {z-index:1002;margin:27px 5px 5px 5px;padding:2px;} #ibox_content object {display:block;} #ibox_content .ibox_image {width:100%;height:100%;margin:0;padding:0;border:0;display:block;} #ibox_footer_wrapper a {float:right;display:block;outline:0;margin:0;padding:0;} #ibox_footer_wrapper {text-align:left;position:absolute;top:5px;right:5px;left:5px;white-space:nowrap;overflow:hidden;}";
		
		// Default style/theme/skin/whatever
		var default_skin = "#ibox_footer_wrapper {font-weight:bold;height:20px;line-height:20px;} #ibox_footer_wrapper a {text-decoration:none;background:#888;border:1px solid #666;line-height:16px;padding:0 5px;color:#333;font-weight:bold;font-family:Verdana, Arial, Helvetica, sans-serif;font-size:10px;} #ibox_footer_wrapper a:hover {background-color:#bbb;color:#111;} #ibox_footer_wrapper {font-size:12px;font-family:Verdana, Arial, Helvetica, sans-serif;color:#111;} #ibox_wrapper {border:1px solid #ccc;} #ibox_wrapper {background-color:#999;}#ibox_content {background-color:#eee;border:1px solid #666;} #ibox_loading {padding:50px; background:#000;color:#fff;font-size:16px;font-weight:bold;}";

		var head = document.getElementsByTagName("head")[0];

		// tricky hack for IE
		// because IE doesn't like when you insert stuff the proper way
		// and we cant use relative paths to include this as an external
		// stylesheet
		var htmDiv = document.createElement('div');

		htmDiv.innerHTML = '<p>x</p><style type="text/css">'+default_skin+'</style>';
		head.insertBefore(htmDiv.childNodes[1], head.firstChild);

		htmDiv.innerHTML = '<p>x</p><style type="text/css">'+core_styles+'</style>';
		head.insertBefore(htmDiv.childNodes[1], head.firstChild);
	};

	var _initialized = false;
	var initialize = function() {
		// make sure we haven't already done this
		if (_initialized) {
			// MS: added checkTags call here on a second init, so after an ajax update we
			// reprocess ibox links 
			_pub.checkTags(document.body, 'a');
			return;
		}
		_initialized = true;
		// elements here start the look up from the start non <a> tags
		// MS check for an existing ibox
		var new_ibox = document.getElementById('ibox') == null;
		if (new_ibox) {
			drawCSS();
		}
		var els = document.getElementsByTagName('script');
		var src;
		for (var i=0, el=null; (el = els[i]); i++) {
			if (!(src = el.getAttribute('src'))) continue;
			src = src.split('?')[0];
			if (src.substr(src.length-8) == '/ibox.js') {
				_pub.setPath(src.substr(0, src.length-7));
				break;
			}
		}
		// MS check for an existing ibox
		if (new_ibox) {
			create(document.body);
			_pub.checkTags(document.body, 'a');
			_pub.http = _pub.createXMLHttpRequest();
			_pub.fireEvent('load');
		}
	};
	
	//AK : keypress didn't work for some reason
	// MS: Add 'cancel' flag to hide
	_pub.addEvent(window, 'keyup', function(e){ if (e.keyCode == (window.event ? 27 : e.DOM_VK_ESCAPE)) { iBox.hide(true); }});
	_pub.addEvent(window, 'resize', _pub.reposition);
	_pub.addEvent(window, 'load', initialize);
	_pub.addEvent(window, 'scroll', _pub.reposition);

	// DEFAULT PLUGINS

	/**
	 * Handles embedded containers in the page based on url of #container.
	 * This _ONLY_ works with hidden containers.
	 */
	var iBoxPlugin_Container = function() {
		var was_error = false;
		var original_wrapper = null;
		return {
			/**
			 * Matches the url and returns true if it fits this plugin.
			 */
			match: function(url) {
				return url.indexOf('#') != -1;
			},
			/**
			 * Called when this plugin is unloaded.
			 */
			unload: function() {
				if (was_error) return;
				var elemSrc = _pub.html().firstChild;
				if (elemSrc) {
					elemSrc.style.display = 'none';
					original_wrapper.appendChild(elemSrc);
				}
			},
			/**
			 * Handles the output
			 * @param {iBox} ibox
			 * @param {String} url
			 * @return {iBoxContent} an instance or subclass of iBoxContent
			 */
			render: function(url, params) {
				was_error = false;
				var elemSrcId = url.substr(url.indexOf("#") + 1);
				var elemSrc = document.getElementById(elemSrcId);
				// If the element doesnt exist, break the switch
				if (!elemSrc) {
					was_error = true;
					// AK: Changed to _pub.error_message_loading
					_pub.html(document.createTextNode(_pub.error_message_loading), params);
				}
				else {
					original_wrapper = elemSrc.parentNode;
					elemSrc.style.display = 'block';
					_pub.html(elemSrc, params);
				}
			}
		}
	}();
	_pub.plugins.register(iBoxPlugin_Container, true);

	/**
	 * Handles images
	 */
	var iBoxPlugin_Image = function() {
		// Image types (for auto detection of image display)
		var image_types = /\.jpg|\.jpeg|\.png|\.gif/gi;

		return {
			match: function(url) {
				return url.match(image_types);
			},

			render: function(url, params) {	
				var img = document.createElement('img');
				// MS: Add 'cancel' flag to hide
				img.onclick = function() { _pub.hide(true) };
				img.className = 'ibox_image'
				img.style.cursor = 'pointer';
				img.onload = function() {
					_pub.html(img, {width: this.width, height: this.height, constrain: true})
				}
				img.onerror = function() {
					// AK: Changed to _pub.error_message_loading
					_pub.html(document.createTextNode(_pub.error_message_loading), params);
				}
				img.src = url;
			}
		}
	}();
	_pub.plugins.register(iBoxPlugin_Image);

	var iBoxPlugin_YouTube = function() {
		var youtube_url = /(?:http:\/\/)?(?:www\d*\.)?(youtube\.(?:[a-z]+))\/(?:v\/|(?:watch(?:\.php)?)?\?(?:.+&)?v=)([^&]+).*/;
		return {
			match: function(url) {
				return url.match(youtube_url);
			},

			render: function(url, params) {
				var _match = url.match(youtube_url);
				var domain = _match[1];
				var id = _match[2];
				params.width = 425;
				params.height = 355;
				params.constrain = true;
				var html = '<span><object width="100%" height="100%" style="overflow: hidden; display: block;"><param name="movie" value="http://www.' + domain + '/v/' + id + '"/><param name="wmode" value="transparent"/><embed src="http://www.' + domain + '/v/' + id + '" type="application/x-shockwave-flash" wmode="transparent" width="100%" height="100%"></embed></object></span>';
				_pub.html(html, params);
			}
		}
	}();
	_pub.plugins.register(iBoxPlugin_YouTube);

	//AK added support for iframes/external urls
	var iBoxPlugin_External = function()
	{
		return {
			match: function(url)
			{
				// MS fix for https support
				var server = document.location.href.replace(new RegExp("(https?:\/\/[^\/]+)\/.*"), "$1");
				return url.indexOf(server) == -1;
			},

			render: function(url, params)
			{
				var html = "<iframe style='width: 100%; height: 100%; margin: 0; padding: 0; border: 0' src='" + url + "'></iframe>";
				_pub.html(html, params);
			}
		}
	}();
	// RB schould be placed at the end of the plugin queue.  Otherwise it brakes the YouTube plugin.
	_pub.plugins.register(iBoxPlugin_External, true);

	var iBoxPlugin_Document = function() {
		return {
			match: function(url) {
				return true;
			},

			render: function(url, params) {
				_pub.http.open('get', url, true);

				_pub.http.onreadystatechange = function() {
					if (_pub.http.readyState == 4) {
						// XXX: why does status return 0?
						if (_pub.http.status == 200 || _pub.http.status == 0) {
							_pub.html(_pub.http.responseText, params);
						}
						else {
							// AK: Changed to _pub.error_message_loading
							_pub.html(document.createTextNode(_pub.error_message_loading), params);
						}
					}
				}
				_pub.http.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
				try {
					// MS add in the XMLHttpRequest header
					_pub.http.setRequestHeader("x-requested-with", "XMLHttpRequest");
					_pub.http.send(null);
				}
				catch (ex) {
					_pub.html(document.createTextNode('There was an error loading the document.'), params);
				}
			}
		};
	}();
	_pub.plugins.register(iBoxPlugin_Document, true);

	return _pub;
}();