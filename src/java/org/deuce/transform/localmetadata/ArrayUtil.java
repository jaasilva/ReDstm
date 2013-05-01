package org.deuce.transform.localmetadata;

import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.objectweb.asm.commons.Method;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transform.ExcludeTM;
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

@ExcludeTM
public class ArrayUtil
{

	public static Type getArrayType(String desc)
	{
		return Type.getType(desc.substring(desc.lastIndexOf("[") + 1));
	}

	public static Type getArrayType(Type t)
	{
		return getArrayType(t.getDescriptor());
	}

	public static int getArrayDim(String desc)
	{
		int i = 0;
		while (desc.charAt(i) == '[')
		{
			i++;
		}
		return i;
	}

	public static int getArrayDim(Type t)
	{
		return getArrayDim(t.getDescriptor());
	}

	public static Type getTxType(String desc)
	{
		return Type.getType(ContextDelegator.getMetadataClass());
	}

	public static Type getTxArrayType(String desc)
	{
		return Type.getType(ContextDelegator.getMetadataClass());
	}

	public static Type getTxArrayType(Type t)
	{
		return getTxArrayType(t.getDescriptor());
	}

	public static Type getTxArrayType(int opcode)
	{
		return getTxArrayType(getTypeFromNewarrayOpcode(opcode));
	}

	private static String getArrayDimDesc(int dim)
	{
		String res = "";
		for (int i = 0; i < dim; i++)
		{
			res += "[";
		}
		return res;
	}

	public static Type getTxArrayArrayType(String desc)
	{
		Type arrT = getTxArrayType(desc);
		int dim = getArrayDim(desc);
		return Type.getType(dim > 1 ? getArrayDimDesc(1)
				+ Type.getType(ContextDelegator.getMetadataClass())
				: getArrayDimDesc(dim) + arrT.getDescriptor());
	}

	public static Type getTxArrayArrayType(Type t)
	{
		return getTxArrayArrayType(t.getDescriptor());
	}

	public static Type getArrayContainerType(String desc)
	{
		/*
		 * For uni-dimensional arrays t[], there is a respective ArrTContainer
		 * where: t => T ----------------- boolean => Bool byte => Byte char =>
		 * Char double => Double float => Float int => Int long => Long short =>
		 * Short * => Object Multi-dimensional arrays are chained with
		 * MultiArrContainers.
		 */
		Type t = getArrayType(desc);
		int dim = getArrayDim(desc);
		if (dim < 2)
		{
			switch (t.getSort())
			{
				case Type.INT:
					return Type.getType(IntArrayContainer.class);
				case Type.SHORT:
					return Type.getType(ShortArrayContainer.class);
				case Type.LONG:
					return Type.getType(LongArrayContainer.class);
				case Type.BYTE:
					return Type.getType(ByteArrayContainer.class);
				case Type.BOOLEAN:
					return Type.getType(BooleanArrayContainer.class);
				case Type.CHAR:
					return Type.getType(CharArrayContainer.class);
				case Type.FLOAT:
					return Type.getType(FloatArrayContainer.class);
				case Type.DOUBLE:
					return Type.getType(DoubleArrayContainer.class);
				default:
					return Type.getType(ObjectArrayContainer.class);
			}
		}
		return Type.getType(MultiArrayContainer.class);
	}

	public static Type getArrayContainerType(Type t)
	{
		return getArrayContainerType(t.getDescriptor());
	}

	public static Type getArrayContainerType(int operand)
	{
		return getArrayContainerType(getTypeFromNewarrayOpcode(operand));
	}

	public static Type getArrayContainerArrayType(String desc)
	{
		return Type.getType(getArrayDimDesc(1)
				+ getArrayContainerType(desc).getDescriptor());
	}

	public static Type getArrayContainerArrayType(Type t)
	{
		return getArrayContainerArrayType(t.getDescriptor());
	}

	public static String getArrayContainerCtorDesc(Type t)
	{
		switch (t.getSort())
		{
			case Type.INT:
				return IntArrayContainer.CTOR_DESC;
			case Type.SHORT:
				return ShortArrayContainer.CTOR_DESC;
			case Type.LONG:
				return LongArrayContainer.CTOR_DESC;
			case Type.BYTE:
				return ByteArrayContainer.CTOR_DESC;
			case Type.BOOLEAN:
				return BooleanArrayContainer.CTOR_DESC;
			case Type.CHAR:
				return CharArrayContainer.CTOR_DESC;
			case Type.FLOAT:
				return FloatArrayContainer.CTOR_DESC;
			case Type.DOUBLE:
				return DoubleArrayContainer.CTOR_DESC;
			default:
				return ObjectArrayContainer.CTOR_DESC;
		}
	}

	public static Type getTypeFromNewarrayOpcode(int opcode)
	{
		switch (opcode)
		{
			case Opcodes.T_BOOLEAN:
				return Type.BOOLEAN_TYPE;
			case Opcodes.T_CHAR:
				return Type.CHAR_TYPE;
			case Opcodes.T_FLOAT:
				return Type.FLOAT_TYPE;
			case Opcodes.T_DOUBLE:
				return Type.DOUBLE_TYPE;
			case Opcodes.T_BYTE:
				return Type.BYTE_TYPE;
			case Opcodes.T_SHORT:
				return Type.SHORT_TYPE;
			case Opcodes.T_INT:
				return Type.INT_TYPE;
			case Opcodes.T_LONG:
				return Type.LONG_TYPE;
			default:
				return null;
		}
	}

	public static Method updateMethodArrayArgumentsAndReturn(Method m)
	{

		Type[] arguments = m.getArgumentTypes();
		Type[] newArguments = new Type[arguments.length];
		System.arraycopy(arguments, 0, newArguments, 0, arguments.length);

		for (int i = 0; i < newArguments.length; i++)
		{
			if (newArguments[i].getSort() == Type.ARRAY)
			{
				newArguments[i] = getArrayContainerType(newArguments[i]);
			}
		}

		Type newReturn = m.getReturnType();
		if (newReturn.getSort() == Type.ARRAY)
		{
			newReturn = getArrayContainerType(newReturn);
		}

		return new Method(m.getName(), newReturn, newArguments);
	}

	public static Method updateMethodArrayArgumentsAndReturn(String desc)
	{
		return updateMethodArrayArgumentsAndReturn(new Method("<dummy>", desc));
	}

	public static int getArrayTypeOpcode(Type t)
	{
		switch (t.getSort())
		{
			case Type.INT:
				return Opcodes.T_INT;
			case Type.SHORT:
				return Opcodes.T_SHORT;
			case Type.BYTE:
				return Opcodes.T_BYTE;
			case Type.BOOLEAN:
				return Opcodes.T_BOOLEAN;
			case Type.FLOAT:
				return Opcodes.T_FLOAT;
			case Type.CHAR:
				return Opcodes.T_CHAR;
			case Type.DOUBLE:
				return Opcodes.T_DOUBLE;
			case Type.LONG:
				return Opcodes.T_LONG;
			default:
				return -1;
		}
	}

	public static boolean isArrayContainer(String desc)
	{
		if (desc.equals(ContextDelegator.BOOLEAN_ARRAY_CONTAINER_DESC)
				|| desc.equals(ContextDelegator.BYTE_ARRAY_CONTAINER_DESC)
				|| desc.equals(ContextDelegator.CHAR_ARRAY_CONTAINER_DESC)
				|| desc.equals(ContextDelegator.ARRAY_CONTAINER_DESC)
				|| desc.equals(ContextDelegator.DOUBLE_ARRAY_CONTAINER_DESC)
				|| desc.equals(ContextDelegator.FLOAT_ARRAY_CONTAINER_DESC)
				|| desc.equals(ContextDelegator.INT_ARRAY_CONTAINER_DESC)
				|| desc.equals(ContextDelegator.LONG_ARRAY_CONTAINER_DESC)
				|| desc.equals(ContextDelegator.OBJECT_ARRAY_CONTAINER_DESC)
				|| desc.equals(ContextDelegator.SHORT_ARRAY_CONTAINER_DESC)
				|| desc.equals(ContextDelegator.MULTI_ARRAY_CONTAINER_DESC))
		{
			return true;
		}
		return false;
	}
}
