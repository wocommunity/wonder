// ==========================================================================
// HelloWorld.AppController
// ==========================================================================

require('hello_world/core');

/** @class

  (Document Your View Here)

  @extends SC.Object
  @author    AuthorName  
  @version 0.1
  @static
*/
HelloWorld.appController = SC.Object.create(
/** @scope HelloWorld.appController */ {
  
  greeting: 'Hello World!',
  
  toggleGreeting: function() {
    var currentGreeting = this.get('greeting');
    var newGreeting = (currentGreeting === 'Hello World!') ? 'I am on SproutCore!' : 'Hello World!' ;
    this.set('greeting', newGreeting);
  },
  
  isClockShowing: NO,
  
  isClockShowingObserver: function() {
    var isClockShowing = this.get('isClockShowing') ;

    if (!this._timer) this._timer = SC.Timer.schedule({
      target: this, action: 'tick', repeats: YES, interval: 1000
    });
    this._timer.set('isPaused', !isClockShowing) ;
    
    var newGreeting = (isClockShowing) ? this.now() : 'Hello World'; 
    this.set('greeting', newGreeting) ;
  }.observes('isClockShowing'),
  
  tick: function() {
    this.set('greeting', this.now()) ;
  },
  
  now: function() { 
    return new Date().format('hh:mm:ss');
  }
  
}) ;
