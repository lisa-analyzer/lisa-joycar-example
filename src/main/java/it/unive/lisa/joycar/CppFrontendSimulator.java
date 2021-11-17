package it.unive.lisa.joycar;

import java.util.HashMap;
import java.util.Map;

import it.unive.lisa.program.Global;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.annotations.Annotations;
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
import it.unive.lisa.program.cfg.statement.call.CFGCall;
import it.unive.lisa.program.cfg.statement.call.OpenCall;
import it.unive.lisa.program.cfg.statement.comparison.Equal;
import it.unive.lisa.program.cfg.statement.comparison.GreaterThan;
import it.unive.lisa.program.cfg.statement.comparison.LessThan;
import it.unive.lisa.program.cfg.statement.global.AccessGlobal;
import it.unive.lisa.program.cfg.statement.literal.Int32Literal;
import it.unive.lisa.program.cfg.statement.numeric.Addition;
import it.unive.lisa.program.cfg.statement.numeric.Division;
import it.unive.lisa.program.cfg.statement.numeric.Multiplication;
import it.unive.lisa.program.cfg.statement.numeric.Subtraction;
import it.unive.lisa.type.VoidType;
import it.unive.lisa.type.common.BoolType;
import it.unive.lisa.type.common.Int32;
import it.unive.lisa.type.common.Int64;

public class CppFrontendSimulator {

	private static final String CPP_SRC = "JoyCar.cpp";

	public static final Map<String, CFG> cfgs = new HashMap<>();

	public static void simulateCppParsing(Program program) {
		buildSignatures(program);
		
		buildDelay(program);
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

	private static void buildSignatures(Program program) {
		CFG cfg = new CFG(new CFGDescriptor(cppLoc(29, 6), program, false, "communicate", VoidType.INSTANCE,
				new Parameter(cppLoc(29, 22), "pin", Int32.INSTANCE),
				// the second parameter of softPwmWrite is marked as a sink for
				// tainted data,
				// but at the moment you cannot specify annotations on code
				// outside
				// of the analysis - annotating this parameter is equivalent
				new Parameter(cppLoc(29, 31), "value", Int32.INSTANCE, new Annotations(TaintChecker.SINK_ANNOTATION))));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(134, 24), program, false, "Java_JoyCar_runMotor", VoidType.INSTANCE,
				new Parameter(cppLoc(134, 66), "o", App.OBJECT_TYPE),
				new Parameter(cppLoc(134, 74), "val", Int32.INSTANCE)));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(127, 24), program, false, "Java_JoyCar_initializeMotor",
				VoidType.INSTANCE, new Parameter(cppLoc(127, 73), "o", App.OBJECT_TYPE)));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(112, 6), program, false, "motor", VoidType.INSTANCE,
				new Parameter(cppLoc(112, 16), "ADC", Int32.INSTANCE)));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(104, 24), program, false, "Java_JoyCar_turnLeft", VoidType.INSTANCE,
				new Parameter(cppLoc(104, 66), "o", App.OBJECT_TYPE)));
		program.addCFG(cfg);
		
		cfg = new CFG(new CFGDescriptor(cppLoc(96, 24), program, false, "Java_JoyCar_turnRight", VoidType.INSTANCE,
				new Parameter(cppLoc(92, 67), "o", App.OBJECT_TYPE)));
		program.addCFG(cfg);

		cfg = new CFG(
				new CFGDescriptor(cppLoc(33, 24), program, false, "Java_JoyCar_initializeWiringPi", Int32.INSTANCE,
						new Parameter(cppLoc(33, 76), "o", App.OBJECT_TYPE)));
		program.addCFG(cfg);

		cfg = new CFG(
				new CFGDescriptor(cppLoc(40, 24), program, false, "Java_JoyCar_initializePCF8591", VoidType.INSTANCE,
						new Parameter(cppLoc(40, 75), "o", App.OBJECT_TYPE)));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(46, 5), program, false, "readAnalog", Int32.INSTANCE,
				new Parameter(cppLoc(46, 20), "pin", Int32.INSTANCE)));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(50, 5), program, false, "readDigital", Int32.INSTANCE,
				new Parameter(cppLoc(50, 21), "pin", Int32.INSTANCE)));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(54, 24), program, false, "Java_JoyCar_readUpDown", Int32.INSTANCE,
				new Parameter(cppLoc(54, 68), "o", App.OBJECT_TYPE)));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(58, 24), program, false, "Java_JoyCar_readLeftRight", Int32.INSTANCE,
				new Parameter(cppLoc(58, 71), "o", App.OBJECT_TYPE)));
		program.addCFG(cfg);

		cfg = new CFG(
				new CFGDescriptor(cppLoc(62, 28), program, false, "Java_JoyCar_isButtonPressed", BoolType.INSTANCE,
						new Parameter(cppLoc(62, 77), "o", App.OBJECT_TYPE)));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(66, 6), program, false, "map", Int64.INSTANCE,
				new Parameter(cppLoc(66, 15), "value", Int64.INSTANCE),
				new Parameter(cppLoc(66, 27), "fromLow", Int64.INSTANCE),
				new Parameter(cppLoc(66, 41), "fromHigh", Int64.INSTANCE),
				new Parameter(cppLoc(66, 56), "toLow", Int64.INSTANCE),
				new Parameter(cppLoc(66, 68), "toHigh", Int64.INSTANCE)));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(70, 6), program, false, "servoInit", VoidType.INSTANCE,
				new Parameter(cppLoc(70, 20), "pin", Int32.INSTANCE)));
		program.addCFG(cfg);

		cfg = new CFG(
				new CFGDescriptor(cppLoc(75, 24), program, false, "Java_JoyCar_turnAtAngle", VoidType.INSTANCE,
						new Parameter(cppLoc(75, 70), "o", App.OBJECT_TYPE),
						new Parameter(cppLoc(75, 77), "angle", Int32.INSTANCE)));
		program.addCFG(cfg);

		cfg = new CFG(new CFGDescriptor(cppLoc(84, 6), program, false, "servoWriteMS", VoidType.INSTANCE,
				new Parameter(cppLoc(84, 23), "pin", Int32.INSTANCE),
				new Parameter(cppLoc(84, 32), "ms", Int32.INSTANCE)));
		program.addCFG(cfg);

		cfg = new CFG(
				new CFGDescriptor(cppLoc(92, 24), program, false, "Java_JoyCar_initializeServo", VoidType.INSTANCE,
						new Parameter(cppLoc(92, 73), "o", App.OBJECT_TYPE)));
		program.addCFG(cfg);
		
		cfg = new CFG(new CFGDescriptor(App.LIB_LOCATION, program, false, "delay", VoidType.INSTANCE,
				new Parameter(App.LIB_LOCATION, "amount", Int64.INSTANCE)));
		program.addCFG(cfg);
		
		for (CFG cg : program.getCFGs())
			cfgs.put(cg.getDescriptor().getName(), cg);
	}

	private static void buildDelay(Program program) {
		CFG cfg = cfgs.get("delay");
		Statement st = new Ret(cfg, App.LIB_LOCATION);
		cfg.addNode(st, true);
	}

	private static void buildCommunicate(Program program) {
		CFG cfg = cfgs.get("communicate");
		Statement first = new OpenCall(cfg, cppLoc(30, 5), "softPwmWrite", VoidType.INSTANCE,
				new VariableRef(cfg, cppLoc(30, 18), "pin", Int32.INSTANCE),
				new VariableRef(cfg, cppLoc(30, 23), "value", Int32.INSTANCE));
		cfg.addNode(first, true);

		Statement second = new Ret(cfg, cppLoc(31, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
	}

	private static void buildJava_JoyCar_initializeWiringPi(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_initializeWiringPi");

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
	}

	private static void buildJava_JoyCar_initializePCF8591(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_initializePCF8591");

		Statement first = new OpenCall(cfg, cppLoc(41, 5), "wiringPiSetup", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(41, 13), program, new Global(cppLoc(24, 9), "Z_Pin", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(41, 20), program, new Global(App.LIB_LOCATION, "INPUT", Int32.INSTANCE)));
		cfg.addNode(first, true);

		Statement second = new OpenCall(cfg, cppLoc(42, 5), "pullUpDnControl", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(42, 21), program, new Global(cppLoc(24, 9), "Z_Pin", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(42, 28), program, new Global(App.LIB_LOCATION, "PUD_UP", Int32.INSTANCE)));
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
	}

	private static void buildReadAnalog(Program program) {
		CFG cfg = cfgs.get("readAnalog");

		Statement first = new Return(cfg, cppLoc(47, 5),
				new OpenCall(cfg, cppLoc(47, 12), "analogRead", Int32.INSTANCE,
						new VariableRef(cfg, cppLoc(47, 23), "pin", Int32.INSTANCE)));
		cfg.addNode(first, true);

		// the return value of analogRead is marked as a source of tainted data,
		// but at the moment you cannot specify annotations on code outside
		// of the analysis - annotating this function is equivalent
		cfg.getDescriptor().addAnnotation(Taint.TAINTED_ANNOTATION);
	}

	private static void buildReadDigital(Program program) {
		CFG cfg = cfgs.get("readDigital");

		Statement first = new Return(cfg, cppLoc(51, 5),
				new OpenCall(cfg, cppLoc(51, 12), "digitalRead", Int32.INSTANCE,
						new VariableRef(cfg, cppLoc(51, 24), "pin", Int32.INSTANCE)));
		cfg.addNode(first, true);
	}

	private static void buildJava_JoyCar_readUpDown(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_readUpDown");

		Statement first = new Return(cfg, cppLoc(55, 5),
				new OpenCall(cfg, cppLoc(55, 12), "readAnalog", Int32.INSTANCE,
						new AccessGlobal(cfg, cppLoc(55, 23), program,
								new Global(cppLoc(13, 9), "A1", Int32.INSTANCE))));
		cfg.addNode(first, true);
	}

	private static void buildJava_JoyCar_readLeftRight(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_readLeftRight");

		Statement first = new Return(cfg, cppLoc(59, 5),
				new OpenCall(cfg, cppLoc(59, 12), "readAnalog", Int32.INSTANCE,
						new AccessGlobal(cfg, cppLoc(59, 23), program,
								new Global(cppLoc(12, 9), "A0", Int32.INSTANCE))));
		cfg.addNode(first, true);
	}

	private static void buildJava_JoyCar_isButtonPressed(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_isButtonPressed");

		Statement first = new Return(cfg, cppLoc(63, 5),
				new Equal(cfg, cppLoc(63, 31),
						new OpenCall(cfg, cppLoc(63, 12), "readDigital", Int32.INSTANCE,
								new AccessGlobal(cfg, cppLoc(63, 23), program,
										new Global(cppLoc(24, 9), "Z_Pin", Int32.INSTANCE))),
						new Int32Literal(cfg, cppLoc(63, 34), 0)));
		cfg.addNode(first, true);
	}

	private static void buildMap(Program program) {
		CFG cfg = cfgs.get("map");

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

		// the map function is marked as a sanitizer of tainted data,
		// and thus its return type is always clean
		if (App.useSanitizer)
			cfg.getDescriptor().addAnnotation(Taint.CLEAN_ANNOTATION);
	}

	private static void buildServoInit(Program program) {
		CFG cfg = cfgs.get("servoInit");

		Statement first = new OpenCall(cfg, cppLoc(71, 5), "softPwmCreate", VoidType.INSTANCE,
				new VariableRef(cfg, cppLoc(71, 18), "pin", Int32.INSTANCE),
				new Int32Literal(cfg, cppLoc(71, 24), 0),
				new Int32Literal(cfg, cppLoc(71, 27), 200));
		cfg.addNode(first, true);

		Statement second = new Ret(cfg, cppLoc(72, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
	}

	private static void buildJava_JoyCar_turnAtAngle(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_turnAtAngle");

		Statement condition = new GreaterThan(cfg, cppLoc(76, 15),
				new VariableRef(cfg, cppLoc(76, 9), "angle", Int32.INSTANCE),
				new Int32Literal(cfg, cppLoc(76, 17), 180));
		cfg.addNode(condition, true);

		Statement first = new Assignment(cfg, cppLoc(77, 15),
				new VariableRef(cfg, cppLoc(77, 9), "angle", Int32.INSTANCE),
				new Int32Literal(cfg, cppLoc(77, 17), 180));
		cfg.addNode(first);
		cfg.addEdge(new TrueEdge(condition, first));

		Statement second = new LessThan(cfg, cppLoc(78, 15),
				new VariableRef(cfg, cppLoc(78, 9), "angle", Int32.INSTANCE),
				new Int32Literal(cfg, cppLoc(78, 17), 0));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(condition, second));
		cfg.addEdge(new SequentialEdge(first, second));
		condition = second;

		first = new Assignment(cfg, cppLoc(79, 15),
				new VariableRef(cfg, cppLoc(79, 9), "angle", Int32.INSTANCE),
				new Int32Literal(cfg, cppLoc(79, 17), 0));
		cfg.addNode(first);
		cfg.addEdge(new TrueEdge(condition, first));

		second = new CFGCall(cfg, cppLoc(80, 5), program.getName() + ".communicate", cfgs.get("communicate"),
				new AccessGlobal(cfg, cppLoc(80, 17), program, new Global(cppLoc(25, 9), "servoPin", Int32.INSTANCE)),
				new CFGCall(cfg, cppLoc(80, 27), program.getName() + ".map", cfgs.get("map"),
						new VariableRef(cfg, cppLoc(80, 31), "angle", Int32.INSTANCE),
						new Int32Literal(cfg, cppLoc(80, 38), 0),
						new Int32Literal(cfg, cppLoc(80, 41), 180),
						new AccessGlobal(cfg, cppLoc(80, 46), program,
								new Global(cppLoc(21, 9), "SERVO_MIN_MS", Int32.INSTANCE)),
						new AccessGlobal(cfg, cppLoc(80, 60), program,
								new Global(cppLoc(22, 9), "SERVO_MAX_MS", Int32.INSTANCE))));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(condition, second));
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;

		second = new CFGCall(cfg, cppLoc(81, 5), program.getName() + ".delay", cfgs.get("delay"),
				new Int32Literal(cfg, cppLoc(81, 11), 100));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;

		second = new Ret(cfg, cppLoc(82, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
	}

	private static void buildServoWriteMS(Program program) {
		CFG cfg = cfgs.get("servoWriteMS");

		Statement condition = new GreaterThan(cfg, cppLoc(85, 12),
				new VariableRef(cfg, cppLoc(85, 9), "ms", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(85, 14), program,
						new Global(cppLoc(22, 9), "SERVO_MAX_MS", Int32.INSTANCE)));
		cfg.addNode(condition, true);

		Statement first = new Assignment(cfg, cppLoc(86, 12),
				new VariableRef(cfg, cppLoc(86, 9), "ms", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(86, 14), program,
						new Global(cppLoc(22, 9), "SERVO_MAX_MS", Int32.INSTANCE)));
		cfg.addNode(first);
		cfg.addEdge(new TrueEdge(condition, first));

		Statement second = new LessThan(cfg, cppLoc(87, 12),
				new VariableRef(cfg, cppLoc(87, 9), "ms", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(87, 14), program,
						new Global(cppLoc(21, 9), "SERVO_MIN_MS", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(condition, second));
		cfg.addEdge(new SequentialEdge(first, second));
		condition = second;

		first = new Assignment(cfg, cppLoc(88, 12),
				new VariableRef(cfg, cppLoc(88, 9), "ms", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(88, 14), program,
						new Global(cppLoc(21, 9), "SERVO_MIN_MS", Int32.INSTANCE)));
		cfg.addNode(first);
		cfg.addEdge(new TrueEdge(condition, first));

		second = new CFGCall(cfg, cppLoc(89, 5), program.getName() + ".communicate", cfgs.get("communicate"),
				new VariableRef(cfg, cppLoc(89, 17), "pin", Int32.INSTANCE),
				new VariableRef(cfg, cppLoc(89, 22), "ms", Int32.INSTANCE));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(condition, second));
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;

		second = new Ret(cfg, cppLoc(90, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
	}

	private static void buildJava_JoyCar_initializeServo(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_initializeServo");

		Statement first = new CFGCall(cfg, cppLoc(93, 5), program.getName() + ".servoInit", cfgs.get("servoInit"),
				new AccessGlobal(cfg, cppLoc(93, 15), program, new Global(cppLoc(25, 9), "servoPin", Int32.INSTANCE)));
		cfg.addNode(first, true);

		Statement second = new Ret(cfg, cppLoc(72, 0));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
	}

	private static void buildJava_JoyCar_turnRight(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_turnRight");

		Statement first = new Assignment(cfg, cppLoc(98, 12),
				new VariableRef(cfg, cppLoc(98, 10), "i", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(98, 14), program,
						new Global(cppLoc(21, 9), "SERVO_MIN_MS", Int32.INSTANCE)));
		cfg.addNode(first, true);

		Statement condition = new LessThan(cfg, cppLoc(98, 30),
				new VariableRef(cfg, cppLoc(98, 28), "i", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(98, 32), program,
						new Global(cppLoc(22, 9), "SERVO_MAX_MS", Int32.INSTANCE)));
		cfg.addNode(condition);
		cfg.addEdge(new SequentialEdge(first, condition));

		Statement second = new CFGCall(cfg, cppLoc(97, 9), program.getName() + ".servoWriteMS", cfgs.get("servoWriteMS"),
				new AccessGlobal(cfg, cppLoc(99, 22), program, new Global(cppLoc(25, 9), "servoPin", Int32.INSTANCE)),
				new VariableRef(cfg, cppLoc(99, 32), "i", Int32.INSTANCE));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(condition, second));
		first = second;

		second = new CFGCall(cfg, cppLoc(100, 9), program.getName() + ".delay", cfgs.get("delay"),
				new Int32Literal(cfg, cppLoc(100, 15), 100));
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
	}

	private static void buildJava_JoyCar_turnLeft(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_turnLeft");

		Statement first = new Assignment(cfg, cppLoc(106, 12),
				new VariableRef(cfg, cppLoc(106, 10), "i", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(106, 14), program,
						new Global(cppLoc(22, 9), "SERVO_MAX_MS", Int32.INSTANCE)));
		cfg.addNode(first, true);

		Statement condition = new GreaterThan(cfg, cppLoc(106, 30),
				new VariableRef(cfg, cppLoc(106, 28), "i", Int32.INSTANCE),
				new AccessGlobal(cfg, cppLoc(106, 32), program,
						new Global(cppLoc(21, 9), "SERVO_MIN_MS", Int32.INSTANCE)));
		cfg.addNode(condition);
		cfg.addEdge(new SequentialEdge(first, condition));

		Statement second = new CFGCall(cfg, cppLoc(107, 9), program.getName() + ".servoWriteMS", cfgs.get("servoWriteMS"),
				new AccessGlobal(cfg, cppLoc(107, 22), program, new Global(cppLoc(25, 9), "servoPin", Int32.INSTANCE)),
				new VariableRef(cfg, cppLoc(107, 32), "i", Int32.INSTANCE));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(condition, second));
		first = second;

		second = new CFGCall(cfg, cppLoc(108, 9), program.getName() + ".delay", cfgs.get("delay"),
				new Int32Literal(cfg, cppLoc(100, 15), 100));
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
	}

	private static void buildMotor(Program program) {
		CFG cfg = cfgs.get("motor");
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
				new AccessGlobal(cfg, cppLoc(115, 33), program, new Global(App.LIB_LOCATION, "HIGH", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new TrueEdge(condition, second));
		first = second;

		second = new OpenCall(cfg, cppLoc(116, 9), "digitalWrite", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(116, 22), program, new Global(cppLoc(17, 9), "motorPin2", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(116, 33), program, new Global(App.LIB_LOCATION, "LOW", Int32.INSTANCE)));
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
				new AccessGlobal(cfg, cppLoc(118, 33), program, new Global(App.LIB_LOCATION, "LOW", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new TrueEdge(condition, second));
		first = second;

		second = new OpenCall(cfg, cppLoc(119, 9), "digitalWrite", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(119, 22), program, new Global(cppLoc(17, 9), "motorPin2", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(119, 33), program, new Global(App.LIB_LOCATION, "HIGH", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;
		tmp2 = first;

		second = new OpenCall(cfg, cppLoc(121, 9), "digitalWrite", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(121, 22), program, new Global(cppLoc(16, 9), "motorPin1", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(121, 33), program, new Global(App.LIB_LOCATION, "LOW", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(condition, second));
		first = second;

		second = new OpenCall(cfg, cppLoc(122, 9), "digitalWrite", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(122, 22), program, new Global(cppLoc(17, 9), "motorPin2", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(122, 33), program, new Global(App.LIB_LOCATION, "LOW", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;

		second = new CFGCall(cfg, cppLoc(124, 5), program.getName() + ".communicate", cfgs.get("communicate"),
				new AccessGlobal(cfg, cppLoc(124, 17), program, new Global(cppLoc(18, 9), "enablePin", Int32.INSTANCE)),
				new CFGCall(cfg, cppLoc(124, 27), program.getName() + ".map", cfgs.get("map"),
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
	}

	private static void buildJava_JoyCar_initializeMotor(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_initializeMotor");

		Statement first = new OpenCall(cfg, cppLoc(128, 5), "pinMode", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(128, 13), program, new Global(cppLoc(18, 9), "enablePin", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(128, 24), program,
						new Global(App.LIB_LOCATION, "OUTPUT", Int32.INSTANCE)));
		cfg.addNode(first, true);

		Statement second = new OpenCall(cfg, cppLoc(129, 5), "pinMode", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(129, 13), program, new Global(cppLoc(16, 9), "motorPin1", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(129, 24), program,
						new Global(App.LIB_LOCATION, "OUTPUT", Int32.INSTANCE)));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;

		second = new OpenCall(cfg, cppLoc(130, 5), "pinMode", VoidType.INSTANCE,
				new AccessGlobal(cfg, cppLoc(130, 13), program, new Global(cppLoc(17, 9), "motorPin2", Int32.INSTANCE)),
				new AccessGlobal(cfg, cppLoc(130, 24), program,
						new Global(App.LIB_LOCATION, "OUTPUT", Int32.INSTANCE)));
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
	}

	private static void buildJava_JoyCar_runMotor(Program program) {
		CFG cfg = cfgs.get("Java_JoyCar_runMotor");

		Statement first = new CFGCall(cfg, cppLoc(135, 5), program.getName() + ".motor", cfgs.get("motor"),
				new VariableRef(cfg, cppLoc(135, 11), "val", Int32.INSTANCE));
		cfg.addNode(first, true);

		Statement second = new CFGCall(cfg, cppLoc(136, 5), program.getName() + ".delay", cfgs.get("delay"),
				new Int32Literal(cfg, cppLoc(136, 11), 100));
		cfg.addNode(second);
		cfg.addEdge(new SequentialEdge(first, second));
		first = second;

		second = new Ret(cfg, cppLoc(137, 0));
		cfg.addNode(second);
		cfg.addEdge(new FalseEdge(first, second));
	}

	private static SourceCodeLocation cppLoc(int line, int col) {
		return new SourceCodeLocation(CPP_SRC, line, col);
	}

}
