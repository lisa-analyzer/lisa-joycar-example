package it.lucaneg.lisa.joycar.java.types;

import it.unive.lisa.program.CompilationUnit;

public class StringType extends ClassType implements it.unive.lisa.type.StringType {

	public StringType(CompilationUnit unit) {
		super(JAVA_LANG_STRING, unit);
		types.put(JAVA_LANG_STRING, this);
	}
}
