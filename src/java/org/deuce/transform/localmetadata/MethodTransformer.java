package org.deuce.transform.localmetadata;

import java.util.HashMap;
import java.util.Stack;

import org.deuce.Unsafe;
import org.deuce.objectweb.asm.AnnotationVisitor;
import org.deuce.objectweb.asm.Attribute;
import org.deuce.objectweb.asm.Label;
import org.deuce.objectweb.asm.MethodAdapter;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.objectweb.asm.commons.AnalyzerAdapter;
import org.deuce.objectweb.asm.commons.Method;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.asm.FieldsHolder;
import org.deuce.transform.asm.method.AtomicMethod;
import org.deuce.transform.asm.method.MethodAnnotationVisitor;
import org.deuce.transform.localmetadata.ClassTransformer.NonInsnMethod;
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

@ExcludeTM
public class MethodTransformer implements MethodVisitor
{
	final static private String UNSAFE_DESCRIPTOR = Type
			.getDescriptor(Unsafe.class);

	private MethodVisitor originalMethod;

	final private MethodVisitor originalCopyMethod;

	private MethodVisitor copyMethod;

	final private String className;
	final private String methodName;
	final private String descriptor; // original descriptor
	final private boolean isStatic;
	final private Method newMethod;

	final private HashMap<Label, Label> labelMap = new HashMap<Label, Label>();

	private AnalyzerAdapter analyzerAdapter = null;
	private MethodAdapter duplicateMethod;
	private ClassTransformer clazzT;

	public MethodTransformer(MethodVisitor originalMethod,
			MethodVisitor copyMethod, String className, int access,
			String methodName, String descriptor, String originalDesc,
			Method newMethod, FieldsHolder fieldsHolder)
	{
		this.originalMethod = originalMethod;
		this.newMethod = newMethod;
		this.isStatic = (access & Opcodes.ACC_STATIC) != 0;
		this.originalCopyMethod = copyMethod;

		// The AnalyzerAdapter delegates the call to the DuplicateMethod, while
		// the DuplicateMethod uses the analyzer for stack state in the original
		// method.
		// AnalyzerAdapter analyzerAdapter = null;
		analyzerAdapter = new AnalyzerAdapter(className, access, methodName,
				originalDesc, null);

		if (originalCopyMethod != null)
		{
			duplicateMethod = new DuplicateMethod(copyMethod, isStatic,
					newMethod, fieldsHolder);
			((DuplicateMethod) duplicateMethod).setAnalyzer(analyzerAdapter);
		}

		this.copyMethod = duplicateMethod;
		this.className = className;
		this.methodName = methodName;
		this.descriptor = descriptor;

		clazzT = (ClassTransformer) fieldsHolder;
	}

	public void visitCode()
	{
		originalMethod.visitCode();
		if (originalCopyMethod != null)
		{
			copyMethod.visitCode();
		}
	}

	private Stack<Boolean> enables = new Stack<Boolean>();

	public void disableDuplicateInstrumentation(boolean state)
	{
		if (!enables.empty())
		{
			if (enables.peek() == state)
			{
				enables.push(state);
				return;
			}
			else
			{
				enables.pop();
				if (!enables.isEmpty())
				{
					return;
				}
			}
		}
		else
		{
			enables.push(state);
		}
		if (originalCopyMethod != null)
		{
			((DuplicateMethod) duplicateMethod).setIgnore(state);
		}
	}

	private boolean ignore = false;

	public void disableMethodInstrumentation(boolean state)
	{
		this.ignore = state;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible)
	{
		// FIXME we might saw other annotations before and we need to put it on
		// the new AtomicMethod need to create an atomic method from the
		// original method
		if (AtomicMethod.ATOMIC_DESCRIPTOR.equals(desc)
				&& !(originalMethod instanceof AtomicMethod)
				&& originalCopyMethod != null)
		{
			originalMethod = new AtomicMethod(originalMethod, className,
					methodName, descriptor, newMethod, isStatic);
		}

		// if marked as Unsafe no just duplicate the method as is.
		if (UNSAFE_DESCRIPTOR.equals(desc))
			copyMethod = originalCopyMethod;

		// TODO find another way
		if (!desc.contains("org/junit"))
		{
			return new MethodAnnotationVisitor(originalMethod.visitAnnotation(
					desc, visible),
					originalCopyMethod != null ? copyMethod.visitAnnotation(
							desc, visible) : null);
		}
		else
		{
			return originalMethod.visitAnnotation(desc, visible);
		}
	}

	public AnnotationVisitor visitAnnotationDefault()
	{
		return new MethodAnnotationVisitor(
				originalMethod.visitAnnotationDefault(),
				copyMethod.visitAnnotationDefault());
	}

	public void visitAttribute(Attribute attr)
	{
		analyzerAdapter.visitAttribute(attr);
		originalMethod.visitAttribute(attr);
		if (originalCopyMethod != null)
		{
			copyMethod.visitAttribute(attr);
		}
	}

	public void visitEnd()
	{
		originalMethod.visitEnd();
		if (originalCopyMethod != null)
		{
			copyMethod.visitEnd();
		}
	}

	public void visitFieldInsn(int opcode, String owner, String name,
			String desc)
	{
		if (!ignore)
		{
			analyzerAdapter.visitFieldInsn(opcode, owner, name, desc);
			if (Type.getType(desc).getSort() == Type.ARRAY)
			{
				desc = ArrayUtil.getArrayContainerType(desc).getDescriptor();
				originalMethod.visitFieldInsn(opcode, owner, name, desc);
				if (originalCopyMethod != null)
				{
					copyMethod.visitFieldInsn(opcode, owner, name, desc);
				}
				return;
			}
		}
		originalMethod.visitFieldInsn(opcode, owner, name, desc);
		if (originalCopyMethod != null)
		{
			copyMethod.visitFieldInsn(opcode, owner, name, desc);
		}
	}

	public void visitFrame(int type, int local, Object[] local2, int stack,
			Object[] stack2)
	{
		if (!ignore)
		{
			analyzerAdapter.visitFrame(type, local, local2, stack, stack2);
		}
		originalMethod.visitFrame(type, local, local2, stack, stack2);
		if (originalCopyMethod != null)
		{
			copyMethod.visitFrame(type, local, local2, stack, stack2);
		}
	}

	public void visitIincInsn(int var, int increment)
	{
		if (!ignore)
		{
			analyzerAdapter.visitIincInsn(var, increment);
		}
		originalMethod.visitIincInsn(var, increment);
		if (originalCopyMethod != null)
		{
			copyMethod.visitIincInsn(var, increment);
		}
	}

	private String getAnalyzerTopStackObjectDesc()
	{
		if (analyzerAdapter.stack.size() == 0)
		{
			return null;
		}

		String tos = null;

		Object object = analyzerAdapter.stack
				.get(analyzerAdapter.stack.size() - 1);

		if (object instanceof Integer)
		{
			Integer integer = (Integer) object;
			if (integer == Opcodes.NULL)
				return Type.getType(Object.class).getDescriptor();
			else
				return Type.INT_TYPE.getDescriptor();
		}
		else
		{
			tos = (String) object;
		}

		int i = tos.lastIndexOf("[");
		if (i != -1)
		{
			return tos;
		}

		return "L" + tos + ";";
	}

	private String getAnalyzerAaloadArrayDesc()
	{
		return (String) analyzerAdapter.stack
				.get(analyzerAdapter.stack.size() - 2);
	}

	private String getAnalyzerAastoreArrayDesc()
	{
		return (String) analyzerAdapter.stack
				.get(analyzerAdapter.stack.size() - 3);
	}

	public void visitInsn(int opcode)
	{
		if (!ignore)
		{
			String arrDesc = null;
			String arrField = null;
			boolean isMultiArray = false;

			String arrayDesc = null;
			String methodName = null;
			String methodDesc = null;
			String newValueDesc = null;

			if (opcode == Opcodes.AALOAD)
			{
				arrayDesc = getAnalyzerAaloadArrayDesc();
			}
			else if (opcode == Opcodes.AASTORE)
			{
				arrayDesc = getAnalyzerAastoreArrayDesc();
				newValueDesc = getAnalyzerTopStackObjectDesc();
				if (originalCopyMethod != null)
					((DuplicateMethod) copyMethod).AastoreNewValueDesc = newValueDesc;
			}
			else if (opcode == Opcodes.ARRAYLENGTH)
			{
				arrayDesc = getAnalyzerTopStackObjectDesc();
			}
			if (opcode == Opcodes.AALOAD || opcode == Opcodes.AASTORE)
				isMultiArray = ArrayUtil.getArrayDim(arrayDesc) > 1;

			if (originalCopyMethod != null)
				((DuplicateMethod) copyMethod).isMultiArray = isMultiArray;

			analyzerAdapter.visitInsn(opcode);
			Type arrCntType = null;

			if (opcode == Opcodes.AALOAD || opcode == Opcodes.BALOAD
					|| opcode == Opcodes.CALOAD || opcode == Opcodes.SALOAD
					|| opcode == Opcodes.IALOAD || opcode == Opcodes.LALOAD
					|| opcode == Opcodes.FALOAD || opcode == Opcodes.DALOAD)
			{
				/*
				 * If the opcode is one of the ALOAD family, that means the
				 * stack is of the form: [ ..., arrayref, index => We must
				 * replace the instruction with an AALOAD, because the array
				 * elements are TxArrField. Then we cast it to the appropriate
				 * TxArrField type (correspondent to the original _ALOAD
				 * specific type instruction), and load
				 * TxArrField.array[TxArrField.index]. Legend: arr -> arrayref
				 * idx -> array access index (int) obj -> objectref obj* ->
				 * objectref (correct TxArrField type) val -> value of the
				 * original array's element val* -> value of the original
				 * array's element (correct type) newval -> new value to be
				 * inserted
				 */
				String originalResultTypeDesc = getAnalyzerTopStackObjectDesc();
				if (originalCopyMethod != null)
					((DuplicateMethod) copyMethod).originalResultTypeDesc = originalResultTypeDesc;

				switch (opcode)
				{
					case Opcodes.AALOAD:
						if (isMultiArray)
						{
							arrCntType = Type
									.getType(MultiArrayContainer.class);
							arrDesc = MultiArrayContainer.NEXTDIM_FIELD_DESC;
							arrField = MultiArrayContainer.NEXTDIM_FIELD_NAME;
							// methodName = ContextDelegator
							// .getTxFieldReadMethodName(Type
							// .getType(Object[].class));
							// methodDesc = ContextDelegator
							// .getTxFieldReadMethodDesc(Type
							// .getType(Object[].class));
						}
						else
						{
							arrCntType = Type
									.getType(ObjectArrayContainer.class);
							arrDesc = ObjectArrayContainer.ARRAY_FIELD_DESC;
							arrField = ArrayContainer.ARRAY_FIELD_NAME;
							// methodName = ContextDelegator
							// .getTxFieldReadMethodName(Type
							// .getType(Object.class));
							// methodDesc = ContextDelegator
							// .getTxFieldReadMethodDesc(Type
							// .getType(Object.class));
						}
						break;
					case Opcodes.BALOAD:
						arrCntType = Type.getType(ByteArrayContainer.class);
						arrDesc = ByteArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodName = ContextDelegator
						// .getTxFieldReadMethodName(Type.BYTE_TYPE);
						// methodDesc = ContextDelegator
						// .getTxFieldReadMethodDesc(Type.BYTE_TYPE);
						break;
					case Opcodes.CALOAD:
						arrCntType = Type.getType(CharArrayContainer.class);
						arrDesc = CharArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodName = ContextDelegator
						// .getTxFieldReadMethodName(Type.CHAR_TYPE);
						// methodDesc = ContextDelegator
						// .getTxFieldReadMethodDesc(Type.CHAR_TYPE);
						break;
					case Opcodes.SALOAD:
						arrCntType = Type.getType(ShortArrayContainer.class);
						arrDesc = ShortArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodName = ContextDelegator
						// .getTxFieldReadMethodName(Type.SHORT_TYPE);
						// methodDesc = ContextDelegator
						// .getTxFieldReadMethodDesc(Type.SHORT_TYPE);
						break;
					case Opcodes.IALOAD:
						arrCntType = Type.getType(IntArrayContainer.class);
						arrDesc = IntArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodName = ContextDelegator
						// .getTxFieldReadMethodName(Type.INT_TYPE);
						// methodDesc = ContextDelegator
						// .getTxFieldReadMethodDesc(Type.INT_TYPE);
						break;
					case Opcodes.LALOAD:
						arrCntType = Type.getType(LongArrayContainer.class);
						arrDesc = LongArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodName = ContextDelegator
						// .getTxFieldReadMethodName(Type.LONG_TYPE);
						// methodDesc = ContextDelegator
						// .getTxFieldReadMethodDesc(Type.LONG_TYPE);
						break;
					case Opcodes.FALOAD:
						arrCntType = Type.getType(FloatArrayContainer.class);
						arrDesc = FloatArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodName = ContextDelegator
						// .getTxFieldReadMethodName(Type.FLOAT_TYPE);
						// methodDesc = ContextDelegator
						// .getTxFieldReadMethodDesc(Type.FLOAT_TYPE);
						break;
					case Opcodes.DALOAD:
						arrCntType = Type.getType(DoubleArrayContainer.class);
						arrDesc = DoubleArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodName = ContextDelegator
						// .getTxFieldReadMethodName(Type.DOUBLE_TYPE);
						// methodDesc = ContextDelegator
						// .getTxFieldReadMethodDesc(Type.DOUBLE_TYPE);
						break;
				}

				// ..., cnt, idx =>
				originalMethod.visitInsn(Opcodes.SWAP);
				// ..., idx, cnt =>
				originalMethod.visitFieldInsn(Opcodes.GETFIELD,
						arrCntType.getInternalName(), arrField, arrDesc);
				// ..., idx, cnt[]/arr =>
				// if (opcode == Opcodes.AALOAD) {
				// if (isMultiArray) {
				// originalMethod.visitTypeInsn(
				// Opcodes.CHECKCAST,
				// ArrayUtil.getArrayContainerArrayType(
				// originalResultTypeDesc)
				// .getInternalName());
				// } else {
				// originalMethod.visitTypeInsn(Opcodes.CHECKCAST, Type
				// .getType(originalArrDesc).getInternalName());
				// }
				// }
				// ..., idx, cnt*[]/arr* =>
				originalMethod.visitInsn(Opcodes.SWAP);
				// ..., cnt*[]/arr*, idx =>
				originalMethod.visitInsn(opcode);
				// ..., val* =>
				if (opcode == Opcodes.AALOAD)
				{
					if (isMultiArray)
					{
						originalMethod.visitTypeInsn(
								Opcodes.CHECKCAST,
								ArrayUtil.getArrayContainerType(
										originalResultTypeDesc)
										.getInternalName());
					}
					else
					{
						originalMethod.visitTypeInsn(Opcodes.CHECKCAST, Type
								.getType(originalResultTypeDesc)
								.getInternalName());
					}
				}

				if (originalCopyMethod != null)
				{
					copyMethod.visitInsn(opcode);
				}
				return;
			}
			else if (opcode == Opcodes.AASTORE || opcode == Opcodes.BASTORE
					|| opcode == Opcodes.CASTORE || opcode == Opcodes.SASTORE
					|| opcode == Opcodes.IASTORE || opcode == Opcodes.LASTORE
					|| opcode == Opcodes.FASTORE || opcode == Opcodes.DASTORE)
			{
				/*
				 * If the opcode is one of the ASTORE family, that means the
				 * stack is of the form: [ ..., arrayref, index, value => We
				 * must AALOAD the arrayref[index] element first, cast it to the
				 * appropriate TxArrField type, and substitute the arrayref and
				 * index below with the TxArrField.array and TxArrField.index,
				 * respectively. Legend: arr -> arrayref idx -> array access
				 * index (int) obj -> objectref obj* -> objectref (correct
				 * TxArrField type) val -> value of the original array's element
				 * val* -> value of the original array's element (correct type)
				 * newval -> new value to be inserted
				 */
				Type originalArrElemType = null;
				switch (opcode)
				{
					case Opcodes.AASTORE:
						originalArrElemType = Type.getType(Object.class);
						if (isMultiArray)
						{
							arrCntType = Type
									.getType(MultiArrayContainer.class);
							arrDesc = MultiArrayContainer.NEXTDIM_FIELD_DESC;
							arrField = MultiArrayContainer.NEXTDIM_FIELD_NAME;
						}
						else
						{
							arrCntType = Type
									.getType(ObjectArrayContainer.class);
							arrDesc = ObjectArrayContainer.ARRAY_FIELD_DESC;
							arrField = ArrayContainer.ARRAY_FIELD_NAME;
						}
						// methodDesc = ContextDelegator
						// .getTxFieldWriteMethodDesc(Type
						// .getType(newValueDesc));
						break;
					case Opcodes.BASTORE:
						originalArrElemType = Type.BYTE_TYPE;
						arrCntType = Type.getType(ByteArrayContainer.class);
						arrDesc = ByteArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodDesc = ContextDelegator
						// .getTxFieldWriteMethodDesc(originalArrElemType);
						break;
					case Opcodes.CASTORE:
						originalArrElemType = Type.CHAR_TYPE;
						arrCntType = Type.getType(CharArrayContainer.class);
						arrDesc = CharArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodDesc = ContextDelegator
						// .getTxFieldWriteMethodDesc(originalArrElemType);
						break;
					case Opcodes.SASTORE:
						originalArrElemType = Type.SHORT_TYPE;
						arrCntType = Type.getType(ShortArrayContainer.class);
						arrDesc = ShortArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodDesc = ContextDelegator
						// .getTxFieldWriteMethodDesc(originalArrElemType);
						break;
					case Opcodes.IASTORE:
						originalArrElemType = Type.INT_TYPE;
						arrCntType = Type.getType(IntArrayContainer.class);
						arrDesc = IntArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodDesc = ContextDelegator
						// .getTxFieldWriteMethodDesc(originalArrElemType);
						break;
					case Opcodes.LASTORE:
						originalArrElemType = Type.LONG_TYPE;
						arrCntType = Type.getType(LongArrayContainer.class);
						arrDesc = LongArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodDesc = ContextDelegator
						// .getTxFieldWriteMethodDesc(originalArrElemType);
						break;
					case Opcodes.FASTORE:
						originalArrElemType = Type.FLOAT_TYPE;
						arrCntType = Type.getType(FloatArrayContainer.class);
						arrDesc = FloatArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodDesc = ContextDelegator
						// .getTxFieldWriteMethodDesc(originalArrElemType);
						break;
					case Opcodes.DASTORE:
						originalArrElemType = Type.DOUBLE_TYPE;
						arrCntType = Type.getType(DoubleArrayContainer.class);
						arrDesc = DoubleArrayContainer.ARRAY_FIELD_DESC;
						arrField = ArrayContainer.ARRAY_FIELD_NAME;
						// methodDesc = ContextDelegator
						// .getTxFieldWriteMethodDesc(originalArrElemType);
						break;
				}

				// methodName = ContextDelegator.TXFIELD_WRITE_METHOD_NAME;

				// ..., cnt, idx, newval =>
				if (originalArrElemType == Type.LONG_TYPE
						|| originalArrElemType == Type.DOUBLE_TYPE)
				{
					originalMethod.visitInsn(Opcodes.DUP2_X2);
					originalMethod.visitInsn(Opcodes.POP2);
				}
				else
				{
					originalMethod.visitInsn(Opcodes.DUP_X2);
					originalMethod.visitInsn(Opcodes.POP);
				}
				// ..., newval, cnt, idx =>
				originalMethod.visitInsn(Opcodes.SWAP);
				// ..., newval, idx, cnt =>
				if (opcode == Opcodes.AASTORE && isMultiArray)
				{
					if (originalArrElemType == Type.LONG_TYPE
							|| originalArrElemType == Type.DOUBLE_TYPE)
					{
						originalMethod.visitInsn(Opcodes.DUP2_X2);
					}
					else
					{
						originalMethod.visitInsn(Opcodes.DUP2_X1);
					}
				}
				// ...,[ idx, cnt,] newval, idx, cnt =>
				originalMethod.visitFieldInsn(Opcodes.GETFIELD,
						arrCntType.getInternalName(), arrField, arrDesc);
				// ...,[ idx, cnt,] newval, idx, cnt[]/arr =>
				if (opcode == Opcodes.AASTORE && !isMultiArray)
				{
					originalMethod.visitTypeInsn(Opcodes.CHECKCAST, Type
							.getType(arrayDesc).getInternalName());
				}
				// ...,[ idx, cnt,] newval, idx, cnt[]/arr* =>
				originalMethod.visitInsn(Opcodes.SWAP);
				// ...,[ idx, cnt,] newval, cnt[]/arr*, idx =>
				if (originalArrElemType == Type.LONG_TYPE
						|| originalArrElemType == Type.DOUBLE_TYPE)
				{
					originalMethod.visitInsn(Opcodes.DUP2_X2);
				}
				else
				{
					originalMethod.visitInsn(Opcodes.DUP2_X1);
				}
				// ...,[ idx, cnt,] cnt[]/arr*, idx, newval, cnt[]/arr*, idx =>
				originalMethod.visitInsn(Opcodes.POP2);
				// ...,[ idx, cnt,] cnt[]/arr*, idx, newval =>
				if (opcode == Opcodes.AASTORE && isMultiArray)
				{
					if (originalArrElemType == Type.LONG_TYPE
							|| originalArrElemType == Type.DOUBLE_TYPE)
					{
						originalMethod.visitInsn(Opcodes.DUP2_X2);
					}
					else
					{
						originalMethod.visitInsn(Opcodes.DUP_X2);
					}
				}
				// ...,[ idx, cnt, newval] cnt[]/arr*, idx, newval =>
				originalMethod.visitInsn(opcode);
				// ...,[ idx, cnt, newval] =>
				if (opcode == Opcodes.AASTORE && isMultiArray)
				{
					String owner = null;
					String desc = null;
					if (ArrayUtil.getArrayDim(arrayDesc) > 2)
					{
						owner = MultiArrayContainer.NAME;
						desc = MultiArrayContainer.ARRAY_FIELD_DESC;
					}
					else
					{
						switch (ArrayUtil.getArrayType(arrayDesc.substring(1))
								.getSort())
						{
							case Type.ARRAY:
								owner = MultiArrayContainer.NAME;
								desc = MultiArrayContainer.ARRAY_FIELD_DESC;
								break;
							case Type.BOOLEAN:
								owner = BooleanArrayContainer.NAME;
								desc = BooleanArrayContainer.ARRAY_FIELD_DESC;
								break;
							case Type.BYTE:
								owner = ByteArrayContainer.NAME;
								desc = ByteArrayContainer.ARRAY_FIELD_DESC;
								break;
							case Type.CHAR:
								owner = CharArrayContainer.NAME;
								desc = CharArrayContainer.ARRAY_FIELD_DESC;
								break;
							case Type.DOUBLE:
								owner = DoubleArrayContainer.NAME;
								desc = DoubleArrayContainer.ARRAY_FIELD_DESC;
								break;
							case Type.FLOAT:
								owner = FloatArrayContainer.NAME;
								desc = FloatArrayContainer.ARRAY_FIELD_DESC;
								break;
							case Type.INT:
								owner = IntArrayContainer.NAME;
								desc = IntArrayContainer.ARRAY_FIELD_DESC;
								break;
							case Type.LONG:
								owner = LongArrayContainer.NAME;
								desc = LongArrayContainer.ARRAY_FIELD_DESC;
								break;
							case Type.OBJECT:
								owner = ObjectArrayContainer.NAME;
								desc = ObjectArrayContainer.ARRAY_FIELD_DESC;
								break;
							case Type.SHORT:
								owner = ShortArrayContainer.NAME;
								desc = ShortArrayContainer.ARRAY_FIELD_DESC;
								break;
						}
					}
					originalMethod.visitFieldInsn(Opcodes.GETFIELD, owner,
							ArrayContainer.ARRAY_FIELD_NAME, desc);
					// ..., idx, cnt, arr =>
					originalMethod.visitInsn(Opcodes.SWAP);
					// ..., idx, arr, cnt =>
					originalMethod.visitFieldInsn(Opcodes.GETFIELD,
							MultiArrayContainer.NAME,
							ArrayContainer.ARRAY_FIELD_NAME,
							MultiArrayContainer.ARRAY_FIELD_DESC);
					// ..., idx, arr, arr' =>
					originalMethod.visitInsn(Opcodes.DUP_X2);
					// ..., arr', idx, arr, arr' =>
					originalMethod.visitInsn(Opcodes.POP);
					// ..., arr', idx, arr =>
					originalMethod.visitInsn(Opcodes.AASTORE);
				}
				// ... =>

				if (originalCopyMethod != null)
				{
					copyMethod.visitInsn(opcode);
				}
				return;
			}
			else if (opcode == Opcodes.ARRAYLENGTH)
			{
				// ..., cnt =>
				originalMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
						ArrayContainer.NAME,
						ArrayContainer.GETARRAY_METHOD_NAME,
						ArrayContainer.GETARRAY_METHOD_DESC);
				// ..., obj =>
				originalMethod.visitTypeInsn(Opcodes.CHECKCAST,
						Type.getType(arrayDesc).getInternalName());
				// ..., arr =>
				originalMethod.visitInsn(opcode);
				// ... =>

				/*
				 * Exact same code.
				 */
				if (originalCopyMethod != null)
				{
					copyMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
							ArrayContainer.NAME,
							ArrayContainer.GETARRAY_METHOD_NAME,
							ArrayContainer.GETARRAY_METHOD_DESC);
					copyMethod.visitTypeInsn(Opcodes.CHECKCAST,
							Type.getType(arrayDesc).getInternalName());
					copyMethod.visitInsn(opcode);
				}
				return;
			}
		}

		originalMethod.visitInsn(opcode);
		if (originalCopyMethod != null)
		{
			copyMethod.visitInsn(opcode);
		}
	}

	public void visitIntInsn(int opcode, int operand)
	{
		if (!ignore)
		{
			analyzerAdapter.visitIntInsn(opcode, operand);
		}

		originalMethod.visitIntInsn(opcode, operand);
		if (originalCopyMethod != null)
		{
			copyMethod.visitIntInsn(opcode, operand);
		}

		if (!ignore)
		{
			if (opcode == Opcodes.NEWARRAY)
			{
				/*
				 * If the opcode is NEWARRAY, that means the stack is of the
				 * form: [ ..., arrayref => So, the desired transformation is to
				 * start with the original arrayref on top of the stack, and
				 * finish with the new TxArray arrayref instead. [ ..., arrayref
				 * (TxArray) => In the end, the original array is "lost" in the
				 * heap, and the only objects holding a reference to it will be
				 * the TxArray elements. Legend: arr -> arrayref obj ->
				 * objectref len -> result of ARRAYLENGTH (int) arr* -> arrayref
				 * (TxArray)
				 */
				Type arrCntType = ArrayUtil.getArrayContainerType(operand);
				String arrCntName = arrCntType.getInternalName();
				String arrCntCtorDesc = ArrayUtil
						.getArrayContainerCtorDesc(ArrayUtil
								.getTypeFromNewarrayOpcode(operand));

				// ..., arr =>
				originalMethod.visitTypeInsn(Opcodes.NEW, arrCntName);
				// ..., arr, cnt =>
				originalMethod.visitInsn(Opcodes.DUP_X1);
				// ..., cnt, arr, cnt =>
				originalMethod.visitInsn(Opcodes.SWAP);
				// ..., cnt, cnt, arr =>
				originalMethod.visitMethodInsn(Opcodes.INVOKESPECIAL,
						arrCntName, "<init>", arrCntCtorDesc);
				// ..., cnt =>

				/*
				 * Exactly the same code as above.
				 */
				if (originalCopyMethod != null)
				{
					disableDuplicateInstrumentation(true);

					copyMethod.visitTypeInsn(Opcodes.NEW, arrCntName);
					copyMethod.visitInsn(Opcodes.DUP_X1);
					copyMethod.visitInsn(Opcodes.SWAP);
					copyMethod.visitMethodInsn(Opcodes.INVOKESPECIAL,
							arrCntName, "<init>", arrCntCtorDesc);

					disableDuplicateInstrumentation(false);
				}
			}
		}
	}

	public void visitJumpInsn(int opcode, Label label)
	{
		if (!ignore)
		{
			analyzerAdapter.visitJumpInsn(opcode, label);
		}
		originalMethod.visitJumpInsn(opcode, label);
		if (originalCopyMethod != null)
		{
			copyMethod.visitJumpInsn(opcode, getLabel(label));
		}
	}

	public void visitLabel(Label label)
	{
		if (!ignore)
		{
			analyzerAdapter.visitLabel(label);
		}
		originalMethod.visitLabel(label);
		if (originalCopyMethod != null)
		{
			copyMethod.visitLabel(getLabel(label));
		}
	}

	public void visitLdcInsn(Object cst)
	{
		if (!ignore)
		{
			analyzerAdapter.visitLdcInsn(cst);
		}
		originalMethod.visitLdcInsn(cst);
		if (originalCopyMethod != null)
		{
			copyMethod.visitLdcInsn(cst);
		}
	}

	public void visitLineNumber(int line, Label start)
	{
		originalMethod.visitLineNumber(line, start);
		if (originalCopyMethod != null)
		{
			copyMethod.visitLineNumber(line, getLabel(start));
		}
	}

	public void visitLocalVariable(String name, String desc, String signature,
			Label start, Label end, int index)
	{
		originalMethod.visitLocalVariable(name, desc, signature, start, end,
				index);
		if (originalCopyMethod != null)
		{
			copyMethod.visitLocalVariable(name, desc, signature,
					getLabel(start), getLabel(end), index);
		}
	}

	public void visitLookupSwitchInsn(Label dflt, int[] keys, Label[] labels)
	{
		if (!ignore)
		{
			analyzerAdapter.visitLookupSwitchInsn(dflt, keys, labels);
		}
		originalMethod.visitLookupSwitchInsn(dflt, keys, labels);
		if (originalCopyMethod != null)
		{
			copyMethod.visitLookupSwitchInsn(getLabel(dflt), keys,
					getCopyLabels(labels));
		}
	}

	public void visitMaxs(int maxStack, int maxLocals)
	{
		if (!ignore)
		{
			analyzerAdapter.visitMaxs(maxStack, maxLocals);
		}
		originalMethod.visitMaxs(maxStack, maxLocals);
		if (originalCopyMethod != null)
		{
			copyMethod.visitMaxs(maxStack, maxLocals);
		}
	}

	public boolean isBootstrapClass(String owner)
	{
		owner = owner.replace('/', '.');
		return owner.matches("java\\..*|javax\\..*");
	}

	public boolean hasArrayReturnOrParameter(String desc)
	{
		Method m = new Method("dummy", desc);
		for (Type t : m.getArgumentTypes())
		{
			if (t.getSort() == Type.ARRAY)
			{
				return true;
			}
		}
		return m.getReturnType().getSort() == Type.ARRAY;
	}

	public boolean hasArrayReturnOrArgument(String desc)
	{
		Method m = new Method("dummy", desc);
		int numberOfParameters = m.getArgumentTypes().length;

		Object obj;
		String str;
		int stackSize = analyzerAdapter.stack.size();
		for (int i = numberOfParameters; i > 0; i--)
		{
			obj = analyzerAdapter.stack.get(stackSize - i);

			if (!(obj instanceof Integer))
			{
				str = (String) obj;
				if (str.lastIndexOf('[') != -1
						|| ArrayUtil.isArrayContainer(desc))
					return true;
			}
		}

		return m.getReturnType().getSort() == Type.ARRAY;
	}

	public String updateParametersToArray(String desc)
	{
		Method m = new Method("dummy", desc);
		Type[] params = m.getArgumentTypes();
		int numberOfParameters = params.length;
		Type[] newParams = new Type[numberOfParameters];

		Object obj;
		String str;
		int stackSize = analyzerAdapter.stack.size();

		// i is the argument's offset from the top of the stack
		// j is its index on the Type[] arrays
		for (int i = numberOfParameters, j = 0; i > 0; i--, j++)
		{
			obj = analyzerAdapter.stack.get(stackSize - i);

			if (!(obj instanceof Integer))
			{
				str = (String) obj;
				if (str.lastIndexOf('[') != -1)
					newParams[j] = Type.getType(str);
				else
					newParams[j] = params[j];
			}
			else
			{
				newParams[j] = params[j];
			}
		}

		return new Method("dummy", m.getReturnType(), newParams)
				.getDescriptor();
	}

	public void visitMethodInsn(int opcode, String owner, String name,
			String desc)
	{
		if (!ignore
				&& !(name.equals("main") && desc
						.equals("([Ljava/lang/String;)V")))
		{
			boolean isBC = isBootstrapClass(owner);

			// If it is a Bootstrap class with at least one array as argument.
			if (isBC && hasArrayReturnOrArgument(desc))
			{
				String nimDesc = updateParametersToArray(desc);

				NonInsnMethod nim = clazzT.new NonInsnMethod(name, nimDesc,
						desc, owner, opcode);
				if (!clazzT.nonInsnMethods.contains(nim))
				{
					clazzT.nonInsnMethods.add(nim);
				}

				originalMethod.visitInsn(Opcodes.ACONST_NULL);
				originalMethod.visitMethodInsn(Opcodes.INVOKESTATIC,
						nim.insnOwner, nim.insnName, nim.insnDesc);

				if (opcode == Opcodes.INVOKESPECIAL)
				{
					originalMethod.visitInsn(Opcodes.DUP_X2);
					originalMethod.visitInsn(Opcodes.POP);
					originalMethod.visitInsn(Opcodes.POP2);
				}

				if (originalCopyMethod != null)
				{
					disableDuplicateInstrumentation(true);

					copyMethod.visitInsn(Opcodes.ACONST_NULL);
					copyMethod.visitMethodInsn(Opcodes.INVOKESTATIC,
							nim.insnOwner, nim.insnName, nim.insnDesc);

					if (opcode == Opcodes.INVOKESPECIAL)
					{
						copyMethod.visitInsn(Opcodes.DUP_X2);
						copyMethod.visitInsn(Opcodes.POP);
						copyMethod.visitInsn(Opcodes.POP2);
					}

					disableDuplicateInstrumentation(false);
				}
			}
			else
			{
				if (owner.charAt(0) == '[')
				{
					Type t = Type.getType(owner);
					if (t.getSort() == Type.ARRAY)
					{
						owner = ArrayUtil.getArrayContainerType(t)
								.getDescriptor();
					}
				}

				originalMethod.visitMethodInsn(opcode, owner, name, ArrayUtil
						.updateMethodArrayArgumentsAndReturn(desc)
						.getDescriptor());
				if (originalCopyMethod != null)
				{
					copyMethod.visitMethodInsn(opcode, owner, name, ArrayUtil
							.updateMethodArrayArgumentsAndReturn(desc)
							.getDescriptor());
				}
			}
		}
		else
		{
			originalMethod.visitMethodInsn(opcode, owner, name, desc);
			if (originalCopyMethod != null)
			{
				copyMethod.visitMethodInsn(opcode, owner, name, desc);
			}
		}

		if (!ignore)
		{
			analyzerAdapter.visitMethodInsn(opcode, owner, name, desc);
		}
	}

	public void visitMultiANewArrayInsn(String desc, int dims)
	{
		if (!ignore)
		{
			analyzerAdapter.visitMultiANewArrayInsn(desc, dims);
		}

		originalMethod.visitMultiANewArrayInsn(desc, dims);
		if (originalCopyMethod != null)
			copyMethod.visitMultiANewArrayInsn(desc, dims);

		if (!ignore)
		{
			// ..., arr =>
			originalMethod.visitTypeInsn(Opcodes.NEW, MultiArrayContainer.NAME);
			// ..., arr, obj =>
			originalMethod.visitInsn(Opcodes.DUP_X1);
			// ..., obj, arr, obj =>
			originalMethod.visitInsn(Opcodes.SWAP);
			// ..., obj, obj, arr =>
			originalMethod.visitInsn(Opcodes.ICONST_1);
			// ..., obj, obj, arr, I =>
			// XXX Is BIPUSH enough?
			originalMethod.visitIntInsn(Opcodes.BIPUSH, dims);
			// ..., obj, obj, arr, I, I =>
			// XXX Is BIPUSH enough?
			originalMethod.visitIntInsn(Opcodes.BIPUSH,
					ArrayUtil.getArrayDim(desc));
			// ..., obj, obj, arr, I, I, I =>
			originalMethod.visitIntInsn(Opcodes.BIPUSH,
					ArrayUtil.getArrayType(desc).getSort());
			// ..., obj, obj, arr, I, I, I, I =>
			originalMethod.visitMethodInsn(Opcodes.INVOKESPECIAL,
					MultiArrayContainer.NAME, "<init>",
					MultiArrayContainer.CTOR_DESC);
			// ..., cnt =>

			/*
			 * Exactly the same code as above.
			 */
			if (originalCopyMethod != null)
			{
				disableDuplicateInstrumentation(true);

				copyMethod.visitTypeInsn(Opcodes.NEW, MultiArrayContainer.NAME);
				copyMethod.visitInsn(Opcodes.DUP_X1);
				copyMethod.visitInsn(Opcodes.SWAP);
				copyMethod.visitInsn(Opcodes.ICONST_1);
				copyMethod.visitIntInsn(Opcodes.BIPUSH, dims);
				copyMethod.visitIntInsn(Opcodes.BIPUSH,
						ArrayUtil.getArrayDim(desc));
				copyMethod.visitIntInsn(Opcodes.BIPUSH,
						ArrayUtil.getArrayType(desc).getSort());
				copyMethod.visitMethodInsn(Opcodes.INVOKESPECIAL,
						MultiArrayContainer.NAME, "<init>",
						MultiArrayContainer.CTOR_DESC);

				disableDuplicateInstrumentation(false);
			}
		}
	}

	public AnnotationVisitor visitParameterAnnotation(int parameter,
			String desc, boolean visible)
	{
		return new MethodAnnotationVisitor(
				originalMethod.visitParameterAnnotation(parameter, desc,
						visible), copyMethod.visitParameterAnnotation(
						parameter, desc, visible));
	}

	public void visitTableSwitchInsn(int min, int max, Label dflt,
			Label[] labels)
	{
		if (!ignore)
		{
			analyzerAdapter.visitTableSwitchInsn(min, max, dflt, labels);
		}
		originalMethod.visitTableSwitchInsn(min, max, dflt, labels);
		if (originalCopyMethod != null)
		{
			copyMethod.visitTableSwitchInsn(min, max, getLabel(dflt),
					getCopyLabels(labels));
		}
	}

	public void visitTryCatchBlock(Label start, Label end, Label handler,
			String type)
	{
		originalMethod.visitTryCatchBlock(start, end, handler, type);
		if (originalCopyMethod != null)
		{
			copyMethod.visitTryCatchBlock(getLabel(start), getLabel(end),
					getLabel(handler), type);
		}
	}

	public void visitTypeInsn(int opcode, String type)
	{
		if (!ignore)
		{
			analyzerAdapter.visitTypeInsn(opcode, type);

			if (opcode == Opcodes.ANEWARRAY)
			{
				/*
				 * If the opcode is ANEWARRAY, that means the stack is of the
				 * form: [ ..., arrayref => So, the desired transformation is to
				 * start with the original arrayref on top of the stack, and
				 * finish with the new TxArray arrayref instead. [ ..., arrayref
				 * (TxArray) => In the end, the original array is "lost" in the
				 * heap, and the only objects holding a reference to it will be
				 * the TxArray elements. Legend: arr -> arrayref obj ->
				 * objectref len -> result of ARRAYLENGTH (int) arr* -> arrayref
				 * (TxArray)
				 */
				boolean isMultiArray = type.lastIndexOf('[') != -1;

				// ..., I (count) =>
				originalMethod.visitTypeInsn(opcode, type);
				if (originalCopyMethod != null)
					copyMethod.visitTypeInsn(opcode, type);
				// ..., arr =>

				if (isMultiArray)
				{
					originalMethod.visitTypeInsn(Opcodes.NEW,
							MultiArrayContainer.NAME);
				}
				else
				{
					originalMethod.visitTypeInsn(Opcodes.NEW,
							ObjectArrayContainer.NAME);
				}
				// ..., arr, obj =>
				originalMethod.visitInsn(Opcodes.DUP_X1);
				// ..., obj, arr, obj =>
				originalMethod.visitInsn(Opcodes.SWAP);
				// ..., obj, obj, arr =>
				if (isMultiArray)
				{
					originalMethod.visitInsn(Opcodes.ICONST_1);
					// ..., obj, obj, arr, I =>
					originalMethod.visitInsn(Opcodes.ICONST_1);
					// ..., obj, obj, arr, I, I =>
					originalMethod.visitIntInsn(Opcodes.BIPUSH,
							ArrayUtil.getArrayDim(type) + 1);
					// ..., obj, obj, arr, I, I, I =>
					originalMethod.visitInsn(Opcodes.ICONST_0);
					// ..., obj, obj, arr, I, I, I, I =>
					originalMethod.visitMethodInsn(Opcodes.INVOKESPECIAL,
							MultiArrayContainer.NAME, "<init>",
							MultiArrayContainer.CTOR_DESC);
				}
				else
				{
					originalMethod.visitMethodInsn(Opcodes.INVOKESPECIAL,
							ObjectArrayContainer.NAME, "<init>",
							ObjectArrayContainer.CTOR_DESC);
				}
				// ..., cnt =>

				/*
				 * Exactly the same code as above.
				 */
				if (originalCopyMethod != null)
				{
					disableDuplicateInstrumentation(true);

					if (isMultiArray)
					{
						copyMethod.visitTypeInsn(Opcodes.NEW,
								MultiArrayContainer.NAME);
					}
					else
					{
						copyMethod.visitTypeInsn(Opcodes.NEW,
								ObjectArrayContainer.NAME);
					}
					copyMethod.visitInsn(Opcodes.DUP_X1);
					copyMethod.visitInsn(Opcodes.SWAP);
					if (isMultiArray)
					{
						copyMethod.visitInsn(Opcodes.ICONST_1);
						copyMethod.visitInsn(Opcodes.ICONST_1);
						copyMethod.visitIntInsn(Opcodes.BIPUSH,
								ArrayUtil.getArrayDim(type) + 1);
						copyMethod.visitInsn(Opcodes.ICONST_0);
						copyMethod.visitMethodInsn(Opcodes.INVOKESPECIAL,
								MultiArrayContainer.NAME, "<init>",
								MultiArrayContainer.CTOR_DESC);
					}
					else
					{
						copyMethod.visitMethodInsn(Opcodes.INVOKESPECIAL,
								ObjectArrayContainer.NAME, "<init>",
								ObjectArrayContainer.CTOR_DESC);
					}

					disableDuplicateInstrumentation(false);
				}

				return;
			}
			else if (opcode == Opcodes.CHECKCAST)
			{
				if (type.lastIndexOf("[") == -1)
					type = "L" + type + ";";

				Type t = Type.getType(type);

				if (t.getSort() == Type.ARRAY)
					t = ArrayUtil.getArrayContainerType(t);

				originalMethod.visitTypeInsn(opcode, t.getInternalName());
				if (originalCopyMethod != null)
					copyMethod.visitTypeInsn(opcode, t.getInternalName());

				return;
			}
		}

		originalMethod.visitTypeInsn(opcode, type);
		if (originalCopyMethod != null)
			copyMethod.visitTypeInsn(opcode, type);
	}

	public void visitVarInsn(int opcode, int var)
	{
		if (!ignore)
		{
			analyzerAdapter.visitVarInsn(opcode, var);
		}
		originalMethod.visitVarInsn(opcode, var);
		if (originalCopyMethod != null)
		{
			copyMethod.visitVarInsn(opcode, var);
		}
	}

	private Label[] getCopyLabels(Label[] labels)
	{
		Label[] copyLabels = new Label[labels.length];
		for (int i = 0; i < labels.length; ++i)
		{
			copyLabels[i] = getLabel(labels[i]);
		}
		return copyLabels;
	}

	private Label getLabel(Label label)
	{
		Label duplicateLabel = labelMap.get(label);
		if (duplicateLabel == null)
		{
			duplicateLabel = new Label();
			labelMap.put(label, duplicateLabel);
		}
		return duplicateLabel;
	}
}
