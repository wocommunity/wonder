var TagField = Class.create();
TagField.prototype = {
	typedTags: [],
	typingTagNum: -1,

	availableTags: [],
	suggestedTags: [],
	suggestionsElement: [],
	
	tagElements: {},
	
	textElement: null,
	tagsElement: null,
	
  initialize: function(textElement, tagsElement) {
  	this.textElement = $(textElement);
  	this.tagsElement = $(tagsElement);
  	this.lastValue = this.textElement.value;
		Event.observe(this.textElement, "keypress", this.keyPress.bindAsEventListener(this));
		Event.observe(this.textElement, "keyup", this.keyUp.bindAsEventListener(this));
		
		this.processTyping();
		this.updateHighlights();
	},
		
	keyPress : function(e) {
		if (e.keyCode == Event.KEY_TAB) {
			if (this.addSuggestedTag()) {
				Event.stop(e);
			}
		}
	},
	
	keyUp : function(e) {
		if (e.keyCode == Event.KEY_TAB) {
			//this.replaceTagWithSuggestion();
		}
		else {
			this.processTyping();
		}
	},
	
	toggleTag: function(tag) {
		if (!this.isTagUsed(tag)) {
			var typingTagNum = this.typingTagNum;
			if (typingTagNum == -1) {
				typingTagNum = this.typedTags.length;
				this.typedTags.push(tag);
			}
			else {
				this.typedTags[this.typingTagNum] = tag;
			}
		}
		else {
			var normalizedTag = this.normalizeTag(tag);
			var newTypedTags = [];
			for (var tagNum = 0; tagNum < this.typedTags.length; tagNum ++) {
				if (this.normalizeTag(this.typedTags[tagNum]) != normalizedTag) {
					newTypedTags.push(this.typedTags[tagNum]);
				}
			}
			this.typedTags = newTypedTags;
		}
		var value = this.typedTags.join(' ').strip();
		if (value.length > 0) {
			value += ' ';
		}
		this.textElement.value = value;
		this.processTyping();
		
	},
	
	addSuggestedTag: function() {
		var tagAdded = false;
		if (this.typingTagNum != -1 && this.suggestedTags.length > 0) {
			this.toggleTag(this.suggestedTags[0]);
			tagAdded = true;
		}
		return tagAdded;
	},
	
	processTyping: function() {
		var value = this.textElement.value;
		var typingTags = value.split(' ');
		var typingTagNum = -1;

		for (var tagNum = 0; tagNum < typingTags.length && tagNum < this.typedTags.length; tagNum ++) {
			if (typingTags[typingTags.length - tagNum - 1] != this.typedTags[this.typedTags.length - tagNum - 1]) {
				typingTagNum = typingTags.length - tagNum - 1;
				break;
			}
		}
		
		if (typingTagNum == -1 && typingTags.length > 0) {
			typingTagNum = typingTags.length - 1;
		}
		
		this.typedTags = typingTags;
		this.typingTagNum = typingTagNum;
		
		var suggestedTags = [];
		if (typingTagNum != -1) {
			var typingTag = typingTags[typingTagNum];
			suggestedTags = this.suggestedTagsForPartialTag(typingTag);
		}
		this.suggestedTags = suggestedTags;

		this.updateHighlights();
	},

	normalizeTag: function(tag) {
		return tag == null ? null : tag.toLowerCase().strip();
	},
	
	updateHighlights: function() {
		for (var tagNum = 0; tagNum < this.availableTags.length; tagNum ++) {
			var tag = this.availableTags[tagNum];
			var tagElement = this.tagElements[tag];
			if (this.isTagUsed(tag)) {
				tagElement.addClassName('used');
			}
			else {
				tagElement.removeClassName('used');
			}
			
			if (this.isTagSuggested(tag)) {
				tagElement.addClassName('suggested');
			}
			else {
				tagElement.removeClassName('suggested');
			}
		}
		
		if (this.suggestedTags.length > 0) {
			this.tagsElement.addClassName('suggestions');
		}
		else {
			this.tagsElement.removeClassName('suggestions');
		}
	},

	isTagUsed: function(tag) {
		var tagUsed = false;
		var normalizedTag = this.normalizeTag(tag);
		for (var usedTagNum = 0; usedTagNum < this.typedTags.length; usedTagNum ++) {
			if (this.normalizeTag(this.typedTags[usedTagNum]) == normalizedTag) {
				tagUsed = true;
				break;
			}
		}
		return tagUsed;
	},

	isTagSuggested: function(tag) {
		var tagSuggested = false;
		var normalizedTag = this.normalizeTag(tag);
		for (var tagNum = 0; tagNum < this.suggestedTags.length; tagNum ++) {
			if (this.normalizeTag(this.suggestedTags[tagNum]) == normalizedTag) {
				tagSuggested = true;
				break;
			}
		}
		return tagSuggested;
	},
			
	suggestedTagsForPartialTag: function(partialTag) {
		var suggestedTags = [];
		var normalizedTag = this.normalizeTag(partialTag);
		if (normalizedTag.length > 0) {
			for (var availableTagNum = 0; availableTagNum < this.availableTags.length; availableTagNum ++) {
				var tag = this.availableTags[availableTagNum];
				if (this.normalizeTag(tag).search(normalizedTag) == 0 && !this.isTagUsed(tag)) {
					suggestedTags.push(tag);
				} 
			}
		}
		return suggestedTags;
	},
	
	displayTagsInElement: function(tags, element) {
		var tagElements = {};
		var containerElement = $(element);
		if (containerElement != null) {
			containerElement.innerHTML = '';
			tags.each(function(tag, i) {
		  	var tagElement = document.createElement("a");
		  	tagElement.href = 'javascript:void(0)';
		  	tagElement.innerHTML = tag;
				Event.observe(tagElement, "click", function() { this.toggleTag(tag); }.bindAsEventListener(this));
		  	containerElement.appendChild(tagElement);
		  	tagElements[tag] = tagElement;
			}.bind(this));
		}
		return tagElements;
	},
	
	add: function(tag) {
		this.availableTags.push(tag);
		this.tagElements = this.displayTagsInElement(this.availableTags, this.tagsElement);
		this.updateHighlights();
	}, 
	
	addAll: function(tags) {
		for (var tagNum = 0; tagNum < tags.length; tagNum ++) {
			this.availableTags.push(tags[tagNum]);
		}
		this.tagElements = this.displayTagsInElement(this.availableTags, this.tagsElement);
		this.updateHighlights();
	} 
}