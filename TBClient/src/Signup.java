import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Signup {
    private JPanel signupP;
    private JTextField nameF;
    private JTextField usernameF;
    private JTextField passF;
    private JLabel nameL;
    private JLabel usernameL;
    private JLabel passL;
    private JButton signupB;
    private JLabel loginL;
    private JButton loginB;

    public Signup() {
        jframe signupFrame = new jframe("Time Boxing - Signup");
        signupFrame.add(signupP);
        loginB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                signupFrame.dispose();
                new Login();
            }
        });
        signupB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (Socket client = new Socket("localhost", 8585)) {
                    ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                    ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                    output.writeInt(2);
                    output.flush();
                    output.writeObject(usernameF.getText());
                    output.flush();
                    output.writeObject(passF.getText());
                    output.flush();
                    output.writeObject(nameF.getText());
                    output.flush();
                    int response = input.readInt();
                    if (response == 1) {
                        Main.filePath = usernameF.getText() + ".txt";
                        Main.name = nameF.getText();
                        signupFrame.dispose();
                        new HomeNull();
                    } else if (response == 0) {
                        JOptionPane.showMessageDialog(null, "Username already created!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
    }
}
