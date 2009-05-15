package er.attachment.metadata;

/**
 * Thrown when metadata parsing fails.
 * 
 * @author mschrag
 */
public class ERMetadataParserException extends Exception {
  /**
   * Constructs a new ERMetadataParserException.
   * 
   * @param message the exception message
   */
  public ERMetadataParserException(String message) {
    super(message);
  }

  /**
   * Constructs a new ERMetadataParserException.
   * 
   * @param message the exception message
   * @param cause the cause
   */
  public ERMetadataParserException(String message, Throwable cause) {
    super(message, cause);
  }
}
