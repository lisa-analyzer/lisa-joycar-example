package it.unive.lisa.joycar.units;

import it.unive.lisa.program.ClassUnit;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.SyntheticLocation;

public class JavaObject extends ClassUnit {

	public static final String NAME = "java.lang.Object";
	public static final String SHORT_NAME = "Object";

	public JavaObject(Program program) {
		super(SyntheticLocation.INSTANCE, program, NAME, false);
	}
}
