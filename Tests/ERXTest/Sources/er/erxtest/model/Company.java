package er.erxtest.model;

public class Company extends _Company {
  @Override
  public void addToEmployees(er.erxtest.model.Employee object) {
    includeObjectIntoPropertyWithKey(object, Company.EMPLOYEES_KEY);
  }

  @Override
  public void removeFromEmployees(er.erxtest.model.Employee object) {
    excludeObjectFromPropertyWithKey(object, Company.EMPLOYEES_KEY);
  }

}
