package View;

import javax.swing.*;

public class UserDashboard extends JFrame {

    public UserDashboard() {
        setTitle("User Dashboard");
        setSize(400, 300);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        add(new JLabel("USER PANEL", SwingConstants.CENTER));
    }
}
