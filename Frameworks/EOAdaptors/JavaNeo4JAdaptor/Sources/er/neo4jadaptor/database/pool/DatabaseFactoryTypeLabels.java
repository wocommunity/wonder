package er.neo4jadaptor.database.pool;

/**
 * Defines possible database factory type labels. Available values are:
 * <ul>
 * <li><code>{@value #LABEL_EMBEDDED_READ_ONLY}</code> for {@link DatabaseFactoryType#EMBEDDED_READ_ONLY}</li>
 * <li><code>{@value #LABEL_EMBEDDED_WRITABLE}</code> for {@link DatabaseFactoryType#EMBEDDED_WRITABLE}</li>
 * <li><code>{@value #LABEL_HIGHLY_AVAILABLE}</code> for {@link DatabaseFactoryType#HIGHLY_AVAILABLE}</li>
 * </ul>
 * 
 * 
 * @author Jedrzej Sobanski
 */
public class DatabaseFactoryTypeLabels {
	public static final String LABEL_EMBEDDED_READ_ONLY = "embedded-read-only";
	public static final String LABEL_EMBEDDED_WRITABLE = "embedded-writable";
	public static final String LABEL_HIGHLY_AVAILABLE = "highly-available";
}
