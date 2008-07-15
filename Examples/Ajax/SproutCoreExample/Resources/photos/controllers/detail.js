// ==========================================================================
// Photos.DetailController
// ==========================================================================

require('core');

/** @class

  (Document Your View Here)

  @extends SC.Object
  @author    AuthorName  
  @version 0.1
  @static
*/
Photos.detailController = SC.ArrayController.create(
/** @scope Photos.detailController */ {

  useControllersForContent: NO,

  /**
    Since this controller displays the photos for the selected album, its
    content property is bound to the photos property of the selected album
    controller.  This is the typical architecture for a "master-view" style
    UI.
  */
  contentBinding: 'Photos.albumController.photos',

  /**
    This computed property is used to display a label at the bottom telling
    you how may photos are in the current album and how many photos are
    currently selected.
    
    Note that to create a computed property, you just define a method and
    add property() to the end of it.  If your property depends on some
    other properties (such as the length of the album and the selection
    in this case), then declare those also.  
    
    This will cause anyone observing your property to be notified whenever
    these other properties change.
  */
  countLabel: function() {
    
    // photo count label
    var len = this.get('length') ;
    var label = (len <= 0) ? "No Photos".loc() : (len > 1) ? "%@ Photos".fmt(len) : "%@ Photo".fmt(len);
    
    // selection label
    var sel = this.get('selection') ;
    var len = (sel) ? sel.get('length') : 0 ;
    var selLabel = (len > 0) ? "(%@ selected)".fmt(len) : null ;
    
    return [label, selLabel].compact().join(' ');
  }.property('length', 'selection'),
    
  /**
    This property is bound to the slider in the UI to set the ideal photo
    size.  An observer monitors this slider and the will update the 
    photo detail itself.
  */
  photoSize: 160,
  
  /**
    This is an example of how easy it can be to change some basic behaviors
    about your application if you have designed it properly.  This binding
    connects the photoSize property to the currently selected album, which 
    means that as you step through the various albums, your photo size will
    change for you automatically.
    
    If you remove this binding, the photo sizing system will still work, but
    now the photoSize setting will be shared application wide by all photos.
  */
  photoSizeBinding: 'Photos.albumController.photoSize',
  
  /**
    Wheneve the photoSize changes, this observer will be triggered.  Since
    you generally want the slider to remain smooth while the user drags it,
    all this observer does it schedule another method to run later that will
    actually update the collection view's rowHeight and columnWidth
    properties.
    
    Note that once we have scheduled this kind of timer, we generally do not
    want to call it multiple times, so this method checks to see if a valid
    timer has already been scheduled and does nothing in that case.
  */
  photoSizeObserver: function() {
    if (this._photoSizeDidChangeTimer && this._photoSizeDidChangeTimer.get('isValid')) return ;
    
    this._photoSizeDidChangeTimer = this.invokeLater('photoSizeDidChange') ;
  }.observes('photoSize'),
  
  /**
    This is the actual method that updates the photo detail view with the new
    photo size.  It computes and idea row and column width based on the
    photoSize property and sets them.  Then it calls updateChildren(true),
    which will force the collection view to relayout all of its children.
    
    Most of the time collection views will notice anytime a property affecting
    their content has changed and update accordingly.  However if you change
    some shared properties such as this one that effect all layout, you may
    sometimes need to call updateChildren() manually.
  */
  photoSizeDidChange: function() {
    // clear the timer..
    this._photoSizeDidChangeTimer = null ;
    
    // get the photoSize and compute the new row and colum width
    var photoSize = this.get('photoSize') ;
    var columnWidth = photoSize ;
    var rowHeight = Math.floor((photoSize * 120) / 160) ; // ratio 
    
    var view = SC.page.getPath('workspace.gridView.firstChild') ;
    if (view && ((view.get('columnWidth') != columnWidth) || (view.get('rowHeight') != rowHeight))) {
      view.beginPropertyChanges() ;
      view.set('rowHeight', rowHeight) ;
      view.set('columnWidth', columnWidth) ;
      view.endPropertyChanges() ;
      view.updateChildren(true) ;
    }
  },
  
  /**
    The detail list of photos should provide the photo records as a valid
    data type during drags.  This will enable the items to be dropped onto
    items in the source list.
  */
  collectionViewDragDataTypes: function(view) { 
    return [Photos.PHOTO_TYPE] ;
  },
  
  /**
    Provide the actual photo data.
  */
  collectionViewDragDataForType: function(view, dataType, drag) {  
    var ret = (dataType === Photos.PHOTO_TYPE) ? this.get('selection').slice() : null;
    return ret ;
  }
  
  
}) ;
