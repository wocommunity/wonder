package er.extensions.statistics.store;

public class ERXEmptyRequestDescription implements IERXRequestDescription {

    public static final String ERROR_STRING = "Error-during-context-description";
    private String descriptionString = ERROR_STRING;

    public ERXEmptyRequestDescription(String descriptionString) {
        if (descriptionString != null) {
            this.descriptionString = descriptionString;
        }
    }

    public String getComponentName() {
        throw new IllegalStateException("field was not set use toString method instead!");
    }

    public String getRequestHandler() {
        throw new IllegalStateException("field was not set use toString method instead!");
    }

    public String getAdditionalInfo() {
        throw new IllegalStateException("field was not set use toString method instead!");
    }

    public RequestDescriptionType getType() {
        return RequestDescriptionType.EMPTY;
    }

    @Override
    public String toString() {
        return descriptionString;
    }
}
