package org.deuce.transform.localmetadata.replication.full;

import java.util.Map;

import org.deuce.distribution.replication.full.FullReplicationProtocol;
import org.deuce.objectweb.asm.AnnotationVisitor;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.AnnotationVisitorAdapter;


@ExcludeTM
public class BootstrapAnnotationVisitor extends AnnotationVisitorAdapter {
	private Map<String, Integer> field2OID;
	private String field;

	public BootstrapAnnotationVisitor(AnnotationVisitor av,
			Map<String, Integer> field2OID, String field) {
		super(av);
		this.field2OID = field2OID;
		this.field = field;
	}

	public void visit(String name, Object value) {
		if (name.equals(FullReplicationProtocol.BOOTSTRAP_ID_PARAM_NAME))
			field2OID.put(field, (Integer) value);
		
		super.visit(name, value);
	}
}
