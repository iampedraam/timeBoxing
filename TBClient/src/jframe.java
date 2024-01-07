import javax.swing.*;

public class jframe extends JFrame {
    public jframe(String title) {
        setTitle(title);
        setVisible(true);
        setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        setSize(500,400);
    }
}
