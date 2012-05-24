package er.attachment.metadata;

/**
 * Thrown when metadata parsing fails.
 * 
 * @author mschrag
 */
public class ERMetadataParserException extends Exception {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

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
