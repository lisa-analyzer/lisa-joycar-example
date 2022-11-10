package it.unive.lisa.joycar.units;

import it.unive.lisa.program.ClassUnit;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.SyntheticLocation;

public class JNIEnv extends ClassUnit {

	public static final String SHORT_NAME = "JNIEnv";

	public JNIEnv(Program program, JavaObject obj) {
		super(SyntheticLocation.INSTANCE, program, SHORT_NAME, true);
		addAncestor(obj);
	}
}
