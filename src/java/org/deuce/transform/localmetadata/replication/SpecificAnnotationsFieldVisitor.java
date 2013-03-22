package org.deuce.transform.localmetadata.replication;

import java.util.Map;
import java.util.Set;

import org.deuce.distribution.DistributedProtocol;
import org.deuce.distribution.replication.partial.PartialReplicationProtocol;
import org.deuce.objectweb.asm.AnnotationVisitor;
import org.deuce.objectweb.asm.FieldVisitor;
import org.deuce.transform.ExcludeTM;
import org.deuce.transform.localmetadata.FieldVisitorAdapter;
import org.deuce.transform.localmetadata.replication.full.BootstrapAnnotationVisitor;

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

		if (desc.equals(DistributedProtocol.BOOTSTRAP_DESC))
		{ // Bootstrap field
			return new BootstrapAnnotationVisitor(va, field2OID, field);
		}
		else if (desc.equals(PartialReplicationProtocol.PARTIALREP_DESC))
		{ // Partial replicated field
			partialRepFields.add(field);
			return va;
		}
		else
		{ // Normal field
			return va;
		}
	}
}
