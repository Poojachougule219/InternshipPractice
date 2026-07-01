package studentData;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;


public class StudentDao {
	
	
	//CREATE : 1. Add Student
	public void addStudent(Student student)
	{
		String sql = "INSERT INTO student (name,age,course,city) VALUES(?,?,?,?)";
		try(Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			
			ps.setString(1, student.getName());
			ps.setInt(2, student.getAge());
			ps.setString(3, student.getCourse());
			ps.setString(4, student.getCity());
			
			int i = ps.executeUpdate();
			if(i > 0)
			{
				System.out.println("Student Added Successfully");
			}
			
		} catch (Exception e) {
			System.out.println("Error when adding students :  "+e.getMessage());
		}
	}
	
	
	//2. READ -  View Students
	public List<Student> getAllStudents(){
		List<Student> students = new ArrayList<Student>();
		
		String sql = "SELECT * FROM student";
		
		try(Connection con = DBConnection.getConnection();
				Statement st = con.createStatement();
						ResultSet rs = st.executeQuery(sql)) {
			
			while(rs.next())
			{
				Student s = new Student(
						rs.getInt("id"),
						rs.getString("name"),
						rs.getInt("age"),
						rs.getString("course"),
						rs.getString("city")
						);
						
						students.add(s);
			}
			
		} catch (Exception e) {
			System.out.println("Error message when viewing students : "+e.getMessage());
		}
		return students;
	}
	
	
	//3. Get student by id
	public Student getStudentById(int id)
	{
		String sql = "SELECT * FROM student WHERE id = ?";
		
		try(Connection con = DBConnection.getConnection();
				PreparedStatement ps = con.prepareStatement(sql)) {
			
			ps.setInt(1, id);
			ResultSet rs = ps.executeQuery();
			
			if(rs.next())
			{
				return new Student(
						rs.getInt("id"),
						rs.getString("name"),
						rs.getInt("age"),
						rs.getString("course"),
						rs.getString("city")
						);
			}
			
		} catch (Exception e) {
			System.out.println("Error while searching student : "+e.getMessage());
		}
		
		return null;
	}
		
		//4 Update Student
		public void updateStudent(Student student)
		
		{
			String sql = "UPDATE student SET name=?,age=?,course=?,city=? WHERE id=?";
			try(Connection con = DBConnection.getConnection();
					PreparedStatement ps = con.prepareStatement(sql)) {
				
				ps.setString(1, student.getName());
				ps.setInt(2, student.getAge());
				ps.setString(3, student.getCourse());
				ps.setString(4, student.getCity());
				ps.setInt(5, student.getId());
				
				
				int i = ps.executeUpdate();
				if(i > 0)
				{
					System.out.println("Student updated successfully ");
				}
				else {
					{
						System.out.println("student not found");
					}
				}
				
				
				
			} catch (Exception e) {
				System.out.println("Error when updating students : "+e.getMessage());
			}
		
	}
		
		
		//5 DELETE Student
		public void deleteStudent(int id)
		{
			String sql = "DELETE from student WHERE id = ?";
			
			try(Connection con =  DBConnection.getConnection();
					PreparedStatement ps = con.prepareStatement(sql)) {
				
				ps.setInt(1, id);
				
				int i = ps.executeUpdate();
				
				if(i > 0)
				{
					System.out.println("Student deleted successfully");
				}
				else {
					System.out.println("Student not found");
				}
				
			}catch (Exception e) {
				System.out.println("Error when deleting students : "+e.getMessage());
			}
				
			
		}
	

}
