package org.deuce.transform.localmetadata;

import java.util.List;
import java.util.Map;

import org.deuce.distribution.TribuDSTM;
import org.deuce.distribution.replication.full.FullReplicationSerializer;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.objectweb.asm.commons.AnalyzerAdapter;
import org.deuce.objectweb.asm.commons.Method;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

/*
 * TODO Class fields should not be instrumented inside the constructor We need
 *      to implement this optimization!
 *      I believe that implementing this optimization will fix the following 
 *      issue.
 *      
 *      Consider the following class, in bytecode:
 *        public class asmconstructortest.Test2 extends java.lang.Object {
 *        
 *        public int property;
 *        
 *        public asmconstructortest.Test2();
 *          Code:
 *          0:   aload_0
 *          1:   iconst_1
 *          2:   putfield        #10; //Field property:I
 *          5:   aload_0
 *          6:   invokespecial   #12; //Method java/lang/Object."<init>":()V
 *          9:   return
 *        }
 *      While the putfield instruction before the call to the super's <init>
 *      is impossible to produce programming in Java, it is possible in 
 *      bytecode. And it executes.
 *      
 *      But it wouldn't run if instead of a putfield it was a getfield.
 *      The following error occurs (line 5 of Main2.java is "new Test2()"):
 *        
 *        Exception in thread "main" java.lang.VerifyError: 
 *          (class: asmconstructortest/Test2, method: <init> signature: ()V) 
 *          Expecting to find object/array on stack at 
 *          asmconstructortest.Main2.main(Main2.java:5)
 *          
 *      This is probably because self is not initialized yet.
 *      
 *      The transaction-aware duplication of Test2(), Test2(IContext), will 
 *      inject before the aforementioned putfield a getfield to the 
 *      corresponding metadata. That will cause a similar exception to the 
 *      above to be thrown.
 */
/*
 * TODO: Try to move @Bootstrap related transformations to other class
 */
@ExcludeTM
public class ConstructorMethodTransformer extends AnalyzerAdapter {
	final static public String CLASS_BASE = "__CLASS_BASE__";

	protected final List<Field> fields;
	protected final String fieldsHolderName;
	protected boolean callsOtherCtor;
	protected final String className;
	// protected boolean isUniqueObject;

	// FIXME @Bootstrap
	protected final Map<String, Integer> field2OID;

	public ConstructorMethodTransformer(MethodVisitor mv, List<Field> fields,
			Map<String, Integer> field2OID, String className, int access,
			String name, String desc, String fieldsHolderName/*
															 * , boolean
															 * isUniqueObject
															 */) {
		super(className, access, name, desc, mv);
		this.fields = fields;
		this.fieldsHolderName = fieldsHolderName;
		this.callsOtherCtor = false;
		this.className = className;
		this.field2OID = field2OID;
		// this.isUniqueObject = isUniqueObject;
	}

	protected void initMetadataField(Field field) {
		// stack: ... =>
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		// stack: ..., Object (this) =>
		mv.visitTypeInsn(Opcodes.NEW, field.getType().getInternalName());
		// stack: ..., Object (this), TxField =>
		mv.visitInsn(Opcodes.DUP);
		// stack: ..., Object (this), TxField, TxField =>
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		// stack: ..., Object (this), TxField, TxField,
		// Object (this) =>
		mv.visitFieldInsn(Opcodes.GETSTATIC, fieldsHolderName, "__STATIC__"
				+ field.getFieldNameAddress(), Type.LONG_TYPE.getDescriptor());
		// stack: ..., Object (this), TxField, TxField,
		// Object (this), long (this field's address) =>
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, field.getType()
				.getInternalName(), "<init>", TxField.CTOR_DESC);
		// stack: ..., Object (this), TxField =>

		// XXX @Bootstrap, assumes FullReplicationSerializer
		Integer oid = field2OID.get(field.getFieldName());
		if (oid != null) {
			mv.visitInsn(Opcodes.DUP);
			// stack: ..., Object (this), TxField, TxField =>
			mv.visitMethodInsn(Opcodes.INVOKESTATIC, TribuDSTM.NAME,
					TribuDSTM.GETSERIALIZER_METHOD_NAME,
					TribuDSTM.GETSERIALIZER_METHOD_DESC);
			// stack: ..., Object (this), TxField, TxField, ObjectSerializer =>
			mv.visitTypeInsn(Opcodes.CHECKCAST, FullReplicationSerializer.NAME);
			// stack: ..., Object (this), TxField, TxField,
			// FullReplicationSerializer =>
			mv.visitInsn(Opcodes.SWAP);
			// stack: ..., Object (this), TxField, FullReplicationSerializer,
			// TxField =>
			mv.visitLdcInsn(oid);
			// stack: ..., Object (this), TxField, FullReplicationSerializer,
			// TxField, int =>
			mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL,
					FullReplicationSerializer.NAME,
					FullReplicationSerializer.BOOTSTRAP_METHOD_NAME,
					FullReplicationSerializer.BOOTSTRAP_METHOD_DESC);
			// stack: ..., Object (this), TxField =>
		}

		// stack: ..., Object (this), TxField =>
	}

	protected void addField(Field field) {
		// stack: ... =>
		initMetadataField(field);
		// stack: ..., Object (this), TxField =>
		mv.visitFieldInsn(Opcodes.PUTFIELD, fieldsHolderName,
				field.getFieldNameAddress(), field.getType().getDescriptor());
		// stack: ... =>
	}

	@Override
	public void visitInsn(int opcode) {
		if (opcode == Opcodes.RETURN && !callsOtherCtor) {
			((MethodTransformer) mv).disableDuplicateInstrumentation(true);
			((MethodTransformer) mv).disableMethodInstrumentation(true);
			if (fields.size() > 0) {
				for (Field field : fields) {
					if ((field.getAccess() & Opcodes.ACC_STATIC) == 0) {
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
	public void visitEnd() {
		// ((MethodTransformer)mv).disableDuplicateInstrumentation(false);
		super.visitEnd();
	}

	@Override
	public void visitMethodInsn(final int opcode, final String owner,
			final String name, final String desc) {
		if (opcode == Opcodes.INVOKESPECIAL && name.equals("<init>")) {
			/*
			 * if (owner.equals(Type.getInternalName(Object.class)) &&
			 * isUniqueObject) { super.visitMethodInsn(opcode,
			 * UniqueObject.NAME, name, desc); return; } else
			 */if (owner.equals(className)) {
				// The method is created merely to know the #params
				int nParams = new Method(name, desc).getArgumentTypes().length;

				// With that number, we go down the stack #params and check
				// what's
				// there
				Object stackObj = stack.get(stack.size() - nParams - 1);

				// Is it <this>, uninitialized?
				if (stackObj instanceof Integer
						&& ((Integer) stackObj) == Opcodes.UNINITIALIZED_THIS) {
					callsOtherCtor = true;
				}
			}
		}

		super.visitMethodInsn(opcode, owner, name, desc);
	}
}
