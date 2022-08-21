import java.io.IOException;
import java.security.InvalidParameterException;

import org.iq80.leveldb.DB;


/** A general example of a data access layer */
public class DataAccess {
	public DB store = null;

	/**
	 * Initialize the object with the LevelDB database that will be used to store
	 * this data
	 * 
	 * @param store
	 */
	public DataAccess(DB store) {
		if (store == null) {
			throw new InvalidParameterException("Store can't be null");
		}
		this.store = store;
	}

	/**
	 * Store an object
	 * 
	 * @param o
	 * @throws IOException 
	 */
	public synchronized void put(DAO o) throws IOException {
		byte[] key = Serializer.serialize(o.getKey());
		byte[] value = Serializer.serialize(o.getValue());
		store.put(key, value);
	}

	/**
	 * Retrieve an object
	 * 
	 * @param key
	 * @return
	 */
	public synchronized DAO get(Object key) {
		try {
			byte[] k = Serializer.serialize(key);
			byte[] v = store.get(k);
			Object value = Serializer.deserializeToObject(v);
			return new DAO(key, value);
		} catch (IOException e) {
			return null;
		} catch (ClassNotFoundException e) {
			return null;
		}
	}

	void delete(String key) {
		if (key == null) {
			return;
		}
		byte[] k = Serializer.serialize(key);
		store.delete(k);
	}
}
