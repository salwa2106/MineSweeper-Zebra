package View;

import Model.Question;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

public class QuestionsTableModel extends AbstractTableModel {

    private static final long serialVersionUID = 1L;

    private final String[] cols = {
            "ID", "Question", "Difficulty",
            "A", "B", "C", "D",
            "Correct", "Right+", "Wrong-", "LifeΔ"
    };

    private List<Question> rows = new ArrayList<>();

    public void setRows(List<Question> rows) {
        this.rows = rows;
        fireTableDataChanged();
    }

    public Question getRow(int r) {
        return rows.get(r);
    }

    @Override public int getRowCount() { return rows.size(); }
    @Override public int getColumnCount() { return cols.length; }
    @Override public String getColumnName(int c) { return cols[c]; }

    @Override
    public Object getValueAt(int r, int c) {
        Question q = rows.get(r);
        return switch (c) {
            case 0 -> q.getId();                 // display only
            case 1 -> q.getText();
            case 2 -> q.getDifficulty();
            case 3 -> q.getOptA();
            case 4 -> q.getOptB();
            case 5 -> q.getOptC();
            case 6 -> q.getOptD();
            case 7 -> String.valueOf(q.getCorrect());
            case 8 -> q.getPointsRight();
            case 9 -> q.getPointsWrong();
            case 10 -> q.getLifeDelta();
            default -> "";
        };
    }

    // ❌ IMPORTANT: table is READ-ONLY
    @Override
    public boolean isCellEditable(int row, int column) {
        return false;
    }
}
