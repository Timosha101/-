import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseManager {
    private final List<Student> students;

    public DatabaseManager() {
        this.students = new ArrayList<>();
    }

    public List<Student> getAllStudents() {
        return new ArrayList<>(students);
    }

    public void addStudent(Student student) {
        students.add(student);
    }

    public void deleteStudent(String lastName) {
        students.removeIf(s -> s.getLastName().equals(lastName));
    }

    public Student findStudentByLastName(String lastName) {
        return students.stream()
                .filter(s -> s.getLastName().equals(lastName))
                .findFirst()
                .orElse(null);
    }

    public void updateStudent(Student updatedStudent) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getLastName().equals(updatedStudent.getLastName())) {
                students.set(i, updatedStudent);
                break;
            }
        }
    }

    public List<Student> searchStudents(String query) {
        if (query == null || query.isEmpty()) {
            return getAllStudents();
        }
        String lowercaseQuery = query.toLowerCase();
        return students.stream()
                .filter(s -> s.getLastName().toLowerCase().contains(lowercaseQuery) ||
                        s.getFirstName().toLowerCase().contains(lowercaseQuery) ||
                        s.getGroup().toLowerCase().contains(lowercaseQuery))
                .collect(Collectors.toList());
    }

}