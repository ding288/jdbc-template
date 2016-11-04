package mapper;

import java.util.Date;
import org.junit.Test;
import com.di.jdbc.template.JdbcMapper;
import com.di.jdbc.template.JdbcTemplate;

/**
* @author di
*/
public class JtTest {
	int count=10000;
	@Test
	public void test(){
		jt();
		jm();
	}
	public void jt() {
		JdbcTemplate j=new JdbcTemplate();
		long start = new Date().getTime();
		for (int i = 0; i < count; i++) {
			Msg m = new Msg();
			m.setMsgName("JdbcTemplate");
			j.prepareInsert(m); 
		}
		long end = new Date().getTime();
		System.out.println("JdbcTemplate : "+(end - start) + " ms");
	}
	
	public void jm() {
		JdbcMapper j = new JdbcMapper();
		long start = new Date().getTime();
		for (int i = 0; i < count; i++) {
			Msg m = new Msg();
			m.setMsgName("JdbcMapper");
			j.prepareInsert(m);
		}
		long end = new Date().getTime();
		System.out.println("JdbcMapper : "+(end - start) + " ms");
	}
	
	public static void main(String[] args) {
		JtTest jt=new JtTest();
		jt.test();
	}
}
