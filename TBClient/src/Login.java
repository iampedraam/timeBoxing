import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class Login {
    private JPanel loginP;
    private JTextField usernameF;
    private JPasswordField passwordField;
    private JLabel usernameL;
    private JLabel passL;
    private JButton loginB;
    private JLabel signupL;
    private JButton signupB;

    public Login() {
        jframe loginFrame = new jframe("Time Boxing - Login");
        loginFrame.add(loginP);

        loginB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                try (Socket client = new Socket("localhost", 8585)) {
                    ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());

                    output.writeInt(1);
                    output.flush();

                    output.writeObject(usernameF.getText());
                    output.flush();

                    char[] passChar = passwordField.getPassword();
                    String pass = new String(passChar);
                    output.writeObject(pass);
                    output.flush();

                    ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                    int response = input.readInt();

                    if (response == 1) {
                        Main.filePath = usernameF.getText() + ".txt";
                        try {
                            Main.name = (String) input.readObject();
                        } catch (ClassNotFoundException ex) {
                            throw new RuntimeException(ex);
                        }

                        client.close();

                        loginFrame.dispose();
                        new HomeTask(); //if there's no task for today, HomeTask will check and pass user to HomeNull

                    } else if (response == 0) {
                        JOptionPane.showMessageDialog(null, "Wrong Username/Password", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        signupB.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                loginFrame.dispose();
                new Signup();
            }
        });
    }
}
