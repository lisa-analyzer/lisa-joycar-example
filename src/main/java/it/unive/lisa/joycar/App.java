package it.unive.lisa.joycar;

import java.io.IOException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAReport;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.checks.warnings.Warning;
import it.unive.lisa.conf.LiSAConfiguration;
import it.unive.lisa.interprocedural.ReturnTopPolicy;
import it.unive.lisa.interprocedural.callgraph.RTACallGraph;
import it.unive.lisa.interprocedural.context.ContextBasedAnalysis;
import it.unive.lisa.interprocedural.context.FullStackToken;
import it.unive.lisa.joycar.types.ArrayType;
import it.unive.lisa.joycar.types.ClassType;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.call.Call;
import it.unive.lisa.program.type.BoolType;
import it.unive.lisa.program.type.Int32Type;
import it.unive.lisa.program.type.Int64Type;
import it.unive.lisa.type.VoidType;

public class App {

	private static final Logger LOG = LogManager.getLogger(App.class);

	public static void main(String[] args) throws AnalysisException, IOException {
		boolean useSanitizer = args.length == 1 && args[0].equals("sanitize");
		mainImpl(useSanitizer);
	}

	public static LiSAReport mainImpl(boolean useSanitizer) throws IOException, AnalysisException {
		// need to parse java first, as it registers the jni types
		Program p1 = JavaFrontend.parse("original/JoyCar.java");
		Program p2 = CppFrontend.parse("original/JoyCar.cpp");
		addTaintAnnotations(p2, useSanitizer);

		p2.getTypes().registerType(Int32Type.INSTANCE);
		p2.getTypes().registerType(Int64Type.INSTANCE);
		p2.getTypes().registerType(BoolType.INSTANCE);
		p2.getTypes().registerType(VoidType.INSTANCE);
		ClassType.all().forEach(t -> p1.getTypes().registerType(t));
		ArrayType.all().forEach(t -> p1.getTypes().registerType(t));
		p1.getTypes().registerType(Int32Type.INSTANCE);
		p1.getTypes().registerType(Int64Type.INSTANCE);
		p1.getTypes().registerType(BoolType.INSTANCE);
		p1.getTypes().registerType(VoidType.INSTANCE);

		LiSAConfiguration conf = new LiSAConfiguration();
		conf.openCallPolicy = ReturnTopPolicy.INSTANCE;
		conf.hotspots = st -> st instanceof Expression && ((Expression) st).getParentStatement() instanceof Call;
		conf.interproceduralAnalysis = new ContextBasedAnalysis<>(FullStackToken.getSingleton());
		conf.callGraph = new RTACallGraph();
		conf.abstractState = new SimpleAbstractState<>(
				new MonolithicHeap(),
				new ValueEnvironment<>(new Taint()),
				new TypeEnvironment<>(new InferredTypes()));
		conf.semanticChecks.add(new TaintChecker());
		LiSA lisa = new LiSA(conf);
		LiSAReport report = lisa.run(p2, p1);

		if (report.getWarnings().isEmpty())
			LOG.info("The analysis generated no warnings");
		else {
			LOG.info("The analysis generated the following warnings:");
			for (Warning warn : report.getWarnings())
				LOG.info(warn);
		}
		return report;
	}

	private static void addTaintAnnotations(Program cppProgram, boolean useSanitizer) {
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
}
