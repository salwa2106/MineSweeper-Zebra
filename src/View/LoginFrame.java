package View;

import Model.SysData;
import javax.swing.*;
import java.awt.*;

public class LoginFrame extends JFrame {

    private JTextField tfUser;
    private JPasswordField tfPass;
    private JButton btnLogin, btnSignup, btnExit;

    private static final String A_BG =
            fixPath("assets/mix/mix.png");

    public LoginFrame() {
    	
    	SysData.init();  // loads questions CSV once (safe even if called again)
        setTitle("MineSweeper + Trivia — Forest Edition");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);

        setContentPane(buildPage());
        setVisible(true);

        getRootPane().setDefaultButton(btnLogin);
        
    }

    /* ---------------- PAGE ---------------- */

    private JPanel buildPage() {
        JPanel page = new JPanel(new BorderLayout());
        page.setOpaque(false);

        BackgroundPanel bg = new BackgroundPanel(A_BG);
        bg.setLayout(new GridBagLayout());

        JPanel glass = createGlass();
        glass.setLayout(new BoxLayout(glass, BoxLayout.Y_AXIS));

        JLabel title = new JLabel("LOGIN", SwingConstants.CENTER);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setFont(new Font("Georgia", Font.BOLD, 48));
        title.setForeground(new Color(190, 255, 220));

        glass.add(title);
        glass.add(Box.createVerticalStrut(40));
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

    /* ---------------- FORM ---------------- */

    private JPanel buildForm() {
        JPanel p = new JPanel(new GridLayout(3, 2, 15, 15));
        p.setOpaque(false);
        p.setMaximumSize(new Dimension(420, 200));

        tfUser = new JTextField();
        tfPass = new JPasswordField();

        styleField(tfUser);
        styleField(tfPass);

        btnLogin  = createFrostedButton("Login");
        btnSignup = createFrostedButton("Sign Up");
        btnExit   = createFrostedButton("Exit");

        p.add(createLabel("Username"));
        p.add(tfUser);

        p.add(createLabel("Password"));
        p.add(tfPass);

        p.add(btnLogin);
        p.add(btnSignup);

        JPanel bottom = new JPanel();
        bottom.setOpaque(false);
        bottom.add(btnExit);

        btnLogin.addActionListener(e -> doLogin());
        btnSignup.addActionListener(e -> {
            dispose();
            new SignUpFrame();
        });
        btnExit.addActionListener(e -> System.exit(0));

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        wrapper.add(p);
        wrapper.add(Box.createVerticalStrut(15));
        wrapper.add(bottom);
        btnLogin.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnLogin.putClientProperty("JButton.defaultButton", Boolean.FALSE);
        btnSignup.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btnSignup.putClientProperty("JButton.defaultButton", Boolean.FALSE);
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

    /* ---------------- LOGIN ---------------- */

    private void doLogin() {
        String u = tfUser.getText().trim();
        String p = new String(tfPass.getPassword());

        if (SysData.authenticateUser(u, p) == null) {
            JOptionPane.showMessageDialog(this,
                    "Invalid username or password");
            return;
        }

        dispose();
        new MineSweeperPrototype().setVisible(true);
    }

    /* ---------------- GLASS PANEL ---------------- */

    private JPanel createGlass() {
        JPanel glass = new JPanel() {
            @Override
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
        glass.setPreferredSize(new Dimension(600, 420));
        return glass;
    }

    /* ---------------- UI HELPERS ---------------- */

    private void styleField(JTextField f) {
        f.setOpaque(true);
        f.setBackground(new Color(30, 55, 50, 200));
        f.setForeground(new Color(220, 255, 240));
        f.setCaretColor(Color.WHITE);

        f.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(160, 255, 220), 2),
                BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
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
