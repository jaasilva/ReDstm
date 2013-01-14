package org.deuce.transaction.field;

import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.type.TxField;

@ExcludeTM
public class FloatWriteFieldAccess extends WriteFieldAccess {
	private static final long serialVersionUID = 1L;
	private float value;

	public void set(float value, TxField field) {
		super.init(field);
		this.value = value;
	}

	@Override
	public void put() {
//		UnsafeHolder.getUnsafe().putFloat(field.ref, field.address, value);
		field.writeFloat(value);
//		clear();
	}

	public float getValue() {
		return value;
	}

}
