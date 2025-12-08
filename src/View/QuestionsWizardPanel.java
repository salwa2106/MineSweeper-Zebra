package View;

import Model.Question;
import Model.SysData;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Very simple questions editor.
 * MineSweeperPrototype uses: new QuestionsWizardPanel(() -> { ... });
 */
public class QuestionsWizardPanel extends JPanel {

    private final DefaultTableModel model;
    private final JTable table;
    private final Runnable onSave;

    public QuestionsWizardPanel(Runnable onSave) {
        this.onSave = onSave;
        setLayout(new BorderLayout());

        String[] cols = {
                "Question",
                "Option A",
                "Option B",
                "Option C",
                "Option D",
                "Correct (A-D)",
                "Points Right",
                "Points Wrong",
                "Life Δ",
                "Difficulty"   // NEW column
        };
        model = new DefaultTableModel(cols, 0);
        table = new JTable(model);

        loadFromSysData();

        add(new JScrollPane(table), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
        JButton addBtn    = new JButton("Add");
        JButton deleteBtn = new JButton("Delete");
        JButton saveBtn   = new JButton("Save");

        buttons.add(addBtn);
        buttons.add(deleteBtn);
        buttons.add(saveBtn);
        add(buttons, BorderLayout.SOUTH);

        // default new row: easy question with standard points/life
        addBtn.addActionListener(e ->
                model.addRow(new Object[]{"", "", "", "", "", "A", 3, -1, 1, "easy"}));

        deleteBtn.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) model.removeRow(row);
        });

        saveBtn.addActionListener(e -> {
            saveToSysData();
            if (onSave != null) onSave.run();
        });
    }

    private void loadFromSysData() {
        model.setRowCount(0);
        List<Question> list = SysData.getQuestions();
        for (Question q : list) {
            String diff = q.getDifficulty();
            if (diff == null || diff.isBlank()) diff = "easy";

            model.addRow(new Object[]{
                    q.getText(),
                    q.getOptA(),
                    q.getOptB(),
                    q.getOptC(),
                    q.getOptD(),
                    String.valueOf(q.getCorrect()),
                    q.getPointsRight(),
                    q.getPointsWrong(),
                    q.getLifeDelta(),
                    diff
            });
        }
    }

    private void saveToSysData() {
        SysData.clear();

        int rows = model.getRowCount();
        for (int i = 0; i < rows; i++) {
            String text = (String) model.getValueAt(i, 0);
            String a    = (String) model.getValueAt(i, 1);
            String b    = (String) model.getValueAt(i, 2);
            String c    = (String) model.getValueAt(i, 3);
            String d    = (String) model.getValueAt(i, 4);

            String correctStr = String.valueOf(model.getValueAt(i, 5));
            char correct = correctStr.isEmpty() ? 'A'
                    : correctStr.trim().toUpperCase().charAt(0);

            int ptsRight = parseInt(model.getValueAt(i, 6), 3);
            int ptsWrong = parseInt(model.getValueAt(i, 7), -1);
            int lifeDelta = parseInt(model.getValueAt(i, 8), 1);

            Object diffObj = model.getValueAt(i, 9);
            String difficulty = (diffObj == null) ? "easy" : diffObj.toString().trim();
            if (difficulty.isEmpty()) difficulty = "easy";

            // UPDATED constructor with difficulty
            Question q = new Question(text, a, b, c, d, correct,
                                      ptsRight, ptsWrong, lifeDelta, difficulty);
            SysData.addQuestion(q);
        }

        SysData.saveToCsv();
    }

    private int parseInt(Object value, int def) {
        if (value == null) return def;
        try {
            return Integer.parseInt(value.toString());
        } catch (NumberFormatException ex) {
            return def;
        }
    }
}
