package it.lucaneg.lisa.joycar;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import it.lucaneg.lisa.joycar.java.statements.NewObj;
import it.lucaneg.lisa.joycar.java.types.ArrayType;
import it.lucaneg.lisa.joycar.java.types.ClassType;
import it.lucaneg.lisa.joycar.java.types.StringType;
import it.unive.lisa.AnalysisException;
import it.unive.lisa.LiSA;
import it.unive.lisa.LiSAConfiguration;
import it.unive.lisa.program.CompilationUnit;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CFGDescriptor;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.Parameter;
import it.unive.lisa.program.cfg.edge.FalseEdge;
import it.unive.lisa.program.cfg.edge.SequentialEdge;
import it.unive.lisa.program.cfg.edge.TrueEdge;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Ret;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall.ResolutionStrategy;
import it.unive.lisa.program.cfg.statement.comparison.Equal;
import it.unive.lisa.program.cfg.statement.comparison.GreaterOrEqual;
import it.unive.lisa.program.cfg.statement.comparison.LessOrEqual;
import it.unive.lisa.program.cfg.statement.literal.Int32Literal;
import it.unive.lisa.program.cfg.statement.literal.TrueLiteral;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.VoidType;
import it.unive.lisa.type.common.BoolType;
import it.unive.lisa.type.common.Int32;

public class App {

	private static final String JAVA_SRC = "JoyCar.java";
	private static final Logger LOG = LogManager.getLogger(App.class);

	private static final CodeLocation LIB_LOCATION = new CodeLocation() {
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

		buildJavaCode(program);
		buildCppCode(program);

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

	private static void buildJavaCode(Program program) {
		buildObject(program);
		buildString(program);
		SourceCodeLocation classLoc = mkJavaLoc(3, 0);
		CompilationUnit jc = new CompilationUnit(classLoc, "JoyCar", false);
		jc.addSuperUnit(ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null).getUnit());
		program.addCompilationUnit(jc);
		ClassType jcType = ClassType.lookup("JoyCar", jc);

		CFG constructor = new CFG(new CFGDescriptor(classLoc, jc, true, "JoyCar", 
				VoidType.INSTANCE, new Parameter[] { new Parameter(classLoc, "this", jcType) }));
		jc.addInstanceCFG(constructor);

		Type stringType = ClassType.lookup(ClassType.JAVA_LANG_STRING, null);
		ArrayType stringArrayType = ArrayType.lookup(stringType, 1);
		Parameter param = new Parameter(mkJavaLoc(31, 38), "args", stringArrayType);
		CFG main = new CFG(new CFGDescriptor(mkJavaLoc(31, 4), jc, false, "main", 
				VoidType.INSTANCE, new Parameter[] { param }));
		jc.addCFG(main);

		ResolutionStrategy javaStrat = ResolutionStrategy.FIRST_DYNAMIC_THEN_STATIC;
		
		// JoyCar rc = new JoyCar();
		Statement first = new Assignment(main, mkJavaLoc(33, 19),
				new VariableRef(main, mkJavaLoc(33, 16), "rc", jcType),
				new NewObj(main, mkJavaLoc(33, 21), jcType));
		main.addNode(first, true);

		// if (rc.initializeWiringPi() == 0)
		Statement second = new Equal(main, mkJavaLoc(34, 37),
				new UnresolvedCall(main, mkJavaLoc(34, 15), javaStrat, true, "initializeWiringPi",
						new VariableRef(main, mkJavaLoc(34, 13), "rc", jcType)),
				new Int32Literal(main, mkJavaLoc(34, 40), 0));
		main.addNode(second);
		main.addEdge(new SequentialEdge(first, second));
		first = second;

		// return;
		second = new Ret(main, mkJavaLoc(35, 13));
		main.addNode(second);
		main.addEdge(new TrueEdge(first, second));

		// rc.initializePCF8591();
		second = new UnresolvedCall(main, mkJavaLoc(36, 11), javaStrat, true, "initializePCF8591", 
				new VariableRef(main, mkJavaLoc(36, 9), "rc", jcType));
		main.addNode(second);
		main.addEdge(new FalseEdge(first, second));
		first = second;

		// rc.initializeServo();
		second = new UnresolvedCall(main, mkJavaLoc(37, 11), javaStrat, true, "initializeServo", 
				new VariableRef(main, mkJavaLoc(37, 9), "rc", jcType));
		main.addNode(second);
		main.addEdge(new FalseEdge(first, second));
		first = second;

		// rc.initializeMotor();
		second = new UnresolvedCall(main, mkJavaLoc(38, 11), javaStrat, true, "initializeMotor",
				new VariableRef(main, mkJavaLoc(38, 9), "rc", jcType));
		main.addNode(second);
		main.addEdge(new SequentialEdge(first, second));

		// while (true)
		Statement condition = new TrueLiteral(main, mkJavaLoc(40, 16));
		main.addNode(condition);
		main.addEdge(new SequentialEdge(second, condition));

		// rc.runMotor(rc.readUpDown());
		first = new UnresolvedCall(main, mkJavaLoc(41, 15), javaStrat, true, "runMotor",
				new VariableRef(main, mkJavaLoc(41, 13), "rc", jcType),
				new UnresolvedCall(main, mkJavaLoc(41, 27), javaStrat, true, "readUpDown",
						new VariableRef(main, mkJavaLoc(41, 25), "rc", jcType)));
		main.addNode(first);
		main.addEdge(new TrueEdge(condition, first));
		
		// if (rc.readLeftRight() >= 200)
		second = new GreaterOrEqual(main, mkJavaLoc(43, 36),
				new UnresolvedCall(main, mkJavaLoc(43, 19), javaStrat, true, "readLeftRight",
						new VariableRef(main, mkJavaLoc(43, 17), "rc", jcType)),
				new Int32Literal(main, mkJavaLoc(43, 39), 200));
		main.addNode(second);
		main.addEdge(new SequentialEdge(first, second));
		first = second;

		// rc.turnRight();
		second = new UnresolvedCall(main, mkJavaLoc(44, 19), javaStrat, true, "turnRight",
				new VariableRef(main, mkJavaLoc(44, 17), "rc", jcType));
		main.addNode(second);
		main.addEdge(new TrueEdge(first, second));
		Statement inner = second;
		
		// else if (rc.readLeftRight() <= 100)
		second = new LessOrEqual(main, mkJavaLoc(45, 41),
				new UnresolvedCall(main, mkJavaLoc(45, 24), javaStrat, true, "readLeftRight",
						new VariableRef(main, mkJavaLoc(45, 22), "rc", jcType)),
				new Int32Literal(main, mkJavaLoc(45, 44), 100));
		main.addNode(second);
		main.addEdge(new FalseEdge(first, second));
		main.addEdge(new SequentialEdge(inner, second));
		first = second;
		
		// rc.turnLeft();
		second = new UnresolvedCall(main, mkJavaLoc(46, 19), javaStrat, true, "turnLeft",
				new VariableRef(main, mkJavaLoc(46, 17), "rc", jcType));
		main.addNode(second);
		main.addEdge(new TrueEdge(first, second));
		inner = second;
		
		// else if (rc.isButtonPressed())
		second = new UnresolvedCall(main, mkJavaLoc(47, 24), javaStrat, true, "isButtonPressed",
				new VariableRef(main, mkJavaLoc(47, 22), "rc", jcType));
		main.addNode(second);
		main.addEdge(new FalseEdge(first, second));
		main.addEdge(new SequentialEdge(inner, second));
		first = second;
		
		// rc.turnAtAngle(0);
		second = new UnresolvedCall(main, mkJavaLoc(48, 19), javaStrat, true, "turnAtAngle",
				new VariableRef(main, mkJavaLoc(48, 17), "rc", jcType),
				new Int32Literal(main, mkJavaLoc(48, 32), 0));
		main.addNode(second);
		main.addEdge(new TrueEdge(first, second));
		main.addEdge(new FalseEdge(first, condition));
		main.addEdge(new SequentialEdge(second, condition));
		
		// instrumentation: return at the end of method
		first = new Ret(main, mkJavaLoc(49, 10));
		main.addNode(first);
		main.addEdge(new FalseEdge(condition, first));
	}

	private static void buildObject(Program program) {
		CompilationUnit object = new CompilationUnit(LIB_LOCATION, ClassType.JAVA_LANG_OBJECT, false);
		program.addCompilationUnit(object);
		ClassType.lookup(ClassType.JAVA_LANG_OBJECT, object);
	}

	private static void buildString(Program program) {
		CompilationUnit string = new CompilationUnit(LIB_LOCATION, ClassType.JAVA_LANG_STRING, false);
		string.addSuperUnit(ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null).getUnit());
		program.addCompilationUnit(string);
		new StringType(string); // this will automatically register it inside
								// the types cache
	}

	private static SourceCodeLocation mkJavaLoc(int line, int col) {
		return new SourceCodeLocation(JAVA_SRC, line, col);
	}

	private static void buildCppCode(Program program) {
		// TODO Auto-generated method stub
	}
}
