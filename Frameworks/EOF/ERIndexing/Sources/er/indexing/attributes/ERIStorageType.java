package er.indexing.attributes;

import er.extensions.eof.ERXConstant;

public class ERIStorageType extends ERXConstant.NumberConstant {
	/**
	 * Do I need to update serialVersionUID?
	 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
	 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
	 */
	private static final long serialVersionUID = 1L;

    public static ERIStorageType PLAIN = new ERIStorageType(1, "ERIStorageTypePlain");
    public static ERIStorageType TOKENIZED = new ERIStorageType(2, "ERIStorageTypeTokenized");

    protected ERIStorageType(int value, String name) {
        super(value, name);
    }
    
    public static ERIStorageType valueType(int key) {
        return (ERIStorageType) constantForClassNamed(key, ERIStorageType.class.getName());
    }
}
