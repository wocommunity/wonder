package er.extensions;

public class ERXFormatterFactory {

    public ERXFormatterFactory() {
        super();
    }
    
    public ERXMultiplyingNumberFormatter multiplyingFormatter() {
        return new ERXMultiplyingNumberFormatter();
    }
    
    public ERXDividingNumberFormatter dividingFormatter() {
        return new ERXDividingNumberFormatter();
    }
    
    public ERXDividingNumberFormatter bytesToKilobytesFormatter() {
        ERXDividingNumberFormatter d = new ERXDividingNumberFormatter();
        d.setPattern("(1024=)0.00");
        return d;
    }

    public ERXDividingNumberFormatter bytesToMegabytesFormatter() {
        ERXDividingNumberFormatter d = new ERXDividingNumberFormatter();
        d.setPattern("(1048576=)0.00");
        return d;
    }

    public ERXMultiplyingNumberFormatter megabytesToBytesFormatter() {
        ERXMultiplyingNumberFormatter d = new ERXMultiplyingNumberFormatter();
        d.setPattern("(1048576=)0.00");
        return d;
    }

    public ERXMultiplyingNumberFormatter megabytesToKilobytesFormatter() {
        ERXMultiplyingNumberFormatter d = new ERXMultiplyingNumberFormatter();
        d.setPattern("(1024=)0.00");
        return d;
    }
    
}
