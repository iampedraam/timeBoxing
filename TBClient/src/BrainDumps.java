import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class BrainDumps {
    private JPanel panel;
    private JLabel step1;
    private JLabel info;
    private JTextArea textArea;
    private JLabel note;
    private JButton previousButton;
    private JButton nextButton;

    public BrainDumps() {
        jframe brainDumpsF = new jframe("Time Boxing - Brain Dumps");
        brainDumpsF.add(panel);

        //check saved tasks
        try (Socket client1 = new Socket("localhost", 8585)) {
            ObjectOutputStream out = new ObjectOutputStream(client1.getOutputStream());
            out.writeInt(4); //get tasks
            out.flush();

            out.writeObject(Main.filePath);
            out.flush();

            ObjectInputStream in = new ObjectInputStream(client1.getInputStream());
            int response1 = in.readInt();

            if (response1 == 1) {
                try {
                    String all = (String) in.readObject();
                    textArea.setText(all);
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }



        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String all = textArea.getText();
                if (!all.isEmpty()) {
                    try (Socket client = new Socket("localhost", 8585);) {
                        ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                        output.writeInt(3); //save tasks
                        output.flush();

                        output.writeObject(Main.filePath);
                        output.flush();

                        output.writeObject(all);
                        output.flush();

                        ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                        int response = input.readInt();
                        if (response == 1) {
                            brainDumpsF.dispose();
                            new Priorities();
                        }
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Enter at least one task", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                brainDumpsF.dispose();
                new HomeNull();
            }
        });
    }
}
