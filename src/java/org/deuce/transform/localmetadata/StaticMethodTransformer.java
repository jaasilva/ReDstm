package org.deuce.transform.localmetadata;

import java.util.List;
import java.util.Map;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.full.FullReplicationSerializer;
import org.deuce.objectweb.asm.MethodAdapter;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/*
 * TODO: Try to move @Bootstrap related transformations to other class
 */
@ExcludeTM
public class StaticMethodTransformer extends MethodAdapter
{
	final static public String CLASS_BASE = "__CLASS_BASE__";

	protected final List<Field> fields;
	protected final String className;
	protected final MethodVisitor staticMethod;
	protected final String fieldsHolderName;
	protected final String staticField;

	// FIXME @Bootstrap
	private final Map<String, Integer> field2OID;

	public StaticMethodTransformer(MethodVisitor mv,
			MethodVisitor staticMethod, List<Field> fields,
			Map<String, Integer> field2OID, String staticField,
			String className, String fieldsHolderName)
	{

		super(mv);
		this.staticMethod = staticMethod;
		this.fields = fields;
		this.staticField = staticField;
		this.className = className;
		this.fieldsHolderName = fieldsHolderName;
		this.field2OID = field2OID;
	}

	@Override
	public void visitCode()
	{
		((MethodTransformer) mv).disableMethodInstrumentation(true);
		if (staticField != null)
		{
			addClassBase(staticField);
		}

		if (fields.size() > 0)
		{
			for (Field field : fields)
			{
				if ((field.getAccess() & Opcodes.ACC_STATIC) != 0)
				{
					addField(field);
				}
				else
				{
					addField2(field);
				}
			}
		}
		((MethodTransformer) mv).disableMethodInstrumentation(false);
	}

	protected void addField2(Field field)
	{
		super.visitLdcInsn(Type.getObjectType(className));
		super.visitLdcInsn(field.getFieldName());
		super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
				"getDeclaredField",
				"(Ljava/lang/String;)Ljava/lang/reflect/Field;");
		super.visitMethodInsn(Opcodes.INVOKESTATIC,
				"org/deuce/reflection/AddressUtil", "getAddress",
				"(Ljava/lang/reflect/Field;)J");

		super.visitFieldInsn(Opcodes.PUTSTATIC, fieldsHolderName, "__STATIC__"
				+ field.getFieldNameAddress(), "J");

	}

	protected void addField(Field field)
	{
		// stack: ... =>
		super.visitTypeInsn(Opcodes.NEW, field.getType().getInternalName());
		// stack: ..., TxField =>
		super.visitInsn(Opcodes.DUP);
		// stack: ..., TxField, TxField =>
		super.visitFieldInsn(Opcodes.GETSTATIC, fieldsHolderName, CLASS_BASE,
				"Ljava/lang/Object;");
		// stack: ..., TxField, TxField, Object =>
		super.visitLdcInsn(Type.getObjectType(this.className));
		// stack: ..., TxField, TxField, Object, Class (owner) =>
		super.visitLdcInsn(field.getFieldName());
		// stack: ..., TxField, TxField, Object, Class (owner),
		// String (this field) =>
		super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
				"getDeclaredField",
				"(Ljava/lang/String;)Ljava/lang/reflect/Field;");
		// stack: ..., TxField, TxField, Object, Field =>
		super.visitMethodInsn(Opcodes.INVOKESTATIC,
				"org/deuce/reflection/AddressUtil", "getAddress",
				"(Ljava/lang/reflect/Field;)J");
		// stack: ..., TxField, TxField, Object,
		// long (this field's address) =>
		super.visitMethodInsn(Opcodes.INVOKESPECIAL, field.getType()
				.getInternalName(), "<init>", TxField.CTOR_DESC);
		// stack: ..., TxField =>

		// XXX @Bootstrap, assumes FullReplicationSerializer
		Integer oid = field2OID.get(field.getFieldName());
		if (oid != null)
		{
			super.visitInsn(Opcodes.DUP);
			// stack: ..., TxField, TxField =>
			super.visitMethodInsn(Opcodes.INVOKESTATIC, TribuDSTM.NAME,
					TribuDSTM.GETSERIALIZER_METHOD_NAME,
					TribuDSTM.GETSERIALIZER_METHOD_DESC);
			// stack: ..., TxField, TxField, ObjectSerializer =>
			super.visitTypeInsn(Opcodes.CHECKCAST,
					FullReplicationSerializer.NAME);
			// stack: ..., TxField, TxField, FullReplicationSerializer =>
			super.visitInsn(Opcodes.SWAP);
			// stack: ..., TxField, FullReplicationSerializer, TxField =>
			super.visitLdcInsn(oid);
			// stack: ..., TxField, FullReplicationSerializer, TxField, int =>
			super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
					FullReplicationSerializer.NAME,
					FullReplicationSerializer.BOOTSTRAP_METHOD_NAME,
					FullReplicationSerializer.BOOTSTRAP_METHOD_DESC);
			// stack: ..., TxField =>
		}

		// stack: ..., TxField =>
		super.visitFieldInsn(Opcodes.PUTSTATIC, fieldsHolderName,
				field.getFieldNameAddress(), field.getType().getDescriptor());
		// stack: ... =>
	}

	protected void addClassBase(String staticFieldBase)
	{
		super.visitLdcInsn(Type.getObjectType(className));
		super.visitLdcInsn(staticFieldBase);
		super.visitMethodInsn(Opcodes.INVOKESTATIC,
				"org/deuce/reflection/AddressUtil", "staticFieldBase",
				"(Ljava/lang/Class;Ljava/lang/String;)Ljava/lang/Object;");
		super.visitFieldInsn(Opcodes.PUTSTATIC, fieldsHolderName, CLASS_BASE,
				"Ljava/lang/Object;");
	}

	@Override
	public void visitMaxs(int maxStack, int maxLocals)
	{
		super.visitMaxs(maxStack, maxLocals);
	}

	@Override
	public void visitEnd()
	{
		super.visitEnd();
		// TODO can we do it cleaner?
		if (staticMethod != null && super.mv != staticMethod)
			staticMethod.visitEnd();
	}
}
