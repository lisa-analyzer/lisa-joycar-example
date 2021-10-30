package it.unive.lisa.joycar.java.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unive.lisa.program.CompilationUnit;
import it.unive.lisa.type.PointerType;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.UnitType;
import it.unive.lisa.util.collections.workset.FIFOWorkingSet;
import it.unive.lisa.util.collections.workset.WorkingSet;

public class ClassType implements PointerType, UnitType {

	public static final String JAVA_LANG_OBJECT = "java.lang.Object";
	public static final String JAVA_LANG_STRING = "java.lang.String";

	protected static final Map<String, ClassType> types = new HashMap<>();

	public static Collection<ClassType> all() {
		return types.values();
	}

	public static ClassType lookup(String name, CompilationUnit unit) {
		return types.computeIfAbsent(name, x -> new ClassType(name, unit));
	}

	private final String name;

	private final CompilationUnit unit;

	protected ClassType(String name, CompilationUnit unit) {
		this.name = name;
		this.unit = unit;
	}

	@Override
	public CompilationUnit getUnit() {
		return unit;
	}

	@Override
	public final boolean canBeAssignedTo(Type other) {
		return other instanceof ClassType && subclass((ClassType) other);
	}

	private boolean subclass(ClassType other) {
		return this == other || unit.isInstanceOf(other.unit);
	}

	@Override
	public Type commonSupertype(Type other) {
		if (other.isNullType())
			return this;

		if (!other.isUnitType())
			return lookup(JAVA_LANG_OBJECT, null);

		if (canBeAssignedTo(other))
			return other;

		if (other.canBeAssignedTo(this))
			return this;

		return scanForSupertypeOf((UnitType) other);
	}

	private Type scanForSupertypeOf(UnitType other) {
		WorkingSet<ClassType> ws = FIFOWorkingSet.mk();
		Set<ClassType> seen = new HashSet<>();
		ws.push(this);
		ClassType current;
		while (!ws.isEmpty()) {
			current = ws.pop();
			if (!seen.add(current))
				continue;

			if (other.canBeAssignedTo(current))
				return current;

			// null since we do not want to create new types here
			current.unit.getSuperUnits().forEach(u -> ws.push(lookup(u.getName(), null)));
		}

		return lookup(JAVA_LANG_OBJECT, null);
	}

	@Override
	public String toString() {
		return name;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		result = prime * result + ((unit == null) ? 0 : unit.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ClassType other = (ClassType) obj;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		if (unit == null) {
			if (other.unit != null)
				return false;
		} else if (!unit.equals(other.unit))
			return false;
		return true;
	}

	@Override
	public Collection<Type> allInstances() {
		Collection<Type> instances = new HashSet<>();
		for (CompilationUnit in : unit.getInstances())
			instances.add(lookup(in.getName(), null));
		return instances;
	}
}
