package org.deuce.transform.localmetadata.array;

import org.deuce.objectweb.asm.Type;
import org.deuce.transaction.ContextDelegator;
import org.deuce.transaction.IContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class MultiArrayContainer extends ArrayContainer {
	public static final String NAME = Type.getInternalName(MultiArrayContainer.class);
	public static final String DESC = Type.getDescriptor(MultiArrayContainer.class);
	
	public static final String ARRAY_FIELD_DESC = Type.getDescriptor(Object[].class);
	public Object[] array;
	
	public TxField[] metadata;
	
	public static final String NEXTDIM_FIELD_NAME = "nextDim";
	public static final String NEXTDIM_FIELD_DESC = Type.getDescriptor(ArrayContainer[].class); 
	public ArrayContainer[] nextDim;
		
	public static final String CTOR_DESC = String.format("(%s%s%s%s%s)%s", 
			Type.getDescriptor(Object[].class),	// matrix
			Type.INT_TYPE.getDescriptor(),		// currDim
			Type.INT_TYPE.getDescriptor(),		// nDims
			Type.INT_TYPE.getDescriptor(),		// totalDims
			Type.INT_TYPE.getDescriptor()		// desc
			,
			Type.VOID_TYPE.getDescriptor());
	public MultiArrayContainer(Object[] matrix, int currDim, int nDims,
			int totalDims, int arrElemType)
	{
		array = matrix;
		
		int length = matrix.length;
		metadata = new TxField[length];
		nextDim = new ArrayContainer[length];
		
		if (currDim < (totalDims - 1)) {
			/* Next dimension is not the last, so we still chain more
			 * MultiArrContainers.
			 */
			if (currDim < nDims) {
				/* We only initialise the containers IFF the original multiarray
				 * also did.
				 */
				for (int i = 0; i < length; i++) {
					nextDim[i] = new MultiArrayContainer((Object[]) matrix[i],
							currDim + 1, nDims, totalDims, arrElemType);
				}
			}
		} else if (nDims == totalDims) {
			/* Next dimension is the last, so we use desc to create the correct
			 * ArrContainers.
			 */
			switch (arrElemType) {
			case Type.INT:
				for (int i = 0; i < length; i++)
					nextDim[i] = new IntArrayContainer((int[]) matrix[i]);
				break;
			case Type.SHORT:
				for (int i = 0; i < length; i++)
					nextDim[i] = new ShortArrayContainer((short[]) matrix[i]);
				break;
			case Type.LONG:
				for (int i = 0; i < length; i++)
					nextDim[i] = new LongArrayContainer((long[]) matrix[i]);
				break;
			case Type.BYTE:
				for (int i = 0; i < length; i++)
					nextDim[i] = new ByteArrayContainer((byte[]) matrix[i]);
				break;
			case Type.BOOLEAN:
				for (int i = 0; i < length; i++)
					nextDim[i] = new BooleanArrayContainer((boolean[]) matrix[i]);
				break;
			case Type.CHAR:
				for (int i = 0; i < length; i++)
					nextDim[i] = new CharArrayContainer((char[]) matrix[i]);
				break;
			case Type.FLOAT:
				for (int i = 0; i < length; i++)
					nextDim[i] = new FloatArrayContainer((float[]) matrix[i]);
				break;
			case Type.DOUBLE:
				for (int i = 0; i < length; i++)
					nextDim[i] = new DoubleArrayContainer((double[]) matrix[i]);
				break;
			default: // object
				for (int i = 0; i < length; i++)
					nextDim[i] = new ObjectArrayContainer((Object[]) matrix[i]);
				break;
			}
		}
		
		for (int i = 0; i < length; i++) {
			Object obj;
			try {
				obj = ContextDelegator.getMetadataClass().newInstance();
				TxField field = (TxField) obj;
				field.init(nextDim, i, matrix);
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
