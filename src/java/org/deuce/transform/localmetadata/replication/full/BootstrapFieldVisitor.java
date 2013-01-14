package org.deuce.transform.localmetadata.replication.full;

import java.util.Map;

import org.deuce.distribution.replication.full.FullReplicationProtocol;
import org.deuce.objectweb.asm.AnnotationVisitor;
import org.deuce.objectweb.asm.FieldVisitor;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.FieldVisitorAdapter;


@ExcludeTM
public class BootstrapFieldVisitor extends FieldVisitorAdapter {
	private Map<String, Integer> field2OID;
	private String field;

	public BootstrapFieldVisitor(FieldVisitor next,
			Map<String, Integer> field2OID, String field) {

		super(next);
		this.field2OID = field2OID;
		this.field = field;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		AnnotationVisitor va = super.visitAnnotation(desc, visible);

		if (desc.equals(FullReplicationProtocol.BOOTSTRAP_DESC))
			return new BootstrapAnnotationVisitor(va, field2OID, field);
		else
			return va;
	}
}
