package er.xiss.example;

import er.xiss.ERXML;

public class Parsing {
  public static void main(String[] args) {
    ERXML.Doc doc = ERXML.doc("<person><first-name>Mike</first-name><last-name>Schrag</last-name><addresses><address location=\"home\"><address>100 Main St</address><city>Richmond</city></address></addresses></person>");
    System.out.println(doc);

    org.w3c.dom.Document w3cDoc = doc.w3c();
    ERXML.Doc doc2 = ERXML.doc(w3cDoc);
    System.out.println(doc2);
  }
}
