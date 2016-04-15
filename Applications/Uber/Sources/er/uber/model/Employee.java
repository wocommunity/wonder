package er.uber.model;

import er.taggable.ERTaggable;
import er.taggable.ERTaggableEntity;

public class Employee extends _Employee {
  public ERTaggable<Employee> taggable() {
    return ERTaggable.taggable(this);
  }

  public static ERTaggableEntity<Employee> taggableEntity() {
    return ERTaggableEntity.taggableEntity(Employee.ENTITY_NAME);
  }
}
