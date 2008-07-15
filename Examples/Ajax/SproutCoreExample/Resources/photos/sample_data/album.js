// ==========================================================================
// Photos.Album Fixtures
// ==========================================================================

require('core') ;

Photos.FIXTURES = Photos.FIXTURES.concat(function() {
  
  var recs = [

  {
    guid: '1',
    type: 'Album',
    name: 'Favorites',
    albumType: 'Library'
  },

  {
    guid: '2',
    type: 'Album',
    name: 'France Trip',
    albumType: 'Library'
  },

  {
    guid: '3',
    type: 'Album',
    name: 'Prague',
    albumType: 'Library'
  },

  {
    guid: '4',
    type: 'Album',
    name: 'Cesky Krumlev',
    albumType: 'Library'
  },

  {
    guid: '5',
    type: 'Album',
    name: 'Cannes',
    albumType: 'Library'
  },

  {
    guid: '6',
    type: 'Album',
    name: "Jenna's Photos",
    albumType: 'Subscription'
  },

  {
    guid: '7',
    type: 'Album',
    name: "Peters's Photos",
    albumType: 'Subscription'
  },

  {
    guid: '8',
    type: 'Album',
    name: "Chris's Photos",
    albumType: 'Subscription'
  } ] ;
  
  // for each rec, randomly assign some photos.
  var idx = recs.length ;
  while(--idx >= 0) {
    var rec = recs[idx] ;
    var photoCount= (rec.albumType == 'Subscription') ? PHOTO_COUNT : Math.floor(Math.random() * (PHOTO_COUNT/2)) ;
    var guids = [] ;
    while(--photoCount >= 0) {
      guids.push(Math.floor(Math.random() * (PHOTO_COUNT))+1) ;
    }
    guids = guids.uniq().compact() ;
    rec.photos = guids ;
  }
  
  return recs ;
  
}());
