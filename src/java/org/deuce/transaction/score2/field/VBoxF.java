package org.deuce.transaction.score2.field;

import org.deuce.transform.ExcludeTM;

/**
 * 
 * @author Ricardo Dias <ricardo.dias@campus.fct.unl.pt>
 */
@ExcludeTM
public interface VBoxF extends VBox
{
	void commit(float value, int txNumber);
}