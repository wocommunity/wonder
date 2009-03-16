// ==========================================================================
// SampleControls.PaneController
// ==========================================================================

require('core');

/** @class

  (Document Your View Here)

  @extends SC.Object
  @author    AuthorName  
  @version 0.1
  @static
*/
SampleControls.paneController = SC.Object.create(
/** @scope SampleControls.paneController */ {

  showDialog: function() {
    SC.page.get('dialogPane').set('isVisible', YES) ;
  },

  hideDialog: function() {
    SC.page.get('dialogPane').set('isVisible', NO) ;
  },

  showPanel: function() {
    SC.page.get('panelPane').set('isVisible', YES) ;
  },

  hidePanel: function() {
    SC.page.get('panelPane').set('isVisible', NO) ;
  }
  
}) ;
