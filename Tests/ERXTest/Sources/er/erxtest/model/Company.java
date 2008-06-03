package er.erxtest.model;

import org.apache.log4j.Logger;

public class Company extends _Company {
  private static Logger log = Logger.getLogger(Company.class);
  
  public void addToEmployees(er.erxtest.model.Employee object) {
    includeObjectIntoPropertyWithKey(object, Company.EMPLOYEES_KEY);
  }

  public void removeFromEmployees(er.erxtest.model.Employee object) {
    excludeObjectFromPropertyWithKey(object, Company.EMPLOYEES_KEY);
  }

}
