package er.xiss.example;

import er.xiss.ERXML;

public class Printing {
  public static void main(String[] args) {
    ERXML.Doc doc = ERXML.doc();
    doc.comment("This is the structure for a person");
    ERXML.E person = doc.root("person").set("firstName", "Mike");

    System.out.println(person);

    System.out.println(doc);
  }
}
