/*
---
description: A non-obtrusive image dropdown menu that extends and replaces a standard HTML Select control. 

license: MIT-style

authors:
- Lorenzo Stanco

requires:
- core/1.4.1: '*'

provides: [FancySelect]

...
*/

var FancySelect = new Class({

	Implements: [Options, Events],

	options: {
		showText: true,
		showImages: true,
		className: 'fancy-select',
		offset: { x: 0, y: 0 },
		autoHide: false,
		autoScrollWindow: false,
		animateFade: true,
		animateSlide: true,
		fx: { 'duration': 'short' }
	},

	initialize: function(element, options) {
	
		this.setOptions(options);
		/*if (!Fx.Slide)*/ this.options.animateSlide = false; // Need review
		this.element = document.id(element);
		this.element.store('fancyselect_object', this);
		this._create();
		this.attach();
		
		// Auto-scroll when FancySelect is out of viewport
		if (this.options.autoScrollWindow) this.addEvent('show', function() {
			var windowScroll = window.getScroll();
			var overflow = this.ul.getPosition().y + this.ul.getSize().y - window.getSize().y - windowScroll.y;
			if (overflow > 0) window.scrollTo(windowScroll.x, windowScroll.y + overflow + 10);
		});
		
		// Auto-hide the dropdown menu when user clicks outside
		if (this.options.autoHide) document.addEvent('click', function(e) {
			if (!this.shown) return;
			var target = document.id(e.target);
			var parents = target.getParents().include(target);
			if (!parents.contains(this.ul) && !parents.contains(this.div)) this.hide();
		}.bind(this));
		
		return this;
		
	},

	attach: function() {
		this.element.setStyle('display', 'none');
		this.select(this.element.get('value')); // Select current item
		if (Browser.ie) window.addEvent('load', function() { this.select(this.element.get('value')); }.bind(this)); // IE refresh fix
		this.ul.fade('hide').inject(document.id(document.body));
		this.div.inject(this.element, 'after');
		this.attached = true;
		this.fireEvent('attach');
		return this;
	},

	detach: function() {
		if (this.ul) this.ul.dispose();
		if (this.div) this.div.dispose();
		this.element.setStyle('display', '');
		this.attached = false;
		this.fireEvent('detach');
		return this;
	},
	
	select: function(value) {
		this.element.set('value', value); // Update hidden <select>
		if (this.options.showText) this.div.getElement('span.text').set('text', this.selectOptions[value].text);
		if (this.options.showImages) this.div.getElement('img.image').setProperties({
			'src': this.selectOptions[value].image,
			'alt': this.selectOptions[value].alt
		});
		if (this.ul) {
			this.ul.getElements('li').each(function(li) {
				if (li.getProperty('data-value') == value) li.addClass('selected');
				else li.removeClass('selected');
			});
		}
		return this;
	},
	
	update: function() {
		var attached = this.attached;
		this.detach();
		this._create(); // Re-create
		if (attached) this.attach(); // Re-attach if needed
		return this;
	},

	show: function() {
		var offset = this.options.offset;
		var position = this.div.getCoordinates();
		this.ul.setStyles({
			'top': position.top + position.height + offset.y,
			'left': position.left + offset.x });
		this._animate(false);
		this.shown = true;
		this.fireEvent('show');
		return this;
	},
	
	hide: function() {
		this._animate(true);
		this.shown = false;
		this.fireEvent('hide');
		return this;
	},
	
	toggle: function() {
		if (this.shown) return this.hide();
		else return this.show();
	},
	
	_create: function() {
		
		var o = this.options;
		
		if (this.ul) this.ul.destroy();
		if (this.div) this.div.destroy();
		
		// Create options array
		this.selectOptions = {};
		this.element.getElements('option').each(function(option) {
			var value = option.getProperty('value');
			this.selectOptions[value] = {};
			if (option.get('disabled')) this.selectOptions[value].disabled = true;
			if (o.showText) this.selectOptions[value].text = option.get('text');
			if (o.showImages) {
				this.selectOptions[value].image = option.getProperty('data-image');
				this.selectOptions[value].alt = option.getProperty('data-alt');
			}
		}.bind(this));
		
		// Create <li> elements
		this.ul = new Element('ul').addClass(o.className);
		Object.each(this.selectOptions, function(option, value) {
			var li = new Element('li', { 'data-value': value });
			if (option.disabled) li.addClass('disabled');
			if (o.showImages && option.image) li.adopt(new Element('img.image', { 'src': option.image, 'alt': option.alt }));
			if (o.showText && option.text) li.adopt(new Element('span.text', { 'text': option.text }));
			li.addEvent('click', function() { 
				if (li.hasClass('disabled')) return;
				this.select(li.getProperty('data-value')); 
				this.hide(); 
			}.bind(this));
			this.ul.adopt(li);
		}.bind(this));
		
		// Force <ul> custom positioning
		this.ul.setStyles({ position: 'absolute', top: 0, left: 0 });
		if (o.animateFade) this.ul.set('tween', o.fx);
		if (o.animateSlide) this.ul.set('slide', o.fx);
		
		// Create <div> replacement for select
		this.div = new Element('div').addClass(o.className);
		if (o.showImages) this.div.adopt(new Element('img.image'));
		if (o.showText) this.div.adopt(new Element('span.text'));
		this.div.adopt(new Element('span.arrow'));
		this.div.addEvent('click', function() { this.toggle(); }.bind(this));
		
		return this;
		
	},
	
	_animate: function(out) {
		var o = this.options;
		if (o.animateFade) this.ul.fade(out ? 'out' : 'in');
		if (o.animateSlide) this.ul.slide(out ? 'out' : 'in');
		if (!o.animateFade && !o.animateSlide) this.ul.fade(out ? 'hide' : 'show');
		return this;
	}
	
});

Elements.implement({
	
	fancySelect: function(options) {
		this.each(function(el) { new FancySelect(el, options); });
		return this;
	}
	
});

Element.implement({
	
	fancySelect: function(options) {
		new FancySelect(document.id(this), options);
		return this;
	},
	
	fancySelectShow: function() {
		var fs = this.retrieve('fancyselect_object');
		if (fs) fs.show(this);
		return this;
	},
	
	fancySelectHide: function() {
		var fs = this.retrieve('fancyselect_object');
		if (fs) fs.hide(this);
		return this;
	},
	
	fancySelectToggle: function() {
		var fs = this.retrieve('fancyselect_object');
		if (fs) fs.toggle(this);
		return this;
	}
	
});

Element.Properties.fancySelect = {
 
    get: function() {
        return this.retrieve('fancyselect_object');
    }
 
};
