package org.deuce.transform.localmetadata;

import org.deuce.distribution.TribuDSTM;
import org.deuce.objectweb.asm.MethodAdapter;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ObjectArrayContainer;

@ExcludeTM
public class MainMethodTransformer extends MethodAdapter {
	final static public String CLASS_BASE = "__CLASS_BASE__";

	public MainMethodTransformer(MethodVisitor mv) {
		super(mv);
	}

	@Override
	public void visitCode() {
		((MethodTransformer) mv).disableMethodInstrumentation(true);
		((MethodTransformer) mv).disableDuplicateInstrumentation(true);

		// ... =>
		mv.visitVarInsn(Opcodes.ALOAD, 0);
		// ..., arr =>
		mv.visitTypeInsn(Opcodes.NEW, ObjectArrayContainer.NAME);
		// ..., arr, obj =>
		mv.visitInsn(Opcodes.DUP_X1);
		// ..., obj, arr, obj =>
		mv.visitInsn(Opcodes.SWAP);
		// ..., obj, obj, arr =>
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, ObjectArrayContainer.NAME,
				"<init>", ObjectArrayContainer.CTOR_DESC);
		// ..., cnt =>
		mv.visitVarInsn(Opcodes.ASTORE, 0);
		// ... =>
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, TribuDSTM.NAME,
				TribuDSTM.INIT_METHOD_NAME, TribuDSTM.INIT_METHOD_DESC);
		// ... =>

		((MethodTransformer) mv).disableMethodInstrumentation(false);
		((MethodTransformer) mv).disableDuplicateInstrumentation(false);
	}

	@Override
	public void visitEnd() {
		((MethodTransformer) mv).disableMethodInstrumentation(true);
		((MethodTransformer) mv).disableDuplicateInstrumentation(true);
		
		mv.visitMethodInsn(Opcodes.INVOKESTATIC, TribuDSTM.NAME,
				TribuDSTM.CLOSE_METHOD_NAME, TribuDSTM.CLOSE_METHOD_DESC);
		
		((MethodTransformer) mv).disableMethodInstrumentation(false);
		((MethodTransformer) mv).disableDuplicateInstrumentation(false);
		
		super.visitEnd();
	}
}
