package er.excel;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

/**
 * This class simplifies the creation of HSSFWorkbook objects, allowing one to create
 * a workbook and add data to it with fairly simple and (hopefully) self-explanatory code.
 * It also provides a useful subset of a very large API.
 * <p>
 * The <code>sheetWithNumber(int)</code> and <code>sheetWithName(String)</code> methods
 * may return a null object, but any of the other methods for returning a sheet or a cell
 * are guaranteed to not return a null. If necessary, a sheet or a cell will be created.
 * The <code>stringAtLocation</code> and <code>numberAtLocation</code> methods are also
 * guaranteed to not return null. This allows one to use code like the following, without
 * fear of an exception.
 * <pre><code>
 * EGSimpleWorkbookHelper helper = new EGSimpleWorkbookHelper();
 * helper.currentSheet().setNumberAtLocation(24, 2, 2);
 * helper.currentSheet().setStringAtLocation("Hello", 3, 4);
 * String aNumber = helper.currentSheet().numberAtLocation(1, 1).toString();
 * </code></pre>
 * The POI classes use numbers for rows and, implicitly, columns. This class also allows
 * the use of the string-based column names one always sees on spreadsheets. So the methods
 * that take a row number and a column number and return a call can also take a column name,
 * such as "A", "B", ..., "Z", "AA", "AB", and so on. Using a column name not wholly
 * composed of capital letters will generate a IllegalArgumentException.
 */
public class EGSimpleWorkbookHelper {

	protected HSSFWorkbook _workbook;

	private int currentSheetIdx;

	private ArrayList<HSSFSheet> sheets;

	private static final String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";

	private static final int NotAValidColumnName = -1;

	private static int numberForString(String column) {
		if (column == null || "".equals(column))
			throw new java.lang.IllegalArgumentException("Column name \""+column+"\" is not a valid name. Use capital letters only.");

		int columnNumber = 0;
		for (int idx = 0, len = column.length(); idx < len; idx++) {
			int offset = alphabet.indexOf(column.charAt(idx));
			if (offset == NotAValidColumnName)
				throw new java.lang.IllegalArgumentException("Column name \""+column+"\" is not a valid name. Use capital letters only.");
			columnNumber = (columnNumber * 26) + offset;
		}
		return columnNumber;
	}

	public EGSimpleWorkbookHelper() {
		_workbook = new HSSFWorkbook();
		currentSheetIdx = 0;
		sheets = new ArrayList<>();
		sheets.add(_workbook.createSheet("Sheet0"));
	}

	public static EGSimpleWorkbookHelper newInstance() {
		return new EGSimpleWorkbookHelper();
	}

	public EGSimpleWorkbookHelper(HSSFWorkbook workbook) {
		_workbook = workbook;
		currentSheetIdx = 0;
		sheets = new ArrayList<>();
		for (int idx = 0, num = _workbook.getNumberOfSheets(); idx < num; idx++) {
			sheets.add(_workbook.getSheetAt(idx));
		}
	}

	/**
	 * Returns the HSSFWorkbook instance that this class is helping to build.
	 */
	public HSSFWorkbook workbook() { return _workbook; }

	private int numberOfSheet(HSSFSheet sheet) {
		return sheets.indexOf(sheet);
	}

	/**
	 * Switches the current sheet to a sheet identified by number, with the number
	 * being set by the order in which the sheets were created.
	 */
	public void switchToSheetNumber(int number) {
		// will only enter the for loop if number is greater than the number of sheets.
		//
		for (int idx = sheets.size(); idx <= number; idx++) {
			sheets.add(_workbook.createSheet("Sheet"+idx));
		}
		currentSheetIdx = number;
	}

	/**
	 * Switches the current sheet to a sheet identified by name. If a name is not explicitly
	 * set on a sheet, its default name is "Sheet#", with "#" being the number of the sheet.
	 */
	public void switchToSheetWithName(String name) {
		HSSFSheet sheet = sheetWithName(name);
		if (sheet == null) {
			sheet = _workbook.createSheet(name);
			sheets.add(sheet);
		}
		currentSheetIdx = numberOfSheet(sheet);
	}

	/**
	 * Returns the sheet that has been designated as the current sheet. This method
	 * is guaranteed to return a sheet and it will not return <code>null</code>.
	 */
	public HSSFSheet currentSheet() {
		return sheets.get(currentSheetIdx);
	}

	/**
	 * Returns the sheet in this workbook that is identified by a number, with the numbers
	 * being set in the order in which the sheets were created. This method is not guaranteed
	 * to return a sheet and may return <code>null</code>. To avoid getting a null, use the
	 * switchToSheetNumber() method and then call currentSheet().
	 */
	public HSSFSheet sheetWithNumber(int number) {
		if (number < sheets.size())
			return sheets.get(number);
		else
			return null;
	}

	/**
	 * Returns the sheet in this workbook that is identified by a particular name. This
	 * method is not guaranteed to return a sheet and may return <code>null</code>.
	 * To avoid getting a null, use the switchToSheetWithName() method and then
	 * call currentSheet().
	 */
	public HSSFSheet sheetWithName(String name) {
		return _workbook.getSheet(name);
	}

	/**
	 * Returns the number of the current sheet.
	 * @return a non-zero integer
	 */
	public int currentSheetNumber() { return currentSheetIdx; }

	public void setCurrentSheetName(String name) {
		_workbook.setSheetName(currentSheetIdx, name);
	}

	
	public HSSFCell cellAtLocation(int rownum, int colnum) {
		HSSFSheet sheet = currentSheet();

		HSSFRow row = sheet.getRow(rownum);
		if (row == null) row = sheet.createRow(rownum);

		HSSFCell cell = row.getCell(colnum);
		if (cell == null) row.createCell(colnum);

		return currentSheet().getRow(rownum).getCell(colnum);
	}

	public HSSFCell cellAtLocation(int rownum, String columnName) {
		int colnum = numberForString(columnName);

		if (colnum == NotAValidColumnName)
			throw new java.lang.IllegalArgumentException("Column name \""+columnName+"\" is not a valid name. Use capital letters only.");

		return cellAtLocation(rownum, colnum);
	}

	public Number numberAtLocation(int rownum, int colnum) {
		return Double.valueOf(this.cellAtLocation(rownum, colnum).getNumericCellValue());
	}

	public void setNumberAtLocation(Number value, int rownum, int colnum) {
		this.cellAtLocation(rownum, colnum).setCellValue(value.doubleValue());
	}

	public Number numberAtLocation(int rownum, String columnName) {
		return numberAtLocation(rownum, numberForString(columnName));
	}

	public void setNumberAtLocation(Number value, int rownum, String columnName) {
		this.setNumberAtLocation(value, rownum, numberForString(columnName));
	}

	public Date dateAtLocation(int rownum, int colnum) {
		return this.cellAtLocation(rownum, colnum).getDateCellValue();
	}

	public void setDateAtLocation(Date value, int rownum, int colnum) {
		this.cellAtLocation(rownum, colnum).setCellValue(value);
	}

	public Date dateAtLocation(int rownum, String columnName) {
		return dateAtLocation(rownum, numberForString(columnName));
	}

	public void setDateAtLocation(Date value, int rownum, String columnName) {
		this.setDateAtLocation(value, rownum, numberForString(columnName));
	}

	public String stringAtLocation(int rownum, int colnum) {
		return this.cellAtLocation(rownum, colnum).getStringCellValue();
	}

	public void setStringAtLocation(String value, int rownum, int colnum) {
		this.cellAtLocation(rownum, colnum).setCellValue(value);
	}

	public String stringAtLocation(int rownum, String columnName) {
		return stringAtLocation(rownum, numberForString(columnName));
	}

	public void setStringAtLocation(String value, int rownum, String columnName) {
		this.setStringAtLocation(value, rownum, numberForString(columnName));
	}

	public String writeToTemp() {
		File file = null;
		try {
			file = File.createTempFile("eg_", ".xls");
		} catch (java.io.IOException e) {
			return null;
		}
		if (file == null) return null;
		return (write(file.getName())) ? file.getName() : null;
	}

	/**
	 * Writes the XLS data to a file given the filename.
	 * @param filename to be overwritten with the spreadsheet data
	 * @return true if the file was written to successfully, false is there was an
	 * exception from the java.io classes
	 */
	public boolean write(String filename) {
		java.io.FileOutputStream stream;
		try {
			stream = new java.io.FileOutputStream(filename);
		} catch (java.io.FileNotFoundException e) {
			return false;
		}
		try {
			_workbook.write(stream);
		} catch (java.io.IOException e) {
			return false;
		} finally {
			try {
				stream.close();
			} catch (IOException e) {
				// ignore
			}
		}
		return true;
	}
}
