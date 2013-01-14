package org.deuce.transform.localmetadata.type.speculative;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.concurrent.LinkedBlockingDeque;

import org.deuce.transaction.SpeculativeContext;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.array.ArrayContainer;
import org.deuce.transform.localmetadata.type.TxField;


@ExcludeTM
public class SpeculativeTxField extends TxField {
	public transient Deque<SpeculativeVersion> speculativeList = new LinkedBlockingDeque<SpeculativeVersion>();

	public SpeculativeTxField() {
	}

	public SpeculativeTxField(Object ref, long address) {
		super(ref, address);
	}

	public boolean speculativeReadBoolean(SpeculativeContext ctx) {
		try {
			SpeculativeVersion version = speculativeList.getLast();
			if (version.ctx.getSpeculativeVersionNumber() > ctx
					.getSpeculativeVersionNumber())
				throw SpeculativeContext.EARLY_CONFLICT;
			else
				return (Boolean) version.value;

		} catch (NoSuchElementException e) {
			return super.readBoolean();
		}
	}
	
	public byte speculativeReadByte(SpeculativeContext ctx) {
		try {
			SpeculativeVersion version = speculativeList.getLast();
			if (version.ctx.getSpeculativeVersionNumber() > ctx
					.getSpeculativeVersionNumber())
				throw SpeculativeContext.EARLY_CONFLICT;
			else
				return (Byte) version.value;

		} catch (NoSuchElementException e) {
			return super.readByte();
		}
	}

	public char speculativeReadChar(SpeculativeContext ctx) {
		try {
			SpeculativeVersion version = speculativeList.getLast();
			if (version.ctx.getSpeculativeVersionNumber() > ctx
					.getSpeculativeVersionNumber())
				throw SpeculativeContext.EARLY_CONFLICT;
			else
				return (Character) version.value;

		} catch (NoSuchElementException e) {
			return super.readChar();
		}
	}

	public double speculativeReadDouble(SpeculativeContext ctx) {
		try {
			SpeculativeVersion version = speculativeList.getLast();
			if (version.ctx.getSpeculativeVersionNumber() > ctx
					.getSpeculativeVersionNumber())
				throw SpeculativeContext.EARLY_CONFLICT;
			else
				return (Double) version.value;

		} catch (NoSuchElementException e) {
			return super.readDouble();
		}
	}

	public float speculativeReadFloat(SpeculativeContext ctx) {
		try {
			SpeculativeVersion version = speculativeList.getLast();
			if (version.ctx.getSpeculativeVersionNumber() > ctx
					.getSpeculativeVersionNumber())
				throw SpeculativeContext.EARLY_CONFLICT;
			else
				return (Float) version.value;

		} catch (NoSuchElementException e) {
			return super.readFloat();
		}
	}

	public int speculativeReadInt(SpeculativeContext ctx) {
		try {
			SpeculativeVersion version = speculativeList.getLast();
			if (version.ctx.getSpeculativeVersionNumber() > ctx
					.getSpeculativeVersionNumber())
				throw SpeculativeContext.EARLY_CONFLICT;
			else
				return (Integer) version.value;

		} catch (NoSuchElementException e) {
			return super.readInt();
		}
	}

	public long speculativeReadLong(SpeculativeContext ctx) {
		try {
			SpeculativeVersion version = speculativeList.getLast();
			if (version.ctx.getSpeculativeVersionNumber() > ctx
					.getSpeculativeVersionNumber())
				throw SpeculativeContext.EARLY_CONFLICT;
			else
				return (Long) version.value;

		} catch (NoSuchElementException e) {
			return super.readLong();
		}
	}

	public Object speculativeReadObject(SpeculativeContext ctx) {
		try {
			SpeculativeVersion version = speculativeList.getLast();
			if (version.ctx.getSpeculativeVersionNumber() > ctx
					.getSpeculativeVersionNumber())
				throw SpeculativeContext.EARLY_CONFLICT;
			else
				return version.value;

		} catch (NoSuchElementException e) {
			return super.readObject();
		}
	}

	public short speculativeReadShort(SpeculativeContext ctx) {
		try {
			SpeculativeVersion version = speculativeList.getLast();
			if (version.ctx.getSpeculativeVersionNumber() > ctx
					.getSpeculativeVersionNumber())
				throw SpeculativeContext.EARLY_CONFLICT;
			else
				return (Short) version.value;

		} catch (NoSuchElementException e) {
			return super.readShort();
		}
	}
	
	public ArrayContainer speculativeReadArray(SpeculativeContext ctx) {
		try {
			SpeculativeVersion version = speculativeList.getLast();
			if (version.ctx.getSpeculativeVersionNumber() > ctx
					.getSpeculativeVersionNumber())
				throw SpeculativeContext.EARLY_CONFLICT;
			else
				return (ArrayContainer) version.value;

		} catch (NoSuchElementException e) {
			return super.readArray();
		}
	}

	public void speculativeCommit(Object value, SpeculativeContext ctx) {
		speculativeList.add(new SpeculativeVersion(value, ctx));
	}

	public void commitBoolean(Boolean value) {
		speculativeList.remove(value);
		super.writeBoolean(value);
	}

	public void commitByte(Byte value) {
		speculativeList.remove(value);
		super.writeByte(value);
	}

	public void commitChar(Character value) {
		speculativeList.remove(value);
		super.writeChar(value);
	}

	public void commitDouble(Double value) {
		speculativeList.remove(value);
		super.writeDouble(value);
	}

	public void commitFloat(Float value) {
		speculativeList.remove(value);
		super.writeFloat(value);
	}

	public void commitInt(Integer value) {
		speculativeList.remove(value);
		super.writeInt(value);
	}

	public void commitLong(Long value) {
		speculativeList.remove(value);
		super.writeLong(value);
	}

	public void commitObject(Object value) {
		speculativeList.remove(value);
		super.writeObject(value);
	}

	public void commitShort(Short value) {
		speculativeList.remove(value);
		super.writeShort(value);
	}

	public void commitArray(ArrayContainer value) {
		speculativeList.remove(value);
		super.writeArray(value);
	}

	public void speculativeAbort(Object value) {
		speculativeList.remove(value);
	}

	private void readObject(ObjectInputStream stream) throws IOException,
			ClassNotFoundException {
		stream.defaultReadObject();
		speculativeList = new LinkedBlockingDeque<SpeculativeVersion>();
	}
}
