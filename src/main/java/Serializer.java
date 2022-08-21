import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.charset.Charset;
import java.security.InvalidParameterException;

/**
 * Utility methods for serializing different types to/from bytes. When byte
 * arrays are used types, type information is lost by the JVM.
 * 
 * @author djp3
 *
 */
public class Serializer {
	/**
	 * Convert a Long to a byte array
	 * 
	 * @param x
	 * @return
	 */
	public static byte[] serialize(Long x) {
		byte[] result = new byte[Long.SIZE / Byte.SIZE];
		if (x == null) {
			return new byte[0];
		}
		long l = x.longValue();
		for (int i = 7; i >= 0; i--) {
			result[i] = (byte) (l & 0xFF);
			l >>= Byte.SIZE;
		}
		return result;
	}

	/**
	 * Convert a byte array to a Long.
	 * @param bytes
	 * @return
	 */
	public static Long deserializeToLong(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		if (bytes.length == 0) {
			return null;
		}
		if(bytes.length != Long.SIZE/Byte.SIZE){
			throw new InvalidParameterException("Wrong number of bytes for a long");
		}
	    long result = 0;
	    for (int i = 0; i < Long.SIZE/Byte.SIZE; i++) {
		    result <<= Byte.SIZE;
		    result |= (bytes[i] & 0xFF);
	    }
	    return result;
	}

	/**
	 * Convert a String to a byte array
	 * 
	 * @param s
	 * @return
	 * @throws IOException
	 */
	public static byte[] serialize(String s) {
		if (s == null) {
			return new byte[0];
		}
		return s.getBytes(Charset.forName("UTF-8"));
	}

	/**
	 * Convert a byte array to a String
	 * 
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static String deserializeToString(byte[] bytes) {
		if (bytes == null) {
			return null;
		}
		String r = new String(bytes, Charset.forName("UTF-8"));
		return r;
	}

	/* Some code to make sure we can make a ByteArrayOutputStream */
	static boolean first = true;
	static ByteArrayOutputStream baos = null;

	private static void checkStreams() throws IOException {
		if (first) {
			baos = new ByteArrayOutputStream();
			first = false;
		}
	}

	/**
	 * Convert a generic object into a byte array
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public static byte[] serialize(Object obj) throws IOException {
		if (obj == null) {
			return new byte[0];
		}
		checkStreams();
		try (ObjectOutputStream oos = new ObjectOutputStream(baos)) {
			oos.writeObject(obj);
		}
		byte[] ret = baos.toByteArray();
		baos.reset();
		return ret;
	}

	/**
	 * Convert a byte array into some kind of Object
	 * 
	 * @param bytes
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static Object deserializeToObject(byte[] bytes) throws IOException, ClassNotFoundException {
		if (bytes == null) {
			return null;
		}
		if (bytes.length == 0) {
			return null;
		}
		try (ByteArrayInputStream b = new ByteArrayInputStream(bytes)) {
			try (ObjectInputStream o = new ObjectInputStream(b)) {
				return o.readObject();
			}
		}
	}
}