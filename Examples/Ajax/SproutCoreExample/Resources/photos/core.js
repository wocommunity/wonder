// ==========================================================================
// Photos
// ==========================================================================

Photos = SC.Object.create({

  // This will create the server for your application.  Add any namespaces
  // your model objects are defined in to the prefix array.
  server: SC.Server.create({ prefix: ['Photos'] }),

  // When you are in development mode, this array will be populated with 
  // any fixtures you create for testing and loaded automatically in your
  // main method.  When in production, this will be an empty array.
  FIXTURES: [],

  // Any keys in this array will be instantiated automatically from main.
  controllers: [],
  
  PHOTO_TYPE: 'Photos.Photo'
  
}) ;

SC.CAPTURE_BACKSPACE_KEY = YES ;

PHOTO_COUNT = 250 ;