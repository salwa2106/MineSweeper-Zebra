package View;

import Controller.SettingsController;
import Model.*;

import javax.swing.*;
import java.awt.*;

public class SettingsFrame extends JFrame {

    private static final long serialVersionUID = 1L;

    private static final Color TEXT   = new Color(225, 245, 240);
    private static final Color BORDER = new Color(160, 255, 255, 130);

    private final SettingsController controller;
    private final Runnable onSaved;

    public SettingsFrame(SettingsController controller, Runnable onSaved) {
        super(safeT("btn.settings", "Settings"));
        this.controller = controller;
        this.onSaved = onSaved;

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setUndecorated(true);
        setLocationRelativeTo(null);

        setContentPane(buildContent());
        SysData.applyGlobalFont(this);

        getRootPane().registerKeyboardAction(
                e -> dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );
    }

    private JComponent buildContent() {

        GameSettings gs = controller.getGameSettings();

        JPanel root = new JPanel(new GridBagLayout());
        root.setOpaque(false);

        JPanel glass = frostedCard();
        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));

        // ---------- TITLE ----------
        JLabel title = new JLabel("SETTINGS", SwingConstants.CENTER);
        title.setFont(uiFont(Font.BOLD, 28));
        title.setForeground(new Color(190, 255, 220));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        glass.add(title);
        glass.add(Box.createVerticalStrut(18));

        // ---------- GAMEPLAY ----------
        glass.add(sectionTitle("Gameplay"));

        JComboBox<String> cbDiff = new JComboBox<>(new String[]{
                "Easy", "Medium", "Hard"
        });
        styleCombo(cbDiff);
        cbDiff.setSelectedIndex(
                gs.getDefaultDifficulty() == Difficulty.MEDIUM ? 1 :
                gs.getDefaultDifficulty() == Difficulty.HARD ? 2 : 0
        );

        JSlider lives = new JSlider(JSlider.HORIZONTAL, 1, 10, gs.getMaxSharedLives());

        lives.setMajorTickSpacing(1);
        lives.setPaintTicks(true);
        lives.setPaintLabels(true);

        // optional: nicer spacing
        lives.setPreferredSize(new Dimension(300, 45));

        styleSlider(lives);


        JComboBox<String> cbLang = new JComboBox<>(new String[]{"English", "Hebrew"});
        styleCombo(cbLang);
        cbLang.setSelectedIndex(gs.getLanguage() == Language.HE ? 1 : 0);

        JComboBox<ThemeType> cbTheme = new JComboBox<>(ThemeType.values());
        cbTheme.setFont(uiFont(Font.PLAIN, 14));
        cbTheme.setSelectedItem(SysData.getTheme());

        JCheckBox cbAnim = themedCheck("Animations", gs.isAnimationsEnabled());
        JCheckBox cbAuto = themedCheck("Auto-save history", gs.isAutoSaveHistory());

        JPanel gameplay = centeredForm();
        addRow(gameplay, 0, "Difficulty:", cbDiff);
        addRow(gameplay, 1, "Shared lives:", lives);
        addRow(gameplay, 2, "Language:", cbLang);
        addRow(gameplay, 3, "Theme:", cbTheme);

        glass.add(gameplay);
        glass.add(Box.createVerticalStrut(8));
        glass.add(cbAnim);
        glass.add(cbAuto);

        glass.add(Box.createVerticalStrut(18));

        // ---------- AUDIO ----------
        glass.add(sectionTitle("Audio"));

        JSlider volume = new JSlider(JSlider.HORIZONTAL, 0, 100,
                (int) (SysData.getMusicVolume() * 100));

        volume.setMajorTickSpacing(25);
        volume.setMinorTickSpacing(5);
        volume.setPaintTicks(true);
        volume.setPaintLabels(true);

        // 🔥 CRITICAL: give it enough height
        volume.setPreferredSize(new Dimension(300, 55));

        styleSlider(volume);

        volume.addChangeListener(e -> {
            SysData.setMusicVolume(volume.getValue() / 100f);
            MusicManager.applyVolume();
        });

        JCheckBox cbSound = themedCheck("Sound effects", gs.isSoundEnabled());
        JCheckBox cbMusic = themedCheck("Background music", SysData.isMusicEnabled());

        JPanel audio = centeredForm();
        addRow(audio, 0, "Music volume:", volume);

        glass.add(audio);
        glass.add(Box.createVerticalStrut(8));
        glass.add(cbSound);
        glass.add(cbMusic);

        glass.add(Box.createVerticalStrut(20));

        // ---------- ACTIONS ----------
        JButton save = frostedButton("Save");
        JButton cancel = frostedButton("Cancel");

        save.addActionListener(e -> {
            controller.setSoundEnabled(cbSound.isSelected());
            controller.setAnimationsEnabled(cbAnim.isSelected());
            controller.setAutoSaveHistory(cbAuto.isSelected());
            controller.setMaxSharedLives(lives.getValue());

            controller.setDefaultDifficulty(
                    cbDiff.getSelectedIndex() == 1 ? Difficulty.MEDIUM :
                    cbDiff.getSelectedIndex() == 2 ? Difficulty.HARD :
                    Difficulty.EASY
            );

            Language lang = cbLang.getSelectedIndex() == 1 ? Language.HE : Language.EN;
            controller.setLanguage(lang);
            SysData.setLanguage(lang);

            SysData.setTheme((ThemeType) cbTheme.getSelectedItem());
            SysData.setMusicEnabled(cbMusic.isSelected());

            MusicManager.play("assets/ice/music.wav");


            if (onSaved != null) onSaved.run();
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

    // ================= HELPERS =================

    private JPanel centeredForm() {
        JPanel p = new JPanel(new GridBagLayout());
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(520, Integer.MAX_VALUE));
        return p;
    }

    private void addRow(JPanel p, int y, String label, JComponent comp) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(6, 8, 6, 8);

        gc.gridx = 0;
        gc.gridy = y;
        gc.anchor = GridBagConstraints.LINE_END;

        JLabel l = new JLabel(label);
        l.setFont(uiFont(Font.BOLD, 14));
        l.setForeground(TEXT);
        p.add(l, gc);

        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;
        if (comp instanceof JSlider) {
            comp.setPreferredSize(new Dimension(300, 55));
        } else {
            comp.setPreferredSize(new Dimension(260, 28));
        }

        p.add(comp, gc);
    }

    private JPanel frostedCard() {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setColor(new Color(20, 35, 35, 190));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 36, 36);
            }
        };
        p.setOpaque(false);
        p.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        p.setMaximumSize(new Dimension(700, 620));
        return p;
    }

    private JLabel sectionTitle(String t) {
        JLabel l = new JLabel(t);
        l.setFont(uiFont(Font.BOLD, 16));
        l.setForeground(new Color(160, 230, 210));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(10, 0, 6, 0));
        return l;
    }

    private JCheckBox themedCheck(String text, boolean sel) {
        JCheckBox c = new JCheckBox(text, sel);
        c.setOpaque(false);
        c.setFont(uiFont(Font.PLAIN, 14));
        c.setForeground(TEXT);
        c.setAlignmentX(Component.CENTER_ALIGNMENT);
        return c;
    }

    private JButton frostedButton(String text) {
        JButton b = new JButton(text);
        b.setFont(uiFont(Font.BOLD, 16));
        b.setForeground(new Color(200, 255, 230));
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(140, 42));
        return b;
    }

    private void styleCombo(JComboBox<?> cb) {
        cb.setFont(uiFont(Font.PLAIN, 14));
        cb.setBackground(Color.WHITE);
    }

    private void styleSlider(JSlider s) {
        s.setOpaque(false);
        s.setForeground(TEXT);
    }

    private JComponent wrapBackground(JComponent center) {
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setBackground(new Color(10, 15, 15));
        bg.add(center);
        return bg;
    }

    private Font uiFont(int style, int size) {
        return new Font("Georgia", style, size);
    }

    private static String safeT(String key, String fallback) {
        try {
            return SysData.getI18n().t(key);
        } catch (Exception e) {
            return fallback;
        }
    }
}
