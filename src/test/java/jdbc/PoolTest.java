//package jdbc;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//
//import org.junit.Test;
//
//import com.di.jdbc.connection.ConnectionPool;
//
///**
// * @author di
// */
//public class PoolTest {
//	@Test
//	public void test() {
//		String driver = "com.mysql.jdbc.Driver";
//		String url = "jdbc:mysql://localhost:3306/test?useUnicode=true&amp;characterEncoding=UTF-8&useSSL=true";
//		String user = "root";
//		String password = "root";
//		ConnectionPool connPool = new ConnectionPool(driver, url, user, password);
//		try {
//			connPool.createPool();
//		} catch (Exception ex) {
//			ex.printStackTrace();
//		}
//		try {
//			Connection conn = connPool.getConnection();
//			conn.isValid(10);
//		} catch (SQLException ex1) {
//			ex1.printStackTrace();
//		}
//	}
//}
