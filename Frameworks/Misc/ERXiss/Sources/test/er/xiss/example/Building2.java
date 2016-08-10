package er.xiss.example;

import er.xiss.ERXML;

public class Building2 {
  public static void main(String[] args) {
    ERXML.Doc doc = ERXML.doc();
    doc.comment("This is the structure for a person");
    ERXML.E person = doc.root("person");
    {
      person.e("first-name", "Bob");
      person.e("last-name").setText("Jones");
      person.comment("This is the structure for addresses");
      ERXML.E addresses = person.e("addresses");
      {
        ERXML.E homeAddress = addresses.e("address").set("location", "Home");
        {
          homeAddress.e("address", "100 Main St");
          homeAddress.e("city", "Richmond");
          homeAddress.e("state", "VA");
          homeAddress.e("zip", "23233");
        }
        addresses.cdata("This is a cdata section! <test> of cdata!");
      }
    }
    System.out.println(doc);
  }
}
