/*
 * Created on 04.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.excel;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRichTextString;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSData;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSForwardException;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;
import com.webobjects.foundation.NSNumberFormatter;

import er.extensions.formatters.ERXNumberFormatter;
import er.extensions.foundation.ERXDictionaryUtilities;
import er.extensions.foundation.ERXKeyValueCodingUtilities;


/**
 * Parses an input stream for tables and converts them into excel 
 * sheets. You must have a surrounding element as there is only one 
 * root element in XML allowed.
 * <blockquote>
 * Eg:<code>&lt;div&gt;&lt;table 1&gt;&lt;table 2&gt;...&lt;/div&gt;</code>
 * </blockquote>
 * <p>You <emp>must</emp> take care that your content is XML readable.
 * There is support for a CSS-like style tagging. Either supply
 * font and style dictionaries in the constructor or via &lt;style&gt; and &lt;font&gt; tags.
 * The tags are shown in the example, but mainly the attributes are named the same as the properties
 * of the {@link org.apache.poi.hssf.usermodel.HSSFCellStyle HSSFCellStyle} and {@link org.apache.poi.hssf.usermodel.HSSFFont HSSFFont}
 * objects. The symbolic names from theses classes (eg. <code>ALIGN_RIGHT</code>) are also supported.
 * In addition, the tags <emp>must</emp> have an <code>id</code> attribute and can specify an
 * <code>extends</code> attribute that contains the ID of the style that is extended - all properties from this
 * style and it's predecessors are copied to the current style.</p>
 * <p>In addition, you can specify an attribute in any &lt;table&gt;, &lt;tr&gt;, &lt;th&gt; and &lt;td&gt; tag.
 * When this happens a new style is created and it applies to the contents of this tag.
 * The value is copied as text from the cell's content, so you better take care that it is parsable
 * and matches the <code>cellStyle</code> and <code>cellFormat</code> definition.</p> 
 * <p>The parser also supports the <code>some-name</code> attribute names in addition to 
 * <code>someName</code> as using the <b>Reformat</b> command in WOBuilder messes up the case 
 * of the tags. When used in .wod files, the attributes must be enclosed in quotes 
 * (<code>"cell-type"=foo;</code>). Some care must be taken when the attributes in the current node override the ones 
 * from the parent as this is not thoroughly tested.</p>
 * <p>A client would use this class like: <pre><code>
 * EGSimpleTableParser parser = new EGSimpleTableParser(new ByteArrayInputStream(someContentString));
 * NSData result = parser.data();
 * </code></pre></p>
 * @author ak
 */
public class EGSimpleTableParser {
	
	/** logging support */
	protected final Logger log = Logger.getLogger(EGSimpleTableParser.class);
	
	private InputStream _contentStream;
	private HSSFWorkbook _workbook;
	private NSMutableDictionary _styles = new NSMutableDictionary();
	private NSMutableDictionary _fonts = new NSMutableDictionary();
	private NSMutableDictionary _styleDicts;
	private NSMutableDictionary _fontDicts;

	public EGSimpleTableParser(InputStream contentStream) {
		this(contentStream, null, null);
 	}
	
	public EGSimpleTableParser(InputStream contentStream, NSDictionary fontDicts, NSDictionary styleDicts) {
		_contentStream = contentStream;
		_fontDicts = new NSMutableDictionary();
		if(_fontDicts != null) {
			_fontDicts.addEntriesFromDictionary(fontDicts);
		}
		_styleDicts = new NSMutableDictionary();
		if(styleDicts != null) {
			_styleDicts.addEntriesFromDictionary(styleDicts);
		}
	}

	public void writeToStream(OutputStream out) throws IOException {
		workbook().write(out);
		out.close();
	}
	
	public NSData data() {
		try {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			workbook().write(out);
			out.close();
			
			return new NSData(out.toByteArray());
		} catch (IOException e) {
			log.error(e,e);
		}
		return null;
	}

    public HSSFWorkbook workbook() {
    	if(_workbook == null) {
    		parse();
    	}
    	return _workbook;
    }
    
    private String nodeValueForKey(Node node, String key, String defaultValue) {
    	NamedNodeMap attributes = node.getAttributes();
    	String result = defaultValue;
        
        if(attributes.getNamedItem(key) != null) {
            result = attributes.getNamedItem(key).getNodeValue();
            if (result == null || result.length() == 0) {
                result = defaultValue;
            }
		}    
		return result;
	}
    
    private String keyPathToAttributeString(String aString) {
        int i, cnt = aString.length();
        StringBuilder result = new StringBuilder(cnt*2);
        for(i = 0; i < cnt; i++) {
            char c = aString.charAt(i);
            if(Character.isUpperCase(c)) {
                result.append('-');
                result.append(Character.toLowerCase(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    private String attributeStringToKeyPath(String aString) {
        int i, cnt = aString.length();
        boolean upperNext = false;
        StringBuilder result = new StringBuilder(cnt*2);
        for(i = 0; i < cnt; i++) {
            char c = aString.charAt(i);
            if(upperNext) {
                if(Character.isLowerCase(c)) {
                    c = Character.toUpperCase(c);
                }
                result.append(c);
                upperNext = false;
            } else if(c == '-') {
                upperNext = true;
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }
    
    /**
     * @param fontDictionary
     * @param tableNode
     */
    private void addEntriesFromNode(NSMutableDictionary dictionary, Node node) {
        NamedNodeMap attributes = node.getAttributes();
        for(int i = 0; i < attributes.getLength(); i ++) {
            Node n = attributes.item(i);
            String key = attributeStringToKeyPath(n.getNodeName());
            String value = n.getNodeValue();
            if("".equals(value)) {
                dictionary.removeObjectForKey(key);
            } else {
                dictionary.setObjectForKey(value, key);
            }
        }
    }

    private String dictValueForKey(NSDictionary dict, String key, String defaultValue) {
    	String result = (String)dict.objectForKey(key);
    	if(result == null) {
            result = (String)dict.objectForKey(keyPathToAttributeString(key));
        }
		if(result == null) {
			result = defaultValue;
		}    
		return result;
	}
    
    private void takeBooleanValueForKey(NSDictionary dict, String key, Object target, String defaultValue) {
    	String value = dictValueForKey(dict, key, defaultValue);
    	if(value != null) {
    		NSKeyValueCoding.Utility.takeValueForKey(target, Boolean.valueOf(value), key);
    	}
    }
    
    private void takeNumberValueForKey(NSDictionary dict, String key, Object target, String defaultValue) {
    	String value = dictValueForKey(dict, key, defaultValue);
    	if(value != null) {
    		NSKeyValueCoding.Utility.takeValueForKey(target, Integer.valueOf(value), key);
    	}
    }
    
    private void takeClassValueForKey(NSDictionary dict, String key, Object target, String defaultValue) {
    	String value = dictValueForKey(dict, key, defaultValue);
    	if(value != null) {
   			Number number = (Number)ERXKeyValueCodingUtilities.classValueForKey(target.getClass(), value);
			NSKeyValueCoding.Utility.takeValueForKey(target, number, key);
    	}
    }
    
    private void takeClassValueForKey(NSDictionary dict, String key, Object target, Class source, String defaultValue) {
    	String value = dictValueForKey(dict, key, defaultValue);
    	if(value != null) {
   			Number number = (Number)ERXKeyValueCodingUtilities.classValueForKey(source, value);
			NSKeyValueCoding.Utility.takeValueForKey(target, number, key);
    	}
    }

    private void parse() {
    	try {
    		Document document = null;
    		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
    		dbf.setNamespaceAware(true);
    		dbf.setIgnoringElementContentWhitespace(true);
    		DocumentBuilder builder = dbf.newDocumentBuilder();
    		InputStream stream = _contentStream;
    		document = builder.parse(stream);
    		
    		_workbook = new HSSFWorkbook();
    		
    		if (log.isDebugEnabled())
    		    log.debug(document.getDocumentElement());
    		
    		NodeList nodes = document.getDocumentElement().getChildNodes();
    		for (int i = 0; i < nodes.getLength(); i++) {
    		    Node node = nodes.item(i);
    		    if(node.getNodeType() == Node.ELEMENT_NODE) {
    		        parseNode(node);
    		    }
    		}
    	} catch(Exception ex) {
    	    throw new NSForwardException(ex);
    	}
    }
    
    private void parseNode(Node node) {
    	String tagName = node.getLocalName().toLowerCase();
    	if("font".equals(tagName)) {
    		parseFont(node);
    	} else if("style".equals(tagName)) {
    		parseStyle(node);
    	} else if("table".equals(tagName)) {
    		parseTable(node);
    	} else {
    		// descend
    		NodeList nodes = node.getChildNodes();
    		for (int i = 0; i < nodes.getLength(); i++) {
    			Node child = nodes.item(i);
    			if(child.getNodeType() == Node.ELEMENT_NODE) {
    				parseNode(child);
    			}
    		}
    	}
    }
    
    private void parseStyle(Node node) {
		String id =  nodeValueForKey(node, "id", null);
		if(id != null) {
			// we're only handling styles with IDs
			NSMutableDictionary dict = new NSMutableDictionary();
			
			String extendsID = nodeValueForKey(node, "extends", null);
			if(extendsID != null) {
				NSDictionary otherDict = (NSDictionary)_styleDicts.objectForKey(extendsID);
				if(otherDict == null) {
					throw new NullPointerException("Extends Style Id not found");
				}
				dict.addEntriesFromDictionary(otherDict);
			}
			addEntriesFromNode(dict, node);
			
			_styleDicts.setObjectForKey(dict, id);
		}
    }
    
    private void parseFont(Node node) {
     	String id =  nodeValueForKey(node, "id", null);
    	if(id != null) {
    		// we're only handling fonts with IDs
    		NSMutableDictionary dict = new NSMutableDictionary();
    		
    		String extendsID = nodeValueForKey(node, "extends", null);
    		if(extendsID != null) {
    			NSDictionary otherDict = (NSDictionary)_fonts.objectForKey(extendsID);
    			if(otherDict == null) {
    				throw new NullPointerException("Extends Font Id not found");
    			}
    			dict.addEntriesFromDictionary(otherDict);
    		}
    		addEntriesFromNode(dict, node);
    		
    		_fontDicts.setObjectForKey(dict, id);
    	}
    }
    
    private void parseTable(Node tableNode) {
    	String sheetName = nodeValueForKey(tableNode, "name", "Unnamed Sheet " + (_workbook.getNumberOfSheets() + 1));
    	NSMutableDictionary sheetDict = new NSMutableDictionary();
    	addEntriesFromNode(sheetDict, tableNode);
        if(sheetName.matches("[\\/\\\\\\*\\?\\[\\]]")) {
            sheetName = sheetName.replaceAll("[\\/\\\\\\*\\?\\[\\]]", "-");
            log.warn("Illegal characters in sheet name (/\\*?[]): " + sheetName);
        }
        if(sheetName.length() > 31) {
            sheetName = sheetName.substring(0,31);
            log.warn("Sheet name too long (max 31 Characters): " + sheetName);
        }
        HSSFSheet sheet = _workbook.createSheet(sheetName);
 
        NodeList rowNodes = tableNode.getChildNodes();
    	
    	//takeNumberValueForKey(tableNode, "defaultColumnWidthInPoints", workbook, null);
    	takeNumberValueForKey(sheetDict, "defaultColumnWidth", sheet, null);
    	takeNumberValueForKey(sheetDict, "defaultRowHeight", sheet, null);
    	takeNumberValueForKey(sheetDict, "defaultRowHeightInPoints", sheet, null);
    	
    	if (log.isDebugEnabled()) log.debug("Sheet: " + _workbook.getNumberOfSheets());
    	
    	int rowNum = 0;
    	for (int j = 0; j < rowNodes.getLength(); j++) {
    		Node rowNode = rowNodes.item(j);
    		if(rowNode.getNodeType() == Node.ELEMENT_NODE
    				&& "tr".equals(rowNode.getLocalName().toLowerCase())) {
    			NSMutableDictionary rowDict = new NSMutableDictionary(sheetDict);
    			addEntriesFromNode(rowDict, rowNode);

                        if(log.isDebugEnabled()) {
                            log.debug("Row: " + rowNum);
                        }
    			HSSFRow row = sheet.createRow(rowNum);
    			
    			rowNum = rowNum + 1;
    			NodeList cellNodes = rowNode.getChildNodes();
    			for (int k = 0; k < cellNodes.getLength(); k++) {
    				Node cellNode = cellNodes.item(k);
    				if(cellNode.getNodeType() == Node.ELEMENT_NODE
    						&& ("td".equals(cellNode.getLocalName().toLowerCase())
    								|| "th".equals(cellNode.getLocalName().toLowerCase()))) {
    					int currentColumnNumber = row.getPhysicalNumberOfCells();
						HSSFCell cell = row.createCell(currentColumnNumber); 
    					Object value = null;
    					if(cellNode.getFirstChild() != null) {
    	   					value = cellNode.getFirstChild().getNodeValue();
     					}
    					
    					NSMutableDictionary cellDict = new NSMutableDictionary(rowDict);
    					addEntriesFromNode(cellDict, cellNode);
    					
    					String cellTypeName = dictValueForKey(cellDict, "cellType", "CELL_TYPE_NUMERIC");
    					String cellFormatName = dictValueForKey(cellDict, "cellFormat", "0.00;-;-0.00");
    					
    					if(log.isDebugEnabled()) {
    						log.debug(value + ": " + cellFormatName + "-" + cellTypeName);
    					}
    					Integer cellType = (Integer)ERXKeyValueCodingUtilities.classValueForKey(Cell.class, cellTypeName);
    					
    					switch(cellType.intValue()) {
    						case HSSFCell.CELL_TYPE_FORMULA:
    							cell.setCellType(HSSFCell.CELL_TYPE_FORMULA);
    						cell.setCellFormula(value != null ? value.toString() : null);
    						break;
    						case HSSFCell.CELL_TYPE_NUMERIC:
    							try {
    								if(value != null) {
    									NSNumberFormatter f = ERXNumberFormatter.numberFormatterForPattern(cellFormatName);
    									Number numberValue = (Number)f.parseObject(value.toString());
    									if(log.isDebugEnabled()) {
    										log.debug(f.pattern() + ": " + numberValue);
    									}
    									if(numberValue != null) {
    										cell.setCellValue(numberValue.doubleValue());
    									}
    								}
     								break;
    							} catch (ParseException e1) {
    								log.info(e1);
    							}
    							
    						case HSSFCell.CELL_TYPE_BOOLEAN:
    							cell.setCellType(cellType.intValue());
    							if (value != null) {
    								try {
    									Integer integer = Integer.parseInt(value.toString());
    									cell.setCellValue(integer > 0);
    								} catch (NumberFormatException ex) {
    									if (log.isDebugEnabled()) {
    										log.debug(ex.getMessage(), ex);
    									}
    	    							cell.setCellValue(new Boolean(value.toString()));
    								}
    							}
    							break;
    							
    						case HSSFCell.CELL_TYPE_STRING:
							default:
								cell.setCellType(cellType.intValue());
								cell.setCellValue(new HSSFRichTextString(value != null ? value.toString() : null));
								break;
    					}
    					
    					String cellWidthString = nodeValueForKey(cellNode, "width", null);
    					if(cellWidthString != null && cellWidthString.indexOf("%") < 0) {
    						if ("auto".equalsIgnoreCase(cellWidthString)) {
	      						try {
	      							sheet.autoSizeColumn((short) currentColumnNumber);
	      						} catch (Exception ex) {
	      							log.warn(ex);
	      						}
    						} else {
        						try {
        							short width = Integer.valueOf(cellWidthString).shortValue();
        							sheet.setColumnWidth(currentColumnNumber, width * 256);
        						} catch (Exception ex) {
        							log.warn(ex);
        						}
    						}
    					}
    					
    					String cellHeightString = nodeValueForKey(cellNode, "height", null);
    					if(cellHeightString != null && cellHeightString.indexOf("%") < 0) {
    						try {
    							short height = Integer.valueOf(cellHeightString).shortValue();
    							row.setHeightInPoints(height);
    						} catch (Exception ex) {
    							log.warn(ex);
    						}
    					}
    					
    					HSSFCellStyle style = styleWithDictionary(cellDict);
    					
    					if(style != null) {
    						cell.setCellStyle(style);
    					}
    					
    					String colspanString = dictValueForKey(cellDict, "colspan", "1");
    					short colspan = Integer.valueOf(colspanString).shortValue();
    					for(int col = 1; col < colspan; col++) {
    						int nextColumnNumber = row.getPhysicalNumberOfCells();
							cell = row.createCell(nextColumnNumber);
    						if(style != null) {
    							cell.setCellStyle(style);
    						}
    					}
    					
    					if(log.isDebugEnabled()) {
    					    log.debug("Cell: " + value);
    					}
    				}
    			}
    		}
    	}
    }
    
    private HSSFFont fontWithID(String id) {
    	HSSFFont font = (HSSFFont)_fonts.objectForKey(id);
    	if(font == null) {
    		font = _workbook.createFont();
    		
    		NSDictionary dict = (NSDictionary)_fontDicts.objectForKey(id);
    		String value;
    		
    		value = dictValueForKey(dict, "name", null);
    		if(value != null) {
    			font.setFontName(value);
    		}
    		
    		takeNumberValueForKey(dict, "fontHeight", font, null);
    		takeNumberValueForKey(dict, "fontHeightInPoints", font, null);
    		takeNumberValueForKey(dict, "color", font, null);
    		
    		takeBooleanValueForKey(dict, "italic", font, null);
    		takeBooleanValueForKey(dict, "strikeout", font, null);
    		
    		takeClassValueForKey(dict, "underline", font, Font.class, null);
    		takeClassValueForKey(dict, "typeOffset", font, Font.class, null);
    		takeClassValueForKey(dict, "boldweight", font, Font.class, null);
    		
    		_fonts.setObjectForKey(font, id);
    	}
    	
    	return font;
    }
    
    private static final NSArray STYLE_KEYS = new NSArray(new Object[] {
    		"font","hidden","locked","wrapText",
			"leftBorderColor","rightBorderColor","topBorderColor","bottomBorderColor",
			"borderLeft","borderRight","borderTop","borderBottom",
			"fillForegroundColor","fillBackgroundColor","fillPattern",
			"rotation","indention", "wrapText",
			"alignment","verticalAlignment","format"
	});
    
    private HSSFCellStyle styleWithDictionary(NSDictionary dict) {
    	String cellClass = dictValueForKey(dict, "class", null);
    	
    	if(log.isDebugEnabled()) {
        	log.debug("before - " + cellClass + ": " + dict);
    	}
    	dict = ERXDictionaryUtilities.dictionaryFromObjectWithKeys(dict, STYLE_KEYS);
    	if(cellClass != null) {
    		// first, we pull in the default named styles, remembering
    		// we can have multiple styles like 'class="header bold"
    		String styles[] = cellClass.split(" +");
    		NSMutableDictionary stylesFromClass = new NSMutableDictionary();
    		for (String string : styles) {
    			NSDictionary current = ((NSDictionary)_styleDicts.objectForKey(string));
    			if(current == null) {
    				throw new IllegalArgumentException("Cell Style not found: " + cellClass);
    			} else {
    				stylesFromClass.addEntriesFromDictionary(current);
    			}
    		}
    		stylesFromClass = ERXDictionaryUtilities.dictionaryFromObjectWithKeys(stylesFromClass, STYLE_KEYS).mutableClone();
    		stylesFromClass.addEntriesFromDictionary(dict);
    		dict = stylesFromClass.immutableClone();
    	}
    	if(log.isDebugEnabled()) {
        	log.debug("after - " + cellClass + ": " + dict);
    	}
    	
    	HSSFCellStyle cellStyle = (HSSFCellStyle)_styles.objectForKey(dict);
    	if(cellStyle == null) {
    		cellStyle = _workbook.createCellStyle();
    		
    		String fontID = dictValueForKey(dict, "font", null);
    		if(fontID != null) {
    			HSSFFont font = fontWithID(fontID);
    			if(font == null) {
    				throw new IllegalArgumentException("Font ID not found!");
    			}
    			cellStyle.setFont(font);
    		}
    		takeBooleanValueForKey(dict, "hidden", cellStyle, null);
    		takeBooleanValueForKey(dict, "locked", cellStyle, null);
    		takeBooleanValueForKey(dict, "wrapText", cellStyle, null);
    		
    		takeNumberValueForKey(dict, "leftBorderColor", cellStyle, null);
    		takeNumberValueForKey(dict, "rightBorderColor", cellStyle, null);
    		takeNumberValueForKey(dict, "topBorderColor", cellStyle, null);
    		takeNumberValueForKey(dict, "bottomBorderColor", cellStyle, null);
    		
    		takeNumberValueForKey(dict, "fillBackgroundColor", cellStyle, null);
    		takeNumberValueForKey(dict, "fillForegroundColor", cellStyle, null);
    		takeNumberValueForKey(dict, "indention", cellStyle, null);
    		takeNumberValueForKey(dict, "rotation", cellStyle, null);
    		
    		takeClassValueForKey(dict, "borderLeft", cellStyle, CellStyle.class, null);
    		takeClassValueForKey(dict, "borderRight", cellStyle, CellStyle.class, null);
    		takeClassValueForKey(dict, "borderTop", cellStyle, CellStyle.class, null);
    		takeClassValueForKey(dict, "borderBottom", cellStyle, CellStyle.class, null);
    		
    		takeClassValueForKey(dict, "fillPattern", cellStyle, CellStyle.class, null);
    		takeClassValueForKey(dict, "alignment", cellStyle, CellStyle.class, null);
    		takeClassValueForKey(dict, "verticalAlignment", cellStyle, CellStyle.class, null);
    		
    		String formatString = dictValueForKey(dict, "format", null);
    		if(formatString != null) {
    			HSSFDataFormat format = _workbook.createDataFormat();
    			short formatId = format.getFormat(formatString);
    			cellStyle.setDataFormat(formatId);
    		}
    		
    		_styles.setObjectForKey(cellStyle, dict);
                if(log.isDebugEnabled()) {
                    log.debug("Created style (" + cellClass + "): " + dict);
                }
    	}
    	return cellStyle;
    }
    
}