package mapper;

import java.util.Date;

import com.di.jdbc.template.annotation.Column;
import com.di.jdbc.template.annotation.Id;
import com.di.jdbc.template.annotation.Sql;
import com.di.jdbc.template.annotation.Sqls;
import com.di.jdbc.template.annotation.Table;

/**
 * @author di
 */
@Table(name = "msg")
@Sqls(sqls = { @Sql(name = "selectAll", value = "select * from msg"),
		@Sql(name = "selectOne", value = "select * from msg where msg_id=?") })
public class Msg {
	@Id
	@Column(name = "msg_id")
	private Long msgId;
	@Column(name = "msg_name")
	private String msgName;
	@Column(name = "create_time")
	private Date createTime;
	private String remark;
	private int count;

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Long getMsgId() {
		return msgId;
	}

	public void setMsgId(Long msgId) {
		this.msgId = msgId;
	}

	public String getMsgName() {
		return msgName;
	}

	public void setMsgName(String msgName) {
		this.msgName = msgName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	@Override
	public String toString() {
		return "Msg [msgId=" + msgId + ", msgName=" + msgName + ", createTime=" + createTime + ", remark=" + remark
				+ ", count=" + count + "]";
	}

}
