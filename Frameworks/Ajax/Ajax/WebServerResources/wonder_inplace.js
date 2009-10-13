/*
 * InPlaceEditor extension that adds a 'click to edit' text when the field is 
 * empty.
 * See http://codetocustomer.com/blog/2008/06/empty-text-for-ajaxinplaceeditor (also see the comments)
 */
if (typeof Ajax != 'undefined' && typeof Ajax.InPlaceEditor != 'undefined') {
  Ajax.InPlaceEditorWithEmptyText = Class.create(Ajax.InPlaceEditor, {
    
    initialize: function($super, element, url, options) {
      var newOptions = Object.extend({
        valueWhenEmpty: 'click to edit...',
        emptyClassName: 'inplaceeditor-empty'
      }, options || {});
      $super(element, url, newOptions);
      this.checkEmpty();
    },
    
    checkEmpty: function() {
      if(this.element.innerHTML.length == 0 && this.options.valueWhenEmpty){
        this.element.appendChild(
            new Element("span", {className:this.options.emptyClassName}).update(this.options.valueWhenEmpty)
        );
      }
    },
    
    getText: function($super) {
      if(empty_span = this.element.select("." + this.options.emptyClassName).first()) {
        empty_span.remove();
      }
      return $super();
    },
    
    leaveEditMode : function($super, transport) {
      var retval = $super(transport);
      this.checkEmpty();
      return retval;
    }
    
  });
}
