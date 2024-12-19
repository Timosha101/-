import java.io.Serial;
import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Student implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    private static final Logger logger = Logger.getLogger(Student.class.getName());

    private String firstName;
    private String lastName;
    private String group;

    // Добавляем константы для подключения к БД
    private static final String DB_URL = "jdbc:mysql://localhost:3306/your_database";
    private static final String DB_USER = "your_username";
    private static final String DB_PASSWORD = "your_password";

    public Student() {
        // Пустой конструктор для сериализации
    }

    public Student(String firstName, String lastName, String group) {
        this.firstName = firstName;
        this.lastName = lastName;
        this.group = group;
    }

    // Геттеры и сеттеры
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    // Метод для получения подключения к БД
    private static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    public void save() {
        try (Connection conn = getConnection()) {
            String sql = "INSERT INTO students (first_name, last_name, study_group) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, firstName);
                stmt.setString(2, lastName);
                stmt.setString(3, group);
                stmt.executeUpdate();
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error saving student", e);
            throw new RuntimeException("Error saving student", e);
        }
    }

    public static ArrayList<Student> loadAll() {
        ArrayList<Student> students = new ArrayList<>();
        try (Connection conn = getConnection()) {
            String sql = "SELECT first_name, last_name, study_group FROM students";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Student student = new Student(
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("study_group")
                    );
                    students.add(student);
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error loading students", e);
            throw new RuntimeException("Error loading students", e);
        }
        return students;
    }
}