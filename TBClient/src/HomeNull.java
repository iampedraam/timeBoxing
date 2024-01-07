import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class HomeNull {
    private JPanel homeP;
    private JLabel welcome;
    private JLabel startL;
    private JButton timeboxB;

    public HomeNull() {
        jframe homeF = new jframe("Time Boxing");
        homeF.add(homeP);
        welcome.setText("Welcome back " + Main.name + "!");
        timeboxB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                homeF.dispose();
                new BrainDumps();
            }
        });
    }
}
