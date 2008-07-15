// ==========================================================================
// Photos.PhotoView
// ==========================================================================

require('core');

/** @class

  Displays a single photo.

  @extends SC.View
  @author    AuthorName  
  @version 0.1
*/
Photos.PhotoView = SC.ImageCellView.extend(
/** @scope Photos.PhotoView.prototype */ {
  
  /** The current rotation of the image view. */
  rotation: 0,
  
  imageMargin: 10,
  
  // handle rotation...
  contentPropertyDidChange: function(target, key) {
    
    if (key === 'rotation' || key === '*') {
      var content = this.get('content') ;
      var rot = (content) ? content.get('rotation') : 0 ;
      if (rot !== this._rotation) {
        this._rotation = rot ;
        this.set('rotation', rot) ;
      }
    }

    if (key === 'opacity' || key === '*') {
      var content = this.get('content') ;
      var opacity = (content) ? content.get('opacity') : 1.0 ;
      if (opacity !== this._opacity) {
        this._opacity = opacity ;
        this.set('opacity', opacity) ;
      }
    }
    
    arguments.callee.base.apply(this, arguments) ;
  },
  

  /** When the rotation changes, set the transform... */
  rotationObserver: function() {
    var rot = this.get('rotation') ;
    this.set('styleWebkitTransform', 'rotate(%@deg)'.fmt(rot)) ;
  }.observes('rotation'),

  /** When the opacity changes, set the transform... */
  opacityObserver: function() {
    var opacity = this.get('opacity') ;
    this.set('styleOpacity', opacity) ;
  }.observes('opacity') 
  
}) ;
