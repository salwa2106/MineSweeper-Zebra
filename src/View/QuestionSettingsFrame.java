package View;

import Controller.SettingsController;
import Model.QuestionSettings;

import javax.swing.*;
import java.awt.*;
import java.util.EnumSet;

public class QuestionSettingsFrame extends JFrame {

    private static final Color TEXT = new Color(225, 245, 240);
    private static final Color BORDER = new Color(170, 255, 255, 150);

    private final SettingsController controller;
    private final Runnable onSaved;

    public QuestionSettingsFrame(SettingsController controller, Runnable onSaved) {
        super("Question Settings");
        this.controller = controller;
        this.onSaved = onSaved;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(760, 560);
        setLocationRelativeTo(null);

        setContentPane(buildContent());
    }

    private JComponent buildContent() {
        QuestionSettings qs = controller.getQuestionSettings();

        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);

        JPanel glass = frostedCard();
        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("QUESTION SETTINGS", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Georgia", Font.BOLD, 30));
        title.setForeground(new Color(190, 255, 220));
        glass.add(title);
        glass.add(Box.createVerticalStrut(20));

        JCheckBox enable = themedCheck("Enable Trivia Questions", qs.isQuestionsEnabled());

        JCheckBox easy = themedCheck("Easy", qs.getAllowed().contains(QuestionSettings.QDiff.EASY));
        JCheckBox med  = themedCheck("Medium", qs.getAllowed().contains(QuestionSettings.QDiff.MEDIUM));
        JCheckBox hard = themedCheck("Hard", qs.getAllowed().contains(QuestionSettings.QDiff.HARD));
        JCheckBox pro  = themedCheck("Pro", qs.getAllowed().contains(QuestionSettings.QDiff.PRO));

        JCheckBox overrideCost = themedCheck("Override Activation Cost", qs.isOverrideActivationCost());
        JSlider cost = new JSlider(0, 30, qs.getActivationCost());
        styleSlider(cost);

        JCheckBox limit = themedCheck("Limit Activations Per Game", qs.isLimitPerGame());
        JSpinner maxActs = new JSpinner(new SpinnerNumberModel(qs.getMaxActivations(), 1, 99, 1));
        styleSpinner(maxActs);

        Runnable refresh = () -> {
            boolean on = enable.isSelected();
            easy.setEnabled(on); med.setEnabled(on); hard.setEnabled(on); pro.setEnabled(on);
            overrideCost.setEnabled(on);
            cost.setEnabled(on && overrideCost.isSelected());
            limit.setEnabled(on);
            maxActs.setEnabled(on && limit.isSelected());
        };

        enable.addActionListener(e -> refresh.run());
        overrideCost.addActionListener(e -> refresh.run());
        limit.addActionListener(e -> refresh.run());

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0; gc.gridwidth = 2;
        form.add(enable, gc);

        gc.gridwidth = 1;
        addRow(form, gc, 1, "Allowed Difficulties:", wrapChecks(easy, med, hard, pro));
        addRow(form, gc, 2, "", overrideCost);
        addRow(form, gc, 3, "Activation Cost:", cost);
        addRow(form, gc, 4, "", limit);
        addRow(form, gc, 5, "Max Activations:", maxActs);

        glass.add(form);
        glass.add(Box.createVerticalStrut(18));

        JButton save = frostedButton("Save");
        JButton reset = frostedButton("Reset");
        JButton cancel = frostedButton("Cancel");

        save.addActionListener(e -> {
            qs.setQuestionsEnabled(enable.isSelected());

            EnumSet<QuestionSettings.QDiff> allowed = EnumSet.noneOf(QuestionSettings.QDiff.class);
            if (easy.isSelected()) allowed.add(QuestionSettings.QDiff.EASY);
            if (med.isSelected())  allowed.add(QuestionSettings.QDiff.MEDIUM);
            if (hard.isSelected()) allowed.add(QuestionSettings.QDiff.HARD);
            if (pro.isSelected())  allowed.add(QuestionSettings.QDiff.PRO);

            if (qs.isQuestionsEnabled() && allowed.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Select at least one difficulty.", "Validation",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            qs.setAllowed(allowed);
            qs.setOverrideActivationCost(overrideCost.isSelected());
            qs.setActivationCost(cost.getValue());
            qs.setLimitPerGame(limit.isSelected());
            qs.setMaxActivations((Integer) maxActs.getValue());

            if (onSaved != null) onSaved.run();
            JOptionPane.showMessageDialog(this, "Question settings saved.", "Settings",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });

        reset.addActionListener(e -> {
            enable.setSelected(true);
            easy.setSelected(true); med.setSelected(true); hard.setSelected(true); pro.setSelected(true);
            overrideCost.setSelected(false);
            cost.setValue(5);
            limit.setSelected(false);
            maxActs.setValue(10);
            refresh.run();
        });

        cancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        actions.setOpaque(false);
        actions.add(save);
        actions.add(reset);
        actions.add(cancel);

        glass.add(actions);

        refresh.run();
        root.add(glass);

        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(new Color(10, 15, 15));
        bg.add(root);
        return bg;
    }

    private JPanel frostedCard() {
        JPanel glass = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 35, 35, 180));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);

                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(2, 2, getWidth()-4, getHeight()-4, 30, 30);

                g2.dispose();
            }
        };
        glass.setOpaque(false);
        glass.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
        glass.setPreferredSize(new Dimension(660, 480));
        return glass;
    }

    private JCheckBox themedCheck(String text, boolean selected) {
        JCheckBox cb = new JCheckBox(text, selected);
        cb.setOpaque(false);
        cb.setFont(new Font("Georgia", Font.BOLD, 16));
        cb.setForeground(TEXT);
        cb.setFocusPainted(false);
        return cb;
    }

    private void styleSlider(JSlider s) {
        s.setOpaque(false);
        s.setForeground(TEXT);
        s.setPaintTicks(true);
        s.setPaintLabels(true);
        s.setMajorTickSpacing(5);
    }

    private void styleSpinner(JSpinner sp) {
        sp.setFont(new Font("Georgia", Font.PLAIN, 15));
    }

    private JPanel wrapChecks(JCheckBox... cbs) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 0));
        p.setOpaque(false);
        for (JCheckBox cb : cbs) p.add(cb);
        return p;
    }

    private void addRow(JPanel p, GridBagConstraints gc, int y, String label, JComponent comp) {
        gc.gridx = 0; gc.gridy = y; gc.gridwidth = 1; gc.weightx = 0; gc.anchor = GridBagConstraints.LINE_END;
        JLabel l = new JLabel(label);
        l.setFont(new Font("Georgia", Font.BOLD, 16));
        l.setForeground(TEXT);
        p.add(l, gc);

        gc.gridx = 1; gc.weightx = 1; gc.anchor = GridBagConstraints.LINE_START;
        p.add(comp, gc);
    }

    private JButton frostedButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Georgia", Font.BOLD, 18));
        b.setForeground(new Color(200, 255, 230));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(160, 46));
        return b;
    }
}
