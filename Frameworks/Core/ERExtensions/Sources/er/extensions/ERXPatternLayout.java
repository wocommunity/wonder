package er.extensions;
/**
 * Do not use in property files, use er.extensions.logging.ERXPatternLayout instead.
 * @deprecated use {@link er.extensions.logging.ERXPatternLayout}
 * @author ak
 */
@Deprecated
public class ERXPatternLayout extends er.extensions.logging.ERXPatternLayout {
	public ERXPatternLayout() {
		System.err.println("You are referencing er.extensions.ERXPatternLayout but should use er.extensions.logging.ERXPatternLayout instead");
	}
}
