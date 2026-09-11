package studentData;


	import java.util.List;
	import java.util.Scanner;

	public class Main {

	    static Scanner sc = new Scanner(System.in);
	    static StudentFileDao dao = new StudentFileDao();

	    public static void main(String[] args) {

	        while (true) {

	            System.out.println("\n===== Student Management System =====");
	            System.out.println("1. Add Student");
	            System.out.println("2. View All Students");
	            System.out.println("3. Search Student");
	            System.out.println("4. Update Student");
	            System.out.println("5. Delete Student");
	            System.out.println("6. Exit");

	            System.out.print("Enter Choice : ");

	            int choice = sc.nextInt();

	            switch (choice) {

	                case 1:
	                    addStudent();
	                    break;

	                case 2:
	                    viewStudents();
	                    break;

	                case 3:
	                    searchStudent();
	                    break;

	                case 4:
	                    updateStudent();
	                    break;

	                case 5:
	                    deleteStudent();
	                    break;

	                case 6:
	                    System.exit(0);

	                default:
	                    System.out.println("Invalid Choice");
	            }

	        }

	    }

	    static void addStudent() {

	        System.out.print("Enter Id : ");
	        int id = sc.nextInt();

	        sc.nextLine();

	        System.out.print("Enter Name : ");
	        String name = sc.nextLine();

	        System.out.print("Enter Age : ");
	        int age = sc.nextInt();

	        sc.nextLine();

	        System.out.print("Enter Course : ");
	        String course = sc.nextLine();

	        System.out.print("Enter City : ");
	        String city = sc.nextLine();

	        dao.addStudent(new Student(id, name, age, course, city));

	    }

	    static void viewStudents() {

	        List<Student> list = dao.getAllStudents();

	        if (list.isEmpty()) {

	            System.out.println("No Students Found");
	        } else {

	            list.forEach(System.out::println);
	        }

	    }

	    static void searchStudent() {

	        System.out.print("Enter Student ID : ");

	        int id = sc.nextInt();

	        Student s = dao.getStudentById(id);

	        if (s != null)
	            System.out.println(s);
	        else
	            System.out.println("Student Not Found");

	    }

	    static void updateStudent() {

	        System.out.print("Enter Student ID : ");

	        int id = sc.nextInt();

	        sc.nextLine();

	        Student old = dao.getStudentById(id);

	        if (old == null) {

	            System.out.println("Student Not Found");

	            return;
	        }

	        System.out.print("Enter Name : ");
	        String name = sc.nextLine();

	        System.out.print("Enter Age : ");
	        int age = sc.nextInt();

	        sc.nextLine();

	        System.out.print("Enter Course : ");
	        String course = sc.nextLine();

	        System.out.print("Enter City : ");
	        String city = sc.nextLine();

	        dao.updateStudent(new Student(id, name, age, course, city));

	    }

	    static void deleteStudent() {

	        System.out.print("Enter Student ID : ");

	        int id = sc.nextInt();

	        dao.deleteStudent(id);

	    }

	}

