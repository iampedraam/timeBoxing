import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.DateTimeException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Server {
    public static void main(String[] args) {
        try (ServerSocket server = new ServerSocket(8585)) {
            while (true) {
                Socket client = server.accept();
                System.out.println("Client Connected...");
                ObjectInputStream input = new ObjectInputStream(client.getInputStream());
                ObjectOutputStream output = new ObjectOutputStream(client.getOutputStream());
                int order = input.readInt();
                System.out.println("Order: " + order);

                switch (order) {
                    case 1: //Login
                        try {
                            System.out.println("Login Check...");
                            String username = (String) input.readObject();
                            String filePath = username + ".txt";
                            String pass = (String) input.readObject();
                            File file = new File(filePath);
                            if (file.exists()) {
                                FileReader reader = new FileReader(filePath);
                                BufferedReader bf = new BufferedReader(reader);
                                String[] data = bf.readLine().split(",");
                                if (pass.equals(data[2])) {
                                    output.writeInt(1);
                                    output.flush();
                                    output.writeObject(data[0]);
                                    output.flush();
                                    System.out.println("User " + data[0] + " Logged in...");
                                } else {
                                    output.writeInt(0);
                                    output.flush();
                                    System.out.println("Wrong Pass!");
                                }
                            } else {
                                output.writeInt(0);
                                output.flush();
                                System.out.println("User not found!");
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 2: //Signup
                        try {
                            System.out.println("New user...");
                            String username = (String) input.readObject();
                            String path = username + ".txt";
                            String pass = (String) input.readObject();
                            String name = (String) input.readObject();
                            File file = new File(path);
                            if (!file.exists()) {
                                FileWriter writer = new FileWriter(path);
                                writer.write(name + "," + username + "," + pass + "\n");
                                writer.close();
                                output.writeInt(1);
                                output.flush();
                                System.out.println("New user created (" + username + ")");
                            } else {
                                output.writeInt(0);
                                output.flush();
                                System.out.println("Username already available!");
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 3: // save tasks
                        try {
                            System.out.println("Saving Tasks...");
                            String filePath = (String) input.readObject();
                            String all = (String) input.readObject();

                            boolean existingTasks = false;
                            StringBuilder existingContent = new StringBuilder();

                            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    if (!line.equals("All Tasks:")) {
                                        existingContent.append(line).append("\n");
                                    } else {
                                        existingTasks = true;
                                        break;
                                    }
                                }
                            }

                            if (existingTasks) {
                                try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath)))) {
                                    writer.print(existingContent);
                                    writer.println("All Tasks:");
                                    writer.println(all);
                                    writer.print("End");
                                }
                            } else { // If no existing tasks, simply write the new tasks
                                try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath, true)))) {
                                    writer.println("All Tasks:");
                                    writer.println(all);
                                    writer.print("End");
                                }
                            }

                            output.writeInt(1);
                            output.flush();
                            System.out.println("Tasks saved to " + filePath);
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 4: //get tasks
                        try {
                            System.out.println("Getting all tasks...");
                            String filePath = (String) input.readObject();
                            BufferedReader reader = new BufferedReader(new FileReader(filePath));
                            String line;
                            boolean savedTasks = false;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("All Tasks:")) {
                                    savedTasks = true;
                                    System.out.println("Found tasks...");
                                    break;
                                }
                            }

                            if (!savedTasks) {
                                output.writeInt(0);
                                output.flush();
                                System.out.println("No saved tasks found!");
                            } else {
                                output.writeInt(1);
                                output.flush();

                                StringBuilder all = new StringBuilder();
                                while ((line = reader.readLine()) != null) {
                                    if (line.equals("End")) {
                                        break;
                                    } else {
                                        all.append(line).append("\n");
                                    }
                                }

                                output.writeObject(all.toString());
                                output.flush();
                                System.out.println("Tasks sent to client...");
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 5: //Save time box
                        try {
                            System.out.println("Saving Time Box...");
                            String filePath = (String) input.readObject();
                            List<Task> tasks = new ArrayList<>();

                            int size = input.readInt();
                            for (int i = 0; i < size; i++) {
                                Task task = (Task) input.readObject();
                                tasks.add(task);
                            }

                            System.out.println("Got tasks from client...");

                            StringBuilder existingContent = new StringBuilder();
                            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                                String line;
                                while ((line = reader.readLine()) != null) {
                                    existingContent.append(line).append("\n");
                                    if (line.startsWith("All Tasks:")) {
                                        while (!(line = reader.readLine()).equals("End")) {
                                            boolean taskFound = false;
                                            for (Task task : tasks) {
                                                if (task.getDescription().equals(line)) {
                                                    taskFound = true;
                                                    break;
                                                }
                                            }
                                            if (!taskFound) {
                                                existingContent.append(line).append("\n");
                                            }
                                        }
                                        existingContent.append("End").append("\n");
                                    }
                                }
                            }


                            try (PrintWriter writer = new PrintWriter(new BufferedWriter(new FileWriter(filePath)))) {
                                writer.print(existingContent);

                                writer.println("Time Boxing:");
                                for (Task task : tasks) {
                                    writer.println(task.getDescription() + " - " + task.getStart() + " - " + task.getEnd());
                                }
                                writer.print("End of time box");
                            }

                            System.out.println("Time Boxes saved");

                            output.writeInt(1);
                            output.flush();
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                        break;
                    case 6:
                        try {
                            String filePath = (String) input.readObject();
                            BufferedReader reader = new BufferedReader(new FileReader(filePath));
                            boolean timeBoxFound = false;
                            List<Task> tasks = new ArrayList<>();

                            String line;
                            while ((line = reader.readLine()) != null) {
                                if (line.startsWith("Time Boxing:")) {
                                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm");

                                    while (!(line = reader.readLine()).startsWith("End of time box")) {
                                        String[] taskData = line.split(" - ");

                                        LocalDateTime start = LocalDateTime.parse(taskData[1], formatter);
                                        LocalDateTime end = LocalDateTime.parse(taskData[2], formatter);
                                        LocalDateTime today = LocalDateTime.now();

                                        if (start.getDayOfYear() == today.getDayOfYear() && end.getDayOfYear() == today.getDayOfYear()) {
                                            timeBoxFound = true;
                                            Task task = new Task(taskData[0]);
                                            task.setStart(start);
                                            task.setEnd(end);
                                            tasks.add(task);
                                        }
                                    }
                                }
                            }

                            if (timeBoxFound) {
                                output.writeInt(1);
                                output.flush();

                                System.out.println("Time Box for today found");

                                int size = tasks.size();
                                output.writeInt(size);
                                output.flush();

                                for (Task task : tasks) {
                                    output.writeObject(task);
                                    output.flush();
                                }

                                System.out.println("Tasks sent to client");
                            } else {
                                output.writeInt(0);
                                output.flush();
                                System.out.println("No Time Box for today found!");
                            }
                        } catch (ClassNotFoundException e) {
                            throw new RuntimeException(e);
                        }
                    default:
                        break;
                }

                client.close();
            }

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}