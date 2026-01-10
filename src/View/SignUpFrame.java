package View;

import Model.SysData;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import Model.SoundManager;


public class SignUpFrame extends JFrame {

    private JTextField tfUser;
    private JPasswordField tfPass, tfConfirm;
    private JProgressBar strengthBar;
    private JCheckBox cbShowPass;
    private JButton btnCreate, btnBack, btnExit;

    private static final String A_BG =
            fixPath("assets/mix/mix.png");

    public SignUpFrame() {
        setTitle("MineSweeper + Trivia — Forest Edition");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setContentPane(buildPage());
        setVisible(true);
    }

    /* ================= PAGE ================= */

    private JPanel buildPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        BackgroundPanel bg = new BackgroundPanel(A_BG);
        bg.setLayout(new GridBagLayout());

        JPanel glass = createGlass();
        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("SIGN UP", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Georgia", Font.BOLD, 48));
        title.setForeground(new Color(190, 255, 220));

        glass.add(title);
        glass.add(Box.createVerticalStrut(30));
        glass.add(buildForm());

        JPanel stacked = new JPanel(new BorderLayout());
        stacked.setOpaque(false);
        stacked.add(glass, BorderLayout.CENTER);

        bg.add(stacked);
        page.add(bg, BorderLayout.CENTER);
        

        return wrapWithSlideFade(page);
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

    /* ================= FORM ================= */

    private JPanel buildForm() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new GridLayout(6, 2, 15, 12));
        p.setMaximumSize(new Dimension(460, 320));

        tfUser = new JTextField();
        tfPass = new JPasswordField();
        tfConfirm = new JPasswordField();

        styleField(tfUser);
        styleField(tfPass);
        styleField(tfConfirm);

        strengthBar = new JProgressBar(0, 100);
        strengthBar.setStringPainted(true);
        strengthBar.setForeground(new Color(120, 200, 160));
        strengthBar.setBackground(new Color(30, 50, 50));

        tfPass.getDocument().addDocumentListener(new DocumentListener() {
            public void insertUpdate(DocumentEvent e) { updateStrength(); }
            public void removeUpdate(DocumentEvent e) { updateStrength(); }
            public void changedUpdate(DocumentEvent e) {}
        });

        cbShowPass = new JCheckBox("👁 Show password");
        cbShowPass.setOpaque(false);
        cbShowPass.setForeground(new Color(200, 255, 230));
        cbShowPass.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	togglePassword();});

        btnCreate = createFrostedButton("Create");
        btnBack   = createFrostedButton("Back");
        btnExit   = createFrostedButton("Exit");

        p.add(createLabel("Username"));
        p.add(tfUser);

        p.add(createLabel("Password"));
        p.add(tfPass);

        p.add(createLabel("Confirm Password"));
        p.add(tfConfirm);

        p.add(createLabel("Strength"));
        p.add(strengthBar);

        p.add(cbShowPass);
        p.add(createLabel(""));

        p.add(btnCreate);
        p.add(btnBack);

        btnCreate.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            doSignup();
        });

        btnBack.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            dispose();
            new LoginFrame();
        });

        btnExit.addActionListener(e -> {
            SoundManager.play(SoundManager.Sfx.CLICK);
            System.exit(0);
        });

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(p);
        wrapper.add(Box.createVerticalStrut(15));

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(btnExit);
        btnExit.addActionListener(e -> {
        	SoundManager.play(SoundManager.Sfx.CLICK);
        	System.exit(0);});

        wrapper.add(bottom);
        btnCreate.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnCreate.putClientProperty("JButton.defaultButton", Boolean.FALSE);
        btnBack.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnBack.putClientProperty("JButton.defaultButton", Boolean.FALSE);
        btnExit.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnExit.putClientProperty("JButton.defaultButton", Boolean.FALSE);

        return wrapper;
    }

    private JLabel createLabel(String text) {
        JLabel l = new JLabel(text);
        l.setForeground(new Color(200, 255, 235));
        l.setFont(new Font("Georgia", Font.BOLD, 16));
        return l;
    }

    /* ================= LOGIC ================= */

    private void doSignup() {
        String u  = tfUser.getText().trim();
        String p1 = new String(tfPass.getPassword());
        String p2 = new String(tfConfirm.getPassword());

        if (u.isEmpty() || p1.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Fields cannot be empty");
            return;
        }

        if (!p1.equals(p2)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match");
            return;
        }

        if (u.equalsIgnoreCase("admin")) {
            JOptionPane.showMessageDialog(this, "Username reserved");
            return;
        }

        if (!SysData.addUser(u, p1, "USER")) {
            JOptionPane.showMessageDialog(this, "Username already exists");
            return;
        }

        JOptionPane.showMessageDialog(this, "Account created!");
        dispose();
        new LoginFrame();
    }
    
    private void togglePassword() {
        char echo = cbShowPass.isSelected() ? (char) 0 : '•';
        tfPass.setEchoChar(echo);
        tfConfirm.setEchoChar(echo);
    }
    
    private void updateStrength() {
        String p = new String(tfPass.getPassword());
        int score = 0;

        if (p.length() >= 6) score += 30;
        if (p.matches(".*[A-Z].*")) score += 20;
        if (p.matches(".*[0-9].*")) score += 20;
        if (p.matches(".*[^a-zA-Z0-9].*")) score += 30;

        strengthBar.setValue(score);

        strengthBar.setString(
                score < 40 ? "Weak" :
                score < 70 ? "Medium" : "Strong"
        );
    }

    /* ================= UI HELPERS ================= */

    private JPanel createGlass() {
        JPanel glass = new JPanel() {
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(20, 35, 35, 170));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 40, 40);

                g2.setColor(new Color(160, 255, 255, 130));
                g2.setStroke(new BasicStroke(4f));
                g2.drawRoundRect(2, 2,
                        getWidth() - 4, getHeight() - 4, 36, 36);

                g2.dispose();
                super.paintComponent(g);
            }
        };

        glass.setOpaque(false);
        glass.setBorder(BorderFactory.createEmptyBorder(30, 50, 30, 50));
        glass.setPreferredSize(new Dimension(650, 500));
        return glass;
    }

    private void styleField(JTextField f) {
        f.setOpaque(false);
        f.setForeground(Color.WHITE);
        f.setCaretColor(Color.WHITE);
        f.setBorder(BorderFactory.createLineBorder(
                new Color(160, 255, 220)));
    }

    private JButton createFrostedButton(String text) {
        JButton b = new JButton(text);
        b.setFont(new Font("Georgia", Font.BOLD, 18));
        b.setFocusPainted(false);
        b.setBackground(new Color(80, 130, 110));
        b.setForeground(new Color(200, 255, 230));
        b.setBorder(BorderFactory.createLineBorder(
                new Color(160, 255, 220), 2));
        return b;
    }

    private static String fixPath(String rel) {
        try {
            String base = MineSweeperPrototype.class
                    .getProtectionDomain()
                    .getCodeSource()
                    .getLocation()
                    .getPath();

            String decoded = java.net.URLDecoder.decode(base, "UTF-8");

            if (decoded.contains("/bin")) {
                decoded = decoded.substring(0, decoded.indexOf("/bin"));
                return decoded + "/src/" + rel;
            }

            decoded = decoded.substring(0, decoded.lastIndexOf("/"));
            return decoded + "/" + rel;

        } catch (Exception e) {
            return rel;
        }
    }
}
