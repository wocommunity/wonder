package er.rest.example.model;

import org.apache.log4j.Logger;

import com.webobjects.foundation.NSTimestamp;

public class Person extends _Person {
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(Person.class);
  
  /**
   * This method exists just to show a derived non-model method appearing in the rest results.
   * 
   * @return
   */
  public NSTimestamp derivedCurrentTime() {
  	return new NSTimestamp();
  }
}
