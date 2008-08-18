/**
 * iBox version 2.17b
 * For more info & download: http://labs.ibegin.com/ibox/
 * Created as a part of the iBegin iBegin Labs Project - http://labs.ibegin.com/
 * For licensing please see readme.html (MIT Open Source License)
*/
var iBox = function()
{
  var _pub = {
    // label for the close link
    close_label: 'Close',

    // AK: added message
    error_message_loading: 'There was an error loading the document.',

    // AK: added message
    loading_message: 'Loading...',

    // padding around the box
    padding: 100,
    
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

    // browser checks
    is_opera: navigator.userAgent.indexOf('Opera/9') != -1,
    is_ie: navigator.userAgent.indexOf("MSIE ") != -1,
    is_ie6: false /*@cc_on || @_jscript_version < 5.7 @*/,
    is_firefox: navigator.appName == "Netscape" && navigator.userAgent.indexOf("Gecko") != -1 && navigator.userAgent.indexOf("Netscape") == -1,
    is_mac: navigator.userAgent.indexOf('Macintosh') != -1,
    
    base_url: '',
    
    /**
     * Updates the base_url variable.
     * @param {String} path Relative or absolute path to this file.
     */
    setPath: function(path)
    {
      _pub.base_url = path;
    },
    
    /**
     * Binds arguments to a callback function
     */
    bind: function(fn)
    {
        var args = [];
        for (var n=1; n<arguments.length; n++) args.push(arguments[n]);
        return function(e) { return fn.apply(this, [e].concat(args)); };
    },

    /**
     * Sets the content of the ibox
     * @param {String} content HTML content
     * @param {Object} params
     */
    html: function(content, params)
    {
      if (content === undefined) return els.content;
      if (cancelled) return;
      _pub.clear();
      els.wrapper.style.display = "block";
      els.wrapper.style.visibility = "hidden";
      els.content.style.height = 'auto';
      
      // MS Run HTML updates through prototype's .update() method so evalScripts works as expected 
      //if (typeof(content) == 'string') els.content.innerHTML = content;
      if (typeof(content) == 'string') els.content.update(content);
      else els.content.appendChild(content);

      var elemSize = _pub.getElementSize(els.content);
      var pageSize = _pub.getPageSize();

      if (params.can_resize === undefined) params.can_resize = true;
      if (params.fade_in === undefined) params.use_fade = true;

      if (params.width) var width = parseInt(params.width);
      else var width = _pub.default_width;

      if (params.height) var height = parseInt(params.height);
      else var height = elemSize.height;

      els.wrapper.style.width = width + 'px';
      els.wrapper.style.height = height + 'px';

      // if we dont do this twice we get a bug on the first display
      if (!params.height)
      {
        var elemSize = _pub.getElementSize(els.content);
        var height = elemSize.height;
      }
      if (params.can_resize) _pub.resizeObjectToScreen(els.content, width, height, params.constrain);
      else
      {
        els.content.style.width = width + 'px';
        els.content.style.height = height + 'px';
      }

      // now we set the wrapper
      var elemSize = _pub.getElementSize(els.content);
      els.wrapper.style.width = elemSize.width + 'px';
      els.wrapper.style.height = elemSize.height + 'px';

      _pub.reposition();
      
      // MS show tags inside of the ibox that were accidentally hidden earlier
      for (var i=0; i<_pub.tags_to_hide.length; i++) showTags(els.wrapper, _pub.tags_to_hide[i]);
      
      els.wrapper.style.visibility = "visible";
      _pub.fadeIn(els.wrapper, 10, params.fade_in ? _pub.fade_in_speed : 0);
    },
    
    /**
     * Empties the content of the iBox (also hides the loading indicator)
     */
    clear: function()
    {
      els.loading.style.display = "none";
      while (els.content.firstChild) els.content.removeChild(els.content.firstChild);
    },
    
    /**
     * Loads text into the ibox
     * @param {String} url
     * @param {String} title
     * @param {Object} params
     */
    show: function(text, title, params)
    {
	    // MS: Add 'cancel' flag to hide
      _pub.hide(false);
      showInit(title, params, function(){
        _pub.html(text, params);
      });
    },
    /**
     * Loads a url into the ibox
     * @param {String} url
     * @param {String} title
     * @param {Object} params
     */
    showURL: function(url, title, params)
    {
      showInit(title, params, function(){
        cancelled = false;
        for (var i=0; i<_pub.plugins.list.length; i++)
        {
          var plugin = _pub.plugins.list[i];
          if (plugin.match(url))
          {
            active_plugin = plugin;
            plugin.render(url, params);
            break;
          }
        }
      });
    },

    /**
     * Hides the iBox
     */
    // MS added "cancel" flag
    hide: function(cancel)
    {
    	// MS Added "locked" support
    	if (_pub.params.locked) {
    		return;
    	}
    
      if (active_plugin)
      {
        // call the plugins unload method
        if (active_plugin.unload) active_plugin.unload();
        active_plugin = null;
      }
      window.onscroll = null;
      _pub.clear();
      // restore elements that were hidden
      // MS pass in document param
      for (var i=0; i<_pub.tags_to_hide.length; i++) showTags(document, _pub.tags_to_hide[i]);

      els.loading.style.display = 'none';
      els.overlay.style.display = 'none';
      els.wrapper.style.display = 'none';
      _pub.fireEvent('hide');
    },

    /**
     * Resizes an object to fit on screen
     * @param {Object} obj
     * @param {Integer} width
     * @param {Integer} height
     * @param {Boolean} constrain
     */
    resizeObjectToScreen: function(obj, width, height, constrain)
    {

      var pagesize = _pub.getPageSize();

      var x = pagesize.width - _pub.padding;
      var y = pagesize.height - _pub.padding;
      
      if (!height) var height = obj.height;
      if (!width) var width = obj.width;
      if (width > x)
      {
        if (constrain) height = height * (x/width);
        width = x;
      }
      if (height > y)
      {
        if (constrain) width = width * (y/height);
        height = y;
      }
      obj.style.width = width + 'px';
      obj.style.height = height + 'px';
    },

    /**
     * Repositions the iBox wrapper (from events)
     */
    reposition: function(e)
    {
      // verify height doesnt overreach browser's viewpane
      _pub.center(els.loading);
      _pub.center(els.wrapper);
      var pageSize = _pub.getPageSize();
      var scrollPos = _pub.getScrollPos();
      
      if (_pub.is_ie6) els.overlay.style.width = document.documentElement.clientWidth + 'px';
      var height = Math.max(document.documentElement.clientHeight, document.body.clientHeight);
      els.overlay.style.height = height + 'px';
      // AK: added
      var width = Math.max(document.documentElement.clientWidth, document.body.clientWidth);
      els.overlay.style.width = width + 'px';
    },

    /**
     * Centers an object
     * @param {Object} obj
     */
    center: function(obj)
    {
      var pageSize = _pub.getPageSize();
      var scrollPos = _pub.getScrollPos();
      var emSize = _pub.getElementSize(obj);
      var x = Math.round((pageSize.width - emSize.width) / 2 + scrollPos.scrollX);
      var y = Math.round((pageSize.height - emSize.height) / 2 + scrollPos.scrollY);
      obj.style.left = x + 'px';
      obj.style.top = y + 'px';
    },
    
    getStyle: function(obj, styleProp)
    {
      if (obj.currentStyle)
        return obj.currentStyle[styleProp];
      else if (window.getComputedStyle)
        return document.defaultView.getComputedStyle(obj,null).getPropertyValue(styleProp);
    },

    /**
     * Gets the scroll positions
     */
    getScrollPos: function()
    {
      var docElem = document.documentElement;
      return {
        scrollX: document.body.scrollLeft || window.pageXOffset || (docElem && docElem.scrollLeft),
        scrollY: document.body.scrollTop || window.pageYOffset || (docElem && docElem.scrollTop)
      };
    },

    /**
     * Gets the page constraints
     */
    getPageSize: function()
    {
      return {
        width: window.innerWidth || (document.documentElement && document.documentElement.clientWidth) || document.body.clientWidth,
        height: window.innerHeight || (document.documentElement && document.documentElement.clientHeight) || document.body.clientHeight
      };
    },

    /**
     * Gets an objects offsets
     * @param {Object} obj
     */
    getElementSize: function(obj)
    {
      return {
        width: obj.offsetWidth || obj.style.pixelWidth,
        height: obj.offsetHeight || obj.style.pixelHeight
      };
    },

    fadeIn: function(obj, level, speed, callback)
    {
      if (level === undefined) var level = 100;
      if (speed === undefined) var speed = 70;
      if (!speed)
      {
        _pub.setOpacity(null, obj, level*10);
        if (callback) callback();
        return;
      }
    
      _pub.setOpacity(null, obj, 0);
      for (var i=0; i<=level; i++)
      {
        setTimeout(_pub.bind(_pub.setOpacity, obj, i*10), speed*i);
      }
      if (callback) setTimeout(callback, speed*(i+1));
    },

    /**
     * Sets the opacity of an element
     * @param {Object} obj
     * @param {Integer} value
     */
    setOpacity: function(e, obj, value)
    {
      obj.style.opacity = value/100;
      obj.style.filter = 'alpha(opacity=' + value + ')';
    },
    
    /**
     * Creates a new XMLHttpRequest object based on browser
     */
    createXMLHttpRequest: function()
    {
      var http;
      if (window.XMLHttpRequest)
      { // Mozilla, Safari,...
        http = new XMLHttpRequest();
        if (http.overrideMimeType)
        {
          // set type accordingly to anticipated content type
          http.overrideMimeType('text/html');
        }
      }
      else if (window.ActiveXObject)
      { // IE
        try {
          http = new ActiveXObject("Msxml2.XMLHTTP");
        } catch (e) {
          try {
            http = new ActiveXObject("Microsoft.XMLHTTP");
          } catch (e) {}
        }
      }
      if (!http)
      {
        alert('Cannot create XMLHTTP instance');
        return false;
      }
      return http;
    },
    
    addEvent: function(obj, evType, fn)
    {
      if (obj.addEventListener)
      {
        obj.addEventListener(evType, fn, false);
        return true;
      }
      else if (obj.attachEvent)
      {
        var r = obj.attachEvent("on"+evType, fn);
        return r;
      }
      else
      {
        return false;
      }
    },
    
    addEventListener: function(name, callback)
    {
      if (!events[name]) events[name] = new Array();
      events[name].push(callback);
    },
    
    fireEvent: function(name)
    {
        if (events[name] && events[name].length)
        {
          for (var i=0; i<events[name].length; i++)
          {
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
    parseQuery: function(query)
    {
       var params = new Object();
       if (!query) return params; 
       var pairs = query.split(/[;&]/);
       var end_token;
       for (var i=0; i<pairs.length; i++)
       {
          var keyval = pairs[i].split('=');
          if (!keyval || keyval.length != 2) continue;
          var key = unescape(keyval[0]);
          var val = unescape(keyval[1]);
          val = val.replace(/\+/g, ' ');
          if (val[0] == '"') var token = '"';
          else if (val[0] == "'") var token = "'";
          else var token = null;
          if (token)
          {
            if (val[val.length-1] != token)
            {
              do
              {
                i += 1;
                val += '&'+pairs[i];
              }
              while ((end_token = pairs[i][pairs[i].length-1]) != token)
            }
            val = val.substr(1, val.length-2);
          }
          params[key] = val;
       }
       return params;
    },
    handleTag: function(e)
    {
      var t = this.getAttribute('rel');
      var params = _pub.parseQuery(t.substr(5,999));
      if (params.target) var url = params.target
      else if (this.target && !params.ignore_target) var url = this.target;
      else var url = this.href;
      var title = this.title;
      if (_pub.inherit_frames && window.parent) window.parent.iBox.showURL(url, title, params);
      else _pub.showURL(url, title, params);
      return false;
    },
    
    plugins: {
      list: new Array(),
      register: function(func, last)
      {
        if (!last)
        {
          _pub.plugins.list = _pub.plugins.list.concat([func],_pub.plugins.list);
        }
        else
        {
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
  var cancelled = false;
  var active_plugin = null;
  
  // events
  var events = {};

  // some containers
  // we store these in memory instead of finding them each time
  var els = {
    wrapper: null,
    footer: null,
    content: null,
    overlay: null,
    loading: null
  };

  /**
   * Creates the iBox container and appends it to an element
   * @param {Object} elem Container to attach to
   * @return {Object} iBox element
   */
  var create = function(elem)
  {
    // TODO: why isnt this using DOM tools
    // a trick on just creating an ibox wrapper then doing an innerHTML on our root ibox element
    var container = document.createElement('div');
    container.id = 'ibox';
    container.style.display = 'block';

    els.overlay = document.createElement('div');
    els.overlay.style.display = 'none';
    els.overlay.id = 'ibox_overlay';
    // MS: Add 'cancel' flag to hide
    els.overlay.onclick = function() { _pub.hide(true) };
    container.appendChild(els.overlay);

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
    container.appendChild(els.loading);

    els.wrapper = document.createElement('div')
    els.wrapper.id = 'ibox_wrapper';
    els.wrapper.style.display = 'none';

    els.content = document.createElement('div');
    els.content.id = 'ibox_content';
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

    container.appendChild(els.wrapper);

    elem.appendChild(container);
    return container;
  };
  
  // MS added "base" param
  var hideTags = function(base, tag)
  {
  	// MS use "base" instead of "document"
    var list = base.getElementsByTagName(tag);
    for (var i=0; i<list.length; i++)
    {
      if (_pub.getStyle(list[i], 'visibility') != 'hidden' && list[i].style.display != 'none')
      {
        list[i].style.visibility = 'hidden';
        list[i].wasHidden = true;
      }
    }
  };
  
  // MS added "base" param
  var showTags = function(base, tag)
  {
  	// MS use "base" instead of "document"
    var list = base.getElementsByTagName(tag);
    for (var i=0; i<list.length; i++)
    {
      if (list[i].wasHidden)
      {
        list[i].style.visibility = 'visible';
        list[i].wasHidden = null;
      }
    }
  };
  
  var showInit = function(title, params, callback)
  {
  	// MS added params to _pub
  	_pub.params = params;
    els.loading.style.display = "block";
    _pub.center(els.loading);
    
    _pub.reposition();
    if (!_pub.is_firefox) var amount = 8;
    else var amount = 10;
    // MS pass in document param
    for (var i=0; i<_pub.tags_to_hide.length; i++) hideTags(document, _pub.tags_to_hide[i]);

    window.onscroll = _pub.reposition;

    // set title here
    els.footer.innerHTML = title || "&nbsp;";

    els.overlay.style.display = "block";
    // AK commented, is already in CSS
    // els.overlay.style.backgroundImage = "url('" + _pub.base_url + "images/bg.png')";
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
    
    _pub.fadeIn(els.overlay, amount, _pub.fade_in_speed, callback);
    _pub.fireEvent('show');
  };
  
  var drawCSS = function()
  {
    // Core CSS (positioning/etc)
    var core_styles = "#ibox {z-index:1000000;} #ibox_overlay {position:absolute;top:0;left:0;right:0;z-index:1000000;} #ibox_loading {position:absolute;z-index:1000001;} #ibox_wrapper {position:absolute;top:0;left:0;z-index:1000001;padding:25px 10px 10px 10px;} #ibox_content {z-index:1000002;overflow:auto;height:100%;position:relative;padding:2px;text-align:left;} #ibox_content object { display:block;} #ibox_content .ibox_image {width:100%;height:100%;margin:0;padding:0;border:0;display:block;} #ibox_footer_wrapper a {float:right;display:block;outline:0;margin:0;padding:0;} #ibox_footer_wrapper {text-align:left;position:absolute;top:5px;right:10px;left:10px;white-space:nowrap;overflow:hidden;}";
    
    // Default style/theme/skin/whatever
    var default_skin = "#ibox_footer_wrapper {font-weight:bold;}#ibox_footer_wrapper a {text-decoration:underline;color:darkblue;text-transform:lowercase;font-weight:normal;font-family:Verdana, Arial, Helvetica, sans-serif;font-size:12px;}#ibox_footer_wrapper {font-size:12px;font-family:Verdana, Arial, Helvetica, sans-serif;}#ibox_wrapper {border:1px solid #ccc;}#ibox_wrapper, #ibox_footer_wrapper a {background-color:#999;}#ibox_content {background-color:#fff;border:1px solid #666;}#ibox_loading {padding:50px; background:#000;color:#fff;font-size:16px;font-weight:bold;}";

    var head = document.getElementsByTagName("head")[0];
    // tricky hack for IE
    var htmDiv = document.createElement('div');

    htmDiv.innerHTML = '<p>x</p><style type="text/css">'+default_skin+'</style>';
    head.insertBefore(htmDiv.childNodes[1], head.firstChild);

    htmDiv.innerHTML = '<p>x</p><style type="text/css">'+core_styles+'</style>';
    head.insertBefore(htmDiv.childNodes[1], head.firstChild);
  }

  var initialize = function()
  {
    // elements here start the look up from the start non <a> tags
    // MS check for an existing ibox
  	var new_ibox = document.getElementById('ibox') == null;
    if (new_ibox) {
	    drawCSS();
    }
    var els = document.getElementsByTagName("a");
    for (var i=0; i<els.length; i++)
    {
      if (els[i].getAttribute(_pub.attribute_name))
      {
        var t = els[i].getAttribute(_pub.attribute_name);
        if ((t.indexOf("ibox") != -1) || t.toLowerCase() == "ibox")
        { // check if this element is an iBox element
          els[i].onclick = _pub.handleTag;
        }
      }
    }
    // MS check for an existing ibox
    if (new_ibox) {
	    create(document.body);
	    _pub.http = _pub.createXMLHttpRequest();
	  }
  };

  //AK : keypress didn't work for some reason
  // MS: Add 'cancel' flag to hide
  _pub.addEvent(window, 'keyup', function(e){if (e.keyCode == (window.event ? 27 : e.DOM_VK_ESCAPE)) { iBox.hide(true); }});
  _pub.addEvent(window, 'resize', _pub.reposition);
  _pub.addEvent(window, 'load', initialize);

  // DEFAULT PLUGINS

  /**
   * Handles embedded containers in the page based on url of #container.
   * This _ONLY_ works with hidden containers.
   */
  var iBoxPlugin_Container = function()
  {
    var was_error = false;
    var original_wrapper = null;
    return {
      /**
       * Matches the url and returns true if it fits this plugin.
       */
      match: function(url)
      {
        return url.indexOf('#') != -1;
      },
      /**
       * Called when this plugin is unloaded.
       */
      unload: function()
      {
        if (was_error) return;
        var elemSrc = _pub.html().firstChild;
        elemSrc.style.display = 'none';
        original_wrapper.appendChild(elemSrc);
      },
      /**
       * Handles the output
       * @param {iBox} ibox
       * @param {String} url
       * @return {iBoxContent} an instance or subclass of iBoxContent
       */
      render: function(url, params)
      {
        was_error = false;
        var elemSrcId = url.substr(url.indexOf("#") + 1);
        var elemSrc = document.getElementById(elemSrcId);
        // If the element doesnt exist, break the switch
        if (!elemSrc)
        {
          was_error = true;
          // AK: Changed to _pub.error_message_loading
          _pub.html(document.createTextNode(_pub.error_message_loading), params);
        }
        else
        {
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
  var iBoxPlugin_Image = function()
  {
    // Image types (for auto detection of image display)
    var image_types = /\.jpg|\.jpeg|\.png|\.gif/gi;

    return {
      match: function(url)
      {
        return url.match(image_types);
      },

      render: function(url, params)
      {  
        var img = document.createElement('img');
		    // MS: Add 'cancel' flag to hide
        img.onclick = function() { _pub.hide(true) };
        img.className = 'ibox_image'
        img.style.cursor = 'pointer';
        img.onload = function()
        {
          _pub.html(img, {height: img.height, width: img.width, constrain: true})
        }
        img.onerror = function()
        {
        	// AK: Changed to _pub.error_message_loading
          _pub.html(document.createTextNode(_pub.error_message_loading), params);
        }
        img.src = url;
      }
    }
  }();
  _pub.plugins.register(iBoxPlugin_Image);

  var iBoxPlugin_YouTube = function()
  {
    var youtube_url = /(?:http:\/\/)?(?:www\d*\.)?(youtube\.(?:[a-z]+))\/(?:v\/|(?:watch(?:\.php)?)?\?(?:.+&)?v=)([^&]+).*/;
    return {
      match: function(url)
      {
        return url.match(youtube_url);
      },

      render: function(url, params)
      {
        var _match = url.match(youtube_url);
        var domain = _match[1];
        var id = _match[2];
        params.width = 425;
        params.height = 355;
        params.can_resize = false;
        var html = '<div><object width="425" height="355"><param name="movie" value="http://www.' + domain + '/v/' + id + '"/><param name="wmode" value="transparent"/><embed src="http://www.' + domain + '/v/' + id + '" type="application/x-shockwave-flash" wmode="transparent" width="425" height="355"></embed></object></div>';
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
      	var server = document.location.href.replace(new RegExp("(http:\/\/[^\/]+)\/.*"), "$1");
        return url.indexOf(server) == -1;
      },

      render: function(url, params)
      {
       var html = "<iframe style='width: 100%; height: 100%; margin: 0; padding: 0; border: 0' src='" + url + "'></iframe>";
        _pub.html(html, params);
      }
    }
  }();
  _pub.plugins.register(iBoxPlugin_External);

  var iBoxPlugin_Document = function()
  {
    return {
      match: function(url)
      {
        return true;
      },

      render: function(url, params)
      {
        _pub.http.open('get', url, true);

        _pub.http.onreadystatechange = function()
        {
          if (_pub.http.readyState == 4)
          {
            // XXX: why does status return 0?
            if (_pub.http.status == 200 || _pub.http.status == 0)
            {
              _pub.html(_pub.http.responseText, params);
            }
            else
            {
            	// AK: Changed to _pub.error_message_loading
              _pub.html(document.createTextNode(_pub.error_message_loading), params);
            }
          }
        }
        _pub.http.setRequestHeader("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        // MS add in the XMLHttpRequest header
        _pub.http.setRequestHeader("x-requested-with", "XMLHttpRequest");
        _pub.http.send(null);
      }
    };
  }();
  _pub.plugins.register(iBoxPlugin_Document);

  return _pub;
}();