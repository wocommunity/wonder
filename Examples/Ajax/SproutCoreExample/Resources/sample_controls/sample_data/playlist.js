// ==========================================================================
// SampleControls.Playlist Fixtures
// ==========================================================================

require('core') ;


SampleControls.FIXTURES = SampleControls.FIXTURES.concat(function() {
  
  var library = 'Music,Movies,TV Shows,Podcasts,Radio,Ringtones'.split(',');
  var store = 'Store Purchased Downloads'.split(' ');
  var playlists ='Shuffle,Christmas,Blues,Classical,Electronic,Jazz,New Music,Top 50 Most Played,Unrated,Chillout'.split(',');
  
  var ret = [] ;
  var fadd = function(group, names, iconClassName) {
    for(var idx =0;idx<names.length;idx++) {
      ret.push({
        guid: (group + idx).toString(),
        type: "Playlist",
        group: group,
        name: names[idx],
        unreadCount: (idx % 4 > 2) ? 1000 : (idx % 4 > 1) ? 100 : (idx % 4 > 0) ? 1 : 0,
        iconClassName: iconClassName
      }) ;
    }
  } ;

  fadd('library', library, 'sc-icon-folder-16') ;
  fadd('store', store, 'sc-icon-bookmark-16') ;
  fadd('playlists', playlists, 'sc-icon-favorite-16') ;
  
  return ret ;
}()) ;


