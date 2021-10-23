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
import it.unive.lisa.program.Global;
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
import it.unive.lisa.program.cfg.statement.Return;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.call.OpenCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall.ResolutionStrategy;
import it.unive.lisa.program.cfg.statement.comparison.Equal;
import it.unive.lisa.program.cfg.statement.comparison.GreaterOrEqual;
import it.unive.lisa.program.cfg.statement.comparison.GreaterThan;
import it.unive.lisa.program.cfg.statement.comparison.LessOrEqual;
import it.unive.lisa.program.cfg.statement.comparison.LessThan;
import it.unive.lisa.program.cfg.statement.global.AccessGlobal;
import it.unive.lisa.program.cfg.statement.literal.Int32Literal;
import it.unive.lisa.program.cfg.statement.literal.TrueLiteral;
import it.unive.lisa.program.cfg.statement.numeric.Addition;
import it.unive.lisa.program.cfg.statement.numeric.Division;
import it.unive.lisa.program.cfg.statement.numeric.Multiplication;
import it.unive.lisa.program.cfg.statement.numeric.Subtraction;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.VoidType;
import it.unive.lisa.type.common.BoolType;
import it.unive.lisa.type.common.Int32;
import it.unive.lisa.type.common.Int64;

public class App {

	private static final String JAVA_SRC = "JoyCar.java";
	private static final String CPP_SRC = "JoyCar.cpp";
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
		SourceCodeLocation classLoc = javaLoc(3, 0);
		CompilationUnit jc = new CompilationUnit(classLoc, "JoyCar", false);
		jc.addSuperUnit(ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null).getUnit());
		program.addCompilationUnit(jc);
		ClassType jcType = ClassType.lookup("JoyCar", jc);

		CFG constructor = new CFG(new CFGDescriptor(classLoc, jc, true, "JoyCar", 
				VoidType.INSTANCE, new Parameter(classLoc, "this", jcType)));
		jc.addInstanceCFG(constructor);

		buildJavaMain(jc, jcType);
		buildNativeMethods(jc, jcType);
	}

	private static void buildJavaMain(CompilationUnit jc, ClassType jcType) {
		Type stringType = ClassType.lookup(ClassType.JAVA_LANG_STRING, null);
		ArrayType stringArrayType = ArrayType.lookup(stringType, 1);
		CFG main = new CFG(new CFGDescriptor(javaLoc(31, 4), jc, false, "main", VoidType.INSTANCE, 
				new Parameter(javaLoc(31, 38), "args", stringArrayType)));
		jc.addCFG(main);

		ResolutionStrategy javaStrat = ResolutionStrategy.FIRST_DYNAMIC_THEN_STATIC;
		
		// JoyCar rc = new JoyCar();
		Statement first = new Assignment(main, javaLoc(33, 19),
				new VariableRef(main, javaLoc(33, 16), "rc", jcType),
				new NewObj(main, javaLoc(33, 21), jcType));
		main.addNode(first, true);

		// if (rc.initializeWiringPi() == 0)
		Statement second = new Equal(main, javaLoc(34, 37),
				new UnresolvedCall(main, javaLoc(34, 15), javaStrat, true, "initializeWiringPi",
						new VariableRef(main, javaLoc(34, 13), "rc", jcType)),
				new Int32Literal(main, javaLoc(34, 40), 0));
		main.addNode(second);
		main.addEdge(new SequentialEdge(first, second));
		first = second;

		// return;
		second = new Ret(main, javaLoc(35, 13));
		main.addNode(second);
		main.addEdge(new TrueEdge(first, second));

		// rc.initializePCF8591();
		second = new UnresolvedCall(main, javaLoc(36, 11), javaStrat, true, "initializePCF8591", 
				new VariableRef(main, javaLoc(36, 9), "rc", jcType));
		main.addNode(second);
		main.addEdge(new FalseEdge(first, second));
		first = second;

		// rc.initializeServo();
		second = new UnresolvedCall(main, javaLoc(37, 11), javaStrat, true, "initializeServo", 
				new VariableRef(main, javaLoc(37, 9), "rc", jcType));
		main.addNode(second);
		main.addEdge(new FalseEdge(first, second));
		first = second;

		// rc.initializeMotor();
		second = new UnresolvedCall(main, javaLoc(38, 11), javaStrat, true, "initializeMotor",
				new VariableRef(main, javaLoc(38, 9), "rc", jcType));
		main.addNode(second);
		main.addEdge(new SequentialEdge(first, second));

		// while (true)
		Statement condition = new TrueLiteral(main, javaLoc(40, 16));
		main.addNode(condition);
		main.addEdge(new SequentialEdge(second, condition));

		// rc.runMotor(rc.readUpDown());
		first = new UnresolvedCall(main, javaLoc(41, 15), javaStrat, true, "runMotor",
				new VariableRef(main, javaLoc(41, 13), "rc", jcType),
				new UnresolvedCall(main, javaLoc(41, 27), javaStrat, true, "readUpDown",
						new VariableRef(main, javaLoc(41, 25), "rc", jcType)));
		main.addNode(first);
		main.addEdge(new TrueEdge(condition, first));
		
		// if (rc.readLeftRight() >= 200)
		second = new GreaterOrEqual(main, javaLoc(43, 36),
				new UnresolvedCall(main, javaLoc(43, 19), javaStrat, true, "readLeftRight",
						new VariableRef(main, javaLoc(43, 17), "rc", jcType)),
				new Int32Literal(main, javaLoc(43, 39), 200));
		main.addNode(second);
		main.addEdge(new SequentialEdge(first, second));
		first = second;

		// rc.turnRight();
		second = new UnresolvedCall(main, javaLoc(44, 19), javaStrat, true, "turnRight",
				new VariableRef(main, javaLoc(44, 17), "rc", jcType));
		main.addNode(second);
		main.addEdge(new TrueEdge(first, second));
		Statement inner = second;
		
		// else if (rc.readLeftRight() <= 100)
		second = new LessOrEqual(main, javaLoc(45, 41),
				new UnresolvedCall(main, javaLoc(45, 24), javaStrat, true, "readLeftRight",
						new VariableRef(main, javaLoc(45, 22), "rc", jcType)),
				new Int32Literal(main, javaLoc(45, 44), 100));
		main.addNode(second);
		main.addEdge(new FalseEdge(first, second));
		main.addEdge(new SequentialEdge(inner, second));
		first = second;
		
		// rc.turnLeft();
		second = new UnresolvedCall(main, javaLoc(46, 19), javaStrat, true, "turnLeft",
				new VariableRef(main, javaLoc(46, 17), "rc", jcType));
		main.addNode(second);
		main.addEdge(new TrueEdge(first, second));
		inner = second;
		
		// else if (rc.isButtonPressed())
		second = new UnresolvedCall(main, javaLoc(47, 24), javaStrat, true, "isButtonPressed",
				new VariableRef(main, javaLoc(47, 22), "rc", jcType));
		main.addNode(second);
		main.addEdge(new FalseEdge(first, second));
		main.addEdge(new SequentialEdge(inner, second));
		first = second;
		
		// rc.turnAtAngle(0);
		second = new UnresolvedCall(main, javaLoc(48, 19), javaStrat, true, "turnAtAngle",
				new VariableRef(main, javaLoc(48, 17), "rc", jcType),
				new Int32Literal(main, javaLoc(48, 32), 0));
		main.addNode(second);
		main.addEdge(new TrueEdge(first, second));
		main.addEdge(new FalseEdge(first, condition));
		main.addEdge(new SequentialEdge(second, condition));
		
		// instrumentation: return at the end of method
		first = new Ret(main, javaLoc(49, 10));
		main.addNode(first);
		main.addEdge(new FalseEdge(condition, first));
	}

	private static void buildNativeMethods(CompilationUnit jc, ClassType jcType) {
		SourceCodeLocation natLoc = javaLoc(4, 23);
		CFG nat = new CFG(new CFGDescriptor(natLoc, jc, true, "initializeWiringPi", Int32.INSTANCE,
				new Parameter(natLoc, "this", jcType)));
		Statement body = new Return(nat, natLoc, 
				new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_initializeWiringPi", 
				new VariableRef(nat, natLoc, "this", jcType)));
		nat.addNode(body, true);
		jc.addInstanceCFG(nat);
		
		natLoc = javaLoc(6, 24);
		nat = new CFG(new CFGDescriptor(natLoc, jc, true, "initializePCF8591", VoidType.INSTANCE,
				new Parameter(natLoc, "this", jcType)));
		body = new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_initializePCF8591", 
				new VariableRef(nat, natLoc, "this", jcType));
		nat.addNode(body, true);
		Statement ret = new Ret(nat, natLoc);
		nat.addNode(ret);
		nat.addEdge(new SequentialEdge(body, ret));
		jc.addInstanceCFG(nat);
		
		natLoc = javaLoc(8, 23);
		nat = new CFG(new CFGDescriptor(natLoc, jc, true, "readUpDown", Int32.INSTANCE,
				new Parameter(natLoc, "this", jcType)));
		body = new Return(nat, natLoc, 
				new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_readUpDown", 
				new VariableRef(nat, natLoc, "this", jcType)));
		nat.addNode(body, true);
		jc.addInstanceCFG(nat);
		
		natLoc = javaLoc(10, 23);
		nat = new CFG(new CFGDescriptor(natLoc, jc, true, "readLeftRight", Int32.INSTANCE,
				new Parameter(natLoc, "this", jcType)));
		body = new Return(nat, natLoc, 
				new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_readLeftRight", 
				new VariableRef(nat, natLoc, "this", jcType)));
		nat.addNode(body, true);
		jc.addInstanceCFG(nat);
		
		natLoc = javaLoc(12, 27);
		nat = new CFG(new CFGDescriptor(natLoc, jc, true, "isButtonPressed", BoolType.INSTANCE,
				new Parameter(natLoc, "this", jcType)));
		body = new Return(nat, natLoc, 
				new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_isButtonPressed", 
				new VariableRef(nat, natLoc, "this", jcType)));
		nat.addNode(body, true);
		ret = new Ret(nat, natLoc);
		nat.addNode(ret);
		nat.addEdge(new SequentialEdge(body, ret));
		jc.addInstanceCFG(nat);
		
		natLoc = javaLoc(14, 24);
		nat = new CFG(new CFGDescriptor(natLoc, jc, true, "initializeServo", VoidType.INSTANCE,
				new Parameter(natLoc, "this", jcType)));
		body = new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_initializeServo", 
				new VariableRef(nat, natLoc, "this", jcType));
		nat.addNode(body, true);
		ret = new Ret(nat, natLoc);
		nat.addNode(ret);
		nat.addEdge(new SequentialEdge(body, ret));
		jc.addInstanceCFG(nat);
		
		natLoc = javaLoc(16, 24);
		nat = new CFG(new CFGDescriptor(natLoc, jc, true, "turnRight", VoidType.INSTANCE,
				new Parameter(natLoc, "this", jcType)));
		body = new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_turnRight", 
				new VariableRef(nat, natLoc, "this", jcType));
		nat.addNode(body, true);
		ret = new Ret(nat, natLoc);
		nat.addNode(ret);
		nat.addEdge(new SequentialEdge(body, ret));
		jc.addInstanceCFG(nat);
		
		natLoc = javaLoc(18, 24);
		nat = new CFG(new CFGDescriptor(natLoc, jc, true, "turnLeft", VoidType.INSTANCE,
				new Parameter(natLoc, "this", jcType)));
		body = new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_turnLeft", 
				new VariableRef(nat, natLoc, "this", jcType));
		nat.addNode(body, true);
		ret = new Ret(nat, natLoc);
		nat.addNode(ret);
		nat.addEdge(new SequentialEdge(body, ret));
		jc.addInstanceCFG(nat);
		
		natLoc = javaLoc(20, 24);
		nat = new CFG(new CFGDescriptor(natLoc, jc, true, "turnAtAngle", VoidType.INSTANCE,
				new Parameter(natLoc, "this", jcType), new Parameter(natLoc, "angle", Int32.INSTANCE)));
		body = new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_turnAtAngle", 
				new VariableRef(nat, natLoc, "this", jcType), new VariableRef(nat, natLoc, "angle", Int32.INSTANCE));
		nat.addNode(body, true);
		ret = new Ret(nat, natLoc);
		nat.addNode(ret);
		nat.addEdge(new SequentialEdge(body, ret));
		jc.addInstanceCFG(nat);
		
		natLoc = javaLoc(22, 24);
		nat = new CFG(new CFGDescriptor(natLoc, jc, true, "initializeMotor", VoidType.INSTANCE,
				new Parameter(natLoc, "this", jcType)));
		body = new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_initializeMotor", 
				new VariableRef(nat, natLoc, "this", jcType));
		nat.addNode(body, true);
		ret = new Ret(nat, natLoc);
		nat.addNode(ret);
		nat.addEdge(new SequentialEdge(body, ret));
		jc.addInstanceCFG(nat);
		
		natLoc = javaLoc(24, 24);
		nat = new CFG(new CFGDescriptor(natLoc, jc, true, "runMotor", VoidType.INSTANCE,
				new Parameter(natLoc, "this", jcType), new Parameter(natLoc, "value", Int32.INSTANCE)));
		body = new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false, "Java_JoyCar_runMotor", 
				new VariableRef(nat, natLoc, "this", jcType), new VariableRef(nat, natLoc, "value", Int32.INSTANCE));
		nat.addNode(body, true);
		ret = new Ret(nat, natLoc);
		nat.addNode(ret);
		nat.addEdge(new SequentialEdge(body, ret));		
		jc.addInstanceCFG(nat);		
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
		 // this will automatically register it inside the types cache
		new StringType(string);
	}

	private static SourceCodeLocation javaLoc(int line, int col) {
		return new SourceCodeLocation(JAVA_SRC, line, col);
	}

	private static void buildCppCode(Program program) {
		buildCommunicate(program);
		buildJava_JoyCar_initializeWiringPi(program);
		buildJava_JoyCar_initializePCF8591(program);
		buildReadAnalog(program);
		buildReadDigital(program);
		buildJava_JoyCar_readUpDown(program);
		buildJava_JoyCar_readLeftRight(program);
		buildJava_JoyCar_isButtonPressed(program);
		buildMap(program);
		buildServoInit(program);
		buildJava_JoyCar_turnAtAngle(program);
		buildServoWriteMS(program);
		buildJava_JoyCar_initializeServo(program);
		buildJava_JoyCar_turnRight(program);
		buildJava_JoyCar_turnLeft(program);
		buildMotor(program);
		buildJava_JoyCar_initializeMotor(program);
		buildJava_JoyCar_runMotor(program);
	}

	private static void buildCommunicate(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(29, 6), program, false, "communicate", VoidType.INSTANCE, 
				new Parameter(cppLoc(29, 22), "pin", Int32.INSTANCE), new Parameter(cppLoc(29, 31), "value", Int32.INSTANCE)));
		
		Statement first = new OpenCall(cfg, cppLoc(30, 5), "softPwmWrite", VoidType.INSTANCE, 
				new VariableRef(cfg, cppLoc(30, 18), "pin", Int32.INSTANCE), new VariableRef(cfg, cppLoc(30, 23), "value", Int32.INSTANCE));
		cfg.addNode(first, true);
		
		Statement second = new Ret(cfg, cppLoc(31, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_initializeWiringPi(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(33, 24), program, false, "Java_JoyCar_initializeWiringPi", Int32.INSTANCE, 
				new Parameter(cppLoc(33, 76), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null))));
		
		Statement first = new Equal(cfg, cppLoc(34, 25),
				new OpenCall(cfg, cppLoc(34, 9), "wiringPiSetup", Int32.INSTANCE),
				new Int32Literal(cfg, cppLoc(34, 28), -1));
		cfg.addNode(first, true);
		
		Statement second = new Return(cfg, cppLoc(35, 9), new Int32Literal(cfg, cppLoc(35, 16), 0));
		cfg.addNode(second);
		cfg.addEdge(new TrueEdge(first, second));
		
		second = new Return(cfg, cppLoc(37, 9), new Int32Literal(cfg, cppLoc(37, 16), 1));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(first, second));
		
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_initializePCF8591(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(40, 24), program, false, "Java_JoyCar_initializePCF8591", VoidType.INSTANCE, 
				new Parameter(cppLoc(40, 75), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null))));
		
		Statement first = new OpenCall(cfg, cppLoc(41, 5), "wiringPiSetup", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(41, 13), program, new Global(cppLoc(24, 9), "Z_Pin", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(41, 20), program, new Global(LIB_LOCATION, "INPUT", Int32.INSTANCE)));
		cfg.addNode(first, true);
		
		Statement second = new OpenCall(cfg, cppLoc(42, 5), "pullUpDnControl", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(42, 21), program, new Global(cppLoc(24, 9), "Z_Pin", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(42, 28), program, new Global(LIB_LOCATION, "PUD_UP", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		
		second = new OpenCall(cfg, cppLoc(43, 5), "pcf8591Setup", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(43, 18), program, new Global(cppLoc(11, 9), "pinbase", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(43, 27), program, new Global(cppLoc(10, 9), "address", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		
		second = new Ret(cfg, cppLoc(44, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		
		program.addCFG(cfg);
	}

	private static void buildReadAnalog(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(46, 5), program, false, "readAnalog", Int32.INSTANCE, 
				new Parameter(cppLoc(46, 20), "pin", Int32.INSTANCE)));
		
		Statement first = new Return(cfg, cppLoc(47, 5),
				new OpenCall(cfg, cppLoc(47, 12), "analogRead", Int32.INSTANCE,
						new VariableRef(cfg, cppLoc(47, 23), "pin", Int32.INSTANCE)));
		cfg.addNode(first, true);
				
		program.addCFG(cfg);
	}

	private static void buildReadDigital(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(50, 5), program, false, "readDigital", Int32.INSTANCE, 
				new Parameter(cppLoc(50, 21), "pin", Int32.INSTANCE)));
		
		Statement first = new Return(cfg, cppLoc(51, 5),
				new OpenCall(cfg, cppLoc(51, 12), "digitalRead", Int32.INSTANCE,
						new VariableRef(cfg, cppLoc(51, 24), "pin", Int32.INSTANCE)));
		cfg.addNode(first, true);
				
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_readUpDown(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(54, 24), program, false, "Java_JoyCar_readUpDown", Int32.INSTANCE, 
				new Parameter(cppLoc(54, 68), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null))));
		
		Statement first = new Return(cfg, cppLoc(55, 5),
				new OpenCall(cfg, cppLoc(55, 12), "readAnalog", Int32.INSTANCE,
						new AccessGlobal(cfg, cppLoc(55, 23), program, new Global(cppLoc(13, 9), "A1", Int32.INSTANCE))));
		cfg.addNode(first, true);
				
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_readLeftRight(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(58, 24), program, false, "Java_JoyCar_readLeftRight", Int32.INSTANCE, 
				new Parameter(cppLoc(58, 71), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null))));
		
		Statement first = new Return(cfg, cppLoc(59, 5),
				new OpenCall(cfg, cppLoc(59, 12), "readAnalog", Int32.INSTANCE,
						new AccessGlobal(cfg, cppLoc(59, 23), program, new Global(cppLoc(12, 9), "A0", Int32.INSTANCE))));
		cfg.addNode(first, true);
				
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_isButtonPressed(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(62, 28), program, false, "Java_JoyCar_isButtonPressed", BoolType.INSTANCE, 
				new Parameter(cppLoc(62, 77), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null))));
		
		Statement first = new Return(cfg, cppLoc(63, 5), 
				new Equal(cfg, cppLoc(63, 31),
						new OpenCall(cfg, cppLoc(63, 12), "readDigital", Int32.INSTANCE,
								new AccessGlobal(cfg, cppLoc(63, 23), program, new Global(cppLoc(24, 9), "Z_Pin", Int32.INSTANCE))),
						new Int32Literal(cfg, cppLoc(63, 34), 0)));
		cfg.addNode(first, true);
				
		program.addCFG(cfg);
	}

	private static void buildMap(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(66, 6), program, false, "map", Int64.INSTANCE, 
				new Parameter(cppLoc(66, 15), "value", Int64.INSTANCE), 
				new Parameter(cppLoc(66, 27), "fromLow", Int64.INSTANCE), 
				new Parameter(cppLoc(66, 41), "fromHigh", Int64.INSTANCE), 
				new Parameter(cppLoc(66, 56), "toLow", Int64.INSTANCE), 
				new Parameter(cppLoc(66, 68), "toHigh", Int64.INSTANCE)));
		
		Statement first = new Assignment(cfg, cppLoc(67, 16), 
				new VariableRef(cfg, cppLoc(67, 9), "result", Int32.INSTANCE),
				new Addition(cfg, cppLoc(67, 78), 
						new Division(cfg, cppLoc(67, 55), 
								new Multiplication(cfg, cppLoc(67, 35), 
										new Subtraction(cfg, cppLoc(67, 26), 
												new VariableRef(cfg, cppLoc(67, 19), "toHigh", Int64.INSTANCE), 
												new VariableRef(cfg, cppLoc(67, 28), "toLow", Int64.INSTANCE)), 
										new Subtraction(cfg, cppLoc(67, 44), 
												new VariableRef(cfg, cppLoc(67, 38), "value", Int64.INSTANCE), 
												new VariableRef(cfg, cppLoc(67, 46), "fromLow", Int64.INSTANCE))), 
								new Subtraction(cfg, cppLoc(67, 67), 
										new VariableRef(cfg, cppLoc(67, 58), "fromHigh", Int64.INSTANCE), 
										new VariableRef(cfg, cppLoc(67, 69), "fromLow", Int64.INSTANCE))), 
						new VariableRef(cfg, cppLoc(67, 80), "toLow", Int64.INSTANCE)));
		cfg.addNode(first, true);
		
		Statement second = new Return(cfg, cppLoc(68, 5), 
				new VariableRef(cfg, cppLoc(68, 12), "result", Int32.INSTANCE));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
				
		program.addCFG(cfg);
	}

	private static void buildServoInit(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(70, 6), program, false, "servoInit", VoidType.INSTANCE, 
				new Parameter(cppLoc(70, 20), "pin", Int32.INSTANCE)));
		
		Statement first = new OpenCall(cfg, cppLoc(71, 5), "softPwmCreate", VoidType.INSTANCE, 
				new VariableRef(cfg, cppLoc(71, 18), "pin", Int32.INSTANCE), 
				new Int32Literal(cfg, cppLoc(71, 24), 0), 
				new Int32Literal(cfg, cppLoc(71, 27), 200));
		cfg.addNode(first, true);
		
		Statement second = new Ret(cfg, cppLoc(72, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_turnAtAngle(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(75, 24), program, false, "Java_JoyCar_turnAtAngle", VoidType.INSTANCE, 
				new Parameter(cppLoc(75, 70), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null)),
				new Parameter(cppLoc(75, 77), "angle", Int32.INSTANCE)));
		
		Statement first = new GreaterThan(cfg, cppLoc(76, 15),
				new VariableRef(cfg, cppLoc(76, 9), "angle", Int32.INSTANCE),
				new Int32Literal(cfg, cppLoc(76, 17), 180));
		cfg.addNode(first, true);
		
		Statement second = new Assignment(cfg, cppLoc(77, 15), 
				new VariableRef(cfg, cppLoc(77, 9), "angle", Int32.INSTANCE),
				new Int32Literal(cfg, cppLoc(77, 17), 180));
		cfg.addNode(second);
		cfg.addEdge(new TrueEdge(first, second));
		
		second = new LessThan(cfg, cppLoc(78, 15),
				new VariableRef(cfg, cppLoc(78, 9), "angle", Int32.INSTANCE),
				new Int32Literal(cfg, cppLoc(78, 17), 0));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(first, second));
		first = second;
		
		second = new Assignment(cfg, cppLoc(79, 15), 
				new VariableRef(cfg, cppLoc(79, 9), "angle", Int32.INSTANCE),
				new Int32Literal(cfg, cppLoc(79, 17), 0));
		cfg.addNode(second);
		cfg.addEdge(new TrueEdge(first, second));

		second = new UnresolvedCall(cfg, cppLoc(80, 5), ResolutionStrategy.STATIC_TYPES, false, "communicate",
				new AccessGlobal(cfg, cppLoc(80, 17), program, new Global(cppLoc(25, 9), "servoPin", Int32.INSTANCE)),
				new UnresolvedCall(cfg, cppLoc(80, 27), ResolutionStrategy.STATIC_TYPES, false, "map",
						new VariableRef(cfg, cppLoc(80, 31), "angle", Int32.INSTANCE),
						new Int32Literal(cfg, cppLoc(80, 38), 0),
						new Int32Literal(cfg, cppLoc(80, 41), 180),
						new AccessGlobal(cfg, cppLoc(80, 46), program, new Global(cppLoc(21, 9), "SERVO_MIN_MS", Int32.INSTANCE)),
						new AccessGlobal(cfg, cppLoc(80, 60), program, new Global(cppLoc(22, 9), "SERVO_MAX_MS", Int32.INSTANCE))));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(first, second));
		first = second;
		
		second = new OpenCall(cfg, cppLoc(81, 5), "delay", VoidType.INSTANCE, new Int32Literal(cfg, cppLoc(81, 11), 100));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		
		second = new Ret(cfg, cppLoc(82, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		
		program.addCFG(cfg);
	}

	private static void buildServoWriteMS(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(84, 6), program, false, "servoWriteMS", VoidType.INSTANCE, 
				new Parameter(cppLoc(84, 23), "pin", Int32.INSTANCE),
				new Parameter(cppLoc(84, 32), "ms", Int32.INSTANCE)));
		
		Statement first = new GreaterThan(cfg, cppLoc(85, 12),
				new VariableRef(cfg, cppLoc(85, 9), "ms", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(85, 14), program, new Global(cppLoc(22, 9), "SERVO_MAX_MS", Int32.INSTANCE)));
		cfg.addNode(first, true);
		
		Statement second = new Assignment(cfg, cppLoc(86, 12), 
				new VariableRef(cfg, cppLoc(86, 9), "ms", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(86, 14), program, new Global(cppLoc(22, 9), "SERVO_MAX_MS", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new TrueEdge(first, second));
		
		second = new LessThan(cfg, cppLoc(87, 12),
				new VariableRef(cfg, cppLoc(87, 9), "ms", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(87, 14), program, new Global(cppLoc(21, 9), "SERVO_MIN_MS", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(first, second));
		first = second;
		
		second = new Assignment(cfg, cppLoc(88, 12), 
				new VariableRef(cfg, cppLoc(88, 9), "ms", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(88, 14), program, new Global(cppLoc(21, 9), "SERVO_MIN_MS", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new TrueEdge(first, second));

		second = new UnresolvedCall(cfg, cppLoc(89, 5), ResolutionStrategy.STATIC_TYPES, false, "communicate",
				new VariableRef(cfg, cppLoc(89, 17), "pin", Int32.INSTANCE),
				new VariableRef(cfg, cppLoc(89, 22), "ms", Int32.INSTANCE));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(first, second));
		first = second;
		
		second = new Ret(cfg, cppLoc(90, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_initializeServo(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(92, 24), program, false, "Java_JoyCar_initializeServo", VoidType.INSTANCE, 
				new Parameter(cppLoc(92, 73), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null))));
		
		Statement first = new UnresolvedCall(cfg, cppLoc(93, 5), ResolutionStrategy.STATIC_TYPES, false, "servoInit", 
				new AccessGlobal(cfg, cppLoc(93, 15), program, new Global(cppLoc(25, 9), "servoPin", Int32.INSTANCE)));
		cfg.addNode(first, true);
		
		Statement second = new Ret(cfg, cppLoc(72, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_turnRight(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(96, 24), program, false, "Java_JoyCar_turnRight", VoidType.INSTANCE, 
				new Parameter(cppLoc(92, 67), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null))));
		
		Statement first = new Assignment(cfg, cppLoc(98, 12), 
				new VariableRef(cfg, cppLoc(98, 10), "i", Int32.INSTANCE), 
				new AccessGlobal(cfg, cppLoc(98, 14), program, new Global(cppLoc(21, 9), "SERVO_MIN_MS", Int32.INSTANCE)));
		cfg.addNode(first, true);
		
		Statement condition = new LessThan(cfg, cppLoc(98, 30), 
				new VariableRef(cfg, cppLoc(98, 28), "i", Int32.INSTANCE), 
				new AccessGlobal(cfg, cppLoc(98, 32), program, new Global(cppLoc(22, 9), "SERVO_MAX_MS", Int32.INSTANCE)));
		cfg.addNode(condition);
		cfg.addEdge(new SequentialEdge(first, condition));

		Statement second = new UnresolvedCall(cfg, cppLoc(97, 9), ResolutionStrategy.STATIC_TYPES, false, "servoWriteMS", 
						new AccessGlobal(cfg, cppLoc(99, 22), program, new Global(cppLoc(25, 9), "servoPin", Int32.INSTANCE)),
						new VariableRef(cfg, cppLoc(99, 32), "i", Int32.INSTANCE));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(condition, second));
		first = second;
		
		second = new OpenCall(cfg, cppLoc(100, 9), "delay", VoidType.INSTANCE, new Int32Literal(cfg, cppLoc(100, 15), 100));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		
		second = new Assignment(cfg, cppLoc(98, 47), 
				new VariableRef(cfg, cppLoc(98, 46), "i", Int32.INSTANCE),
				new Addition(cfg, cppLoc(98, 48), 
						new VariableRef(cfg, cppLoc(98, 49), "i", Int32.INSTANCE), 
						new Int32Literal(cfg, cppLoc(98, 50), 1)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		cfg.addEdge(new SequentialEdge(second, condition));
		
		
		second = new Ret(cfg, cppLoc(102, 0));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(condition, second));
		
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_turnLeft(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(104, 24), program, false, "Java_JoyCar_turnLeft", VoidType.INSTANCE, 
				new Parameter(cppLoc(104, 66), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null))));
		
		Statement first = new Assignment(cfg, cppLoc(106, 12), 
				new VariableRef(cfg, cppLoc(106, 10), "i", Int32.INSTANCE), 
				new AccessGlobal(cfg, cppLoc(106, 14), program, new Global(cppLoc(22, 9), "SERVO_MAX_MS", Int32.INSTANCE)));
		cfg.addNode(first, true);
		
		Statement condition = new GreaterThan(cfg, cppLoc(106, 30), 
				new VariableRef(cfg, cppLoc(106, 28), "i", Int32.INSTANCE), 
				new AccessGlobal(cfg, cppLoc(106, 32), program, new Global(cppLoc(21, 9), "SERVO_MIN_MS", Int32.INSTANCE)));
		cfg.addNode(condition);
		cfg.addEdge(new SequentialEdge(first, condition));

		Statement second = new UnresolvedCall(cfg, cppLoc(107, 9), ResolutionStrategy.STATIC_TYPES, false, "servoWriteMS", 
						new AccessGlobal(cfg, cppLoc(107, 22), program, new Global(cppLoc(25, 9), "servoPin", Int32.INSTANCE)),
						new VariableRef(cfg, cppLoc(107, 32), "i", Int32.INSTANCE));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(condition, second));
		first = second;
		
		second = new OpenCall(cfg, cppLoc(108, 9), "delay", VoidType.INSTANCE, new Int32Literal(cfg, cppLoc(100, 15), 100));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		
		second = new Assignment(cfg, cppLoc(106, 47), 
				new VariableRef(cfg, cppLoc(106, 46), "i", Int32.INSTANCE),
				new Subtraction(cfg, cppLoc(106, 48), 
						new VariableRef(cfg, cppLoc(106, 49), "i", Int32.INSTANCE), 
						new Int32Literal(cfg, cppLoc(106, 50), 1)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		cfg.addEdge(new SequentialEdge(second, condition));
		
		
		second = new Ret(cfg, cppLoc(110, 0));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(condition, second));
		
		program.addCFG(cfg);
	}

	private static void buildMotor(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(112, 6), program, false, "motor", VoidType.INSTANCE, 
				new Parameter(cppLoc(112, 16), "ADC", Int32.INSTANCE)));
		Statement tmp1, tmp2;
		
		Statement first = new Assignment(cfg, cppLoc(113, 15),
				new VariableRef(cfg, cppLoc(113, 9), "value", Int32.INSTANCE), 
				new Subtraction(cfg, cppLoc(113, 21), 
						new VariableRef(cfg, cppLoc(113, 17), "ADC", Int32.INSTANCE),
						new Int32Literal(cfg, cppLoc(113, 23), 130)));
		cfg.addNode(first, true);
		
		Statement condition = new GreaterThan(cfg, cppLoc(114, 15), 
				new VariableRef(cfg, cppLoc(114, 9), "value", Int32.INSTANCE), 
				new Int32Literal(cfg, cppLoc(114, 17), 0));
		cfg.addNode(condition);
		cfg.addEdge(new SequentialEdge(first, condition));

		Statement second = new OpenCall(cfg, cppLoc(115, 9), "digitalWrite", VoidType.INSTANCE, 
				new AccessGlobal(cfg, cppLoc(115, 22), program, new Global(cppLoc(16, 9), "motorPin1", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(115, 33), program, new Global(LIB_LOCATION, "HIGH", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new TrueEdge(condition, second));
		first = second;
		
		second = new OpenCall(cfg, cppLoc(116, 9), "digitalWrite", VoidType.INSTANCE, 
				new AccessGlobal(cfg, cppLoc(116, 22), program, new Global(cppLoc(17, 9), "motorPin2", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(116, 33), program, new Global(LIB_LOCATION, "LOW", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		tmp1 = first;
		
		second = new LessThan(cfg, cppLoc(117, 22), 
				new VariableRef(cfg, cppLoc(114, 16), "value", Int32.INSTANCE), 
				new Int32Literal(cfg, cppLoc(114, 24), 0));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(condition, second));
		condition = second;

		second = new OpenCall(cfg, cppLoc(118, 9), "digitalWrite", VoidType.INSTANCE, 
				new AccessGlobal(cfg, cppLoc(118, 22), program, new Global(cppLoc(16, 9), "motorPin1", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(118, 33), program, new Global(LIB_LOCATION, "LOW", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new TrueEdge(condition, second));
		first = second;
		
		second = new OpenCall(cfg, cppLoc(119, 9), "digitalWrite", VoidType.INSTANCE, 
				new AccessGlobal(cfg, cppLoc(119, 22), program, new Global(cppLoc(17, 9), "motorPin2", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(119, 33), program, new Global(LIB_LOCATION, "HIGH", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		tmp2 = first;
		
		second = new OpenCall(cfg, cppLoc(121, 9), "digitalWrite", VoidType.INSTANCE, 
				new AccessGlobal(cfg, cppLoc(121, 22), program, new Global(cppLoc(16, 9), "motorPin1", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(121, 33), program, new Global(LIB_LOCATION, "LOW", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(condition, second));
		first = second;
		
		second = new OpenCall(cfg, cppLoc(122, 9), "digitalWrite", VoidType.INSTANCE, 
				new AccessGlobal(cfg, cppLoc(122, 22), program, new Global(cppLoc(17, 9), "motorPin2", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(122, 33), program, new Global(LIB_LOCATION, "LOW", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		
		second = new UnresolvedCall(cfg, cppLoc(124, 5), ResolutionStrategy.STATIC_TYPES, false, "communicate",
				new AccessGlobal(cfg, cppLoc(124, 17), program, new Global(cppLoc(18, 9), "enablePin", Int32.INSTANCE)),
				new UnresolvedCall(cfg, cppLoc(124, 27), ResolutionStrategy.STATIC_TYPES, false, "map",
						new OpenCall(cfg, cppLoc(124, 32), "abs", Int32.INSTANCE, 
								new VariableRef(cfg, cppLoc(124, 36), "value", Int32.INSTANCE)),
						new Int32Literal(cfg, cppLoc(124, 44), 0),
						new Int32Literal(cfg, cppLoc(124, 47), 130),
						new Int32Literal(cfg, cppLoc(124, 52), 0),
						new Int32Literal(cfg, cppLoc(124, 55), 255)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(tmp1, second)); // from if branch
		cfg.addEdge(new SequentialEdge(tmp2, second)); // from else-if branch
		cfg.addEdge(new SequentialEdge(first, second)); // from else branch
		first = second;
		
		second = new Ret(cfg, cppLoc(125, 0));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(first, second));
		
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_initializeMotor(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(127, 24), program, false, "Java_JoyCar_initializeMotor", VoidType.INSTANCE, 
				new Parameter(cppLoc(127, 73), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null))));
		
		Statement first = new OpenCall(cfg, cppLoc(128, 5), "pinMode", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(128, 13), program, new Global(cppLoc(18, 9), "enablePin", Int32.INSTANCE)), 
				new AccessGlobal(cfg, cppLoc(128, 24), program, new Global(LIB_LOCATION, "OUTPUT", Int32.INSTANCE)));
		cfg.addNode(first, true);
		
		Statement second = new OpenCall(cfg, cppLoc(129, 5), "pinMode", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(129, 13), program, new Global(cppLoc(16, 9), "motorPin1", Int32.INSTANCE)), 
				new AccessGlobal(cfg, cppLoc(129, 24), program, new Global(LIB_LOCATION, "OUTPUT", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		
		second = new OpenCall(cfg, cppLoc(130, 5), "pinMode", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(130, 13), program, new Global(cppLoc(17, 9), "motorPin2", Int32.INSTANCE)), 
				new AccessGlobal(cfg, cppLoc(130, 24), program, new Global(LIB_LOCATION, "OUTPUT", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		
		second = new OpenCall(cfg, cppLoc(131, 5), "softPwmCreate", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(131, 19), program, new Global(cppLoc(18, 9), "enablePin", Int32.INSTANCE)), 
				new Int32Literal(cfg, cppLoc(131, 30), 0),
				new Int32Literal(cfg, cppLoc(131, 33), 100));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		
		second = new Ret(cfg, cppLoc(132, 0));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(first, second));
		
		program.addCFG(cfg);
	}

	private static void buildJava_JoyCar_runMotor(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(134, 24), program, false, "Java_JoyCar_runMotor", VoidType.INSTANCE, 
				new Parameter(cppLoc(134, 66), "o", ClassType.lookup(ClassType.JAVA_LANG_OBJECT, null)),
				new Parameter(cppLoc(134, 74), "val", Int32.INSTANCE)));
		
		Statement first = new UnresolvedCall(cfg, cppLoc(135, 5), ResolutionStrategy.STATIC_TYPES, false, "motor",
				new VariableRef(cfg, cppLoc(135, 11), "val", Int32.INSTANCE));
		cfg.addNode(first, true);
		
		Statement second = new OpenCall(cfg, cppLoc(136, 5), "delay", VoidType.INSTANCE, new Int32Literal(cfg, cppLoc(136, 11), 100));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		
		second = new Ret(cfg, cppLoc(137, 0));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(first, second));
		
		program.addCFG(cfg);
	}

	private static SourceCodeLocation cppLoc(int line, int col) {
		return new SourceCodeLocation(CPP_SRC, line, col);
	}
}
