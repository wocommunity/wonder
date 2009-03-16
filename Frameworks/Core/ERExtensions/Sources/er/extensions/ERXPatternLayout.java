package er.extensions;
/**
 * Do not use in property files, use er.extensions.logging.ERXPatternLayout instead.
 * @deprecated
 * @author ak
 *
 */
public class ERXPatternLayout extends er.extensions.logging.ERXPatternLayout {
	public ERXPatternLayout() {
		System.err.println("You are referencing er.extensions.ERXPatternLayout but should use er.extensions.logging.ERXPatternLayout instead");
	}
}
