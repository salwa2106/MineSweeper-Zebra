package View;

import Controller.GameController;
import Model.Question;
import Model.SysData;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.util.Random;

/**
 * MineSweeper + Trivia — Forest Edition (Swing / Java 8+)
 * Iteration 1 version: menu, new game, game board, score & lives.
 * History / settings / questions wizard screens were removed.
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
    private TileButton[][][] buttons;

    private ImageIcon loadIconFit(String path, int w, int h) {
        Image img = new ImageIcon(path).getImage();
        return new ImageIcon(img.getScaledInstance(w, h, Image.SCALE_SMOOTH));
    }

    private ImageIcon tinted(String path, Color tint, float alpha, int w, int h) {
        Image baseScaled = new ImageIcon(path).getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
        BufferedImage out = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = out.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(baseScaled, 0, 0, null);
        g.setComposite(AlphaComposite.SrcAtop.derive(alpha));
        g.setColor(tint);
        g.fillRect(0, 0, w, h);
        g.dispose();
        return new ImageIcon(out);
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

    // Difficulty index (0=Easy,1=Medium,2=Hard)
    private int difficultyIdx = 0;

    // Start lives per difficulty
    private static final int[] START_LIVES = {6, 8, 10};
    // Overflow conversion (points per extra life) per difficulty
    private static final int[] LIFE_OVERFLOW_POINTS = {1, 2, 3};

    // per-player counters
    private final int[] flagsCount    = {0, 0};
    private final int[] revealedCount = {0, 0};

    /* ------------------------------ CONSTRUCTOR ------------------------------ */
    public MineSweeperPrototype() {
        super("MineSweeper + Trivia — Forest Edition");

        try { UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName()); } catch (Exception ignore) {}
        System.setProperty("awt.useSystemAAFontSettings", "on");
        System.setProperty("swing.aatext", "true");
        UIManager.put("defaultFont", Theme.regular());

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1200, 800));
        setLocationRelativeTo(null);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        try {
            java.util.List<Image> list = new java.util.ArrayList<>();
            for (String s : new String[]{"16","24","32","48","64","128","256"}) {
                list.add(new ImageIcon("assets/minesweeper_trivia_icon_" + s + ".png").getImage());
            }
            setIconImages(list);
        } catch (Exception ignore) {}

        // Load questions from CSV (used for question cells)
        SysData.init();

        // Build screens
        root.setOpaque(false);
        root.add(buildMenu(), SCREEN_MENU);
        root.add(buildNewGame(), SCREEN_NEW_GAME);

        // default game board (easy) – will be rebuilt when "Start Game" is pressed
        gamePanel = buildGame(9, 9);
        root.add(gamePanel, SCREEN_GAME);

        BackgroundPanel forest = new BackgroundPanel(A_BG);
        forest.add(root, BorderLayout.CENTER);
        setContentPane(forest);

        if (!new java.io.File(A_BG).exists()) {
            JOptionPane.showMessageDialog(this,
                    "Background image not found at:\n" + A_BG,
                    "Missing Image", JOptionPane.ERROR_MESSAGE);
        }
    }

    /* ------------------------------ THEME HELPERS ------------------------------ */

    private JPanel card() {
        JPanel p = new JPanel(new BorderLayout());
        p.setOpaque(true);
        p.setBackground(Theme.CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                softShadow(),
                BorderFactory.createCompoundBorder(
                        BorderFactory.createLineBorder(Theme.CARD_STROKE, 1, true),
                        BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD)
                )
        ));
        return p;
    }

    private Border softShadow() {
        return new SoftDropShadowBorder(new Color(10, 10, 10), 12, 0.22f, Theme.RADIUS);
    }

    private JPanel gradientHeader(String title) {
        JPanel header = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, Theme.SECONDARY, 0, getHeight(), Theme.SECONDARY.darker());
                g2.setPaint(gp);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), Theme.RADIUS, Theme.RADIUS);
            }
        };
        header.setOpaque(false);
        header.setLayout(new GridBagLayout());
        header.setBorder(BorderFactory.createEmptyBorder(16, 24, 16, 24));
        JLabel lbl = new JLabel(title);
        lbl.setFont(Theme.bold(24));
        lbl.setForeground(Color.WHITE);
        header.add(lbl);
        return header;
    }

    private JButton btn(String text, Color bg, Color fg) {
        JButton b = new JButton(text) {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color paint = bg;
                if (getModel().isPressed()) paint = bg.darker();
                else if (getModel().isRollover()) paint = bg.brighter();
                g2.setColor(paint);
                g2.fillRoundRect(0, 0, getWidth()-1, getHeight()-1, Theme.RADIUS_SM, Theme.RADIUS_SM);
                super.paintComponent(g);
            }
        };
        b.setForeground(fg);
        b.setContentAreaFilled(false);
        b.setFocusPainted(false);
        b.setBorderPainted(false);
        b.setFont(Theme.bold(14));
        b.setPreferredSize(new Dimension(200, 42));
        b.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return b;
    }

    private JButton btnPrimary(String text)   { return btn(text, new Color(52,168,83), Color.WHITE); }
    private JButton btnSecondary(String text) { return btn(text, new Color(90,120,70), Color.WHITE); }
    private JButton btnDanger(String text)    { return btn(text, Theme.DANGER, Color.WHITE); }
    private JButton btnSmall(String text) {
        JButton b = btn(text, new Color(90,120,70), Color.WHITE);
        b.setPreferredSize(new Dimension(92, 36));
        b.setFont(Theme.bold(12));
        return b;
    }

    private Component space(int w) { return Box.createRigidArea(new Dimension(w, 0)); }

    /* ------------------------------ SCREENS ------------------------------ */

    private void equalizeButtons(Dimension size, JButton... btns) {
        for (JButton b : btns) {
            b.setMinimumSize(size);
            b.setPreferredSize(size);
            b.setMaximumSize(size);
            b.setAlignmentX(Component.CENTER_ALIGNMENT);
        }
    }

    private JPanel buildMenu() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        JPanel center = new JPanel(new GridBagLayout());
        center.setOpaque(false);

        JPanel card = card();
        card.setPreferredSize(new Dimension(520, 400));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("MINESWEEPER");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(Theme.bold(42));
        title.setForeground(new Color(215,235,215));

        JLabel subtitle = new JLabel("+ Trivia — Forest Edition");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setFont(Theme.regular());
        subtitle.setForeground(Theme.MUTED);

        JButton newGame = btnPrimary("New Game");
        JButton exit    = btnDanger("Exit");

        equalizeButtons(new Dimension(260, 48), newGame, exit);

        newGame.addActionListener(e -> cards.show(root, SCREEN_NEW_GAME));
        exit.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this, "Exit the game?", "Confirm", JOptionPane.YES_NO_OPTION);
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
        JLabel ver = new JLabel("v1.0 - Iteration 1");
        ver.setForeground(Theme.MUTED);
        ver.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.add(ver);
        card.add(Box.createVerticalStrut(8));

        center.add(card);
        page.add(center, BorderLayout.CENTER);
        return page;
    }

    private JPanel buildNewGame() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);
        page.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD));

        JPanel header = gradientHeader("NEW GAME SETUP");
        page.add(header, BorderLayout.NORTH);

        JPanel holder = new JPanel(new GridBagLayout());
        holder.setOpaque(false);

        JPanel formCard = card();
        formCard.setPreferredSize(new Dimension(520, 360));

        JPanel form = new JPanel(new GridBagLayout());
        form.setOpaque(false);
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(12, 16, 12, 16);
        gc.fill = GridBagConstraints.HORIZONTAL;

        JLabel l1 = new JLabel("Player 1 Name:");
        JLabel l2 = new JLabel("Player 2 Name:");
        JLabel l3 = new JLabel("Difficulty:");

        Color labelColor = new Color(220, 240, 220);
        Font labelFont = Theme.bold(15);

        l1.setForeground(labelColor);
        l2.setForeground(labelColor);
        l3.setForeground(labelColor);
        l1.setFont(labelFont);
        l2.setFont(labelFont);
        l3.setFont(labelFont);

        styleField(tfP1);
        styleField(tfP2);

        cbDifficulty.setModel(new DefaultComboBoxModel<>(
                new String[]{"Easy (9x9)", "Medium (13x13)", "Hard (16x16)"}
        ));
        useBasicComboLook(cbDifficulty);

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
        JButton back  = btnSecondary("Back");
        JButton start = btnPrimary("Start Game");
        actions.add(back);
        actions.add(start);

        back.addActionListener(e -> cards.show(root, SCREEN_MENU));
        start.addActionListener(e -> {
            String p1 = tfP1.getText().trim();
            String p2 = tfP2.getText().trim();

            if (p1.isEmpty() || p2.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter both player names.",
                        "Input Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int rows = 9, cols = 9;
            difficultyIdx = cbDifficulty.getSelectedIndex();
            if (difficultyIdx == 1) rows = cols = 13;
            else if (difficultyIdx == 2) rows = cols = 16;

            // reset per-player counters
            flagsCount[0] = flagsCount[1] = 0;
            revealedCount[0] = revealedCount[1] = 0;

            // rebuild the game board
            root.remove(gamePanel);
            gamePanel = buildGame(rows, cols);
            root.add(gamePanel, SCREEN_GAME);

            // reset shared lives & scores
            resetSharedLives();
            turnLabel.setText("Turn: " + p1);
            sharedPoints = 0;
            updateSharedScoreLabel();
            p1Turn = true;
            refreshRightStats();

            cards.show(root, SCREEN_GAME);
        });

        formCard.add(form, BorderLayout.CENTER);
        formCard.add(actions, BorderLayout.SOUTH);

        holder.add(formCard);
        page.add(holder, BorderLayout.CENTER);
        return page;
    }

    private void useBasicComboLook(JComboBox<?> cb) {
        cb.updateUI();
        cb.setRenderer(new DefaultListCellRenderer());
        cb.setOpaque(true);
        cb.setBackground(UIManager.getColor("ComboBox.background"));
        cb.setForeground(UIManager.getColor("ComboBox.foreground"));
        Border b = UIManager.getBorder("ComboBox.border");
        if (b != null) cb.setBorder(b);
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

        JPanel boards = new JPanel(new GridBagLayout());
        boards.setOpaque(false);
        boards.setBorder(BorderFactory.createEmptyBorder(Theme.PAD, Theme.PAD, Theme.PAD, Theme.PAD));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(Theme.GAP, Theme.GAP, Theme.GAP, Theme.GAP);
        gbc.gridx = 0; gbc.gridy = 0;
        boards.add(boardCard("Player 1 Board", rows, cols, new Color(52, 168, 83), moss, 0), gbc);

        gbc.gridx = 1;
        boards.add(boardCard("Player 2 Board", rows, cols, new Color(139, 90, 43), cedar, 1), gbc);


        page.add(boards, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER));
        bottom.setOpaque(false);
        JButton endTurn = btn("End Turn", new Color(166,124,82), Color.WHITE);
        endTurn.addActionListener(e -> toggleTurnLabel());
        bottom.add(endTurn);
        page.add(bottom, BorderLayout.SOUTH);

        return page;
    }

    private JPanel boardCard(String title, int rows, int cols, Color accent, TileSet tiles, int ownerIdx) {
        JPanel outer = card();
        outer.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(accent, 2, true),
                BorderFactory.createEmptyBorder(12, 12, 12, 12)
        ));

        JLabel lbl = new JLabel(title, SwingConstants.CENTER);
        lbl.setFont(Theme.bold(16));
        lbl.setForeground(accent);
        outer.add(lbl, BorderLayout.NORTH);

        JPanel grid = new JPanel(new GridLayout(rows, cols, 2, 2));
        grid.setOpaque(false);
        grid.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

        for (int r=0; r<rows; r++) {
            for (int c=0; c<cols; c++) {
                final JButton cell = tileButton(tiles);

                final JPopupMenu m = new JPopupMenu();
                JMenuItem flag = new JMenuItem("Toggle Flag");
                m.add(flag);

                cell.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e)  { if (e.isPopupTrigger() || SwingUtilities.isRightMouseButton(e)) m.show(cell, e.getX(), e.getY()); }
                    public void mouseReleased(MouseEvent e) { if (e.isPopupTrigger()) m.show(cell, e.getX(), e.getY()); }
                    @Override public void mouseClicked(MouseEvent e) {
                        if (SwingUtilities.isRightMouseButton(e)) flag.doClick();
                    }
                });

                flag.addActionListener(ev -> {
                    boolean flagged = Boolean.TRUE.equals(cell.getClientProperty("flagged"));
                    flagged = !flagged;
                    cell.putClientProperty("flagged", flagged);
                    int W = cell.getPreferredSize().width, H = cell.getPreferredSize().height;
                    ((TileButton)cell).setOverlayIcon(flagged ? loadIconFit(A_FLAG, W/2, H/2) : null);

                    // update the owner's flag count and header
                    flagsCount[ownerIdx] += flagged ? 1 : -1;
                    if (flagsCount[ownerIdx] < 0) flagsCount[ownerIdx] = 0;
                    refreshRightStats();
                });

                // Iteration-1 demo logic (later replaced by Board/GameController calls)
                cell.addActionListener(e -> demoCellClick(cell));
                grid.add(cell);
            }
        }

        JScrollPane sp = new JScrollPane(grid);
        sp.setBorder(null);
        sp.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        sp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        outer.add(sp, BorderLayout.CENTER);

        return outer;
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
            setFont(Theme.bold(18));
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

    private JLabel vsChip() {
        JLabel vs = new JLabel("VS", SwingConstants.CENTER);
        vs.setPreferredSize(new Dimension(64, 64));
        vs.setFont(Theme.bold(18));
        vs.setForeground(Color.WHITE);
        vs.setOpaque(true);
        vs.setBackground(new Color(58,83,45));
        vs.setBorder(BorderFactory.createLineBorder(new Color(0,0,0,60), 1, true));
        return vs;
    }

    private void styleField(JTextField tf) {
        tf.setFont(Theme.regular());
        tf.setForeground(new Color(240, 250, 240));
        tf.setCaretColor(new Color(230, 255, 230));
        tf.setOpaque(false);
        tf.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(100, 140, 100), 2, true),
                BorderFactory.createEmptyBorder(10, 12, 10, 12)
        ));
    }

    /* ------------------------------ HEADER BAR / SCORE / LIVES ------------------------------ */

    private JPanel headerBarForGame() {
        JPanel bar = new JPanel() {
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                GradientPaint gp = new GradientPaint(0, 0, new Color(40,58,35), 0, getHeight(), new Color(28,40,25));
                g2.setPaint(gp);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        bar.setLayout(new FlowLayout(FlowLayout.LEFT, 24, 10));
        bar.setPreferredSize(new Dimension(0, 60));

        turnLabel.setFont(Theme.bold(16));
        turnLabel.setForeground(Color.WHITE);

        sharedScoreLabel.setFont(Theme.bold(14));
        sharedScoreLabel.setForeground(Color.WHITE);
        updateSharedScoreLabel();

        JPanel sharedBar = heartsBarShared(sharedHearts, sharedLives);

        JButton help = btnSmall("Help");
        JButton menu = btnSmall("Menu");
        help.addActionListener(e -> showHelp());
        menu.addActionListener(e -> cards.show(root, SCREEN_MENU));

        bar.add(turnLabel);
        bar.add(space(24));

        JLabel scoreTitle = new JLabel("Score:");
        scoreTitle.setForeground(Color.WHITE);
        scoreTitle.setFont(Theme.bold(14));
        bar.add(scoreTitle);
        bar.add(sharedScoreLabel);

        bar.add(space(24));
        JLabel livesLbl = new JLabel("Lives:");
        livesLbl.setForeground(Color.WHITE);
        livesLbl.setFont(Theme.bold(14));
        bar.add(livesLbl);
        bar.add(sharedBar);

        bar.add(space(36));
        bar.add(help);
        bar.add(menu);

        bar.add(space(24));
        rightStats.setFont(Theme.bold(14));
        rightStats.setForeground(Color.WHITE);
        refreshRightStats(); // initial
        bar.add(rightStats);

        return bar;
    }

    private void refreshRightStats() {
        int idx = p1Turn ? 0 : 1;
        String currentName = p1Turn ? tfP1.getText().trim() : tfP2.getText().trim();
        rightStats.setText(currentName + " \u2022 Revealed: " + revealedCount[idx] + "  |  Flags: " + flagsCount[idx]);
    }

    /* ------------------------------ GAME LOGIC (Iteration-1 placeholder) ------------------------------ */

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

    // For Iteration-1 you will later replace this with calls to GameController / Board.
    private void demoCellClick(JButton cell) {
        // already revealed? do nothing
        if (Boolean.TRUE.equals(cell.getClientProperty("revealed"))) {
            return;
        }

        // flagged? don't reveal (optional – keep this if you want flags to protect cells)
        if (Boolean.TRUE.equals(cell.getClientProperty("flagged"))) {
            return;
        }

        String[] types = {"Empty", "Number", "Question", "Surprise", "Mine"};
        String pick = types[new Random().nextInt(types.length)];
        final int W = cell.getPreferredSize().width, H = cell.getPreferredSize().height;

        if ("Empty".equals(pick)) {
            cell.putClientProperty("revealed", true);
            cell.setText("");
            cell.setIcon(loadIconFit(A_DIRT, W, H));
            bumpScore(1);
            bumpRevealedForCurrentTurn();

        } else if ("Number".equals(pick)) {
            cell.putClientProperty("revealed", true);
            int num = new Random().nextInt(8) + 1;
            cell.setText(String.valueOf(num));
            cell.setIcon(loadIconFit(A_DIRT, W, H));

            // strong colors for numbers (now they are NOT disabled so this works)
            Color[] pal = {
                    new Color(52,152,219),   // 1 blue
                    new Color(46,204,113),   // 2 green
                    new Color(231,76,60),    // 3 red
                    new Color(155,89,182),   // 4 purple
                    new Color(230,126,34),   // 5 orange
                    new Color(26,188,156),   // 6 teal
                    new Color(52,73,94),     // 7 dark
                    new Color(149,165,166)   // 8 gray
            };
            cell.setForeground(pal[Math.min(num - 1, pal.length - 1)]);
            bumpRevealedForCurrentTurn();

        } else if ("Question".equals(pick)) {
            // question does NOT mark the cell as revealed yet (your choice)
            showQuestionDialog();

        } else if ("Surprise".equals(pick)) {
            cell.putClientProperty("revealed", true);
            cell.setText("");
            cell.setIcon(loadIconFit(A_DIRT, W, H));
            ((TileButton) cell).setOverlayIcon(loadIconFit(A_SPIKES, W / 2, H / 2));
            JOptionPane.showMessageDialog(this,
                    "Surprise! (example effect)", "Surprise",
                    JOptionPane.INFORMATION_MESSAGE);
            bumpRevealedForCurrentTurn();

        } else { // Mine
            cell.putClientProperty("revealed", true);
            cell.setText("");
            cell.setIcon(loadIconFit(A_DIRT, W, H));
            ((TileButton) cell).setOverlayIcon(loadIconFit(A_MINE, W / 2, H / 2));
            JOptionPane.showMessageDialog(this,
                    "BOOM! Mine hit!", "Mine",
                    JOptionPane.WARNING_MESSAGE);

            bumpRevealedForCurrentTurn();
            loseSharedLives(1);
            toggleTurnLabel();
        }
    }


    private void bumpRevealedForCurrentTurn() {
        int idx = p1Turn ? 0 : 1;
        revealedCount[idx]++;
        refreshRightStats();
    }

    /** Ask a random question from SysData (CSV). */
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

    private JPanel heartsBarShared(JLabel[] slots, int lives) {
        JPanel p = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        p.setOpaque(false);
        for (int i = 0; i < MAX_LIVES; i++) {
            JLabel h = new JLabel();
            h.setPreferredSize(new Dimension(22, 22));
            slots[i] = h;
            p.add(h);
        }
        updateSharedHearts(slots, lives);
        return p;
    }

    private void updateSharedHearts(JLabel[] slots, int lives) {
        for (int i = 0; i < MAX_LIVES; i++) {
            boolean full = (i < lives);
            ImageIcon icon = loadIconFit(full ? A_HEART_FULL : A_HEART_EMPTY, 22, 22);
            if (icon != null && icon.getIconWidth() > 0) {
                slots[i].setText(null);
                slots[i].setIcon(icon);
            } else {
                slots[i].setIcon(null);
                slots[i].setText(full ? "♥" : "♡");
                slots[i].setForeground(full ? new Color(220, 70, 70) : new Color(220, 220, 220));
                slots[i].setFont(Theme.bold(18));
            }
        }
    }

    private void resetSharedLives() {
        sharedLives = START_LIVES[Math.max(0, Math.min(difficultyIdx, START_LIVES.length-1))];
        updateSharedHearts(sharedHearts, sharedLives);
    }

    private void loseSharedLives(int n) {
        if (n <= 0) return;
        sharedLives = Math.max(0, sharedLives - n);
        updateSharedHearts(sharedHearts, sharedLives);
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
        updateSharedHearts(sharedHearts, sharedLives);

        int overflow = before + n - MAX_LIVES;
        if (overflow > 0) {
            int perLife = LIFE_OVERFLOW_POINTS[Math.max(0, Math.min(difficultyIdx, LIFE_OVERFLOW_POINTS.length-1))];
            bumpScore(overflow * perLife);
            sharedLives = MAX_LIVES;
            updateSharedHearts(sharedHearts, sharedLives);
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
