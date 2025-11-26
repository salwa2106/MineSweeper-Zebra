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

    private static final int TILE_SIZE = 44;

    // (for later when you fully hook MVC)
    private GameController controller;

    // buttons[owner][row][col]
    private TileButton[][][] buttons;

    // Boards for each player (0 = P1, 1 = P2)
    private final Board[] boards = new Board[2];

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

    /* ------------------------------ MENU SCREEN ------------------------------ */

    private JPanel buildMenu() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = woodCard();
        card.setPreferredSize(new Dimension(520, 380));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MINESWEEPER");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Georgia", Font.BOLD, 40));
        title.setForeground(MOSS_GLOW);

        JLabel subtitle = new JLabel("+ Trivia — Forest Edition");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(new Font("Georgia", Font.PLAIN, 18));
        subtitle.setForeground(TEXT_MUTED);

        JButton newGame = woodButton("New Game");
        JButton exit    = woodButton("Exit");

        newGame.addActionListener(e -> cards.show(root, SCREEN_NEW_GAME));
        exit.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                    "Exit the game?", "Confirm",
                    JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) System.exit(0);
        });

        card.add(Box.createVerticalStrut(28));
        card.add(title);
        card.add(Box.createVerticalStrut(8));
        card.add(subtitle);
        card.add(Box.createVerticalStrut(36));
        card.add(newGame);
        card.add(Box.createVerticalStrut(14));
        card.add(exit);
        card.add(Box.createVerticalGlue());

        center.add(card);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    /* ------------------------------ NEW GAME SCREEN ------------------------------ */

    private JPanel buildNewGame() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        JPanel header = woodHeader("NEW GAME SETUP");
        page.add(header, BorderLayout.NORTH);

        JPanel holder = new JPanel(new GridBagLayout());
        holder.setOpaque(false);

        JPanel formCard = woodCard();
        formCard.setPreferredSize(new Dimension(520, 360));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 16, 12, 16);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel l1 = new JLabel("Player 1 Name:");
        JLabel l2 = new JLabel("Player 2 Name:");
        JLabel l3 = new JLabel("Difficulty:");

        for (JLabel l : new JLabel[]{l1, l2, l3}) {
            l.setFont(new Font("Georgia", Font.BOLD, 15));
            l.setForeground(TEXT_PRIMARY);
        }

        styleField(tfP1);
        styleField(tfP2);

        cbDifficulty.setFont(new Font("Georgia", Font.PLAIN, 14));
        cbDifficulty.setBackground(WOOD);
        cbDifficulty.setForeground(TEXT_PRIMARY);
        cbDifficulty.setBorder(BorderFactory.createLineBorder(WOOD_LIGHT, 1, true));

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

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 0));
        actions.setOpaque(false);
        JButton back  = woodButton("Back");
        JButton start = woodButton("Start Game");
        actions.add(start);
        actions.add(back);

        back.addActionListener(e -> cards.show(root, SCREEN_MENU));
        start.addActionListener(e -> startGame());

        gc.gridx = 0; gc.gridy = 3; gc.gridwidth = 2;
        gc.anchor = GridBagConstraints.CENTER;
        form.add(actions, gc);

        formCard.add(form, BorderLayout.CENTER);
        holder.add(formCard);
        page.add(holder, BorderLayout.CENTER);

        return page;
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
        root.add(gamePanel, SCREEN_GAME);

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
        TileButton b = new TileButton(TILE_SIZE);
        b.setIcon(tiles.normal);
        b.getModel().addChangeListener(e -> {
            if (!b.isEnabled()) return;
            boolean roll = b.getModel().isRollover();
            b.setIcon(roll ? tiles.hover : tiles.normal);
        });
        return b;
    }

    private JPanel buildGame(int rows, int cols) {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        // allocate buttons array for both boards
        buttons = new TileButton[2][rows][cols];

        page.add(headerBarForGame(), BorderLayout.NORTH);

        // Player 1 (moss/grass)
        TileSet moss = new TileSet(
                loadIconFit(A_GRASS,   TILE_SIZE, TILE_SIZE),
                loadIconFit(A_GRASS_H, TILE_SIZE, TILE_SIZE)
        );
        // Player 2 (brown tile)
        TileSet cedar = new TileSet(
                loadIconFit(A_BROWN, TILE_SIZE, TILE_SIZE),
                loadIconFit(A_BROWN, TILE_SIZE, TILE_SIZE)
        );

        JPanel boardsPanel = new JPanel(new GridLayout(1, 2, 25, 10));
        boardsPanel.setOpaque(false);
        boardsPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        boardsPanel.add(boardCard("Player 1 Board", rows, cols, moss, 0));
        boardsPanel.add(boardCard("Player 2 Board", rows, cols, cedar, 1));

        page.add(boardsPanel, BorderLayout.CENTER);
        return page;
    }

    private JPanel boardCard(String title, int rows, int cols, TileSet tiles, int ownerIdx) {
        JPanel outer = woodCard();

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(new Font("Georgia", Font.BOLD, 18));
        lbl.setForeground(MOSS_GLOW);
        outer.add(lbl, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(rows, cols, 2, 2));
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
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        outer.add(sp, BorderLayout.CENTER);

        return outer;
    }

    /* ------------------------------ FLAG TOGGLE (NOW A METHOD) ------------------------------ */

    private void toggleFlag(int ownerIdx, int row, int col) {
        Board board = boards[ownerIdx];
        if (board == null) return;
        Cell modelCell = board.getCell(row, col);

        if (modelCell.isRevealed()) return;

        modelCell.toggleFlag();
        boolean flagged = modelCell.isFlagged();

        TileButton cellButton = buttons[ownerIdx][row][col];
        int W = cellButton.getPreferredSize().width;
        int H = cellButton.getPreferredSize().height;
        cellButton.setOverlayIcon(flagged ? loadIconFit(A_FLAG, W/2, H/2) : null);

        flagsCount[ownerIdx] += flagged ? 1 : -1;
        if (flagsCount[ownerIdx] < 0) flagsCount[ownerIdx] = 0;
        refreshRightStats();
    }

    /* ------------------------------ WIDGETS ------------------------------ */

    private static class TileButton extends JButton {
        private Image overlay;
        TileButton(int size) {
            setPreferredSize(new Dimension(size, size));
            setMargin(new Insets(0,0,0,0));
            setContentAreaFilled(false);
            setBorderPainted(false);
            setFocusPainted(false);
            setHorizontalTextPosition(SwingConstants.CENTER);
            setVerticalTextPosition(SwingConstants.CENTER);
            setFont(new Font("Georgia", Font.BOLD, 18));
            setForeground(Color.WHITE);
            setCursor(new Cursor(Cursor.HAND_CURSOR));
        }
        void setOverlayIcon(ImageIcon icon) {
            overlay = icon == null ? null : icon.getImage();
            repaint();
        }
        @Override protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            if (overlay != null) {
                int w = Math.min(getWidth(), getHeight()) * 3 / 5;
                int x = (getWidth() - w)/2, y = (getHeight() - w)/2;
                g.drawImage(overlay, x, y, w, w, this);
            }
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
        JButton menu = woodButton("Menu");
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
        String p1 = tfP1.getText().trim(), p2 = tfP2.getText().trim();
        turnLabel.setText("Turn: " + (p1Turn ? p1 : p2));
        refreshRightStats();
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

    private void handleCellClick(int ownerIdx, int row, int col) {
        Board board = boards[ownerIdx];
        if (board == null) return;

        Cell cell = board.getCell(row, col);
        if (cell.isRevealed() || cell.isFlagged()) {
            return;
        }

        CellType type = cell.getType();

        if (type == CellType.MINE) {
            cell.reveal();
            updateButtonForCell(ownerIdx, cell);
            bumpRevealedForCurrentTurn();
            JOptionPane.showMessageDialog(this,
                    "BOOM! Mine hit!", "Mine",
                    JOptionPane.WARNING_MESSAGE);
            loseSharedLives(1);
            toggleTurnLabel();
        } else if (type == CellType.EMPTY) {
            List<Cell> revealed = board.revealCascade(row, col);
            for (Cell c : revealed) {
                updateButtonForCell(ownerIdx, c);
                bumpRevealedForCurrentTurn();
            }
        } else if (type == CellType.NUMBER) {
            cell.reveal();
            updateButtonForCell(ownerIdx, cell);
            bumpRevealedForCurrentTurn();
        } else if (type == CellType.QUESTION) {
            showQuestionDialog();
            cell.reveal();
            updateButtonForCell(ownerIdx, cell);
            bumpRevealedForCurrentTurn();
        } else if (type == CellType.SURPRISE) {
            cell.reveal();
            updateButtonForCell(ownerIdx, cell);
            JOptionPane.showMessageDialog(this,
                    "Surprise! (example effect)", "Surprise",
                    JOptionPane.INFORMATION_MESSAGE);
            bumpRevealedForCurrentTurn();
        }
    }

    private void updateButtonForCell(int ownerIdx, Cell cell) {
        TileButton btn = buttons[ownerIdx][cell.getRow()][cell.getCol()];
        int W = btn.getPreferredSize().width;
        int H = btn.getPreferredSize().height;

        btn.setIcon(loadIconFit(A_DIRT, W, H));
        btn.setOverlayIcon(null);
        btn.setText("");

        CellType type = cell.getType();

        if (type == CellType.MINE) {
            btn.setOverlayIcon(loadIconFit(A_MINE, W / 2, H / 2));
        } else if (type == CellType.NUMBER) {
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
            int idx = Math.min(Math.max(num - 1, 0), pal.length - 1);
            btn.setForeground(pal[idx]);
        } else if (type == CellType.SURPRISE) {
            btn.setOverlayIcon(loadIconFit(A_SPIKES, W / 2, H / 2));
        }
        // QUESTION cells still look like empty ground; you can add icon later if you want.
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
            JOptionPane.showMessageDialog(this,
                    "Game Over – Final Score: " + sharedPoints,
                    "Game Over", JOptionPane.INFORMATION_MESSAGE);
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
