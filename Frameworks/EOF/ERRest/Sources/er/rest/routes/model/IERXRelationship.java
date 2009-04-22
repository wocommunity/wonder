package er.rest.routes.model;

public interface IERXRelationship extends IERXProperty {
	public boolean isToMany();

	public boolean isMandatory();
	
	public IERXEntity destinationEntity();
}
