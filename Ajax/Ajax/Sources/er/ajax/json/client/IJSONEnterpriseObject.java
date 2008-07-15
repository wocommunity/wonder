package er.ajax.json.client;

/**
 * IJSONEnterpriseObject is a stub interface you can implement in
 * a JSON-based Java client to preserve EO global ID and entity 
 * information across the service interface.
 *  
 * @author mschrag
 */
public interface IJSONEnterpriseObject {
  /**
   * Returns the global ID of the original EO.
   * 
   * @return the global ID of the original EO
   */
  public String globalID();

  /**
   * Sets the global ID of the original EO.
   * 
   * @param globalID the global ID of the original EO
   */
  public void setGlobalID(String globalID);
}
