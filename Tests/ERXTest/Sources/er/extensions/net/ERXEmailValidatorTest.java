package er.extensions.net;

import junit.framework.Assert;

import org.junit.Ignore;

import er.erxtest.ERXTestCase;

public class ERXEmailValidatorTest extends ERXTestCase {
	private ERXEmailValidator allowBoth = new ERXEmailValidator(true, true);
	private ERXEmailValidator allowQuoted = new ERXEmailValidator(true, false);
	private ERXEmailValidator allowLiterals = new ERXEmailValidator(false, true);
	private ERXEmailValidator allowNeither = new ERXEmailValidator(false, false);

	public void testIsValidEmailString() {
		Assert.assertTrue(allowBoth.isValidEmailString("j@x.com"));
		Assert.assertTrue(allowBoth.isValidEmailString("j@ x.com"));
		Assert.assertTrue(allowBoth.isValidEmailString("sales@3com.com"));
		//Anyone know what kind of address this is?
		Assert.assertTrue(allowBoth.isValidEmailString("\"John Doe\" <john.doe@[23:33:A2:22:16:1F]>"));
		Assert.assertTrue(allowBoth.isValidEmailString("\"Someone\" <someone@[192.168.1.100]>"));
		Assert.assertTrue(allowBoth.isValidEmailString("\"Someone\" <someone@[ 	192.168.1.100 ]>"));
		Assert.assertFalse(allowBoth.isValidEmailString("\"Someone\" <someone@ [192.168.1.100]>"));
		Assert.assertTrue(allowBoth.isValidEmailString("\"me\" <me@[my computer]>"));
		Assert.assertFalse(allowQuoted.isValidEmailString("\"me\" <me@[my computer]>"));
		Assert.assertTrue(allowQuoted.isValidEmailString("\"me\" <me@somewhere.com>"));
		Assert.assertFalse(allowLiterals.isValidEmailString("\"me\" <me@[my computer]>"));
		Assert.assertTrue(allowLiterals.isValidEmailString("me@[my computer]"));
		Assert.assertFalse(allowNeither.isValidEmailString("\"me\" <me@[my computer]>"));
		Assert.assertFalse(allowNeither.isValidEmailString("\"me\" <me@somewhere.com>"));
		Assert.assertFalse(allowNeither.isValidEmailString("me@[my computer]"));
		Assert.assertTrue(allowNeither.isValidEmailString("me@somewhere.com"));
	}
	
	@Ignore
	public void testIsValidDomainString() {
		Assert.assertTrue(!Boolean.FALSE.equals(ERXEmailValidator.isValidDomainString("gmail.com", 500)));
		Assert.assertNull(ERXEmailValidator.isValidDomainString("gmail.com", 0));
		Assert.assertTrue(!Boolean.TRUE.equals(ERXEmailValidator.isValidDomainString("flitter.blah", 500)));
		Assert.assertTrue(!Boolean.FALSE.equals(ERXEmailValidator.isValidDomainString("x.com", 500)));
		Assert.assertTrue(!Boolean.TRUE.equals(ERXEmailValidator.isValidDomainString(" x.com", 500)));
	}
	
	public void testHostNameForEmailString() {
		Assert.assertEquals("gmail.com", ERXEmailValidator.hostNameForEmailString("tom@gmail.com"));
		Assert.assertEquals("74.125.224.182", ERXEmailValidator.hostNameForEmailString("tom@[74.125.224.182]"));
		Assert.assertEquals("74.125.224.182", ERXEmailValidator.hostNameForEmailString("\"Thomas Thomson\" < tom@[	74.125.224.182 ]>"));
		Assert.assertEquals("", ERXEmailValidator.hostNameForEmailString("thomas"));
	}
}
