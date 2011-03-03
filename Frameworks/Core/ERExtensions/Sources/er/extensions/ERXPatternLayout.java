package er.extensions;

import edu.umd.cs.findbugs.annotations.SuppressWarnings;

/**
 * Do not use in property files, use er.extensions.logging.ERXPatternLayout instead.
 * @deprecated
 * @author ak
 *
 */
@SuppressWarnings(value="NM_SAME_SIMPLE_NAME_AS_SUPERCLASS", justification="deprecated compatibility")
public class ERXPatternLayout extends er.extensions.logging.ERXPatternLayout {
    public ERXPatternLayout() {
		System.err.println("You are referencing er.extensions.ERXPatternLayout but should use er.extensions.logging.ERXPatternLayout instead");
	}
}
