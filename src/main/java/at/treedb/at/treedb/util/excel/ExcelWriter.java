package at.treedb.util.excel;

import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelWriter {
    private String path;
    private ArrayList<String> sheet = new ArrayList<String>();
    private HashMap<String, ExcelCell> table = null;
    private String sheetName;
    private XSSFCellStyle headerStyle;
    private XSSFCellStyle defaultStyle;
    private XSSFWorkbook wb;
    private HashMap<String, HashMap<String, ExcelCell>> sheetMap = new HashMap<String, HashMap<String, ExcelCell>>();
    private HashMap<String, XSSFCellStyle> headerStyleMap = new HashMap<String, XSSFCellStyle>();
    private HashMap<String, XSSFCellStyle> defaultStyleMap = new HashMap<String, XSSFCellStyle>();

    public ExcelWriter(String sheetName, String path) {
        this.path = path;
        if (sheetName != null) {
            sheet.add(sheetName);
            this.sheetName = sheetName;
            table = new HashMap<String, ExcelCell>();
            sheetMap.put(sheetName, table);
        }
        wb = new XSSFWorkbook();

    }

    public XSSFWorkbook getWorkbook() {
        return wb;
    }

    public void switchSheet(String name) {
        HashMap<String, ExcelCell> tab = sheetMap.get(name);
        if (tab != null) {
            table = tab;
        } else {
            sheet.add(name);
            table = new HashMap<String, ExcelCell>();
            sheetMap.put(name, table);

        }
        headerStyle = headerStyleMap.get(name);
        defaultStyle = defaultStyleMap.get(name);
        sheetName = name;

    }

    public void setDefaultCellStyle(XSSFCellStyle style) {
        defaultStyle = style;
        defaultStyleMap.put(sheetName, style);
    }

    public void setHeaderCellStyle(XSSFCellStyle style) {
        headerStyle = style;
        headerStyleMap.put(sheetName, style);
    }

    private void putCell(String key, ExcelCell cell) throws Exception {
        if (table.containsKey(key)) {
            throw new Exception("ExcelCell.addString() - double entry! " + key + ":" + sheetName);
        }
        table.put(key, cell);
    }

    public ExcelCell addString(int row, int column, String text) throws Exception {
        ExcelCell cell = new ExcelCell(row, column, text, ExcelCell.CELL_TYPE.STRING);
        String key = row + "_" + column;
        putCell(key, cell);
        return cell;
    }

    public ExcelCell addString(int row, int column, String text, XSSFCellStyle style) throws Exception {
        ExcelCell cell = new ExcelCell(row, column, text, ExcelCell.CELL_TYPE.STRING);
        String key = row + "_" + column;
        putCell(key, cell);
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    public ExcelCell addLong(int row, int column, long l) throws Exception {
        ExcelCell cell = new ExcelCell(row, column, new Long(l), ExcelCell.CELL_TYPE.LONG);
        String key = row + "_" + column;
        putCell(key, cell);
        return cell;
    }

    public ExcelCell addLong(int row, int column, long l, XSSFCellStyle style) throws Exception {
        ExcelCell cell = new ExcelCell(row, column, new Long(l), ExcelCell.CELL_TYPE.LONG);
        String key = row + "_" + column;
        putCell(key, cell);
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    public ExcelCell addDouble(int row, int column, double l) throws Exception {
        ExcelCell cell = new ExcelCell(row, column, new Double(l), ExcelCell.CELL_TYPE.DOUBLE);
        String key = row + "_" + column;
        putCell(key, cell);
        return cell;
    }

    public ExcelCell addDouble(int row, int column, double l, XSSFCellStyle style) throws Exception {
        ExcelCell cell = new ExcelCell(row, column, new Double(l), ExcelCell.CELL_TYPE.DOUBLE);
        String key = row + "_" + column;
        putCell(key, cell);
        if (style != null) {
            cell.setCellStyle(style);
        }
        return cell;
    }

    public void write() throws IOException {
        for (String name : sheet) {
            XSSFSheet sheet = wb.createSheet(name);
            switchSheet(name);
            // create matrix
            int rowMax = -1;
            int columnMax = -1;
            for (ExcelCell cell : table.values()) {
                rowMax = Math.max(rowMax, cell.getRow());
                columnMax = Math.max(columnMax, cell.getColumn());
            }
            rowMax++;
            columnMax++;
            for (int row = 0; row < rowMax; ++row) {
                XSSFRow r = sheet.createRow(row);
                for (int col = 0; col < columnMax; ++col) {
                    String key = row + "_" + col;
                    ExcelCell c = table.get(key);
                    if (c == null) {
                        continue;
                    }
                    XSSFCell cell = r.createCell(col);
                    if (c.getCellStyle() != null) {
                        cell.setCellStyle(c.getCellStyle());
                    } else {
                        XSSFCellStyle style = null;
                        if (row == 0 && headerStyle != null) {
                            style = headerStyle;
                        } else if (defaultStyle != null) {
                            style = defaultStyle;
                        }
                        if (style != null) {
                            cell.setCellStyle(style);
                        }

                    }
                    if (c != null) {
                        switch (c.getType()) {
                        case BOOLEAN:
                            cell.setCellValue((Boolean) c.getData());
                            break;

                        case STRING:
                            cell.setCellValue((String) c.getData());
                            break;
                        case LONG:
                            cell.setCellValue((Long) c.getData());
                            break;
                        case DOUBLE:
                            cell.setCellValue((Double) c.getData());
                            break;
                        }
                    }
                }
            }
        }
        FileOutputStream fileOut = new FileOutputStream(path);
        wb.write(fileOut);
        fileOut.close();

    }

    public XSSFCellStyle getHeaderStyle() {
        XSSFCellStyle styleHeader = wb.createCellStyle();
        styleHeader.setFillForegroundColor(new XSSFColor(new java.awt.Color(148, 139, 84)));
        styleHeader.setFillPattern(XSSFCellStyle.SOLID_FOREGROUND);
        styleHeader.setBorderBottom(XSSFCellStyle.BORDER_MEDIUM);
        styleHeader.setBottomBorderColor(IndexedColors.BLACK.getIndex());
        return styleHeader;
    }

    public static void main(String[] args) throws Exception {

        ExcelWriter test = new ExcelWriter(null, "d:/cmdb2excel.xlsx");

        test.write();

    }

}
