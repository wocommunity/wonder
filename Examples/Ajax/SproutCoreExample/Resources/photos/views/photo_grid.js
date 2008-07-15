// ==========================================================================
// Photos.PhotoGridView
// ==========================================================================

require('core');

/** @class

  This custom collection view implements a default action to display a 
  an editor pop for the selected photo or photos.
  
  This has to be done in a subclass currently in order to get the view and
  event which are needed for the popup method.

  @extends SC.GridView
  @author    AuthorName  
  @version 0.1
*/
Photos.PhotoGridView = SC.GridView.extend(
/** @scope Photos.PhotoGridView.prototype */ {
  
  action: function(view, evt) { 
    console.log('ACTION!') ;
    Photos.photoController.showPicker(view,evt); 
  }
  
}) ;
