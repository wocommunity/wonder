package er.extensions.statistics.store;

public interface RequestDescription {

    public String getComponentName();

    public String getRequestHandler();

    public String getAdditionalInfo();

    public RequestDescriptionType getType();

}
