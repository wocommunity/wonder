package er.taggable;

/**
 * ERDefaultTagNormalizer trims and lowercases all tags.
 * 
 * @author mschrag
 */
public class ERDefaultTagNormalizer implements ERTagNormalizer {
  public String normalize(String tag) {
    String normalizedTag = tag;
    if (normalizedTag != null) {
      normalizedTag = normalizedTag.trim().toLowerCase();
    }
    return normalizedTag;
  }
}
