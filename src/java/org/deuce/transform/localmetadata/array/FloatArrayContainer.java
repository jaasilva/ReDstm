package org.deuce.transform.localmetadata.array;

import org.deuce.objectweb.asm.Type;
import org.deuce.reflection.AddressUtil;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.IContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class FloatArrayContainer extends ArrayContainer {
	public static final String NAME = Type.getInternalName(FloatArrayContainer.class);
	public static final String DESC = Type.getDescriptor(FloatArrayContainer.class);
	
	public static final String ARRAY_FIELD_DESC = Type.getDescriptor(float[].class);
	public float[] array;
	
	public TxField[] metadata;
	
	public static final String CTOR_DESC = String.format("(%s)%s",
			Type.getDescriptor(float[].class)	// arr
			,
			Type.VOID_TYPE.getDescriptor());
	public FloatArrayContainer(float[] arr) {
		array = arr;
		
		final int base = AddressUtil.arrayBaseOffset(float[].class);
		final int scale = AddressUtil.arrayIndexScale(float[].class);
		int length = arr.length;
		metadata = new TxField[length];
		for (int i = 0; i < length; i++) {
			Object obj;
			try {
				obj = ContextDelegator.getMetadataClass().newInstance();
				TxField field = (TxField) obj;
				field.init(arr, base + scale * i);
				metadata[i] = field;
			} catch (InstantiationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IllegalAccessException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	public Object getArray() {
		return array;
	}

	public Object getArray(IContext ctx) {
		return array;
	}
}
