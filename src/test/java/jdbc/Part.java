package jdbc;

import com.di.jdbc.template.annotation.Column;
import com.di.jdbc.template.annotation.Id;
import com.di.jdbc.template.annotation.Table;

/** 
* @author  di: 
* @date 创建时间：2016年10月22日 上午12:51:30 
* @version
*/
@Table(name="part")
public class Part {
	@Id
	@Column(name="part_id")
	private Integer partId;
	@Column(name="part_name")
	private String partName;
	public Integer getPartId() {
		return partId;
	}
	public void setPartId(Integer partId) {
		this.partId = partId;
	}
	public String getPartName() {
		return partName;
	}
	public void setPartName(String partName) {
		this.partName = partName;
	}
	
}
