// ==========================================================================
// SampleControls.Contact
// ==========================================================================

require('core');

/** @class
  
  (Document your class here)

  @extends SC.Record
  @author    AuthorName  
  @version 0.1
*/  
SampleControls.Photo = SC.Record.extend(
/** @scope SampleControls.Photo.prototype */ {
  
  // TODO: Add your own code here.
  fullName: function() {
    return this.getEach('itemIndex', 'photoName').compact().join(' ');
  }.property('photoName', 'itemIndex'),
  
  icon: 'sc-icon-document-16'
  
}) ;
