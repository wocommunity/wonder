package er.extensions;

import java.util.Properties;

/**
 * This class doesn't do much in 2.0.0, but it was brought back to 
 * from the trunk to maintain parity between the two versions as much as possible.
 * 
 * @author mschrag
 */
public class ERXSystem {
  /**
   * Converts and evaluates the properties from System.getProperties() and replaces
   * the converted values in-place.
   */
  public static void updateProperties() {
    Properties originalProperties = System.getProperties();
    ERXProperties.evaluatePropertyOperators(originalProperties, originalProperties);
  }
}
