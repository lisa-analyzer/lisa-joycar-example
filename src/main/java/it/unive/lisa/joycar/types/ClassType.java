package it.unive.lisa.joycar.types;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import it.unive.lisa.joycar.units.JavaObject;
import it.unive.lisa.program.CompilationUnit;
import it.unive.lisa.program.Unit;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.TypeSystem;
import it.unive.lisa.type.UnitType;
import it.unive.lisa.util.collections.workset.FIFOWorkingSet;
import it.unive.lisa.util.collections.workset.WorkingSet;

public class ClassType implements UnitType {

	protected static final Map<String, ClassType> types = new HashMap<>();

	public static Collection<ClassType> all() {
		return types.values();
	}

	public static ClassType create(String name, CompilationUnit unit) {
		return types.computeIfAbsent(name, x -> new ClassType(name, unit));
	}

	public static ClassType search(String name) {
		return types.get(name);
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
			// valid since in this program we do not have cpp classes
			return search(JavaObject.NAME);

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
			current.unit.getImmediateAncestors().forEach(u -> ws.push(search(u.getName())));
		}

		return search(JavaObject.NAME);
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
	public Set<Type> allInstances(TypeSystem types) {
		Set<Type> instances = new HashSet<>();
		for (Unit in : unit.getInstances())
			instances.add(search(in.getName()));
		return instances;
	}
}
