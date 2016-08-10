package er.xiss.example;

import er.xiss.ERXML;

public class Parsing2 {

  /*----------------------------------------
	<?xml version="1.0" encoding="UTF-8"?>
	<epsp:EpsProtocolDetails xmlns:epsp="http://www.stuzza.at/namespaces/eps/protocol/20031001" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.stuzza.at/namespaces/eps/protocol/20031001 http://www.eps.or.at/eps/protocol/20031001/EPSProtocol-V211.xsd">
  		<epsp:BankResponseDetails>
    		<epsp:ErrorDetails>
      			<epsp:ErrorCode>007</epsp:ErrorCode>
      			<epsp:ErrorMsg>Fehler im XML-Stream: content type 'text/xml' expected but was 'text/plain; charset=ISO-8859-1'</epsp:ErrorMsg>
    		</epsp:ErrorDetails>
  		</epsp:BankResponseDetails>
	</epsp:EpsProtocolDetails>
	----------------------------------------*/
  public static void main(String[] args) {

    String xml;
    xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>";
    xml += "<epsp:EpsProtocolDetails xmlns:epsp=\"http://www.stuzza.at/namespaces/eps/protocol/20031001\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.stuzza.at/namespaces/eps/protocol/20031001 http://www.eps.or.at/eps/protocol/20031001/EPSProtocol-V211.xsd\">";
    xml += "<epsp:BankResponseDetails><epsp:ErrorDetails><epsp:ErrorCode>007</epsp:ErrorCode><epsp:ErrorMsg>Fehler im XML-Stream: content type 'text/xml' expected but was 'text/plain; charset=ISO-8859-1'</epsp:ErrorMsg>";
    xml += "</epsp:ErrorDetails></epsp:BankResponseDetails></epsp:EpsProtocolDetails>";

    System.err.println(xml);

    ERXML.Doc parseDoc = ERXML.doc(xml);

    if(parseDoc.root().child("epsp:BankResponseDetails").child("epsp:ErrorDetails") != null) {
      System.out.println(parseDoc.root().child("epsp:BankResponseDetails").child("epsp:ErrorDetails").childText("epsp:ErrorCode"));
      System.out.println(parseDoc.root().child("epsp:BankResponseDetails").child("epsp:ErrorDetails").childText("epsp:ErrorMsg"));
    }
  }
}
