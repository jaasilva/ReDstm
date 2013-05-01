package org.deuce.transaction;

import org.deuce.LocalMetadata;
import org.deuce.objectweb.asm.Type;
import org.deuce.reflection.AddressUtil;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.array.BooleanArrayContainer;
import org.deuce.transform.localmetadata.array.ByteArrayContainer;
import org.deuce.transform.localmetadata.array.CharArrayContainer;
import org.deuce.transform.localmetadata.array.DoubleArrayContainer;
import org.deuce.transform.localmetadata.array.FloatArrayContainer;
import org.deuce.transform.localmetadata.array.IntArrayContainer;
import org.deuce.transform.localmetadata.array.LongArrayContainer;
import org.deuce.transform.localmetadata.array.MultiArrayContainer;
import org.deuce.transform.localmetadata.array.ObjectArrayContainer;
import org.deuce.transform.localmetadata.array.ShortArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * Cluster static delegate methods. These methods delegates calls from the
 * dynamic generated code to the context.
 * 
 * 
 * @author Guy Korland
 * @since 1.0
 * 
 */
public class ContextDelegator
{

	final static public String TXFIELD_DESCRIPTOR = Type
			.getDescriptor(TxField.class);

	static public String ARRAY_CONTAINER_DESC = Type
			.getDescriptor(ArrayContainer.class);
	static public String MULTI_ARRAY_CONTAINER_DESC = Type
			.getDescriptor(MultiArrayContainer.class);
	static public String OBJECT_ARRAY_CONTAINER_DESC = Type
			.getDescriptor(ObjectArrayContainer.class);
	static public String INT_ARRAY_CONTAINER_DESC = Type
			.getDescriptor(IntArrayContainer.class);
	static public String SHORT_ARRAY_CONTAINER_DESC = Type
			.getDescriptor(ShortArrayContainer.class);
	static public String FLOAT_ARRAY_CONTAINER_DESC = Type
			.getDescriptor(FloatArrayContainer.class);
	static public String CHAR_ARRAY_CONTAINER_DESC = Type
			.getDescriptor(CharArrayContainer.class);
	static public String BOOLEAN_ARRAY_CONTAINER_DESC = Type
			.getDescriptor(BooleanArrayContainer.class);
	static public String BYTE_ARRAY_CONTAINER_DESC = Type
			.getDescriptor(ByteArrayContainer.class);
	static public String LONG_ARRAY_CONTAINER_DESC = Type
			.getDescriptor(LongArrayContainer.class);
	static public String DOUBLE_ARRAY_CONTAINER_DESC = Type
			.getDescriptor(DoubleArrayContainer.class);

	static public String BEFORE_READ_METHOD_DESC_META = "("
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";

	static private String WRITE_METHOD_BOOLEAN_DESC_META = "(Z"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String WRITE_METHOD_BYTE_DESC_META = "(B"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String WRITE_METHOD_CHAR_DESC_META = "(C"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String WRITE_METHOD_SHORT_DESC_META = "(S"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String WRITE_METHOD_INT_DESC_META = "(I"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String WRITE_METHOD_LONG_DESC_META = "(J"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String WRITE_METHOD_FLOAT_DESC_META = "(F"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String WRITE_METHOD_DOUBLE_DESC_META = "(D"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String WRITE_METHOD_OBJ_DESC_META = "(Ljava/lang/Object;"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String WRITE_METHOD_ARRAY_DESC_META = "("
			+ ARRAY_CONTAINER_DESC + TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC
			+ ")V";

	static private String STATIC_WRITE_METHOD_BOOLEAN_DESC_META = "(Z"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String STATIC_WRITE_METHOD_BYTE_DESC_META = "(B"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String STATIC_WRITE_METHOD_CHAR_DESC_META = "(C"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String STATIC_WRITE_METHOD_SHORT_DESC_META = "(S"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String STATIC_WRITE_METHOD_INT_DESC_META = "(I"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String STATIC_WRITE_METHOD_LONG_DESC_META = "(J"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String STATIC_WRITE_METHOD_FLOAT_DESC_META = "(F"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String STATIC_WRITE_METHOD_DOUBLE_DESC_META = "(D"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String STATIC_WRITE_METHOD_OBJ_DESC_META = "(Ljava/lang/Object;"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")V";
	static private String STATIC_WRITE_METHOD_ARRAY_DESC_META = "("
			+ ARRAY_CONTAINER_DESC + TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC
			+ ")V";

	static private String READ_METHOD_BOOLEAN_DESC_META = "(Z"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")Z";
	static private String READ_METHOD_BYTE_DESC_META = "(B"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")B";
	static private String READ_METHOD_CHAR_DESC_META = "(C"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")C";
	static private String READ_METHOD_SHORT_DESC_META = "(S"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")S";
	static private String READ_METHOD_INT_DESC_META = "(I" + TXFIELD_DESCRIPTOR
			+ Context.CONTEXT_DESC + ")I";
	static private String READ_METHOD_LONG_DESC_META = "(J"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")J";
	static private String READ_METHOD_FLOAT_DESC_META = "(F"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")F";
	static private String READ_METHOD_DOUBLE_DESC_META = "(D"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")D";
	static private String READ_METHOD_OBJ_DESC_META = "(Ljava/lang/Object;"
			+ TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC + ")Ljava/lang/Object;";
	static private String READ_METHOD_ARRAY_DESC_META = "("
			+ ARRAY_CONTAINER_DESC + TXFIELD_DESCRIPTOR + Context.CONTEXT_DESC
			+ ")" + ARRAY_CONTAINER_DESC;

	static public String WRITE_ARRAY_METHOD_BOOLEAN_DESC_META = "("
			+ BOOLEAN_ARRAY_CONTAINER_DESC + "IZ" + Context.CONTEXT_DESC + ")V";
	static public String WRITE_ARRAY_METHOD_BYTE_DESC_META = "("
			+ BYTE_ARRAY_CONTAINER_DESC + "IB" + Context.CONTEXT_DESC + ")V";
	static public String WRITE_ARRAY_METHOD_CHAR_DESC_META = "("
			+ CHAR_ARRAY_CONTAINER_DESC + "IC" + Context.CONTEXT_DESC + ")V";
	static public String WRITE_ARRAY_METHOD_SHORT_DESC_META = "("
			+ SHORT_ARRAY_CONTAINER_DESC + "IS" + Context.CONTEXT_DESC + ")V";
	static public String WRITE_ARRAY_METHOD_INT_DESC_META = "("
			+ INT_ARRAY_CONTAINER_DESC + "II" + Context.CONTEXT_DESC + ")V";
	static public String WRITE_ARRAY_METHOD_LONG_DESC_META = "("
			+ LONG_ARRAY_CONTAINER_DESC + "IJ" + Context.CONTEXT_DESC + ")V";
	static public String WRITE_ARRAY_METHOD_FLOAT_DESC_META = "("
			+ FLOAT_ARRAY_CONTAINER_DESC + "IF" + Context.CONTEXT_DESC + ")V";
	static public String WRITE_ARRAY_METHOD_DOUBLE_DESC_META = "("
			+ DOUBLE_ARRAY_CONTAINER_DESC + "ID" + Context.CONTEXT_DESC + ")V";
	static public String WRITE_ARRAY_METHOD_OBJ_DESC_META = "("
			+ OBJECT_ARRAY_CONTAINER_DESC + "ILjava/lang/Object;"
			+ Context.CONTEXT_DESC + ")V";
	static public String WRITE_ARRAY_METHOD_MULTI_DESC_META = "("
			+ MULTI_ARRAY_CONTAINER_DESC + "I" + ARRAY_CONTAINER_DESC
			+ Context.CONTEXT_DESC + ")V";

	static public String READ_ARRAY_METHOD_BOOLEAN_DESC_META = "("
			+ BOOLEAN_ARRAY_CONTAINER_DESC + "I" + Context.CONTEXT_DESC + ")Z";
	static public String READ_ARRAY_METHOD_BYTE_DESC_META = "("
			+ BYTE_ARRAY_CONTAINER_DESC + "I" + Context.CONTEXT_DESC + ")B";
	static public String READ_ARRAY_METHOD_CHAR_DESC_META = "("
			+ CHAR_ARRAY_CONTAINER_DESC + "I" + Context.CONTEXT_DESC + ")C";
	static public String READ_ARRAY_METHOD_SHORT_DESC_META = "("
			+ SHORT_ARRAY_CONTAINER_DESC + "I" + Context.CONTEXT_DESC + ")S";
	static public String READ_ARRAY_METHOD_INT_DESC_META = "("
			+ INT_ARRAY_CONTAINER_DESC + "I" + Context.CONTEXT_DESC + ")I";
	static public String READ_ARRAY_METHOD_LONG_DESC_META = "("
			+ LONG_ARRAY_CONTAINER_DESC + "I" + Context.CONTEXT_DESC + ")J";
	static public String READ_ARRAY_METHOD_FLOAT_DESC_META = "("
			+ FLOAT_ARRAY_CONTAINER_DESC + "I" + Context.CONTEXT_DESC + ")F";
	static public String READ_ARRAY_METHOD_DOUBLE_DESC_META = "("
			+ DOUBLE_ARRAY_CONTAINER_DESC + "I" + Context.CONTEXT_DESC + ")D";
	static public String READ_ARRAY_METHOD_OBJ_DESC_META = "("
			+ OBJECT_ARRAY_CONTAINER_DESC + "I" + Context.CONTEXT_DESC
			+ ")Ljava/lang/Object;";
	static public String READ_ARRAY_METHOD_MULTI_DESC_META = "("
			+ MULTI_ARRAY_CONTAINER_DESC + "I" + Context.CONTEXT_DESC + ")"
			+ ARRAY_CONTAINER_DESC;

	private static boolean metadataInitialized = false;
	private static boolean inLocalMetadata = false;

	private static Class<?> metadataClass = null;

	private static void metadataInitializer()
	{
		if (!metadataInitialized)
		{
			IContext c = THREAD_CONTEXT.get(); // force context loading
			LocalMetadata a = c.getClass().getAnnotation(LocalMetadata.class);
			if (a != null)
			{
				inLocalMetadata = true;
				try
				{
					metadataClass = Class.forName(a.metadataClass());
				}
				catch (ClassNotFoundException e)
				{
					metadataClass = TxField.class;
				}
			}

			metadataInitialized = true;
		}
	}

	public static boolean inLocalMetadata()
	{
		metadataInitializer();
		return inLocalMetadata;
	}

	public static Class<?> getMetadataClass()
	{
		metadataInitializer();
		return metadataClass;
	}

	// private static boolean testedForDistributedContext = false;
	// private static boolean inDistributedContext = false;
	//
	// private static void testDistributedContext() {
	// if (!testedForDistributedContext) {
	// String className = System
	// .getProperty("org.deuce.transaction.contextClass");
	// Class<? extends IContext> contextClass = null;
	// if (className != null) {
	// try {
	// contextClass = (Class<? extends IContext>) Class
	// .forName(className);
	// inDistributedContext = DistributedContext.class
	// .isAssignableFrom(contextClass);
	// } catch (Exception e) {
	// e.printStackTrace(); // TODOs add logger
	// contextClass = tribu.transaction.tl2.Context.class;
	// }
	// } else {
	// contextClass = tribu.transaction.tl2.Context.class;
	// }
	//
	// testedForDistributedContext = true;
	// }
	// }
	//
	// public static boolean inDistributedContext() {
	// testDistributedContext();
	// return inDistributedContext;
	// }

	final static public String CONTEXT_DELEGATOR_INTERNAL = Type
			.getInternalName(ContextDelegator.class);

	final static public String BEFORE_READ_METHOD_NAME = "beforeReadAccess";
	final static public String BEFORE_READ_METHOD_DESC = "(Ljava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";
	final static public String IRREVOCABLE_METHOD_NAME = "onIrrevocableAccess";
	final static public String IRREVOCABLE_METHOD_DESC = "("
			+ Context.CONTEXT_DESC + ")V";

	final static public String WRITE_METHOD_NAME = "onWriteAccess";
	final static public String WRITE_ARR_METHOD_NAME = "onArrayWriteAccess";
	final static public String STATIC_WRITE_METHOD_NAME = "addStaticWriteAccess";
	final static public String READ_METHOD_NAME = "onReadAccess";
	final static public String READ_ARR_METHOD_NAME = "onArrayReadAccess";

	final static private String WRITE_METHOD_BOOLEAN_DESC = "(Ljava/lang/Object;ZJ"
			+ Context.CONTEXT_DESC + ")V";
	final static private String WRITE_METHOD_BYTE_DESC = "(Ljava/lang/Object;BJ"
			+ Context.CONTEXT_DESC + ")V";
	final static private String WRITE_METHOD_CHAR_DESC = "(Ljava/lang/Object;CJ"
			+ Context.CONTEXT_DESC + ")V";
	final static private String WRITE_METHOD_SHORT_DESC = "(Ljava/lang/Object;SJ"
			+ Context.CONTEXT_DESC + ")V";
	final static private String WRITE_METHOD_INT_DESC = "(Ljava/lang/Object;IJ"
			+ Context.CONTEXT_DESC + ")V";
	final static private String WRITE_METHOD_LONG_DESC = "(Ljava/lang/Object;JJ"
			+ Context.CONTEXT_DESC + ")V";
	final static private String WRITE_METHOD_FLOAT_DESC = "(Ljava/lang/Object;FJ"
			+ Context.CONTEXT_DESC + ")V";
	final static private String WRITE_METHOD_DOUBLE_DESC = "(Ljava/lang/Object;DJ"
			+ Context.CONTEXT_DESC + ")V";
	final static private String WRITE_METHOD_OBJ_DESC = "(Ljava/lang/Object;Ljava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";

	final static private String STATIC_WRITE_METHOD_BOOLEAN_DESC = "(ZLjava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";
	final static private String STATIC_WRITE_METHOD_BYTE_DESC = "(BLjava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";
	final static private String STATIC_WRITE_METHOD_CHAR_DESC = "(CLjava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";
	final static private String STATIC_WRITE_METHOD_SHORT_DESC = "(SLjava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";
	final static private String STATIC_WRITE_METHOD_INT_DESC = "(ILjava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";
	final static private String STATIC_WRITE_METHOD_LONG_DESC = "(JLjava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";
	final static private String STATIC_WRITE_METHOD_FLOAT_DESC = "(FLjava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";
	final static private String STATIC_WRITE_METHOD_DOUBLE_DESC = "(DLjava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";
	final static private String STATIC_WRITE_METHOD_OBJ_DESC = "(Ljava/lang/Object;Ljava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")V";

	final static private String READ_METHOD_BOOLEAN_DESC = "(Ljava/lang/Object;ZJ"
			+ Context.CONTEXT_DESC + ")Z";
	final static private String READ_METHOD_BYTE_DESC = "(Ljava/lang/Object;BJ"
			+ Context.CONTEXT_DESC + ")B";
	final static private String READ_METHOD_CHAR_DESC = "(Ljava/lang/Object;CJ"
			+ Context.CONTEXT_DESC + ")C";
	final static private String READ_METHOD_SHORT_DESC = "(Ljava/lang/Object;SJ"
			+ Context.CONTEXT_DESC + ")S";
	final static private String READ_METHOD_INT_DESC = "(Ljava/lang/Object;IJ"
			+ Context.CONTEXT_DESC + ")I";
	final static private String READ_METHOD_LONG_DESC = "(Ljava/lang/Object;JJ"
			+ Context.CONTEXT_DESC + ")J";
	final static private String READ_METHOD_FLOAT_DESC = "(Ljava/lang/Object;FJ"
			+ Context.CONTEXT_DESC + ")F";
	final static private String READ_METHOD_DOUBLE_DESC = "(Ljava/lang/Object;DJ"
			+ Context.CONTEXT_DESC + ")D";
	final static private String READ_METHOD_OBJ_DESC = "(Ljava/lang/Object;Ljava/lang/Object;J"
			+ Context.CONTEXT_DESC + ")Ljava/lang/Object;";

	final static public String WRITE_ARRAY_METHOD_BYTE_DESC = "([BIB"
			+ Context.CONTEXT_DESC + ")V";
	final static public String WRITE_ARRAY_METHOD_CHAR_DESC = "([CIC"
			+ Context.CONTEXT_DESC + ")V";
	final static public String WRITE_ARRAY_METHOD_SHORT_DESC = "([SIS"
			+ Context.CONTEXT_DESC + ")V";
	final static public String WRITE_ARRAY_METHOD_INT_DESC = "([III"
			+ Context.CONTEXT_DESC + ")V";
	final static public String WRITE_ARRAY_METHOD_LONG_DESC = "([JIJ"
			+ Context.CONTEXT_DESC + ")V";
	final static public String WRITE_ARRAY_METHOD_FLOAT_DESC = "([FIF"
			+ Context.CONTEXT_DESC + ")V";
	final static public String WRITE_ARRAY_METHOD_DOUBLE_DESC = "([DID"
			+ Context.CONTEXT_DESC + ")V";
	final static public String WRITE_ARRAY_METHOD_OBJ_DESC = "([Ljava/lang/Object;ILjava/lang/Object;"
			+ Context.CONTEXT_DESC + ")V";

	final static public String READ_ARRAY_METHOD_BYTE_DESC = "([BI"
			+ Context.CONTEXT_DESC + ")B";
	final static public String READ_ARRAY_METHOD_CHAR_DESC = "([CI"
			+ Context.CONTEXT_DESC + ")C";
	final static public String READ_ARRAY_METHOD_SHORT_DESC = "([SI"
			+ Context.CONTEXT_DESC + ")S";
	final static public String READ_ARRAY_METHOD_INT_DESC = "([II"
			+ Context.CONTEXT_DESC + ")I";
	final static public String READ_ARRAY_METHOD_LONG_DESC = "([JI"
			+ Context.CONTEXT_DESC + ")J";
	final static public String READ_ARRAY_METHOD_FLOAT_DESC = "([FI"
			+ Context.CONTEXT_DESC + ")F";
	final static public String READ_ARRAY_METHOD_DOUBLE_DESC = "([DI"
			+ Context.CONTEXT_DESC + ")D";
	final static public String READ_ARRAY_METHOD_OBJ_DESC = "([Ljava/lang/Object;I"
			+ Context.CONTEXT_DESC + ")Ljava/lang/Object;";

	final static private int BYTE_ARR_BASE = AddressUtil
			.arrayBaseOffset(byte[].class);
	final static private int CHAR_ARR_BASE = AddressUtil
			.arrayBaseOffset(char[].class);
	final static private int SHORT_ARR_BASE = AddressUtil
			.arrayBaseOffset(short[].class);
	final static private int INT_ARR_BASE = AddressUtil
			.arrayBaseOffset(int[].class);
	final static private int LONG_ARR_BASE = AddressUtil
			.arrayBaseOffset(long[].class);
	final static private int FLOAT_ARR_BASE = AddressUtil
			.arrayBaseOffset(float[].class);
	final static private int DOUBLE_ARR_BASE = AddressUtil
			.arrayBaseOffset(double[].class);
	final static public int OBJECT_ARR_BASE = AddressUtil
			.arrayBaseOffset(Object[].class);

	final static private int BYTE_ARR_SCALE = AddressUtil
			.arrayIndexScale(byte[].class);
	final static private int CHAR_ARR_SCALE = AddressUtil
			.arrayIndexScale(char[].class);
	final static private int SHORT_ARR_SCALE = AddressUtil
			.arrayIndexScale(short[].class);
	final static private int INT_ARR_SCALE = AddressUtil
			.arrayIndexScale(int[].class);
	final static private int LONG_ARR_SCALE = AddressUtil
			.arrayIndexScale(long[].class);
	final static private int FLOAT_ARR_SCALE = AddressUtil
			.arrayIndexScale(float[].class);
	final static private int DOUBLE_ARR_SCALE = AddressUtil
			.arrayIndexScale(double[].class);
	final static public int OBJECT_ARR_SCALE = AddressUtil
			.arrayIndexScale(Object[].class);

	final private static ContextThreadLocal THREAD_CONTEXT = new ContextThreadLocal();

	@ExcludeTM
	private static class ContextThreadLocal extends ThreadLocal<IContext>
	{
		private Class<? extends IContext> contextClass;

		public ContextThreadLocal()
		{
			String className = System
					.getProperty("org.deuce.transaction.contextClass");
			if (className != null)
			{
				try
				{
					this.contextClass = (Class<? extends IContext>) Class
							.forName(className);
					return;
				}
				catch (Exception e)
				{
					e.printStackTrace(); // TODOs add logger
				}
			}
			this.contextClass = org.deuce.transaction.tl2.Context.class;
		}

		@Override
		protected synchronized IContext initialValue()
		{
			try
			{
				return this.contextClass.newInstance();
			}
			catch (Exception e)
			{
				throw new TransactionException(e);
			}
		}
	}

	public static IContext getInstance()
	{
		return THREAD_CONTEXT.get();
	}

	public static String getWriteMethodDesc(Type type)
	{
		if (!inLocalMetadata())
		{
			switch (type.getSort())
			{
				case Type.BOOLEAN:
					return WRITE_METHOD_BOOLEAN_DESC;
				case Type.BYTE:
					return WRITE_METHOD_BYTE_DESC;
				case Type.CHAR:
					return WRITE_METHOD_CHAR_DESC;
				case Type.SHORT:
					return WRITE_METHOD_SHORT_DESC;
				case Type.INT:
					return WRITE_METHOD_INT_DESC;
				case Type.LONG:
					return WRITE_METHOD_LONG_DESC;
				case Type.FLOAT:
					return WRITE_METHOD_FLOAT_DESC;
				case Type.DOUBLE:
					return WRITE_METHOD_DOUBLE_DESC;
				default:
					return WRITE_METHOD_OBJ_DESC;
			}
		}
		else
		{
			switch (type.getSort())
			{
				case Type.BOOLEAN:
					return WRITE_METHOD_BOOLEAN_DESC_META;
				case Type.BYTE:
					return WRITE_METHOD_BYTE_DESC_META;
				case Type.CHAR:
					return WRITE_METHOD_CHAR_DESC_META;
				case Type.SHORT:
					return WRITE_METHOD_SHORT_DESC_META;
				case Type.INT:
					return WRITE_METHOD_INT_DESC_META;
				case Type.LONG:
					return WRITE_METHOD_LONG_DESC_META;
				case Type.FLOAT:
					return WRITE_METHOD_FLOAT_DESC_META;
				case Type.DOUBLE:
					return WRITE_METHOD_DOUBLE_DESC_META;
				case Type.ARRAY:
					return WRITE_METHOD_ARRAY_DESC_META;
				default:
					return WRITE_METHOD_OBJ_DESC_META;
			}
		}
	}

	public static String getStaticWriteMethodDesc(Type type)
	{
		if (!inLocalMetadata())
		{
			switch (type.getSort())
			{
				case Type.BOOLEAN:
					return STATIC_WRITE_METHOD_BOOLEAN_DESC;
				case Type.BYTE:
					return STATIC_WRITE_METHOD_BYTE_DESC;
				case Type.CHAR:
					return STATIC_WRITE_METHOD_CHAR_DESC;
				case Type.SHORT:
					return STATIC_WRITE_METHOD_SHORT_DESC;
				case Type.INT:
					return STATIC_WRITE_METHOD_INT_DESC;
				case Type.LONG:
					return STATIC_WRITE_METHOD_LONG_DESC;
				case Type.FLOAT:
					return STATIC_WRITE_METHOD_FLOAT_DESC;
				case Type.DOUBLE:
					return STATIC_WRITE_METHOD_DOUBLE_DESC;
				default:
					return STATIC_WRITE_METHOD_OBJ_DESC;
			}
		}
		else
		{
			switch (type.getSort())
			{
				case Type.BOOLEAN:
					return STATIC_WRITE_METHOD_BOOLEAN_DESC_META;
				case Type.BYTE:
					return STATIC_WRITE_METHOD_BYTE_DESC_META;
				case Type.CHAR:
					return STATIC_WRITE_METHOD_CHAR_DESC_META;
				case Type.SHORT:
					return STATIC_WRITE_METHOD_SHORT_DESC_META;
				case Type.INT:
					return STATIC_WRITE_METHOD_INT_DESC_META;
				case Type.LONG:
					return STATIC_WRITE_METHOD_LONG_DESC_META;
				case Type.FLOAT:
					return STATIC_WRITE_METHOD_FLOAT_DESC_META;
				case Type.DOUBLE:
					return STATIC_WRITE_METHOD_DOUBLE_DESC_META;
				case Type.ARRAY:
					return STATIC_WRITE_METHOD_ARRAY_DESC_META;
				default:
					return STATIC_WRITE_METHOD_OBJ_DESC_META;
			}
		}
	}

	public static String getReadMethodDesc(Type type)
	{
		if (!inLocalMetadata())
		{
			switch (type.getSort())
			{
				case Type.BOOLEAN:
					return READ_METHOD_BOOLEAN_DESC;
				case Type.BYTE:
					return READ_METHOD_BYTE_DESC;
				case Type.CHAR:
					return READ_METHOD_CHAR_DESC;
				case Type.SHORT:
					return READ_METHOD_SHORT_DESC;
				case Type.INT:
					return READ_METHOD_INT_DESC;
				case Type.LONG:
					return READ_METHOD_LONG_DESC;
				case Type.FLOAT:
					return READ_METHOD_FLOAT_DESC;
				case Type.DOUBLE:
					return READ_METHOD_DOUBLE_DESC;
				default:
					return READ_METHOD_OBJ_DESC;
			}
		}
		else
		{
			switch (type.getSort())
			{
				case Type.BOOLEAN:
					return READ_METHOD_BOOLEAN_DESC_META;
				case Type.BYTE:
					return READ_METHOD_BYTE_DESC_META;
				case Type.CHAR:
					return READ_METHOD_CHAR_DESC_META;
				case Type.SHORT:
					return READ_METHOD_SHORT_DESC_META;
				case Type.INT:
					return READ_METHOD_INT_DESC_META;
				case Type.LONG:
					return READ_METHOD_LONG_DESC_META;
				case Type.FLOAT:
					return READ_METHOD_FLOAT_DESC_META;
				case Type.DOUBLE:
					return READ_METHOD_DOUBLE_DESC_META;
				case Type.ARRAY:
					return READ_METHOD_ARRAY_DESC_META;
				default:
					return READ_METHOD_OBJ_DESC_META;
			}
		}
	}

	public static String getReadArrayMethodDesc(Type type)
	{
		if (inLocalMetadata())
		{
			switch (type.getSort())
			{
				case Type.BOOLEAN:
					return READ_ARRAY_METHOD_BOOLEAN_DESC_META;
				case Type.BYTE:
					return READ_ARRAY_METHOD_BYTE_DESC_META;
				case Type.CHAR:
					return READ_ARRAY_METHOD_CHAR_DESC_META;
				case Type.SHORT:
					return READ_ARRAY_METHOD_SHORT_DESC_META;
				case Type.INT:
					return READ_ARRAY_METHOD_INT_DESC_META;
				case Type.LONG:
					return READ_ARRAY_METHOD_LONG_DESC_META;
				case Type.FLOAT:
					return READ_ARRAY_METHOD_FLOAT_DESC_META;
				case Type.DOUBLE:
					return READ_ARRAY_METHOD_DOUBLE_DESC_META;
				case Type.ARRAY:
					return READ_ARRAY_METHOD_MULTI_DESC_META;
				default:
					return READ_ARRAY_METHOD_OBJ_DESC_META;
			}
		}
		else
		{
			return "<null>";
		}
	}

	public static String getWriteArrayMethodDesc(Type type)
	{
		if (inLocalMetadata())
		{
			switch (type.getSort())
			{
				case Type.BOOLEAN:
					return WRITE_ARRAY_METHOD_BOOLEAN_DESC_META;
				case Type.BYTE:
					return WRITE_ARRAY_METHOD_BYTE_DESC_META;
				case Type.CHAR:
					return WRITE_ARRAY_METHOD_CHAR_DESC_META;
				case Type.SHORT:
					return WRITE_ARRAY_METHOD_SHORT_DESC_META;
				case Type.INT:
					return WRITE_ARRAY_METHOD_INT_DESC_META;
				case Type.LONG:
					return WRITE_ARRAY_METHOD_LONG_DESC_META;
				case Type.FLOAT:
					return WRITE_ARRAY_METHOD_FLOAT_DESC_META;
				case Type.DOUBLE:
					return WRITE_ARRAY_METHOD_DOUBLE_DESC_META;
				case Type.ARRAY:
					return WRITE_ARRAY_METHOD_MULTI_DESC_META;
				default:
					return WRITE_ARRAY_METHOD_OBJ_DESC_META;
			}
		}
		else
		{
			return "<null>";
		}
	}

	static public void beforeReadAccess(Object obj, long field, IContext context)
	{
		((Context) context).beforeReadAccess(obj, field);
	}

	static public void beforeReadAccess(TxField field, IContext context)
	{
		((ContextMetadata) context).beforeReadAccess(field);
	}

	static public Object onReadAccess(Object obj, Object value, long field,
			IContext context)
	{
		return ((Context) context).onReadAccess(obj, value, field);
	}

	static public boolean onReadAccess(Object obj, boolean value, long field,
			IContext context)
	{
		return ((Context) context).onReadAccess(obj, value, field);
	}

	static public byte onReadAccess(Object obj, byte value, long field,
			IContext context)
	{
		return ((Context) context).onReadAccess(obj, value, field);
	}

	static public char onReadAccess(Object obj, char value, long field,
			IContext context)
	{
		return ((Context) context).onReadAccess(obj, value, field);
	}

	static public short onReadAccess(Object obj, short value, long field,
			IContext context)
	{
		return ((Context) context).onReadAccess(obj, value, field);
	}

	static public int onReadAccess(Object obj, int value, long field,
			IContext context)
	{
		return ((Context) context).onReadAccess(obj, value, field);
	}

	static public long onReadAccess(Object obj, long value, long field,
			IContext context)
	{
		return ((Context) context).onReadAccess(obj, value, field);
	}

	static public float onReadAccess(Object obj, float value, long field,
			IContext context)
	{
		return ((Context) context).onReadAccess(obj, value, field);
	}

	static public double onReadAccess(Object obj, double value, long field,
			IContext context)
	{
		return ((Context) context).onReadAccess(obj, value, field);
	}

	static public ArrayContainer onReadAccess(ArrayContainer value,
			TxField field, IContext context)
	{
		return ((ContextMetadata) context).onReadAccess(value, field);
	}

	static public Object onReadAccess(Object value, TxField field,
			IContext context)
	{
		return ((ContextMetadata) context).onReadAccess(value, field);
	}

	static public boolean onReadAccess(boolean value, TxField field,
			IContext context)
	{
		return ((ContextMetadata) context).onReadAccess(value, field);
	}

	static public byte onReadAccess(byte value, TxField field, IContext context)
	{
		return ((ContextMetadata) context).onReadAccess(value, field);
	}

	static public char onReadAccess(char value, TxField field, IContext context)
	{
		return ((ContextMetadata) context).onReadAccess(value, field);
	}

	static public short onReadAccess(short value, TxField field,
			IContext context)
	{
		return ((ContextMetadata) context).onReadAccess(value, field);
	}

	static public int onReadAccess(int value, TxField field, IContext context)
	{
		return ((ContextMetadata) context).onReadAccess(value, field);
	}

	static public long onReadAccess(long value, TxField field, IContext context)
	{
		return ((ContextMetadata) context).onReadAccess(value, field);
	}

	static public float onReadAccess(float value, TxField field,
			IContext context)
	{
		return ((ContextMetadata) context).onReadAccess(value, field);
	}

	static public double onReadAccess(double value, TxField field,
			IContext context)
	{
		return ((ContextMetadata) context).onReadAccess(value, field);
	}

	static public void onWriteAccess(Object obj, Object value, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void onWriteAccess(Object obj, boolean value, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void onWriteAccess(Object obj, byte value, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void onWriteAccess(Object obj, char value, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void onWriteAccess(Object obj, short value, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void onWriteAccess(Object obj, int value, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void onWriteAccess(Object obj, long value, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void onWriteAccess(Object obj, float value, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void onWriteAccess(Object obj, double value, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void onWriteAccess(ArrayContainer value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void onWriteAccess(Object value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void onWriteAccess(boolean value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void onWriteAccess(byte value, TxField field, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void onWriteAccess(char value, TxField field, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void onWriteAccess(short value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void onWriteAccess(int value, TxField field, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void onWriteAccess(long value, TxField field, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void onWriteAccess(float value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void onWriteAccess(double value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void addStaticWriteAccess(Object value, Object obj,
			long field, IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void addStaticWriteAccess(boolean value, Object obj,
			long field, IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void addStaticWriteAccess(byte value, Object obj, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void addStaticWriteAccess(char value, Object obj, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void addStaticWriteAccess(short value, Object obj,
			long field, IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void addStaticWriteAccess(int value, Object obj, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void addStaticWriteAccess(long value, Object obj, long field,
			IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void addStaticWriteAccess(float value, Object obj,
			long field, IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void addStaticWriteAccess(double value, Object obj,
			long field, IContext context)
	{
		((Context) context).onWriteAccess(obj, value, field);
	}

	static public void addStaticWriteAccess(Object value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void addStaticWriteAccess(boolean value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void addStaticWriteAccess(byte value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void addStaticWriteAccess(char value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void addStaticWriteAccess(short value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void addStaticWriteAccess(int value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void addStaticWriteAccess(long value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void addStaticWriteAccess(float value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public void addStaticWriteAccess(double value, TxField field,
			IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value, field);
	}

	static public Object onArrayReadAccess(Object[] arr, int index,
			IContext context)
	{
		int address = OBJECT_ARR_BASE + OBJECT_ARR_SCALE * index;
		((Context) context).beforeReadAccess(arr, address);
		return ((Context) context).onReadAccess(arr, arr[index], address);
	}

	static public byte onArrayReadAccess(byte[] arr, int index, IContext context)
	{
		int address = BYTE_ARR_BASE + BYTE_ARR_SCALE * index;
		((Context) context).beforeReadAccess(arr, address);
		return ((Context) context).onReadAccess(arr, arr[index], address);
	}

	static public char onArrayReadAccess(char[] arr, int index, IContext context)
	{
		int address = CHAR_ARR_BASE + CHAR_ARR_SCALE * index;
		((Context) context).beforeReadAccess(arr, address);
		return ((Context) context).onReadAccess(arr, arr[index], address);
	}

	static public short onArrayReadAccess(short[] arr, int index,
			IContext context)
	{
		int address = SHORT_ARR_BASE + SHORT_ARR_SCALE * index;
		((Context) context).beforeReadAccess(arr, address);
		return ((Context) context).onReadAccess(arr, arr[index], address);
	}

	static public int onArrayReadAccess(int[] arr, int index, IContext context)
	{
		int address = INT_ARR_BASE + INT_ARR_SCALE * index;
		((Context) context).beforeReadAccess(arr, address);
		return ((Context) context).onReadAccess(arr, arr[index], address);
	}

	static public long onArrayReadAccess(long[] arr, int index, IContext context)
	{
		int address = LONG_ARR_BASE + LONG_ARR_SCALE * index;
		((Context) context).beforeReadAccess(arr, address);
		return ((Context) context).onReadAccess(arr, arr[index], address);
	}

	static public float onArrayReadAccess(float[] arr, int index,
			IContext context)
	{
		int address = FLOAT_ARR_BASE + FLOAT_ARR_SCALE * index;
		((Context) context).beforeReadAccess(arr, address);
		return ((Context) context).onReadAccess(arr, arr[index], address);
	}

	static public double onArrayReadAccess(double[] arr, int index,
			IContext context)
	{
		int address = DOUBLE_ARR_BASE + DOUBLE_ARR_SCALE * index;
		((Context) context).beforeReadAccess(arr, address);
		return ((Context) context).onReadAccess(arr, arr[index], address);
	}

	static public ArrayContainer onArrayReadAccess(MultiArrayContainer arr,
			int index, IContext context)
	{
		TxField elem = arr.metadata[index];
		((ContextMetadata) context).beforeReadAccess(elem);
		assert (arr.array == arr.metadata[index].backend);
		return ((ContextMetadata) context).onReadAccess(arr.nextDim[index],
				elem);
	}

	static public Object onArrayReadAccess(ObjectArrayContainer arr, int index,
			IContext context)
	{
		TxField elem = arr.metadata[index];
		((ContextMetadata) context).beforeReadAccess(elem);
		assert (arr.array == arr.metadata[index].ref);
		return ((ContextMetadata) context).onReadAccess(arr.array[index], elem);
	}

	static public boolean onArrayReadAccess(BooleanArrayContainer arr,
			int index, IContext context)
	{
		TxField elem = arr.metadata[index];
		((ContextMetadata) context).beforeReadAccess(elem);
		assert (arr.array == arr.metadata[index].ref);
		return ((ContextMetadata) context).onReadAccess(arr.array[index], elem);
	}

	static public byte onArrayReadAccess(ByteArrayContainer arr, int index,
			IContext context)
	{
		TxField elem = arr.metadata[index];
		((ContextMetadata) context).beforeReadAccess(elem);
		assert (arr.array == arr.metadata[index].ref);
		return ((ContextMetadata) context).onReadAccess(arr.array[index], elem);
	}

	static public char onArrayReadAccess(CharArrayContainer arr, int index,
			IContext context)
	{
		TxField elem = arr.metadata[index];
		((ContextMetadata) context).beforeReadAccess(elem);
		assert (arr.array == arr.metadata[index].ref);
		return ((ContextMetadata) context).onReadAccess(arr.array[index], elem);
	}

	static public short onArrayReadAccess(ShortArrayContainer arr, int index,
			IContext context)
	{
		TxField elem = arr.metadata[index];
		((ContextMetadata) context).beforeReadAccess(elem);
		assert (arr.array == arr.metadata[index].ref);
		return ((ContextMetadata) context).onReadAccess(arr.array[index], elem);
	}

	static public int onArrayReadAccess(IntArrayContainer arr, int index,
			IContext context)
	{
		TxField elem = arr.metadata[index];
		((ContextMetadata) context).beforeReadAccess(elem);
		assert (arr.array == arr.metadata[index].ref);
		return ((ContextMetadata) context).onReadAccess(arr.array[index], elem);
	}

	static public long onArrayReadAccess(LongArrayContainer arr, int index,
			IContext context)
	{
		TxField elem = arr.metadata[index];
		((ContextMetadata) context).beforeReadAccess(elem);
		assert (arr.array == arr.metadata[index].ref);
		return ((ContextMetadata) context).onReadAccess(arr.array[index], elem);
	}

	static public float onArrayReadAccess(FloatArrayContainer arr, int index,
			IContext context)
	{
		TxField elem = arr.metadata[index];
		((ContextMetadata) context).beforeReadAccess(elem);
		assert (arr.array == arr.metadata[index].ref);
		return ((ContextMetadata) context).onReadAccess(arr.array[index], elem);
	}

	static public double onArrayReadAccess(DoubleArrayContainer arr, int index,
			IContext context)
	{
		TxField elem = arr.metadata[index];
		((ContextMetadata) context).beforeReadAccess(elem);
		assert (arr.array == arr.metadata[index].ref);
		return ((ContextMetadata) context).onReadAccess(arr.array[index], elem);
	}

	static public <T> void onArrayWriteAccess(T[] arr, int index, T value,
			IContext context)
	{
		T t = arr[index]; // dummy access just to check the index in range
		((Context) context).onWriteAccess(arr, value, OBJECT_ARR_BASE
				+ OBJECT_ARR_SCALE * index);
	}

	static public void onArrayWriteAccess(byte[] arr, int index, byte value,
			IContext context)
	{
		byte t = arr[index]; // dummy access just to check the index in range
		((Context) context).onWriteAccess(arr, value, BYTE_ARR_BASE
				+ BYTE_ARR_SCALE * index);
	}

	static public void onArrayWriteAccess(char[] arr, int index, char value,
			IContext context)
	{
		char t = arr[index]; // dummy access just to check the index in range
		((Context) context).onWriteAccess(arr, value, CHAR_ARR_BASE
				+ CHAR_ARR_SCALE * index);
	}

	static public void onArrayWriteAccess(short[] arr, int index, short value,
			IContext context)
	{
		short t = arr[index]; // dummy access just to check the index in range
		((Context) context).onWriteAccess(arr, value, SHORT_ARR_BASE
				+ SHORT_ARR_SCALE * index);
	}

	static public void onArrayWriteAccess(int[] arr, int index, int value,
			IContext context)
	{
		int t = arr[index]; // dummy access just to check the index in range
		((Context) context).onWriteAccess(arr, value, INT_ARR_BASE
				+ INT_ARR_SCALE * index);
	}

	static public void onArrayWriteAccess(long[] arr, int index, long value,
			IContext context)
	{
		long t = arr[index]; // dummy access just to check the index in range
		((Context) context).onWriteAccess(arr, value, LONG_ARR_BASE
				+ LONG_ARR_SCALE * index);
	}

	static public void onArrayWriteAccess(float[] arr, int index, float value,
			IContext context)
	{
		float t = arr[index]; // dummy access just to check the index in range
		((Context) context).onWriteAccess(arr, value, FLOAT_ARR_BASE
				+ FLOAT_ARR_SCALE * index);
	}

	static public void onArrayWriteAccess(double[] arr, int index,
			double value, IContext context)
	{
		double t = arr[index]; // dummy access just to check the index in range
		((Context) context).onWriteAccess(arr, value, DOUBLE_ARR_BASE
				+ DOUBLE_ARR_SCALE * index);
	}

	static public void onArrayWriteAccess(MultiArrayContainer arr, int index,
			ArrayContainer value, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value,
				(TxField) arr.metadata[index]);
	}

	static public <T> void onArrayWriteAccess(ObjectArrayContainer arr,
			int index, T value, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value,
				(TxField) arr.metadata[index]);
	}

	static public void onArrayWriteAccess(BooleanArrayContainer arr, int index,
			boolean value, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value,
				(TxField) arr.metadata[index]);
	}

	static public void onArrayWriteAccess(ByteArrayContainer arr, int index,
			byte value, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value,
				(TxField) arr.metadata[index]);
	}

	static public void onArrayWriteAccess(CharArrayContainer arr, int index,
			char value, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value,
				(TxField) arr.metadata[index]);
	}

	static public void onArrayWriteAccess(ShortArrayContainer arr, int index,
			short value, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value,
				(TxField) arr.metadata[index]);
	}

	static public void onArrayWriteAccess(IntArrayContainer arr, int index,
			int value, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value,
				(TxField) arr.metadata[index]);
	}

	static public void onArrayWriteAccess(LongArrayContainer arr, int index,
			long value, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value,
				(TxField) arr.metadata[index]);
	}

	static public void onArrayWriteAccess(FloatArrayContainer arr, int index,
			float value, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value,
				(TxField) arr.metadata[index]);
	}

	static public void onArrayWriteAccess(DoubleArrayContainer arr, int index,
			double value, IContext context)
	{
		((ContextMetadata) context).onWriteAccess(value,
				(TxField) arr.metadata[index]);
	}

	static public void onIrrevocableAccess(IContext context)
	{
		context.onIrrevocableAccess();
	}

}
