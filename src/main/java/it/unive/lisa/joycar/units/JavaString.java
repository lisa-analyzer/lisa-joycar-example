package it.unive.lisa.joycar.units;

import it.unive.lisa.program.ClassUnit;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.SyntheticLocation;

public class JavaString extends ClassUnit {

	public static final String NAME = "java.lang.String";
	public static final String SHORT_NAME = "String";

	public JavaString(Program program, JavaObject obj) {
		super(SyntheticLocation.INSTANCE, program, NAME, true);
		addAncestor(obj);
	}
}
