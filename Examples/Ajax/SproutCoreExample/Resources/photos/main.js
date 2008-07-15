// ==========================================================================
// Photos
// ==========================================================================

// This is the function that will start your app running.  The default 
// implementation will load any fixtures you have created then instantiate
// your controllers and awake the elements on your page.
//
// As you develop your application you will probably want to override this.
// See comments for some pointers on what to do next.
//
function main() {
  
  Photos.initDatabase() ;
  
  // Start the page views that are currently visible on screen.
  SC.page.get('workspace') ;
  SC.page.get('footer') ;

  // Now load the data.
  if (Photos.db) {
    Photos.loadDatabase();
  } else Photos.loadFixtures() ;

};

// This method is called either after the fixtures are loaded or the data 
// is restored from the database.  It will actually cause the UI to "come 
// alive" with data.
Photos.startApplication = function() {
  var albums = Photos.Album.findAll().clone() ;
  Photos.masterController.set('content', albums) ;
} ;

// Just loads fixtures into the store.
Photos.loadFixtures = function() {
  Photos.server.preload(Photos.FIXTURES) ;
  Photos.startApplication() ;
};

// Tries to start the db for client-side storage.  If this works it will set
// Photos.db.
Photos.initDatabase = function() {
  // If DB storage is supported, use it.
  try {
    if (window.openDatabase) {
      Photos.db = openDatabase("Photos.DemoStore", "1.0", "SproutCore Photos Demo Store", 200000) ;
      if (!Photos.db) console.log("Failed to open database on disk.  Probably because the version was bad or the quota is exceeded.  Will proceed with fixture data.") ;
    } else {
      console.log("This browser does not support HTML5 client side storage.  Will proceed with fixture data.") ;
    }
  } catch(err) {}
} ;

// Tries to load data from the database.  If no data is found, loads fixtures.
Photos.loadDatabase = function() {
  // determine if the table already exists...
  Photos.db.transaction(function(tx) {
    tx.executeSql("SELECT COUNT(*) FROM PhotoStoreData", [], 
    
    // success! -- load the data unless the count is 0....
    function(tx, result) {
      Photos.restoreRecordsFromDatabase() ;
      
    // table does not yet exist...build it...
    }, function(tx, error) {
      tx.executeSql("CREATE TABLE PhotoStoreData (id REAL UNIQUE, json TEXT)", [], function() { 
        Photos.loadFixtures(); 

        // also dump initial data...
        var recs = SC.Store.get('records').invoke('get','attributes') ;
        var str = recs.toJSONString() ;
        tx.executeSql("INSERT INTO PhotoStoreData (id, json) VALUES (?, ?)", [1, str]) ;
      }) ;
    });
  }) ;
} ;

// Actually restores the records from the database...
Photos.restoreRecordsFromDatabase = function() {
  if (!Photos.db) return ;
  Photos.db.transaction(function(tx) {
    tx.executeSql("SELECT id, json FROM PhotoStoreData", [], 
    
    // success! process records...
    function(tx, result) {
      
      if (result.rows.length >= 1) {
        var json = result.rows.item(0)["json"] ;
        var recs = eval(json) ;
        Photos.server.preload(recs) ;
        Photos.startApplication() ;
      } else Photo.loadFixtures() ;
      
    // major fail...
    }, function(tx, error) {
      Photo.loadFixtures() ;
    }) ;
  }) ;
} ;

// Actually saves the records...
Photos.dumpRecordsToDatabase = function() {
  if (!Photos.db) return ;
  var recs = SC.Store.get('records').invoke('get','attributes') ;
  var str = recs.toJSONString() ;
  Photos.db.transaction(function(tx) {
    tx.executeSql("UPDATE PhotoStoreData SET json = ? WHERE id = ?", [1, str]) ;
  }) ;
} ;
