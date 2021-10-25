package it.lucaneg.lisa.joycar;

import it.lucaneg.lisa.joycar.java.statements.NewObj;
import it.lucaneg.lisa.joycar.java.types.ArrayType;
import it.lucaneg.lisa.joycar.java.types.ClassType;
import it.lucaneg.lisa.joycar.java.types.StringType;
import it.unive.lisa.program.CompilationUnit;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CFGDescriptor;
import it.unive.lisa.program.cfg.Parameter;
import it.unive.lisa.program.cfg.edge.FalseEdge;
import it.unive.lisa.program.cfg.edge.SequentialEdge;
import it.unive.lisa.program.cfg.edge.TrueEdge;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Ret;
import it.unive.lisa.program.cfg.statement.Return;
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

public class JavaFrontendSimulator {

	private static final String JAVA_SRC = "JoyCar.java";
	
	public static ClassType OBJECT_TYPE;
	public static ClassType STRING_TYPE;

	public static void simulateJavaParsing(Program program) {
		buildObject(program);
		buildString(program);
		SourceCodeLocation classLoc = javaLoc(3, 0);
		CompilationUnit jc = new CompilationUnit(classLoc, "JoyCar", false);
		jc.addSuperUnit(OBJECT_TYPE.getUnit());
		program.addCompilationUnit(jc);
		ClassType jcType = ClassType.lookup("JoyCar", jc);

		CFG constructor = new CFG(new CFGDescriptor(classLoc, jc, true, "JoyCar",
				VoidType.INSTANCE, new Parameter(classLoc, "this", jcType)));
		constructor.addNode(new Ret(constructor, classLoc), true);
		jc.addInstanceCFG(constructor);

		buildJavaMain(jc, jcType);
		buildNativeMethods(jc, jcType);
	}

	private static void buildJavaMain(CompilationUnit jc, ClassType jcType) {
		Type stringType = STRING_TYPE;
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
				new UnresolvedCall(nat, natLoc, ResolutionStrategy.STATIC_TYPES, false,
						"Java_JoyCar_initializeWiringPi",
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

	private static SourceCodeLocation javaLoc(int line, int col) {
		return new SourceCodeLocation(JAVA_SRC, line, col);
	}
}
