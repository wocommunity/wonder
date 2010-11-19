package ns.foundation;

public class TestNSData extends BaseTestCase {
  public void testHexString() {
    byte[] test = { 0, 0, 10, 0, 1, -59, 0, 0, -83, -100, 23, 0, 0, 0, 1, 22, 57, 83, 83, -68, 79, 46, 74, 74 };
    String hexString = "00A01C500AD9C17000116395353BC4F2E4A4A";
    
    NSData testData = new NSData(test);
    String h = testData._hexString();
    assertEquals(hexString, h);
  }
}
