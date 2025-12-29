package View;

import Model.SysData;

public class Main {
    public static void main(String[] args) {
        SysData.init(); // loads questions CSV
        new LoginFrame().setVisible(true);
    }
}