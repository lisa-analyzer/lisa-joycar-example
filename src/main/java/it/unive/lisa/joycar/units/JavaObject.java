package it.unive.lisa.joycar.units;

import it.unive.lisa.program.CompilationUnit;
import it.unive.lisa.program.SyntheticLocation;

public class JavaObject extends CompilationUnit {

	public static final String NAME = "java.lang.Object";
	public static final String SHORT_NAME = "Object";

	public static final JavaObject INSTANCE = new JavaObject();

	private JavaObject() {
		super(SyntheticLocation.INSTANCE, NAME, false);
	}
}
