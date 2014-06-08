package org.deuce.transform.localmetadata;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.deuce.distribution.Defaults;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.full.FullReplicationSerializer;
import org.deuce.distribution.replication.partial.PartialReplicationSerializer;
import org.deuce.objectweb.asm.MethodAdapter;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.transaction.score.field.VBoxField;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/*
 * TODOs: Try to move @Bootstrap related transformations to other class
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

	// ################################# @Bootstrap @Partial
	protected final Map<String, Integer> field2OID;
	protected final Set<String> partialRepFields;
	protected boolean partial;

	public StaticMethodTransformer(MethodVisitor mv,
			MethodVisitor staticMethod, List<Field> fields,
			Map<String, Integer> field2OID, Set<String> partialRepFields,
			boolean partial, String staticField, String className,
			String fieldsHolderName)
	{
		super(mv);
		this.staticMethod = staticMethod;
		this.fields = fields;
		this.staticField = staticField;
		this.className = className;
		this.fieldsHolderName = fieldsHolderName;
		this.field2OID = field2OID;
		this.partialRepFields = partialRepFields;
		this.partial = partial;
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

		// super.visitInsn(Opcodes.DUP);
		// super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
		// "Ljava/io/PrintStream;");
		// super.visitInsn(Opcodes.SWAP);
		// super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Object",
		// "getClass", "()Ljava/lang/Class;");
		// super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/lang/Class",
		// "getName", "()Ljava/lang/String;");
		// super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
		// "println", "(Ljava/lang/String;)V");
		//
		// super.visitInsn(Opcodes.DUP);
		// super.visitFieldInsn(Opcodes.GETSTATIC, "java/lang/System", "out",
		// "Ljava/io/PrintStream;");
		// super.visitInsn(Opcodes.SWAP);
		// super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/System",
		// "identityHashCode", "(Ljava/lang/Object;)I");
		// super.visitMethodInsn(Opcodes.INVOKESTATIC, "java/lang/Integer",
		// "toHexString", "(I)Ljava/lang/String;");
		//
		// super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, "java/io/PrintStream",
		// "println", "(Ljava/lang/String;)V");

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

		Integer oid = field2OID.get(field.getFieldName());
		boolean partialField = partialRepFields.contains(field.getFieldName());
		if (partial)
		{
			// ##### setType (VBoxField)
			// stack: ..., TxField =>
			super.visitInsn(Opcodes.DUP);
			// stack: ..., TxField, TxField =>
			super.visitTypeInsn(Opcodes.CHECKCAST, VBoxField.NAME);
			// stack: ..., TxField, VBoxField =>
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
			case Type.ARRAY:
				super.visitLdcInsn(Type.ARRAY);
				break;
			}
			// stack: ..., TxField, VBoxField, Type =>
			super.visitMethodInsn(Opcodes.INVOKEVIRTUAL, VBoxField.NAME,
					VBoxField.SET_TYPE_METHOD_NAME,
					VBoxField.SET_TYPE_METHOD_DESC);

			// ##### setMetadata
			// stack: ..., TxField =>
			super.visitInsn(Opcodes.DUP);
			// stack: ..., TxField, TxField =>
			super.visitMethodInsn(Opcodes.INVOKESTATIC, TribuDSTM.NAME,
					TribuDSTM.GETSERIALIZER_METHOD_NAME,
					TribuDSTM.GETSERIALIZER_METHOD_DESC);
			// stack: ..., TxField, TxField, ObjectSerializer =>
			super.visitTypeInsn(Opcodes.CHECKCAST,
					PartialReplicationSerializer.NAME);
			// stack: ..., TxField, TxField, PartialReplicationSerializer =>
			super.visitInsn(Opcodes.SWAP);
			// stack: ..., TxField, PartialReplicationSerializer, TxField =>

			if (oid != null)
			{ // Bootstrap field -> fullRepOID(oid) [id:rand(oid),group:ALL]
				// stack: ..., TxField, PartialReplicationSerializer, TxField =>
				super.visitLdcInsn(oid);
				// stack: ..., TxField, PartialReplicationSerializer, TxField,
				// int =>
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
						PartialReplicationSerializer.NAME,
						PartialReplicationSerializer.BOOTSTRAP_METHOD_NAME,
						PartialReplicationSerializer.BOOTSTRAP_METHOD_DESC);
				// stack: ..., TxField =>
			}
			else if (partialField)
			{ // Partial field -> partialRepOID() [id:rand(),group:null]
				// stack: ..., TxField, PartialReplicationSerializer, TxField =>
				super.visitMethodInsn(
						Opcodes.INVOKEVIRTUAL,
						PartialReplicationSerializer.NAME,
						PartialReplicationSerializer.CREATE_PARTIAL_METADATA_METHOD_NAME,
						PartialReplicationSerializer.CREATE_PARTIAL_METADATA_METHOD_DESC);
				// stack: ..., TxField =>
			}
			else
			{ // Full field -> fullRepOID() [id:rand(),group:ALL]
				// stack: ..., TxField, PartialReplicationSerializer, TxField =>
				super.visitMethodInsn(
						Opcodes.INVOKEVIRTUAL,
						PartialReplicationSerializer.NAME,
						PartialReplicationSerializer.CREATE_FULL_METADATA_METHOD_NAME,
						PartialReplicationSerializer.CREATE_FULL_METADATA_METHOD_DESC);
				// stack: ..., TxField =>
			}
		}
		else
		{ // @Bootstrap, assumes FullReplicationSerializer
			if (oid != null)
			{
				// stack: ..., TxField =>
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
				// stack: ..., TxField, FullReplicationSerializer, TxField, int
				// =>
				super.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
						FullReplicationSerializer.NAME,
						FullReplicationSerializer.BOOTSTRAP_METHOD_NAME,
						FullReplicationSerializer.BOOTSTRAP_METHOD_DESC);
				// stack: ..., TxField =>
			}
		}

		String className = System.getProperty(Defaults._CTX_CLASS,
				Defaults.CTX_CLASS);
		if (className.equals(org.deuce.transaction.mvstm.Context.class
				.getName()))
		{ // XXX pedreiro style
			// ##### setType (VBoxField)
			// stack: ..., TxField =>
			super.visitInsn(Opcodes.DUP);
			// stack: ..., TxField, TxField =>
			super.visitTypeInsn(Opcodes.CHECKCAST,
					org.deuce.transaction.mvstm.field.VBoxField.NAME);
			// stack: ..., TxField, TxField, VBoxField =>
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
			case Type.ARRAY:
				super.visitLdcInsn(Type.ARRAY);
				break;
			}
			// stack: ..., TxField, VBoxField, Type =>
			mv.visitMethodInsn(
					Opcodes.INVOKEVIRTUAL,
					org.deuce.transaction.mvstm.field.VBoxField.NAME,
					org.deuce.transaction.mvstm.field.VBoxField.SET_TYPE_METHOD_NAME,
					org.deuce.transaction.mvstm.field.VBoxField.SET_TYPE_METHOD_DESC);
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
		// TODOs can we do it cleaner?
		if (staticMethod != null && super.mv != staticMethod)
			staticMethod.visitEnd();
	}
}
