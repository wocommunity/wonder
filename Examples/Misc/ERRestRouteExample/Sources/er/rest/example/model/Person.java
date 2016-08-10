package er.rest.example.model;

import com.webobjects.foundation.NSTimestamp;

public class Person extends _Person {
  /**
   * This method exists just to show a derived non-model method appearing in the rest results.
   * 
   * @return
   */
  public NSTimestamp derivedCurrentTime() {
  	return new NSTimestamp();
  }
}
