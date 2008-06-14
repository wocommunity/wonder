/**
 * 
 */
package er.indexing.attributes;

import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSKeyValueCoding;

import er.indexing.ERIndex;
import er.indexing.ERIndexModel;
import er.indexing.ERIndex.IndexDocument;

public class ERIDocument implements NSKeyValueCoding {
    
    private final ERIAttributeGroup _attributeGroup;
    private IndexDocument _document;
    private EOKeyGlobalID _globalID;

    public ERIDocument(ERIAttributeGroup attributeGroup, EOKeyGlobalID globalID) {
        _attributeGroup = attributeGroup;
        _globalID = globalID;
    }

    private ERIAttribute attributeForName(String key) {
        return _attributeGroup.attributeForName(key);
    }

    public void takeValueForKey(Object value, String key) {
        willRead();
        String stringValue = attributeForName(key).formatValue(value);
        document().takeValueForKey(stringValue, key);
    }

    public Object valueForKey(String key) {
        willRead();
        Object value = document().valueForKey(key);
        return attributeForName(key).parseValue((String)value);
    }

    public void willRead() {
        if(_document == null) {
            _document = index().documentForGlobalID(globalID());
            if(_document == null) {
                _document = index().createDocumentForGlobalID(globalID());
            }
        }
    }

    private ERIndex index() {
        return ERIndexModel.indexModel().indexNamed(_attributeGroup.name());
    }

    private EOKeyGlobalID globalID() {
        return _globalID;
    }

    private IndexDocument document() {
        return _document;
    }

    public ERIAttributeGroup attributeGroup() {
        return _attributeGroup;
    }
    
    public void save() {
        willRead();
        document().save();
    }
    
    public void revert() {
        willRead();
        document().revert();
    }
    
    public void delete() {
        willRead();
        document().delete();
    }
}