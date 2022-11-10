package it.unive.lisa.joycar.types;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.tuple.Pair;

import it.unive.lisa.joycar.units.JavaObject;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.TypeSystem;

public class ArrayType implements it.unive.lisa.type.ArrayType {

	private static final Map<Pair<Type, Integer>, ArrayType> types = new HashMap<>();

	/**
	 * Clears the cache of {@link ArrayType}s created up to now.
	 */
	public static void clearAll() {
		types.clear();
	}

	/**
	 * Yields all the {@link ArrayType}s defined up to now.
	 * 
	 * @return the collection of all the array types
	 */
	public static Collection<ArrayType> all() {
		return types.values();
	}

	/**
	 * Yields a unique instance (either an existing one or a fresh one) of
	 * {@link ArrayType} representing an array with the given {@code base} type
	 * and the given {@code dimensions}.
	 * 
	 * @param base       the base type of the array
	 * @param dimensions the number of dimensions of this array
	 * 
	 * @return the unique instance of {@link ArrayType} representing the class
	 *             with the given name
	 */
	public static ArrayType lookup(Type base, int dimensions) {
		return types.computeIfAbsent(Pair.of(base, dimensions), x -> new ArrayType(base, dimensions));
	}

	private final Type base;

	private final int dimensions;

	private ArrayType(Type base, int dimensions) {
		this.base = base;
		this.dimensions = dimensions;
	}

	@Override
	public final boolean canBeAssignedTo(Type other) {
		return other instanceof ArrayType && getInnerType().canBeAssignedTo(other.asArrayType().getInnerType());
	}

	@Override
	public Type commonSupertype(Type other) {
		if (canBeAssignedTo(other))
			return other;

		if (other.canBeAssignedTo(this))
			return this;

		if (other.isNullType())
			return this;

		if (!other.isArrayType())
			return ClassType.lookup(JavaObject.NAME, null);

		return getInnerType().commonSupertype(other.asArrayType().getInnerType());
	}

	@Override
	public String toString() {
		return base + "[]".repeat(dimensions);
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((base == null) ? 0 : base.hashCode());
		result = prime * result + dimensions;
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
		ArrayType other = (ArrayType) obj;
		if (base == null) {
			if (other.base != null)
				return false;
		} else if (!base.equals(other.base))
			return false;
		if (dimensions != other.dimensions)
			return false;
		return true;
	}

	@Override
	public Type getInnerType() {
		if (dimensions == 0)
			return base;
		return lookup(base, dimensions - 1);
	}

	@Override
	public Type getBaseType() {
		return base;
	}

	@Override
	public Set<Type> allInstances(TypeSystem types) {
		return Collections.singleton(this);
	}
}

