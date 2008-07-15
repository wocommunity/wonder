// ==========================================================================
// Photos.MasterController
// ==========================================================================

require('core');

/** @class

  (Document Your View Here)

  @extends SC.Object
  @author    AuthorName  
  @version 0.1
  @static
*/
Photos.masterController = SC.ArrayController.create(
/** @scope Photos.masterController */ {

  allowsMultipleSelection: NO,
  allowsEmptySelection: NO,
  useControllersForContent: NO,
  
  albumsVisible: YES,
  
  albumsVisibleObserver: function() {
    SC.page.workspace.layout() ; // update layout for splitview.
  }.observes('albumsVisible'),

  /**
    Called by the Add Album button.  Adding and removing content is generally
    left up to you to write because the steps involved can vary so much
    between applications.  In this case, we just create a new Album record
    and then insert it into the content array at the proper location.  We
    then tell them source list to begin editing the album so the user can
    rename it as desired.
  */
  addAlbum: function(sender) {
    
    // determine the name of the new album.
    var name = 'Untitled' ;
    var cnt = 1 ;
    while (Photos.Album.find({ name: name })) {
      name = "Untitled %@".fmt(cnt++) ;
    }
    
    var album = Photos.Album.newRecord({
      name: name,
      albumType: 'Library',
      photos: []     
    }) ;
    
    // insert into the end of the list of albums with a Library type.
    var content = this.get('content') ;
    var idx = 0;
    var len = content.get('length') ;
    while((idx < len) && (content.objectAt(idx).get('albumType') === 'Library')) idx++ ;
    
    // get the wrapped version of the album (which is what we will actually
    // use for selecting)
    //
    // Note that we wrap these two changes in beginEditing/endEditing calls
    // This way the collection view will only be notified once of the changes
    // which speeds things up quite a bit.
    this.beginPropertyChanges() ;
    this.insertAt(idx, album) ;
    album = this.objectAt(idx) ;
    this.set('selection', [album]) ;
    this.endPropertyChanges() ;

    // Since bindings and other deferred rendering may not have completed yet
    // we defer actually begin editing for a few msec.  Alternatively you 
    // could try to force the collection view to render right now but this 
    // approach will tend to make the app feel more responsive.
    this.invokeLater(this.beginEditingSelection, 50) ;
  },
  
  /**
    This is the action invoked by the "Delete Album" button on the UI.
  */
  deleteAlbum: function(sender) {
    
    // be sure to wrap this in begin/end property changes to avoid sending
    // notifications
    this.beginPropertyChanges() ;
    
    // remove any selected albums from the content array and then delete the
    // record itself.  Note that we clone the selection in case the index 
    // changes it.
    var sel = (this.get('selection') || []).clone() ;
    idx = sel.get('length') ;
    while(--idx >= 0) {
      var album = sel.objectAt(idx) ;
      this.removeObject(album) ;
      
      // also destroy the source record object.
      album.destroy() ;
    }
    
    // select the first album.
    this.set('selection', [this.objectAt(0)].compact()) ;
    
    // done making changes to the system to send property changes.  This
    // will allow the UI to update.
    this.endPropertyChanges() ;
  },
  
  beginEditingSelection: function() {
    var view = SC.page.getPath('workspace.sidebar.sourceList.firstChild') ;
    var album = this.get('selection').objectAt(0) ;
    if (view) {
      view.scrollToContent(album) ;
      var itemView = view.itemViewForContent(album) ;
      if (itemView) itemView.beginEditing() ;
    }
  },
  
  /**
    Implements the collection view delegate method to validate drops of 
    photos.  The default behavior will allow you to only drop photos ON
    albums of type "Library" and will not let you drop onto the album that
    is currently selected.
  */
  collectionViewValidateDrop: function(view, drag, dropOperation, proposedInsertionIndex, proposedDragOperation) {

    var ret = SC.DRAG_NONE ;
    
    // always allow reordering of albums...
    console.log('proposedDragoperation: %@'.fmt(proposedDragOperation)) ;
    
    if (proposedDragOperation === SC.DRAG_REORDER) {
      return proposedDragOperation;
    }
    
    switch(dropOperation) {
      
      // this is the parameter passed in when the collection view just wants
      // to know if the data types provided by the drag are supported.
      case SC.DROP_ANY:
        ret = (drag.hasDataType(Photos.PHOTO_TYPE)) ? SC.DRAG_ANY : SC.DRAG_NONE ;
        break ;
        
      // This parameter is called when the collection view thinks we might
      // want to drop the item ON a view.
      case SC.DROP_ON:
        ret = (drag.hasDataType(Photos.PHOTO_TYPE)) ? SC.DRAG_COPY : SC.DRAG_NONE ;
        
        // make sure we don't accept content of type other the library.
        if (ret !== SC.DRAG_NONE) {
          var content = this.objectAt(proposedInsertionIndex) ;
          var sel = this.get('selection') || [] ;

          // if there is no object at index, do not allow.
          if (!content) {
            ret = SC.DRAG_NONE ;
            
          // do not allow if the content is not of type library
          } else if (content.get('albumType') !== 'Library') {
            ret = SC.DRAG_NONE ;
            
          // do not allow if the content is the selected album...
          } else if (sel.indexOf(content) >= 0) {
            ret = SC.DRAG_NONE ;
          }
        }
        
        break ;
        
      default:
        ret = SC.DRAG_NONE ;
    }
    
    return ret ;
  },

  /**
    Called by the collection view when the user releases the drop.  This 
    should perform the drop operation if the data is a photo.  Note that
    this must already be validated to be called so we don't need to check 
    again.
  */
  collectionViewAcceptDrop: function(view, drag, dropOperation, proposedInsertionIndex, dragOperation) {

    // get the photos to add and the album.
    var photosToAdd = drag.dataForType(Photos.PHOTO_TYPE) ;
    var album = this.objectAt(proposedInsertionIndex) ;

    // basic consistancy checks...
    if (dropOperation !== SC.DROP_ON) return SC.DRAG_NONE ;
    if (!photosToAdd) return SC.DRAG_NONE ;
    if (!album) return SC.DRAG_NONE ;

    // get the photos array for the album and add the photos to the end.
    // Note that if the photo already exists in the album, we do not want to
    // add it a second time.
    //
    var photosToAddCount = photosToAdd.get('length') ;
    var photos = album.get('photos') ;
    
    for(var idx=0; idx<=photosToAddCount; idx++) {
      var photo = photosToAdd.objectAt(idx) ;
      if (photo && (photos.indexOf(photo) < 0)) {
        photos.pushObject(photo) ;
      } 
    }
    
    return SC.DRAG_COPY ;
  }
  
  
}) ;




