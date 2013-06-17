package org.deuce.transform.localmetadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.full.FullReplicationSerializer;
import org.deuce.distribution.replication.partial.PartialReplicationSerializer;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.objectweb.asm.commons.AnalyzerAdapter;
import org.deuce.objectweb.asm.commons.Method;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/*
 * TODOs Class fields should not be instrumented inside the constructor We need
 * to implement this optimization! I believe that implementing this optimization
 * will fix the following issue. Consider the following class, in bytecode:
 * public class asmconstructortest.Test2 extends java.lang.Object { public int
 * property; public asmconstructortest.Test2(); Code: 0: aload_0 1: iconst_1 2:
 * putfield #10; //Field property:I 5: aload_0 6: invokespecial #12; //Method
 * java/lang/Object."<init>":()V 9: return } While the putfield instruction
 * before the call to the super's <init> is impossible to produce programming in
 * Java, it is possible in bytecode. And it executes. But it wouldn't run if
 * instead of a putfield it was a getfield. The following error occurs (line 5
 * of Main2.java is "new Test2()"): Exception in thread "main"
 * java.lang.VerifyError: (class: asmconstructortest/Test2, method: <init>
 * signature: ()V) Expecting to find object/array on stack at
 * asmconstructortest.Main2.main(Main2.java:5) This is probably because self is
 * not initialized yet. The transaction-aware duplication of Test2(),
 * Test2(IContext), will inject before the aforementioned putfield a getfield to
 * the corresponding metadata. That will cause a similar exception to the above
 * to be thrown.
 */
/*
 * TODOs: Try to move @Bootstrap related transformations to other class
 */
@ExcludeTM
public class ConstructorMethodTransformer extends AnalyzerAdapter
{
	final static public String CLASS_BASE = "__CLASS_BASE__";

	protected final List<Field> fields;
	protected final String fieldsHolderName;
	protected boolean callsOtherCtor;
	protected final String className;

	// ################################# @Bootstrap @Partial
	protected final Map<String, Integer> field2OID;
	protected final Set<String> partialRepFields;
	protected boolean partial;

	public ConstructorMethodTransformer(MethodVisitor mv, List<Field> fields,
			Map<String, Integer> field2OID, Set<String> partialRepFields,
			boolean partial, String className, int access, String name,
			String desc, String fieldsHolderName)
	{
		super(className, access, name, desc, mv);
		this.fields = fields;
		this.fieldsHolderName = fieldsHolderName;
		this.callsOtherCtor = false;
		this.className = className;
		this.field2OID = field2OID;
		this.partialRepFields = partialRepFields;
		this.partial = partial;
	}

	protected void initMetadataField(Field field)
	{
		// stack: ... =>
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		// stack: ..., Object (this) =>
		mv.visitTypeInsn(Opcodes.NEW, field.getType().getInternalName());
		// stack: ..., Object (this), TxField =>
		mv.visitInsn(Opcodes.DUP);
		// stack: ..., Object (this), TxField, TxField =>
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		// stack: ..., Object (this), TxField, TxField, Object (this) =>
		mv.visitFieldInsn(Opcodes.GETSTATIC, fieldsHolderName, "__STATIC__"
				+ field.getFieldNameAddress(), Type.LONG_TYPE.getDescriptor());
		// stack: ..., Object (this), TxField, TxField,
		// Object (this), long (this field's address) =>
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, field.getType()
				.getInternalName(), "<init>", TxField.CTOR_DESC);
		// stack: ..., Object (this), TxField =>

		Integer oid = field2OID.get(field.getFieldName());
		boolean partialField = partialRepFields.contains(field.getFieldName());
		if (partial)
		{
			// ##### setType (VBoxField)
			// stack: ..., Object (this), TxField =>
			mv.visitInsn(Opcodes.DUP);
			// stack: ..., Object (this), TxField, TxField =>
			mv.visitTypeInsn(Opcodes.CHECKCAST, VBoxField.NAME);
			// stack: ..., Object (this), TxField, VBoxField =>

			switch (field.getOriginalType().getSort())
			{
				case Type.BYTE:
					super.visitLdcInsn(Type.BYTE);
					break;
				case Type.BOOLEAN:
					super.visitLdcInsn(Type.BOOLEAN);
					break;
				case Type.CHAR:
					super.visitLdcInsn(Type.CHAR);
					break;
				case Type.SHORT:
					super.visitLdcInsn(Type.SHORT);
					break;
				case Type.INT:
					super.visitLdcInsn(Type.INT);
					break;
				case Type.LONG:
					super.visitLdcInsn(Type.LONG);
					break;
				case Type.FLOAT:
					super.visitLdcInsn(Type.FLOAT);
					break;
				case Type.DOUBLE:
					super.visitLdcInsn(Type.DOUBLE);
					break;
				case Type.OBJECT:
					super.visitLdcInsn(Type.OBJECT);
					break;
			}
			// stack: ..., Object (this), TxField, VBoxField, Type =>
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, VBoxField.NAME,
					VBoxField.SET_TYPE_METHOD_NAME,
					VBoxField.SET_TYPE_METHOD_DESC);

			// ##### setMetadata
			// stack: ..., Object (this), TxField =>
			mv.visitInsn(Opcodes.DUP);
			// stack: ..., Object (this), TxField, TxField =>
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, TribuDSTM.NAME,
					TribuDSTM.GETSERIALIZER_METHOD_NAME,
					TribuDSTM.GETSERIALIZER_METHOD_DESC);
			// stack: ..., Object (this), TxField, TxField, ObjectSerializer =>
			mv.visitTypeInsn(Opcodes.CHECKCAST,
					PartialReplicationSerializer.NAME);
			// stack: ..., Object (this), TxField, TxField,
			// PartialReplicationSerializer =>
			mv.visitInsn(Opcodes.SWAP);
			// stack: ..., Object (this), TxField, PartialReplicationSerializer,
			// TxField =>

			if (oid != null)
			{ // Bootstrap field -> fullRepOID(oid) [id:rand(oid),group:ALL]
				// stack: ..., Object (this), TxField,
				// PartialReplicationSerializer, TxField =>
				mv.visitLdcInsn(oid);
				// stack: ..., Object (this), TxField,
				// PartialReplicationSerializer, TxField,
				// int =>
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
						PartialReplicationSerializer.NAME,
						PartialReplicationSerializer.BOOTSTRAP_METHOD_NAME,
						PartialReplicationSerializer.BOOTSTRAP_METHOD_DESC);
				// stack: ..., Object (this), TxField =>
			}
			else if (partialField)
			{ // Partial field -> partialRepOID() [id:rand(),group:null]
				// stack: ..., Object (this), TxField,
				// PartialReplicationSerializer, TxField =>
				mv.visitMethodInsn(
						Opcodes.INVOKEVIRTUAL,
						PartialReplicationSerializer.NAME,
						PartialReplicationSerializer.CREATE_PARTIAL_METADATA_METHOD_NAME,
						PartialReplicationSerializer.CREATE_PARTIAL_METADATA_METHOD_DESC);
				// stack: ..., Object (this), TxField =>
			}
			else
			{ // Full field -> fullRepOID() [id:rand(),group:ALL]
				// stack: ..., Object (this), TxField,
				// PartialReplicationSerializer, TxField =>
				mv.visitMethodInsn(
						Opcodes.INVOKEVIRTUAL,
						PartialReplicationSerializer.NAME,
						PartialReplicationSerializer.CREATE_FULL_METADATA_METHOD_NAME,
						PartialReplicationSerializer.CREATE_FULL_METADATA_METHOD_DESC);
				// stack: ..., Object (this), TxField =>
			}
		}
		else
		{ // @Bootstrap, assumes FullReplicationSerializer
			if (oid != null)
			{
				// stack: ..., Object (this), TxField =>
				mv.visitInsn(Opcodes.DUP);
				// stack: ..., Object (this), TxField, TxField =>
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, TribuDSTM.NAME,
						TribuDSTM.GETSERIALIZER_METHOD_NAME,
						TribuDSTM.GETSERIALIZER_METHOD_DESC);
				// stack: ..., Object (this), TxField, TxField, ObjectSerializer
				// =>
				mv.visitTypeInsn(Opcodes.CHECKCAST,
						FullReplicationSerializer.NAME);
				// stack: ..., Object (this), TxField, TxField,
				// FullReplicationSerializer =>
				mv.visitInsn(Opcodes.SWAP);
				// stack: ..., Object (this), TxField,
				// FullReplicationSerializer,
				// TxField =>
				mv.visitLdcInsn(oid);
				// stack: ..., Object (this), TxField,
				// FullReplicationSerializer,
				// TxField, int =>
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
						FullReplicationSerializer.NAME,
						FullReplicationSerializer.BOOTSTRAP_METHOD_NAME,
						FullReplicationSerializer.BOOTSTRAP_METHOD_DESC);
				// stack: ..., Object (this), TxField =>
			}
		}
		// stack: ..., Object (this), TxField =>
	}

	protected void addField(Field field)
	{
		// stack: ... =>
		initMetadataField(field);
		// stack: ..., Object (this), TxField =>
		mv.visitFieldInsn(Opcodes.PUTFIELD, fieldsHolderName,
				field.getFieldNameAddress(), field.getType().getDescriptor());
		// stack: ... =>
	}

	@Override
	public void visitInsn(int opcode)
	{
		if (opcode == Opcodes.RETURN && !callsOtherCtor)
		{
			((MethodTransformer) mv).disableDuplicateInstrumentation(true);
			((MethodTransformer) mv).disableMethodInstrumentation(true);
			if (fields.size() > 0)
			{
				for (Field field : fields)
				{
					if ((field.getAccess() & Opcodes.ACC_STATIC) == 0)
					{
						addField(field);
					}
				}
			}
			((MethodTransformer) mv).disableMethodInstrumentation(false);
			((MethodTransformer) mv).disableDuplicateInstrumentation(false);
		}

		super.visitInsn(opcode);
	}

	@Override
	public void visitEnd()
	{
		// ((MethodTransformer)mv).disableDuplicateInstrumentation(false);
		super.visitEnd();
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc)
	{
		if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>"))
		{
			/*
			 * if (owner.equals(Type.getInternalName(Object.class)) &&
			 * isUniqueObject) { super.visitMethodInsn(opcode,
			 * UniqueObject.NAME, name, desc); return; } else
			 */if (owner.equals(className))
			{
				// The method is created merely to know the #params
				int nParams = new Method(name, desc).getArgumentTypes().length;

				// With that number, we go down the stack #params and check
				// what's
				// there
				Object stackObj = stack.get(stack.size() - nParams - 1);

				// Is it <this>, uninitialized?
				if (stackObj instanceof Integer
						&& ((Integer) stackObj) == Opcodes.UNINITIALIZED_THIS)
				{
					callsOtherCtor = true;
				}
			}
		}

		super.visitMethodInsn(opcode, owner, name, desc);
	}
}
