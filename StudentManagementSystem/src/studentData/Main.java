package studentData;

import java.util.List;
import java.util.Scanner;

public class Main {
	
	private static Scanner  sc = new Scanner(System.in);
	private static StudentDao dao = new StudentDao();
	
	public static void main(String[] args) {
		
		while(true)
		{
			System.out.println("========Student Management System==============");
			System.out.println("1. Add Student");
			System.out.println("2. View Students");
			System.out.println("3. View student by id");
			System.out.println("4. Update student");
			System.out.println("5. Delete Student");
			System.out.println("6. Exit");
			System.out.println("Enter your choice");
			
			
			int choice = sc.nextInt();
			sc.nextLine();
			
			switch(choice)
			{
			case 1 : 
				addStudent();
				break;
				
			case 2 : 
				viewAllStudents();
				break;
				
			case 3 :
				viewStudentById();
				break;
				
			case 4:
				updateStudent();
				break;
				
			case 5 :
				deleteStudent();
				break;
				
			case 6:
				System.out.println("Thank you ");
				sc.close();
				return;
//				
			default:
				System.out.println("Invalid choice Plrase try again...");
			}
		
		}
	}

	
	public static void addStudent()
	{
		System.out.println("Add new student");
		System.out.println("Enter name :");
		String name = sc.nextLine();
		
		System.out.println("Enter age :");
		int age = sc.nextInt();
		sc.nextLine();
		
		System.out.println("Enter course : ");
		String course = sc.nextLine();
		
		System.out.println("Enter city : ");
		String city = sc.nextLine();
		
		Student student = new Student(0, name, age, course, city);
		dao.addStudent(student);
	}
	
	
	public static void viewAllStudents()
	{
		System.out.println("All Students");
		List<Student> students = dao.getAllStudents();
		
		if(students.isEmpty())
		{
			System.out.println("No Student Found");
		}
		else {
			for(Student s : students)
			{
				System.out.println(s);
			}
		}
	}
	
	
	public static void viewStudentById()
	{
		System.out.println("Enter student ID : ");
		int id = sc.nextInt();
		Student s = dao.getStudentById(id);
		
		if(s!=null)
		{
			System.out.println(s);
		}
		else {
			System.out.println("Student not found");
		}
	}
	
	
	public static void updateStudent()
	{
		System.out.println("Enter Student id to update");
		int id = sc.nextInt();
		sc.nextLine();
		
		Student existing = dao.getStudentById(id);
		
		if(existing == null)
		{
			System.out.println("Student not found ");
			return;
		}
		
		System.out.println("Current details : "+existing);
		
		
		System.out.println("Enter new name : ");
		String name = sc.nextLine();
		
		System.out.println("Enter new age : ");
		int age = sc.nextInt();
		sc.nextLine();
		
		System.out.println("Enter new course : ");
		String course = sc.nextLine();
		
		System.out.println("Enter new city : ");
		String city = sc.nextLine();
		
		
		Student updateStudent =  new Student(id,name,age,course,city);
		dao.updateStudent(updateStudent);

	}
	
	
	public static void deleteStudent()
	{
		System.out.println("Enter student ID to delete : ");
		int id = sc.nextInt();
		dao.deleteStudent(id);
	}
}
