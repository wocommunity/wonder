/*
 * Created on 08.03.2004
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package er.excel;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;

import com.webobjects.foundation.NSArray;
import com.webobjects.foundation.NSDictionary;
import com.webobjects.foundation.NSKeyValueCoding;
import com.webobjects.foundation.NSMutableDictionary;

import er.extensions.foundation.ERXFileUtilities;
import er.extensions.foundation.ERXKeyValueCodingUtilities;

/**
 * Dumps a workbook into the "HTML" needed to re-create it by the EGSimpleTableParser.
 * Uses the property list <code>Codes.plist</code> to re-create the needed class constant
 * dictionary, so the output uses <code>ALIGN_GENERAL</code> instead of <code>0</code>.
 * @author ak
 */
public class EGSimpleTableCreator {
	/** logging support */
	protected final Logger log = Logger.getLogger(EGSimpleTableParser.class);
	
	private static NSDictionary _fontDef;

	private static NSDictionary _styleDef;
	
	private static NSDictionary _cellDef;
	
	private static NSDictionary dictionaryFromClassConstants(Class clazz, NSArray constants) {
		NSMutableDictionary result = new NSMutableDictionary();
		for (Enumeration keys = constants.objectEnumerator(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			Object o = ERXKeyValueCodingUtilities.classValueForKey(clazz, key);
			if(o != null) {
				result.setObjectForKey(key, o);
			}
		}
		return result;
	}
	
	private static NSDictionary dictionaryFromClassConstantDefinition(Class clazz, NSDictionary definition) {
		NSMutableDictionary result = new NSMutableDictionary();
		for (Enumeration keys = definition.keyEnumerator(); keys.hasMoreElements();) {
			String key = (String) keys.nextElement();
			NSArray constants = (NSArray)definition.objectForKey(key);
			NSDictionary parsedKeys = dictionaryFromClassConstants(clazz, constants);
			
			result.setObjectForKey(parsedKeys, key);
		}
		return result;
	}

	static {
		NSDictionary codes = (NSDictionary) ERXFileUtilities.readPropertyListFromFileInFramework(
				"Codes.plist", "ExcelGenerator");
	
		_fontDef = dictionaryFromClassConstantDefinition(HSSFFont.class, 
				(NSDictionary)codes.objectForKey("font"));
		_styleDef = dictionaryFromClassConstantDefinition(HSSFCellStyle.class, 
				(NSDictionary)codes.objectForKey("style"));
		_cellDef = dictionaryFromClassConstantDefinition(HSSFCell.class, 
				(NSDictionary)codes.objectForKey("cell"));
		
	}
	
	private HSSFWorkbook _workbook;
	private StringBuffer _html;
	
	public EGSimpleTableCreator(File file) throws IOException {
		POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(file));
		_workbook = new HSSFWorkbook(fs);
	}
	
	public String html() {
		if(_html == null) {
			_html = new StringBuffer();
			appendWorkbook();
		}
		return _html.toString();
	}
	
	private void appendAttribute(String attribute, Object value) {
		if(value != null) {
			_html.append(' ').append(attribute).append("=\"").append(value).append('"');
		}
	}
	
	private void appendValueForKey(Object object, String key) {
		if(object != null) {
			Object value = NSKeyValueCoding.Utility.valueForKey(object, key);
			appendAttribute(key, value);
		}
	}
	
	private void appendColorForKey(Object object, String key) {
		if(object != null) {
			Object value = NSKeyValueCoding.Utility.valueForKey(object, key);
			if(value instanceof Number) {
				short idx = ((Number)value).shortValue();
				if(idx > 0) {
					HSSFColor col = _workbook.getCustomPalette().getColor(idx);
					if(col != null) {
						String stringValue = "#" + col.getHexString();
						appendAttribute(key, stringValue);
					}
				}
			}
		}
	}
	
	private void appendDataFormatForKey(Object object, String key) {
		if(object != null) {
			Object value = NSKeyValueCoding.Utility.valueForKey(object, key);
			if(value instanceof Number) {
				short idx = ((Number)value).shortValue();
				if(idx > 0) {
					String fmt = _workbook.createDataFormat().getFormat(idx);
					if(fmt != null) {
						appendAttribute(key, fmt);
					}
				}
			}
		}
	}
	
	private void appendValueForKeyWithMapping(Object object, String key, NSDictionary mapping) {
		if(object != null) {
			Object value = NSKeyValueCoding.Utility.valueForKey(object, key);
			NSDictionary map = (NSDictionary)mapping.objectForKey(key);
			if(map != null) {
				Object candidate = map.objectForKey(value);
				if(candidate != null) {
					value = candidate;
				}
			}
			appendAttribute(key, value);
		}
	}
	
	private void appendWorkbook() {
		_html.append("<div>\n");
		for(short i = 0; i < _workbook.getNumberOfFonts(); i++) {
			HSSFFont font = _workbook.getFontAt(i);
			appendFont(font, i);
		}
		for(short i = 0; i < _workbook.getNumCellStyles(); i++) {
			HSSFCellStyle cellStyle = _workbook.getCellStyleAt(i);
			appendCellStyle(cellStyle, i);
		}
		for(short i = 0; i < _workbook.getNumberOfSheets(); i++) {
			HSSFSheet sheet = _workbook.getSheetAt(i);
			String name = _workbook.getSheetName(i);
			appendSheet(sheet, name);
		}
		_html.append("</div>");
	}

	/**
	 * @param sheet
	 */
	private void appendSheet(HSSFSheet sheet, String name) {
		_html.append("<table");
		appendAttribute("border", Integer.valueOf(1));
		appendAttribute("name", name);
		appendValueForKey(sheet, "defaultRowHeightInPoints");
		appendValueForKey(sheet, "defaultColumnWidth");
		_html.append('>');
		for(int i = 0; i < sheet.getPhysicalNumberOfRows(); i++) {
			HSSFRow row = sheet.getRow(i);
			appendRow(row);
		}
		_html.append("</table>\n");
	}

	/**
	 * @param row
	 */
	private void appendRow(HSSFRow row) {
		_html.append("\t<tr");
		appendValueForKey(row, "heightInPoints");
		_html.append(">\n");
		for(int i = 0; i < row.getPhysicalNumberOfCells(); i++) {
			HSSFCell cell = row.getCell(i);
			appendCell(cell);
		}
		_html.append("\t</tr>\n");
	}

	/**
	 * @param cell
	 */
	private void appendCell(HSSFCell cell) {
		_html.append("\t\t<td");
		appendValueForKeyWithMapping(cell, "cellType", _cellDef);
		
		short idx = cell.getCellStyle().getIndex();
		
		if(idx >= 0) {
			appendAttribute("class", "egstyle" + idx);
		}
		
		_html.append('>');
		
		int cellType = cell.getCellType();
		Object value = null;
		switch(cellType) {
			case HSSFCell.CELL_TYPE_NUMERIC:
			value = Double.valueOf(cell.getNumericCellValue());
			break;
			
			case HSSFCell.CELL_TYPE_FORMULA:
			value = cell.getCellFormula();
			break;
			
			case HSSFCell.CELL_TYPE_BOOLEAN:
			value = cell.getBooleanCellValue();
			break;
			
			default:
			value = cell.getStringCellValue();
			break;
		}
		_html.append(value);
		_html.append("</td>\n");
	}

	/**
	 * @param font
	 */
	private void appendFont(HSSFFont font, short i) {
		_html.append("<egfont");
		appendAttribute("id", "egfont" + i);
		appendValueForKey(font, "fontName");
		appendValueForKey(font, "fontHeightInPoints");
		appendValueForKeyWithMapping(font, "boldweight", _fontDef);
		appendValueForKeyWithMapping(font, "underline", _fontDef);
		appendValueForKeyWithMapping(font, "typeOffset", _fontDef);
		appendColorForKey(font, "color");
		appendValueForKey(font, "italic");
		appendValueForKey(font, "strikeout");
		_html.append("/>\n");
	}
	
	/**
	 * @param cellStyle
	 */
	private void appendCellStyle(HSSFCellStyle cellStyle, short i) {
		_html.append("<egstyle");
		appendAttribute("id", "egstyle" + i);
		
		appendValueForKey(cellStyle, "wrapText");
		
		appendValueForKeyWithMapping(cellStyle, "verticalAlignment", _styleDef);
		
		appendColorForKey(cellStyle, "topBorderColor");
		appendColorForKey(cellStyle, "leftBorderColor");
		appendColorForKey(cellStyle, "rightBorderColor");
		appendColorForKey(cellStyle, "bottomBorderColor");
		
		appendValueForKey(cellStyle, "locked");
		appendValueForKey(cellStyle, "rotation");
		appendValueForKey(cellStyle, "indention");
		appendValueForKey(cellStyle, "hidden");
		int idx = cellStyle.getFontIndex();
		appendAttribute("fontIndex", "egfont" + idx);
		
		appendValueForKeyWithMapping(cellStyle, "fillPattern", _styleDef);
		
		appendColorForKey(cellStyle, "fillForegroundColor");
		appendColorForKey(cellStyle, "fillBackgroundColor");

		appendDataFormatForKey(cellStyle, "dataFormat");
		appendValueForKeyWithMapping(cellStyle, "borderTop", _styleDef);
		appendValueForKeyWithMapping(cellStyle, "borderLeft", _styleDef);
		appendValueForKeyWithMapping(cellStyle, "borderRight", _styleDef);
		appendValueForKeyWithMapping(cellStyle, "borderBottom", _styleDef);
		_html.append(" />\n");
	}

}
