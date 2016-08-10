package er.xiss.example;

import er.xiss.ERXML;

public class Building {
  public static void main(String[] args) {
    ERXML.Doc doc = ERXML.doc(
        ERXML.comment("This is the structure for a person"),
        ERXML.e("person",
            ERXML.e("first-name", "Mike"),
            ERXML.e("last-name", "Schrag"),
            ERXML.e("addresses",
                ERXML.e("address",
                    ERXML.a("location", "Home"),
                    ERXML.e("address", "100 Main St."),
                    ERXML.e("city", "Richmond"),
                    ERXML.e("state", "VA"),
                    ERXML.e("zip", "23233")
                    ),
                    ERXML.cdata("This is a cdata section! <test> of cdata!")
                )
            )
        );
    System.out.println(doc);
  }
}
