package er.taggable;

/**
 * ERTagNormalizer provides an interface for processing tags to clean them up prior to persisting them.
 * 
 * @author mschrag
 */
public interface ERTagNormalizer {
  /**
   * Returns the normalized tag name given an "unclean" tag name
   * @param tag the "unclean" tag name
   * @return the normalized tag name
   */
  public String normalize(String tag);
}
