package org.deuce.transform.localmetadata;

import org.deuce.objectweb.asm.AnnotationVisitor;
import org.deuce.objectweb.asm.Attribute;
import org.deuce.objectweb.asm.FieldVisitor;
import org.deuce.transform.ExcludeTM;

@ExcludeTM
public abstract class FieldVisitorAdapter implements FieldVisitor {
	private final FieldVisitor next;
	
	public FieldVisitorAdapter(FieldVisitor next) {
		this.next = next;
	}
	
	public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
		return next.visitAnnotation(desc, visible);
	}

	public void visitAttribute(Attribute attr) {
		next.visitAttribute(attr);
	}

	public void visitEnd() {
		next.visitEnd();
	}
}
