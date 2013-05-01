package org.deuce.transform.localmetadata;

import org.deuce.objectweb.asm.AnnotationVisitor;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public abstract class AnnotationVisitorAdapter implements AnnotationVisitor
{
	private final AnnotationVisitor next;

	public AnnotationVisitorAdapter(AnnotationVisitor next)
	{
		this.next = next;
	}

	public void visit(String name, Object value)
	{
		next.visit(name, value);
	}

	public void visitEnum(String name, String desc, String value)
	{
		next.visitEnum(name, desc, value);
	}

	public AnnotationVisitor visitAnnotation(String name, String desc)
	{
		return next.visitAnnotation(name, desc);
	}

	public AnnotationVisitor visitArray(String name)
	{
		return next.visitArray(name);
	}

	public void visitEnd()
	{
		next.visitEnd();
	}
}
