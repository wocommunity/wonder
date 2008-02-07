package er.taggable.model;

import org.apache.log4j.Logger;

/**
 * ERTag represents a single String shared tag.
 * 
 * @author mschrag
 */
public class ERTag extends _ERTag {
  /**
   * Inclusion provides an enum for ANY or ALL.
   * 
   * @author mschrag
   */
  public static enum Inclusion {
    ANY, ALL
  }

  private static Logger log = Logger.getLogger(ERTag.class);
}
