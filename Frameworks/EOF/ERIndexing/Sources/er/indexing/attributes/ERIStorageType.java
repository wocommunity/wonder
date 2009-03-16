package er.indexing.attributes;

import er.extensions.eof.ERXConstant;

public class ERIStorageType extends ERXConstant.NumberConstant {

    public static ERIStorageType PLAIN = new ERIStorageType(1, "ERIStorageTypePlain");
    public static ERIStorageType TOKENIZED = new ERIStorageType(2, "ERIStorageTypeTokenized");

    protected ERIStorageType(int value, String name) {
        super(value, name);
    }
    
    public static ERIStorageType valueType(int key) {
        return (ERIStorageType) constantForClassNamed(key, ERIStorageType.class.getName());
    }
}
