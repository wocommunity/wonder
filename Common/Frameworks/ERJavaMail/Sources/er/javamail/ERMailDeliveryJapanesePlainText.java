/*
 $Id$

 ERMailDeliveryJapanesePlainText.java - Tatsuya Kawano - tatsuya@mac.com
*/

package er.javamail;

import java.util.*;
import java.io.*;

import javax.activation.*;
import javax.mail.*;
import javax.mail.internet.*;

public class ERMailDeliveryJapanesePlainText extends ERMailDeliveryPlainText {

    public ERMailDeliveryJapanesePlainText() {
        setCharset("ISO-2022-JP");
    }
}

