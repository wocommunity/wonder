package er.diva;

/**
 * Interface for stylesheets
 */
public interface ERDIVPageInterface {

    /*
     * D2W keys
     */
    public static interface Keys {
    	public static final String Stylesheet = "stylesheet";
    	public static final String Id = "id";
    }
    
    // accessors
    public String stylesheet();
}
