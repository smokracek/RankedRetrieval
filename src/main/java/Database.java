import org.iq80.leveldb.*;

import static org.iq80.leveldb.impl.Iq80DBFactory.*;
import java.io.*;

public class Database {
	
	DB db;
	
	public Database(String name) {
		Options options = new Options();
		options.createIfMissing(true);
		try {
			db = factory.open(new File("/Users/sammokracek/Desktop/cs128/RankedRetrieval/" + name), options);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
