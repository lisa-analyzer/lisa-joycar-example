package it.unive.lisa.joycar.units;

import it.unive.lisa.program.CompilationUnit;
import it.unive.lisa.program.SyntheticLocation;

public class JNIEnv extends CompilationUnit {

	public static final String SHORT_NAME = "JNIEnv";

	public static final JNIEnv INSTANCE = new JNIEnv();
	
	private  JNIEnv() {
		super(SyntheticLocation.INSTANCE, SHORT_NAME, true);
	}
}
