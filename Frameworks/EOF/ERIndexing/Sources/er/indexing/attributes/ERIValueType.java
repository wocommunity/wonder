package er.indexing.attributes;

import er.extensions.eof.ERXConstant;

public class ERIValueType extends ERXConstant.NumberConstant {

    public static ERIValueType STRING = new ERIValueType(1, "ERIValueTypeString");
    public static ERIValueType INTEGER = new ERIValueType(2, "ERIValueTypeInteger");
    public static ERIValueType DECIMAL = new ERIValueType(3, "ERIValueTypeDecimal");
    public static ERIValueType DATE = new ERIValueType(4, "ERIValueTypeDate");
    public static ERIValueType BOOLEAN = new ERIValueType(5, "ERIValueTypeBoolean");

    protected ERIValueType(int value, String name) {
        super(value, name);
    }
    
    public static ERIValueType valueType(int key) {
        return (ERIValueType) constantForClassNamed(key, ERIValueType.class.getName());
    }
}
