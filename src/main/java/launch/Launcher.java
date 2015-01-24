package launch;

import core.Demo;

import javax.swing.*;
import java.awt.*;

public class Launcher {

    public static void main(String[] args) {
        JFrame frame = new JFrame("Shortest path demo");

        Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
        frame.setLocation(dim.width / 2 - frame.getSize().width / 2, dim.height / 2 - frame.getSize().height / 2);

        frame.add(new Demo());
        frame.setExtendedState(JFrame.MAXIMIZED_BOTH);
        frame.setVisible(true);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

}
