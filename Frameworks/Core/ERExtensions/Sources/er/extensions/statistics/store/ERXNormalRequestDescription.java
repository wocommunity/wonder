package er.extensions.statistics.store;

public class ERXNormalRequestDescription implements IERXRequestDescription {

    private final String componentName;
    private final String requestHandler;
    private final String additionalInfo;

    public ERXNormalRequestDescription(String componentName, String requestHandler, String additionalInfo) {
        this.componentName = componentName;
        this.requestHandler = requestHandler;
        this.additionalInfo = additionalInfo;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getRequestHandler() {
        return requestHandler;
    }

    public String getAdditionalInfo() {
        return additionalInfo;
    }

    public RequestDescriptionType getType() {
        return RequestDescriptionType.NORMAL;
    }

    @Override
    public String toString() {
        return componentName + "-" + requestHandler + additionalInfo;
    }
}
