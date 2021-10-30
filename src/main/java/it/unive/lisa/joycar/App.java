package it.unive.lisa.joycar;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.analysis.nonrelational.inference.InferenceSystem;
import it.unive.lisa.checks.warnings.Warning;
import it.unive.lisa.interprocedural.ContextBasedAnalysis;
import it.unive.lisa.interprocedural.RecursionFreeToken;
import it.unive.lisa.interprocedural.callgraph.RTACallGraph;
import it.unive.lisa.joycar.java.types.ArrayType;
import it.unive.lisa.joycar.java.types.ClassType;
import it.unive.lisa.joycar.java.types.StringType;
import it.unive.lisa.program.CompilationUnit;
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
	
	public static ClassType OBJECT_TYPE;
	public static ClassType STRING_TYPE;
	public static boolean useSanitizer;

	public static void main(String[] args) throws AnalysisException {
		useSanitizer = args.length == 1 && args[0].equals("sanitize");
		
		Program program = new Program();

		buildObject(program);
		buildString(program);
		
		CppFrontendSimulator.simulateCppParsing(program);
		JavaFrontendSimulator.simulateJavaParsing(program);

		program.registerType(Int32.INSTANCE);
		program.registerType(BoolType.INSTANCE);
		program.registerType(VoidType.INSTANCE);
		ClassType.all().forEach(program::registerType);
		ArrayType.all().forEach(program::registerType);

		String workdir = "analysis/" + UUID.randomUUID();

		LiSAConfiguration conf = new LiSAConfiguration()
				.setWorkdir(workdir)
				.setJsonOutput(true)
				.setDumpAnalysis(true)
				.setInferTypes(true)
				.setInterproceduralAnalysis(new ContextBasedAnalysis<>(RecursionFreeToken.getSingleton()))
				.setCallGraph(new RTACallGraph())
				.setAbstractState(new SimpleAbstractState<>(new MonolithicHeap(), new InferenceSystem<>(new Taint())))
				.addSemanticCheck(new TaintChecker());
		LiSA lisa = new LiSA(conf);
		lisa.run(program);
		
		LOG.info("Analysis ran in: " + workdir);
		LOG.info("The analysis generated the following warnings: ");
		for (Warning warn : lisa.getWarnings())
			LOG.info(warn);
	}

	private static void buildObject(Program program) {
		CompilationUnit object = new CompilationUnit(App.LIB_LOCATION, ClassType.JAVA_LANG_OBJECT, false);
		program.addCompilationUnit(object);
		OBJECT_TYPE = ClassType.lookup(ClassType.JAVA_LANG_OBJECT, object);
	}

	private static void buildString(Program program) {
		CompilationUnit string = new CompilationUnit(App.LIB_LOCATION, ClassType.JAVA_LANG_STRING, false);
		string.addSuperUnit(OBJECT_TYPE.getUnit());
		program.addCompilationUnit(string);
		// this will automatically register it inside the types cache
		STRING_TYPE = new StringType(string);
	}
}
