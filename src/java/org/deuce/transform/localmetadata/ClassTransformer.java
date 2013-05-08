package org.deuce.transform.localmetadata;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

import org.deuce.distribution.ObjectMetadata;
import org.deuce.distribution.ObjectSerializer;
import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.UniqueObject;
import org.deuce.objectweb.asm.AnnotationVisitor;
import org.deuce.objectweb.asm.FieldVisitor;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.objectweb.asm.commons.Method;
import org.deuce.transaction.Context;
import org.deuce.transform.Exclude;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.asm.ByteCodeVisitor;
import org.deuce.transform.asm.FieldsHolder;
import org.deuce.transform.asm.type.TypeCodeResolver;
import org.deuce.transform.asm.type.TypeCodeResolverFactory;
import org.deuce.transform.localmetadata.replication.SpecificAnnotationsFieldVisitor;
import org.deuce.transform.util.Util;

@ExcludeTM
public class ClassTransformer extends ByteCodeVisitor implements FieldsHolder
{
	final protected static String ENUM_DESC = Type.getInternalName(Enum.class);

	protected boolean excludeApp = false;
	protected boolean excludeSys = false;
	protected boolean visitclinit = false;

	final protected LinkedList<Field> fields = new LinkedList<Field>();

	protected String staticField = null;

	final static public String EXCLUDE_DESC = Type
			.getDescriptor(ExcludeTM.class);
	final static public String EXCLUDE_AP_DESC = Type
			.getDescriptor(Exclude.class);
	final static protected String ANNOTATION_NAME = Type
			.getInternalName(Annotation.class);

	protected boolean isInterface;
	protected boolean isEnum;
	protected MethodVisitor staticMethod;

	protected final FieldsHolder fieldsHolder;

	public class NonInsnMethod
	{
		public String name;
		public String desc;
		public String origDesc;
		public int opcode;
		public String insnName;
		public String insnDesc;
		public String owner;
		public String insnOwner;

		public NonInsnMethod(String name, String desc, String originalDesc,
				String owner, int opcode)
		{
			this.name = name;
			this.desc = desc;
			this.origDesc = originalDesc;
			this.opcode = opcode;

			Method m = createNewMethod2(owner, name, desc, opcode);
			m = ArrayUtil.updateMethodArrayArgumentsAndReturn(m);

			insnName = owner.replace('/', '_') + "_"
					+ m.getName().replace('<', '_').replace('>', '_');
			insnDesc = m.getDescriptor();

			this.owner = owner;
			this.insnOwner = ClassTransformer.this.className;
		}

		@Override
		public int hashCode()
		{
			final int prime = 31;
			int result = 1;
			result = prime * result + getOuterType().hashCode();
			result = prime * result
					+ ((insnDesc == null) ? 0 : insnDesc.hashCode());
			result = prime * result
					+ ((origDesc == null) ? 0 : origDesc.hashCode());
			result = prime * result
					+ ((insnName == null) ? 0 : insnName.hashCode());
			result = prime * result
					+ ((insnOwner == null) ? 0 : insnOwner.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj)
		{
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			NonInsnMethod other = (NonInsnMethod) obj;
			if (!getOuterType().equals(other.getOuterType()))
				return false;
			if (insnDesc == null)
			{
				if (other.insnDesc != null)
					return false;
			}
			else if (!insnDesc.equals(other.insnDesc))
				return false;
			if (origDesc == null)
			{
				if (other.origDesc != null)
					return false;
			}
			else if (!origDesc.equals(other.origDesc))
				return false;
			if (insnName == null)
			{
				if (other.insnName != null)
					return false;
			}
			else if (!insnName.equals(other.insnName))
				return false;
			if (insnOwner == null)
			{
				if (other.insnOwner != null)
					return false;
			}
			else if (!insnOwner.equals(other.insnOwner))
				return false;
			return true;
		}

		private ClassTransformer getOuterType()
		{
			return ClassTransformer.this;
		}
	}

	public final LinkedList<NonInsnMethod> nonInsnMethods = new LinkedList<NonInsnMethod>();

	protected boolean isAbstract;

	// ################################# @Bootstrap @Partial
	public final Map<String, Integer> field2OID = new java.util.HashMap<String, Integer>();
	public final Set<String> partialRepFields = new java.util.HashSet<String>(
			50);
	public final boolean partial = Boolean.parseBoolean(System.getProperty(
			"tribu.distributed.PartialReplicationMode",
			TribuDSTM.partialDefault));

	public ClassTransformer(String className, FieldsHolder fieldsHolder)
	{
		super(className);
		this.fieldsHolder = fieldsHolder == null ? this : fieldsHolder;
	}

	@Override
	public void visit(final int version, final int access, final String name,
			final String signature, final String superName,
			final String[] interfaces)
	{
		fieldsHolder.visit(superName);
		isInterface = (access & Opcodes.ACC_INTERFACE) != 0;
		isEnum = ENUM_DESC.equals(superName);

		for (String inter : interfaces)
		{
			if (inter.equals(ANNOTATION_NAME))
			{
				excludeApp = true;
				excludeSys = true;
				break;
			}
		}

		// FIXMEs Para fazer o que quero, tem que ser com a tree API. Ou com
		// duas
		// passagens?
		// If it is an application class directly extending Object, then we make
		// it extend UniqueObject
		// if (!excludeApp && !excludeSys && !isInterface
		// && superName.equals(Type.getInternalName(Object.class))) {
		// super.visit(version, access, name, signature, UniqueObject.NAME,
		// interfaces);
		// isUniqueObject = true;
		// } else {
		// super.visit(version, access, name, signature, superName, interfaces);
		// isUniqueObject = false;
		// }

		// TODOs FIXMEs This should be done in a clever way. For example, only
		// the
		// roots from the application's class hierarchies should be
		// instrumented. Will the serialisation technique work?
		isAbstract = (access & Opcodes.ACC_ABSTRACT) != 0;
		// If it is an allowed application class, make it implement
		// UniqueObject.
		if (!excludeApp && !excludeSys && !isInterface && !isAbstract)
		{
			final String[] interfacesWithUniqueObject = Arrays.copyOf(
					interfaces, interfaces.length + 1);
			interfacesWithUniqueObject[interfaces.length] = UniqueObject.NAME;
			super.visit(version, access, name, signature, superName,
					interfacesWithUniqueObject);
		}
		else
		{
			super.visit(version, access, name, signature, superName, interfaces);
		}
	}

	/**
	 * Checks if the class is marked as {@link ExcludeTM Exclude}
	 */
	@Override
	public AnnotationVisitor visitAnnotation(String desc, boolean visible)
	{
		excludeApp = excludeApp ? excludeApp : EXCLUDE_AP_DESC.equals(desc);
		excludeSys = excludeSys ? excludeSys : EXCLUDE_DESC.equals(desc);
		return super.visitAnnotation(desc, visible);
	}

	/**
	 * Creates a new static filed for each existing field. The field will be
	 * statically initialized to hold the field address.
	 */
	@Override
	public FieldVisitor visitField(int access, String name, String desc,
			String signature, Object value)
	{
		String origDesc = desc;

		if (Type.getType(desc).getSort() == Type.ARRAY && !excludeSys)
		{
			desc = ArrayUtil.getArrayContainerType(desc).getDescriptor();
		}

		FieldVisitor fv = super
				.visitField(access, name, desc, signature, value);

		if (excludeApp || excludeSys)
			return fv;

		// Define as constant
		int fieldAccess = 0;
		String addressFieldName = Util.getAddressField(name);

		fieldAccess = Opcodes.ACC_FINAL | Opcodes.ACC_PUBLIC
				| Opcodes.ACC_SYNTHETIC;

		final boolean include = (access & Opcodes.ACC_FINAL) == 0;
		final boolean isStatic = (access & Opcodes.ACC_STATIC) != 0;

		if (isStatic)
		{
			fieldAccess |= Opcodes.ACC_STATIC;
		}

		if (include)
		{ // include field if not final
			Field field = new Field(name, addressFieldName, access,
					ArrayUtil.getTxType(desc), Type.getType(origDesc));
			fields.add(field);
			if (isStatic)
			{
				staticField = name;
			}
			fieldsHolder.addField(fieldAccess, addressFieldName, ArrayUtil
					.getTxType(desc).getDescriptor(), null);
			fieldsHolder.addField(fieldAccess | Opcodes.ACC_STATIC,
					"__STATIC__" + addressFieldName,
					Type.LONG_TYPE.getDescriptor(), -1L);
		}
		else
		{
			fieldsHolder.addField(fieldAccess, addressFieldName, ArrayUtil
					.getTxType(desc).getDescriptor(), null);
			fieldsHolder.addField(fieldAccess | Opcodes.ACC_STATIC,
					"__STATIC__" + addressFieldName,
					Type.LONG_TYPE.getDescriptor(), -1L);
		}

		// ################################# @Bootstrap @Partial
		FieldVisitor bootstrapFv = new SpecificAnnotationsFieldVisitor(fv,
				field2OID, partialRepFields, name);

		return bootstrapFv;
	}

	@Override
	public MethodVisitor visitMethod(int access, String name, String desc,
			String signature, String[] exceptions)
	{
		if (name.matches("000.*"))
		{
			return new NonInstrumentedMethodTransformer(super.visitMethod(
					access, name.substring(3), desc, signature, exceptions),
					this);
		}

		Method nm = new Method(name, desc);
		if (!(name.equals("main") && desc.equals("([Ljava/lang/String;)V"))
				&& !excludeSys)
		{
			nm = ArrayUtil.updateMethodArrayArgumentsAndReturn(nm);
		}
		MethodVisitor originalMethod = super.visitMethod(access, name,
				nm.getDescriptor(), signature, exceptions);

		if (excludeSys)
		{
			return originalMethod;
		}

		final boolean isNative = (access & Opcodes.ACC_NATIVE) != 0;
		if (isNative)
		{
			createNativeMethod(access, name, desc, signature, exceptions);
			return originalMethod;
		}

		if (name.equals("<clinit>") && !excludeApp)
		{
			staticMethod = originalMethod;
			visitclinit = true;

			if (isInterface)
			{
				return originalMethod;
			}

			int fieldAccess = Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC
					| Opcodes.ACC_SYNTHETIC | Opcodes.ACC_FINAL;

			fieldsHolder.addField(fieldAccess,
					StaticMethodTransformer.CLASS_BASE,
					Type.getDescriptor(Object.class), null);

			MethodVisitor mv = new MethodTransformer(originalMethod, null,
					className, access, name, nm.getDescriptor(), desc, null,
					fieldsHolder);

			return new StaticMethodTransformer(mv, null, fields, field2OID,
					partialRepFields, partial, staticField, className,
					fieldsHolder.getFieldsHolderName(className));
		}

		if (name.equals("<init>"))
		{
			Method newMethod = createNewMethod(name, nm.getDescriptor());
			// Create a new duplicate SYNTHETIC method and remove the final
			// marker if has one.
			MethodVisitor copyMethod = null;
			if (!excludeApp)
			{
				copyMethod = super.visitMethod((access | Opcodes.ACC_SYNTHETIC)
						& ~Opcodes.ACC_FINAL, name, newMethod.getDescriptor(),
						signature, exceptions);
			}

			MethodVisitor mv = new MethodTransformer(originalMethod,
					copyMethod, className, access, name, nm.getDescriptor(),
					desc, newMethod, fieldsHolder);

			return new ConstructorMethodTransformer(mv, fields, field2OID,
					partialRepFields, partial, className, access, name,
					nm.getDescriptor(),
					fieldsHolder.getFieldsHolderName(className));
		}

		if (name.equals("main")
				&& nm.getDescriptor().equals("([Ljava/lang/String;)V"))
		{
			Method newMethod = createNewMethod(name, nm.getDescriptor());
			// Create a new duplicate SYNTHETIC method and remove the final
			// marker if has one.
			MethodVisitor copyMethod = null;
			if (!excludeApp)
			{
				copyMethod = super.visitMethod((access | Opcodes.ACC_SYNTHETIC)
						& ~Opcodes.ACC_FINAL, name, newMethod.getDescriptor(),
						signature, exceptions);
			}
			MethodVisitor mv = new MethodTransformer(originalMethod,
					copyMethod, className, access, name, nm.getDescriptor(),
					desc, newMethod, fieldsHolder);

			return new MainMethodTransformer(mv);
		}

		Method newMethod = createNewMethod(name, nm.getDescriptor());

		// Create a new duplicate SYNTHETIC method and remove the final marker
		// if has one.
		MethodVisitor copyMethod = null;
		if (!excludeApp)
		{
			copyMethod = super.visitMethod((access | Opcodes.ACC_SYNTHETIC)
					& ~Opcodes.ACC_FINAL, name, newMethod.getDescriptor(),
					signature, exceptions);
		}

		return new MethodTransformer(originalMethod, copyMethod, className,
				access, name, nm.getDescriptor(), desc, newMethod, fieldsHolder);
	}

	/**
	 * Build a dummy method that delegates the call to the native method
	 */
	protected void createNativeMethod(int access, String name, String desc,
			String signature, String[] exceptions)
	{
		Method newMethod = createNewMethod(name, desc);
		final int newAccess = access & ~Opcodes.ACC_NATIVE;

		MethodVisitor copyMethod = super.visitMethod(newAccess
				| Opcodes.ACC_SYNTHETIC, name, newMethod.getDescriptor(),
				signature, exceptions);

		copyMethod.visitCode();

		// load the arguments before calling the original method
		final boolean isStatic = (access & ~Opcodes.ACC_STATIC) != 0;
		int place = 0; // place on the stack
		if (!isStatic)
		{
			copyMethod.visitVarInsn(Opcodes.ALOAD, 0); // load this
			place = 1;
		}

		Type[] argumentTypes = newMethod.getArgumentTypes();
		for (int i = 0; i < (argumentTypes.length - 1); ++i)
		{
			Type type = argumentTypes[i];
			copyMethod.visitVarInsn(type.getOpcode(Opcodes.ILOAD), place);
			place += type.getSize();
		}

		// call the original method
		copyMethod.visitMethodInsn(isStatic ? Opcodes.INVOKESTATIC
				: Opcodes.INVOKEVIRTUAL, className, name, desc);

		TypeCodeResolver returnReolver = TypeCodeResolverFactory
				.getReolver(newMethod.getReturnType());

		if (returnReolver == null)
		{
			copyMethod.visitInsn(Opcodes.RETURN); // return;
		}
		else
		{
			copyMethod.visitInsn(returnReolver.returnCode());
		}
		copyMethod.visitMaxs(1, 1);
		copyMethod.visitEnd();
	}

	@Override
	public void visitEnd()
	{
		// Didn't see any static method till now, so creates one.
		if (!excludeApp && !excludeSys)
		{
			super.visitAnnotation(EXCLUDE_DESC, false);
			if (!visitclinit && fields.size() > 0)
			{
				// creates a new <clinit> in case we didn't see one already.
				// TODOs avoid creating new static method in case of external
				// fields
				// holder
				visitclinit = true;
				MethodVisitor method = visitMethod(Opcodes.ACC_STATIC,
						"<clinit>", "()V", null, null);
				method.visitCode();
				method.visitInsn(Opcodes.RETURN);
				method.visitMaxs(100, 100); // TODOs set the right value
				method.visitEnd();
			}

			if (isEnum)
			{
				// Build a dummy ordinal() method
				MethodVisitor ordinalMethod = super.visitMethod(
						Opcodes.ACC_PUBLIC | Opcodes.ACC_SYNTHETIC, "ordinal",
						"(Lorg/deuce/transaction/IContext;)I", null, null);
				ordinalMethod.visitCode();
				ordinalMethod.visitVarInsn(Opcodes.ALOAD, 0);
				ordinalMethod.visitMethodInsn(Opcodes.INVOKEVIRTUAL, className,
						"ordinal", "()I");
				ordinalMethod.visitInsn(Opcodes.IRETURN);
				ordinalMethod.visitMaxs(1, 2);
				ordinalMethod.visitEnd();
			}

			if (!isInterface && !isAbstract)
			{
				/*
				 * FIXMEs: Refactor. Add the synthetic metadata field to the
				 * user's class, plus the getters/setters and the
				 * writeReplace/readResolve methods. Basically, implement the
				 * UniqueObject interface.
				 */
				// #metadata field.
				fieldsHolder.addField(Opcodes.ACC_PUBLIC | Opcodes.ACC_FINAL
						| Opcodes.ACC_SYNTHETIC,
						UniqueObject.METADATA_FIELD_NAME, ObjectMetadata.DESC,
						null);
				// getMetadata() method.
				MethodVisitor mv = super.visitMethod(Opcodes.ACC_PUBLIC,
						UniqueObject.GETMETADATA_METHOD_NAME,
						UniqueObject.GETMETADATA_METHOD_DESC, null, null);
				mv.visitCode();
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitFieldInsn(Opcodes.GETFIELD, className,
						UniqueObject.METADATA_FIELD_NAME, ObjectMetadata.DESC);
				mv.visitInsn(Opcodes.ARETURN);
				mv.visitMaxs(1, 1);
				mv.visitEnd();
				// setMetadata(ObjectMetadata) method.
				mv = super.visitMethod(Opcodes.ACC_PUBLIC,
						UniqueObject.SETMETADATA_METHOD_NAME,
						UniqueObject.SETMETADATA_METHOD_DESC, null, null);
				mv.visitCode();
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitVarInsn(Opcodes.ALOAD, 1);
				mv.visitFieldInsn(Opcodes.PUTFIELD, className,
						UniqueObject.METADATA_FIELD_NAME, ObjectMetadata.DESC);
				mv.visitInsn(Opcodes.RETURN);
				mv.visitMaxs(2, 2);
				mv.visitEnd();
				// writeReplace() method.
				mv = super.visitMethod(Opcodes.ACC_PUBLIC, "writeReplace",
						"()Ljava/lang/Object;", null,
						new String[] { "java/io/ObjectStreamException" });
				mv.visitCode();
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, TribuDSTM.NAME,
						TribuDSTM.GETSERIALIZER_METHOD_NAME,
						TribuDSTM.GETSERIALIZER_METHOD_DESC);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
						ObjectSerializer.NAME,
						ObjectSerializer.WRITE_METHOD_NAME,
						ObjectSerializer.WRITE_METHOD_DESC);
				mv.visitInsn(Opcodes.ARETURN);
				mv.visitMaxs(2, 1);
				mv.visitEnd();
				// readResolve() method.
				mv = super.visitMethod(Opcodes.ACC_PUBLIC, "readResolve",
						"()Ljava/lang/Object;", null,
						new String[] { "java/io/ObjectStreamException" });
				mv.visitCode();
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, TribuDSTM.NAME,
						TribuDSTM.GETSERIALIZER_METHOD_NAME,
						TribuDSTM.GETSERIALIZER_METHOD_DESC);
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
						ObjectSerializer.NAME,
						ObjectSerializer.READ_METHOD_NAME,
						ObjectSerializer.READ_METHOD_DESC);
				mv.visitInsn(Opcodes.ARETURN);
				mv.visitMaxs(2, 1);
				mv.visitEnd();
			}
		}

		if (!excludeSys)
		{
			for (NonInsnMethod mim : nonInsnMethods)
			{
				MethodVisitor mv = visitMethod(Opcodes.ACC_STATIC
						| Opcodes.ACC_FINAL | Opcodes.ACC_PRIVATE
						| Opcodes.ACC_SYNTHETIC, "000" + mim.insnName,
						mim.insnDesc, null, null);
				((NonInstrumentedMethodTransformer) mv).setNonInsnMethod(mim);
				mv.visitCode();
				mv.visitEnd();
			}
		}

		super.visitEnd();
		fieldsHolder.close();
	}

	public static Method createNewMethod(String name, String desc)
	{
		Method method = new Method(name, desc);
		Type[] arguments = method.getArgumentTypes();

		Type[] newArguments = new Type[arguments.length + 1];
		System.arraycopy(arguments, 0, newArguments, 0, arguments.length);
		// add as a constant
		newArguments[newArguments.length - 1] = Context.CONTEXT_TYPE;

		return new Method(name, method.getReturnType(), newArguments);
	}

	public static Method createNewMethod2(String owner, String name,
			String desc, int opcode)
	{
		Method method = new Method(name, desc);
		Type[] arguments = method.getArgumentTypes();

		Type[] newArguments = null;
		if (opcode != Opcodes.INVOKESTATIC && opcode != Opcodes.INVOKESPECIAL)
		{
			newArguments = new Type[arguments.length + 2];
			System.arraycopy(arguments, 0, newArguments, 1, arguments.length);
			newArguments[0] = Type.getType("L" + owner + ";");
			// add as a constant
			newArguments[newArguments.length - 1] = Context.CONTEXT_TYPE;
		}
		else
		{
			newArguments = new Type[arguments.length + 1];
			System.arraycopy(arguments, 0, newArguments, 0, arguments.length);
			// add as a constant
			newArguments[newArguments.length - 1] = Context.CONTEXT_TYPE;
		}

		Type returnType = method.getReturnType();
		if (opcode == Opcodes.INVOKESPECIAL)
		{
			returnType = Type.getType("L" + owner + ";");
		}

		return new Method(name, returnType, newArguments);
	}

	public void addField(int fieldAccess, String addressFieldName, String desc,
			Object value)
	{
		super.visitField(fieldAccess, addressFieldName, desc, null, value);
	}

	public void close()
	{
	}

	public MethodVisitor getStaticMethodVisitor()
	{
		return staticMethod;
	}

	public String getFieldsHolderName(String owner)
	{
		return owner;
	}

	public void visit(String superName)
	{
		// nothing to do
	}
}
