// ==========================================================================
// SampleControls.GridView
// ==========================================================================

require('core');

/** @class

  (Document Your View Here)

  @extends SC.View
  @author    AuthorName  
  @version 0.1
*/
SampleControls.GridView = SC.GridView.extend(SC.Scrollable,
/** @scope SampleControls.GridView.prototype */ {
  
  verticalLineScroll: function() {
    return this.get('rowHeight') ;
  }.property('rowHeight')
  
}) ;
