package conexao_SQL;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Conexao_SQL {

	public static Connection run_connection() throws SQLException{
		
		try {
			
			Class.forName("com.mysql.cj.jdbc.Driver");
			
			return DriverManager.getConnection("jdbc:mysql://localhost/pessoa_fisica","root", "");
			
		} catch (ClassNotFoundException e) {
			
			throw new SQLException(e.getException());
		}
		
	}
	
}
