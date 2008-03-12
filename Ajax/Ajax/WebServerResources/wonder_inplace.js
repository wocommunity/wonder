if (typeof Ajax != 'undefined' && typeof Ajax.InPlaceEditor != 'undefined') {
	/*
	 * InPlaceEditor extension that adds a 'click to edit' text when the field is 
	 * empty.
	 */
	Ajax.InPlaceEditor.prototype.__initialize = Ajax.InPlaceEditor.prototype.initialize;
	Ajax.InPlaceEditor.prototype.__getText = Ajax.InPlaceEditor.prototype.getText;
	Ajax.InPlaceEditor.prototype.__onComplete = Ajax.InPlaceEditor.prototype.onComplete;
	Ajax.InPlaceEditor.prototype = Object.extend(Ajax.InPlaceEditor.prototype, {
		  initialize: function(element, url, options) {
		    	var newOptions = Object.extend(options || {}, {
	            valueWhenEmpty: 'click to edit...',
	            emptyClassName: 'inplaceeditor-empty'
	        });
	        this.__initialize(element,url,newOptions)
	        this._checkEmpty();
	    },
	    
	    _checkEmpty: function(){
	        if( this.element.innerHTML.length == 0 ){
	            this.element.appendChild(
	                Builder.node('span',{className:this.options.emptyClassName},this.options.valueWhenEmpty));
	        }
	    },
	
	    getText: function(){
	    		$A(this.element.getElementsByClassName(this.options.emptyClassName)).each(function(child){
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
