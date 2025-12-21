package View;

import Controller.SettingsController;
import Model.Difficulty;
import Model.GameSettings;

import javax.swing.*;
import java.awt.*;

public class SettingsFrame extends JFrame {

    // Theme colors (match your project)
    private static final Color TEXT = new Color(225, 245, 240);
    private static final Color BORDER = new Color(160, 255, 255, 130);

    private final SettingsController controller;
    private final Runnable onSaved; // callback to notify main view

    public SettingsFrame(SettingsController controller, Runnable onSaved) {
        super("Settings");
        this.controller = controller;
        this.onSaved = onSaved;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setSize(720, 520);
        setLocationRelativeTo(null);

        setContentPane(buildContent());
    }

    private JComponent buildContent() {
        GameSettings gs = controller.getGameSettings();

        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);

        JPanel glass = frostedCard();
        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("GENERAL SETTINGS", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Georgia", Font.BOLD, 30));
        title.setForeground(new Color(190, 255, 220));
        glass.add(title);
        glass.add(Box.createVerticalStrut(20));

        JCheckBox cbSound = themedCheck("Sound Effects", gs.isSoundEnabled());
        JCheckBox cbAnim  = themedCheck("Animations", gs.isAnimationsEnabled());
        JCheckBox cbAuto  = themedCheck("Auto-save History", gs.isAutoSaveHistory());

        JComboBox<String> cbDiff = new JComboBox<>(new String[]{"Easy", "Medium", "Hard"});
        styleCombo(cbDiff);
        cbDiff.setSelectedIndex(switch (gs.getDefaultDifficulty()) {
            case EASY -> 0; case MEDIUM -> 1; case HARD -> 2;
        });
        
        cbDiff.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(
                    JList<?> list, Object value, int index,
                    boolean isSelected, boolean cellHasFocus) {

                JLabel label = (JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);

                label.setOpaque(true); // ✅ critical

                if (index == -1) {
                    // ✅ this is the "selected item" shown in the combo field
                    label.setBackground(Color.WHITE);              // matches what Windows paints
                    label.setForeground(new Color(20, 35, 35));    // dark readable text
                } else {
                    // ✅ dropdown list items (your theme)
                    label.setBackground(isSelected ? new Color(60, 90, 80)
                                                   : new Color(30, 40, 35));
                    label.setForeground(new Color(220, 255, 235));
                }

                return label;
            }
        });


        JSlider lives = new JSlider(1, 10, gs.getMaxSharedLives());
        styleSlider(lives);

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(10, 10, 10, 10);
        gc.fill = GridBagConstraints.HORIZONTAL;

        addRow(form, gc, 0, "Default Difficulty:", cbDiff);
        addRow(form, gc, 1, "Max Shared Lives:", lives);

        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        form.add(cbSound, gc);

        gc.gridy = 3;
        form.add(cbAnim, gc);

        gc.gridy = 4;
        form.add(cbAuto, gc);

        glass.add(form);
        glass.add(Box.createVerticalStrut(18));

        JButton save = frostedButton("Save");
        JButton cancel = frostedButton("Cancel");

        save.addActionListener(e -> {
            controller.setSoundEnabled(cbSound.isSelected());
            controller.setAnimationsEnabled(cbAnim.isSelected());
            controller.setAutoSaveHistory(cbAuto.isSelected());
            controller.setMaxSharedLives(lives.getValue());

            Difficulty d = switch (cbDiff.getSelectedIndex()) {
                case 1 -> Difficulty.MEDIUM;
                case 2 -> Difficulty.HARD;
                default -> Difficulty.EASY;
            };
            controller.setDefaultDifficulty(d);

            if (onSaved != null) onSaved.run();
            JOptionPane.showMessageDialog(this, "Settings saved.", "Settings",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        });



        cancel.addActionListener(e -> dispose());

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        actions.setOpaque(false);
        actions.add(save);
        actions.add(cancel);

        glass.add(actions);

        root.add(glass);
        return wrapBackground(root);
    }

    // ---------- UI helpers (same design language) ----------

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
        glass.setPreferredSize(new Dimension(620, 440));
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

    private void styleCombo(JComboBox<String> cb) {
        cb.setFont(new Font("Georgia", Font.PLAIN, 15));
        cb.setBackground(Color.WHITE);
        cb.setForeground(new Color(20, 35, 35)); // ✅ readable on white
        cb.setBorder(BorderFactory.createLineBorder(new Color(90, 65, 35), 2, true));
    }

    private void styleSlider(JSlider s) {
        s.setOpaque(false);
        s.setForeground(TEXT);
        s.setPaintTicks(true);
        s.setPaintLabels(true);
        s.setMajorTickSpacing(1);
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
        b.setOpaque(false);

        b.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override public void paint(Graphics g, JComponent c) {
                AbstractButton ab = (AbstractButton) c;
                ButtonModel m = ab.getModel();
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color base = new Color(45, 30, 20, 200);
                Color hover = new Color(60, 45, 30, 220);
                g2.setColor(m.isRollover() ? hover : base);
                g2.fillRoundRect(0, 0, c.getWidth(), c.getHeight(), 18, 18);

                g2.setColor(new Color(160, 255, 255, m.isRollover() ? 180 : 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, c.getWidth()-4, c.getHeight()-4, 16, 16);

                g2.dispose();
                super.paint(g, c);
            }
        });

        b.setPreferredSize(new Dimension(160, 46));
        return b;
    }

    // If you want: reuse your forest background image here too.
    private JComponent wrapBackground(JComponent center) {
        // Simple dark backdrop. If you want the same forest image, tell me and I’ll hook it to your A_BG.
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(new Color(10, 15, 15));
        bg.add(center);
        return bg;
    }
}
