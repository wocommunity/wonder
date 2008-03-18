package er.ajax.json;

import org.json.JSONArray;

/**
 * The base class of a JSONEnterpriseObject that implements the
 * IJSONEnterpriseObject interface.
 * 
 * @author mschrag
 */
public class JSONEnterpriseObject implements IJSONEnterpriseObject {
  private String _entityName;
  private JSONArray _globalID;

  public String entityName() {
    return _entityName;
  }

  public void setEntityName(String entityName) {
    _entityName = entityName;
  }

  public JSONArray globalID() {
    return _globalID;
  }

  public void setGlobalID(JSONArray globalID) {
    _globalID = globalID;
  }
}
