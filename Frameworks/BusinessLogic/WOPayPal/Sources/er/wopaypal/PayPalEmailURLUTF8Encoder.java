package er.wopaypal;

/**
 * 
 * This class originally came from the w3c.org
 * We have to customize this encoding because PayPal expects the "/" character to be unencoded in an "email link" submission.
 * This is a hack-and-a-half. Stupid non-compliant implementations.  Grumble.  Grumble.  - TC
 * 
 */


/**
 * Map of PayPal's e-mail link character encoding scheme -- sorry, but this map sort-of follows a US qwerty keyboard layout
 *
 * The lower ASCII chars and behave as expected:
 * abcdefghijklmnopqrstuvxyzABCDEFGHIJKLMNOPQRSTUVWXYZ   
 *
 * Here's how the numbers and upper ASCII behave: (numbers are normal)
 * `   1   2   3   4   5   6   7   8   9   0   -   =
 * %60 1   2   3   4   5   6   7   8   9   0   -   %3D
 * 
 * ~   !   @   #   $   %   ^   &   *   (   )   _   +
 * %7E %21 %40 %23 %24 %25 %5E %26 *   %28 %29 _   %2B
 * 
 * [   ]   \   {   }   |   ;   '   :   "   ,   .   /   <   >   ?
 * %5B %5D %5C %7B %7D %7C %3B %27 %3A %22 %2C .   /   %3C %3E %3F
 * 
 */


/** 
 * Provides a method to encode any string into a URL-safe
 * form.
 * Non-ASCII characters are first encoded as sequences of
 * two or three bytes, using the UTF-8 algorithm, before being
 * encoded as %HH escapes.
 */
public class PayPalEmailURLUTF8Encoder
{

    /** Array of hex conversions for characters.
     */
    final static String[] hex = { // note: TC ran a toUpperCase on these; they were originally lower case.
    "%00", "%01", "%02", "%03", "%04", "%05", "%06", "%07",
    "%08", "%09", "%0A", "%0B", "%0C", "%0D", "%0E", "%0F",
    "%10", "%11", "%12", "%13", "%14", "%15", "%16", "%17",
    "%18", "%19", "%1A", "%1B", "%1C", "%1D", "%1E", "%1F",
    "%20", "%21", "%22", "%23", "%24", "%25", "%26", "%27",
    "%28", "%29", "%2A", "%2B", "%2C", "%2D", "%2E", "%2F",
    "%30", "%31", "%32", "%33", "%34", "%35", "%36", "%37",
    "%38", "%39", "%3A", "%3B", "%3C", "%3D", "%3E", "%3F",
    "%40", "%41", "%42", "%43", "%44", "%45", "%46", "%47",
    "%48", "%49", "%4A", "%4B", "%4C", "%4D", "%4E", "%4F",
    "%50", "%51", "%52", "%53", "%54", "%55", "%56", "%57",
    "%58", "%59", "%5A", "%5B", "%5C", "%5D", "%5E", "%5F",
    "%60", "%61", "%62", "%63", "%64", "%65", "%66", "%67",
    "%68", "%69", "%6A", "%6B", "%6C", "%6D", "%6E", "%6F",
    "%70", "%71", "%72", "%73", "%74", "%75", "%76", "%77",
    "%78", "%79", "%7A", "%7B", "%7C", "%7D", "%7E", "%7F",
    "%80", "%81", "%82", "%83", "%84", "%85", "%86", "%87",
    "%88", "%89", "%8A", "%8B", "%8C", "%8D", "%8E", "%8F",
    "%90", "%91", "%92", "%93", "%94", "%95", "%96", "%97",
    "%98", "%99", "%9A", "%9B", "%9C", "%9D", "%9E", "%9F",
    "%A0", "%A1", "%A2", "%A3", "%A4", "%A5", "%A6", "%A7",
    "%A8", "%A9", "%AA", "%AB", "%AC", "%AD", "%AE", "%AF",
    "%B0", "%B1", "%B2", "%B3", "%B4", "%B5", "%B6", "%B7",
    "%B8", "%B9", "%BA", "%BB", "%BC", "%BD", "%BE", "%BF",
    "%C0", "%C1", "%C2", "%C3", "%C4", "%C5", "%C6", "%C7",
    "%C8", "%C9", "%CA", "%CB", "%CC", "%CD", "%CE", "%CF",
    "%D0", "%D1", "%D2", "%D3", "%D4", "%D5", "%D6", "%D7",
    "%D8", "%D9", "%DA", "%DB", "%DC", "%DD", "%DE", "%DF",
    "%E0", "%E1", "%E2", "%E3", "%E4", "%E5", "%E6", "%E7",
    "%E8", "%E9", "%EA", "%EB", "%EC", "%ED", "%EE", "%EF",
    "%F0", "%F1", "%F2", "%F3", "%F4", "%F5", "%F6", "%F7",
    "%F8", "%F9", "%FA", "%FB", "%FC", "%FD", "%FE", "%FF"
  };

  /** 
   * <p>Encode a string to the "special" PayPal version of "x-www-form-urlencoded" form, enhanced
   * with the UTF-8-in-URL proposal. This is what happens:</p>
   * 
   * <ul>
   * <li><p>The ASCII characters 'a' through 'z', 'A' through 'Z',
   * and '0' through '9' remain the same.</p>
   * 
   * <li><p>The unreserved characters - _ . / remain the same.</p>
   * 
   * <li><p>The space character ' ' is converted into a plus sign '+'.</p>
   * 
   * <li><p>All other ASCII characters are converted into the
   * 3-character string "%xy", where xy is
   * the two-digit hexadecimal representation of the character
   * code.</p>
   * 
   * <li><p>All non-ASCII characters are encoded in two steps: first
   * to a sequence of 2 or 3 bytes, using the UTF-8 algorithm;
   * secondly each of these bytes is encoded as "%xx".</p>
   * </ul>
   * 
   * @param s The string to be encoded
   * @return The encoded string
   */
  public static String encode(String s)
  {
    StringBuilder sbuf = new StringBuilder();
    int len = s.length();
    for (int i = 0; i < len; i++) {
        int ch = s.charAt(i);
        if ('A' <= ch && ch <= 'Z') {           // 'A'..'Z'
            sbuf.append((char)ch);
        } else if ('a' <= ch && ch <= 'z') {    // 'a'..'z'
            sbuf.append((char)ch);
        } else if ('0' <= ch && ch <= '9') {    // '0'..'9'
            sbuf.append((char)ch);
        } else if (ch == ' ') {                 // space
            sbuf.append('+');
        } else if (ch == '-' || ch == '_'       // unreserved
            || ch == '.' || ch == '/') {        // this is the part where we hack the class by adding the '/' char to the unreserved set
            sbuf.append((char)ch);
        } else if (ch <= 0x007f) {              // other ASCII
            sbuf.append(hex[ch]);
        } else if (ch <= 0x07FF) {              // non-ASCII <= 0x7FF
            sbuf.append(hex[0xc0 | (ch >> 6)]);
            sbuf.append(hex[0x80 | (ch & 0x3F)]);
        } else {                                // 0x7FF < ch <= 0xFFFF
            sbuf.append(hex[0xe0 | (ch >> 12)]);
            sbuf.append(hex[0x80 | ((ch >> 6) & 0x3F)]);
            sbuf.append(hex[0x80 | (ch & 0x3F)]);
        }
    }
    return sbuf.toString();
  }

}
