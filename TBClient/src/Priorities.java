import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Priorities {
    private JPanel panel;
    private JLabel step;
    private JLabel info;
    private JLabel info1;
    private JButton previousButton;
    private JButton nextButton;
    private JPanel TasksP;
    public static List<Task> tasks = new ArrayList<>();

    public Priorities() {
        jframe frame = new jframe("Time Boxing - Priorities");
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;

        tasks.clear();

        try (Socket client = new Socket("localhost", 8585)) {
            ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
            out.writeInt(4);
            out.flush();

            out.writeObject(Main.filePath);
            out.flush();

            ObjectInputStream in = new ObjectInputStream(client.getInputStream());
            int resp = in.readInt();
            if (resp == 1) {
                try {
                    String all = (String) in.readObject();
                    String[] allTasks = all.split("\n");
                    for (String t : allTasks) {
                        Task task = new Task(t);
                        task.setPriority(false); //Default
                        tasks.add(task);
                    }
                } catch (ClassNotFoundException e) {
                    throw new RuntimeException(e);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        for (Task task : tasks) {
            JToggleButton toggleButton = new JToggleButton(task.getDescription());
            toggleButton.setFocusable(false);
            TasksP.add(toggleButton, gbc);
            gbc.gridy++;
        }

        frame.add(panel);


        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new BrainDumps();
            }
        });

        nextButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                int checked = 0;
                List<JToggleButton> selectedButtons = new ArrayList<>();

                Component[] components = TasksP.getComponents();

                for (Component component : components) {
                    if (component instanceof JToggleButton) {
                        JToggleButton toggleButton = (JToggleButton) component;
                        if (toggleButton.isSelected()) {
                            selectedButtons.add(toggleButton);
                            checked++;
                        }
                    }
                }

                if (checked <= 3) {
                    String text;
                    for (JToggleButton button : selectedButtons) {
                        text = button.getText();
                        for (Task task : tasks) {
                            if (task.getDescription().equals(text)) {
                                task.setPriority(true);
                            }
                        }
                    }
                    frame.dispose();
                    new Planning();
                } else {
                    JOptionPane.showMessageDialog(null, "More than 3 priorities are selected!", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        });
    }
}
