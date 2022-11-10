package it.unive.lisa.joycar;

import it.unive.lisa.joycar.types.ClassType;
import it.unive.lisa.joycar.units.JavaString;
import it.unive.lisa.type.BooleanType;
import it.unive.lisa.type.NumericType;
import it.unive.lisa.type.StringType;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.TypeSystem;
import it.unive.lisa.type.common.BoolType;
import it.unive.lisa.type.common.Int32Type;

public class JavaTypeSystem extends TypeSystem {

	@Override
	public BooleanType getBooleanType() {
		return BoolType.INSTANCE;
	}

	@Override
	public StringType getStringType() {
		return (StringType) ClassType.lookup(JavaString.NAME, null);
	}

	@Override
	public NumericType getIntegerType() {
		return Int32Type.INSTANCE;
	}

	@Override
	public boolean canBeReferenced(Type type) {
		return type.isInMemoryType();
	}
}
