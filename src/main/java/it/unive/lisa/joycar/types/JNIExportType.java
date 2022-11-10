package it.unive.lisa.joycar.types;

import java.util.Collections;
import java.util.Set;

import it.unive.lisa.type.Type;
import it.unive.lisa.type.TypeSystem;

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
	public Set<Type> allInstances(TypeSystem types) {
		return Collections.singleton(this);
	}

}
