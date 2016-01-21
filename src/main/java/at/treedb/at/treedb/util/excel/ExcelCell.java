package at.treedb.util.excel;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;

public class ExcelCell {
    private CELL_TYPE type;
    private Object data;
    private int row;
    private int column;
    private XSSFCellStyle cellStyle;

    public enum CELL_TYPE {
        DOUBLE, LONG, BOOLEAN, STRING, DATE, URL
    };

    public ExcelCell(int row, int column, Object data, CELL_TYPE type) {
        this.setRow(row);
        this.setColumn(column);
        this.setData(data);
        this.setType(type);

    }

    public void setType(CELL_TYPE type) {
        this.type = type;
    }

    public CELL_TYPE getType() {
        return type;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public Object getData() {
        return data;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getRow() {
        return row;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getColumn() {
        return column;
    }

    public void setCellStyle(XSSFCellStyle cellStyle) {
        this.cellStyle = cellStyle;
    }

    public CellStyle getCellStyle() {
        return cellStyle;
    }

}
