package er.extensions;

/**
 * A class to bridge the migration of this class to the eof.qualifiers sub-package.
 * @deprecated use er.extensions.eof.qualifiers.ERXBetweenQualifier
 */
public class ERXBetweenQualifier extends er.extensions.eof.qualifiers.ERXBetweenQualifier {

    public ERXBetweenQualifier(String aKey, Object aMinimumValue, Object aMaximumValue) {
        super(aKey, aMinimumValue, aMaximumValue);
    }

}
