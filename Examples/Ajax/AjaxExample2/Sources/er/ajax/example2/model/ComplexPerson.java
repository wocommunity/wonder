package er.ajax.example2.model;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSMutableArray;

public class ComplexPerson extends SimplePerson {

  private ComplexPerson _spouse;
  private NSMutableArray<ComplexPerson> _children = new NSMutableArray<>();
  private int _votes;

  public ComplexPerson(String name, int age) {
    super(name, age);
  }

  public ComplexPerson() {
  }
  
  public int getVotes() {
    return _votes;
  }
  
  public void setVotes(int votes) {
    _votes = votes;
  }
  
  public synchronized void vote() {
    _votes ++;
  }

  public boolean simpleEquals(ComplexPerson person) {
    return super.equals(person);
  }
  
  @Override
  public boolean equals(Object obj) {
    boolean equals = (obj instanceof ComplexPerson);
    if (equals) {
      ComplexPerson other = (ComplexPerson)obj;
      equals = simpleEquals(other);
      if (equals) {
        if (_spouse == null) {
          equals = (other._spouse == null);
        }
        else {
          equals = _spouse.simpleEquals(other._spouse);
        }
      }
      if (equals) {
        equals = (_children.count() == other._children.count());
      }
      if (equals) {
        for (int i = 0; i < _children.count(); i ++) {
          equals &= _children.objectAtIndex(i).simpleEquals(other._children.objectAtIndex(i)); 
        }
      }
    }
    return equals;
  }

  public ComplexPerson getSpouse() {
    return _spouse;
  }

  public void setSpouse(ComplexPerson spouse) {
    _spouse = spouse;
  }

  public NSArray<ComplexPerson> getChildren() {
    return _children;
  }

  public void setChildren(NSArray<ComplexPerson> children) {
    _children = children != null ? children.mutableClone() : null;
  }
}