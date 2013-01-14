package org.deuce.transaction;

import java.io.Serializable;

import org.deuce.transaction.field.ReadFieldAccess;
import org.deuce.transform.ExcludeTM;


/**
 * Represents the transaction read set.
 * And acts as a recycle pool of the {@link ReadFieldAccess}.
 *  
 * @author Guy Korland
 * @since 0.7
 */
@ExcludeTM
public class ReadSet implements Serializable {
	private static final long serialVersionUID = 8775391877670448988L;
	protected static final int DEFAULT_CAPACITY = 1024;
	protected ReadFieldAccess[] readSet = new ReadFieldAccess[DEFAULT_CAPACITY];
	protected int nextAvaliable = 0;
	
	public ReadSet(){
		fillArray( 0);
	}
	
	public void clear(){
		nextAvaliable = 0;
	}

	private void fillArray( int offset){
		for( int i=offset ; i < readSet.length ; ++i){
			readSet[i] = new ReadFieldAccess();
		}
	}

	public ReadFieldAccess getNext(){
		if( nextAvaliable >= readSet.length){
			int orignLength = readSet.length;
			ReadFieldAccess[] tmpReadSet = new ReadFieldAccess[ 2*orignLength];
			System.arraycopy(readSet, 0, tmpReadSet, 0, orignLength);
			readSet = tmpReadSet;
			fillArray( orignLength);
		}
		return readSet[ nextAvaliable++];
	}
	
    public int size() {
    	return nextAvaliable;
    }
}
