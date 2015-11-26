package er.uber.model;

import org.apache.log4j.Logger;

import er.taggable.ERTaggable;
import er.taggable.ERTaggableEntity;

public class Employee extends _Employee {
  @SuppressWarnings("unused")
  private static Logger log = Logger.getLogger(Employee.class);

  public ERTaggable<Employee> taggable() {
    return ERTaggable.taggable(this);
  }

  public static ERTaggableEntity<Employee> taggableEntity() {
    return ERTaggableEntity.taggableEntity(Employee.ENTITY_NAME);
  }
}
