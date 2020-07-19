package cat.nyaa.heh.ui.component;

import java.util.ArrayList;
import java.util.List;

public class MatrixComponent implements MatrixAccess{
    private List<Integer> rawSlots;
    private int rows;
    private int columns;

    public MatrixComponent(int startRow, int startCol,  int rows, int columns) {
        this(getGuiSection(startRow, startCol, rows, columns), rows, columns);
    }

    public MatrixComponent(List<Integer> section, int rows, int columns){
        this.rawSlots = section;
        this.rows = rows;
        this.columns = columns;
    }

    public int access(int row, int column) {
        return rawSlots.get(columns * row + column);
    }

    public int indexOf(int rawSlot){
        return rawSlots.indexOf(rawSlot);
    }

    @Override
    public int rows() {
        return rows;
    }

    @Override
    public int columns() {
        return columns;
    }

    @Override
    public int size(){
        return rows*columns;
    }

    public boolean containsRawSlot(int rawSlot){
        return rawSlots.contains(rawSlot);
    }

    public static List<Integer> getGuiSection(int row, int index, int rows, int cols) {
        List<Integer> indexes = new ArrayList<>();
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                indexes.add((i + row) * 9 + index + j);
            }
        }
        return indexes;
    }
}
