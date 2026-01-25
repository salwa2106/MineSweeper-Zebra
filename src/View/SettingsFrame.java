package View;

import Controller.SettingsController;
import Model.*;
import javax.swing.*;
import java.awt.*;
import java.util.Hashtable;

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
        setBackground(new Color(0, 0, 0, 0));
        getContentPane().setBackground(new Color(0, 0, 0, 0));
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

        // ✅ Responsive card size (NO scroll)
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        int cardW = Math.min(720, (int)(screen.width * 0.58));
        int cardH = Math.min(860, (int)(screen.height * 0.88));

        JPanel glass = frostedCard();
        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));
        glass.setPreferredSize(new Dimension(cardW, cardH));
        glass.setMinimumSize(new Dimension(Math.min(650, cardW), Math.min(760, cardH)));
        glass.setMaximumSize(new Dimension(cardW, cardH)); // ✅ allow it to fit screen

        // ---------- TITLE ----------
        JLabel title = new JLabel(
                safeT("settings.title", "Settings"),
                SwingConstants.CENTER
        );
        title.setFont(uiFont(Font.BOLD, 26));         // ✅ slightly smaller (word unchanged)
        title.setForeground(new Color(190, 255, 220));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        glass.add(title);
        glass.add(Box.createVerticalStrut(10));       // ✅ less spacing

        // ---------- GAMEPLAY ----------
        glass.add(sectionTitle(safeT("section.gameplay", "Gameplay")));
        glass.add(sectionTitle("Turn Timer"));

        JCheckBox cbTimer = themedCheck("Enable turn timer", SysData.isTurnTimerEnabled());

        JSlider turnSec = new JSlider(JSlider.HORIZONTAL, 5, 120, SysData.getTurnSecondsPerTurn());
        turnSec.setMajorTickSpacing(15);
        turnSec.setMinorTickSpacing(5);
        turnSec.setPaintTicks(true);
        turnSec.setPaintLabels(true);

        // ✅ compact slider + smaller label font (same numbers, same words)
        turnSec.setPreferredSize(new Dimension(320, 44));
        turnSec.setFont(uiFont(Font.PLAIN, 11));
        turnSec.setLabelTable(sliderLabels(new int[]{5, 30, 60, 90, 120}));
        styleSlider(turnSec);

        turnSec.setEnabled(cbTimer.isSelected());
        cbTimer.addActionListener(e -> turnSec.setEnabled(cbTimer.isSelected()));

        JPanel timerPanel = centeredForm();
        addRow(timerPanel, 0, "Seconds per turn:", turnSec);

        glass.add(timerPanel);
        glass.add(Box.createVerticalStrut(4));
        glass.add(cbTimer);
        glass.add(Box.createVerticalStrut(12));

        JComboBox<Difficulty> cbDiff = new JComboBox<>(Difficulty.values());
        cbDiff.setSelectedItem(gs.getDefaultDifficulty());
        styleCombo(cbDiff);

        JSlider lives = new JSlider(JSlider.HORIZONTAL, 1, 10, gs.getMaxSharedLives());
        lives.setMajorTickSpacing(1);
        lives.setPaintTicks(true);
        lives.setPaintLabels(true);

        // ✅ compact slider + fewer labels to stop overlap (numbers unchanged)
        lives.setPreferredSize(new Dimension(320, 44));
        lives.setFont(uiFont(Font.PLAIN, 11));
        lives.setLabelTable(sliderLabels(new int[]{1,2,3,4,5,6,7,8,9,10}));
        styleSlider(lives);

        JComboBox<Language> cbLang = new JComboBox<>(Language.values());
        cbLang.setSelectedItem(SysData.getLanguage());
        styleCombo(cbLang);
        cbLang.setSelectedIndex(gs.getLanguage() == Language.HE ? 1 : 0);

        JComboBox<ThemeType> cbTheme = new JComboBox<>(ThemeType.values());
        cbTheme.setFont(uiFont(Font.PLAIN, 14));
        cbTheme.setSelectedItem(SysData.getTheme());

        JCheckBox cbAuto  = themedCheck(safeT("settings.autosave","Auto-save history"), gs.isAutoSaveHistory());

        JPanel gameplay = centeredForm();
        addRow(gameplay, 0, safeT("settings.defaultDifficulty","Default difficulty:"), cbDiff);
        addRow(gameplay, 1, safeT("settings.maxLives","Max shared lives:"), lives);
        addRow(gameplay, 2, safeT("lbl.language","Language"), cbLang);
        addRow(gameplay, 3, safeT("lbl.theme","Theme"), cbTheme);

        glass.add(gameplay);
        glass.add(Box.createVerticalStrut(4));
        glass.add(cbAuto);
        glass.add(Box.createVerticalStrut(10));

        // ---------- AUDIO ----------
        glass.add(sectionTitle(safeT("section.audio", "Audio")));

        JSlider volume = new JSlider(JSlider.HORIZONTAL, 0, 100,
                (int) (SysData.getMusicVolume() * 100));

        volume.setMajorTickSpacing(25);
        volume.setMinorTickSpacing(5);
        volume.setPaintTicks(true);
        volume.setPaintLabels(true);

        // ✅ compact slider + small label font (fix overlap)
        volume.setPreferredSize(new Dimension(320, 44));
        volume.setFont(uiFont(Font.PLAIN, 11));
        volume.setLabelTable(sliderLabels(new int[]{0,25,50,75,100}));
        styleSlider(volume);

        volume.addChangeListener(e -> {
            SysData.setMusicVolume(volume.getValue() / 100f);
            MusicManager.applyVolume();
        });

        JCheckBox cbSound = themedCheck(safeT("settings.sound","Sound effects"), gs.isSoundEnabled());
        JCheckBox cbMusic = themedCheck(safeT("settings.music","Background music"), SysData.isMusicEnabled());

        JPanel audio = centeredForm();
        addRow(audio, 0, safeT("settings.musicVolume","Music volume:"), volume);

        glass.add(audio);
        glass.add(Box.createVerticalStrut(4));
        glass.add(cbSound);
        glass.add(cbMusic);
        glass.add(Box.createVerticalStrut(10));

        // ---------- ACTIONS ----------
        JButton save   = frostedButton(safeT("btn.save","Save"));
        JButton cancel = frostedButton(safeT("btn.cancel","Cancel"));

        save.addActionListener(e -> {
            controller.setSoundEnabled(cbSound.isSelected());
            controller.setAutoSaveHistory(cbAuto.isSelected());
            controller.setMaxSharedLives(lives.getValue());

            Difficulty selected = (Difficulty) cbDiff.getSelectedItem();
            controller.setDefaultDifficulty(selected);

            SoundManager.play(SoundManager.Sfx.CLICK);
            Language lang = (Language) cbLang.getSelectedItem();
            controller.setLanguage(lang);
            SysData.setLanguage(lang);

            SysData.setTheme((ThemeType) cbTheme.getSelectedItem());
            SysData.setMusicEnabled(cbMusic.isSelected());

            if (SysData.isMusicEnabled()) {
                MusicManager.play(SysData.getTheme().assets.music);
            } else {
                MusicManager.stop();
            }

            // ✅ timer settings
            SysData.setTurnTimerEnabled(cbTimer.isSelected());
            SysData.setTurnSecondsPerTurn(turnSec.getValue());

            if (onSaved != null) onSaved.run();
            dispose();
        });

        cancel.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            dispose();
        });

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
        p.setMaximumSize(new Dimension(620, Integer.MAX_VALUE)); // ✅ a bit wider so rows don't wrap
        return p;
    }

    private void addRow(JPanel p, int y, String label, JComponent comp) {
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 8, 4, 8);  // ✅ less vertical spacing

        gc.gridx = 0;
        gc.gridy = y;
        gc.anchor = GridBagConstraints.LINE_END;

        JLabel l = new JLabel(label);
        l.setFont(uiFont(Font.BOLD, 14));
        l.setForeground(TEXT);
        p.add(l, gc);

        gc.gridx = 1;
        gc.fill = GridBagConstraints.HORIZONTAL;

        // ✅ keep compact sizes
        if (comp instanceof JSlider) {
            comp.setPreferredSize(new Dimension(320, 44));
        } else {
            comp.setPreferredSize(new Dimension(260, 28));
        }

        p.add(comp, gc);
    }

    private JPanel frostedCard() {
        JPanel p = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(15, 25, 20, 220));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                g2.setColor(BORDER);
                g2.setStroke(new BasicStroke(3));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 36, 36);
            }
        };
        p.setOpaque(false);

        // ✅ smaller padding so everything fits
        p.setBorder(BorderFactory.createEmptyBorder(18, 34, 18, 34));

        return p;
    }

    private JLabel sectionTitle(String t) {
        JLabel l = new JLabel(t);
        l.setFont(uiFont(Font.BOLD, 15)); // ✅ slightly smaller, text unchanged
        l.setForeground(new Color(160, 230, 210));
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setBorder(BorderFactory.createEmptyBorder(8, 0, 4, 0)); // ✅ compact
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
        b.setPreferredSize(new Dimension(140, 40)); // ✅ slightly shorter
        b.addActionListener(e -> SoundManager.play(SoundManager.Sfx.CLICK));
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
        bg.setOpaque(false);
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

    // ✅ keeps numbers but controls layout so they don't crash
    private Hashtable<Integer, JLabel> sliderLabels(int[] values) {
        Hashtable<Integer, JLabel> t = new Hashtable<>();
        for (int v : values) {
            JLabel l = new JLabel(String.valueOf(v));
            l.setFont(new Font("Georgia", Font.PLAIN, 11));
            l.setForeground(TEXT);
            t.put(v, l);
        }
        return t;
    }
}
