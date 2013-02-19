package org.deuce.transform.localmetadata;

import org.deuce.objectweb.asm.Type;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public class Field{
	private final String fieldNameAddress;
	private final String fieldName;
	private final int access;
	private final Type type;
	private final Type origType;

	public Field( String fieldName, String fieldNameAddress, int access, Type type, Type origType) {
		this.fieldName = fieldName;
		this.fieldNameAddress = fieldNameAddress;
		this.access = access;
		this.type = type;
		this.origType = origType;
	}

	public String getFieldNameAddress() {
		return fieldNameAddress;
	}

	public String getFieldName() {
		return fieldName;
	}
	
	public int getAccess() {
		return access;
	}
	
	public Type getType() {
		return type;
	}
	
	public Type getOriginalType() {
		return origType;
	}
}