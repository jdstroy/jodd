// Copyright (c) 2003-2014, Jodd Team (jodd.org). All Rights Reserved.

package jodd.jtx.meta;

import jodd.jtx.JtxIsolationLevel;
import jodd.jtx.JtxPropagationBehavior;
import jodd.jtx.JtxTransactionMode;
import jodd.typeconverter.Convert;
import jodd.util.AnnotationDataReader;

import java.lang.annotation.Annotation;
import java.lang.reflect.AccessibleObject;

/**
 * Transaction annotation.
 */
public class TransactionAnnotation<A extends Annotation> extends AnnotationDataReader<A, TransactionAnnotationData<A>> {

	public TransactionAnnotation() {
		super(null, Transaction.class);
	}
	public TransactionAnnotation(Class<A> annotationClass) {
		super(annotationClass, Transaction.class);
	}

	/**
	 * Need to override to make java compiler happy.
	 */
	@Override
	public TransactionAnnotationData<A> readAnnotationData(AccessibleObject accessibleObject) {
		return super.readAnnotationData(accessibleObject);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected TransactionAnnotationData<A> createAnnotationData(A annotation) {

		TransactionAnnotationData<A> td = new TransactionAnnotationData<A>(annotation);

		td.propagation = (JtxPropagationBehavior) readElement(annotation, "propagation");

		td.isolation = (JtxIsolationLevel) readElement(annotation, "isolation");

		td.readOnly = Convert.toBooleanValue(readElement(annotation, "readOnly"));

		td.timeout = Convert.toIntValue(readStringElement(annotation, "timeout"));

		return td;
	}

}
