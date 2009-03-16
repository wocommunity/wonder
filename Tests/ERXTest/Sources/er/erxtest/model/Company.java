package er.erxtest.model;

import org.apache.log4j.Logger;

public class Company extends _Company {
  private static Logger log = Logger.getLogger(Company.class);
  
  @Override
  public void addToEmployees(er.erxtest.model.Employee object) {
    includeObjectIntoPropertyWithKey(object, Company.EMPLOYEES_KEY);
  }

  @Override
  public void removeFromEmployees(er.erxtest.model.Employee object) {
    excludeObjectFromPropertyWithKey(object, Company.EMPLOYEES_KEY);
  }

}
