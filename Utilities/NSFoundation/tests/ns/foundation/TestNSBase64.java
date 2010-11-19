package ns.foundation;

public class TestNSBase64 extends BaseTestCase {
  public void testEncode() {
    String decoded = "Starting message";
    String encoded = "U3RhcnRpbmcgbWVzc2FnZQ==";
    byte[] data = _NSBase64.encode(decoded.getBytes());
    assertEquals(encoded.getBytes(), data);
  }
  
  public void testDecode() {
    String decoded = "Starting message";
    String encoded = "U3RhcnRpbmcgbWVzc2FnZQ==";
    byte[] data = _NSBase64.decode(encoded.getBytes());
    assertEquals(decoded.getBytes(), data);
  }
}
