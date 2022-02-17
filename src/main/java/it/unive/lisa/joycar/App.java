package it.unive.lisa.joycar;

import java.io.IOException;
import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.analysis.nonrelational.inference.InferenceSystem;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.checks.warnings.Warning;
import it.unive.lisa.interprocedural.ContextBasedAnalysis;
import it.unive.lisa.interprocedural.RecursionFreeToken;
import it.unive.lisa.interprocedural.ReturnTopPolicy;
import it.unive.lisa.interprocedural.callgraph.RTACallGraph;
import it.unive.lisa.joycar.types.ArrayType;
import it.unive.lisa.joycar.types.ClassType;
import it.unive.lisa.joycar.types.StringType;
import it.unive.lisa.joycar.units.JNIEnv;
import it.unive.lisa.joycar.units.JavaObject;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.type.VoidType;
import it.unive.lisa.type.common.BoolType;
import it.unive.lisa.type.common.Int32;
import it.unive.lisa.type.common.Int64;

public class App {

	private static final Logger LOG = LogManager.getLogger(App.class);

	private static boolean useSanitizer;

	public static void main(String[] args) throws AnalysisException, IOException {
		useSanitizer = args.length == 1 && args[0].equals("sanitize");

		ClassType.lookup(JavaObject.NAME, JavaObject.INSTANCE);
		ClassType.lookup(JavaObject.SHORT_NAME, JavaObject.INSTANCE);
		new StringType(); // this will register it among the class types
		ClassType.lookup(JNIEnv.SHORT_NAME, JNIEnv.INSTANCE);

		Program p1 = CppFrontend.parse("original/JoyCar.cpp");
		Program p2 = JavaFrontend.parse("original/JoyCar.java");
		addTaintAnnotations(p1);

		Program program = merge(p1, p2);

		ClassType.all().forEach(program::registerType);
		ArrayType.all().forEach(program::registerType);
		program.registerType(Int32.INSTANCE);
		program.registerType(Int64.INSTANCE);
		program.registerType(BoolType.INSTANCE);
		program.registerType(VoidType.INSTANCE);

		String workdir = "analysis/" + UUID.randomUUID();

		LiSAConfiguration conf = new LiSAConfiguration()
				.setWorkdir(workdir)
				.setOpenCallPolicy(ReturnTopPolicy.INSTANCE)
				.setJsonOutput(true)
				.setDumpAnalysis(true)
				.setInterproceduralAnalysis(new ContextBasedAnalysis<>(RecursionFreeToken.getSingleton()))
				.setCallGraph(new RTACallGraph())
				.setAbstractState(new SimpleAbstractState<>(new MonolithicHeap(), new InferenceSystem<>(new Taint()),
						new TypeEnvironment<>(new InferredTypes())))
				.addSemanticCheck(new TaintChecker());
		LiSA lisa = new LiSA(conf);
		lisa.run(program);

		LOG.info("Analysis ran in: " + workdir);
		LOG.info("The analysis generated the following warnings: ");
		for (Warning warn : lisa.getWarnings())
			LOG.info(warn);
	}

	private static void addTaintAnnotations(Program cppProgram) {
		// the second parameter of softPwmWrite is marked as a sink for
		// tainted data, but at the moment you cannot specify annotations on
		// code outside of the analysis - annotating this parameter is
		// equivalent
		cppProgram.getAllCFGs()
				.stream()
				.filter(cfg -> cfg.getDescriptor().getName().equals("communicate"))
				.map(CFG::getDescriptor)
				.forEach(descriptor -> descriptor.getFormals()[1].addAnnotation(TaintChecker.SINK_ANNOTATION));

		// the return value of analogRead is marked as a source of tainted data,
		// but at the moment you cannot specify annotations on code outside
		// of the analysis - annotating this function is equivalent
		cppProgram.getAllCFGs()
				.stream()
				.filter(cfg -> cfg.getDescriptor().getName().equals("readAnalog"))
				.map(CFG::getDescriptor)
				.forEach(descriptor -> descriptor.addAnnotation(Taint.TAINTED_ANNOTATION));

		// the map function is marked as a sanitizer of tainted data,
		// and thus its return type is always clean
		if (useSanitizer)
			cppProgram.getAllCFGs()
					.stream()
					.filter(cfg -> cfg.getDescriptor().getName().equals("map"))
					.map(CFG::getDescriptor)
					.forEach(descriptor -> descriptor.addAnnotation(Taint.CLEAN_ANNOTATION));
	}

	private static Program merge(Program p1, Program p2) {
		Program merged = new Program();

		p1.getCFGs().forEach(merged::addCFG);
		p1.getConstructs().forEach(merged::addConstruct);
		p1.getGlobals().forEach(merged::addGlobal);
		p1.getUnits().forEach(merged::addCompilationUnit);
		p1.getEntryPoints().forEach(merged::addEntryPoint);

		p2.getCFGs().forEach(merged::addCFG);
		p2.getConstructs().forEach(merged::addConstruct);
		p2.getGlobals().forEach(merged::addGlobal);
		p2.getUnits().forEach(merged::addCompilationUnit);
		p2.getEntryPoints().forEach(merged::addEntryPoint);

		return merged;
	}
}
