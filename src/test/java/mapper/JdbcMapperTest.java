package mapper;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import org.junit.Test;
import com.di.jdbc.template.JdbcMapper;

/**
 * @author di
 */
public class JdbcMapperTest {

	@Test
	public void test() {
		// queryForMap();
		// queryForList();
		// queryForObject();
		// queryForSingleValue();
		// executeUpdate();
		// executeInsert();
		// updateSelective();
		// insertSelective();
		// prepareQuery();
		// prepareNamedQuery();
		 prepareInsert();
		// prepareUpdate();
		// prepareExecute();
	}

	public void queryForMap() {
		JdbcMapper jm = new JdbcMapper();
		List<HashMap<String, Object>> queryForMap = jm.queryForMap("select * from msg");
		System.out.println(queryForMap);
	}

	public void queryForList() {
		JdbcMapper jm = new JdbcMapper();
		List<Msg> list = jm.queryForList("select * from msg", Msg.class);
		for (Msg test : list) {
			System.out.println(test);
		}
	}

	public void queryForObject() {
		JdbcMapper jm = new JdbcMapper();
		Msg test = jm.queryForObject("select * from msg where msg_id=1", Msg.class);
		System.out.println(test);
	}

	public void queryForSingleValue() {
		JdbcMapper jm = new JdbcMapper();
		Integer count = jm.queryForSingleValue("select count(0) from msg", int.class);
		System.out.println(count);
	}

	public void executeUpdate() {
		JdbcMapper jm = new JdbcMapper();
		Msg t = jm.queryForObject("select * from test where msg_id=1", Msg.class);
		System.out.println(t);
		jm.executeUpdate("update test set remark='a' where msg_id=1");
		t = jm.queryForObject("select * from test where msg_id=1", Msg.class);
		System.out.println(t);
	}

	public void executeInsert() {
		JdbcMapper jm = new JdbcMapper();
		queryForSingleValue();
		jm.executeInsert("insert into msg(`msg_id`,`msg_name`,`remark`) values(null,'asa','insert')");
		queryForSingleValue();
	}

	public void updateSelective() {
		JdbcMapper jm = new JdbcMapper();
		Msg t = jm.queryForObject("select * from msg where msg_id=1", Msg.class);
		System.out.println(t);
		t.setRemark(t.getRemark() + "*");
		jm.updateSelective(t);
		t = jm.queryForObject("select * from msg where msg_id=1", Msg.class);
		System.out.println(t);
	}

	public void insertSelective() {
		JdbcMapper j = new JdbcMapper();
		Msg m = new Msg();
		m.setMsgName("insertSelective");
		j.insertSelective(m);
		System.out.println(m.getMsgId());
	}

	public void prepareQuery() {
		JdbcMapper j = new JdbcMapper();
		Object[] params = { 2 };
		List<Msg> list = j.prepareQuery("select * from msg where msg_id>?", Msg.class, params);
		for (Msg msg : list) {
			System.out.println(msg);
		}
	}

	public void prepareNamedQuery() {
		JdbcMapper j = new JdbcMapper();
		List<Msg> list = j.prepareNamedQuery("selectAll", Msg.class, null);
		for (Msg msg : list) {
			System.out.println(msg);
		}
	}

	public void prepareInsert() {
		JdbcMapper j = new JdbcMapper();
		long start = new Date().getTime();
		for (int i = 0; i < 100; i++) {
			Msg m = new Msg();
			m.setMsgName("prepareInsert");
			j.prepareInsert(m);
		}
		long end = new Date().getTime();
		System.out.println((end - start) + " ms");
	}

	public void prepareUpdate() {
		JdbcMapper j = new JdbcMapper();
		Msg m = j.queryForObject("select * from msg where msg_id=1", Msg.class);
		System.out.println(m);
		m.setCount(m.getCount() + 1);
		j.prepareUpdate(m);
		m = j.queryForObject("select * from msg where msg_id=1", Msg.class);
		System.out.println(m);
	}

	public void prepareExecute() {
		JdbcMapper j = new JdbcMapper();
		Object[] args = { 3, 1 };
		j.prepareExecute("update msg set count=? where msg_id=?", args);
		queryForObject();
	}
}
