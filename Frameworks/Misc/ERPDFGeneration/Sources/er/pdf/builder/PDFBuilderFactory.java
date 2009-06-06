package er.pdf.builder;


public class PDFBuilderFactory {
  public static PDFBuilder newBuilder() {
    return newBuilder(null);
  }
  
  public static PDFBuilder newBuilder(String type) {
    if (type != null && "ujac".equals(type.toLowerCase()))
      return new UJACImpl();
    
    return new FlyingSaucerImpl();
  }
}
