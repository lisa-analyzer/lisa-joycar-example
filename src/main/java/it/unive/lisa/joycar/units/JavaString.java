package it.unive.lisa.joycar.units;

import it.unive.lisa.program.CompilationUnit;
import it.unive.lisa.program.SyntheticLocation;

public class JavaString extends CompilationUnit {

	public static final String NAME = "java.lang.String";
	public static final String SHORT_NAME = "String";

	public static final JavaString INSTANCE = new JavaString();

	private JavaString() {
		super(SyntheticLocation.INSTANCE, NAME, true);
		addSuperUnit(JavaObject.INSTANCE);
	}
}
