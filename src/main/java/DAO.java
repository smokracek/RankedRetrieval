import java.io.Serializable;

@SuppressWarnings("serial")
public class DAO implements Serializable {
	
	private Object key;
	private Object value;
	
	public DAO(Object key, Object value) {
		this.key = key;
		this.value = value;
	}

	public Object getKey() {
		return key;
	}

	public Object getValue() {
		return value;
	}
}
