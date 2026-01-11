package View;

import Controller.GameController;
import Controller.QuestionsController;
import Model.Board;
import Model.Cell;
import Model.CellType;
import Model.Difficulty;
import Model.MusicManager;
import Model.Question;
import Model.SoundManager;
import Model.SysData;
import Model.ThemeAssets;
import Model.ThemeType;
import Controller.SettingsController;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.FloatControl;
import javax.swing.*;
import javax.swing.plaf.LayerUI;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

/**
 * MineSweeper + Trivia — Forest Edition (Dark Wood + Moss Glow Theme)
 * NOTE: GAME LOGIC IS UNCHANGED – only the visual design is updated.
 */
public class MineSweeperPrototype extends JFrame {


	


	private ThemeAssets assets() {
	    return SysData.getTheme().assets;
	}

	private String A_BG()          { return fixPath(assets().bg); }
	private String A_GRASS()       { return fixPath(assets().tileNormal); }
	private String A_GRASS_H()     { return fixPath(assets().tileHover); }
	private String A_BROWN()       { return fixPath(assets().tileAlt); }
	private String A_FLAG()        { return fixPath(assets().flag); }
	private String A_MINE()        { return fixPath(assets().mine); }
	private String A_QUESTION()    { return fixPath(assets().question); }
	private String A_SPIKES()      { return fixPath(assets().surprise); }
	private String A_HEART_FULL()  { return fixPath(assets().heartFull); }
	private String A_HEART_EMPTY() { return fixPath(assets().heartEmpty); }
	private String A_REFRESH()     { return fixPath(assets().refresh); }

	private BackgroundPanel backgroundPanel;



   
    		private final JComboBox<Difficulty> cbDifficulty =
            new JComboBox<>(Difficulty.values());

            

    public static String fixPath(String rel) {
        try {
            String base = MineSweeperPrototype.class.getProtectionDomain()
                    .getCodeSource().getLocation().getPath();

            String decoded = java.net.URLDecoder.decode(base, "UTF-8");

            // Running in Eclipse (inside /bin/)
            if (decoded.contains("/bin")) {
                decoded = decoded.substring(0, decoded.indexOf("/bin"));
                return decoded + "/src/" + rel;
            }

            // Running from JAR
            decoded = decoded.substring(0, decoded.lastIndexOf("/"));
            return decoded + "/" + rel;

        } catch (Exception e) {
            e.printStackTrace();
            return rel;
        }
    }



    private static final int TILE_SIZE = 44;
    private boolean gameInProgress = false;
    private javax.swing.JButton resumeButton;
    private JPanel[] boardWrappers = new JPanel[2];
    private TurnGlowPanel[] glowPanels = new TurnGlowPanel[2];
    private DimPanel[] dimPanels = new DimPanel[2];
    private java.util.List<String[]> gameHistory = new java.util.ArrayList<>();



    // (for later when you fully hook MVC)
    private GameController controller;

    // buttons[owner][row][col]
    private TileButton[][][] buttons;

    // Boards for each player (0 = P1, 1 = P2)
    private final Board[] boards = new Board[2];
    // Fireworks overlay (for win animation)
    private FireworksPanel fireworks;

    private ImageIcon loadIconFit(String path, int w, int h) {
        Image img = new ImageIcon(path).getImage();
        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    /* ------------------------------ STATE ------------------------------ */
    private final CardLayout cards = new CardLayout();
    private final JPanel root = new JPanel(cards);
    private String currentScreen = SCREEN_MENU;

    private static final String SCREEN_MENU     = "MENU";
    private static final String SCREEN_NEW_GAME = "NEW_GAME";
    private static final String SCREEN_GAME     = "GAME";
    private static final String SCREEN_SETTINGS = "SETTINGS";
    private static final String SCREEN_QSETTINGS = "QSETTINGS";

    private final JLabel turnLabel        = new JLabel(safeT("status.turnInitial","Turn: Player 1"));
    private final JLabel sharedScoreLabel = new JLabel(safeT("status.sharedScoreInitial","Score: 0"));
    private final JLabel rightStats       = new JLabel();   // "<Player> • Revealed: X | Flags: Y"
    private int sharedPoints = 0;

    private final JTextField tfP1 = new JTextField("Alice", 14);
    private final JTextField tfP2 = new JTextField("Bob", 14);


    private JPanel gamePanel;

    // Shared lives (both players)
    private static final int MAX_LIVES = 10;
    private int sharedLives = 0;
    private final JLabel[] sharedHearts = new JLabel[MAX_LIVES];

    // whose turn?
    private boolean p1Turn = true;
    private final Random rng = new Random();


    // Difficulty index (0=Easy,1=Medium,2=Hard) - kept for LIFE_OVERFLOW_POINTS usage
    private int difficultyIdx = 0;

    // Current difficulty (drives rows/cols + startLives)
    private Difficulty currentDifficulty = Difficulty.EASY;

    // Overflow conversion (points per extra life) per difficulty
    private static final int[] LIFE_OVERFLOW_POINTS = {1, 2, 3};

    // per-player counters
    private final int[] flagsCount    = {0, 0};
    private final int[] revealedCount = {0, 0};

    private final Controller.SettingsController settingsController = new Controller.SettingsController();
    private final QuestionsController questionsController = new QuestionsController();
    
 // add near other fields
    private boolean gameEnded = false;




    /* ------------------------------ DARK WOOD + MOSS THEME COLORS ------------------------------ */

    private static final Color WOOD_DARK   = new Color(35, 25, 15);
    private static final Color WOOD        = new Color(60, 42, 25);
    private static final Color WOOD_LIGHT  = new Color(90, 65, 35);
    private static final Color MOSS        = new Color(120, 180, 120);
    private static final Color MOSS_GLOW   = new Color(150, 210, 150);
    private static final Color TEXT_PRIMARY= new Color(240, 235, 220);
    private static final Color TEXT_MUTED  = new Color(185, 180, 165);

    /* ------------------------------ CONSTRUCTOR ------------------------------ */
    public MineSweeperPrototype() {

        super("MineSweeper + Trivia");

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignore) {}

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

     // FULL SCREEN
     setExtendedState(JFrame.MAXIMIZED_BOTH);
     setUndecorated(true);      // removes window border
     setLocationRelativeTo(null);

     setVisible(true);

    

        // Init system & data
        SysData.init();
        loadHistoryFromCSV();

        // Default difficulty / boards
        currentDifficulty = settingsController.getDefaultDifficulty();
        boards[0] = new Board(currentDifficulty);
        boards[1] = new Board(currentDifficulty);

        // Build screens
        root.setOpaque(false);
        root.add(buildMenu(), SCREEN_MENU);
        root.add(buildNewGame(), SCREEN_NEW_GAME);

        gamePanel = buildGame(currentDifficulty.rows, currentDifficulty.cols);
        root.add(gamePanel, SCREEN_GAME);

        backgroundPanel = new BackgroundPanel(A_BG());
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.add(root, BorderLayout.CENTER);
        setContentPane(backgroundPanel);
        if (SysData.isMusicEnabled()) {
            MusicManager.play(SysData.getTheme().assets.music);
        }
        SysData.applyGlobalFont(this);
    }

    private static ImageIcon scaleIcon(String path, int size) {
        ImageIcon icon = new ImageIcon(path);
        Image img = icon.getImage().getScaledInstance(
                size, size, Image.SCALE_SMOOTH
        );
        return new ImageIcon(img);
    }



    
    public void applyThemeFromSettings() {

        ThemeAssets a = SysData.getTheme().assets;

        // 🔁 Replace background panel
        backgroundPanel.removeAll();
        backgroundPanel = new BackgroundPanel(fixPath(a.bg));
        backgroundPanel.setLayout(new BorderLayout());
        backgroundPanel.add(root, BorderLayout.CENTER);
        setContentPane(backgroundPanel);

        if (SysData.isMusicEnabled()) {
            MusicManager.play(SysData.getTheme().assets.music);
        } else {
            MusicManager.stop();   
        }

        // Rebuild game UI
        if (gamePanel != null) {
            root.remove(gamePanel);
            gamePanel = buildGame(currentDifficulty.rows, currentDifficulty.cols);
            root.add(gamePanel, SCREEN_GAME);
        }

        SwingUtilities.updateComponentTreeUI(this);
        revalidate();
        repaint();
    }


   

    private Image loadAndScale(String path) {
        return new ImageIcon(fixPath(path)).getImage()
                .getScaledInstance(getWidth(), getHeight(), Image.SCALE_SMOOTH);
    }

    private static String safeT(String key, String fallback) {
        try {
            if (SysData.getI18n() != null) {
                String v = SysData.getI18n().t(key);
                if (v == null) return fallback;

                v = v.trim();
                if (v.isBlank()) return fallback;

                // ✅ if missing translations are returned like "!some.key!"
                if (v.startsWith("!") && v.endsWith("!")) return fallback;

                // ✅ sometimes libs return the key itself
                if (v.equalsIgnoreCase(key)) return fallback;

                return v;
            }
        } catch (Exception ignored) {}
        return fallback;
    }


    private void refreshLocalization() {
        try {
            boolean he = (SysData.getI18n() != null && SysData.getI18n().isHebrew());
            applyComponentOrientation(he ? java.awt.ComponentOrientation.RIGHT_TO_LEFT
                    : java.awt.ComponentOrientation.LEFT_TO_RIGHT);
            // Rebuild static screens to update texts
            root.removeAll();
            root.setOpaque(false);
            root.add(buildMenu(), SCREEN_MENU);
            root.add(buildNewGame(), SCREEN_NEW_GAME);
            // Settings / question settings are popups now, but keep cards if you use them elsewhere
            // root.add(buildSettingsScreen(), SCREEN_SETTINGS);
            // root.add(buildQuestionSettingsScreen(), SCREEN_QSETTINGS);
            if (gamePanel != null) root.add(gamePanel, SCREEN_GAME);
            cards.show(root, currentScreen == null ? SCREEN_MENU : currentScreen);
            javax.swing.SwingUtilities.updateComponentTreeUI(this);
            SysData.applyGlobalFont(this);
            root.revalidate();
            root.repaint();
        } catch (Exception ignored) {}
    }

    private JPanel buildSettingsScreen() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        JPanel card = woodCard();
        card.add(woodHeader(safeT("settings.title",safeT("menu.settings", "Settings"))), BorderLayout.NORTH);

        JButton back = woodButton(safeT("btn.back",safeT("btn.back", "Back")));
        back.addActionListener(e ->{
        SoundManager.play(SoundManager.Sfx.CLICK);
        showMenu();
        });

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.add(new JLabel(safeT("todo.settings","TODO: Settings UI here")));

        card.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setOpaque(false);
        south.add(back);
        card.add(south, BorderLayout.SOUTH);

        page.add(card, BorderLayout.CENTER);
        return wrapWithSlideFade(page);
    }
    
    private void syncDifficultyComboFromSettings() {
        cbDifficulty.setSelectedItem(settingsController.getDefaultDifficulty());
    }


    private JPanel buildQuestionSettingsScreen() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        JPanel card = woodCard();
        card.add(woodHeader(safeT("questions.settings.title","Question Settings")), BorderLayout.NORTH);

        JButton back = woodButton(safeT("btn.back",safeT("btn.back", "Back")));
        back.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	showMenu();});

        JPanel center = new JPanel();
        center.setOpaque(false);
        center.add(new JLabel(safeT("todo.questionSettings","TODO: Question Settings UI here")));

        card.add(center, BorderLayout.CENTER);

        JPanel south = new JPanel(new FlowLayout(FlowLayout.CENTER));
        south.setOpaque(false);
        south.add(back);
        card.add(south, BorderLayout.SOUTH);

        page.add(card, BorderLayout.CENTER);
        return wrapWithSlideFade(page);
    }


    /* ------------------------------ THEME HELPERS ------------------------------ */

    /** Wooden card container (used for menu, boards, etc.) */
    private JPanel woodCard() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(WOOD_DARK);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WOOD_LIGHT, 3, true),
                BorderFactory.createEmptyBorder(18, 18, 18, 18)
        ));
        return p;
    }

    /** Gradient header bar in dark wood tones. */
    private JPanel woodHeader(String title) {
        GradientPaintPanel header = new GradientPaintPanel(
                new Color(45, 34, 24),
                new Color(25, 18, 12)
        );
        header.setLayout(new GridBagLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        JLabel lbl = new JLabel(title);
        lbl.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 24));
        lbl.setForeground(MOSS_GLOW);

        header.add(lbl);
        return header;
    }

    private static String getHistoryPath() {
        try {
            String path = SysData.class.getProtectionDomain().getCodeSource().getLocation().getPath();
            String decoded = java.net.URLDecoder.decode(path, "UTF-8");

            if (decoded.endsWith(".jar")) {
                decoded = decoded.substring(0, decoded.lastIndexOf("/"));
                System.out.println("History path (JAR): " + decoded + "/history/game_history.csv");
                return decoded + "/history/game_history.csv";
            } else {
                decoded = decoded.substring(0, decoded.lastIndexOf("/"));
                System.out.println("History path (Dev): " + decoded + "/src/history/game_history.csv");
                return decoded + "/src/history/game_history.csv";
            }

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


    /** Cost (in points) to activate a Question/Surprise cell for current difficulty. */
    private int getQuestionActivationCost() {
        return switch (currentDifficulty) {
            case EASY   -> 5;
            case MEDIUM -> 8;
            case HARD   -> 12;
        };
    }

    /** Size of the good/bad surprise effect (points) for current difficulty. */
    private int getSurpriseMagnitude() {
        return switch (currentDifficulty) {
            case EASY   -> 8;
            case MEDIUM -> 12;
            case HARD   -> 16;
        };
    }


    private void loadHistoryFromCSV() {
        String path = getHistoryPath();
        if (path == null) {
            return; // could not resolve path
        }

        java.io.File file = new java.io.File(path);
        if (!file.exists()) return;  // no history yet

        try (java.util.Scanner sc = new java.util.Scanner(file)) {

            if (sc.hasNextLine()) sc.nextLine(); // skip header

            while (sc.hasNextLine()) {
                String line = sc.nextLine();
                String[] parts = line.split(",");

                if (parts.length >= 5) {
                    gameHistory.add(new String[]{
                            parts[0], parts[1], parts[2], parts[3], parts[4]
                    });
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /** Stylized wood button with moss glow hover. */
    private JButton woodButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                Color base = WOOD;
                if (getModel().isPressed()) {
                    base = WOOD_DARK;
                } else if (getModel().isRollover()) {
                    base = WOOD_LIGHT;
                }

                g2.setColor(base);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);

                super.paintComponent(g);
            }
        };
        b.setForeground(MOSS_GLOW);
        b.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 16));
        b.setFocusPainted(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        b.setPreferredSize(new Dimension(200, 45));
        return b;
    }

    private Component space(int w) {
        return Box.createRigidArea(new Dimension(w, 0));
    }

    private JComponent wrapWithFade(JComponent comp) {
        FadeInLayerUI ui = new FadeInLayerUI();
        JLayer<JComponent> layer = new JLayer<>(comp, ui);
        ui.startFade(layer);
        return layer;
    }


    private JPanel wrapWithSlideFade(JComponent comp) {
        FadeInLayerUI ui = new FadeInLayerUI();
        JLayer<JComponent> layer = new JLayer<>(comp, ui);

        ui.startFade(layer);

        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.add(layer, BorderLayout.CENTER);

        return container;
    }



    private JPanel buildMenu() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        SnowPanel snow = new SnowPanel();
        snow.setLayout(new GridBagLayout());

        LightsOverlay lights = new LightsOverlay();

        JPanel glass = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Float alpha = (Float) getClientProperty("fadeAlpha");
                float a = (alpha == null ? 1f : alpha);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(20, 35, 35, 170));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);
                g2.setColor(new Color(160, 255, 255, 130));
                g2.setStroke(new BasicStroke(4f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 36, 36);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        glass.setOpaque(false);
        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));
        glass.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));

        // Title
        JLabel title = new JLabel(safeT("app.minesweeper","MINESWEEPER"), SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 48));
        title.setForeground(new Color(190, 255, 220));

        JLabel subtitle = new JLabel(safeT("app.subtitle",safeT("app.subtitle", "+ Trivia")), SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.PLAIN, 22));
        subtitle.setForeground(new Color(170, 220, 200));

        glass.add(title);
        glass.add(Box.createVerticalStrut(8));
        glass.add(subtitle);
        glass.add(Box.createVerticalStrut(40));

        // ✅ Create ALL 6 buttons
        JButton newGame = createFrostedButton(safeT("menu.newGame", "New Game"));
        resumeButton = createFrostedButton(safeT("menu.resume", "Resume"));
        JButton settings = createFrostedButton(safeT("menu.settings", "Settings"));

        JButton history = createFrostedButton(safeT("menu.history", "History"));
        JButton exit = createFrostedButton(safeT("btn.exit",safeT("menu.exit", "Exit")));
        JButton questionsBtn = null;
        if (SysData.isAdmin()) {
            questionsBtn = createFrostedButton("Questions");
            questionsBtn.addActionListener(e -> {
            	SoundManager.play(SoundManager.Sfx.CLICK);
            	openQuestionsWizard();});
        }


        // ✅ Button size
        Dimension btnSize = new Dimension(200, 55);
        Font btnFont = new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 20);

        for (JButton b : new JButton[]{newGame, resumeButton, settings, history, exit}) {
            b.setPreferredSize(btnSize);
            b.setMaximumSize(btnSize);
            b.setMinimumSize(btnSize);
            b.setFont(btnFont);
        }

        // ✅ Actions
        newGame.addActionListener(e -> { 
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	currentScreen = SCREEN_NEW_GAME; cards.show(root, SCREEN_NEW_GAME); });
        updateResumeButtonState();
        resumeButton.addActionListener(e -> { 
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	currentScreen = SCREEN_GAME; cards.show(root, SCREEN_GAME); });

        // ✅ Settings opens popup window
        settings.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	SettingsFrame frame = new SettingsFrame(settingsController, () -> {

        	    cbDifficulty.setSelectedIndex(switch (settingsController.getDefaultDifficulty()) {
        	        case EASY -> 0;
        	        case MEDIUM -> 1;
        	        case HARD -> 2;
        	    });

        	    int limit = getMaxLivesLimit();
        	    if (sharedLives > limit) sharedLives = limit;

        	    updateSharedHearts();
        	    syncDifficultyComboFromSettings();
        	    refreshLocalization();

        	    // ✅ THIS WAS MISSING
        	    applyThemeFromSettings();
        	});

            frame.setVisible(true);

        });

        history.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	showHistory();});

        exit.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
            int r = JOptionPane.showConfirmDialog(
                    this,
                    safeT("msg.returnToLogin",safeT("msg.returnToLogin", "Return to login screen?")),
                    safeT("dlg.confirm",safeT("dlg.confirm", "Confirm")),
                    JOptionPane.YES_NO_OPTION
            );

            if (r == JOptionPane.YES_OPTION) {
                dispose();          // close MineSweeperPrototype
                new LoginFrame();   // go back to login
            }
        });

        // ✅ 2-COLUMN GRID for all 6 buttons (3 rows × 2 columns)
        JPanel buttonGrid = new JPanel(new GridLayout(3, 2, 15, 15));
        buttonGrid.setOpaque(false);
        buttonGrid.setMaximumSize(new Dimension(430, 200));
        buttonGrid.setAlignmentX(Component.CENTER_ALIGNMENT);

        if (SysData.isAdmin()) {
            // ✅ ADMIN: 6 buttons fill 3x2
            buttonGrid.add(newGame);
            buttonGrid.add(resumeButton);
            buttonGrid.add(settings);
            buttonGrid.add(questionsBtn);   // admin-only
            buttonGrid.add(history);
            buttonGrid.add(exit);
        } else {
            // ✅ USER: only 5 buttons + filler for the 6th slot
            buttonGrid.add(newGame);
            buttonGrid.add(resumeButton);
            buttonGrid.add(settings);
            buttonGrid.add(history);
            buttonGrid.add(exit);
            buttonGrid.add(Box.createGlue()); // filler
        }

        glass.add(buttonGrid);

        JPanel stacked = new JPanel(new BorderLayout());
        stacked.setOpaque(false);
        stacked.add(lights, BorderLayout.NORTH);
        stacked.add(glass, BorderLayout.CENTER);

        snow.add(stacked);
        page.add(snow, BorderLayout.CENTER);

        return wrapWithSlideFade(page);
    }

    private void openQuestionsWizard() {
        if (!SysData.isAdmin()) {
            JOptionPane.showMessageDialog(this, safeT("msg.adminOnly",safeT("msg.adminOnly", "Admin only.")));
            return;
        }

        QuestionsWizardFrame.QuestionsController adapter =
                new QuestionsWizardFrame.QuestionsController() {
                    @Override
                    public java.util.List<Model.Question> getAllQuestions() {
                        return questionsController.getAllQuestions();
                    }


                    @Override
                    public void importFromCsv(java.io.File file) throws Exception {
                        questionsController.importFromCsv(file);
                    }

                    @Override
                    public void exportToCsv(java.io.File file) throws Exception {
                        questionsController.exportToCsv(file);
                    }

                    @Override
                    public void addQuestion(Model.Question q) throws Exception {
                        questionsController.addQuestion(q);
                    }

                    @Override
                    public void updateQuestionAtIndex(int index, Model.Question q) throws Exception {
                        questionsController.updateQuestionAtIndex(index, q);
                    }

                    @Override
                    public void deleteQuestionAtIndex(int index) throws Exception {
                        questionsController.deleteQuestionAtIndex(index);
                    }
                };

        QuestionsWizardFrame wizard = new QuestionsWizardFrame(adapter, () -> {
            // back
        });
        wizard.setVisible(true);
    }



    private JButton createFrostedButton(String text) {
        JButton b = new JButton(text) {

            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                boolean hover = getModel().isRollover();
                boolean pressed = getModel().isPressed();

                // --- Background ---
                Color base   = new Color(50, 80, 65, 220);
                Color hoverC = new Color(70, 115, 95, 235);
                Color pressC = new Color(40, 65, 55, 240);

                Color bg = pressed ? pressC : (hover ? hoverC : base);
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                // --- Border Glow ---
                g2.setStroke(new BasicStroke(2f));
                g2.setColor(new Color(180, 255, 245, hover ? 220 : 140));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 16, 16);

                // --- Text (draw manually for clarity) ---
                FontMetrics fm = g2.getFontMetrics();
                int textW = fm.stringWidth(getText());
                int textH = fm.getAscent();

                g2.setColor(new Color(235, 255, 250));
                g2.drawString(
                        getText(),
                        (getWidth() - textW) / 2,
                        (getHeight() + textH) / 2 - 2
                );

                g2.dispose();
            }
        };

        b.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 15));
        b.setFocusPainted(false);
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        b.setBorderPainted(false);
        b.setContentAreaFilled(false);
        b.setOpaque(true); // IMPORTANT
        b.setPreferredSize(new Dimension(140, 36));

        return b;
    }



    private void updateResumeButtonState() {
        if (resumeButton != null) {
            resumeButton.setEnabled(gameInProgress);
        }
    }

    private void showMenu() {
        updateResumeButtonState();
        currentScreen = SCREEN_MENU;
        cards.show(root, SCREEN_MENU);
        root.revalidate();
        root.repaint();
    }

    /* ------------------------------ NEW GAME SCREEN ------------------------------ */

    private JPanel buildNewGame() {

        // OUTER PAGE (transparent)
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        // ❄ Snow background
        SnowPanel snow = new SnowPanel();
        snow.setLayout(new GridBagLayout());

        // 🎄 Lights (same usage as menu)
        LightsOverlay lights = new LightsOverlay();

        // 🧊 Glass card (IDENTICAL STYLE TO MENU)
        JPanel glass = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                Float alpha = (Float) getClientProperty("fadeAlpha");
                float a = (alpha == null ? 1f : alpha);

                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 35, 35, 170));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                g2.setColor(new Color(160, 255, 255, 130));
                g2.setStroke(new BasicStroke(4f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 36, 36);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        glass.setOpaque(false);
        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));
        glass.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        glass.setPreferredSize(new Dimension(420, 440));

        // ⭐ TITLE (same hierarchy as menu)
        JLabel title = new JLabel(safeT("newgame.title",safeT("newgame.title", "NEW GAME")), SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 42));
        title.setForeground(new Color(190, 255, 220));

        JLabel subtitle = new JLabel(safeT("newgame.subtitle",safeT("newgame.subtitle", "Game Setup")), SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.PLAIN, 20));
        subtitle.setForeground(new Color(170, 220, 200));

        glass.add(title);
        glass.add(Box.createVerticalStrut(8));
        glass.add(subtitle);
        glass.add(Box.createVerticalStrut(30));

        // ---------------- FORM (SIMPLIFIED & MENU-LIKE) ----------------
        JPanel form = new JPanel();
        form.setOpaque(false);
        form.setLayout(new BoxLayout(form, BoxLayout.Y_AXIS));

        form.add(createLabeledField(safeT("newgame.player1", "Player 1 Name"), tfP1));
        form.add(Box.createVerticalStrut(14));
        form.add(createLabeledField(safeT("newgame.player2", "Player 2 Name"), tfP2));
        form.add(Box.createVerticalStrut(14));
        form.add(createLabeledField(safeT("newgame.difficulty", "Difficulty"), cbDifficulty));

        glass.add(form);
        glass.add(Box.createVerticalStrut(35));

        // ---------------- BUTTONS (MATCH MENU) ----------------
        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        actions.setOpaque(false);

        JButton start = createFrostedButton(safeT("newgame.start", "Start Game"));
        JButton back  = createFrostedButton(safeT("btn.back", "Back"));

        start.setPreferredSize(new Dimension(200, 55));
        back.setPreferredSize(new Dimension(200, 55));

        start.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	startGame();});
        back.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	showMenu();});

        actions.add(start);
        actions.add(back);
        glass.add(actions);

        // ---------------- STACK LIKE MENU ----------------
        JPanel stacked = new JPanel(new BorderLayout());
        stacked.setOpaque(false);
        stacked.add(lights, BorderLayout.NORTH);
        stacked.add(glass, BorderLayout.CENTER);

        snow.add(stacked);
        page.add(snow, BorderLayout.CENTER);

        return wrapWithSlideFade(page);
    }

    private JPanel createLabeledField(String label, JComponent field) {
        JLabel l = new JLabel(label);
        l.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 16));
        l.setForeground(new Color(220, 240, 235));

        JPanel row = new JPanel(new BorderLayout(12, 0));
        row.setOpaque(false);
        row.add(l, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        row.setMaximumSize(new Dimension(360, 40));

        return row;
    }



    private void styleField(JTextField tf) {
        tf.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.PLAIN, 14));
        tf.setForeground(TEXT_PRIMARY);
        tf.setCaretColor(MOSS_GLOW);
        tf.setOpaque(true);
        tf.setBackground(WOOD);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(WOOD_LIGHT, 2, true),
                BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
    }

    /** This is the logic that used to be inline in the Start button. */
    private void startGame() {
        String p1 = tfP1.getText().trim();
        String p2 = tfP2.getText().trim();

        if (p1.isEmpty() || p2.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter both player names.",
                    "Input Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        // This difficulty is ONLY for the current game (chosen in safeT("menu.newGame", "New Game") screen)
        currentDifficulty = (Difficulty) cbDifficulty.getSelectedItem();

        difficultyIdx = switch (currentDifficulty) {
            case EASY -> 0;
            case MEDIUM -> 1;
            case HARD -> 2;
        };
       

        // ❌ IMPORTANT:
        // Do NOT save this as "default difficulty", otherwise it overrides SettingsFrame choice.
        // settingsController.setDefaultDifficulty(currentDifficulty);

        int rows = currentDifficulty.rows;
        int cols = currentDifficulty.cols;

        boards[0] = new Board(currentDifficulty);
        boards[1] = new Board(currentDifficulty);

        flagsCount[0] = flagsCount[1] = 0;
        revealedCount[0] = revealedCount[1] = 0;
        gameInProgress = true;

        root.remove(gamePanel);
        gamePanel = buildGame(rows, cols);
        root.add(wrapWithSlideFade(gamePanel), SCREEN_GAME);

        sharedPoints = 0;
        updateSharedScoreLabel();
        resetSharedLives();

        p1Turn = true;
        turnLabel.setText(safeT("status.turnPrefix",safeT("status.turnPrefix", "Turn: ")) + p1);
        refreshRightStats();

        cards.show(root, SCREEN_GAME);
    }


    /* ------------------------------ BOARD SKINS ------------------------------ */

    private static class TileSet {
        final ImageIcon normal, hover;
        TileSet(ImageIcon normal, ImageIcon hover) { this.normal = normal; this.hover = hover; }
    }

    private TileButton tileButton(TileSet tiles) {
        int dynamicSize = computeTileSize(currentDifficulty.rows, currentDifficulty.cols);

        TileButton b = new TileButton(dynamicSize);

        b.setIcon(tiles.normal);
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorder(null);

        b.getModel().addChangeListener(e -> {
            if (!b.isEnabled()) return;
            if (b.isRevealedVisual()) return;

            boolean roll = b.getModel().isRollover();
            b.setIcon(roll ? tiles.hover : tiles.normal);
        });

        return b;
    }


    // 🎆 FIREWORKS OVERLAY
    class FireworksPanel extends JPanel {

        private class Firework {
            float x, y;
            float dx, dy;
            float life;
            Color color;
        }

        private java.util.List<Firework> sparks = new java.util.ArrayList<>();
        private final Timer timer;

        FireworksPanel() {
            setOpaque(false);

            timer = new Timer(16, e -> {
                update();
                repaint();
            });
        }

        void startFireworks() {
            sparks.clear();

            // spawn 80 sparks at random top positions
            for (int i = 0; i < 80; i++) {
                Firework f = new Firework();
                f.x = (float)(getWidth() * Math.random());
                f.y = (float)(getHeight() * Math.random() * 0.4);

                double angle = Math.random() * Math.PI * 2;
                float speed = 2f + (float)(Math.random() * 4);

                f.dx = (float)(Math.cos(angle) * speed);
                f.dy = (float)(Math.sin(angle) * speed);
                f.life = 1f;

                Color[] palette = {
                        new Color(255,70,70),
                        new Color(255,180,40),
                        new Color(120,200,255),
                        new Color(140,255,140),
                        new Color(255,255,255)
                };
                f.color = palette[(int)(Math.random()*palette.length)];

                sparks.add(f);
            }

            timer.start();
        }

        void update() {
            for (Firework f : sparks) {
                f.x += f.dx;
                f.y += f.dy;
                f.dy += 0.05f;   // gravity
                f.life -= 0.015f;
            }
            sparks.removeIf(f -> f.life <= 0);

            if (sparks.isEmpty()) {
                timer.stop();
            }
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            for (Firework f : sparks) {
                int alpha = (int)(255 * f.life);
                g2.setColor(new Color(f.color.getRed(), f.color.getGreen(), f.color.getBlue(), alpha));
                g2.fillOval((int)f.x, (int)f.y, 6, 6);
            }
        }
    }

    private int computeTileSize(int rows, int cols) {
        int screenW = getWidth() == 0 ? 1400 : getWidth(); // fallback before display
        int screenH = getHeight() == 0 ? 900 : getHeight();

        // Fit width of 2 boards with spacing
        int availableW = (int)(screenW * 0.42); // each board takes ~42%

        int sizeW = availableW / cols;
        int sizeH = (int)((screenH * 0.55) / rows);

        // tile size is min of both
        int size = Math.min(sizeW, sizeH);

        // clamp sizes (avoid being too small or too big)
        if (size > 60) size = 60;
        if (size < 28) size = 28; // for Hard mode

        return size;
    }


    private JPanel buildGame(int rows, int cols) {

        // OUTER PAGE
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        // HEADER BAR stays the same
        page.add(headerBarForGame(), BorderLayout.NORTH);

        // allocate buttons
        buttons = new TileButton[2][rows][cols];

        // ❄ BACKGROUND SNOW (same as menu)
        SnowPanel snow = new SnowPanel();
        snow.setLayout(new GridBagLayout());

        // 🎄 CHRISTMAS LIGHTS
        LightsOverlay lights = new LightsOverlay();

        // 🧊 FROSTED MAIN BOARD CARD
        JPanel glassBoard = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Float alpha = (Float) getClientProperty("fadeAlpha");
                float a = (alpha == null ? 1f : alpha);
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));

                g2.setColor(new Color(20, 35, 35, 170));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);

                g2.setColor(new Color(160, 255, 255, 130));
                g2.setStroke(new BasicStroke(3f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 30, 30);

                g2.dispose();
                super.paintComponent(g);
            }
        };
        glassBoard.setOpaque(false);
        glassBoard.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        // -------------------------
        // BUILD THE TWO BOARDS
        // -------------------------
        int TILE = computeTileSize(rows, cols);

        TileSet moss = new TileSet(
                loadIconFit(A_GRASS(), TILE, TILE),
                loadIconFit(A_GRASS(), TILE, TILE)
        );
        TileSet cedar = new TileSet(
                loadIconFit(A_BROWN(), TILE, TILE),
                loadIconFit(A_BROWN(), TILE, TILE)
        );


        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 30, 20));
        boardsPanel.setOpaque(false);

        String p1Name = tfP1.getText().trim();
        String p2Name = tfP2.getText().trim();

        boardsPanel.add(boardCard(p1Name + "'s Board", rows, cols, moss, 0));
        boardsPanel.add(boardCard(p2Name + "'s Board", rows, cols, cedar, 1));


        // add boards to frosted card
        glassBoard.add(boardsPanel, BorderLayout.CENTER);

        // 🔄 Restart icon (centered above boards)
        JButton restartBtn = createRestartIconButton();

        JPanel restartHolder = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 8));
        restartHolder.setOpaque(false);
        restartHolder.add(restartBtn);

        // combine restart + board
        JPanel centerStack = new JPanel(new BorderLayout());
        centerStack.setOpaque(false);
        centerStack.add(restartHolder, BorderLayout.NORTH);
        centerStack.add(glassBoard, BorderLayout.CENTER);

        // add lights on top (same as menu)
        JPanel layered = new JPanel(new BorderLayout());
        layered.setOpaque(false);
        layered.add(lights, BorderLayout.NORTH);
        layered.add(centerStack, BorderLayout.CENTER);

        // place inside snow background
        snow.add(layered);


        // ❄ fade + slide
        JPanel finalPanel = wrapWithSlideFade(snow);

        page.add(finalPanel, BorderLayout.CENTER);

        return page;
    }




    private JComponent boardCard(String title, int rows, int cols, TileSet tiles, int ownerIdx) {
        JPanel outer = new JPanel(new BorderLayout());
        outer.setOpaque(false);


        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 18));
        lbl.setForeground(MOSS_GLOW);
        outer.add(lbl, BorderLayout.NORTH);

        int TILE = computeTileSize(rows, cols);
        int gap = Math.max(1, TILE / 12);
        JPanel grid = new JPanel(new GridLayout(rows, cols, gap, gap));

        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                final int rr = r;
                final int cc = c;

                final TileButton cellButton = tileButton(tiles);


                buttons[ownerIdx][rr][cc] = cellButton;

                final JPopupMenu m = new JPopupMenu();
                JMenuItem flag = new JMenuItem("Toggle Flag");
                m.add(flag);

                cellButton.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e)  {
                        if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e))
                            m.show(cellButton, e.getX(), e.getY());
                    }
                    public void mouseReleased(MouseEvent e) {
                        if (e.isPopupTrigger())
                            m.show(cellButton, e.getX(), e.getY());
                    }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) flag.doClick();
                    }
                });

                flag.addActionListener(ev -> {
                	toggleFlag(ownerIdx, rr, cc);});

                // REAL logic: call handleCellClick using underlying Board & Cell
                cellButton.addActionListener(e -> {
                    handleCellClick(ownerIdx, rr, cc);
                });
                grid.add(cellButton);
            }
        }

        JScrollPane sp = new JScrollPane(grid);
        sp.setBorder(null);
        sp.getViewport().setOpaque(false);
        sp.setOpaque(false);
        sp.setBackground(new Color(0,0,0,0));
        sp.getViewport().setBackground(new Color(0,0,0,0));

        outer.add(sp, BorderLayout.CENTER);


        TurnGlowPanel glow = new TurnGlowPanel();

        JLayeredPane layer = new JLayeredPane();
        layer.setPreferredSize(outer.getPreferredSize());

        // 🟩 NEW: Create and attach DimPanel for this board
        DimPanel dim = new DimPanel();
        dim.setBounds(0, 0, outer.getPreferredSize().width, outer.getPreferredSize().height);
        dimPanels[ownerIdx] = dim;

        outer.setBounds(0, 0, outer.getPreferredSize().width, outer.getPreferredSize().height);
        glow.setBounds(0, 0, outer.getPreferredSize().width, outer.getPreferredSize().height);

        layer.add(outer, JLayeredPane.DEFAULT_LAYER);
        layer.add(dimPanels[ownerIdx], JLayeredPane.MODAL_LAYER);  // dim panel
        layer.add(glow, JLayeredPane.PALETTE_LAYER);

        glowPanels[ownerIdx] = glow;
        boardWrappers[ownerIdx] = outer;

        return layer;

    }

    /* ------------------------------ FLAG TOGGLE (NOW A METHOD) ------------------------------ */

    private void toggleFlag(int ownerIdx, int row, int col) {
        int currentPlayer = p1Turn ? 0 : 1;
        if (ownerIdx != currentPlayer) return;

        Board board = boards[ownerIdx];
        Cell cell = board.getCell(row, col);

        // can't flag revealed cells
        if (cell.isRevealed()) return;

        boolean wasFlagged = cell.isFlagged();
        cell.toggleFlag();
        SoundManager.play(SoundManager.Sfx.FLAG);
        boolean flagged = cell.isFlagged();

        TileButton cellButton = buttons[ownerIdx][row][col];
        int W = cellButton.getPreferredSize().width;
        int H = cellButton.getPreferredSize().height;
        cellButton.setOverlayIcon(flagged ? loadIconFit(A_FLAG(), W/2, H/2) : null);

        // update flag counters
        flagsCount[ownerIdx] += flagged ? 1 : -1;
        if (flagsCount[ownerIdx] < 0) flagsCount[ownerIdx] = 0;
        refreshRightStats();

        // --------------------------------------------------------------------
        // 🔥 POINTS FIX: Only score the FIRST TIME a flag is placed.
        // Removing → NO effect. Replacing → NO effect.
        // --------------------------------------------------------------------
        if (flagged && !cell.isFlagScored()) {
            if (cell.getType() == CellType.MINE) {
                bumpScore(1);        // CORRECT FLAG
            } else {
                bumpScore(-3);       // WRONG FLAG
            }
            cell.setFlagScored(true);
        }

    }



    /* ------------------------------ WIDGETS ------------------------------ */

    private static class TileButton extends JButton {

        private Image overlay;          // ← THIS is the variable you were missing
        private boolean revealedVisual = false;
        private float revealAlpha = 0f;   // fade animation %
        private boolean fading = false;
        private java.util.List<Point> snowOnTile = new java.util.ArrayList<>();


        TileButton(int size) {
            setPreferredSize(new Dimension(size, size));
            setMinimumSize(new Dimension(size, size));
            setMaximumSize(new Dimension(size, size));

            setMargin(new Insets(0,0,0,0));
            setContentAreaFilled(false);
            setOpaque(false);
            setBorder(null);
            setFocusPainted(false);

            setFont(new Font("Georgia", Font.BOLD, Math.max(12, size/3)));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }


        void setOverlayIcon(ImageIcon icon) {
            overlay = (icon == null) ? null : icon.getImage();
            repaint();
        }

        void setRevealedVisual(boolean r) {
            if (r && !revealedVisual) {
                revealedVisual = true;

                // start fade animation
                fading = true;
                revealAlpha = 0f;

                // add 3–5 snowflakes on tile
                snowOnTile.clear();
                int count = 3 + (int)(Math.random() * 3);
                for (int i = 0; i < count; i++) {
                    int x = (int)(Math.random() * getWidth());
                    int y = (int)(Math.random() * getHeight());
                    snowOnTile.add(new Point(x, y));
                }

                // animation timer
                Timer t = new Timer(20, e -> {
                    revealAlpha += 0.08f;
                    if (revealAlpha >= 1f) {
                        revealAlpha = 1f;
                        fading = false;
                        ((Timer)e.getSource()).stop();
                    }
                    repaint();
                });
                t.start();
            } else {
                revealedVisual = r;
            }
        }


        boolean isRevealedVisual() {
            return revealedVisual;
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // Draw transparent background
            g2.setComposite(AlphaComposite.SrcOver);

            // ---------------------------------
            // 1) HIDDEN TILE (normal state)
            // ---------------------------------
            if (!revealedVisual) {

                // draw base tile FIRST
                Icon base = getIcon();
                if (base != null) {
                    base.paintIcon(this, g2, 0, 0);
                }

                // draw overlay ABOVE the tile
                if (overlay != null) {
                    g2.drawImage(
                        overlay,
                        0,
                        0,
                        getWidth(),
                        getHeight(),
                        this
                    );
                }

                g2.dispose();
                return;
            }


            // ---------------------------------
            // 2) REVEALED TILE (fade animation)
            // ---------------------------------
            float alpha = fading ? revealAlpha : 1f;
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));

           

         // draw base tile / text FIRST
            super.paintComponent(g2);

            // draw overlay FULL SIZE ON TOP
            if (overlay != null) {
                g2.drawImage(
                    overlay,
                    0,
                    0,
                    getWidth(),
                    getHeight(),
                    this
                );
            }


            // ---------------------------------
            // 3) GLOW OUTLINE
            // ---------------------------------
            g2.setComposite(AlphaComposite.SrcOver);
            g2.setStroke(new BasicStroke(2f));
            g2.setColor(new Color(0, 255, 255, 120)); // cyan glow
            g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 6, 6);

            // ---------------------------------
            // 4) SNOW ACCUMULATION
            // ---------------------------------
            g2.setColor(new Color(255,255,255,230));
            for (Point p : snowOnTile) {
                g2.fillOval(p.x, p.y, 3, 3);
            }

            g2.dispose();
        }
    }



    /* ------------------------------ HEADER BAR / SCORE / LIVES ------------------------------ */

    private JPanel headerBarForGame() {
        JPanel bar = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(
                        0, 0, WOOD_DARK,
                        0, getHeight(), WOOD
                );
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 10));
        bar.setPreferredSize(new Dimension(0, 60));
        bar.setOpaque(false);

        turnLabel.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 16));
        turnLabel.setForeground(MOSS_GLOW);

        sharedScoreLabel.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 14));
        sharedScoreLabel.setForeground(TEXT_PRIMARY);
        updateSharedScoreLabel();

        JButton help = woodButton(safeT("btn.help","Help"));
        JButton menu = woodButton(safeT("btn.mainMenu","Main Menu"));
        JButton historyBtn = woodButton(safeT("btn.history",safeT("menu.history", "History")));
        historyBtn.setPreferredSize(new Dimension(110, 34));
        historyBtn.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	showHistory();});

        help.setPreferredSize(new Dimension(90, 34));
        menu.setPreferredSize(new Dimension(90, 34));

        help.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	showHelp();});
        menu.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	showMenu();});

        JLabel scoreTitle = new JLabel(safeT("lbl.score","Score:"));
        scoreTitle.setForeground(TEXT_PRIMARY);
        scoreTitle.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 14));

        JLabel livesLbl = new JLabel(safeT("lbl.lives","Lives:"));
        livesLbl.setForeground(TEXT_PRIMARY);
        livesLbl.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 14));

        JPanel sharedBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        sharedBar.setOpaque(false);
        for (int i = 0; i < MAX_LIVES; i++) {
            JLabel h = new JLabel();
            h.setPreferredSize(new Dimension(22, 22));
            sharedHearts[i] = h;
            sharedBar.add(h);
        }
        updateSharedHearts();

        bar.add(turnLabel);
        bar.add(space(24));
        bar.add(scoreTitle);
        bar.add(sharedScoreLabel);
        bar.add(space(24));
        bar.add(livesLbl);
        bar.add(sharedBar);
        bar.add(space(24));
        bar.add(help);
        bar.add(menu);
        bar.add(historyBtn);
        bar.add(space(24));

        rightStats.setFont(new Font((SysData.getI18n()!=null && SysData.getI18n().isHebrew()) ? "SansSerif" : "Georgia", Font.BOLD, 14));
        rightStats.setForeground(TEXT_PRIMARY);
        refreshRightStats();
        bar.add(rightStats);

        return bar;
    }

    private void refreshRightStats() {
        int idx = p1Turn ? 0 : 1;
        String currentName = p1Turn ? tfP1.getText().trim() : tfP2.getText().trim();
        rightStats.setText(currentName + " " + safeT("status.revealedBullet","• Revealed: ") + revealedCount[idx] + " " + safeT("status.flagsSep","| Flags: ") + flagsCount[idx]);
    }

    /* ------------------------------ GAME LOGIC (unchanged) ------------------------------ */

    private void toggleTurnLabel() {
        p1Turn = !p1Turn;

        String p1 = tfP1.getText().trim();
        String p2 = tfP2.getText().trim();
        turnLabel.setText(safeT("status.turnPrefix",safeT("status.turnPrefix", "Turn: ")) + (p1Turn ? p1 : p2));
        refreshRightStats();

        int active = p1Turn ? 0 : 1;
        int inactive = p1Turn ? 1 : 0;

        // frosted aura
        glowPanels[active].setActive(true);
        glowPanels[inactive].setActive(false);

        // dimming
        dimPanels[active].setDim(1f);
        dimPanels[inactive].setDim(0.45f);
    }



    private void showHelp() {
        currentScreen = SCREEN_MENU; // help is a dialog/popup

        JOptionPane.showMessageDialog(this,
                safeT("help.controlsHeader","Controls:\n") +
                        "• Left-click: Reveal cell\n" +
                        "• Right-click: Toggle flag\n" +
                        "• Numbers show adjacent mines\n" +
                        "• Trivia answers can gain/lose lives\n" +
                        "• Shared lives (max 10); overflow converts to points\n",
                "How to Play", JOptionPane.INFORMATION_MESSAGE);
    }
    private void shakeWindow() {
        final Point original = getLocation();
        final int SHAKE_DISTANCE = 8;

        Timer t = new Timer(15, null);
        t.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
            int x = original.x + (int)(Math.random() * SHAKE_DISTANCE - SHAKE_DISTANCE/2);
            int y = original.y + (int)(Math.random() * SHAKE_DISTANCE - SHAKE_DISTANCE/2);
            setLocation(x, y);
        });

        // Stop shake after 250ms
        new Timer(250, e -> {
            t.stop();
            setLocation(original);
        }).start();

        t.start();
    }

    private void handleCellClick(int ownerIdx, int row, int col) {

        // ⛔ If the game is over (no lives), do nothing
        if (sharedLives == 0) {
            return;
        }

        // 🔒 ONLY CURRENT PLAYER MAY CLICK THEIR OWN BOARD
        int currentPlayer = p1Turn ? 0 : 1;
        if (ownerIdx != currentPlayer) {
            return;
        }

        Board board = boards[ownerIdx];
        if (board == null) return;

        Cell cell = board.getCell(row, col);

        // Flags block everything
        if (cell.isFlagged()) {
            return;
        }

        // Already revealed and NOT a special cell → nothing to do
        if (cell.isRevealed()
                && cell.getType() != CellType.QUESTION
                && cell.getType() != CellType.SURPRISE) {
            return;
        }

        CellType type = cell.getType();
        boolean usedTurn = false;

        switch (type) {

            /* ----------------------------------------------------
               MINE
            ---------------------------------------------------- */
            case MINE -> {
                if (!cell.isRevealed()) {
                    cell.reveal();
                    SoundManager.play(SoundManager.Sfx.BOOM);
                    updateButtonForCell(ownerIdx, cell);
                    bumpRevealedForCurrentTurn();

                    // ❌ POINT PENALTY FOR STEPPING ON A MINE
                    bumpScore(-3);

                    JOptionPane.showMessageDialog(
                            this,
                            "BOOM! Mine hit!\n(-3 points)",
                            "Mine",
                            JOptionPane.WARNING_MESSAGE
                    );

                    loseSharedLives(1);
                    if (sharedLives == 0) return;
                    usedTurn = true;


                    shakeWindow();
                }
            }

            /* ----------------------------------------------------
               EMPTY → Cascade reveal
            ---------------------------------------------------- */
            case EMPTY -> {
                if (!cell.isRevealed()) {
                    List<Cell> revealed = board.revealCascade(row, col);
                    
                    if (!revealed.isEmpty()) {
                        SoundManager.play(SoundManager.Sfx.REVEAL); // ✅ play once per cascade
                    }
                    
                    for (Cell c : revealed) {
                        updateButtonForCell(ownerIdx, c);
                        bumpRevealedForCurrentTurn();

                        // ⭐ SAFE CELL SCORING
                        if (!c.isRevealScored() && c.getType() != CellType.MINE) {
                            bumpScore(1);
                            c.setRevealScored(true);
                        }
                    }
                    if (!revealed.isEmpty()) {
                        usedTurn = true;
                    }
                }
            }

            /* ----------------------------------------------------
               NUMBER
            ---------------------------------------------------- */
            case NUMBER -> {
                if (!cell.isRevealed()) {
                    cell.reveal();
                    SoundManager.play(SoundManager.Sfx.REVEAL);
                    updateButtonForCell(ownerIdx, cell);
                    bumpRevealedForCurrentTurn();

                    // ⭐ SAFE CELL SCORING
                    if (!cell.isRevealScored()) {
                        bumpScore(1);
                        cell.setRevealScored(true);
                    }

                    usedTurn = true;
                }
            }

            /* ----------------------------------------------------
               QUESTION — (2-step activation)
            ---------------------------------------------------- */
            case QUESTION -> {

                // If already USED → cannot activate again
                if (cell.isSpecialUsed()) {
                    break;
                }

                // SECOND CLICK → Activation (does NOT change turn)
                if (cell.isRevealed()) {

                    int baseCost = getQuestionActivationCost();

                    // ⛔ Not enough points → block activation
                    if (sharedPoints < baseCost) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Not enough points to activate this Question cell.\n" +
                                        safeT("msg.requiredPrefix","Required: ") + baseCost + safeT("msg.pointsSuffix"," points."),
                                safeT("msg.insufficientPointsTitle","Insufficient Points"),
                                JOptionPane.WARNING_MESSAGE
                        );
                        break;
                    }

                    // 🎲 Random difficulty (NO user choice)
                    String[] diffOptions = {"easy", "medium", "hard", "pro"};
                    String diffKey = diffOptions[rng.nextInt(diffOptions.length)];

                    Question q = SysData.nextRandomByDifficulty(diffKey);
                    if (q == null) {
                        JOptionPane.showMessageDialog(
                                this,
                                safeT("msg.noQuestionsForDifficulty","No questions available for difficulty: ") + diffKey,
                                safeT("cell.question.title","Question Cell"),
                                JOptionPane.WARNING_MESSAGE
                        );
                        break;
                    }

                    int choice = JOptionPane.showConfirmDialog(
                            this,
                            safeT("cell.question.intro","This is a Question cell.\n") +
                                    safeT("cell.question.randomDifficulty","Random difficulty: ") + diffKey.toUpperCase() + "\n" +
                                    safeT("cell.question.costPrefix","Using it costs ") + baseCost + safeT("msg.pointsSuffix"," points.") + "\n" +
                                    safeT("msg.doYouWantToContinue","Do you want to continue?"),
                            safeT("cell.question.title","Question Cell"),
                            JOptionPane.YES_NO_OPTION
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        bumpScore(-baseCost);
                        showQuestionDialog(q);
                        cell.setSpecialUsed(true);
                        updateButtonForCell(ownerIdx, cell);

                    }

                    break;
                }

                // FIRST CLICK → Reveal (this DOES change turn)
                if (!cell.isRevealed()) {
                    cell.reveal();
                    SoundManager.play(SoundManager.Sfx.REVEAL);
                    updateButtonForCell(ownerIdx, cell);
                    bumpRevealedForCurrentTurn();

                    if (!cell.isRevealScored()) {
                        bumpScore(1);
                        cell.setRevealScored(true);
                    }

                    usedTurn = true;
                }
            }

            /* ----------------------------------------------------
               SURPRISE — (2-step activation)
            ---------------------------------------------------- */
            case SURPRISE -> {

                // If already USED → cannot activate again
                if (cell.isSpecialUsed()) {
                    break;
                }

                // SECOND CLICK → Activation (does NOT change turn)
                if (cell.isRevealed()) {

                    int baseCost  = getQuestionActivationCost();
                    int magnitude = getSurpriseMagnitude();

                    // ⛔ Not enough points → block activation
                    if (sharedPoints < baseCost) {
                        JOptionPane.showMessageDialog(
                                this,
                                "Not enough points to activate this Surprise cell.\n" +
                                        safeT("msg.requiredPrefix","Required: ") + baseCost + safeT("msg.pointsSuffix"," points."),
                                safeT("msg.insufficientPointsTitle","Insufficient Points"),
                                JOptionPane.WARNING_MESSAGE
                        );
                        break;
                    }

                    int choice = JOptionPane.showConfirmDialog(
                            this,
                            safeT("cell.surprise.intro","This is a Surprise cell.\n") +
                                    safeT("cell.surprise.costPrefix","Activating it costs ") + baseCost + safeT("msg.pointsSuffix"," points.") + "\n" +
                                    safeT("cell.surprise.odds","There is a 50/50 chance for a good or bad surprise.\n") +
                                    safeT("msg.doYouWantToActivate","Do you want to activate it?"),
                            safeT("cell.surprise.title","Surprise Cell"),
                            JOptionPane.YES_NO_OPTION
                    );

                    if (choice == JOptionPane.YES_OPTION) {
                        bumpScore(-baseCost);

                        boolean good = rng.nextBoolean();

                        if (good) {
                            bumpScore(magnitude);
                            gainSharedLives(1);
                            JOptionPane.showMessageDialog(
                                    this,
                                    safeT("surprise.goodPrefix","Good surprise! 🎁\n+") + magnitude + safeT("surprise.goodSuffix"," points and +1 life."),
                                    safeT("surprise.goodTitle","Good Surprise"),
                                    JOptionPane.INFORMATION_MESSAGE
                            );
                        } else {
                            bumpScore(-magnitude);
                            loseSharedLives(1);
                            JOptionPane.showMessageDialog(
                                    this,
                                    safeT("surprise.badPrefix","Bad surprise! 💀\n-") + magnitude + safeT("surprise.badSuffix"," points and -1 life."),
                                    safeT("surprise.badTitle","Bad Surprise"),
                                    JOptionPane.WARNING_MESSAGE
                            );
                        }

                        cell.setSpecialUsed(true);
                        updateButtonForCell(ownerIdx, cell);

                    }

                    break;
                }

                // FIRST CLICK → Reveal (this DOES change turn)
                if (!cell.isRevealed()) {
                    cell.reveal();
                    SoundManager.play(SoundManager.Sfx.REVEAL);
                    updateButtonForCell(ownerIdx, cell);
                    bumpRevealedForCurrentTurn();

                    if (!cell.isRevealScored()) {
                        bumpScore(1);
                        cell.setRevealScored(true);
                    }

                    usedTurn = true;
                }
            }

            default -> { /* nothing */ }
        }

        // ------------------------------------
        // SWITCH TURN IF THE MOVE WAS VALID
        // ------------------------------------
        if (usedTurn) {
            toggleTurnLabel();
        }

        // ------------------------------------
        // CHECK WIN CONDITION
        // ------------------------------------
        if (areBothBoardsCleared()) {

            SoundManager.play(SoundManager.Sfx.WIN); // 🔊 WIN SOUND

            String p1 = tfP1.getText().trim();
            String p2 = tfP2.getText().trim();

            endGame(
                safeT("dlg.boardCleared", "All boards cleared"),
                p1 + " & " + p2
            );

            if (fireworks != null) {
                fireworks.startFireworks();
            }
            return;
        }

    }



    private boolean areBothBoardsCleared() {
        return boards[0].isAllSafeCellsRevealed()
            && boards[1].isAllSafeCellsRevealed();
    }

    private void updateButtonForCell(int ownerIdx, Cell cell) {
        TileButton btn = buttons[ownerIdx][cell.getRow()][cell.getCol()];

        // base visual reset for revealed tiles
        btn.setIcon(null);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);

        btn.setText("");
        btn.setOverlayIcon(null);

        CellType type = cell.getType();
        int TILE = computeTileSize(currentDifficulty.rows, currentDifficulty.cols);
        int W = TILE;
        int H = TILE;

        boolean specialUsed = cell.isSpecialUsed();  // <--- key flag

        switch (type) {

            case MINE -> {
                // mine always shows mine icon when revealed
            	btn.setOverlayIcon(loadIconFit(A_MINE(), W, H));

            }

            case NUMBER -> {
                int num = cell.getAdjacentMines();
                btn.setText(String.valueOf(num));

                Color[] pal = {
                        new Color(52,152,219),
                        new Color(46,204,113),
                        new Color(231,76,60),
                        new Color(155,89,182),
                        new Color(230,126,34),
                        new Color(26,188,156),
                        new Color(52,73,94),
                        new Color(149,165,166)
                };
                btn.setForeground(pal[Math.min(Math.max(num - 1, 0), pal.length - 1)]);
            }

            case SURPRISE -> {
                if (specialUsed) {
                    // 🔒 USED surprise — text only, muted color
                    btn.setText(safeT("cell.used","USED"));
                    btn.setForeground(new Color(180, 180, 180));
                } else {
                    // normal (not used yet) surprise shows the spikes icon
                	btn.setOverlayIcon(loadIconFit(A_SPIKES(), W, H));

                }
            }

            case QUESTION -> {
                if (specialUsed) {
                    // 🔒 USED question — text only, slightly bluish
                    btn.setText(safeT("cell.used","USED"));
                    btn.setForeground(new Color(190, 200, 255));
                } else {
                    // normal (not used yet) question shows question icon
                	btn.setOverlayIcon(loadIconFit(A_QUESTION(), W, H));

                }
            }

            default -> {
                // EMPTY or any other type: nothing special to draw
            }
        }

        // Disable hover effect & run reveal animation
        btn.setRevealedVisual(true);
    }




    private void bumpRevealedForCurrentTurn() {
        int idx = p1Turn ? 0 : 1;
        revealedCount[idx]++;
        refreshRightStats();
    }

    /**
     * Ask a trivia question and apply points / lives according to:
     *  - Game difficulty (EASY / MEDIUM / HARD)
     *  - Question difficulty ("easy","medium","hard","pro")
     * as specified in the spec table.
     */
    private void showQuestionDialog(Question q) {
        if (q == null) {
            JOptionPane.showMessageDialog(this,
                    "No questions available.",
                    "Trivia Question",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String[] choices = new String[] {
                "A) " + q.getOptA(),
                "B) " + q.getOptB(),
                "C) " + q.getOptC(),
                "D) " + q.getOptD()
        };

        Object ans = JOptionPane.showInputDialog(
                this,
                q.getText(),
                "Trivia Question",
                JOptionPane.QUESTION_MESSAGE,
                null,
                choices,
                choices[0]
        );

        if (ans == null) return;

        char picked  = ans.toString().trim().charAt(0);
        boolean correct = Character.toUpperCase(picked) == Character.toUpperCase(q.getCorrect());

        // ------------------------
        // mapping from picture 2
        // ------------------------
        String qDiff = q.getDifficulty();
        if (qDiff == null) qDiff = "easy";
        qDiff = qDiff.trim().toLowerCase();

        int deltaPts  = 0;
        int deltaLife = 0;

        switch (currentDifficulty) {

            // ---------------- EASY GAME ----------------
            case EASY -> {
                switch (qDiff) {
                    case "easy" -> {
                        if (correct) {
                            deltaPts = 3;
                            deltaLife = 1;
                        } else {
                            // (-3pts) OR nothing
                            if (rng.nextBoolean()) deltaPts = -3;
                        }
                    }
                    case "medium" -> {
                        if (correct) {

                            deltaPts = 6;
                            flagRandomMine();
                        } else {
                            // (-6pts) OR nothing
                            if (rng.nextBoolean()) deltaPts = -6;
                        }
                    }
                    case "hard" -> {
                        if (correct) {
                            // 3x3 mine pattern +10pts – we only implement +10pts

                            openRandom3x3();    // ⭐ ADD THIS
                            // TODO: optionally place extra mines in 3x3 pattern
                        } else {
                            deltaPts = -10;
                        }
                    }
                    case "pro" -> {
                        if (correct) {
                            deltaPts = 15;
                            deltaLife = 2;
                        } else {
                            deltaPts = -15;
                            deltaLife = -1;
                        }
                    }
                }
            }

            // ---------------- MEDIUM GAME ----------------
            case MEDIUM -> {
                switch (qDiff) {
                    case "easy" -> {
                        if (correct) {
                            deltaPts = 8;
                            deltaLife = 1;
                        } else {
                            deltaPts = -8;
                        }
                    }
                    case "medium" -> {
                        if (correct) {
                            deltaPts = 10;
                            deltaLife = 1;
                        } else {
                            // ((-10pts) & (-1♥)) OR nothing
                            if (rng.nextBoolean()) {
                                deltaPts = -10;
                                deltaLife = -1;
                            }
                        }
                    }
                    case "hard" -> {
                        if (correct) {
                            deltaPts = 15;
                            deltaLife = 1;
                        } else {
                            deltaPts = -15;
                            deltaLife = -1;
                        }
                    }
                    case "pro" -> {
                        if (correct) {
                            deltaPts = 20;
                            deltaLife = 2;
                        } else {
                            // (-20pts & -1♥) OR (-20pts & -2♥)
                            deltaPts = -20;
                            deltaLife = rng.nextBoolean() ? -1 : -2;
                        }
                    }
                }
            }

            // ---------------- HARD GAME ----------------
            case HARD -> {
                switch (qDiff) {
                    case "easy" -> {
                        if (correct) {
                            deltaPts = 10;
                            deltaLife = 1;
                        } else {
                            deltaPts = -10;
                            deltaLife = -1;
                        }
                    }
                    case "medium" -> {
                        if (correct) {
                            // (+15pts & +1♥) OR (+15pts & +2♥)
                            deltaPts = 15;
                            deltaLife = rng.nextBoolean() ? 1 : 2;
                        } else {
                            deltaPts = -15;
                            deltaLife = -1;
                        }
                    }
                    case "hard" -> {
                        if (correct) {
                            deltaPts = 20;
                            deltaLife = 2;
                        } else {
                            deltaPts = -20;
                            deltaLife = -2;
                        }
                    }
                    case "pro" -> {
                        if (correct) {
                            deltaPts = 40;
                            deltaLife = 3;
                        } else {
                            deltaPts = -40;
                            deltaLife = -3;
                        }
                    }
                }
            }
        }

        // ---------------- apply the result ----------------
        if (correct) {
            String msg = safeT("trivia.correct","Correct answer!");
            if (deltaPts != 0)  msg += " " + (deltaPts > 0 ? "+" : "") + deltaPts + " pts.";
            if (deltaLife != 0) msg += " " + (deltaLife > 0 ? "+" : "") + deltaLife + " ♥.";
            JOptionPane.showMessageDialog(this, msg, safeT("dlg.triviaResult","Trivia Result"), JOptionPane.INFORMATION_MESSAGE);
        } else {
            String msg = "Wrong answer!";
            if (deltaPts != 0)  msg += " " + (deltaPts > 0 ? "+" : "") + deltaPts + " pts.";
            if (deltaLife != 0) msg += " " + (deltaLife > 0 ? "+" : "") + deltaLife + " ♥.";
            JOptionPane.showMessageDialog(this, msg, safeT("dlg.triviaResult","Trivia Result"), JOptionPane.WARNING_MESSAGE);
        }

        if (deltaPts != 0) {
            bumpScore(deltaPts);
        }
        if (deltaLife > 0) {
            gainSharedLives(deltaLife);
        } else if (deltaLife < 0) {
            loseSharedLives(-deltaLife);
        }
    }


    private void bumpScore(int delta) {
        sharedPoints += delta;

        // ❗ Ensure score never becomes negative
        if (sharedPoints < 0) {
            sharedPoints = 0;
        }

        updateSharedScoreLabel();
    }


    private void updateSharedScoreLabel() {
        sharedScoreLabel.setText(" " + sharedPoints);
    }

    /* ------------------------------ SHARED LIVES ------------------------------ */

    private void updateSharedHearts() {
        int limit = getMaxLivesLimit();

        for (int i = 0; i < MAX_LIVES; i++) {
            // hide hearts above configured max
            sharedHearts[i].setVisible(i < limit);

            if (i >= limit) continue;

            boolean full = (i < sharedLives);
            ImageIcon icon = loadIconFit(full ? A_HEART_FULL() : A_HEART_EMPTY(), 22, 22);
            if (icon != null && icon.getIconWidth() > 0) {
                sharedHearts[i].setIcon(icon);
                sharedHearts[i].setText(null);
            } else {
                sharedHearts[i].setIcon(null);
                sharedHearts[i].setText(full ? "♥" : "♡");
                sharedHearts[i].setForeground(full ? new Color(220, 70, 70) : new Color(220, 220, 220));
            }
        }
    }


    private int getMaxLivesLimit() {
        // respect Settings max lives, but never exceed MAX_LIVES (your UI array size)
        int limit = settingsController.getMaxSharedLives();
        return Math.max(1, Math.min(MAX_LIVES, limit));
    }


    private void resetSharedLives() {
        int limit = getMaxLivesLimit();
        sharedLives = Math.min(currentDifficulty.startLives, limit);
        updateSharedHearts();
    }



    private void loseSharedLives(int n) {
        if (n <= 0) return;

        sharedLives = Math.max(0, sharedLives - n);
        updateSharedHearts();

        if (sharedLives == 0) {
            SoundManager.play(SoundManager.Sfx.LOSE); // 🔊 LOSE SOUND
            endGame("Game Over (0 lives)", null);
            return;
        }

    }




    private void exportHistoryToCSV() {
        try {
            String path = getHistoryPath();
            if (path == null) {
                JOptionPane.showMessageDialog(this,
                        safeT("history.pathError",safeT("export.errNoPath", "Could not determine history file path.")),
                        safeT("history.exportError",safeT("export.errTitle", "Export Error")),
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            java.io.File file = new java.io.File(path);

            // Make sure the folder (history/ or src/history/) exists
            java.io.File parent = file.getParentFile();
            if (parent != null && !parent.exists()) {
                parent.mkdirs();
            }

            try (java.io.PrintWriter pw = new java.io.PrintWriter(file)) {
                // Header
                pw.println("Player,Result,Score,Difficulty,Date");

                // Rows
                for (String[] row : gameHistory) {
                    pw.println(String.join(",", row));
                }
            }

            JOptionPane.showMessageDialog(this,
                    "History exported successfully to:\n" + file.getAbsolutePath(),
                    safeT("history.exportComplete",safeT("export.okTitle", "Export Complete")),
                    JOptionPane.INFORMATION_MESSAGE);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    safeT("history.writeErrorPrefix","Error writing CSV: ") + ex.getMessage(),
                    safeT("history.exportError",safeT("export.errTitle", "Export Error")),
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private JButton createRestartIconButton() {

        // Load & scale refresh icon using YOUR asset system
        ImageIcon icon = loadIconFit(A_REFRESH(), 28, 28);

        JButton btn = new JButton(icon);

        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setContentAreaFilled(false);
        btn.setOpaque(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setToolTipText(safeT("restart.title",safeT("restart.title", "Restart Game")));

        btn.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	confirmRestartGame();});

        return btn;
    }




    private void confirmRestartGame() {
        int r = JOptionPane.showConfirmDialog(
                this,
                safeT("restart.confirm",safeT("restart.confirm", "Are you sure you want to restart the game?Current progress will be lost.")),
                safeT("restart.title",safeT("restart.title", "Restart Game")),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE
        );

        if (r == JOptionPane.YES_OPTION) {
            restartGame();
        }
    }

    private void restartGame() {

        // ✅ APPLY X CHANGE:
        // Restart should respect Settings max lives AND not allow MAX_LIVES overflow.
        // Also keep the current difficulty selected (so restart doesn't accidentally switch difficulty).
        cbDifficulty.setSelectedIndex(switch (currentDifficulty) {
            case EASY -> 0;
            case MEDIUM -> 1;
            case HARD -> 2;
        });

        // reset shared stats
        sharedPoints = 0;

        // lives back to current configured limit
        sharedLives = getMaxLivesLimit();

        // reset turn to Player 1
        p1Turn = true;

        // reset counters if you have them (only keep the ones that exist!)
        if (flagsCount != null && flagsCount.length >= 2) {
            flagsCount[0] = 0;
            flagsCount[1] = 0;
        }
        if (revealedCount != null && revealedCount.length >= 2) {
            revealedCount[0] = 0;
            revealedCount[1] = 0;
        }

        // update UI labels you DO have
        updateSharedScoreLabel();
        updateSharedHearts();
        refreshRightStats();

        // IMPORTANT: reuse your existing init logic so listeners work
        startGame();
    }






    /** Small helper for themed buttons */
    private void styleThemedButton(JButton b, Color bg, Color fg) {
        b.setBackground(bg);
        b.setForeground(fg);
        b.setFocusPainted(false);
        b.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(255, 255, 255, 80), 1),
                BorderFactory.createEmptyBorder(10, 18, 10, 18)
        ));
    }


    // --- helpers (add inside MineSweeperPrototype class) ---
    private static String safe(String[] a, int idx) {
        if (a == null || idx < 0 || idx >= a.length || a[idx] == null) return "";
        return a[idx];
    }

    private String localizeDifficulty(String raw) {
        if (raw == null) return "";
        String u = raw.trim().toUpperCase();
        // Supports values like "EASY" / "MEDIUM" / "HARD"
        if (u.contains("EASY"))   return safeT("diff.easy", "Easy");
        if (u.contains("MEDIUM")) return safeT("diff.medium", "Medium");
        if (u.contains("HARD"))   return safeT("diff.hard", "Hard");
        return raw;
    }

    private String formatDateForUI(String raw) {
        // If it's already nice, keep it.
        // If your date is ISO like 2026-01-07T15:25:13..., trim milliseconds.
        if (raw == null) return "";
        int dot = raw.indexOf('.');
        if (dot > 0) return raw.substring(0, dot);
        return raw;
    }


    private void gainSharedLives(int n) {
        if (n <= 0) return;

        // ✅ APPLY X CHANGE:
        // Respect Settings max lives limit (not MAX_LIVES), and convert overflow to points.
        int limit = getMaxLivesLimit();

        int before = sharedLives;
        int target = before + n;

        if (target <= limit) {
            sharedLives = target;
            updateSharedHearts();
            return;
        }

        // overflow beyond limit becomes points
        int overflow = target - limit;

        int perLife = LIFE_OVERFLOW_POINTS[Math.max(0, Math.min(difficultyIdx, LIFE_OVERFLOW_POINTS.length - 1))];
        if (overflow > 0) {
            bumpScore(overflow * perLife);
        }

        sharedLives = limit;
        updateSharedHearts();
    }

    private void flagRandomMine() {
        int currentPlayer = p1Turn ? 0 : 1;

        // scan current player's board
        Board board = boards[currentPlayer];

        java.util.List<Cell> candidates = new java.util.ArrayList<>();

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (cell.getType() == CellType.MINE && !cell.isFlagged()) {
                    candidates.add(cell);
                }
            }
        }

        if (candidates.isEmpty()) return;

        Cell chosen = candidates.get(rng.nextInt(candidates.size()));
        chosen.toggleFlag();

        TileButton btn = buttons[currentPlayer][chosen.getRow()][chosen.getCol()];
        int W = btn.getPreferredSize().width;
        int H = btn.getPreferredSize().height;
        btn.setOverlayIcon(loadIconFit(A_FLAG(), W, H));


        flagsCount[currentPlayer]++;
        refreshRightStats();
    }

    private void openRandom3x3() {
        int currentPlayer = p1Turn ? 0 : 1;
        Board board = boards[currentPlayer];

        java.util.List<Cell> safeCells = new java.util.ArrayList<>();

        for (int r = 0; r < board.getRows(); r++) {
            for (int c = 0; c < board.getCols(); c++) {
                Cell cell = board.getCell(r, c);
                if (!cell.isRevealed() && cell.getType() != CellType.MINE) {
                    safeCells.add(cell);
                }
            }
        }

        if (safeCells.isEmpty()) return;

        Cell center = safeCells.get(rng.nextInt(safeCells.size()));

        for (int dr = -1; dr <= 1; dr++) {
            for (int dc = -1; dc <= 1; dc++) {
                int nr = center.getRow() + dr;
                int nc = center.getCol() + dc;

                if (nr < 0 || nc < 0 || nr >= board.getRows() || nc >= board.getCols())
                    continue;

                Cell c = board.getCell(nr, nc);
                if (!c.isRevealed() && c.getType() != CellType.MINE) {
                    c.reveal();
                    updateButtonForCell(currentPlayer, c);
                    bumpRevealedForCurrentTurn();

                    if (!c.isRevealScored()) {
                        bumpScore(1);
                        c.setRevealScored(true);
                    }
                }
            }
        }
    }

    private void showHistory() {
        if (gameHistory == null || gameHistory.isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    safeT("history.empty", "No games played yet."),
                    safeT("history.title", "Game History"),
                    JOptionPane.INFORMATION_MESSAGE
            );
            return;
        }

        // ===== Theme colors (Option A) =====
        ThemePalette pal = ThemePalette.of(SysData.getTheme());

        // ===== Columns (localized) =====
        String[] cols = new String[] {
                safeT("history.col.player", "Player"),
                safeT("history.col.result", "Result"),
                safeT("history.col.score", "Score"),
                safeT("history.col.difficulty", "Difficulty"),
                safeT("history.col.date", "Date")
        };

        // ===== Data =====
        Object[][] data = new Object[gameHistory.size()][5];
        for (int i = 0; i < gameHistory.size(); i++) {
            String[] r = gameHistory.get(i);
            data[i][0] = safe(r, 0);
            data[i][1] = safe(r, 1);
            data[i][2] = safe(r, 2);
            data[i][3] = localizeDifficulty(safe(r, 3));
            data[i][4] = formatDateForUI(safe(r, 4));
        }

        DefaultTableModel model = new DefaultTableModel(data, cols) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };

        JTable table = new JTable(model);
        styleTableLikeQuestionsFrame(table); // keep your existing styling (works fine)
        table.setFillsViewportHeight(true);

        JScrollPane sp = new JScrollPane(table);
        sp.setBorder(BorderFactory.createEmptyBorder());
        sp.setOpaque(false);
        sp.getViewport().setOpaque(false);

        // ===== Dialog (transparent) =====
        JDialog dlg = new JDialog(this, safeT("history.title", "Game History"), true);
        dlg.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        dlg.setUndecorated(true);
        dlg.setBackground(new Color(0, 0, 0, 0));

        Rectangle bounds = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
        dlg.setBounds(bounds);
        dlg.setLocationRelativeTo(this);

        // ===== Transparent background container =====
        JPanel bg = new JPanel(new GridBagLayout());
        bg.setOpaque(false);

        // ===== Frosted card (theme-aware) =====
        JPanel glass = new FrostedCardPanel(pal);
        glass.setLayout(new BorderLayout(18, 18));
        glass.setBorder(new javax.swing.border.EmptyBorder(18, 18, 18, 18));

        Dimension cardSize = new Dimension(1200, 760);
        glass.setPreferredSize(cardSize);
        glass.setMinimumSize(cardSize);
        glass.setMaximumSize(cardSize);

        JLabel title = new JLabel(safeT("history.title", "Game History"), SwingConstants.CENTER);
        title.setFont(new Font("Georgia", Font.BOLD, 28));
        title.setForeground(new Color(210, 255, 235));
        glass.add(title, BorderLayout.NORTH);

        glass.add(sp, BorderLayout.CENTER);

        // ===== Bottom buttons (Option A) =====
        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 8));
        bottom.setOpaque(false);

        JButton exportBtn = new PillButton(safeT("history.export", "Export CSV"), pal.primary);
        JButton closeBtn  = new PillButton(safeT("btn.close", "Back"), pal.secondary);
        closeBtn.setPreferredSize(new Dimension(260, 52));

        exportBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            exportHistoryToCSV();
        });

        closeBtn.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            dlg.dispose();
        });

        bottom.add(exportBtn);
        bottom.add(closeBtn);
        glass.add(bottom, BorderLayout.SOUTH);

        // ===== Center the card =====
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 1;
        gbc.weighty = 1;
        bg.add(glass, gbc);

        dlg.setContentPane(bg);
        SysData.applyGlobalFont(dlg);

        dlg.setVisible(true);
    }



    private void styleTableLikeQuestionsFrame(JTable t) {
        t.setRowHeight(34);
        t.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        t.setForeground(new Color(235, 255, 245));
        t.setBackground(new Color(10, 15, 15, 180));
        t.setSelectionBackground(new Color(60, 90, 80));
        t.setSelectionForeground(Color.WHITE);
        t.setShowGrid(false);
        t.setFillsViewportHeight(true);

        JTableHeader header = t.getTableHeader();
        header.setDefaultRenderer((tbl, value, isSelected, hasFocus, row, col) -> {
            JLabel l = new JLabel(value == null ? "" : value.toString());
            l.setOpaque(true);
            boolean he = (SysData.getI18n() != null && SysData.getI18n().isHebrew());
            l.setFont(new Font(he ? "SansSerif" : "Georgia", Font.BOLD, 15));
            l.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
            l.setBackground(new Color(35, 60, 45));
            l.setForeground(new Color(245, 255, 250));
            return l;
        });
        header.setPreferredSize(new Dimension(header.getPreferredSize().width, 38));
    }

    private JButton pillButtonLikeQuestionsFrame(String text, Color bg) {
        JButton base = new JButton(text);
        base.setFont(new Font("Georgia", Font.BOLD, 16));
        base.setForeground(Color.WHITE);
        base.setFocusPainted(false);
        base.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        base.setBorderPainted(false);
        base.setContentAreaFilled(false);
        base.setOpaque(false);
        base.setPreferredSize(new Dimension(190, 52));

        base.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        base.setBorder(new javax.swing.border.EmptyBorder(10, 18, 10, 18));

        PaintedButton painted = new PaintedButton(base, bg);
        painted.addChangeListener(e -> painted.repaint());

        return painted;
    }

    private static class BackgroundImagePanel extends JPanel {
        private final Image img;
        public BackgroundImagePanel(String path) {
            ImageIcon ic = new ImageIcon(path);
            this.img = ic.getImage();
            setOpaque(true);
        }
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (img != null) g.drawImage(img, 0, 0, getWidth(), getHeight(), this);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setColor(new Color(0, 0, 0, 110));
            g2.fillRect(0, 0, getWidth(), getHeight());
            g2.dispose();
        }
    }

    private static class FrostedCardPanel extends JPanel {
        private final ThemePalette pal; // can be null (fallback)

        public FrostedCardPanel() {
            this.pal = null;
            setOpaque(false);
        }

        public FrostedCardPanel(ThemePalette pal) {
            this.pal = pal;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            // ✅ if a theme palette is provided → use it
            if (pal != null) {
                g2.setColor(pal.cardBg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);

                g2.setColor(pal.stroke);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 24, 24);
            }
            // ✅ otherwise fallback to your original hardcoded colors
            else {
                g2.setColor(new Color(15, 25, 20, 200));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);

                g2.setColor(new Color(170, 255, 255, 120));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 24, 24);
            }

            g2.dispose();
        }
    }


    private static class PaintedButton extends JButton {
        private final Color base;
        public PaintedButton(JButton delegate, Color base) {
            super(delegate.getText());
            this.base = base;
            setFont(delegate.getFont());
            setForeground(delegate.getForeground());
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setCursor(delegate.getCursor());
            setPreferredSize(delegate.getPreferredSize());
            setBorder(delegate.getBorder());
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            Color bg = base;
            if (getModel().isPressed()) bg = bg.darker();
            else if (getModel().isRollover()) bg = bg.brighter();

            g2.setColor(bg);
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

            g2.setColor(new Color(255, 255, 255, 70));
            g2.setStroke(new BasicStroke(2f));
            g2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 18, 18);

            g2.dispose();
            super.paintComponent(g);
        }
    }
    
 // ✅ checks if a player "discovered all mines" on their own board
 // (treat flagged mine as discovered because it's marked by the player)
 private boolean isAllMinesDiscovered(int ownerIdx) {
     Board b = boards[ownerIdx];
     for (int r = 0; r < b.getRows(); r++) {
         for (int c = 0; c < b.getCols(); c++) {
             Cell cell = b.getCell(r, c);
             if (cell.getType() == CellType.MINE) {
                 if (!(cell.isRevealed() || cell.isFlagged())) {
                     return false;
                 }
             }
         }
     }
     return true;
 }

 // ✅ reveal everything on both boards (ignores flags / turn rules)
 private void revealAllBoards() {
     for (int p = 0; p < 2; p++) {
         Board b = boards[p];
         for (int r = 0; r < b.getRows(); r++) {
             for (int c = 0; c < b.getCols(); c++) {
                 Cell cell = b.getCell(r, c);
                 if (!cell.isRevealed()) {
                     cell.reveal();
                 }
                 updateButtonForCell(p, cell);
             }
         }
     }

     // optional: disable all buttons so no more interaction after game ends
     for (int p = 0; p < 2; p++) {
         for (int r = 0; r < buttons[p].length; r++) {
             for (int c = 0; c < buttons[p][r].length; c++) {
                 if (buttons[p][r][c] != null) buttons[p][r][c].setEnabled(false);
             }
         }
     }
 }

 // ✅ convert remaining lives to points: each heart = activation cost (by difficulty)
 private int convertRemainingLivesToPoints() {
	    if (sharedLives <= 0) return 0;

	    int valuePerHeart = getQuestionActivationCost(); // EASY=5, MEDIUM=8, HARD=12
	    int bonus = sharedLives * valuePerHeart;

	    bumpScore(bonus);
	    sharedLives = 0;
	    updateSharedHearts();

	    return bonus;   // ✅ מחזירים כמה נקודות נוספו
	}


 // ✅ one place to finish a game properly
 private void endGame(String resultText, String winnerNameOrNull) {
	    if (gameEnded) return;
	    gameEnded = true;
	    gameInProgress = false;

	    // 1) convert lives -> points (keep bonus for message)
	    int bonusFromHearts = convertRemainingLivesToPoints();

	    // 2) reveal boards
	    revealAllBoards();

	    // 3) history
	    String p1 = tfP1.getText().trim();
	    String p2 = tfP2.getText().trim();
	    String who = (winnerNameOrNull != null) ? winnerNameOrNull : (p1 + " & " + p2);

	    gameHistory.add(new String[] {
	            who,
	            resultText,
	            String.valueOf(sharedPoints),
	            currentDifficulty.name(),
	            String.valueOf(java.time.LocalDateTime.now())
	    });

	    if (settingsController.isAutoSaveHistory()) exportHistoryToCSV();

	    // 4) message (clear & consistent)
	    boolean isWin = (winnerNameOrNull != null);

	    String title = safeT("dlg.gameEnded", "Game Ended");
	    String header = isWin
	            ? safeT("dlg.winTitle", "🎉 Winner!") + " " + winnerNameOrNull
	            : safeT("dlg.loseTitle", "💀 Game Over");

	    String reasonLine = safeT("dlg.reasonPrefix", "Result: ") + resultText;
	    String scoreLine  = safeT("msg.finalScorePrefix", "Final Score: ") + sharedPoints;

	    String bonusLine = (bonusFromHearts > 0)
	            ? safeT("msg.bonusFromHeartsPrefix", "Bonus from remaining hearts: +") + bonusFromHearts
	            : safeT("msg.bonusFromHeartsNone", "Bonus from remaining hearts: +0");

	    String askLine = safeT("msg.startNewGameQ", "Start a new game?");

	    String message =
	            header + "\n\n" +
	            reasonLine + "\n" +
	            bonusLine + "\n" +
	            scoreLine + "\n\n" +
	            askLine;

	    int choice = JOptionPane.showConfirmDialog(
	            this,
	            message,
	            title,
	            JOptionPane.YES_NO_OPTION,
	            JOptionPane.INFORMATION_MESSAGE
	    );

	    if (choice == JOptionPane.YES_OPTION) {
	        gameEnded = false;
	        startGame();
	    }
	}





    /* ------------------------------ MAIN ------------------------------ */
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            MineSweeperPrototype app = new MineSweeperPrototype();
            app.setVisible(true);
        });
    }
}
