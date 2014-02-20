/*
	The code in this file is based upon wonder.js. Which is used in Project Wonder's Ajax Framework.
	http://wiki.wocommunity.org/display/WO/Project+WONDER-Frameworks-Ajax-AjaxUpdateContainer
	MoWonder.js ports the functionality found in wonder.js to MooTools.  wonder.js is based upon Prototype/Scriptaculous
	@author Jonathan Miller
 */

 
 var $wi = $;
 
 String.implement({

 	addQueryParameters: function(additionalParameters) {
		if(additionalParameters) {
			return this + (this.match(/\?/) ? '&' : '?') + additionalParameters;
		} else {
			return this;
		}
 	},
 	
 	blank: function() {
 		return this.trim().length === 0;
 	}

 });
 
var MTAjaxInPlace = {

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

var MTAIP = MTAjaxInPlace;
 
 
var MTAjaxOptions = {

	defaultOptions: function(additionalOptions) {
		var options = {
			method: 'get',
			async: true,
			evalScripts: true
		};
		
		return Object.merge(options, additionalOptions);
		
	}

}

var MTAjaxUpdateContainer = {

	registerPeriodic: function(id, canStop, stopped, options) {
		
		var el = $(id);
		var url = el.get('data-updateUrl');
		var updater;
		if(!canStop) {

			var merged = Object.merge(MTAjaxOptions.defaultOptions(options), {
				update: el,
				url: url
			});

			if(! merged.delay && options.frequency) {
				merged.delay = options.frequency * 1000;
			}
			if(! merged.initialDelay && options.frequency) {
				merged.initialDelay = merged.delay;
			}			
			updater = new Request.HTML(merged).startTimer();

		}
		
	},
	
	insertionFunc: function() {},
	
	register : function(id, options) {
		if(!options) options = {},
		eval(id + "Update = function() { MTAjaxUpdateContainer.update(id, options); }");
	},
	
	update: function(id, options) {

		var updateElement = $(id);

		if(updateElement == null) {
			alert('There is no element on this page with the id "' + id + '".');
		}
		
		var actionUrl = updateElement.get('data-updateUrl');
		if (options && options['_r']) {
			actionUrl = actionUrl.addQueryParameters('_r='+ id);
		}
		else {
			actionUrl = actionUrl.addQueryParameters('_u='+ id);
		}
		actionUrl = actionUrl.addQueryParameters(new Date().getTime());

//		new Ajax.Updater(id, actionUrl, AjaxOptions.defaultOptions(options));
		new Request.HTML(Object.merge(MTAjaxOptions.defaultOptions(options), {
			update : $(id),
			url : actionUrl
		})).send();
	
	}

}

var MTAUC = MTAjaxUpdateContainer;

var MTAjaxUpdateLink = {
	
	updateFunc: function(id, options, elementID) {
		var updateFunction = function(queryParams) {
			MTAjaxUpdateLink.update(id, options, elementID, queryParams);
		}
		return updateFunction;
	},
	
	update: function(id, options, elementID, queryParams) {
		var updateElement = $(id);
		if(updateElement == null) {
			alert('There is no element on this page with the id "' + id + '".');
		}
		MTAjaxUpdateLink._update(id, updateElement.get('data-updateUrl'), options, elementID, queryParams);
	},
	
	_update: function(id, actionUrl, options, elementID, queryParams) {
		
		if(elementID) {
			actionUrl = actionUrl.replace(/[^\/]+$/, elementID);
		}

		options.url = actionUrl;
		options.update = $(id);
		options.__updateID = id;
		options.noCache = true;

		if(options && options['_r']) {
			options._r = id;
		}
		else {
			options._u = id;
		}
		
		new Request.HTML(MTAjaxOptions.defaultOptions(options)).send(queryParams);
	
	},
	
	request: function() {

		if(actionUrl, options, elementID, queryParams) {
			actionUrl = actionUrl.replace(/[^\/]+$/, elementID);
		}

		options = Object.merge(options, queryParams.parseQueryString());
		options.url = actionUrl;
		
		new Request(MTAjaxOptions.defaultOptions(options)).send();
	
	}

}

var MTAUL = MTAjaxUpdateLink;

var MTAjaxSubmitButton = {

	PartialFormSenderIDKey: '_partialSenderID',
	AjaxSubmitButtonNameKey: 'AJAX_SUBMIT_BUTTON_NAME',
	defaultOptions: function(additionalOptions) {
		var options = MTAjaxOptions.defaultOptions(additionalOptions);
		options.method = 'post';
		options.noCache = true;
		return options;
	},
	
	generateActionUrl: function(id, form, queryParams, options) {

		var actionUrl = form.action;
		
		if(queryParams != null) {
			actionUrl = actionUrl.addQueryParameters(queryParams);
		}		
		
		actionUrl = actionUrl.replace('/wo/', '/ajax/');
		
		if(id != null) {
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

		if(options != null) {
			processedOptions = Object.merge(new Object(), options);
			var ajaxSubmitButtonName = processedOptions._asbn;
			if(ajaxSubmitButtonName != null) {
				processedOptions._asbn = null;
				var parameters = processedOptions.parameters;
				if(parameters === undefined || parameters == null) {
					var formSerializer = processedOptions._fs;
					var serializedForm = $(form).toQueryString();
					processedOptions.parameters = serializedForm + 
							'&' + MTAjaxSubmitButton.AjaxSubmitButtonNameKey + '=' + ajaxSubmitButtonName;
					
				} else {
					processedOptions.parameters = parameters + 
							'&' + MTAjaxSubmitButton.AjaxSubmitButtonNameKey + '=' + ajaxSubmitButtonName;
				}
			}
		}

		processedOptions = MTAjaxSubmitButton.defaultOptions(processedOptions);
		return processedOptions;
		
	},
	
	partial: function(updateContainerID, formFieldID, options) {

		var optionsCopy = Object.merge(new Object(), options);
		var formField = $(formFieldID);
		var form = formField.form;

		var queryParams = {};
		queryParams[formField.name] = formField.get('value');
		queryParams[MTAjaxSubmitButton.PartialFormSenderIDKey] = formField.name;
		optionsCopy['parameters'] = Object.toQueryString(queryParams);
		

		if(updateContainerID == null) {
			MTAjaxSubmitButton.request(form, null, optionsCopy);
		} else {
			MTAjaxSubmitButton.update(updateContainerID, form, null, optionsCopy);
		}

	},
	
	preventEnterKeySubmit : function() {
		$$('.m-a-s-b').each(function(el) {
			$(el.form).addEvent('keydown', function(e) {
				if(e.key == 'enter' && e.target.tagName != 'SELECT') {
					e.preventDefault();
				}
			});			
		});	
	},

	update : function(id, form, queryParams, options) {
		var updateElement = $(id);
		if(updateElement == null) {
			alert('There is no element on this page with the id "' + id + '".');
		}
		var finalUrl = MTAjaxSubmitButton.generateActionUrl(id, form, queryParams,options);
		var finalOptions = MTAjaxSubmitButton.processOptions(form, options);
		new Request.HTML(Object.merge({
				update : id,
				url : finalUrl
		}, finalOptions)).send(finalOptions.parameters);
		
	},

	request : function(form, queryParams, options) {
		
		var finalUrl = MTAjaxSubmitButton.generateActionUrl(null, form, queryParams, options);
		var finalOptions = MTAjaxSubmitButton.processOptions(form, options);

		new Request.HTML(Object.merge({
				url : finalUrl
		}, finalOptions)).send(finalOptions.parameters);
		
	},	

	observeDescendentFields : function(updateContainerID, containerID, observeFieldFrequency, partial, observeDelay, options) {
		$(containerID).getChildren().each(function(element) {
			if(element.type != 'hidden' && ['input', 'select', 'textarea'].contains(element.tagName.toLowerCase())){
				MTAjaxSubmitButton.observeField(updateContainerID, element, observeFieldFrequency, partial, observeDelay, options);
			}
		});
	},

	observeField: function(updateContainerID, formFieldID, observeFieldFrequency, partial, observeDelay, options) {

		var submitFunction;

		if(partial) {
			submitFunction = function(element, value) {
				if(!options.onBeforeSubmit || options.onBeforeSubmit(formFieldID)) {
					MTASB.partial(updateContainerID, formFieldID, options);
				}
			}
		} else if (updateContainerID != null) {
			submitFunction = function(element, value) {
				if(!options.onBeforeSubmit || options.onBeforeSubmit(formFieldID)) {
					MTASB.update(updateContainerID, $(formFieldID).form, null, options);
				}
			}
		} else {
			submitFunction = function(element, value) {
				if (!options.onBeforeSubmit || options.onBeforeSubmit(formFieldID)) {
					MTASB.request($(formFieldID).form, null, options);
				}
			}

		}

		if(observeDelay) {
			var delayer = new MTAjaxObserveDelayer(observeDelay, submitFunction);
			submitFunction = delayer.valueChanged.bind(delayer);
		}

		$(formFieldID).addEvent('change', submitFunction);

	}

};


var MTASB = MTAjaxSubmitButton;

var MTAjaxObserveDelayer = new Class({
	
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

var MTAjaxDraggable = new Class({

	Implements: [Options, Events],

	options: {

		draggableKeyName: '',
		draggableKeyName: null,
		droppableElementID: null,

		grid: 0,
		handle: null,
		invert: false,
		limit: false,
		modifiers: {x: 'left', y: 'top'},
		style: true,
		snap: 6,
		unit: 'px',
		preventDefault: false,
		stopPropagation: false,
		container: null,
		droppables: '',
		precalculate: false,
		includeMargins: true,
		checkDroppables: true,

		ghost: false,
		ghostOpacity: 0.7,

		id:null,
		useSpinner: false
		/* 
			beforeStart: function(draggable) {},
			onStart: function(draggable) {},
			onSnap: function(draggable) {},
			onDrag: function(draggable) {},
			onComplete: function(draggable) {},
			onCancel: function(draggable) {},
			afterDrop: function(element, droppables) {},
			onDrop: function(element, droppables) {},
			onEnter: function(element, droppables) {},
			onLeave: function(element, droppables) {},
		 */
	},
	
	initialize : function(updateContainerID, options) {
		Object.merge(this.options, options);
		this.setOptions(options);
		this.element = $(this.options.id);
		this.updateContainer = $(updateContainerID);
		var draggableContainer;
		var draggableContainerName = 'draggable_' + this.options.id;
		var draggableContainerType = eval("typeof " + draggableContainerName);

		if (draggableContainerType != 'undefined') {
			eval(draggableContainerName).destroy();
		}		
		if(this.element == null) {
			alert("Please supply the draggable element's id.");		
		} else {

			if(this.options.ghost) {
				
				this.element.addEvent('mousedown', function(event) {
					var clone = this.element.clone().setStyles(this.element.getCoordinates())
					.setStyles({opacity: this.options.ghostOpacity, position: 'absolute'}).inject(document.body);
					draggableContainer = clone;
					var drag = new Drag.Move(clone, Object.merge(this.options, { onDrop: this._onDrop.bind(this), onEnter: this.enter.bind(this), onLeave: this.leave.bind(this) }));
					drag.start(event);							

				}.bind(this));

			} else {
				draggableContainer = element;
				element.makeDraggable(this.options);
			}

			eval(draggableContainerName + "=draggableContainer");

		}

	}, 
	
	_onDrop: function(element, droppable) {

		element = $(element);
		if(droppable) {
		
			var draggableID = element.get('draggableID');
			var droppedAreaID = droppable.get('id');

			if(draggableID == null)
				draggableID = element.get('id');
				
			var data = this.options.draggableKeyName + '=' + draggableID + "&" + "dropAreaID=" + droppedAreaID;

			if(this.updateContainer == null) {
				if(this.form) {
					
				}
			} else {
				if(this.options.form) {
					
				} else {
					MTAUL.update(this.updateContainer, {}, this.options.contextID + '.' + this.options.elementID, data);
				}
			}
		}

		element.destroy();
		this.drop(element, droppable);

	},
	drop: function(element, droppable) {
		this.fireEvent("drop", [element, droppable]);
	},
	enter: function(element, droppable) {
		this.fireEvent("enter", [element, droppable]);
	},
	
	leave: function(element, droppable) {
		this.fireEvent("leave", [element, droppable]);
	}
	
});

var MTAD = MTAjaxDraggable;

var MTAjaxUtils = {
	toggleClassName: function(element, className, toggled) {
		element = document.id(element);
		if (toggled) {
			element.addClass(className);
		}
		else {
			element.removeClass(className);
		}
	},
	
	decode: function(input) {
		var e = document.createElement('div');
		e.innerHTML = input;
		return e.childNodes.length === 0 ? "" : e.childNodes[0].nodeValue;	
	}

	
};


