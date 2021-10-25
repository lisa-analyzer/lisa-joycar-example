package it.lucaneg.lisa.joycar;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.lucaneg.lisa.joycar.java.types.ArrayType;
import it.lucaneg.lisa.joycar.java.types.ClassType;
import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.type.VoidType;
import it.unive.lisa.type.common.BoolType;
import it.unive.lisa.type.common.Int32;

public class App {
	
	private static final Logger LOG = LogManager.getLogger(App.class);
	
	public static final CodeLocation LIB_LOCATION = new CodeLocation() {
		@Override
		public int compareTo(CodeLocation o) {
			return o == this ? 0 : -1;
		}

		@Override
		public String getCodeLocation() {
			return "library code";
		}
	};

	public static void main(String[] args) throws AnalysisException {
		Program program = new Program();

		JavaFrontendSimulator.simulateJavaParsing(program);
		CppFrontendSimulator.simulateCppParsing(program);

		program.registerType(Int32.INSTANCE);
		program.registerType(BoolType.INSTANCE);
		program.registerType(VoidType.INSTANCE);
		ClassType.all().forEach(program::registerType);
		ArrayType.all().forEach(program::registerType);

		String workdir = "analysis/" + UUID.randomUUID();
		LOG.info("Running analysis in: " + workdir);

		LiSAConfiguration conf = new LiSAConfiguration().setDumpCFGs(true).setWorkdir(workdir);
		LiSA lisa = new LiSA(conf);
		lisa.run(program);
	}
}
