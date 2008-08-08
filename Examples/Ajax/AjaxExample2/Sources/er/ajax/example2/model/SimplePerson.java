package er.ajax.example2.model;

import java.util.UUID;

public class SimplePerson {
  private String _uuid;
  private String _name;
  private int _age;

  public SimplePerson(String name, int age) {
    _uuid = UUID.randomUUID().toString();
    _name = name;
    _age = age;
  }

  public SimplePerson() {
    _uuid = UUID.randomUUID().toString();
  }
  
  public String getUuid() {
    return _uuid;
  }
  
  public void setUuid(String uuid) {
    _uuid = uuid;
  }

  public String getName() {
    return _name;
  }

  public void setName(String name) {
    _name = name;
  }

  public int getAge() {
    return _age;
  }

  public void setAge(int age) {
    _age = age;
  }

  @Override
  public int hashCode() {
    return _uuid.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return obj instanceof SimplePerson && ((SimplePerson) obj)._uuid.equals(_uuid);
  }
}