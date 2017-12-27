package org.hw.sml.tools;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.Serializable;

public class SerializationUtils {
	public static Object clone(Serializable object) {
		return deserialize(serialize(object));
	}

	public static void serialize(Serializable obj, OutputStream outputStream) {
		Assert.notNull(obj, "The OutputStream must not be null");
		ObjectOutputStream out = null;
		try {
			out = new ObjectOutputStream(outputStream);
			out.writeObject(obj);
		} catch (Exception ex) {
		} finally {
			IOUtils.safeClose(out);
		}
	}

	public static byte[] serialize(Serializable obj) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream(512);
		serialize(obj, baos);
		return baos.toByteArray();
	}

	public static Object deserialize(InputStream inputStream) {
		Assert.notNull(inputStream, "The InputStream must not be null");
		ObjectInputStream in = null;
		try {
			in = new ObjectInputStream(inputStream);
			return in.readObject();
		}catch (Exception e) {
			e.printStackTrace();
		} finally {
			IOUtils.safeClose(in);
		}
		return null;
	}

	public static Object deserialize(byte[] objectData) {
		Assert.notNull(objectData, "The byte[] must not be null");
		ByteArrayInputStream bais = new ByteArrayInputStream(objectData);
		return deserialize(bais);
	}
}
