package org.deuce.transform.localmetadata;

import org.deuce.objectweb.asm.Label;
import org.deuce.objectweb.asm.MethodAdapter;
import org.deuce.objectweb.asm.MethodVisitor;
import org.deuce.objectweb.asm.Opcodes;
import org.deuce.objectweb.asm.Type;
import org.deuce.objectweb.asm.commons.Method;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.ClassTransformer.NonInsnMethod;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.array.MultiArrayContainer;

@ExcludeTM
public class NonInstrumentedMethodTransformer
extends      MethodAdapter
{
  private NonInsnMethod    insnM;
  private ClassTransformer clazzT;
	
	public NonInstrumentedMethodTransformer(
	    MethodVisitor    mv,
	    ClassTransformer classTransformer)
	{
		super(mv);
		clazzT = classTransformer;
	}

	public void setNonInsnMethod(NonInsnMethod m) {
		this.insnM = m;
	}
	
	public void visitCode() {
		Method mm = new Method(insnM.insnName, insnM.insnDesc);
		Method m = new Method(insnM.name, insnM.desc);
		
		if (insnM.opcode == Opcodes.INVOKESPECIAL) {
			super.visitTypeInsn(Opcodes.NEW, insnM.owner);
			super.visitInsn(Opcodes.DUP);
		}
		
		
		if (insnM.opcode != Opcodes.INVOKESTATIC &&
		    insnM.opcode != Opcodes.INVOKESPECIAL)
		{
			super.visitVarInsn(Opcodes.ALOAD, 0);
		}
		
		int i = (insnM.opcode != Opcodes.INVOKESTATIC &&
		         insnM.opcode != Opcodes.INVOKESPECIAL ?
		               1 
		             : 0);
		
		int varbase = mm.getArgumentTypes().length;
		
		for (Type t : m.getArgumentTypes()) {
			switch(t.getSort()) {
			case Type.INT:
			case Type.SHORT:
			case Type.BYTE:
			case Type.BOOLEAN:
			case Type.CHAR:
				super.visitVarInsn(Opcodes.ILOAD, i);
				break;
			case Type.LONG:
				super.visitVarInsn(Opcodes.LLOAD, i);
				break;
			case Type.DOUBLE:
				super.visitVarInsn(Opcodes.DLOAD, i);
				break;
			case Type.ARRAY:
				super.visitVarInsn(Opcodes.ALOAD, i);
//				txArrayToArray(ArrayUtil.getArrayType(t), mv);
				containerToArray(t, mv);
				super.visitInsn(Opcodes.DUP);
				super.visitVarInsn(Opcodes.ASTORE, varbase++);
				break;
			default:
				super.visitVarInsn(Opcodes.ALOAD, i);
			}
			i++;
		}
		
		super.visitMethodInsn(
		    insnM.opcode,
		    insnM.owner,
		    insnM.name,
		    insnM.origDesc);
		
		Type returnType = m.getReturnType();
		if (returnType.getSort() == Type.ARRAY) {
			if (returnType.getDimensions() > 1)
				// multiArrayToTxArray(returnType, mv);
				multiArrayToContainer(returnType, mv);
			else
				// arrayToTxArray(ArrayUtil.getArrayType(m.getReturnType()),
				// mv);
				arrayToContainer(returnType, mv);
		}
		
		switch(mm.getReturnType().getSort()) {
		case Type.INT:
		case Type.SHORT:
		case Type.BYTE:
		case Type.BOOLEAN:
		case Type.CHAR:
			super.visitInsn(Opcodes.IRETURN);
			break;
		case Type.FLOAT:
			super.visitInsn(Opcodes.FRETURN);
			break;
		case Type.LONG:
			super.visitInsn(Opcodes.LRETURN);
			break;
		case Type.DOUBLE:
			super.visitInsn(Opcodes.DRETURN);
			break;
		case Type.ARRAY:
		case Type.OBJECT:
			super.visitInsn(Opcodes.ARETURN);
			break;
		case Type.VOID:
			super.visitInsn(Opcodes.RETURN);
			break;
		}
	}
	
	public int getArrayArgsCount() {
		Method m = new Method(insnM.name, insnM.desc);
		int c = 0;
		for (Type t : m.getArgumentTypes()) {
			if (t.getSort() == Type.ARRAY) {
				c++;
			}
		}
		return c;
	}
	
	public void visitMaxs(int maxStack, int maxLocals) {
		super.visitMaxs(maxStack, maxLocals + getArrayArgsCount());
	}
	

	private void containerToArray(Type originalType, MethodVisitor mv) {
		// ..., cnt =>
		mv.visitMethodInsn(Opcodes.INVOKEVIRTUAL, ArrayContainer.NAME,
				ArrayContainer.GETARRAY_METHOD_NAME,
				ArrayContainer.GETARRAY_METHOD_DESC);
		// ..., obj =>
		mv.visitTypeInsn(Opcodes.CHECKCAST, originalType.getInternalName());
		// ..., arr =>
	}
  /**
   * This method must be called only when the TxTypeField array reference is on
   * top of the stack. All we need to do is get the array pointed by the 
   * array field of one of the metadata array elements.
   * 
   *  // ...,txarrayref =>
   *  ...
   *  // ...,arrayref =>
   *  
   *  @param arrT the original type of the array
   */
  private void txArrayToArray(Type arrT, MethodVisitor mv) {
    // ..., arr* =>
    mv.visitInsn(Opcodes.ICONST_0);
    // ..., arr*, 0 =>
    mv.visitInsn(Opcodes.AALOAD);
    // ..., obj* =>
    if (arrT.getSort() == Type.ARRAY) {
      // ..., obj* =>
      mv.visitFieldInsn(
          Opcodes.GETFIELD,
          ArrayUtil.getTxArrayType(arrT).getInternalName(),
          "array",
          Type.getDescriptor(Object.class));
      // ..., obj =>
      mv.visitTypeInsn(Opcodes.CHECKCAST, arrT.getInternalName());
      // ..., arr =>
    } else {
      // ..., obj* =>
      mv.visitFieldInsn(
          Opcodes.GETFIELD,
          ArrayUtil.getTxArrayType(arrT).getInternalName(),
          "array",
          "[" + arrT.getDescriptor());
      // ..., arr =>
    }
    // ..., arr =>
  }
	
	private void arrayToContainer(Type originalType, MethodVisitor mv) {
		Type arrCntType = ArrayUtil.getArrayContainerType(originalType);
		String arrCntName = arrCntType.getInternalName();
		String arrCntCtorDesc = ArrayUtil.getArrayContainerCtorDesc(ArrayUtil
				.getArrayType(originalType));

		// ..., arr =>
		mv.visitTypeInsn(Opcodes.NEW, arrCntName);
		// ..., arr, cnt =>
		mv.visitInsn(Opcodes.DUP_X1);
		// ..., cnt, arr, cnt =>
		mv.visitInsn(Opcodes.SWAP);
		// ..., cnt, cnt, arr =>
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, arrCntName, "<init>",
				arrCntCtorDesc);
		// ..., cnt =>
	}
  /**
   * This method must be called only when the array reference is on top of the
   * stack.
   * 
   *  // ...,arrayref =>
   *  ...
   *  // ...,txarrayref =>
   *  
   *  @param arrT the original type of the array
   */
  private void arrayToTxArray(Type arrT, MethodVisitor mv) {
    Label l1 = new Label();
    Label l2 = new Label();
    Label l3 = new Label();
    
    Type arrayType = ArrayUtil.getTxArrayType(arrT);
    String ctorParamDesc = null;
    if (arrT.getSort() == Type.OBJECT)
      ctorParamDesc = "([" + Type.getDescriptor(Object.class) +"I)V";
    else
      ctorParamDesc = "([" + arrT.getDescriptor() + "I)V";

    // ..., arr =>
    mv.visitInsn(Opcodes.DUP);
    // ..., arr, arr =>
    mv.visitInsn(Opcodes.ARRAYLENGTH);
    // ..., arr, len =>
    mv.visitTypeInsn(
        Opcodes.ANEWARRAY,
        arrayType.getInternalName());
    // ..., arr, arr* =>
    mv.visitInsn(Opcodes.DUP2);
    // ..., arr, arr*, arr, arr* =>
    mv.visitInsn(Opcodes.SWAP);
    // ..., arr, arr*, arr*, arr =>
    mv.visitInsn(Opcodes.DUP);
    // ..., arr, arr*, arr*, arr, arr =>
    mv.visitInsn(Opcodes.ARRAYLENGTH);
    // ..., arr, arr*, arr*, arr, i (len) =>
    mv.visitLabel(l3);
    // ..., arr, arr*, arr*, arr, i =>
    mv.visitInsn(Opcodes.DUP);
    // ..., arr, arr*, arr*, arr, i, i =>
    mv.visitJumpInsn(Opcodes.IFGT, l1);
    // ..., arr, arr*, arr*, arr, i =>
    mv.visitInsn(Opcodes.POP2);
    // ..., arr, arr*, arr* =>
    mv.visitInsn(Opcodes.POP);
    // ..., arr, arr* =>
    mv.visitInsn(Opcodes.SWAP);
    // ..., arr*, arr =>
    mv.visitInsn(Opcodes.POP);
    // ..., arr* =>
    mv.visitJumpInsn(Opcodes.GOTO, l2);
    // ..., arr* =>
    mv.visitLabel(l1);
    // ..., arr, arr*, arr*, arr, i =>
    mv.visitInsn(Opcodes.ICONST_1);
    // ..., arr, arr*, arr*, arr, i, 1 =>
    mv.visitInsn(Opcodes.ISUB);
    // ..., arr, arr*, arr*, arr, i-1 =>
    mv.visitInsn(Opcodes.DUP_X2);
    // ..., arr, arr*, i-1, arr*, arr, i-1 =>
    mv.visitInsn(Opcodes.DUP_X1);
    // ..., arr, arr*, i-1, arr*, i-1, arr, i-1 =>
    mv.visitTypeInsn(Opcodes.NEW, arrayType.getInternalName());
    // ..., arr, arr*, i-1, arr*, i-1, arr, i-1, obj =>
    mv.visitInsn(Opcodes.DUP_X2);
    // ..., arr, arr*, i-1, arr*, i-1, obj, arr, i-1, obj =>
    mv.visitInsn(Opcodes.DUP_X2);
    // ..., arr, arr*, i-1, arr*, i-1, obj, obj, arr, i-1, obj =>
    mv.visitInsn(Opcodes.POP);
    // ..., arr, arr*, i-1, arr*, i-1, obj, obj, arr, i-1 =>
    mv.visitMethodInsn(
        Opcodes.INVOKESPECIAL, 
        arrayType.getInternalName(),
        "<init>",
        ctorParamDesc);
    // ..., arr, arr*, i-1, arr*, i-1, obj =>
    mv.visitInsn(Opcodes.AASTORE);
    // ..., arr, arr*, i-1 =>
    mv.visitInsn(Opcodes.DUP_X2);
    // ..., i-1, arr, arr*, i-1 =>
    mv.visitInsn(Opcodes.POP);
    // ..., i-1, arr, arr* =>
    mv.visitInsn(Opcodes.SWAP);
    // ..., i-1, arr*, arr =>
    mv.visitInsn(Opcodes.DUP_X2);
    // ..., arr, i-1, arr*, arr =>
    mv.visitInsn(Opcodes.SWAP);
    // ..., arr, i-1, arr, arr* =>
    mv.visitInsn(Opcodes.DUP_X2);
    // ..., arr, arr*, i-1, arr, arr* =>
    mv.visitInsn(Opcodes.DUP_X2);
    // ..., arr, arr*, arr*, i-1, arr, arr* =>
    mv.visitInsn(Opcodes.POP);
    // ..., arr, arr*, arr*, i-1, arr =>
    mv.visitInsn(Opcodes.SWAP);
    // ..., arr, arr*, arr*, arr, i-1 =>
    mv.visitJumpInsn(Opcodes.GOTO, l3);
    // ..., arr, arr*, arr*, arr, i-1 =>
    mv.visitLabel(l2);
    // ..., arr* =>
  }

	private void multiArrayToContainer(Type originalType, MethodVisitor mv) {
		// ..., arr =>
		mv.visitTypeInsn(Opcodes.NEW, MultiArrayContainer.NAME);
		// ..., arr, obj =>
		mv.visitInsn(Opcodes.DUP_X1);
		// ..., obj, arr, obj =>
		mv.visitInsn(Opcodes.SWAP);
		// ..., obj, obj, arr =>
		mv.visitInsn(Opcodes.ICONST_1);
		// ..., obj, obj, arr, I =>
		// XXX Is BIPUSH enough?
		mv.visitIntInsn(Opcodes.BIPUSH, ArrayUtil.getArrayDim(originalType));
		// ..., obj, obj, arr, I, I =>
		// XXX Is BIPUSH enough?
		mv.visitIntInsn(Opcodes.BIPUSH, ArrayUtil.getArrayDim(originalType));
		// ..., obj, obj, arr, I, I, I =>
		mv.visitIntInsn(Opcodes.BIPUSH, ArrayUtil.getArrayType(originalType)
				.getSort());
		// ..., obj, obj, arr, I, I, I, I =>
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, MultiArrayContainer.NAME,
				"<init>", MultiArrayContainer.CTOR_DESC);
		// ..., cnt =>
	}
}
