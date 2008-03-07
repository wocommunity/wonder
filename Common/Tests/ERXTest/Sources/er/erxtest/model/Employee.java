package er.erxtest.model;

import org.apache.log4j.Logger;

public class Employee extends _Employee {
  private static Logger log = Logger.getLogger(Employee.class);
  
  public void setCompany(Company company) {
    takeStoredValueForKey(company, Employee.COMPANY_KEY);
  }
}
