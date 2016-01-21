package at.treedb.util.excel;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * 
 * @author Peter Sauer
 * 
 */
public class ExcelReader {

    public static String MAGIC_STRING = "++++++++++";

    /*
     * public void parse(String path, String newPath, int sheetNum, int
     * startRow, ExcelRowInterface intf, boolean reverse, boolean remove) throws
     * Exception { InputStream inp = new FileInputStream(path); XSSFWorkbook
     * workBook = new XSSFWorkbook(inp); Sheet sheet =
     * workBook.getSheetAt(sheetNum); int rows = sheet.getLastRowNum();
     * intf.prolog(workBook, sheet); if (!reverse) { for (int index = startRow;
     * index <= rows; ++index) { intf.traverse(workBook, sheet, rows, index,
     * sheet.getRow(index), remove); } } else { for (int index = rows; index >=
     * startRow; --index) { intf.traverse(workBook, sheet, rows, index,
     * sheet.getRow(index), remove); }
     * 
     * } intf.epilog(this,workBook, sheet); FileOutputStream fileOut = new
     * FileOutputStream(newPath); workBook.write(fileOut); fileOut.close();
     * 
     * }
     */

    static public HashMap<String, String[][]> getSheetAsStringArray(String path) throws Exception {
        InputStream inp = new FileInputStream(path);
        XSSFWorkbook workBook = new XSSFWorkbook(inp);
        HashMap<String, String[][]> map = new HashMap<String, String[][]>();
        int sheetNum = workBook.getNumberOfSheets();
        ArrayList<String> slist = new ArrayList<String>();
        for (int j = 0; j < sheetNum; ++j) {
            Sheet sheet = workBook.getSheetAt(j);
            int rows = sheet.getLastRowNum() + 1;
            String[][] array = new String[rows][];
            for (int k = 0; k < rows; ++k) {
                Row row = sheet.getRow(k);
                if (row == null) {
                    array[k] = new String[0];
                    continue;
                }
                int colNum = row.getLastCellNum() + 1;
                String[] column = new String[colNum];
                for (int z = 0; z < colNum; ++z) {
                    Cell cell = row.getCell(z);
                    if (cell == null) {
                        column[z] = "";
                    } else {
                        // cell.getCellType()
                        column[z] = cell.toString();
                    }
                }
                array[k] = column;
            }
            map.put(sheet.getSheetName(), array);
            slist.add(sheet.getSheetName());
            map.put("" + j, array);
        }

        map.put(MAGIC_STRING, new String[][] { slist.toArray(new String[slist.size()]) });
        workBook.close();
        return map;
    }

    static public HashMap<String, Cell[][]> getSheetAsCellArray(InputStream inp) throws Exception {
        // InputStream inp = new FileInputStream(path);
        XSSFWorkbook workBook = new XSSFWorkbook(inp);
        HashMap<String, Cell[][]> map = new HashMap<String, Cell[][]>();
        int sheetNum = workBook.getNumberOfSheets();
        ArrayList<String> slist = new ArrayList<String>();
        for (int j = 0; j < sheetNum; ++j) {
            Sheet sheet = workBook.getSheetAt(j);
            int rows = sheet.getLastRowNum() + 1;
            Cell[][] array = new Cell[rows][];
            for (int k = 0; k < rows; ++k) {
                Row row = sheet.getRow(k);
                if (row == null) {
                    array[k] = new Cell[0];
                    continue;
                }
                int colNum = row.getLastCellNum() + 1;
                Cell[] column = new Cell[colNum];
                for (int z = 0; z < colNum; ++z) {
                    Cell cell = row.getCell(z);
                    if (cell == null) {
                        column[z] = null;
                    } else {
                        // cell.getCellType()
                        column[z] = cell;
                    }
                }
                array[k] = column;
            }
            map.put(sheet.getSheetName(), array);
            slist.add(sheet.getSheetName());
            map.put("" + j, array);
        }
        /*
         * map.put(MAGIC_STRING, new Cell[][] { slist.toArray(new
         * Cell[slist.size()]) });
         */
        workBook.close();
        return map;
    }

    /*
     * public void setWorkbook(XSSFWorkbook workBook) { workBook = workBook; }
     */

    public static int column2int(String c) {
        if (c.length() == 1) {
            return Character.toLowerCase(c.charAt(0)) - 'a';
        }
        return (((int) Character.toLowerCase(c.charAt(0)) - 'a' + 1) * 26) + Character.toLowerCase(c.charAt(1)) - 'a';
    }

    public static void remove(Sheet sheet, int start, int rows, Row row) {
        sheet.removeRow(row);
        sheet.shiftRows(start, start + rows, -1);
    }

    public static void main(String[] args) throws Exception {
        HashMap<String, String[][]> map = getSheetAsStringArray("c:/tmp/taxo/animals.xlsx");
        ArrayList<String> list = new ArrayList<String>();
        for (int j = 1; j <= 20; ++j) {
            String[][] array = map.get("Table " + j);

            for (int i = 1; i < array.length; ++i) {
                String tmp = array[i][1].trim();
                if (tmp.equals("")) {
                    continue;
                }
                boolean error = false;
                try {
                    Double.parseDouble(tmp);
                } catch (Exception e) {
                    error = true;
                }
                if (!error) {
                    continue;
                }
                list.add(tmp);

            }
        }
        for (String s : list) {
            System.out.println(s + " ");
        }
        System.out.println();
        System.out.println(list.size());
    }
}
