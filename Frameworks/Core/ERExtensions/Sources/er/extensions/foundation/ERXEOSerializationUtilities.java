package er.extensions.foundation;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

import com.webobjects.eocontrol.EOClassDescription;
import com.webobjects.eocontrol.EOEditingContext;
import com.webobjects.eocontrol.EOEnterpriseObject;
import com.webobjects.eocontrol.EOFaultHandler;
import com.webobjects.eocontrol.EORelationshipManipulation;
import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSValidation.ValidationException;

public class ERXEOSerializationUtilities {
	
	public static EOEnterpriseObject readEO(ObjectInputStream in) throws IOException, ClassNotFoundException {
		in.readObject();
		return (EOEnterpriseObject) in.readObject();
	}
	
	public static void writeEO(ObjectOutputStream out, EOEnterpriseObject eo) throws IOException {
		EOEditingContext ec = eo==null?null:eo.editingContext();
		out.writeObject(ec);
		out.writeObject(ec==null?null:eo);
	}
	
	public static class SerialEOWrapper implements Serializable, EOEnterpriseObject {
		/**
		 * Do I need to update serialVersionUID?
		 * See section 5.6 <cite>Type Changes Affecting Serialization</cite> on page 51 of the 
		 * <a href="http://java.sun.com/j2se/1.4/pdf/serial-spec.pdf">Java Object Serialization Spec</a>
		 */
		private static final long serialVersionUID = 1L;

		private EOEnterpriseObject _eo;
		
		public SerialEOWrapper(){
			//No args constructor for deserialization
		}
		
		public SerialEOWrapper(EOEnterpriseObject eo) {
			_eo = eo;
		}
		
		public EOEnterpriseObject eo() {
			return _eo;
		}
		
		private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException {
			_eo = readEO(in);
		}
		
		private void writeObject(ObjectOutputStream out) throws IOException {
			writeEO(out, _eo);
		}

		public void addObjectToBothSidesOfRelationshipWithKey(EORelationshipManipulation arg0, String arg1) {
			_eo.addObjectToBothSidesOfRelationshipWithKey(arg0, arg1);
		}

		public void addObjectToPropertyWithKey(Object arg0, String arg1) {
			_eo.addObjectToPropertyWithKey(arg0, arg1);
		}

		public NSArray<String> allPropertyKeys() {
			return _eo.allPropertyKeys();
		}

		public NSArray<String> attributeKeys() {
			return _eo.attributeKeys();
		}

		public void awakeFromClientUpdate(EOEditingContext arg0) {
			_eo.awakeFromClientUpdate(arg0);
		}

		public void awakeFromFetch(EOEditingContext arg0) {
			_eo.awakeFromFetch(arg0);
		}

		public void awakeFromInsertion(EOEditingContext arg0) {
			_eo.awakeFromInsertion(arg0);
		}

		public NSDictionary changesFromSnapshot(NSDictionary<String, Object> arg0) {
			return _eo.changesFromSnapshot(arg0);
		}

		public EOClassDescription classDescription() {
			return _eo.classDescription();
		}

		public EOClassDescription classDescriptionForDestinationKey(String arg0) {
			return _eo.classDescriptionForDestinationKey(arg0);
		}

		public void clearFault() {
			_eo.clearFault();
		}

		public void clearProperties() {
			_eo.clearProperties();
		}

		public int deleteRuleForRelationshipKey(String arg0) {
			return _eo.deleteRuleForRelationshipKey(arg0);
		}

		public EOEditingContext editingContext() {
			return _eo.editingContext();
		}

		public String entityName() {
			return _eo.entityName();
		}

		public String eoDescription() {
			return _eo.eoDescription();
		}

		public String eoShallowDescription() {
			return _eo.eoShallowDescription();
		}

		public EOFaultHandler faultHandler() {
			return _eo.faultHandler();
		}

		public Object handleQueryWithUnboundKey(String arg0) {
			return _eo.handleQueryWithUnboundKey(arg0);
		}

		public void handleTakeValueForUnboundKey(Object arg0, String arg1) {
			_eo.handleTakeValueForUnboundKey(arg0, arg1);
		}

		public String inverseForRelationshipKey(String arg0) {
			return _eo.inverseForRelationshipKey(arg0);
		}

		public Object invokeRemoteMethod(String arg0, Class[] arg1, Object[] arg2) {
			return _eo.invokeRemoteMethod(arg0, arg1, arg2);
		}

		public boolean isFault() {
			return _eo.isFault();
		}

		public boolean isReadOnly() {
			return _eo.isReadOnly();
		}

		public boolean isToManyKey(String arg0) {
			return _eo.isToManyKey(arg0);
		}

		public Object opaqueState() {
			return _eo.opaqueState();
		}

		public boolean ownsDestinationObjectsForRelationshipKey(String arg0) {
			return _eo.ownsDestinationObjectsForRelationshipKey(arg0);
		}

		public void prepareValuesForClient() {
			_eo.prepareValuesForClient();
		}

		public void propagateDeleteWithEditingContext(EOEditingContext arg0) {
			_eo.propagateDeleteWithEditingContext(arg0);
		}

		public void reapplyChangesFromDictionary(NSDictionary<String, Object> arg0) {
			_eo.reapplyChangesFromDictionary(arg0);
		}

		public void removeObjectFromBothSidesOfRelationshipWithKey(EORelationshipManipulation arg0, String arg1) {
			_eo.removeObjectFromBothSidesOfRelationshipWithKey(arg0, arg1);
		}

		public void removeObjectFromPropertyWithKey(Object arg0, String arg1) {
			_eo.removeObjectFromPropertyWithKey(arg0, arg1);
		}

		public NSDictionary<String, Object> snapshot() {
			return _eo.snapshot();
		}

		public Object storedValueForKey(String arg0) {
			return _eo.storedValueForKey(arg0);
		}

		public void takeStoredValueForKey(Object arg0, String arg1) {
			_eo.takeStoredValueForKey(arg0, arg1);
		}

		public void takeValueForKey(Object arg0, String arg1) {
			_eo.takeValueForKey(arg0, arg1);
		}

		public void takeValueForKeyPath(Object arg0, String arg1) {
			_eo.takeValueForKeyPath(arg0, arg1);
		}

		public void takeValuesFromDictionary(NSDictionary arg0) {
			_eo.takeValuesFromDictionary(arg0);
		}

		public void takeValuesFromDictionaryWithMapping(NSDictionary arg0, NSDictionary arg1) {
			_eo.takeValuesFromDictionaryWithMapping(arg0, arg1);
		}

		public NSArray<String> toManyRelationshipKeys() {
			return _eo.toManyRelationshipKeys();
		}

		public NSArray<String> toOneRelationshipKeys() {
			return _eo.toOneRelationshipKeys();
		}

		public void turnIntoFault(EOFaultHandler arg0) {
			_eo.turnIntoFault(arg0);
		}

		public void unableToSetNullForKey(String arg0) {
			_eo.unableToSetNullForKey(arg0);
		}

		public void updateFromSnapshot(NSDictionary<String, Object> arg0) {
			_eo.updateFromSnapshot(arg0);
		}

		public String userPresentableDescription() {
			return _eo.userPresentableDescription();
		}

		public void validateClientUpdate() throws ValidationException {
			_eo.validateClientUpdate();
		}

		public void validateForDelete() throws ValidationException {
			_eo.validateForDelete();
		}

		public void validateForInsert() throws ValidationException {
			_eo.validateForInsert();
		}

		public void validateForSave() throws ValidationException {
			_eo.validateForSave();
		}

		public void validateForUpdate() throws ValidationException {
			_eo.validateForUpdate();
		}

		public Object validateTakeValueForKeyPath(Object arg0, String arg1) throws ValidationException {
			return _eo.validateTakeValueForKeyPath(arg0, arg1);
		}

		public Object validateValueForKey(Object arg0, String arg1) throws ValidationException {
			return _eo.validateValueForKey(arg0, arg1);
		}

		public Object valueForKey(String arg0) {
			return _eo.valueForKey(arg0);
		}

		public Object valueForKeyPath(String arg0) {
			return _eo.valueForKeyPath(arg0);
		}

		public NSDictionary valuesForKeys(NSArray arg0) {
			return _eo.valuesForKeys(arg0);
		}

		public NSDictionary valuesForKeysWithMapping(NSDictionary arg0) {
			return _eo.valuesForKeysWithMapping(arg0);
		}

		public void willChange() {
			_eo.willChange();
		}

		public void willRead() {
			_eo.willRead();
		}

		public Object willReadRelationship(Object arg0) {
			return _eo.willReadRelationship(arg0);
		}
	}
}
