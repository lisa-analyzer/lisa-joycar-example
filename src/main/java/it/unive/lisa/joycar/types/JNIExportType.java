package it.unive.lisa.joycar.types;

import java.util.Collection;
import java.util.Collections;

import it.unive.lisa.type.Type;

public class JNIExportType implements Type {

	public static final JNIExportType INSTANCE = new JNIExportType();
	
	private JNIExportType() {}
	
	@Override
	public boolean canBeAssignedTo(Type other) {
		return false;
	}

	@Override
	public Type commonSupertype(Type other) {
		return this;
	}

	@Override
	public Collection<Type> allInstances() {
		return Collections.singleton(this);
	}

}
