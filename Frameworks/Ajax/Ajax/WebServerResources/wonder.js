// $wi = the Wonder "lookup by ID" function
var $wi = $;
 
Object.extend(Prototype, {
  exec: (function(){
    var script, scriptId = '__prototype_exec_script',
    head = document.getElementsByTagName('HEAD')[0]; //how often is there no head? 
    (script = document.createElement('script')).type = 'text/javascript';
    try { script.appendChild( document.createTextNode('') )} catch (e) {}
    
    return  function(code){
      if((code+='').blank()) return;
      
      var s = script.cloneNode(true);
      if(document.loaded){
        s.text = code;
        if(s.firstChild) s.firstChild.data = code;
        head.appendChild(s);
      }
      else{
        //firefox 2.0.0.2/camino 1.0.4 don't execute inserted scripts synchronously when dom not loaded
        document.write('<script id="'+scriptId+'" type="text/javascript">'+ code +'<\/script>');
        s = $(scriptId);
      }
      s.parentNode.removeChild(s);
    }
  })()
});

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
	
	pressingAltKey: function(event) {
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
	
	pressingShiftKey: function(event) {
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
	
	pressingControlKey: function(event) {
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
	
	pressingMetaKey: function(event) {
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
            if (selects[selectIdx].type == 'select-multiple') {
                selects[selectIdx].selectedIndex = -1;
            } else {
                selects[selectIdx].selectedIndex = 0;
            }
    	}
    }
});  

var AjaxOnDemand = {
	loadScript: function(script) {
		new Ajax.Request(script, { method: 'get', asynchronous: false, evalJS: false, onComplete: AjaxOnDemand.loadedScript });
	},
	
	loadedScript: function(request) {
		Prototype.exec(request.responseText);
	},
	
	loadCSS: function(css) {
        var link=document.createElement("link");
        link.setAttribute("rel", "stylesheet");
        link.setAttribute("type", "text/css");
        link.setAttribute("href", css);
        if (typeof link!="undefined") {
            document.getElementsByTagName("head")[0].appendChild(link);
        }
    },
	
	loadedCSS: function(request) {
		var inlineStyle = new Element("style", {"type": "text/css"});
		inlineStyle.appendChild(document.createTextNode(request.responseText));
		document.getElementsByTagName('HEAD')[0].appendChild(inlineStyle);
	}
};
var AOD = AjaxOnDemand;

var AjaxUtils = {
	toggleClassName: function(element, className, toggled) {
		element = $(element);
		if (toggled) {
			Element.addClassName(element, className);
		}
		else {
			Element.removeClassName(element, className);
		}
	}
};

var AjaxInPlace = {
	saveFunctionName: function(id) {
		return "window." + id + "Save";
	},
	
	cancelFunctionName: function(id) {
		return "window." + id + "Cancel";
	},
	
	editFunctionName: function(id) {
		return "window." + id + "Edit";
	},
	
	cleanupEdit: function(id) {
		var saveFunctionName = this.saveFunctionName(id);
		var cancelFunctionName = this.cancelFunctionName(id);
		if (typeof eval(saveFunctionName) != 'undefined') { eval(saveFunctionName + " = null"); }
		if (typeof eval(cancelFunctionName) != 'undefined') { eval(cancelFunctionName + " = null"); }
	},
	
	cleanupView: function(id) {
		var editFunctionName = this.editFunctionName(id);
		if (typeof eval(editFunctionName) != 'undefined') { eval(editFunctionName + " = null"); }
	}
};
var AIP = AjaxInPlace;

var AjaxModalContainer = {
	close: function() {
		iBox.hide();
	}
};
var AMC = AjaxModalContainer;

var AjaxOptions = {
	defaultOptions: function(additionalOptions) {
		var options = { method: 'get', asynchronous: true, evalScripts: true };
		Object.extend(options, additionalOptions || {});
		return options;
	}
}

var AjaxUpdateContainer = {
	registerPeriodic: function(id, canStop, stopped, options) {
		var url = $(id).getAttribute('data-updateUrl');
		var updater;
		if (!canStop) {
			updater = new Ajax.PeriodicalUpdater(id, url, options);
		}
		else if (stopped) {
			var newOptions = Object.extend({}, options);
			newOptions.stopped = true;
			updater = new Ajax.StoppedPeriodicalUpdater(id, url, newOptions);
		}
		else {
			updater = new Ajax.ActivePeriodicalUpdater(id, url, options);
		}
		
		eval(id + "PeriodicalUpdater = updater;");
		eval(id + "Stop = function() { " + id + "PeriodicalUpdater.stop() };");
	},
	
	insertionFunc: function(effectPairName, beforeDuration, afterDuration) {
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
	
	register: function(id, options) {
		if (!options) {
			options = {};
		}
		eval(id + "Update = function() {AjaxUpdateContainer.update(id, options) }");
	},
	
	update: function(id, options) {
		var updateElement = $(id);
		if (updateElement == null) {
			alert('There is no element on this page with the id "' + id + '".');
		}
		var actionUrl = updateElement.getAttribute('data-updateUrl');
		if (options && options['_r']) {
			actionUrl = actionUrl.addQueryParameters('_r='+ id);
		}
		else {
			actionUrl = actionUrl.addQueryParameters('_u='+ id);
		}
		actionUrl = actionUrl.addQueryParameters(new Date().getTime());
		new Ajax.Updater(id, actionUrl, AjaxOptions.defaultOptions(options));
	}
};
var AUC = AjaxUpdateContainer;

var AjaxUpdateLink = {
	updateFunc: function(id, options, elementID) {
		var updateFunction = function(queryParams) {
			AjaxUpdateLink.update(id, options, elementID, queryParams);
		};
		return updateFunction;
	},
	
	update: function(id, options, elementID, queryParams) {
		var updateElement = $(id);
		if (updateElement == null) {
			alert('There is no element on this page with the id "' + id + '".');
		}
		AjaxUpdateLink._update(id, updateElement.getAttribute('data-updateUrl'), options, elementID, queryParams);
	},
	
	_update: function(id, actionUrl, options, elementID, queryParams) {
		if (elementID) {
			actionUrl = actionUrl.sub(/[^\/]+$/, elementID);
		}
		actionUrl = actionUrl.addQueryParameters(queryParams);
		if (options && options['_r']) {
			actionUrl = actionUrl.addQueryParameters('_r='+ id);
		}
		else {
			actionUrl = actionUrl.addQueryParameters('_u='+ id);
		}
		actionUrl = actionUrl.addQueryParameters(new Date().getTime());
		new Ajax.Updater(id, actionUrl, AjaxOptions.defaultOptions(options));
	},
	
	request: function(actionUrl, options, elementID, queryParams) {
		if (elementID) {
			actionUrl = actionUrl.sub(/[^\/]+$/, elementID);
		}
		actionUrl = actionUrl.addQueryParameters(queryParams);
		new Ajax.Request(actionUrl, AjaxOptions.defaultOptions(options));
	}
};
var AUL = AjaxUpdateLink;

var AjaxSubmitButton = {
	PartialFormSenderIDKey: '_partialSenderID',
	
	AjaxSubmitButtonNameKey: 'AJAX_SUBMIT_BUTTON_NAME',
	
	defaultOptions: function(additionalOptions) {
		var options = AjaxOptions.defaultOptions(additionalOptions);
		options['method'] = 'post';
		return options;
	},
	
	generateActionUrl: function(id, form, queryParams, options) {
		var actionUrl = form.action;
		if (queryParams != null) {
			actionUrl = actionUrl.addQueryParameters(queryParams);
		}
		actionUrl = actionUrl.sub('/wo/', '/ajax/', 1);
		if (id != null) {
			if (options && options['_r']) {
				actionUrl = actionUrl.addQueryParameters('_r=' + id);
			}
			else {
				actionUrl = actionUrl.addQueryParameters('_u=' + id);
			}
		}
		actionUrl = actionUrl.addQueryParameters(new Date().getTime());
		return actionUrl;
	},
	
	processOptions: function(form, options) {
		var processedOptions = null;
		if (options != null) {
			processedOptions = Object.extend(new Object(), options);
			
			var ajaxSubmitButtonName = processedOptions['_asbn'];
			if (ajaxSubmitButtonName != null) {
				processedOptions['_asbn'] = null;
				var parameters = processedOptions['parameters'];
				if (parameters === undefined || parameters == null) {
					$(form).fire('ajax:submit');
					var formSerializer = processedOptions['_fs'];
					if (formSerializer == null) {
						formSerializer = Form.serializeWithoutSubmits;
					}
					else {
						processedOptions['_fs'] = null;
					}
					var serializedForm = formSerializer(form);
					processedOptions['parameters'] = serializedForm + '&' + AjaxSubmitButton.AjaxSubmitButtonNameKey + '=' + ajaxSubmitButtonName;
				}
				else {
					processedOptions['parameters'] = parameters + '&' + AjaxSubmitButton.AjaxSubmitButtonNameKey + '=' + ajaxSubmitButtonName;
				}
			}
		}
		processedOptions = AjaxSubmitButton.defaultOptions(processedOptions);
		return processedOptions;
	},
	
	partial: function(updateContainerID, formFieldID, options) {
		var optionsCopy = Object.extend(new Object(), options); 
		var formField = $(formFieldID);
		var form = formField.form;
		
		var queryParams = {};
		queryParams[formField.name] = $F(formField);
		queryParams[AjaxSubmitButton.PartialFormSenderIDKey] = formField.name;
		optionsCopy['parameters'] = Hash.toQueryString(queryParams);
		
		if (updateContainerID == null) {
			AjaxSubmitButton.request(form, null, optionsCopy);
		}
		else {
			AjaxSubmitButton.update(updateContainerID, form, null, optionsCopy);
		}
	},
	
	update: function(id, form, queryParams, options) {
		var updateElement = $(id);
		if (updateElement == null) {
			alert('There is no element on this page with the id "' + id + '".');
		}
		var finalUrl = AjaxSubmitButton.generateActionUrl(id, form, queryParams, options);
		var finalOptions = AjaxSubmitButton.processOptions(form, options);
		new Ajax.Updater(id, finalUrl, finalOptions);
	},
	
	request: function(form, queryParams, options) {
		var finalUrl = AjaxSubmitButton.generateActionUrl(null, form, queryParams, options);
		var finalOptions = AjaxSubmitButton.processOptions(form, options);
		new Ajax.Request(finalUrl, finalOptions);
	},
	
	observeDescendentFields: function(updateContainerID, containerID, observeFieldFrequency, partial, observeDelay, options, actOnKeyUp) {
    $(containerID).descendants().find(function(element) {
      if (element.type != 'hidden' && ['input', 'select', 'textarea'].include(element.tagName.toLowerCase())) {
      	AjaxSubmitButton.observeField(updateContainerID, element, observeFieldFrequency, partial, observeDelay, options, actOnKeyUp);
      }
    });
	},
	
	observeField: function(updateContainerID, formFieldID, observeFieldFrequency, partial, observeDelay, options, actOnKeyUp) {
		var submitFunction;
		if (partial) {
			// We need to cheat and make the WOForm that contains the form action appear to have been
			// submitted. So we grab the action url, pull off the element ID from its action URL
			// and pass that in as FORCE_FORM_SUBMITTED_KEY, which is processed by ERXWOForm just like
			// senderID is on the real WOForm. Unfortunately we can't hook into the real WOForm to do
			// this :(
			submitFunction = function(element, value) {
				if (!options.onBeforeSubmit || options.onBeforeSubmit(formFieldID)) {
					ASB.partial(updateContainerID, formFieldID, options);
				}
			}
		}
		else if (updateContainerID != null) {
			submitFunction = function(element, value) {
				if (!options.onBeforeSubmit || options.onBeforeSubmit(formFieldID)) {
					ASB.update(updateContainerID, $(formFieldID).form, null, options);
				}
			}
		}
		else {
			submitFunction = function(element, value) {
				if (!options.onBeforeSubmit || options.onBeforeSubmit(formFieldID)) {
					ASB.request($(formFieldID).form, null, options);
				}
			}
		}

		if (observeDelay) {
			var delayer = new AjaxObserveDelayer(observeDelay, submitFunction);
			submitFunction = delayer.valueChanged.bind(delayer);
		}

		if (observeFieldFrequency == null) {
			if ($(formFieldID).type.toLowerCase() == 'radio') {
	    	new Form.Element.RadioButtonObserver($(formFieldID), submitFunction);
			}
			else {
	    	new Form.Element.ExtendedEventObserver($(formFieldID), submitFunction, actOnKeyUp);
			}
		}
		else {
    	new Form.Element.Observer($(formFieldID), observeFieldFrequency, submitFunction);
		}
	}
};
var ASB = AjaxSubmitButton;

var AjaxObserveDelayer = Class.create({
	delay: null,
	waiting: null,
	lastValueChange: null,
	submitFunction: null,

	element: null,
	value: null,
	
	initialize: function(delay, submitFunction) {
		this.delay = delay * 1000.0;
		this.submitFunction = submitFunction;
	},
	
	valueChanged: function(element, value) {
		this.element = element;
		this.value = value;
		this.lastValueChange = new Date().getTime();
		
		if (!this.waiting) {
			this.waiting = true;
			var lastValueChange = this.lastValueChange;
			setTimeout(this.delayFinished.bind(this, lastValueChange), this.delay);
		}
	},
	
	delayFinished: function(lastValueChange) {
		if (lastValueChange == this.lastValueChange) {
			this.waiting = false;
			this.submitFunction(this.element, this.value);
		}
		else {
			var newLastValueChange = this.lastValueChange;
			var now = new Date().getTime();
			var delayLeft = Math.max(250, this.delay - (now - newLastValueChange));
			setTimeout(this.delayFinished.bind(this, newLastValueChange), delayLeft);
		}
	}
});

var AjaxDraggable = {
	register: function(draggableContainerID, options) {
		var draggableContainerName = 'draggable_' + draggableContainerID;
		var draggableContainerType = eval("typeof " + draggableContainerName);
		if (draggableContainerType != 'undefined') {
			eval(draggableContainerName).destroy();
		}
		var draggableContainer = new Draggable(draggableContainerID, options);
		eval(draggableContainerName + "=draggableContainer");
	}
};
var ADG = AjaxDraggable;

var AjaxDroppable = Class.create({
	contextID: null,
	elementID: null,
	droppableElementID: null,
	draggableKeyName: null,
	updateContainerID: null,
	actionUrl: null,
	submit: null,
	form: null,
	onbeforedrop: null,
	ondrop: null,
	options: null,
	
	initialize: function(contextID, elementID, droppableElementID, draggableKeyName, updateContainerID, actionUrl, form, onbeforedrop, ondrop, options) {
		this.contextID = contextID;
		this.elementID = elementID;
		this.droppableElementID = droppableElementID;
		this.draggableKeyName = draggableKeyName;
		this.updateContainerID = updateContainerID;
		this.actionUrl = actionUrl;
		this.form = form;
		this.onbeforedrop = onbeforedrop;
		this.ondrop = ondrop;
		this.options = options;
	},
	
	dropped: function(element, droppableElement) {
    if (droppableElement.id == this.droppableElementID) {
		if(typeof this.options.confirmMessage != "undefined") {
			if(!confirm(this.options.confirmMessage))
				return;
		}
		if (this.onbeforedrop) {
			this.onbeforedrop(element, droppableElement);
		}

		var draggableID = element.getAttribute('draggableID');
		if(draggableID == null)
			draggableID = element.getAttribute('id');
    	var data = this.draggableKeyName + '=' + draggableID;
    	
			if (this.updateContainerID == null) {
				if (this.form) {
					ASB.request(this.form, data, this.options);
				}
				else {
					AUL.request(this.actionUrl, this.options, this.contextID + '.' + this.elementID, data);
				}
			}
			else {
				if (this.form) {
					ASB.update(this.updateContainerID, this.form, data, this.options);
				}
				else {
					AUL.update(this.updateContainerID, this.options, this.contextID + '.' + this.elementID, data);
				}
			}
			
			if (this.ondrop) {
				this.ondrop(element, droppableElement);
			}
    }
	}
});
Object.extend(AjaxDroppable, {
	register: function(droppableContainerID, options) {
		var droppableContainerName = 'droppable_' + droppableContainerID;
		var droppableContainerType = eval("typeof " + droppableContainerName);
		if (droppableContainerType != 'undefined') {
			Droppables.remove(eval(droppableContainerName));
		}
		var droppableContainer = $(droppableContainerID);
		eval(droppableContainerName + '=droppableContainer');
		Droppables.add(droppableContainer, options);
	},
	
	droppedFunc: function(contextID, elementID, droppableElementID, draggableKeyName, updateContainerID, actionUrl, form, onbeforedrop, ondrop, options) {
		var adp = new AjaxDroppable(contextID, elementID, droppableElementID, draggableKeyName, updateContainerID, actionUrl, form, onbeforedrop, ondrop, options);
		return adp.dropped.bind(adp);
	}
});
var ADP = AjaxDroppable;

var AjaxHighlight = {
	highlight: function(id, delay, showEffectName, showDuration, effectName, duration, hideDelay, hideEffectName, hideDuration) {
		if (showEffectName) {
			var showOptions = {};
			showOptions.queue = 'end';
			if (showDuration) {
				showOptions.duration = showDuration;
			}
			if (delay) {
				showOptions.delay = delay; 
			}

			// MS: gotta be a better way		
			eval('new ' + showEffectName + '(id, showOptions)');
		}
		
		var options = {};
		options.queue = 'end';
		if (duration) {
			options.duration = duration;
		}
		if (delay && !showEffectName) {
			options.delay = delay;
		}

		// MS: gotta be a better way		
		eval('new ' + effectName + '(id, options)');

		if (hideEffectName) {
			var hideOptions = {};
			hideOptions.queue = 'end';
			if (hideDuration) {
				hideOptions.duration = hideDuration;
			}
			if (hideDelay) {
				hideOptions.delay = hideDelay;
			}

			// MS: gotta be a better way		
			eval('new ' + hideEffectName + '(id, hideOptions)');
		}
		
	}
};
var AH = AjaxHighlight;

var AjaxPeriodicUpdater = Class.create();
AjaxPeriodicUpdater.prototype = {
	initialize: function(id) {
		this.id = id;
	},
	
	start: function() {
		var actionUrl = $(this.id).getAttribute('data-updateUrl');
		actionUrl = actionUrl.addQueryParameters('_u='+ id);
		this.updater = new Ajax.PeriodicalUpdater(this.id, actionUrl, { evalScripts: true, frequency: 2.0 });
	},

	stop: function() {
		if (this.updater) {
			this.updater.stop();
			this.updater = null;
		}
	}
};

Ajax.ActivePeriodicalUpdater = Class.create(Ajax.Base, {
  initialize: function($super, container, url, options) {
    $super(options);
    this.onComplete = this.options.onComplete;

    this.frequency = (this.options.frequency || 2);
    this.decay = (this.options.decay || 1);

    this.updater = {};
    this.container = container;
    this.url = url;
		this.start();
    this.timer = undefined;
  },

	
  isRunning: function() {
  	return this.updater && this.updater.options && this.updater.options.onComplete != undefined;
  },

  start: function() {
    this.options.onComplete = this.updateComplete.bind(this);
    this.onTimerEvent();
  },
  

  stop: function() {
    this.updater.options.onComplete = undefined;
    clearTimeout(this.timer);
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

Ajax.StoppedPeriodicalUpdater = Class.create(Ajax.Base, {
  initialize: function($super, container, url, options) {
    $super(options);
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
    register: function(tag, id) {
      $$(tag + "#" + id).each(function(script, index) {
          Element.select($(script), 'input', 'textarea').each(function(el, index) {
              AjaxHintedText.textBehaviour(el);
          });
          Element.select($(script), 'form').each(function(form, index) {
              AjaxHintedText.formBehaviour(form);     
          });
      });
    },
    registerForm : function(formselector) {
      $$(formselector).each(function(form,index) {
            Element.select(form,'input','textarea').each(function(el, index) {
              AjaxHintedText.textBehaviour(el);
          });
          AjaxHintedText.formBehaviour(form);     
      });
    },
    textBehaviour : function(e) {
        if(!e.getAttribute('default')) {
            return;
        }
        e.setAttribute('default', unescape(e.getAttribute('default')));
        e.showDefaultValue = function() {
            if(e.value == "" ||
               e.value.replace(/[\r\n]/g, "") == e.getAttribute('default').replace(/[\r\n]/g, "")) {
                Element.addClassName(e, 'ajax-hinted-text-with-default');
                  e.value = e.getAttribute('default');
              } else {
                Element.removeClassName(e, 'ajax-hinted-text-with-default');
              }
        }
        e.showTextValue = function() {
            Element.removeClassName(e, 'ajax-hinted-text-with-default');
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

/**
 * Hoverable, adds support for "delayed onmouseout" events, so you don't
 * that obnoxious onhover behavior where it immediately unhovers when
 * you slightly mouse out of the element.
 *
 * Specify elements that receive this behavior with a .hoverable class,
 * and in CSS, your elements will become .hoverable.hover when they're
 * in the delayed hover state.
 *
 * Call Hoverable.register() in your footer to enable this feature.
 */
var Hoverable = {
	delay: 300,
	
  over: function(event) {
  	var element = this;
    Element.addClassName(element, "hover");
    if (element['hoverCount'] == undefined) {
      element['hoverCount'] = 0;
    }
    else {
      element['hoverCount'] ++;
    }
  },

  out: function(event) {
  	var element = this;
    setTimeout(Hoverable._end.bind(element, element['hoverCount']), Hoverable.delay);
  },

  _end: function(hoverCount) {
    var element = this;
    if (element['hoverCount'] == hoverCount) {
      Element.removeClassName(element, "hover");
    }
  },

	unregister: function(element) {
 		Event.stopObserving(element, "mouseover", Hoverable.over.bindAsEventListener(element));
 		Event.stopObserving(element, "mouseout", Hoverable.out.bindAsEventListener(element));
	},
	
  register: function(delay) {
  	if (delay !== undefined) {
  		Hoverable.delay = delay;
  	}
  	$$('.hoverable').each(function(element, index) {
  		Event.observe(element, "mouseover", Hoverable.over.bindAsEventListener(element));
  		Event.observe(element, "mouseout", Hoverable.out.bindAsEventListener(element));
  	});
  }
}

Form.Element.RadioButtonObserver = Class.create(Form.Element.EventObserver, {
  onElementEvent: function() {
    var value = this.getValue();
	  this.callback(this.element, value);
  	this.lastValue = value;
  }
});

Form.Element.ExtendedEventObserver = Class.create(Form.Element.EventObserver, {
  initialize: function($super, element, callback, actOnKeyUp) {
    this.actOnKeyUp = actOnKeyUp;
    $super(element, callback);
  },

  registerCallback: function(element) {
    if (element.type) {
      switch (element.type.toLowerCase()) {
        case 'checkbox':
        case 'radio':
          Event.observe(element, 'click', this.onElementEvent.bind(this));
          break;
        case 'text':
          Event.observe(element, 'change', this.onElementEvent.bind(this));
          if (this.actOnKeyUp)
	          Event.observe(element, 'keyup', this.onElementEvent.bind(this));
          break;
        default:
          Event.observe(element, 'change', this.onElementEvent.bind(this));
          break;
      }
    }
  }

});

var AjaxBusy = {
	spinners: {},
	
	requestContainer: function(request) {
		var updateContainer;
		if (request && request.container && request.container.success) {
			updateContainer = $(request.container.success);
		}
		return updateContainer;
	},
	
	register: function(busyClass, busyAnimationElement, watchContainerID, onCreateCallback, onCompleteCallback, useSpinJS, spinOpts) {
		Ajax.Responders.register({
			onCreate: function(request, transport) {
	     	var updateContainer = AjaxBusy.requestContainer(request);
	     	if (!watchContainerID || (updateContainer && updateContainer.id == watchContainerID)) {
	     		if (useSpinJS == true) {
	     			var spinner = AjaxBusy.spinners[busyAnimationElement];
	     			if (spinner == undefined) {
	     				spinner = new Spinner(spinOpts);
	     				AjaxBusy.spinners[busyAnimationElement] = spinner;
	     			}
	     			spinner.spin($(busyAnimationElement));
	     		}
			  	if (busyClass && updateContainer) {
						Element.addClassName(updateContainer, busyClass);
			   	}
			   	
			   	if (busyAnimationElement && $(busyAnimationElement)) {
			   		Effect.Appear(busyAnimationElement, {duration:0.0, queue:'front'});
			   	}
			   	
			   	if (onCreateCallback) {
			   		onCreateCallback(request, transport);
			   	}
	     	}
		  },
		  
		  onComplete: function(request, transport) {
	     	var updateContainer = AjaxBusy.requestContainer(request);
	     	if (!watchContainerID || (updateContainer && updateContainer.id == watchContainerID)) {
			  	if (busyClass && updateContainer) {
						Element.removeClassName(updateContainer, busyClass);
					}
					
					if (busyAnimationElement && $(busyAnimationElement)) {
						Effect.Fade(busyAnimationElement, {duration:0.5, queue:'end'});
					}
	
			   	if (onCompleteCallback) {
			   		onCompleteCallback(request, transport);
			   	}
			   	
	     		if (useSpinJS == true) {
	     			var spinner = AjaxBusy.spinners[busyAnimationElement];
	     			if (spinner) {
	     				AjaxBusy.spinners[busyAnimationElement] = undefined;
	     				setTimeout(function() { spinner.stop(); }, 500);
	     			}
	     		}
			  }
			}
	  });
	}
};

var AjaxModalDialog = {
	shouldRefreshCloseUpdateContainer: true,
	
	insertion: function(receiver, response) {
		receiver.update(response);
		Modalbox.resizeToContent({ transitions: false });
	},
	
	close: function() {
		Modalbox.hide();
	},
	
	open: function(id) {
		eval("openAMD_" + id + "()");
	},
	
	contentUpdated: function() {
		Modalbox._putContent();
	}
};
var AMD = AjaxModalDialog;

var AjaxFlexibleUpload = {

	uploaders: {},
	
	create: function(id, uploadButtonId, options) {
		this.uploaders[id] = new AjaxUpload(uploadButtonId, options);
	},
	
	cancelIFrame: function(iframeId, cancelUrl) {
		setTimeout(function(e) { $(iframeId).src = cancelUrl; }, 1000);
	},
	
	progressUpdate: function(uploaderId, fileNameId, additionalFunction, finalFunction) {
		var uploader = this.uploaders[uploaderId];
		if (uploader) {
			var fileName = uploader._input.value; 
			if ($(fileNameId) != null && fileName) { 
				$(fileNameId).update(fileName); 
			} 
		}
		this.executeCallbacks(uploaderId, additionalFunction, finalFunction);
	},
	
	executeCallbacks: function(uploaderId, additionalFunction, finalFunction) {
		if (additionalFunction) {
			additionalFunction(uploaderId);
		}
		if (finalFunction) {
			finalFunction(uploaderId);
		}
	},
	
	submit: function(uploaderId, updateContainerId) {
		var uploader = this.uploaders[uploaderId];
		uploader.submit();
	}
};
var AFU = AjaxFlexibleUpload;

var AjaxUploadClient = Class.create({
	initialize: function(theId, uploadButtonId, jsonrpc, labels, options, uploaderOptions) {
		this.STATE = { DORMANT : 0, STARTED : 1, INPROGRESS : 2, CANCELED : 3, FAILED : 4, SUCCEEDED : 5, FINISHED : 6 };
		this.id = theId;
		this.rpc = jsonrpc;
		this.previousState;
		this.stateChecker;
		this.labels = labels;
		this.options = options;
		this.count = 0;
		this.uploader = new AjaxUpload(uploadButtonId, uploaderOptions);
	},
	prepare: function(filename, extension) {
		$('AFUSubmitUploadButton' + this.id).show();
		$('AFUSelectFileButtonWrapper' + this.id).hide();
    	$('AFUFileObject' + this.id).show();
    	$('AFUFileNameWrapper' + this.id).update(filename);
    	$('AFUProgressBarWrapper' + this.id).hide();
    	$('AFUClearButton' + this.id).show();
	},
	submit: function() {
		$('AFUSubmitUploadButton' + this.id).hide();
		this.uploader.submit();
	},
	start: function() {
	    $('AFUClearButton' + this.id).hide();
		$('AFUSelectFileButtonWrapper' + this.id).hide();
    	$('AFUFileObject' + this.id).show();
    	$('AFUFileNameWrapper' + this.id).update(this.labels.upload_starting);
    	$('AFUProgressBarWrapper' + this.id).show();
    	if (this.options.allowcancel == true)
    		$('AFUCancelButton' + this.id).show();
    	$('AFUProgressBarValue' + this.id).setStyle({ width: '1%' });
   		this.update();
   		var self = this;
   		this.stateChecker = setInterval(function() { self.update(); }, self.options.refreshtime);
   		if (this.options.startedFunction)
			this.options.startedFunction(this.id);
	},
	clear: function() {
	    this.rpc.wopage.clearFileResults();
		$('AFUSelectFileButtonWrapper' + this.id).show();
		$('AFUClearButton' + this.id).hide();
		$('AFUFileObject' + this.id).hide();
		if (this.options.clearedFunction)
			this.options.clearedFunction(this.id);
	},
	cancel: function() {
		$('AFUFileNameWrapper' + this.id).update(this.labels.upload_canceling);
		$('AFUCancelButton' + this.id).hide();
		clearInterval(this.stateChecker);
		this.rpc.wopage.cancelUpload();
		var self = this;
		this.stateChecker = setInterval(function() { self.update() }, 1000);
	},
	update: function() {
		var rpcResult  = this.rpc.wopage.uploadState();
		var stateObj = rpcResult.nsdictionary;
		var state = stateObj.state;
		var filename = stateObj.filename;
		var progress = stateObj.progress;
		this.count++;
		if (this.previousState) {
			switch(this.previousState) {
			case this.STATE.CANCELED:
		    	clearInterval(this.stateChecker);
		    	$('AFUFileObject' + this.id).hide();
		 		$('AFUSelectFileButtonWrapper' + this.id).show();
		 		if (this.options.canceledFunction)
		    		this.options.canceledFunction(this.id);
		 		if (this.options.finishedFunction)
					this.options.finishedFunction(this.id);
				break;
			}
		}
		switch(state) {
		case this.STATE.DORMANT:
		    this.previousState = this.STATE.DORMANT;
			break;
		case this.STATE.STARTED:
		    this.previousState = this.STATE.STARTED;
			break;
		case this.STATE.INPROGRESS:
			$('AFUFileNameWrapper' + this.id).update(filename);
			$('AFUProgressBarValue' + this.id).setStyle({ width:progress + '%' });
			this.previousState = this.STATE.INPROGRESS;
			break;
		case this.STATE.CANCELED:
		 	$('AFUFileNameWrapper' + this.id).update(this.labels.upload_canceling);
		 	$('AFUCancelButton' + this.id).hide();
		 	this.previousState = this.STATE.CANCELED;
		 	try {
		 		var iFrameId = 'AFUIF' + this.id;
		 		var cancelUrl = stateObj.cancelUrl;
		 		$(iFrameId).src = cancelUrl;
		 	} catch (e) { 
		 		// if the iframe is gone already ignore this
		 	}
			break;
		case this.STATE.FAILED:
			clearInterval(this.stateChecker);
		    $('AFUFileNameWrapper' + this.id).update(this.labels.upload_failed);
			$('AFUProgressBarWrapper' + this.id).hide();
			$('AFUClearButton' + this.id).show();
			if (this.options.failedFunction)
		    	this.options.failedFunction(this.id);
			if (this.options.finishedFunction)
				this.options.finishedFunction(this.id);
		    this.previousState = this.STATE.FAILED;
			break;
		case this.STATE.SUCCEEDED:
			clearInterval(this.stateChecker);
			$('AFUFileNameWrapper' + this.id).update(filename);
			$('AFUProgressBarValue' + this.id).setStyle({ width:'100%' });
			$('AFUCancelButton' + this.id).hide();
			$('AFUProgressBarWrapper' + this.id).hide();
			if (this.options.succeededFunction)
		    	this.options.succeededFunction(this.id);
			if (this.options.finishedFunction)
				this.options.finishedFunction(this.id);
			if (this.options.clearUploadProgressOnSuccess) {
				$('AFUFileObject' + this.id).hide();
				$('AFUSelectFileButtonWrapper' + this.id).show();
			} else {
				$('AFUClearButton' + this.id).show();
			}
			this.previousState = this.STATE.SUCCEEDED;
			break;
		case this.STATE.FINISHED:
			break;
		}
	}
});
var AUP = AjaxUploadClient;
AUP.uploaders = {};
AUP.uploader = function(id) {
	return uploaders[id];
};
AUP.add = function(id, jsonrpc, labels, options, uploaderOptions) {
	var uploadButtonId = 'AFUSelectFileButton' + id;
	this.uploaders[id] = new AjaxUploadClient(id, uploadButtonId, jsonrpc, labels, options, uploaderOptions);
};
AUP.start = function(id) {
	var uploader = AUP.uploaders[id];
	uploader.start();
};
AUP.submit = function(id) {
	var uploader = AUP.uploaders[id];
	uploader.submit();
};
AUP.prepare = function(id, filename, extension) {
	var uploader = AUP.uploaders[id];
	uploader.prepare(filename, extension);
};
AUP.cancel = function(id) {
	var uploader = AUP.uploaders[id];
	uploader.cancel();
};
AUP.clear = function(id) {
	var uploader = AUP.uploaders[id];
	uploader.clear();
};

var WonderRemoteLogging = {
	options: {},
	sendTimer: 0,
	messages: [],

	sendLog: function() {
		var parts = WonderRemoteLogging.options.url.split("\?", 2)
		var finalUrl = parts[0] + "/" + WonderRemoteLogging.options.logger + "?" + (parts.length > 1? parts[1]: "");
		var request = new Ajax.Request(finalUrl, {method: 'POST', parameters: {
			l: WonderRemoteLogging.options.level,
			m: WonderRemoteLogging.messages.join("\n")
		}});
		WonderRemoteLogging.messages = [];
		WonderRemoteLogging.sendTimer = null;
	},

	install: function(options) {
		WonderRemoteLogging.options = options;
	    try{ 
			window.console = window.console || {};
	    } catch(e) {}
		window.console.oldlog = window.console.log || function(msg) {};
		window.console.filter = options.filter || function(msg) {return true};
		window.console.log = function(msg) {
			if(window.console.filter(msg)) {
				if(WonderRemoteLogging.sendTimer) {
					clearTimeout(WonderRemoteLogging.sendTimer);
					WonderRemoteLogging.sendTimer = null;
				}
				WonderRemoteLogging.messages.push(msg);
				WonderRemoteLogging.sendTimer = setTimeout(WonderRemoteLogging.sendLog, options.throttle);
			}
			if(window.console.oldlog) {
				window.console.oldlog(msg);
			}
		}
	}
}

var WonderJSON = {
	eoStub: function(eo) {
		var eoStub = new Object();
		eoStub.javaClass = eo.javaClass;
		eoStub.gid = eo.gid;
		return eoStub;
	} 
};