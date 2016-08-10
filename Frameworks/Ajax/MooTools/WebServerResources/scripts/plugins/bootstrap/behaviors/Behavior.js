
// MooTools: the javascript framework.
// Load this file's selection again by visiting: http://mootools.net/more/7484fe5b0f7c444957a2413054e72008 
// Or build this file again with packager using: packager build More/Table
/*
---
name: Table
description: LUA-Style table implementation.
license: MIT-style license
authors:
  - Valerio Proietti
requires: [Core/Array]
provides: [Table]
...
*/

(function(){

var Table = this.Table = function(){

	this.length = 0;
	var keys = [],
	    values = [];
	
	this.set = function(key, value){
		var index = keys.indexOf(key);
		if (index == -1){
			var length = keys.length;
			keys[length] = key;
			values[length] = value;
			this.length++;
		} else {
			values[index] = value;
		}
		return this;
	};

	this.get = function(key){
		var index = keys.indexOf(key);
		return (index == -1) ? null : values[index];
	};

	this.erase = function(key){
		var index = keys.indexOf(key);
		if (index != -1){
			this.length--;
			keys.splice(index, 1);
			return values.splice(index, 1)[0];
		}
		return null;
	};

	this.each = this.forEach = function(fn, bind){
		for (var i = 0, l = this.length; i < l; i++) fn.call(bind, keys[i], values[i], this);
	};
	
};

if (this.Type) new Type('Table', Table);

})();


// Begin: Source/BehaviorAPI.js
/*
---
name: BehaviorAPI
description: HTML getters for Behavior's API model.
requires: [Core/Class, /Element.Data]
provides: [BehaviorAPI]
...
*/


(function(){
	//see Docs/BehaviorAPI.md for documentation of public methods.

	var reggy = /[^a-z0-9\-]/gi;

	window.BehaviorAPI = new Class({
		element: null,
		prefix: '',
		defaults: {},

		initialize: function(element, prefix){
			this.element = element;
			this.prefix = prefix.toLowerCase().replace('.', '-', 'g').replace(reggy, '');
		},

		/******************
		 * PUBLIC METHODS
		 ******************/

		get: function(/* name[, name, name, etc] */){
			if (arguments.length > 1) return this._getObj(Array.from(arguments));
			return this._getValue(arguments[0]);
		},

		getAs: function(/*returnType, name, defaultValue OR {name: returnType, name: returnType, etc}*/){
			if (typeOf(arguments[0]) == 'object') return this._getValuesAs.apply(this, arguments);
			return this._getValueAs.apply(this, arguments);
		},

		require: function(/* name[, name, name, etc] */){
			for (var i = 0; i < arguments.length; i++){
				if (this._getValue(arguments[i]) == undefined) throw new Error('Could not retrieve ' + this.prefix + '-' + arguments[i] + ' option from element.');
			}
			return this;
		},

		requireAs: function(returnType, name /* OR {name: returnType, name: returnType, etc}*/){
			var val;
			if (typeOf(arguments[0]) == 'object'){
				for (var objName in arguments[0]){
					val = this._getValueAs(arguments[0][objName], objName);
					if (val === undefined || val === null) throw new Error("Could not retrieve " + this.prefix + '-' + objName + " option from element.");
				}
			} else {
				val = this._getValueAs(returnType, name);
				if (val === undefined || val === null) throw new Error("Could not retrieve " + this.prefix + '-' + name + " option from element.");
			}
			return this;
		},

		setDefault: function(name, value /* OR {name: value, name: value, etc }*/){
			if (typeOf(arguments[0]) == 'object'){
				for (var objName in arguments[0]){
					this.setDefault(objName, arguments[0][objName]);
				}
				return;
			}
			name = name.camelCase();
			this.defaults[name] = value;
			if (this._getValue(name) == null){
				var options = this._getOptions();
				options[name] = value;
			}
			return this;
		},

		refreshAPI: function(){
			delete this.options;
			this.setDefault(this.defaults);
			return;
		},

		/******************
		 * PRIVATE METHODS
		 ******************/

		//given an array of names, returns an object of key/value pairs for each name
		_getObj: function(names){
			var obj = {};
			names.each(function(name){
				var value = this._getValue(name);
				if (value !== undefined) obj[name] = value;
			}, this);
			return obj;
		},
		//gets the data-behaviorname-options object and parses it as JSON
		_getOptions: function(){
			if (!this.options){
				var options = this.element.getData(this.prefix + '-options', '{}');
				if (options && options.substring(0,1) != '{') options = '{' + options + '}';
				var isSecure = JSON.isSecure(options);
				if (!isSecure) throw new Error('warning, options value for element is not parsable, check your JSON format for quotes, etc.');
				this.options = isSecure ? JSON.decode(options) : {};
				for (option in this.options) {
					this.options[option.camelCase()] = this.options[option];
				}
			}
			return this.options;
		},
		//given a name (string) returns the value for it
		_getValue: function(name){
			name = name.camelCase();
			var options = this._getOptions();
			if (!options.hasOwnProperty(name)){
				var inline = this.element.getData(this.prefix + '-' + name.hyphenate());
				if (inline) options[name] = inline;
			}
			return options[name];
		},
		//given a Type and a name (string) returns the value for it coerced to that type if possible
		//else returns the defaultValue or null
		_getValueAs: function(returnType, name, defaultValue){
			var value = this._getValue(name);
			if (value == null || value == undefined) return defaultValue;
			var coerced = this._coerceFromString(returnType, value);
			if (coerced == null) throw new Error("Could not retrieve value '" + name + "' as the specified type. Its value is: " + value);
			return coerced;
		},
		//given an object of name/Type pairs, returns those as an object of name/value (as specified Type) pairs
		_getValuesAs: function(obj){
			var returnObj = {};
			for (var name in obj){
				returnObj[name] = this._getValueAs(obj[name], name);
			}
			return returnObj;
		},
		//attempts to run a value through the JSON parser. If the result is not of that type returns null.
		_coerceFromString: function(toType, value){
			if (typeOf(value) == 'string' && toType != String){
				if (JSON.isSecure(value)) value = JSON.decode(value);
			}
			if (instanceOf(value, toType)) return value;
			return null;
		}
	});

})();


// Begin: Source/Behavior.js
/*
---
name: Behavior
description: Auto-instantiates widgets/classes based on parsed, declarative HTML.
requires: [Core/Class.Extras, Core/Element.Event, Core/Selectors, More/Table, /Element.Data, /BehaviorAPI]
provides: [Behavior]
...
*/

(function(){

	var getLog = function(method){
		return function(){
			if (window.console && console[method]){
				if(console[method].apply) console[method].apply(console, arguments);
				else console[method](Array.from(arguments).join(' '));
			}
		};
	};

	var PassMethods = new Class({
		//pass a method pointer through to a filter
		//by default the methods for add/remove events are passed to the filter
		//pointed to this instance of behavior. you could use this to pass along
		//other methods to your filters. For example, a method to close a popup
		//for filters presented inside popups.
		passMethod: function(method, fn){
			if (this.API.prototype[method]) throw new Error('Cannot overwrite API method ' + method + ' as it already exists');
			this.API.implement(method, fn);
			return this;
		},

		passMethods: function(methods){
			for (method in methods) this.passMethod(method, methods[method]);
			return this;
		}

	});

	var spaceOrCommaRegex = /\s*,\s*|\s+/g;

	BehaviorAPI.implement({
		deprecate: function(deprecated, asJSON){
			var set,
			    values = {};
			Object.each(deprecated, function(prop, key){
				var value = this.element[ asJSON ? 'getJSONData' : 'getData'](prop);
				if (value !== undefined){
					set = true;
					values[key] = value;
				}
			}, this);
			this.setDefault(values);
			return this;
		}
	});

	this.Behavior = new Class({

		Implements: [Options, Events, PassMethods],

		options: {
			//by default, errors thrown by filters are caught; the onError event is fired.
			//set this to *true* to NOT catch these errors to allow them to be handled by the browser.
			// breakOnErrors: false,
			// container: document.body,

			//default error behavior when a filter cannot be applied
			onError: getLog('error'),
			onWarn: getLog('warn'),
			enableDeprecation: true,
			selector: '[data-behavior]'
		},

		initialize: function(options){
			this.setOptions(options);
			this.API = new Class({ Extends: BehaviorAPI });
			this.passMethods({
				addEvent: this.addEvent.bind(this),
				removeEvent: this.removeEvent.bind(this),
				addEvents: this.addEvents.bind(this),
				removeEvents: this.removeEvents.bind(this),
				fireEvent: this.fireEvent.bind(this),
				applyFilters: this.apply.bind(this),
				applyFilter: this.applyFilter.bind(this),
				getContentElement: this.getContentElement.bind(this),
				cleanup: this.cleanup.bind(this),
				getContainerSize: function(){
					return this.getContentElement().measure(function(){
						return this.getSize();
					});
				}.bind(this),
				error: function(){ this.fireEvent('error', arguments); }.bind(this),
				fail: function(){
					var msg = Array.join(arguments, ' ');
					throw new Error(msg);
				},
				warn: function(){
					this.fireEvent('warn', arguments);
				}.bind(this)
			});
		},

		getContentElement: function(){
			return this.options.container || document.body;
		},

		//Applies all the behavior filters for an element.
		//container - (element) an element to apply the filters registered with this Behavior instance to.
		//force - (boolean; optional) passed through to applyFilter (see it for docs)
		apply: function(container, force){
		  this._getElements(container).each(function(element){
				var plugins = [];
				element.getBehaviors().each(function(name){
					var filter = this.getFilter(name);
					if (!filter){
						this.fireEvent('error', ['There is no filter registered with this name: ', name, element]);
					} else {
						var config = filter.config;
						if (config.delay !== undefined){
							this.applyFilter.delay(filter.config.delay, this, [element, filter, force]);
						} else if(config.delayUntil){
							this._delayFilterUntil(element, filter, force);
						} else if(config.initializer){
							this._customInit(element, filter, force);
						} else {
							plugins.append(this.applyFilter(element, filter, force, true));
						}
					}
				}, this);
				plugins.each(function(plugin){ plugin(); });
			}, this);
			return this;
		},

		_getElements: function(container){
			if (typeOf(this.options.selector) == 'function') return this.options.selector(container);
			else return document.id(container).getElements(this.options.selector);
		},

		//delays a filter until the event specified in filter.config.delayUntil is fired on the element
		_delayFilterUntil: function(element, filter, force){
			var events = filter.config.delayUntil.split(','),
			    attached = {},
			    inited = false;
			var clear = function(){
				events.each(function(event){
					element.removeEvent(event, attached[event]);
				});
				clear = function(){};
			};
			events.each(function(event){
				var init = function(e){
					clear();
					if (inited) return;
					inited = true;
					var setup = filter.setup;
					filter.setup = function(element, api, _pluginResult){
						api.event = e;
						return setup.apply(filter, [element, api, _pluginResult]);
					};
					this.applyFilter(element, filter, force);
					filter.setup = setup;
				}.bind(this);
				element.addEvent(event, init);
				attached[event] = init;
			}, this);
		},

		//runs custom initiliazer defined in filter.config.initializer
		_customInit: function(element, filter, force){
			var api = new this.API(element, filter.name);
			api.runSetup = this.applyFilter.pass([element, filter, force], this);
			filter.config.initializer(element, api);
		},

		//Applies a specific behavior to a specific element.
		//element - the element to which to apply the behavior
		//filter - (object) a specific behavior filter, typically one registered with this instance or registered globally.
		//force - (boolean; optional) apply the behavior to each element it matches, even if it was previously applied. Defaults to *false*.
		//_returnPlugins - (boolean; optional; internal) if true, plugins are not rendered but instead returned as an array of functions
		//_pluginTargetResult - (obj; optional internal) if this filter is a plugin for another, this is whatever that target filter returned
		//                      (an instance of a class for example)
		applyFilter: function(element, filter, force, _returnPlugins, _pluginTargetResult){
			var pluginsToReturn = [];
			if (this.options.breakOnErrors){
				pluginsToReturn = this._applyFilter.apply(this, arguments);
			} else {
				try {
					pluginsToReturn = this._applyFilter.apply(this, arguments);
				} catch (e){
					this.fireEvent('error', ['Could not apply the behavior ' + filter.name, e]);
				}
			}
			return _returnPlugins ? pluginsToReturn : this;
		},

		//see argument list above for applyFilter
		_applyFilter: function(element, filter, force, _returnPlugins, _pluginTargetResult){
			var pluginsToReturn = [];
			element = document.id(element);
			//get the filters already applied to this element
			var applied = getApplied(element);
			//if this filter is not yet applied to the element, or we are forcing the filter
			if (!applied[filter.name] || force){
				//if it was previously applied, garbage collect it
				if (applied[filter.name]) applied[filter.name].cleanup(element);
				var api = new this.API(element, filter.name);

				//deprecated
				api.markForCleanup = filter.markForCleanup.bind(filter);
				api.onCleanup = function(fn){
					filter.markForCleanup(element, fn);
				};

				if (filter.config.deprecated && this.options.enableDeprecation) api.deprecate(filter.config.deprecated);
				if (filter.config.deprecateAsJSON && this.options.enableDeprecation) api.deprecate(filter.config.deprecatedAsJSON, true);

				//deal with requirements and defaults
				if (filter.config.requireAs){
					api.requireAs(filter.config.requireAs);
				} else if (filter.config.require){
					api.require.apply(api, Array.from(filter.config.require));
				}

				if (filter.config.defaults) api.setDefault(filter.config.defaults);

				//apply the filter
				var result = filter.setup(element, api, _pluginTargetResult);
				if (filter.config.returns && !instanceOf(result, filter.config.returns)){
					throw new Error("Filter " + filter.name + " did not return a valid instance.");
				}
				element.store('Behavior Filter result:' + filter.name, result);
				//and mark it as having been previously applied
				applied[filter.name] = filter;
				//apply all the plugins for this filter
				var plugins = this.getPlugins(filter.name);
				if (plugins){
					for (var name in plugins){
						if (_returnPlugins){
							pluginsToReturn.push(this.applyFilter.pass([element, plugins[name], force, null, result], this));
						} else {
							this.applyFilter(element, plugins[name], force, null, result);
						}
					}
				}
			}
			return pluginsToReturn;
		},

		//given a name, returns a registered behavior
		getFilter: function(name){
			return this._registered[name] || Behavior.getFilter(name);
		},

		getPlugins: function(name){
			return this._plugins[name] || Behavior._plugins[name];
		},

		//Garbage collects all applied filters for an element and its children.
		//element - (*element*) container to cleanup
		//ignoreChildren - (*boolean*; optional) if *true* only the element will be cleaned, otherwise the element and all the
		//	  children with filters applied will be cleaned. Defaults to *false*.
		cleanup: function(element, ignoreChildren){
			element = document.id(element);
			var applied = getApplied(element);
			for (var filter in applied){
				applied[filter].cleanup(element);
				element.eliminate('Behavior Filter result:' + filter);
				delete applied[filter];
			}
			if (!ignoreChildren) this._getElements(element).each(this.cleanup, this);
			return this;
		}

	});

	//Export these for use elsewhere (notabily: Delegator).
	Behavior.getLog = getLog;
	Behavior.PassMethods = PassMethods;


	//Returns the applied behaviors for an element.
	var getApplied = function(el){
		return el.retrieve('_appliedBehaviors', {});
	};

	//Registers a behavior filter.
	//name - the name of the filter
	//fn - a function that applies the filter to the given element
	//overwrite - (boolean) if true, will overwrite existing filter if one exists; defaults to false.
	var addFilter = function(name, fn, overwrite){
		if (!this._registered[name] || overwrite) this._registered[name] = new Behavior.Filter(name, fn);
		else throw new Error('Could not add the Behavior filter "' + name  +'" as a previous trigger by that same name exists.');
	};

	var addFilters = function(obj, overwrite){
		for (var name in obj){
			addFilter.apply(this, [name, obj[name], overwrite]);
		}
	};

	//Registers a behavior plugin
	//filterName - (*string*) the filter (or plugin) this is a plugin for
	//name - (*string*) the name of this plugin
	//setup - a function that applies the filter to the given element
	var addPlugin = function(filterName, name, setup, overwrite){
		if (!this._plugins[filterName]) this._plugins[filterName] = {};
		if (!this._plugins[filterName][name] || overwrite) this._plugins[filterName][name] = new Behavior.Filter(name, setup);
		else throw new Error('Could not add the Behavior filter plugin "' + name  +'" as a previous trigger by that same name exists.');
	};

	var addPlugins = function(obj, overwrite){
		for (var name in obj){
			addPlugin.apply(this, [obj[name].fitlerName, obj[name].name, obj[name].setup], overwrite);
		}
	};

	var setFilterDefaults = function(name, defaults){
		var filter = this.getFilter(name);
		if (!filter.config.defaults) filter.config.defaults = {};
		Object.append(filter.config.defaults, defaults);
	};

	//Add methods to the Behavior namespace for global registration.
	Object.append(Behavior, {
		_registered: {},
		_plugins: {},
		addGlobalFilter: addFilter,
		addGlobalFilters: addFilters,
		addGlobalPlugin: addPlugin,
		addGlobalPlugins: addPlugins,
		setFilterDefaults: setFilterDefaults,
		getFilter: function(name){
			return this._registered[name];
		}
	});
	//Add methods to the Behavior class for instance registration.
	Behavior.implement({
		_registered: {},
		_plugins: {},
		addFilter: addFilter,
		addFilters: addFilters,
		addPlugin: addPlugin,
		addPlugins: addPlugins,
		setFilterDefaults: setFilterDefaults
	});

	//This class is an actual filter that, given an element, alters it with specific behaviors.
	Behavior.Filter = new Class({

		config: {
			/**
				returns: Foo,
				require: ['req1', 'req2'],
				//or
				requireAs: {
					req1: Boolean,
					req2: Number,
					req3: String
				},
				defaults: {
					opt1: false,
					opt2: 2
				},
				//simple example:
				setup: function(element, API){
					var kids = element.getElements(API.get('selector'));
					//some validation still has to occur here
					if (!kids.length) API.fail('there were no child elements found that match ', API.get('selector'));
					if (kids.length < 2) API.warn("there weren't more than 2 kids that match", API.get('selector'));
					var fooInstance = new Foo(kids, API.get('opt1', 'opt2'));
					API.onCleanup(function(){
						fooInstance.destroy();
					});
					return fooInstance;
				},
				delayUntil: 'mouseover',
				//OR
				delay: 100,
				//OR
				initializer: function(element, API){
					element.addEvent('mouseover', API.runSetup); //same as specifying event
					//or
					API.runSetup.delay(100); //same as specifying delay
					//or something completely esoteric
					var timer = (function(){
						if (element.hasClass('foo')){
							clearInterval(timer);
							API.runSetup();
						}
					}).periodical(100);
					//or
					API.addEvent('someBehaviorEvent', API.runSetup);
				});
				*/
		},

		//Pass in an object with the following properties:
		//name - the name of this filter
		//setup - a function that applies the filter to the given element
		initialize: function(name, setup){
			this.name = name;
			if (typeOf(setup) == "function"){
				this.setup = setup;
			} else {
				Object.append(this.config, setup);
				this.setup = this.config.setup;
			}
			this._cleanupFunctions = new Table();
		},

		//Stores a garbage collection pointer for a specific element.
		//Example: if your filter enhances all the inputs in the container
		//you might have a function that removes that enhancement for garbage collection.
		//You would mark each input matched with its own cleanup function.
		//NOTE: this MUST be the element passed to the filter - the element with this filters
		//      name in its data-behavior property. I.E.:
		//<form data-behavior="FormValidator">
		//  <input type="text" name="email"/>
		//</form>
		//If this filter is FormValidator, you can mark the form for cleanup, but not, for example
		//the input. Only elements that match this filter can be marked.
		markForCleanup: function(element, fn){
			var functions = this._cleanupFunctions.get(element);
			if (!functions) functions = [];
			functions.include(fn);
			this._cleanupFunctions.set(element, functions);
			return this;
		},

		//Garbage collect a specific element.
		//NOTE: this should be an element that has a data-behavior property that matches this filter.
		cleanup: function(element){
			var marks = this._cleanupFunctions.get(element);
			if (marks){
				marks.each(function(fn){ fn(); });
				this._cleanupFunctions.erase(element);
			}
			return this;
		}

	});

	Behavior.elementDataProperty = 'behavior';

	Element.implement({

		addBehaviorFilter: function(name){
			return this.setData(Behavior.elementDataProperty, this.getBehaviors().include(name).join(' '));
		},

		removeBehaviorFilter: function(name){
			return this.setData(Behavior.elementDataProperty, this.getBehaviors().erase(name).join(' '));
		},

		getBehaviors: function(){
			var filters = this.getData(Behavior.elementDataProperty);
			if (!filters) return [];
			return filters.trim().split(spaceOrCommaRegex);
		},

		hasBehavior: function(name){
			return this.getBehaviors().contains(name);
		},

		getBehaviorResult: function(name){
			return this.retrieve('Behavior Filter result:' + name);
		}

	});


})();
