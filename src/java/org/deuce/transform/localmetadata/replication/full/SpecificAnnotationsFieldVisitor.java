package org.deuce.transform.localmetadata.replication.full;

import java.util.Map;
import java.util.Set;

import org.deuce.distribution.replication.full.FullReplicationProtocol;
import org.deuce.distribution.replication.partial.PartialReplicationProtocol;
import org.deuce.objectweb.asm.AnnotationVisitor;
import org.deuce.objectweb.asm.FieldVisitor;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.FieldVisitorAdapter;

@ExcludeTM
public class SpecificAnnotationsFieldVisitor extends FieldVisitorAdapter
{
	private Map<String, Integer> field2OID;
	private Set<String> partialRepFields;
	private String field;

	public SpecificAnnotationsFieldVisitor(FieldVisitor next,
			Map<String, Integer> field2OID, Set<String> partialRepFields,
			String field)
	{
		super(next);
		this.field2OID = field2OID;
		this.partialRepFields = partialRepFields;
		this.field = field;
	}

	public AnnotationVisitor visitAnnotation(String desc, boolean visible)
	{
		AnnotationVisitor va = super.visitAnnotation(desc, visible);

		if (desc.equals(FullReplicationProtocol.BOOTSTRAP_DESC))
			return new BootstrapAnnotationVisitor(va, field2OID, field);
		else if (desc.equals(PartialReplicationProtocol.PARTIALREP_DESC))
		{ // Partial replicated field
			partialRepFields.add(field);
			return va;
		}
		else
			return va;
	}
}
