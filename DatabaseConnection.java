import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DatabaseConnection {
    private static final Logger logger = Logger.getLogger(DatabaseConnection.class.getName());
    private static final String URL = "jdbc:postgresql://localhost:5432/students_db";
    private static final String USER = "postgres";
    private static final String PASSWORD = "1234";

    public Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    public void addStudent(Student student) {
        String sql = "INSERT INTO students (first_name, last_name, study_group) VALUES (?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getFirstName());
            pstmt.setString(2, student.getLastName());
            pstmt.setString(3, student.getGroup());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error adding student", e);
        }
    }

    public List<Student> getAllStudents() {
        List<Student> students = new ArrayList<>();
        String sql = "SELECT * FROM students";

        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                Student student = new Student(
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("study_group")
                );
                students.add(student);
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error getting all students", e);
        }

        return students;
    }

    public void deleteStudent(String lastName) {
        String sql = "DELETE FROM students WHERE last_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, lastName);
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error deleting student", e);
        }
    }

    public void updateStudent(Student student) {
        String sql = "UPDATE students SET first_name = ?, study_group = ? WHERE last_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, student.getFirstName());
            pstmt.setString(2, student.getGroup());
            pstmt.setString(3, student.getLastName());
            pstmt.executeUpdate();

        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error updating student", e);
        }
    }

    public Student findStudentByLastName(String lastName) {
        String sql = "SELECT * FROM students WHERE last_name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, lastName);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return new Student(
                            rs.getString("first_name"),
                            rs.getString("last_name"),
                            rs.getString("study_group")
                    );
                }
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Error finding student", e);
        }
        return null;
    }
}