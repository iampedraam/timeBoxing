import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Planning {
    private JPanel panel;
    private JLabel info;
    private JLabel info1;
    private JButton previousButton;
    private JButton doneButton;
    private JPanel tasksP;

    public Planning() {
        jframe frame = new jframe("Time Boxing - Planning");
        frame.add(panel);


        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        for (Task task : Priorities.tasks) {
            if (task.getPriority()) {
                JButton button = createTaskButton(task);
                button.setFocusable(false);
                tasksP.add(button, gbc);
                gbc.gridy++;
            }
        }

        GridBagConstraints gbc2 = new GridBagConstraints();
        gbc2.gridx = 0;
        gbc2.gridy = gbc.gridy;
        for (Task task : Priorities.tasks) {
            if (!task.getPriority()) {
                JButton button = createTaskButton(task);
                button.setFocusable(false);
                tasksP.add(button, gbc2);
                gbc2.gridy++;
            }
        }


        previousButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.dispose();
                new Priorities();
            }
        });

        doneButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                boolean correct = false;

                for (Task task : Priorities.tasks) {
                    if (task.getStart() != null && task.getEnd() != null) {
                        correct = true;
                        break;
                    }
                }

                if (correct) {
                    try (Socket client = new Socket("localhost", 8585)) {
                        ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                        output.writeInt(5);
                        output.flush();

                        output.writeObject(Main.filePath);
                        output.flush();

                        //Only save tasks that are for today
                        List<Task> todayTasks = new ArrayList<>();
                        for (Task task : Priorities.tasks) {
                            if (task.getStart() != null && task.getEnd() != null) {
                                todayTasks.add(task);
                            }
                        }

                        output.writeInt(todayTasks.size());
                        output.flush();

                        for (Task task : todayTasks) {
                            output.writeObject(task);
                            output.flush();
                        }

                        ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                        int res = input.readInt();
                        if (res == 1) {
                            JOptionPane.showMessageDialog(null, "Time Boxing Successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                            frame.dispose();
                            new HomeTask();
                        }

                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    }
                } else {
                    JOptionPane.showMessageDialog(null, "Time Box at least one task!", "Error", JOptionPane.ERROR_MESSAGE);
                }

            }
        });
    }


    public JButton createTaskButton(Task task) {
        JButton button = new JButton(getTaskButtonText(task));
        JTextField startTimeField = new JTextField(5); // Adjust the field size as needed
        JTextField endTimeField = new JTextField(5); // Adjust the field size as needed

        button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // Open a dialog to set start and end times
                JPanel inputPanel = new JPanel(new GridLayout(3, 2));
                inputPanel.add(new JLabel("Start Time (HH:mm):"));
                inputPanel.add(startTimeField);
                inputPanel.add(new JLabel("End Time (HH:mm):"));
                inputPanel.add(endTimeField);

                int result = JOptionPane.showConfirmDialog(null, inputPanel, "Set Time for Task", JOptionPane.OK_CANCEL_OPTION);

                if (result == JOptionPane.OK_OPTION) {
                    String startTimeText = startTimeField.getText();
                    String endTimeText = endTimeField.getText();

                    // Parse user input into LocalTime (time only)
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
                    LocalTime startTime = LocalTime.parse(startTimeText, formatter);
                    LocalTime endTime = LocalTime.parse(endTimeText, formatter);

                    // Set the date to today
                    LocalDateTime today = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0).withNano(0);
                    LocalDateTime start = today.with(startTime);
                    LocalDateTime end = today.with(endTime);

                    // Check for conflicts with other tasks
                    if (!isTimeConflict(task, start, end)) {
                        task.setStart(start);
                        task.setEnd(end);
                        button.setText(getTaskButtonText(task));
                    } else {
                        JOptionPane.showMessageDialog(null, "Time conflict with existing tasks!", "Error", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        });

        return button;
    }

    private boolean isTimeConflict(Task currentTask, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        for (Task task : Priorities.tasks) {
            if (!task.equals(currentTask) && isOverlap(task, newStartTime, newEndTime)) {
                return true;
            }
        }
        return false;
    }

    private boolean isOverlap(Task task, LocalDateTime newStartTime, LocalDateTime newEndTime) {
        LocalDateTime taskStartTime = task.getStart();
        LocalDateTime taskEndTime = task.getEnd();

        if (taskStartTime != null && taskEndTime != null) {
            return (newStartTime.isBefore(taskEndTime) || newStartTime.isEqual(taskEndTime)) && (newEndTime.isAfter(taskStartTime) || newEndTime.isEqual(taskStartTime));
        }
        return false;
    }

    private String getTaskButtonText(Task task) {
        //Put time next to task

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm");
        if (task.getStart() != null && task.getEnd() != null) {
            String startTime = task.getStart().format(formatter);
            String endTime = task.getEnd().format(formatter);
            return task.getDescription() + " (" + startTime + " - " + endTime + ")";
        } else {
            return task.getDescription();
        }
    }
}
