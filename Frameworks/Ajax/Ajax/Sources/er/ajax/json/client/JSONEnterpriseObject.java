package er.ajax.json.client;


/**
 * The base class of a JSONEnterpriseObject that implements the
 * IJSONEnterpriseObject interface.
 * 
 * @author mschrag
 */
public class JSONEnterpriseObject implements IJSONEnterpriseObject {
  private String _globalID;

  public String globalID() {
    return _globalID;
  }

  public void setGlobalID(String globalID) {
    _globalID = globalID;
  }
}
