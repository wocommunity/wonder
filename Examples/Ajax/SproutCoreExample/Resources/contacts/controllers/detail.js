// ==========================================================================
// Contacts.DetailController
// ==========================================================================

require('contacts/core');

/** @class

  (Document Your View Here)

  @extends SC.Object
  @author    AuthorName
  @version 0.1
  @static
*/
Contacts.detailController = SC.ObjectController.create(
/** @scope Contacts.detailController */ {

  // TODO: Add your own code here.
  contentBinding: 'Contacts.masterController.selection',

  commitChangesImmediately: false

}) ;
