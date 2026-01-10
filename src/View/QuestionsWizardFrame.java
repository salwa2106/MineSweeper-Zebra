package View;

import Model.Question;
import Model.SoundManager;
import Model.SysData;
import Model.ThemeType;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Full-screen Questions Wizard screen (Import/Export/Add/Edit/Delete + Table + Back)
 * The UI colors auto-follow the selected ThemeType via ThemePalette (Option A).
 */
public class QuestionsWizardFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    // ====== MVC HOOK ======
    private final QuestionsController controller;
    private final Runnable onBack;

    // ====== UI ======
    private JTable table;
    private QuestionsTableModel tableModel;

    public QuestionsWizardFrame(QuestionsController controller, Runnable onBack) {
        super("Questions Wizard");
        this.controller = controller;
        this.onBack = onBack;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // ✅ Full screen
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);

        setUndecorated(true);
        setBackground(new Color(0, 0, 0, 0)); // ✅ transparent window

        setContentPane(buildUI());
        SysData.applyGlobalFont(this);
    }

    private JComponent buildUI() {

        // ===== PALETTE FROM CURRENT THEME =====
        ThemeType currentTheme = SysData.getTheme();
        ThemePalette pal = ThemePalette.of(currentTheme);

        // ===== BACKGROUND =====
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setOpaque(false);

        // ===== FROSTED CARD =====
        JPanel glass = new FrostedCardPanel(pal);
        glass.setLayout(new BorderLayout(18, 18));
        glass.setBorder(new EmptyBorder(18, 18, 18, 18));

        Dimension cardSize = new Dimension(1050, 720);
        glass.setPreferredSize(cardSize);
        glass.setMinimumSize(cardSize);
        glass.setMaximumSize(cardSize);

        // ===== TITLE =====
        JLabel title = new JLabel("QUESTIONS WIZARD", SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 28));
        title.setForeground(new Color(210, 255, 235));
        glass.add(title, BorderLayout.NORTH);

        // ===== TOP BAR (2 ROWS) =====
        JPanel topBar = new JPanel();
        topBar.setOpaque(false);
        topBar.setLayout(new BoxLayout(topBar, BoxLayout.Y_AXIS));

        JPanel row1 = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        row1.setOpaque(false);

        JPanel row2 = new JPanel(new FlowLayout(FlowLayout.CENTER, 14, 8));
        row2.setOpaque(false);

        // ✅ Theme-aware buttons
        JButton importBtn = new PillButton("Import CSV", pal.primary);
        JButton exportBtn = new PillButton("Export CSV", pal.primary);
        JButton addBtn    = new PillButton("Add", pal.secondary);
        JButton editBtn   = new PillButton("Edit", pal.secondary);
        JButton deleteBtn = new PillButton("Delete", pal.danger);

        row1.add(importBtn);
        row1.add(exportBtn);

        row2.add(addBtn);
        row2.add(editBtn);
        row2.add(deleteBtn);

        topBar.add(row1);
        topBar.add(row2);

        // ===== TABLE =====
        tableModel = new QuestionsTableModel();
        table = new JTable(tableModel);
        styleTable(table, pal);
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);

        JPanel center = new JPanel(new BorderLayout(0, 12));
        center.setOpaque(false);
        center.add(topBar, BorderLayout.NORTH);
        center.add(sp, BorderLayout.CENTER);

        glass.add(center, BorderLayout.CENTER);

        // ===== BOTTOM =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        bottom.setOpaque(false);

        JButton backBtn = new PillButton("Back", pal.secondary);
        backBtn.setPreferredSize(new Dimension(260, 52));
        bottom.add(backBtn);

        glass.add(bottom, BorderLayout.SOUTH);

        // ===== CENTER CARD ON SCREEN =====
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1;
        gbc.weighty = 1;

        bg.add(glass, gbc);

        // ===== LOAD DATA =====
        reloadFromModel();

        // ===== ACTIONS (NO DUPLICATES) =====
        importBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            onImportCsv();
        });

        exportBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            onExportCsv();
        });

        addBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            onAdd();
        });

        editBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            onEdit();
        });

        deleteBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            onDelete();
        });

        backBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            dispose();
            if (onBack != null) onBack.run();
        });

        return bg;
    }

    private void reloadFromModel() {
        List<Question> rows = new ArrayList<>(controller.getAllQuestions());
        tableModel.setRows(rows);
    }

    private void onImportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        if (fc.showOpenDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        try {
            controller.importFromCsv(f);
            reloadFromModel();
            JOptionPane.showMessageDialog(this, "Imported successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Import failed:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onExportCsv() {
        JFileChooser fc = new JFileChooser();
        fc.setFileFilter(new FileNameExtensionFilter("CSV files", "csv"));
        if (fc.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;

        File f = fc.getSelectedFile();
        if (!f.getName().toLowerCase().endsWith(".csv")) {
            f = new File(f.getParentFile(), f.getName() + ".csv");
        }

        try {
            controller.exportToCsv(f);
            JOptionPane.showMessageDialog(this, "Exported successfully.");
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Export failed:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onAdd() {
        Question created = QuestionDialog.showDialog(this, null);
        if (created == null) return;

        try {
            controller.addQuestion(created);
            reloadFromModel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Add failed:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onEdit() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to edit.");
            return;
        }

        Question current = tableModel.getRow(r);
        Question edited = QuestionDialog.showDialog(this, current);
        if (edited == null) return;

        try {
            controller.updateQuestionAtIndex(r, edited);
            reloadFromModel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Edit failed:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void onDelete() {
        int r = table.getSelectedRow();
        if (r < 0) {
            JOptionPane.showMessageDialog(this, "Select a row to delete.");
            return;
        }

        int ok = JOptionPane.showConfirmDialog(this,
                "Delete selected question?",
                "Confirm", JOptionPane.YES_NO_OPTION);

        if (ok != JOptionPane.YES_OPTION) return;

        try {
            controller.deleteQuestionAtIndex(r);
            reloadFromModel();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Delete failed:\n" + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void styleTable(JTable t, ThemePalette pal) {
        t.setRowHeight(34);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setForeground(pal.text);
        t.setBackground(new Color(10, 15, 15, 180));
        t.setSelectionBackground(pal.secondary.darker());
        t.setSelectionForeground(Color.WHITE);
        t.setShowGrid(false);
        t.setFillsViewportHeight(true);

        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            JLabel l = new JLabel(value == null ? "" : value.toString());
            l.setOpaque(true);
            l.setFont(new Font("Georgia", Font.BOLD, 15));
            l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            l.setBackground(pal.secondary.darker());
            l.setForeground(new Color(245, 255, 250));
            return l;
        });
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
    }

    // ====== TABLE MODEL (Model.Question) ======
    private static class QuestionsTableModel extends AbstractTableModel {
        private final String[] cols = {
                "Question", "Difficulty", "A", "B", "C", "D",
                "Correct", "Right+", "Wrong-", "LifeΔ"
        };
        private List<Question> rows = new ArrayList<>();

        public void setRows(List<Question> rows) {
            this.rows = rows;
            fireTableDataChanged();
        }

        public Question getRow(int r) { return rows.get(r); }

        @Override public int getRowCount() { return rows.size(); }
        @Override public int getColumnCount() { return cols.length; }
        @Override public String getColumnName(int c) { return cols[c]; }

        @Override
        public Object getValueAt(int r, int c) {
            Question q = rows.get(r);
            return switch (c) {
                case 0 -> q.getText();
                case 1 -> q.getDifficulty();
                case 2 -> q.getOptA();
                case 3 -> q.getOptB();
                case 4 -> q.getOptC();
                case 5 -> q.getOptD();
                case 6 -> String.valueOf(q.getCorrect());
                case 7 -> q.getPointsRight();
                case 8 -> q.getPointsWrong();
                case 9 -> q.getLifeDelta();
                default -> "";
            };
        }
    }

    // ====== CONTROLLER CONTRACT (uses Model.Question) ======
    public interface QuestionsController {
        List<Question> getAllQuestions();
        void importFromCsv(File file) throws Exception;
        void exportToCsv(File file) throws Exception;

        void addQuestion(Question q) throws Exception;

        void updateQuestionAtIndex(int index, Question q) throws Exception;
        void deleteQuestionAtIndex(int index) throws Exception;
    }

    // ===== Frosted Card uses palette =====
    private static class FrostedCardPanel extends JPanel {
        private final ThemePalette pal;

        public FrostedCardPanel(ThemePalette pal) {
            this.pal = pal;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(pal.cardBg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);

            g2.setColor(pal.stroke);
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 24, 24);

            g2.dispose();
        }
    }

    // ====== ADD/EDIT DIALOG (Model.Question) ======
    private static class QuestionDialog extends JDialog {
        private Question result;

        private final JTextField tfQuestion = new JTextField();
        private final JComboBox<String> cbDiff = new JComboBox<>(new String[]{"easy", "medium", "hard", "pro"});
        private final JTextField tfA = new JTextField();
        private final JTextField tfB = new JTextField();
        private final JTextField tfC = new JTextField();
        private final JTextField tfD = new JTextField();
        private final JComboBox<String> cbCorrect = new JComboBox<>(new String[]{"A", "B", "C", "D"});

        private final JSpinner spRight = new JSpinner(new SpinnerNumberModel(3, -999, 999, 1));
        private final JSpinner spWrong = new JSpinner(new SpinnerNumberModel(-1, -999, 999, 1));
        private final JSpinner spLife  = new JSpinner(new SpinnerNumberModel(0, -99, 99, 1));

        static Question showDialog(Component parent, Question existing) {
            Window w = SwingUtilities.getWindowAncestor(parent);
            QuestionDialog d = new QuestionDialog(w, existing);
            d.setVisible(true);
            return d.result;
        }

        QuestionDialog(Window owner, Question existing) {
            super(owner, "Question", ModalityType.APPLICATION_MODAL);
            setSize(720, 520);
            setLocationRelativeTo(owner);

            JPanel p = new JPanel(new GridBagLayout());
            p.setBorder(new EmptyBorder(16, 16, 16, 16));
            GridBagConstraints gc = new GridBagConstraints();
            gc.insets = new Insets(8, 8, 8, 8);
            gc.fill = GridBagConstraints.HORIZONTAL;

            int y = 0;
            addRow(p, gc, y++, "Question:", tfQuestion);
            addRow(p, gc, y++, "Difficulty:", cbDiff);
            addRow(p, gc, y++, "A:", tfA);
            addRow(p, gc, y++, "B:", tfB);
            addRow(p, gc, y++, "C:", tfC);
            addRow(p, gc, y++, "D:", tfD);
            addRow(p, gc, y++, "Correct:", cbCorrect);
            addRow(p, gc, y++, "Points Right:", spRight);
            addRow(p, gc, y++, "Points Wrong:", spWrong);
            addRow(p, gc, y++, "Life Delta:", spLife);

            JButton ok = new JButton("OK");
            JButton cancel = new JButton("Cancel");
            JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            actions.add(ok);
            actions.add(cancel);

            ok.addActionListener(e -> {
                SoundManager.play(SoundManager.Sfx.CLICK);
                if (tfQuestion.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "Question text is required.");
                    return;
                }
                Question q = new Question();
                q.setText(tfQuestion.getText().trim());
                q.setDifficulty((String) cbDiff.getSelectedItem());
                q.setOptA(tfA.getText().trim());
                q.setOptB(tfB.getText().trim());
                q.setOptC(tfC.getText().trim());
                q.setOptD(tfD.getText().trim());
                q.setCorrect(((String) cbCorrect.getSelectedItem()).charAt(0));
                q.setPointsRight((Integer) spRight.getValue());
                q.setPointsWrong((Integer) spWrong.getValue());
                q.setLifeDelta((Integer) spLife.getValue());

                result = q;
                dispose();
            });

            cancel.addActionListener(e -> {
                SoundManager.play(SoundManager.Sfx.CLICK);
                dispose();
            });

            getContentPane().setLayout(new BorderLayout());
            getContentPane().add(p, BorderLayout.CENTER);
            getContentPane().add(actions, BorderLayout.SOUTH);

            if (existing != null) {
                tfQuestion.setText(existing.getText());
                cbDiff.setSelectedItem(existing.getDifficulty());
                tfA.setText(existing.getOptA());
                tfB.setText(existing.getOptB());
                tfC.setText(existing.getOptC());
                tfD.setText(existing.getOptD());
                cbCorrect.setSelectedItem(String.valueOf(existing.getCorrect()));
                spRight.setValue(existing.getPointsRight() == null ? 0 : existing.getPointsRight());
                spWrong.setValue(existing.getPointsWrong() == null ? 0 : existing.getPointsWrong());
                spLife.setValue(existing.getLifeDelta() == null ? 0 : existing.getLifeDelta());
            }
        }

        private void addRow(JPanel p, GridBagConstraints gc, int y, String label, JComponent comp) {
            gc.gridx = 0; gc.gridy = y; gc.weightx = 0;
            p.add(new JLabel(label), gc);
            gc.gridx = 1; gc.weightx = 1;
            p.add(comp, gc);
        }
    }
}
