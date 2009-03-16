/**
 * 
 */
package er.indexing.attributes;

import com.webobjects.eocontrol.EOKeyGlobalID;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSKeyValueCoding;

import er.indexing.ERIndex;
import er.indexing.ERIndex.IndexDocument;

public class ERIDocument implements NSKeyValueCoding {
    
    private final ERIAttributeGroup _attributeGroup;
    private IndexDocument _document;
    private EOKeyGlobalID _globalID;

    private NSArray<ERIAttribute> _attributes;

    public ERIDocument(ERIAttributeGroup attributeGroup, EOKeyGlobalID globalID) {
        _attributeGroup = attributeGroup;
        _attributes = attributeGroup.allAttributes();
        _globalID = globalID;
    }

    private ERIAttribute attributeForName(String key) {
        for (ERIAttribute attribute : _attributes) {
            if(key.equals(attribute.name())) {
                return attribute;
            }
        }
        return null;
    }

    public void takeValueForKey(Object value, String key) {
        willRead();
        String stringValue = attributeForName(key).formatValue(value);
        document().takeValueForKey(stringValue, key);
    }

    public Object valueForKey(String key) {
        willRead();
        if(isRead()) {
            Object value = document().valueForKey(key);
            return attributeForName(key).parseValue((String)value);
        }
        return null;
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
        return _attributeGroup.index();
    }

    public boolean isRead() {
        return _document != null;
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