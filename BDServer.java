import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

public class BDServer {
    private static final Logger logger = Logger.getLogger(BDServer.class.getName());
    private static final int PORT = 5433;
    private final ExecutorService executorService;
    private volatile boolean running = true;
    private final DatabaseManager database;

    public BDServer() {
        this.executorService = Executors.newCachedThreadPool();
        this.database = new DatabaseManager();
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            logger.info("Server started on port " + PORT);

            while (running && !Thread.currentThread().isInterrupted()) {
                try {
                    Socket socket = serverSocket.accept();
                    logger.info("Client connected from: " + socket.getInetAddress());
                    executorService.submit(() -> handleClient(socket));
                } catch (IOException e) {
                    logger.log(Level.SEVERE, "Error accepting client connection", e);
                }
            }
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Server error", e);
        } finally {
            shutdown();
        }
    }

    private void handleClient(Socket socket) {
        try (ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            String command = (String) in.readObject();

            switch (command) {
                case "GET_ALL_STUDENTS":
                    List<Student> students = database.getAllStudents();
                    out.writeObject(students);
                    break;
                case "ADD_STUDENT":
                    Student student = (Student) in.readObject();
                    database.addStudent(student);
                    out.writeObject(database.getAllStudents());
                    break;
                case "DELETE_STUDENT":
                    String lastName = (String) in.readObject();
                    database.deleteStudent(lastName);
                    out.writeObject(database.getAllStudents());
                    break;
                case "EDIT_STUDENT":
                    String searchLastName = (String) in.readObject();
                    Student foundStudent = database.findStudentByLastName(searchLastName);
                    out.writeObject(foundStudent);
                    break;
                case "UPDATE_STUDENT":
                    Student updatedStudent = (Student) in.readObject();
                    database.updateStudent(updatedStudent);
                    out.writeObject(database.getAllStudents());
                    break;
                case "SEARCH":
                    String searchQuery = (String) in.readObject();
                    List<Student> foundStudents = database.searchStudents(searchQuery);
                    out.writeObject(foundStudents);
                    break;
                case "LOAD_FILE":
                    List<Student> loadedStudents = new ArrayList<>();
                    try {
                        while (true) {
                            Student studentFromFile = (Student) in.readObject();
                            database.addStudent(studentFromFile);
                            loadedStudents.add(studentFromFile);
                        }
                    } catch (EOFException e) {
                        // Достигнут конец файла
                    }
                    out.writeObject("Загружено " + loadedStudents.size() + " студентов из файла");
                    break;
                default:
                    logger.warning("Unknown command received: " + command);
                    break;
            }
        } catch (IOException | ClassNotFoundException e) {
            logger.log(Level.SEVERE, "Error handling client request", e);
        } finally {
            try {
                socket.close();
            } catch (IOException e) {
                logger.log(Level.SEVERE, "Error closing client socket", e);
            }
        }
    }

    public void shutdown() {
        running = false;
        executorService.shutdown();
        logger.info("Server shutdown completed");
    }

    public static void main(String[] args) {
        BDServer server = new BDServer();
        server.start();
    }
}