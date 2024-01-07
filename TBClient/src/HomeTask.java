import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class HomeTask {
    private JPanel panel;
    private JLabel welcome;
    private JLabel date;
    private JPanel tasksP;
    private List<Task> todayTasks = new ArrayList<>();

    public HomeTask() {
        jframe frame = new jframe("Time Boxing");
        frame.add(panel);

        tasksP.setLayout(new GridLayout(0, 1));

        welcome.setText("Welcome Back, " + Main.name + "!");

        //Date
        LocalDate currentDate = LocalDate.now();
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String todayDate = currentDate.format(dateFormatter);
        date.setText(todayDate); //set today date in GUI

        //Get Time Boxed tasks for today
        try (Socket client = new Socket("localhost", 8585)) {
            ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
            output.writeInt(6);
            output.flush();

            output.writeObject(Main.filePath);
            output.flush();

            ObjectInputStream input = new ObjectInputStream(client.getInputStream());
            int response = input.readInt();

            if (response == 1) {
                int size = input.readInt();

                for (int i = 0; i < size; i++) {
                    try {
                        todayTasks.add((Task) input.readObject());
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }

                todayTasks.sort(Comparator.comparing(Task::getStart));

            } else if (response == 0) {
                frame.dispose();
                new HomeNull(); //No Tasks for today ==> HomeNull
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (Task task : todayTasks) {
            JLabel taskLabel = createTaskLabel(task);
            tasksP.add(taskLabel);
        }

        //Refresh frame every 10 seconds
        Timer timer = new Timer(10000, new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                updateLabels();
            }
        });
        timer.start();

    }

    private JLabel createTaskLabel(Task task) {
        JLabel taskL = new JLabel(getTaskLabel(task));
        updateLabelStyle(taskL, task);
        return taskL;
    }

    private void updateLabels() {
        Component[] components = tasksP.getComponents();
        for (Component component : components) {
            if (component instanceof JLabel) {
                JLabel taskLabel = (JLabel) component;
                Task task = findTaskByName(taskLabel.getText());
                if (task != null) {
                    updateLabelStyle(taskLabel, task);
                }
            }
        }
    }

    private Task findTaskByName(String text) {
        for (Task task : todayTasks) {
            String taskText = getTaskLabel(task);
            if (taskText.equals(text)) {
                return task;
            }
        }
        return null;
    }

    private String getTaskLabel(Task task) {
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");
        String startTime = task.getStart().format(timeFormatter);
        String endTime = task.getEnd().format(timeFormatter);
        return task.getDescription() + "  (" + startTime + " - " + endTime + ")  ";
    }

    private void updateLabelStyle(JLabel taskLabel, Task task) {
        //Update Label Time Based on Current Time
        LocalDateTime nowTime = LocalDateTime.now();

        if (nowTime.isAfter(task.getEnd())) { //Task Time passed
            taskLabel.setForeground(Color.gray);
            taskLabel.setFont(taskLabel.getFont().deriveFont(Font.PLAIN));
        } else if ((nowTime.isAfter(task.getStart()) || nowTime.isEqual(task.getStart())) && (nowTime.isBefore(task.getEnd()) || nowTime.isEqual(task.getEnd()))) { //Task in progress
            taskLabel.setForeground(Color.BLACK);
            taskLabel.setFont(taskLabel.getFont().deriveFont(Font.BOLD));
        } else {
            taskLabel.setForeground(Color.black);
            taskLabel.setFont(taskLabel.getFont().deriveFont(Font.PLAIN));
        }
    }
}
