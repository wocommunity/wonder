// ==========================================================================
// Photos.PhotoController
// ==========================================================================

require('core');

/** @class

  (Document Your View Here)

  @extends SC.Object
  @author    AuthorName  
  @version 0.1
  @static
*/
Photos.photoController = SC.ObjectController.create(
/** @scope Photos.photoController */ {

  contentBinding: 'Photos.detailController.selection',

  // Most of the effects here are only good on Safari.  Disable on other 
  // browsers.
  canShowEffects: (SC.Platform.Safari > 0),
  
  pickerIsVisible: NO,
  
  // called from the action method in the photo grid view.  Only shows a 
  // picker if we have valid content.  Since the content bindings default 
  // is to a single value, this will only work if you have one photo selected.
  showPicker: function(sourceView, evt) {
    if (this.get('content')) {
      SC.page.get('photoPicker').popup(sourceView, evt) ;
    }
  },
  
  hidePicker: function() {
    SC.page.get('photoPicker').set('isVisible', NO) ;
  }
  
}) ;
