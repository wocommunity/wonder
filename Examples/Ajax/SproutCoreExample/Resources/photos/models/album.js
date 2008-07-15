// ==========================================================================
// Photos.Album
// ==========================================================================

require('core');

/** @class
  
  (Document your class here)

  @extends SC.Record
  @author    AuthorName  
  @version 0.1
*/  
Photos.Album = SC.Record.extend(
/** @scope Photos.Album.prototype */ {
  
  icon: function() {
    return (this.get('albumType') == 'Subscription') ? 'sc-icon-user-16' : 'sc-icon-folder-16' ;
  }.property('albumType'),
  
  photosType: 'Photos.Photo',
  
  photoCount: function() {
    return this.get('photos').get('length') ;
  }.property('photos'),
  
  photoSize: 160 // default size
  
}) ;
