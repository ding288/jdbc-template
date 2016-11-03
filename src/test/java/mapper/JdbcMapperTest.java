package mapper;

import org.junit.Test;

import com.di.jdbc.template.JdbcMapper;

/**
 * @author di
 */
public class JdbcMapperTest {
	@Test
	public void test() {
		JdbcMapper jm=new JdbcMapper();
		jm.queryForMap("select * from person");
	}
}
