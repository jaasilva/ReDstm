/**
 * 
 */
package org.deuce.transaction.score;

import org.deuce.LocalMetadata;
import org.deuce.transaction.DistributedContext;
import org.deuce.transaction.DistributedContextState;
import org.deuce.transaction.ReadSet;
import org.deuce.transaction.WriteSet;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;

/**
 * @author jaasilva
 * 
 */
@ExcludeTM
@LocalMetadata(metadataClass = "...")
public class SCOReContext extends DistributedContext
{

	/**
	 * 
	 */
	public SCOReContext()
	{
		// TODO SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.transaction.ContextMetadata#beforeReadAccess(org.deuce.transform
	 * .localmetadata.type.TxField)
	 */
	@Override
	public void beforeReadAccess(TxField field)
	{
		// TODO beforeReadAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.transaction.ContextMetadata#onReadAccess(org.deuce.transform
	 * .localmetadata.array.ArrayContainer,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public ArrayContainer onReadAccess(ArrayContainer value, TxField field)
	{
		// TODO onReadAccess SCOReContext
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(java.lang.Object,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public Object onReadAccess(Object value, TxField field)
	{
		// TODO onReadAccess SCOReContext
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(boolean,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public boolean onReadAccess(boolean value, TxField field)
	{
		// TODO onReadAccess SCOReContext
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(byte,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public byte onReadAccess(byte value, TxField field)
	{
		// TODO onReadAccess SCOReContext
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(char,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public char onReadAccess(char value, TxField field)
	{
		// TODO onReadAccess SCOReContext
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(short,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public short onReadAccess(short value, TxField field)
	{
		// TODO onReadAccess SCOReContext
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(int,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public int onReadAccess(int value, TxField field)
	{
		// TODO onReadAccess SCOReContext
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(long,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public long onReadAccess(long value, TxField field)
	{
		// TODO onReadAccess SCOReContext
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(float,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public float onReadAccess(float value, TxField field)
	{
		// TODO onReadAccess SCOReContext
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onReadAccess(double,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public double onReadAccess(double value, TxField field)
	{
		// TODO onReadAccess SCOReContext
		return 0;
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.transaction.ContextMetadata#onWriteAccess(org.deuce.transform
	 * .localmetadata.array.ArrayContainer,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(ArrayContainer value, TxField field)
	{
		// TODO onWriteAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see
	 * org.deuce.transaction.ContextMetadata#onWriteAccess(java.lang.Object,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(Object value, TxField field)
	{
		// TODO onWriteAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(boolean,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(boolean value, TxField field)
	{
		// TODO onWriteAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(byte,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(byte value, TxField field)
	{
		// TODO onWriteAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(char,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(char value, TxField field)
	{
		// TODO onWriteAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(short,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(short value, TxField field)
	{
		// TODO onWriteAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(int,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(int value, TxField field)
	{
		// TODO onWriteAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(long,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(long value, TxField field)
	{
		// TODO onWriteAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(float,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(float value, TxField field)
	{
		// TODO onWriteAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.ContextMetadata#onWriteAccess(double,
	 * org.deuce.transform.localmetadata.type.TxField)
	 */
	@Override
	public void onWriteAccess(double value, TxField field)
	{
		// TODO onWriteAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.IContext#onIrrevocableAccess()
	 */
	@Override
	public void onIrrevocableAccess()
	{
		// TODO onIrrevocableAccess SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#createReadSet()
	 */
	@Override
	protected ReadSet createReadSet()
	{
		// TODO createReadSet SCOReContext
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#createWriteSet()
	 */
	@Override
	protected WriteSet createWriteSet()
	{
		// TODO createWriteSet SCOReContext
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#createState()
	 */
	@Override
	public DistributedContextState createState()
	{
		// TODO createState SCOReContext
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#initialise(int,
	 * java.lang.String)
	 */
	@Override
	protected void initialise(int atomicBlockId, String metainf)
	{
		// TODO initialise SCOReContext
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#performValidation()
	 */
	@Override
	protected boolean performValidation()
	{
		// TODO performValidation SCOReContext
		return false;
	}

	/*
	 * (non-Javadoc)
	 * @see org.deuce.transaction.DistributedContext#applyUpdates()
	 */
	@Override
	protected void applyUpdates()
	{
		// TODO applyUpdates SCOReContext
	}

}
