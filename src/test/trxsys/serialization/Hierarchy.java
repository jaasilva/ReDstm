package trxsys.serialization;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamException;
import java.io.Serializable;
import java.lang.ref.WeakReference;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

import org.deuce.distribution.replication.full.OID;
import org.deuce.distribution.replication.full.OID2Object;
import org.deuce.distribution.replication.full.OIDFactory;
import org.deuce.distribution.replication.full.oid.SimpleOIDFactory;

public class Hierarchy
{
	static final String FILE_NAME = "HIERARCHY";
	static Map<OID, WeakReference<UniqueObject>> map = Collections
			.synchronizedMap(new WeakHashMap<OID, WeakReference<UniqueObject>>());
	static final OIDFactory oidFactory = new SimpleOIDFactory();

	public static void putObject(OID oid, UniqueObject obj)
	{
		map.put(oid, new WeakReference<UniqueObject>(obj));

		// map.put(oid, obj);
	}

	public static UniqueObject getObject(OID oid)
	{
		WeakReference<UniqueObject> ref = map.get(oid);
		return (ref != null ? ref.get() : null);

		// return map.get(oid);
	}

	public static void main(String[] args) throws IOException,
			ClassNotFoundException
	{
		A o1 = new A();
		A o2;

		// Serialise.
		FileOutputStream fos = new FileOutputStream(FILE_NAME);
		ObjectOutputStream oos = new ObjectOutputStream(fos);
		oos.writeObject(o1);
		oos.close();

		// De-serialise.
		FileInputStream fis = new FileInputStream(FILE_NAME);
		ObjectInputStream ois = new ObjectInputStream(fis);
		o2 = (A) ois.readObject();

		System.out.println("After serialisation #1");
		System.out.printf("%s\n%s", o1, o2);
	}

	public static Object writeReplaceHook(UniqueObject obj)
	{
		OID oid = (OID) obj.getMetadata();
		if (oid == null)
		{
			oid = oidFactory.generateOID();
			obj.setMetadata(oid);
			putObject(oid, obj);
			return obj;
		}

		return new OID2Object(oid);
	}

	public static Object readResolveHook(UniqueObject obj)
	{
		OID oid = (OID) obj.getMetadata();

		UniqueObject object = getObject(oid);
		if (object != null)
		{
			return object;
		}

		putObject(oid, obj);
		return obj;
	}
}

class UniqueObject implements Serializable
{
	private static final long serialVersionUID = -2725068194283143294L;
	private Object metadata;

	public Object getMetadata()
	{
		return metadata;
	}

	public void setMetadata(Object metadata)
	{
		this.metadata = metadata;
	}

	protected Object writeReplace() throws ObjectStreamException
	{
		return Hierarchy.writeReplaceHook(this);
	}

	protected Object readResolve() throws ObjectStreamException
	{
		return Hierarchy.readResolveHook(this);
	}
}

class A extends UniqueObject
{
	private static final long serialVersionUID = -2443579556071247518L;
	int i = 1;

	@Override
	public String toString()
	{
		return super.toString() + "|" + i;
	}
}

class B extends A
{
	private static final long serialVersionUID = -1320095242338828927L;
	Object o = new SerializableObject();

	@Override
	public String toString()
	{
		return super.toString() + "|" + o;
	}
}

class SerializableObject implements Serializable
{
}
