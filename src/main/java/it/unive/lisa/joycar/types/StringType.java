package it.unive.lisa.joycar.types;

import it.unive.lisa.joycar.units.JavaString;

public class StringType extends ClassType implements it.unive.lisa.type.StringType {

	public StringType() {
		super(JavaString.NAME, JavaString.INSTANCE);
		types.put(JavaString.NAME, this);
		types.put(JavaString.SHORT_NAME, this);
	}
}
