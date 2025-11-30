	package View;
	
	import Controller.GameController;
	import Model.Board;
	import Model.Cell;
	import Model.CellType;
	import Model.Difficulty;
	import Model.Question;
	import Model.SysData;
	
	import javax.swing.*;
	import java.awt.*;
	import java.awt.event.*;
	import java.util.List;
	
	/**
	 * MineSweeper + Trivia — Forest Edition (Dark Wood + Moss Glow Theme)
	 * NOTE: GAME LOGIC IS UNCHANGED – only the visual design is updated.
	 */
	public class MineSweeperPrototype extends JFrame {
	
		
		
	    /* ------------------------------ FOREST ASSETS ------------------------------ */
	    private static final String A_BG          = "src/assets/forest/forest_bg_1920x1080.png";
	    private static final String A_GRASS       = "src/assets/forest/tile_grass.png";
	    private static final String A_GRASS_H     = "src/assets/forest/tile_grass_hover.png";
	    private static final String A_DIRT        = "src/assets/forest/tile_dirt.png";
	    private static final String A_FLAG        = "src/assets/forest/icon_flag.png";
	    private static final String A_MINE        = "src/assets/forest/icon_mine.png";
	    private static final String A_SPIKES      = "src/assets/forest/mushroom_surprise.png";
	    private static final String A_HEART_FULL  = "src/assets/forest/heart_full.png";
	    private static final String A_HEART_EMPTY = "src/assets/forest/heart_empty.png";
	    private static final String A_BROWN       = "src/assets/forest/tile_brown.png";
	    private static final String A_QUESTION = "src/assets/forest/question.png";
	    private static final Color GRID_LINE = new Color(212,175,55,140);
	
	
	    private static final int TILE_SIZE = 44;
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
	
	    private static final String SCREEN_MENU     = "MENU";
	    private static final String SCREEN_NEW_GAME = "NEW_GAME";
	    private static final String SCREEN_GAME     = "GAME";
	
	    private final JLabel turnLabel        = new JLabel("Turn: Player 1");
	    private final JLabel sharedScoreLabel = new JLabel("Score: 0");
	    private final JLabel rightStats       = new JLabel();   // "<Player> • Revealed: X | Flags: Y"
	    private int sharedPoints = 0;
	
	    private final JTextField tfP1 = new JTextField("Alice", 14);
	    private final JTextField tfP2 = new JTextField("Bob", 14);
	    private final JComboBox<String> cbDifficulty =
	            new JComboBox<>(new String[]{"Easy (9x9)", "Medium (13x13)", "Hard (16x16)"});
	
	    private JPanel gamePanel;
	
	    // Shared lives (both players)
	    private static final int MAX_LIVES = 10;
	    private int sharedLives = 0;
	    private final JLabel[] sharedHearts = new JLabel[MAX_LIVES];
	
	    // whose turn?
	    private boolean p1Turn = true;
	
	    // Difficulty index (0=Easy,1=Medium,2=Hard) - kept for LIFE_OVERFLOW_POINTS usage
	    private int difficultyIdx = 0;
	
	    // Current difficulty (drives rows/cols + startLives)
	    private Difficulty currentDifficulty = Difficulty.EASY;
	
	    // Overflow conversion (points per extra life) per difficulty
	    private static final int[] LIFE_OVERFLOW_POINTS = {1, 2, 3};
	
	    // per-player counters
	    private final int[] flagsCount    = {0, 0};
	    private final int[] revealedCount = {0, 0};
	
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
	        super("MineSweeper + Trivia — Forest Edition");
	
	        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignore) {}
	
	        setDefaultCloseOperation(EXIT_ON_CLOSE);
	        setMinimumSize(new Dimension(1200, 800));
	        setLocationRelativeTo(null);
	        setExtendedState(JFrame.MAXIMIZED_BOTH);
	
	        // Load questions from CSV (used for question cells)
	        SysData.init();
	        loadHistoryFromCSV();

	
	        // Default difficulty / boards
	        currentDifficulty = Difficulty.EASY;
	        boards[0] = new Board(currentDifficulty);
	        boards[1] = new Board(currentDifficulty);
	
	        // Build screens
	        root.setOpaque(false);
	        root.add(buildMenu(), SCREEN_MENU);
	        root.add(buildNewGame(), SCREEN_NEW_GAME);
	
	        // default game board (easy) – will be rebuilt when "Start Game" is pressed
	        gamePanel = buildGame(currentDifficulty.rows, currentDifficulty.cols);
	        root.add(gamePanel, SCREEN_GAME);
	
	        BackgroundPanel forest = new BackgroundPanel(A_BG);
	        forest.setLayout(new BorderLayout());
	        forest.add(root, BorderLayout.CENTER);
	        setContentPane(forest);
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
	        lbl.setFont(new Font("Georgia", Font.BOLD, 24));
	        lbl.setForeground(MOSS_GLOW);
	
	        header.add(lbl);
	        return header;
	    }
	    
	    /* ------------------------------ FROSTED BLUE AURA (Turn Glow) ------------------------------ */
	    class TurnGlowPanel extends JPanel {

	        private float phase = 0f;
	        private final Timer timer;
	        private boolean active = false;

	        TurnGlowPanel() {
	            setOpaque(false);

	            timer = new Timer(40, e -> {
	                if (active) {
	                    phase += 0.07f;
	                    repaint();
	                }
	            });
	            timer.start();
	        }

	        void setActive(boolean isActive) {
	            active = isActive;
	            repaint();
	        }

	        @Override
	        protected void paintComponent(Graphics g) {
	            if (!active) return;

	            Graphics2D g2 = (Graphics2D) g.create();
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

	            float glow = 0.4f + (float)Math.sin(phase) * 0.4f;
	            int alpha = (int)(160 * glow);

	            Color icy = new Color(150, 255, 255, alpha);

	            g2.setStroke(new BasicStroke(6f));
	            g2.setColor(icy);
	            g2.drawRoundRect(3, 3, getWidth() - 6, getHeight() - 6, 28, 28);

	            g2.dispose();
	        }
	    }
	    class DimPanel extends JPanel {

	        private float alpha = 1f;

	        DimPanel() {
	            setOpaque(false);
	        }

	        void setDim(float value) {
	            alpha = value;
	            repaint();
	        }

	        @Override
	        protected void paintComponent(Graphics g) {
	            if (alpha >= 0.99f) return;

	            Graphics2D g2 = (Graphics2D) g.create();
	            g2.setColor(new Color(0, 0, 0, (int)(140 * (1f - alpha))));
	            g2.fillRect(0, 0, getWidth(), getHeight());
	            g2.dispose();
	        }
	    }


	
	    
	    private void loadHistoryFromCSV() {
	        java.io.File file = new java.io.File("game_history.csv");

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
	        b.setFont(new Font("Georgia", Font.BOLD, 16));
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
	
	
	
	
	
	    /* ------------------------------ MENU SCREEN ------------------------------ */
	
	    private JPanel buildMenu() {
	        JPanel page = new JPanel(new BorderLayout());
	        page.setOpaque(false);
	
	        // ❄️ SNOW behind menu
	        SnowPanel snow = new SnowPanel();
	        snow.setLayout(new GridBagLayout());
	
	        // MAIN GLASS CARD
	        JPanel glass = new JPanel() {
	        	@Override
	        	protected void paintComponent(Graphics g) {
	        	    Float alpha = (Float) getClientProperty("fadeAlpha");
	        	    float a = (alpha == null ? 1f : alpha);
	
	        	    Graphics2D g2 = (Graphics2D) g.create();
	        	    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
	
	        	    // Your frosted drawing
	        	    Color frost = new Color(20, 35, 35, 170);
	        	    g2.setColor(frost);
	        	    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
	
	        	    g2.setColor(new Color(160, 255, 255, 130));
	        	    g2.setStroke(new BasicStroke(3f));
	        	    g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 30, 30);
	
	        	    g2.dispose();
	        	    super.paintComponent(g);
	        	}
	
	        };
	        glass.setOpaque(false);
	        glass.setPreferredSize(new Dimension(520, 380));
	        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));
	        glass.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
	
	        // TITLE
	        JLabel title = new JLabel("MINESWEEPER", SwingConstants.CENTER);
	        title.setAlignmentX(Component.CENTER_ALIGNMENT);
	        title.setFont(new Font("Georgia", Font.BOLD, 48));
	        title.setForeground(new Color(190, 255, 220));   // icy mint glow
	
	        JLabel subtitle = new JLabel("+ Trivia — Forest Edition", SwingConstants.CENTER);
	        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
	        subtitle.setFont(new Font("Georgia", Font.PLAIN, 22));
	        subtitle.setForeground(new Color(170, 220, 200));
	
	        glass.add(title);
	        glass.add(Box.createVerticalStrut(8));
	        glass.add(subtitle);
	        glass.add(Box.createVerticalStrut(45));
	
	        // FROSTED BUTTON STYLE
	        JButton newGame = createFrostedButton("New Game");
	        JButton exit = createFrostedButton("Exit");
	
	        newGame.addActionListener(e -> cards.show(root, SCREEN_NEW_GAME));
	        exit.addActionListener(e -> {
	            int r = JOptionPane.showConfirmDialog(this,
	                    "Exit the game?", "Confirm",
	                    JOptionPane.YES_NO_OPTION);
	            if (r == JOptionPane.YES_OPTION) System.exit(0);
	        });
	
	        glass.add(newGame);
	        glass.add(Box.createVerticalStrut(18));
	        glass.add(exit);
	
	        // ⭐ Christmas lights above
	        LightsOverlay lights = new LightsOverlay();
	
	        // STACKING LAYERS
	        JPanel centerLayer = new JPanel(new BorderLayout());
	        centerLayer.setOpaque(false);
	        centerLayer.add(glass, BorderLayout.CENTER);
	        centerLayer.add(lights, BorderLayout.NORTH);
	
	        snow.add(centerLayer);   // snow behind the menu
	
	        page.add(snow, BorderLayout.CENTER);
	
	        JComponent faded = wrapWithFade(glass);
	        snow.removeAll();
	        snow.add(faded);   // menu panel now fades in
	        snow.add(centerLayer);
	
	
	        return page;
	    }
	    private JButton createFrostedButton(String text) {
	        JButton b = new JButton(text) {
	            @Override
	            protected void paintComponent(Graphics g) {
	                Graphics2D g2 = (Graphics2D) g;
	                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
	                Color base = new Color(45, 30, 20, 200);
	                Color hover = new Color(60, 45, 30, 220);
	
	                Color bg = getModel().isRollover() ? hover : base;
	                g2.setColor(bg);
	                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
	
	                // icy glow border
	                g2.setColor(new Color(160, 255, 255, getModel().isRollover() ? 180 : 120));
	                g2.setStroke(new BasicStroke(2f));
	                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 16, 16);
	
	                super.paintComponent(g);
	            }
	        };
	
	        b.setForeground(new Color(200, 255, 230));
	        b.setFont(new Font("Georgia", Font.BOLD, 18));
	        b.setFocusPainted(false);
	        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
	        b.setBorderPainted(false);
	        b.setContentAreaFilled(false);
	        b.setOpaque(false);
	        b.setPreferredSize(new Dimension(200, 48));
	
	        return b;
	    }
	    
	
	
	    /* ------------------------------ NEW GAME SCREEN ------------------------------ */
	
	    private JPanel buildNewGame() {
	
	        // OUTER PAGE (transparent)
	        JPanel page = new JPanel(new BorderLayout());
	        page.setOpaque(false);
	
	        // ❄ Snow behind the setup card
	        SnowPanel snow = new SnowPanel();
	        snow.setLayout(new GridBagLayout());
	
	        // 🎄 Christmas lights across top
	        LightsOverlay lights = new LightsOverlay();
	
	        // 🧊 Frosted glass card (same style as menu)
	        JPanel glass = new JPanel() {
	            @Override
	            protected void paintComponent(Graphics g) {
	                Float alpha = (Float) getClientProperty("fadeAlpha");
	                float a = (alpha == null ? 1f : alpha);
	
	                Graphics2D g2 = (Graphics2D) g.create();
	                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, a));
	
	                // Frost panel background
	                g2.setColor(new Color(20, 35, 35, 170));
	                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 35, 35);
	
	                // Frost border
	                g2.setColor(new Color(160, 255, 255, 130));
	                g2.setStroke(new BasicStroke(3f));
	                g2.drawRoundRect(2, 2, getWidth() - 4, getHeight() - 4, 30, 30);
	
	                g2.dispose();
	                super.paintComponent(g);
	            }
	        };
	
	        glass.setOpaque(false);
	        glass.setPreferredSize(new Dimension(520, 420));
	        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));
	        glass.setBorder(BorderFactory.createEmptyBorder(30, 40, 30, 40));
	
	        // ⭐ TITLE
	        JLabel title = new JLabel("NEW GAME SETUP", SwingConstants.CENTER);
	        title.setAlignmentX(Component.CENTER_ALIGNMENT);
	        title.setFont(new Font("Georgia", Font.BOLD, 38));
	        title.setForeground(new Color(190, 255, 220));
	
	        glass.add(title);
	        glass.add(Box.createVerticalStrut(25));
	
	        // -------------------------------
	        // FORM (same fields but prettier)
	        // -------------------------------
	        JPanel form = new JPanel(new GridBagLayout());
	        form.setOpaque(false);
	        GridBagConstraints gc = new GridBagConstraints();
	        gc.insets = new Insets(12, 12, 12, 12);
	        gc.fill = GridBagConstraints.HORIZONTAL;
	
	        JLabel l1 = new JLabel("Player 1 Name:");
	        JLabel l2 = new JLabel("Player 2 Name:");
	        JLabel l3 = new JLabel("Difficulty:");
	
	        for (JLabel l : new JLabel[]{l1, l2, l3}) {
	            l.setFont(new Font("Georgia", Font.BOLD, 17));
	            l.setForeground(new Color(225, 245, 240));
	        }
	
	        styleField(tfP1);
	        styleField(tfP2);
	
	        cbDifficulty.setFont(new Font("Georgia", Font.PLAIN, 15));
	        cbDifficulty.setBackground(new Color(50, 40, 28));
	        cbDifficulty.setForeground(new Color(240, 235, 220));
	        cbDifficulty.setBorder(BorderFactory.createLineBorder(new Color(90, 65, 35), 2, true));
	
	        // Layout
	        gc.gridx = 0; gc.gridy = 0; gc.anchor = GridBagConstraints.LINE_END;
	        form.add(l1, gc);
	        gc.gridx = 1; gc.anchor = GridBagConstraints.LINE_START;
	        form.add(tfP1, gc);
	
	        gc.gridx = 0; gc.gridy = 1; gc.anchor = GridBagConstraints.LINE_END;
	        form.add(l2, gc);
	        gc.gridx = 1; gc.anchor = GridBagConstraints.LINE_START;
	        form.add(tfP2, gc);
	
	        gc.gridx = 0; gc.gridy = 2; gc.anchor = GridBagConstraints.LINE_END;
	        form.add(l3, gc);
	        gc.gridx = 1; gc.anchor = GridBagConstraints.LINE_START;
	        form.add(cbDifficulty, gc);
	
	        // Buttons
	        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
	        actions.setOpaque(false);
	
	        JButton back  = createFrostedButton("Back");
	        JButton start = createFrostedButton("Start Game");
	
	        back.addActionListener(e -> cards.show(root, SCREEN_MENU));
	        start.addActionListener(e -> startGame());
	
	        actions.add(start);
	        actions.add(back);
	
	        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
	        form.add(actions, gc);
	
	        glass.add(form);
	
	        // Stack snow + glass card + lights
	        JPanel stack = new JPanel(new BorderLayout());
	        stack.setOpaque(false);
	        stack.add(lights, BorderLayout.NORTH);
	        stack.add(glass, BorderLayout.CENTER);
	
	        snow.add(stack);
	
	        // Apply fade animation
	        return wrapWithSlideFade(snow);
	    }
	
	
	    private void styleField(JTextField tf) {
	        tf.setFont(new Font("Georgia", Font.PLAIN, 14));
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
	
	        difficultyIdx = cbDifficulty.getSelectedIndex();
	        switch (difficultyIdx) {
	            case 0 -> currentDifficulty = Difficulty.EASY;
	            case 1 -> currentDifficulty = Difficulty.MEDIUM;
	            case 2 -> currentDifficulty = Difficulty.HARD;
	            default -> currentDifficulty = Difficulty.EASY;
	        }
	
	        int rows = currentDifficulty.rows;
	        int cols = currentDifficulty.cols;
	
	        boards[0] = new Board(currentDifficulty);
	        boards[1] = new Board(currentDifficulty);
	
	        flagsCount[0] = flagsCount[1] = 0;
	        revealedCount[0] = revealedCount[1] = 0;
	
	        root.remove(gamePanel);
	        gamePanel = buildGame(rows, cols);
	        root.add(wrapWithSlideFade(gamePanel), SCREEN_GAME);
	
	
	        sharedPoints = 0;
	        updateSharedScoreLabel();
	        resetSharedLives();
	
	        p1Turn = true;
	        turnLabel.setText("Turn: " + p1);
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
	            loadIconFit(A_GRASS, TILE, TILE),
	            loadIconFit(A_GRASS_H, TILE, TILE)
	        );
	        TileSet cedar = new TileSet(
	            loadIconFit(A_BROWN, TILE, TILE),
	            loadIconFit(A_BROWN, TILE, TILE)
	        );

	
	        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 30, 20));
	        boardsPanel.setOpaque(false);
	
	        String p1Name = tfP1.getText().trim();
	        String p2Name = tfP2.getText().trim();

	        boardsPanel.add(boardCard(p1Name + "'s Board", rows, cols, moss, 0));
	        boardsPanel.add(boardCard(p2Name + "'s Board", rows, cols, cedar, 1));

	
	        // add boards to frosted card
	        glassBoard.add(boardsPanel, BorderLayout.CENTER);
	
	        // add lights on top
	        JPanel layered = new JPanel(new BorderLayout());
	        layered.setOpaque(false);
	        layered.add(lights, BorderLayout.NORTH);
	        layered.add(glassBoard, BorderLayout.CENTER);
	
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
	        lbl.setFont(new Font("Georgia", Font.BOLD, 18));
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
	
	                flag.addActionListener(ev -> toggleFlag(ownerIdx, rr, cc));
	
	                // REAL logic: call handleCellClick using underlying Board & Cell
	                cellButton.addActionListener(e -> handleCellClick(ownerIdx, rr, cc));
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
	        boolean flagged = cell.isFlagged();

	        TileButton cellButton = buttons[ownerIdx][row][col];
	        int W = cellButton.getPreferredSize().width;
	        int H = cellButton.getPreferredSize().height;
	        cellButton.setOverlayIcon(flagged ? loadIconFit(A_FLAG, W/2, H/2) : null);

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

	        // --------------------------------------------------------------------
	        // 🔄 NEW: FLAGGING uses your turn (just like a reveal)
	        // Only apply turn switch if a flag was PLACED (not removed)
	        // --------------------------------------------------------------------
	        if (flagged) {
	            toggleTurnLabel();
	        }
	    }


	
	
	    /* ------------------------------ WIDGETS ------------------------------ */
	
	    /* ------------------------------ WIDGETS ------------------------------ */
	
	 // ❄️ SNOW PANEL — multi-layer, wind, sparkles
	    class SnowPanel extends JPanel {
	
	        private static class Snowflake {
	            float x, y, speed, drift;
	            float size;
	            float opacity;
	            boolean sparkle;
	        }
	
	        private final java.util.List<Snowflake> flakes = new java.util.ArrayList<>();
	        private final Timer timer;
	
	        SnowPanel() {
	            setOpaque(false);
	
	            // create snowflakes
	            for (int i = 0; i < 150; i++) {
	                flakes.add(makeFlake());
	            }
	
	            timer = new Timer(33, e -> {
	                updateFlakes();
	                repaint();
	            });
	            timer.start();
	        }
	
	        private Snowflake makeFlake() {
	            Snowflake f = new Snowflake();
	            f.x = (float)(Math.random() * 2000);
	            f.y = (float)(Math.random() * 1200);
	            f.speed = 1.5f + (float)Math.random() * 2f;
	            f.drift = -0.5f + (float)Math.random();
	            f.size = 2f + (float)Math.random() * 3f;
	            f.opacity = 0.4f + (float)Math.random() * 0.6f;
	            f.sparkle = Math.random() < 0.05;
	            return f;
	        }
	
	        private void updateFlakes() {
	            for (Snowflake f : flakes) {
	                f.y += f.speed;
	                f.x += f.drift;
	
	                if (f.sparkle)
	                    f.opacity = 0.6f + (float)Math.random()*0.4f;
	
	                if (f.y > getHeight()) {
	                    // recycle at top
	                    f.x = (float)(Math.random() * getWidth());
	                    f.y = -10;
	                }
	            }
	        }
	
	        @Override
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);
	            Graphics2D g2 = (Graphics2D) g;
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
	            for (Snowflake f : flakes) {
	                g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f.opacity));
	                g2.setColor(Color.white);
	                g2.fillOval((int)f.x, (int)f.y, (int)f.size, (int)f.size);
	
	                // sparkle
	                if (f.sparkle) {
	                    g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, f.opacity * 0.7f));
	                    g2.drawOval((int)f.x-1, (int)f.y-1, (int)f.size+2, (int)f.size+2);
	                }
	            }
	        }
	    }
	
	 // 🎄 Animated Christmas lights overlay
	    class LightsOverlay extends JPanel {
	
	        private static class Light {
	            int x, y, radius;
	            Color base;
	            float glowPhase;
	        }
	
	        private final java.util.List<Light> bulbs = new java.util.ArrayList<>();
	        private final Timer timer;
	
	        LightsOverlay() {
	            setOpaque(false);
	
	            // generate bulbs across top
	            for (int i = 0; i < 18; i++) {
	                Light L = new Light();
	                L.x = 80 + i*85;
	                L.y = 20;
	                L.radius = 8;
	                L.base = pickColor();
	                L.glowPhase = (float)(Math.random()*Math.PI*2);
	                bulbs.add(L);
	            }
	
	            timer = new Timer(50, e -> {
	                for (Light L : bulbs) {
	                    L.glowPhase += 0.1f;
	                }
	                repaint();
	            });
	            timer.start();
	        }
	
	        private Color pickColor() {
	            Color[] c = {
	                    new Color(255,75,75),
	                    new Color(255,180,40),
	                    new Color(120,200,255),
	                    new Color(140,255,140)
	            };
	            return c[(int)(Math.random()*c.length)];
	        }
	
	        @Override
	        
	        protected void paintComponent(Graphics g) {
	            super.paintComponent(g);
	            Graphics2D g2 = (Graphics2D) g;
	            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
	
	            for (Light L : bulbs) {
	
	                float glow = 0.4f + (float) Math.sin(L.glowPhase) * 0.4f;
	                Color glowColor = new Color(
	                        (int)(L.base.getRed() * glow),
	                        (int)(L.base.getGreen() * glow),
	                        (int)(L.base.getBlue() * glow),
	                        150
	                );
	
	                // Glow halo
	                g2.setColor(glowColor);
	                g2.fillOval(L.x - L.radius * 2, L.y - L.radius * 2, L.radius * 4, L.radius * 4);
	
	                // Solid bulb
	                g2.setColor(L.base);
	                g2.fillOval(L.x - L.radius, L.y - L.radius, L.radius * 2, L.radius * 2);
	            }
	        }
	
	
	    }
	
	
	    
	    
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
	                super.paintComponent(g2);
	
	                // draw overlay icons (flag)
	                if (overlay != null) {
	                    int iconSize = 16;
	                    int x = (getWidth() - iconSize) / 2;
	                    int y = (getHeight() - iconSize) / 2;
	                    g2.drawImage(overlay, x, y, iconSize, iconSize, this);
	                }
	
	                g2.dispose();
	                return;
	            }
	
	            // ---------------------------------
	            // 2) REVEALED TILE (fade animation)
	            // ---------------------------------
	            float alpha = fading ? revealAlpha : 1f;
	            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha));
	
	            // draw TEXT (numbers)
	            super.paintComponent(g2);
	
	            // draw overlay icons (mine, spikes, question)
	            if (overlay != null) {
	                int iconSize = 16;
	                int x = (getWidth() - iconSize) / 2;
	                int y = (getHeight() - iconSize) / 2;
	                g2.drawImage(overlay, x, y, iconSize, iconSize, this);
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
	
	        turnLabel.setFont(new Font("Georgia", Font.BOLD, 16));
	        turnLabel.setForeground(MOSS_GLOW);
	
	        sharedScoreLabel.setFont(new Font("Georgia", Font.BOLD, 14));
	        sharedScoreLabel.setForeground(TEXT_PRIMARY);
	        updateSharedScoreLabel();
	
	        JButton help = woodButton("Help");
	        JButton menu = woodButton("Main Menu");
	        JButton historyBtn = woodButton("History");
	        historyBtn.setPreferredSize(new Dimension(110, 34));
	        historyBtn.addActionListener(e -> showHistory());

	        help.setPreferredSize(new Dimension(90, 34));
	        menu.setPreferredSize(new Dimension(90, 34));
	
	        help.addActionListener(e -> showHelp());
	        menu.addActionListener(e -> cards.show(root, SCREEN_MENU));
	
	        JLabel scoreTitle = new JLabel("Score:");
	        scoreTitle.setForeground(TEXT_PRIMARY);
	        scoreTitle.setFont(new Font("Georgia", Font.BOLD, 14));
	
	        JLabel livesLbl = new JLabel("Lives:");
	        livesLbl.setForeground(TEXT_PRIMARY);
	        livesLbl.setFont(new Font("Georgia", Font.BOLD, 14));
	
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

	        rightStats.setFont(new Font("Georgia", Font.BOLD, 14));
	        rightStats.setForeground(TEXT_PRIMARY);
	        refreshRightStats();
	        bar.add(rightStats);
	
	        return bar;
	    }
	
	    private void refreshRightStats() {
	        int idx = p1Turn ? 0 : 1;
	        String currentName = p1Turn ? tfP1.getText().trim() : tfP2.getText().trim();
	        rightStats.setText(currentName + " • Revealed: " + revealedCount[idx] + "  |  Flags: " + flagsCount[idx]);
	    }
	
	    /* ------------------------------ GAME LOGIC (unchanged) ------------------------------ */
	
	    private void toggleTurnLabel() {
	        p1Turn = !p1Turn;

	        String p1 = tfP1.getText().trim();
	        String p2 = tfP2.getText().trim();
	        turnLabel.setText("Turn: " + (p1Turn ? p1 : p2));
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
	        JOptionPane.showMessageDialog(this,
	                "Controls:\n" +
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

	                // SECOND CLICK → Activation
	                if (cell.isRevealed() && !cell.isSpecialUsed()) {

	                    int baseCost = 5; // from PDF later

	                    int choice = JOptionPane.showConfirmDialog(
	                            this,
	                            "This is a Question cell.\n" +
	                            "Using it will cost " + baseCost + " points.\n" +
	                            "Do you want to answer it now?",
	                            "Use Question?",
	                            JOptionPane.YES_NO_OPTION
	                    );

	                    if (choice == JOptionPane.YES_OPTION) {
	                        bumpScore(-baseCost);
	                        showQuestionDialog();
	                        cell.setSpecialUsed(true);
	                    }

	                    usedTurn = true;
	                    break;
	                }

	                // FIRST CLICK → Reveal only
	                if (!cell.isRevealed()) {
	                    cell.reveal();
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
	               SURPRISE — (2-step activation)
	            ---------------------------------------------------- */
	            case SURPRISE -> {

	                // SECOND CLICK → Activation
	                if (cell.isRevealed() && !cell.isSpecialUsed()) {

	                    int baseCost = 3; // from PDF later

	                    int choice = JOptionPane.showConfirmDialog(
	                            this,
	                            "This is a Surprise cell.\n" +
	                            "Using it will cost " + baseCost + " points.\n" +
	                            "Activate the surprise?",
	                            "Use Surprise?",
	                            JOptionPane.YES_NO_OPTION
	                    );

	                    if (choice == JOptionPane.YES_OPTION) {
	                        bumpScore(-baseCost);

	                        JOptionPane.showMessageDialog(
	                                this,
	                                "Surprise activated!\n(Apply real surprise logic here.)",
	                                "Surprise",
	                                JOptionPane.INFORMATION_MESSAGE
	                        );

	                        cell.setSpecialUsed(true);
	                    }

	                    usedTurn = true;
	                    break;
	                }

	                // FIRST CLICK → Reveal only
	                if (!cell.isRevealed()) {
	                    cell.reveal();
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
	        if (boards[ownerIdx].isAllSafeCellsRevealed()) {
	        	String winner = (ownerIdx == 0 ? tfP1.getText().trim() : tfP2.getText().trim());
	        	gameHistory.add(new String[]{
	        	    winner,
	        	    "Cleared Board",
	        	    String.valueOf(sharedPoints),
	        	    currentDifficulty.name(),
	        	    String.valueOf(java.time.LocalDateTime.now())
	        	});

	        	JOptionPane.showMessageDialog(
	                    this,
	                    "🎉 Congratulations! You cleared the board!",
	                    "Board Cleared",
	                    JOptionPane.INFORMATION_MESSAGE
	            );

	            if (fireworks != null) {
	                fireworks.startFireworks();
	            }
	        }
	    }

	
	
	
	    private void updateButtonForCell(int ownerIdx, Cell cell) {
	        TileButton btn = buttons[ownerIdx][cell.getRow()][cell.getCol()];
	
	        // 🔥 Make fully transparent when revealed
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

	
	        switch (type) {
	
	            case MINE ->
	                btn.setOverlayIcon(loadIconFit(A_MINE, W / 2, H / 2));
	
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
	
	            case SURPRISE ->
	                btn.setOverlayIcon(loadIconFit(A_SPIKES, W / 2, H / 2));
	
	            case QUESTION ->
	                btn.setOverlayIcon(loadIconFit(A_QUESTION, W / 2, H / 2));
	        }
	
	        // 🔒 Disable hover for revealed cells
	        btn.setRevealedVisual(true);
	    }
	
	
	
	
	    private void bumpRevealedForCurrentTurn() {
	        int idx = p1Turn ? 0 : 1;
	        revealedCount[idx]++;
	        refreshRightStats();
	    }
	
	    private void showQuestionDialog() {
	        Question q = SysData.nextRandom();
	        if (q == null) {
	            Object ans = JOptionPane.showInputDialog(this,
	                    "What is the capital of France?",
	                    "Trivia Question",
	                    JOptionPane.QUESTION_MESSAGE,
	                    null,
	                    new String[]{"A) London", "B) Berlin", "C) Paris", "D) Madrid"},
	                    "C) Paris");
	            if (ans != null && ans.toString().startsWith("C")) {
	                JOptionPane.showMessageDialog(this, "Correct! +3 points and +1 life");
	                bumpScore(3);
	                gainSharedLives(1);
	            } else if (ans != null) {
	                JOptionPane.showMessageDialog(this, "Wrong! -1 point and -1 life");
	                bumpScore(-1);
	                loseSharedLives(1);
	            }
	            return;
	        }
	
	        String[] choices = new String[] {
	                "A) " + q.getOptA(),
	                "B) " + q.getOptB(),
	                "C) " + q.getOptC(),
	                "D) " + q.getOptD()
	        };
	
	        Object ans = JOptionPane.showInputDialog(this,
	                q.getText(),
	                "Trivia Question",
	                JOptionPane.QUESTION_MESSAGE,
	                null,
	                choices,
	                choices[0]);
	
	        if (ans == null) return;
	
	        char picked = ans.toString().trim().charAt(0);
	        boolean correct = Character.toUpperCase(picked) == Character.toUpperCase(q.getCorrect());
	
	        int ptsRight = q.getPointsRight() != null ? q.getPointsRight() : 3;
	        int ptsWrong = q.getPointsWrong() != null ? q.getPointsWrong() : -1;
	        int lifeDelta = q.getLifeDelta() != null ? q.getLifeDelta() : 1;
	
	        if (correct) {
	            JOptionPane.showMessageDialog(this, "Correct! +" + ptsRight + " points and +" + Math.max(0, lifeDelta) + " life");
	            bumpScore(ptsRight);
	            if (lifeDelta > 0) gainSharedLives(lifeDelta);
	        } else {
	            JOptionPane.showMessageDialog(this, "Wrong! " + ptsWrong + " points and -" + Math.max(0, lifeDelta) + " life");
	            bumpScore(ptsWrong);
	            if (lifeDelta > 0) loseSharedLives(lifeDelta);
	        }
	    }
	
	    private void bumpScore(int delta) {
	        sharedPoints += delta;
	        updateSharedScoreLabel();
	    }
	
	    private void updateSharedScoreLabel() {
	        sharedScoreLabel.setText(" " + sharedPoints);
	    }
	
	    /* ------------------------------ SHARED LIVES ------------------------------ */
	
	    private void updateSharedHearts() {
	        for (int i = 0; i < MAX_LIVES; i++) {
	            boolean full = (i < sharedLives);
	            ImageIcon icon = loadIconFit(full ? A_HEART_FULL : A_HEART_EMPTY, 22, 22);
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
	
	    private void resetSharedLives() {
	        sharedLives = currentDifficulty.startLives;
	        if (sharedLives > MAX_LIVES) {
	            sharedLives = MAX_LIVES;
	        }
	        updateSharedHearts();
	    }
	
	    private void loseSharedLives(int n) {
	        if (n <= 0) return;
	        sharedLives = Math.max(0, sharedLives - n);
	        updateSharedHearts();
	        if (sharedLives == 0) {
	        	String p1 = tfP1.getText().trim();
	        	String p2 = tfP2.getText().trim();

	        	gameHistory.add(new String[]{
	        	    p1 + " & " + p2,
	        	    "Game Over (0 lives)",
	        	    String.valueOf(sharedPoints),
	        	    currentDifficulty.name(),
	        	    String.valueOf(java.time.LocalDateTime.now())
	        	});
	            JOptionPane.showMessageDialog(this,
	                    "Game Over – Final Score: " + sharedPoints,
	                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
	        }
	    }
	
	    private void exportHistoryToCSV() {
	        try {
	            java.io.File file = new java.io.File("game_history.csv");
	            java.io.PrintWriter pw = new java.io.PrintWriter(file);

	            // Header
	            pw.println("Player,Result,Score,Difficulty,Date");

	            // Rows
	            for (String[] row : gameHistory) {
	                pw.println(String.join(",", row));
	            }

	            pw.close();

	            JOptionPane.showMessageDialog(this,
	                    "History exported successfully to:\n" + file.getAbsolutePath(),
	                    "Export Complete",
	                    JOptionPane.INFORMATION_MESSAGE);

	        } catch (Exception ex) {
	            ex.printStackTrace();
	            JOptionPane.showMessageDialog(this,
	                    "Error writing CSV: " + ex.getMessage(),
	                    "Export Error",
	                    JOptionPane.ERROR_MESSAGE);
	        }
	    }

	    
	    private void showHistory() {
	        if (gameHistory.isEmpty()) {
	            JOptionPane.showMessageDialog(this,
	                    "No games played yet.",
	                    "Game History",
	                    JOptionPane.INFORMATION_MESSAGE);
	            return;
	        }

	        // Build text preview
	        StringBuilder sb = new StringBuilder();
	        sb.append("Player | Result | Score | Difficulty | Date\n");
	        sb.append("--------------------------------------------------------\n");

	        for (String[] row : gameHistory) {
	            sb.append(row[0]).append(" | ")
	              .append(row[1]).append(" | ")
	              .append(row[2]).append(" | ")
	              .append(row[3]).append(" | ")
	              .append(row[4]).append("\n");
	        }

	        JTextArea txt = new JTextArea(sb.toString());
	        txt.setEditable(false);
	        txt.setFont(new Font("Georgia", Font.PLAIN, 15));

	        JScrollPane sp = new JScrollPane(txt);
	        sp.setPreferredSize(new Dimension(550, 350));

	        // Buttons: OK or Export to Excel (CSV)
	        int choice = JOptionPane.showOptionDialog(
	                this,
	                sp,
	                "Game History",
	                JOptionPane.YES_NO_OPTION,
	                JOptionPane.INFORMATION_MESSAGE,
	                null,
	                new String[]{"Export to Excel", "Close"},
	                "Close"
	        );

	        if (choice == 0) {
	            exportHistoryToCSV();
	        }
	    }

	    private void gainSharedLives(int n) {
	        if (n <= 0) return;
	        int before = sharedLives;
	        sharedLives = Math.min(MAX_LIVES, sharedLives + n);
	        updateSharedHearts();
	
	        int overflow = before + n - MAX_LIVES;
	        if (overflow > 0) {
	            int perLife = LIFE_OVERFLOW_POINTS[Math.max(0, Math.min(difficultyIdx, LIFE_OVERFLOW_POINTS.length-1))];
	            bumpScore(overflow * perLife);
	            sharedLives = MAX_LIVES;
	            updateSharedHearts();
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
	
	/* ------------------------------ GRADIENT PANEL CLASS ------------------------------ */
	
	class GradientPaintPanel extends JPanel {
	
	    private final Color top;
	    private final Color bottom;
	
	    GradientPaintPanel(Color top, Color bottom) {
	        this.top = top;
	        this.bottom = bottom;
	        setOpaque(false);
	    }
	
	    @Override
	    protected void paintComponent(Graphics g) {
	        super.paintComponent(g);
	        int h = getHeight();
	        Graphics2D g2 = (Graphics2D) g;
	        g2.setPaint(new GradientPaint(
	                0, 0, top,
	                0, h, bottom
	        ));
	        g2.fillRect(0, 0, getWidth(), h);
	    } 
	    
	
	   
	
	}
