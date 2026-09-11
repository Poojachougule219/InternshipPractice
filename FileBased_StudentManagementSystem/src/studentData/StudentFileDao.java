package studentData;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class StudentFileDao {
	
	private final String FILE_NAME = "Student.txt";

    // Add Student
    public void addStudent(Student student) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME, true))) {

            bw.write(student.getId() + "," +
                    student.getName() + "," +
                    student.getAge() + "," +
                    student.getCourse() + "," +
                    student.getCity());

            bw.newLine();

            System.out.println("Student Added Successfully");

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
    
    
    
 // View All Students
    public List<Student> getAllStudents() {

        List<Student> list = new ArrayList<>();

        try (BufferedReader br = new BufferedReader(new FileReader(FILE_NAME))) {

            String line;

            while ((line = br.readLine()) != null) {

                String arr[] = line.split(",");

                Student s = new Student(
                        Integer.parseInt(arr[0]),
                        arr[1],
                        Integer.parseInt(arr[2]),
                        arr[3],
                        arr[4]);

                list.add(s);
            }

        } catch (Exception e) {

        }

        return list;
    }

  
    
 // Search by ID
    public Student getStudentById(int id) {

        List<Student> list = getAllStudents();

        for (Student s : list) {

            if (s.getId() == id) {
                return s;
            }
        }

        return null;
    }

    
    
 // Update Student
    public void updateStudent(Student student) {

        List<Student> list = getAllStudents();

        boolean found = false;

        for (Student s : list) {

            if (s.getId() == student.getId()) {

                s.setName(student.getName());
                s.setAge(student.getAge());
                s.setCourse(student.getCourse());
                s.setCity(student.getCity());

                found = true;
                break;
            }
        }

        if (found) {

            saveAllStudents(list);
            System.out.println("Student Updated Successfully");

        } else {

            System.out.println("Student Not Found");
        }

    }


 // Delete Student
    public void deleteStudent(int id) {

        List<Student> list = getAllStudents();

        boolean removed = list.removeIf(s -> s.getId() == id);

        if (removed) {

            saveAllStudents(list);
            System.out.println("Student Deleted Successfully");

        } else {

            System.out.println("Student Not Found");
        }

    }

    
    
    private void saveAllStudents(List<Student> list) {

        try (BufferedWriter bw = new BufferedWriter(new FileWriter(FILE_NAME))) {

            for (Student s : list) {

                bw.write(s.getId() + "," +
                        s.getName() + "," +
                        s.getAge() + "," +
                        s.getCourse() + "," +
                        s.getCity());

                bw.newLine();
            }

        } catch (Exception e) {
            System.out.println(e.getMessage());
        }

    }
}




