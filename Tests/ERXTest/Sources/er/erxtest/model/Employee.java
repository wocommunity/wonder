package er.erxtest.model;

public class Employee extends _Employee {
  @Override
  public void setCompany(Company company) {
    takeStoredValueForKey(company, Employee.COMPANY_KEY);
  }
}
