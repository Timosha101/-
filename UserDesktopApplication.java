import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class UserDesktopApplication extends JFrame {
    private static final Logger logger = Logger.getLogger(UserDesktopApplication.class.getName());
    private static final int SERVER_PORT = 5433;

    private final JTextField firstNameField;
    private final JTextField lastNameField;
    private final JTextField groupField;
    private final JTextArea outputArea;

    public UserDesktopApplication() {
        setTitle("Управление студентами");
        setSize(1200, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Используем BorderLayout для главного окна
        setLayout(new BorderLayout());

        // Панель для ввода данных
        JPanel inputPanel = new JPanel(new GridLayout(4, 2, 5, 5));
        // Панель для кнопок
        JPanel buttonPanel = new JPanel(new FlowLayout());

        firstNameField = new JTextField(20);
        lastNameField = new JTextField(20);
        groupField = new JTextField(20);
        outputArea = new JTextArea();
        outputArea.setEditable(false);

        initializeComponents(inputPanel, buttonPanel);

        // Добавляем панели в главное окно
        add(inputPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.CENTER);
        add(new JScrollPane(outputArea), BorderLayout.SOUTH);

        // Показываем список студентов при запуске
        showAllStudents();

        setVisible(true);
    }

    private void initializeComponents(JPanel inputPanel, JPanel buttonPanel) {
        // Добавляем поля ввода
        inputPanel.add(new JLabel("Имя:"));
        inputPanel.add(firstNameField);
        inputPanel.add(new JLabel("Фамилия:"));
        inputPanel.add(lastNameField);
        inputPanel.add(new JLabel("Группа:"));
        inputPanel.add(groupField);

        // Создаем и настраиваем кнопки
        JButton addButton = new JButton("Добавить студента");
        JButton showAllButton = new JButton("Показать всех студентов");
        JButton editButton = new JButton("Редактировать данные студента");
        JButton deleteButton = new JButton("Удалить студента");

        // Добавляем слушатели событий
        addButton.addActionListener(e -> addStudentToServer());
        showAllButton.addActionListener(e -> showAllStudents());
        deleteButton.addActionListener(e -> deleteStudentByLastNameFromServer());
        editButton.addActionListener(e -> editStudentData());

        // Добавляем кнопки на панель
        buttonPanel.add(addButton);
        buttonPanel.add(showAllButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
    }

    private void addStudentToServer() {
        if (validateInputFields()) {
            Student student = new Student();
            student.setFirstName(firstNameField.getText().trim());
            student.setLastName(lastNameField.getText().trim());
            student.setGroup(groupField.getText().trim());

            try (Socket socket = new Socket("localhost", SERVER_PORT);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject("ADD_STUDENT");
                out.writeObject(student);

                clearInputFields();
                updateStudentList(in);
                JOptionPane.showMessageDialog(this, "Студент успешно добавлен!");

            } catch (IOException | ClassNotFoundException ex) {
                logger.log(Level.SEVERE, "Ошибка при добавлении студента", ex);
                JOptionPane.showMessageDialog(this, "Ошибка при добавлении студента: " + ex.getMessage());
            }
        }
    }

    private boolean validateInputFields() {
        if (firstNameField.getText().trim().isEmpty() ||
                lastNameField.getText().trim().isEmpty() ||
                groupField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Все поля должны быть заполнены!");
            return false;
        }
        return true;
    }

    private void showAllStudents() {
        try (Socket socket = new Socket("localhost", SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("GET_ALL_STUDENTS");
            updateStudentList(in);

        } catch (IOException | ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Ошибка при получении списка студентов", ex);
            JOptionPane.showMessageDialog(this, "Ошибка при получении списка студентов: " + ex.getMessage());
        }
    }

    private void editStudentData() {
        String lastName = JOptionPane.showInputDialog(this, "Введите фамилию студента для редактирования:");
        if (lastName != null && !lastName.trim().isEmpty()) {
            try (Socket socket = new Socket("localhost", SERVER_PORT);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject("EDIT_STUDENT");
                out.writeObject(lastName.trim());

                Object response = in.readObject();
                if (response instanceof Student) {
                    Student student = (Student) response;
                    if (showEditDialog(student)) {
                        updateStudentOnServer(student);
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Студент с фамилией " + lastName + " не найден");
                }

            } catch (IOException | ClassNotFoundException ex) {
                logger.log(Level.SEVERE, "Ошибка при редактировании студента", ex);
                JOptionPane.showMessageDialog(this, "Ошибка при редактировании студента: " + ex.getMessage());
            }
        }
    }

    private boolean showEditDialog(Student student) {
        JTextField editFirstName = new JTextField(student.getFirstName());
        JTextField editLastName = new JTextField(student.getLastName());
        JTextField editGroup = new JTextField(student.getGroup());

        JPanel panel = new JPanel(new GridLayout(3, 2, 5, 5));
        panel.add(new JLabel("Имя:"));
        panel.add(editFirstName);
        panel.add(new JLabel("Фамилия:"));
        panel.add(editLastName);
        panel.add(new JLabel("Группа:"));
        panel.add(editGroup);

        int result = JOptionPane.showConfirmDialog(this, panel,
                "Редактирование данных студента", JOptionPane.OK_CANCEL_OPTION);

        if (result == JOptionPane.OK_OPTION) {
            student.setFirstName(editFirstName.getText().trim());
            student.setLastName(editLastName.getText().trim());
            student.setGroup(editGroup.getText().trim());
            return true;
        }
        return false;
    }

    private void updateStudentOnServer(Student student) {
        try (Socket socket = new Socket("localhost", SERVER_PORT);
             ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
             ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

            out.writeObject("UPDATE_STUDENT");
            out.writeObject(student);
            updateStudentList(in);
            JOptionPane.showMessageDialog(this, "Данные студента успешно обновлены!");

        } catch (IOException | ClassNotFoundException ex) {
            logger.log(Level.SEVERE, "Ошибка при обновлении данных студента", ex);
            JOptionPane.showMessageDialog(this, "Ошибка при обновлении данных студента: " + ex.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void updateStudentList(ObjectInputStream in) throws IOException, ClassNotFoundException {
        Object received = in.readObject();
        if (received instanceof List<?>) {
            List<Student> students = (List<Student>) received;
            outputArea.setText("");
            StringBuilder sb = new StringBuilder();
            sb.append("Список всех студентов:\n\n");
            for (Student s : students) {
                sb.append(String.format("Имя: %s, Фамилия: %s, Группа: %s\n",
                        s.getFirstName(), s.getLastName(), s.getGroup()));
            }
            outputArea.setText(sb.toString());
        }
    }

    private void deleteStudentByLastNameFromServer() {
        String lastName = JOptionPane.showInputDialog(this, "Введите фамилию студента для удаления:");
        if (lastName != null && !lastName.trim().isEmpty()) {
            try (Socket socket = new Socket("localhost", SERVER_PORT);
                 ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                 ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {

                out.writeObject("DELETE_STUDENT");
                out.writeObject(lastName.trim());
                updateStudentList(in);
                JOptionPane.showMessageDialog(this, "Студент успешно удален!");

            } catch (IOException | ClassNotFoundException ex) {
                logger.log(Level.SEVERE, "Ошибка при удалении студента", ex);
                JOptionPane.showMessageDialog(this, "Ошибка при удалении студента: " + ex.getMessage());
            }
        }
    }

    private void clearInputFields() {
        firstNameField.setText("");
        lastNameField.setText("");
        groupField.setText("");
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(UserDesktopApplication::new);
    }
}