package studentData;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

class DBConnection {
	
	private static final String URL = "jdbc:mysql://localhost:3306/student_management_system";
	private static final String USERNAME = "root";
	private static final String PASSWORD = "root";
	
	public static Connection getConnection() throws SQLException {
		try {
			
			Class.forName("com.mysql.cj.jdbc.Driver");
		} catch (Exception e) {
			e.printStackTrace();
			
		}
		return DriverManager.getConnection(URL,USERNAME,PASSWORD);	
		}
	
}


