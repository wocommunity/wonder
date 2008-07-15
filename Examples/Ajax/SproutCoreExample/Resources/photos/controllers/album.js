// ==========================================================================
// Photos.AlbumController
// ==========================================================================

require('core');

/** @class

  (Document Your View Here)

  @extends SC.Object
  @author    AuthorName  
  @version 0.1
  @static
*/
Photos.albumController = SC.ObjectController.create(
/** @scope Photos.albumController */ {

  contentBinding: 'Photos.masterController.selection'
  
}) ;
