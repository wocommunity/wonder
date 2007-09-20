Object.extend(String.prototype, {
	addQueryParameters: function(additionalParameters) {
		if (additionalParameters) {
			return this + (this.match(/\?/) ? '&' : '?') + additionalParameters;
		}
		else {
			return this;
		}
	}
});

Object.extend(Event, {
	keyValue: function(event) {
		var keynum;
		if (window.event) {
			keynum = event.keyCode;
		}
		else if (event.which) {
			keynum = event.which;
		}
		else {
			keynum = event.keyCode;
		}
		return keynum;
	},
	
	pressingAltKey : function(event) {
	  if (document.all) {
	  	return window.event.altKey;
	  }
	  else if (document.getElementById) {
	  	return event.altKey;
	  }
	  else if (document.layers) {
	  	return event.modifiers & Event.ALT_MASK;
	  }
	  else {
	  	return false;
	  }
	},
	
	pressingShiftKey : function(event) {
	  if (document.all) {
	  	return window.event.shiftKey;
	  }
	  else if (document.getElementById) {
	  	return event.shiftKey;
	  }
	  else if (document.layers) {
	  	return event.modifiers & Event.SHIFT_MASK;
	  }
	  else {
	  	return false;
	  }
	},
	
	pressingControlKey : function(event) {
	  if (document.all) {
	  	return window.event.ctrlKey;
	  }
	  else if (document.getElementById) {
	  	return event.ctrlKey;
	  }
	  else if (document.layers) {
	  	return event.modifiers & Event.CONTROL_MASK;
	  }
	  else {
	  	return false;
	  }
	},
	
	pressingMetaKey : function(event) {
	  if (document.all) {
	  	return window.event.metaKey;
	  }
	  else if (document.getElementById) {
	  	return event.metaKey;
	  }
	  else if (document.layers) {
	  	return event.modifiers & Event.META_MASK;
	  }
	  else {
	  	return false;
	  }
	}
});

Object.extend(Form, {
  serializeWithoutSubmits: function(form) {
    var elements = Form.getElements($(form));
    var queryComponents = new Array();

    for (var i = 0; i < elements.length; i++) {
			if (elements[i].type != 'submit') {
        if (elements[i].type == 'select-multiple' && !elements[i].value) continue;
	      var queryComponent = Form.Element.serialize(elements[i]);
	      if (queryComponent) {
	        queryComponents.push(queryComponent);
				}
			}
    }

    return queryComponents.join('&');
  }
});

Object.extend(Form, {
  clear: function(form) {  // Clears data rather than reset to original values
      	inputs=Form.getInputs(form);
    	for (inputIdx=0; inputIdx < inputs.length; inputIdx++) {
    		anInput = inputs[inputIdx];
    		if (anInput.type.toLowerCase() == 'text') anInput.value = '';
    		if (anInput.type.toLowerCase() == 'radio') anInput.checked = anInput.defaultChecked;
    		if (anInput.type.toLowerCase() == 'checkbox') anInput.checked = false;
    		if (anInput.type.toLowerCase() == 'file') anInput.value = '';
    		if (anInput.type.toLowerCase() == 'password') anInput.value = '';
    	}
    	
    	selects = $(form).getElementsByTagName('select');
    	for (selectIdx=0; selectIdx < selects.length; selectIdx++) {
    		selects[selectIdx].selectedIndex = 0;
    	}
    }
});  

var AjaxInPlace = {
	saveFunctionName : function(id) {
		return "window." + id + "Save";
	},
	
	cancelFunctionName : function(id) {
		return "window." + id + "Cancel";
	},
	
	editFunctionName : function(id) {
		return "window." + id + "Edit";
	},
	
	cleanupEdit : function(id) {
		var saveFunctionName = this.saveFunctionName(id);
		var cancelFunctionName = this.cancelFunctionName(id);
		if (typeof eval(saveFunctionName) != 'undefined') { eval(saveFunctionName + " = null"); }
		if (typeof eval(cancelFunctionName) != 'undefined') { eval(cancelFunctionName + " = null"); }
	},
	
	cleanupView : function(id) {
		var editFunctionName = this.editFunctionName(id);
		if (typeof eval(editFunctionName) != 'undefined') { eval(editFunctionName + " = null"); }
	}
};
var AIP = AjaxInPlace;

var AjaxOptions = {
	options : function(additionalOptions) {
		var options = { method: 'get', asynchronous: true, evalScripts: true };
		Object.extend(options, additionalOptions || {});
		return options;
	}
}

var AjaxUpdateContainer = {
	insertionFunc : function(effectPairName, beforeDuration, afterDuration) {
		var insertionFunction;
		
		var showEffect = 0;
		var hideEffect = 1;
		
		for (var existingPairName in Effect.PAIRS) {
			var pairs = Effect.PAIRS[existingPairName];
	
			if (effectPairName == existingPairName) {
				insertionFunction = function(receiver, response) {
					Effect[Effect.PAIRS[effectPairName][hideEffect]](receiver, { 
						duration: beforeDuration || 0.5,
						afterFinish: function() { 
							receiver.update(response); 
							Effect[Effect.PAIRS[effectPairName][showEffect]](receiver, {
									duration: afterDuration || 0.5
							});
						}
					});
				};
			}
			else if (effectPairName == pairs[hideEffect]) {
				insertionFunction = function(receiver, response) {
					Effect[effectPairName](receiver, { 
						duration: beforeDuration || 0.5,
						afterFinish: function() { 
							receiver.update(response);
							receiver.show();
						}
					});
				};
			}
			else if (effectPairName == pairs[showEffect]) {
				insertionFunction = function(receiver, response) {
					receiver.hide();
					receiver.update(response); 
					Effect[effectPairName](receiver, {
						duration: afterDuration || 0.5
					});
				};
			}
		}
		
		return insertionFunction;
	},
	
	register : function(id, options) {
		if (!options) {
			options = {};
		}
		eval(id + "Update = function() { AjaxUpdateContainer.update(id, options) }");
	},
	
	update : function(id, options) {
		new Ajax.Updater(id, $(id).getAttribute('updateUrl'), AjaxOptions.options(options));
	}
};
var AUC = AjaxUpdateContainer;

var AjaxUpdateLink = {
	updateFunc : function(id, options, elementID) {
		var updateFunction = function(queryParams) {
			AjaxUpdateLink.update(id, options, elementID, queryParams);
		};
		return updateFunction;
	},
	
	update : function(id, options, elementID, queryParams) {
		var actionUrl = $(id).getAttribute('updateUrl').sub('[^/]+$', elementID);
		actionUrl = actionUrl.addQueryParameters(queryParams);
		new Ajax.Updater(id, actionUrl, AjaxOptions.options(options));
	}
};
var AUL = AjaxUpdateLink;

var AjaxPeriodicUpdater = Class.create();
AjaxPeriodicUpdater.prototype = {
	initialize : function(id) {
		this.id = id;
	},
	
	start : function() {
		this.updater = new Ajax.PeriodicalUpdater(this.id, $(this.id).getAttribute('updateUrl'), { evalScripts: true, frequency: 2.0 });
	},

	stop : function() {
		if (this.updater) {
			this.updater.stop();
			this.updater = null;
		}
	}
};

Ajax.StoppedPeriodicalUpdater = Class.create();
Ajax.StoppedPeriodicalUpdater.prototype = Object.extend(new Ajax.Base(), {
  initialize: function(container, url, options) {
    this.setOptions(options);
    this.onComplete = this.options.onComplete;

    this.frequency = (this.options.frequency || 2);
    this.decay = (this.options.decay || 1);

    this.updater = {};
    this.container = container;
    this.url = url;
	if(!options.stopped) {
		this.start();
	}
    this.timer = undefined;
  },

  start: function() {
    this.options.onComplete = this.updateComplete.bind(this);
    this.onTimerEvent();
  },

  isRunning: function() {
  	// return this.updater && this.updater.options && this.updater.options.onComplete != undefined;
  	return this.timer != undefined;
  },
  

  stop: function() {
    this.updater.options.onComplete = undefined;
    clearTimeout(this.timer);
    this.timer = undefined;
    (this.onComplete || Prototype.emptyFunction).apply(this, arguments);
  },

  updateComplete: function(request) {
    if (this.options.decay) {
      this.decay = (request.responseText == this.lastText ?
        this.decay * this.options.decay : 1);

      this.lastText = request.responseText;
    }
    this.timer = setTimeout(this.onTimerEvent.bind(this),
      this.decay * this.frequency * 1000);
  },

  onTimerEvent: function() {
    this.updater = new Ajax.Updater(this.container, this.url, this.options);
  }
});

var AjaxHintedText = {
    register : function(name) {
        name = name ? "form#" + name : "form";
        var e = new Object();
        e[name + " input"] = AjaxHintedText.textBehaviour;
        e[name + " textarea"] = AjaxHintedText.textBehaviour;
        e[name + ""] = AjaxHintedText.formBehaviour;
        Behaviour.register(e);
    },
    textBehaviour : function(e) {
        if(!e.getAttribute('default')) {
            return;
        }
        e.setAttribute('default', unescape(e.getAttribute('default')));
        e.showDefaultValue = function() {
            if(e.value == "") {
                e.className = "ajax-hinted-text-with-default";
                e.value = e.getAttribute('default');
            } else {
                e.className = "";
            }
        }
        e.showTextValue = function() {
            e.className = "";
            if(e.value.replace(/[\r\n]/g, "") == e.getAttribute('default').replace(/[\r\n]/g, "")) {
                e.value = "";
            }
        }
        e.showDefaultValue();
        var oldFocus = e.onfocus;
        e.onfocus = function() {
            e.showTextValue();
            return oldFocus ? oldFocus() : true;
        }
        var oldBlur = e.onblur;
        e.onblur = function() {
            e.showDefaultValue();
            return oldBlur ? oldBlur() : true;
        }
        e.onsubmit = e.showTextValue;
    },
    formBehaviour :  function(e) {
        var old = e.onsubmit;
        e.onsubmit = function() {
            for(var i = 0; i < e.elements.length; i++) {
                var el = e.elements[i];
                if(el.onsubmit) {
                    el.onsubmit();
                }
            }
            return old ? old() : true;
        }
    }
};


// our own extensions 
// MS: This doesn't appear to be used and it causes a failure 
// in IE6.
/*
var Wonder = {};
Wonder.Autocompleter = Class.create();
Object.extend(Object.extend(Wonder.Autocompleter.prototype, Ajax.Autocompleter.prototype), {
   initialize: function(element, update, url, options) {
     Ajax.Autocompleter.prototype.initialize(element, update, url, options);
     this.defaultValue = options.defaultValue;
     if(this.defaultValue) {
     	Event.observe(this.element, "focus", this.onDefaultValueFocus.bindAsEventListener(this));
     	Event.observe(this.element, "blur", this.onDefaultValueBlur.bindAsEventListener(this));
	    $(element).value = defaultValue;
     }
   },
   onDefaultValueFocus: function(e) {
   	 if(this.element.value == this.defaultValue) {
	   	 this.element = "";
	 }
   },
   onDefaultValueBlur: function(e) {
   	 if(this.element.value == "") {
	   	 this.element = this.defaultValue;
	 }
   }
});
*/

if (typeof Ajax != 'undefined' && typeof Ajax.InPlaceEditor != 'undefined') {
	/*
	 * InPlaceEditor extension that adds a 'click to edit' text when the field is 
	 * empty.
	 */
	Ajax.InPlaceEditor.prototype.__initialize = Ajax.InPlaceEditor.prototype.initialize;
	Ajax.InPlaceEditor.prototype.__getText = Ajax.InPlaceEditor.prototype.getText;
	Ajax.InPlaceEditor.prototype.__onComplete = Ajax.InPlaceEditor.prototype.onComplete;
	Ajax.InPlaceEditor.prototype = Object.extend(Ajax.InPlaceEditor.prototype, {
	
	    initialize: function(element, url, options){
	        this.__initialize(element,url,options)
	        this.setOptions(options); 
	        this._checkEmpty();
	    },
	
	    setOptions: function(options){
	        this.options = Object.extend(Object.extend(this.options,{
	            valueWhenEmpty: 'click to edit...',
	            emptyClassName: 'inplaceeditor-empty'
	        }),options||{});
	    },
	    _checkEmpty: function(){
	        if( this.element.innerHTML.length == 0 ){
	            this.element.appendChild(
	                Builder.node('span',{className:this.options.emptyClassName},this.options.valueWhenEmpty));
	        }
	    },
	
	    getText: function(){
	        document.getElementsByClassName(this.options.emptyClassName,this.element).each(function(child){
	            this.element.removeChild(child);
	        }.bind(this));
	        return this.__getText();
	    },
	
	    onComplete: function(transport){
	        this._checkEmpty();
	        this.__onComplete(transport);
	    }
	});
}

/**
 * Hoverable, adds support for "delayed onmouseout" events, so you don't
 * that obnoxious onhover behavior where it immediately unhovers when
 * you slightly mouse out of the element.
 *
 * Specify elements that receive this behavior with a .hoverable class,
 * and in CSS, your elements will become .hoverable.hover when they're
 * in the delayed hover state.
 *
 * Call Hoverable.register() in your footer to enable this feature, and
 * include behaviour.js in your page.     
 */
var Hoverable = {
  _rules : {
    '.hoverable' : function(element) {
      element.onmouseover = function() {
        Hoverable.over(this);
      },

      element.onmouseout = function() {
        Hoverable.out(this);
      }
    }
  },

  over : function(element) {
    element.addClassName("hover");
    if (element['hoverCount'] == undefined) {
      element['hoverCount'] = 0;
    }
    else {
      element['hoverCount'] ++;
    }
  },

  out : function(element) {
    setTimeout(Hoverable._end.bind(element, element['hoverCount']), 300);
  },

  _end : function(hoverCount) {
    var element = this;
    if (element['hoverCount'] == hoverCount) {
      element.removeClassName("hover");
    }
  },

  register : function() {
    Behaviour.register(Hoverable._rules);
  }
}
 