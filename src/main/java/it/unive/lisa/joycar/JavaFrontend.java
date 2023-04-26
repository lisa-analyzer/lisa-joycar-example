package it.unive.lisa.joycar;

import static it.unive.lisa.joycar.ANTLRUtils.fromContext;
import static it.unive.lisa.joycar.ANTLRUtils.fromSingle;
import static it.unive.lisa.joycar.ANTLRUtils.fromToken;
import static it.unive.lisa.joycar.ANTLRUtils.notSupported;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.apache.commons.lang3.tuple.Triple;

import it.unive.lisa.joycar.antlr.Java8Lexer;
import it.unive.lisa.joycar.antlr.Java8Parser;
import it.unive.lisa.joycar.antlr.Java8Parser.AdditiveExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.AmbiguousNameContext;
import it.unive.lisa.joycar.antlr.Java8Parser.AndExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ArgumentListContext;
import it.unive.lisa.joycar.antlr.Java8Parser.AssignmentExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.BlockContext;
import it.unive.lisa.joycar.antlr.Java8Parser.BlockStatementContext;
import it.unive.lisa.joycar.antlr.Java8Parser.BlockStatementsContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ClassBodyDeclarationContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ClassDeclarationContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ClassInstanceCreationExpression_lfno_primaryContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ClassMemberDeclarationContext;
import it.unive.lisa.joycar.antlr.Java8Parser.CompilationUnitContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ConditionalAndExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ConditionalExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ConditionalOrExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.EqualityExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ExclusiveOrExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ExpressionNameContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ExpressionStatementContext;
import it.unive.lisa.joycar.antlr.Java8Parser.FormalParameterContext;
import it.unive.lisa.joycar.antlr.Java8Parser.FormalParameterListContext;
import it.unive.lisa.joycar.antlr.Java8Parser.FormalParametersContext;
import it.unive.lisa.joycar.antlr.Java8Parser.IfThenElseStatementContext;
import it.unive.lisa.joycar.antlr.Java8Parser.IfThenStatementContext;
import it.unive.lisa.joycar.antlr.Java8Parser.InclusiveOrExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.IntegralTypeContext;
import it.unive.lisa.joycar.antlr.Java8Parser.LastFormalParameterContext;
import it.unive.lisa.joycar.antlr.Java8Parser.LiteralContext;
import it.unive.lisa.joycar.antlr.Java8Parser.LocalVariableDeclarationContext;
import it.unive.lisa.joycar.antlr.Java8Parser.LocalVariableDeclarationStatementContext;
import it.unive.lisa.joycar.antlr.Java8Parser.MethodBodyContext;
import it.unive.lisa.joycar.antlr.Java8Parser.MethodDeclarationContext;
import it.unive.lisa.joycar.antlr.Java8Parser.MethodHeaderContext;
import it.unive.lisa.joycar.antlr.Java8Parser.MethodInvocationContext;
import it.unive.lisa.joycar.antlr.Java8Parser.MethodInvocation_lfno_primaryContext;
import it.unive.lisa.joycar.antlr.Java8Parser.MethodModifierContext;
import it.unive.lisa.joycar.antlr.Java8Parser.MultiplicativeExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.NormalClassDeclarationContext;
import it.unive.lisa.joycar.antlr.Java8Parser.NumericTypeContext;
import it.unive.lisa.joycar.antlr.Java8Parser.PackageOrTypeNameContext;
import it.unive.lisa.joycar.antlr.Java8Parser.PostfixExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.PrimaryContext;
import it.unive.lisa.joycar.antlr.Java8Parser.PrimaryNoNewArray_lfno_primaryContext;
import it.unive.lisa.joycar.antlr.Java8Parser.RelationalExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ResultContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ReturnStatementContext;
import it.unive.lisa.joycar.antlr.Java8Parser.ShiftExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.StatementContext;
import it.unive.lisa.joycar.antlr.Java8Parser.StatementExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.StatementNoShortIfContext;
import it.unive.lisa.joycar.antlr.Java8Parser.StatementWithoutTrailingSubstatementContext;
import it.unive.lisa.joycar.antlr.Java8Parser.TypeDeclarationContext;
import it.unive.lisa.joycar.antlr.Java8Parser.TypeNameContext;
import it.unive.lisa.joycar.antlr.Java8Parser.UnannArrayTypeContext;
import it.unive.lisa.joycar.antlr.Java8Parser.UnannClassOrInterfaceTypeContext;
import it.unive.lisa.joycar.antlr.Java8Parser.UnannClassType_lfno_unannClassOrInterfaceTypeContext;
import it.unive.lisa.joycar.antlr.Java8Parser.UnannPrimitiveTypeContext;
import it.unive.lisa.joycar.antlr.Java8Parser.UnannReferenceTypeContext;
import it.unive.lisa.joycar.antlr.Java8Parser.UnannTypeContext;
import it.unive.lisa.joycar.antlr.Java8Parser.UnaryExpressionContext;
import it.unive.lisa.joycar.antlr.Java8Parser.UnaryExpressionNotPlusMinusContext;
import it.unive.lisa.joycar.antlr.Java8Parser.VariableDeclaratorContext;
import it.unive.lisa.joycar.antlr.Java8Parser.VariableInitializerContext;
import it.unive.lisa.joycar.antlr.Java8Parser.WhileStatementContext;
import it.unive.lisa.joycar.antlr.Java8ParserBaseVisitor;
import it.unive.lisa.joycar.statements.JavaNewObj;
import it.unive.lisa.joycar.types.ArrayType;
import it.unive.lisa.joycar.types.ClassType;
import it.unive.lisa.joycar.types.StringType;
import it.unive.lisa.joycar.units.JNIEnv;
import it.unive.lisa.joycar.units.JavaObject;
import it.unive.lisa.joycar.units.JavaString;
import it.unive.lisa.program.ClassUnit;
import it.unive.lisa.program.CompilationUnit;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.CodeMemberDescriptor;
import it.unive.lisa.program.cfg.Parameter;
import it.unive.lisa.program.cfg.controlFlow.IfThenElse;
import it.unive.lisa.program.cfg.controlFlow.Loop;
import it.unive.lisa.program.cfg.edge.Edge;
import it.unive.lisa.program.cfg.edge.FalseEdge;
import it.unive.lisa.program.cfg.edge.SequentialEdge;
import it.unive.lisa.program.cfg.edge.TrueEdge;
import it.unive.lisa.program.cfg.statement.Assignment;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.NoOp;
import it.unive.lisa.program.cfg.statement.Ret;
import it.unive.lisa.program.cfg.statement.Return;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.program.cfg.statement.comparison.Equal;
import it.unive.lisa.program.cfg.statement.comparison.GreaterOrEqual;
import it.unive.lisa.program.cfg.statement.comparison.GreaterThan;
import it.unive.lisa.program.cfg.statement.comparison.LessOrEqual;
import it.unive.lisa.program.cfg.statement.comparison.LessThan;
import it.unive.lisa.program.cfg.statement.comparison.NotEqual;
import it.unive.lisa.program.cfg.statement.literal.FalseLiteral;
import it.unive.lisa.program.cfg.statement.literal.Int32Literal;
import it.unive.lisa.program.cfg.statement.literal.TrueLiteral;
import it.unive.lisa.program.type.BoolType;
import it.unive.lisa.program.type.Int32Type;
import it.unive.lisa.type.ReferenceType;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.VoidType;
import it.unive.lisa.util.datastructures.graph.code.NodeList;

public class JavaFrontend extends Java8ParserBaseVisitor<Object> {

	public static Program parse(String file) throws IOException {
		Java8Lexer lexer;
		try (InputStream stream = new FileInputStream(file)) {
			lexer = new Java8Lexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
		}
		Java8Parser parser = new Java8Parser(new CommonTokenStream(lexer));

		// this is needed to get an exception on malformed input
		// otherwise an error is dumped to stderr and the partial
		// parsing result is returned
		parser.setErrorHandler(new BailErrorStrategy());
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		parser.setBuildParseTree(true);

		CompilationUnitContext unit = parser.compilationUnit();
		JavaFrontend frontend = new JavaFrontend(file);
		ClassType.create(JavaObject.NAME, frontend.objectClass);
		ClassType.create(JavaObject.SHORT_NAME, frontend.objectClass);
		new StringType(frontend.stringClass); // this will register it among the class types
		ClassType.create(JNIEnv.SHORT_NAME, frontend.jniEnvClass);
		
		Program p = frontend.visitCompilationUnit(unit);

		p.getAllCFGs().stream()
				.filter(cfg -> !cfg.getDescriptor().isInstance() && cfg.getDescriptor().getName().equals("main"))
				.forEach(p::addEntryPoint);
		
		return p;
	}

	private final String file;

	private Program program;

	private NodeList<CFG, Statement, Edge> matrix;

	private CFG currentCfg;

	private boolean lastParsedStaticFlag;

	private CompilationUnit currentUnit;
	
	public JavaObject objectClass;
	public JavaString stringClass;
	public JNIEnv jniEnvClass;

	private JavaFrontend(String file) {
		this.file = file;
		program = new Program(new JavaFeatures(), new JavaTypeSystem());
		objectClass = new JavaObject(program);
		stringClass = new JavaString(program, objectClass);
		jniEnvClass = new JNIEnv(program, objectClass);
	}

	@Override
	public Program visitCompilationUnit(CompilationUnitContext ctx) {
		if (ctx.typeDeclaration() != null)
			for (TypeDeclarationContext type : ctx.typeDeclaration())
				visitTypeDeclaration(type);
		return program;
	}

	@Override
	public CompilationUnit visitTypeDeclaration(TypeDeclarationContext ctx) {
		if (ctx.classDeclaration() == null)
			throw notSupported(ctx);

		return visitClassDeclaration(ctx.classDeclaration());
	}

	@Override
	public CompilationUnit visitClassDeclaration(ClassDeclarationContext ctx) {
		if (ctx.normalClassDeclaration() == null)
			throw notSupported(ctx);

		return visitNormalClassDeclaration(ctx.normalClassDeclaration());
	}

	@Override
	public CompilationUnit visitNormalClassDeclaration(NormalClassDeclarationContext ctx) {
		String name = ctx.Identifier().getText();

		currentUnit = new ClassUnit(fromContext(file, ctx), program, name, false);
		currentUnit.addAncestor(objectClass);

		program.addUnit(currentUnit);

		ClassType.create(name, currentUnit);

		if (ctx.classBody().classBodyDeclaration() != null)
			for (ClassBodyDeclarationContext decl : ctx.classBody().classBodyDeclaration())
				visitClassBodyDeclaration(decl);

		return currentUnit;
	}

	@Override
	public Object visitClassBodyDeclaration(ClassBodyDeclarationContext ctx) {
		if (ctx.staticInitializer() != null) {
			System.out.println("Ignoring '" + ctx.getText() + "'");
			return null;
		}

		if (ctx.classMemberDeclaration() != null)
			return visitClassMemberDeclaration(ctx.classMemberDeclaration());

		throw notSupported(ctx);
	}

	@Override
	public Object visitClassMemberDeclaration(ClassMemberDeclarationContext ctx) {
		if (ctx.methodDeclaration() == null)
			throw notSupported(ctx);

		return visitMethodDeclaration(ctx.methodDeclaration());
	}

	@Override
	public CFG visitMethodDeclaration(MethodDeclarationContext ctx) {
		boolean static_ = false, native_ = false;
		if (ctx.methodModifier() != null)
			for (MethodModifierContext mod : ctx.methodModifier())
				if (mod.STATIC() != null)
					static_ = true;
				else if (mod.NATIVE() != null)
					native_ = true;

		lastParsedStaticFlag = static_;

		CodeMemberDescriptor descriptor = visitMethodHeader(ctx.methodHeader());

		Collection<Statement> entrypoints = new LinkedList<>();
		matrix = new NodeList<>(ANTLRUtils.SEQUENTIAL_SINGLETON);
		currentCfg = new CFG(descriptor, entrypoints, matrix);

		if (native_)
			parseAsNative(ctx, descriptor);
		else
			parseCode(ctx, descriptor, entrypoints);

		currentCfg.simplify();

		if (lastParsedStaticFlag)
			currentUnit.addCodeMember(currentCfg);
		else
			currentUnit.addInstanceCodeMember(currentCfg);

		return currentCfg;
	}

	private void parseCode(MethodDeclarationContext ctx, CodeMemberDescriptor descriptor,
			Collection<Statement> entrypoints) {
		Triple<Statement, NodeList<CFG, Statement, Edge>,
				Statement> visited = visitMethodBody(ctx.methodBody());
		entrypoints.add(visited.getLeft());
		matrix.mergeWith(visited.getMiddle());

		if (descriptor.getReturnType().isVoidType()) {
			Ret ret = new Ret(currentCfg, descriptor.getLocation());
			if (currentCfg.getAllExitpoints().isEmpty()) {
				if (currentCfg.getNodesCount() == 0) {
					// empty method, so the ret is also the entrypoint
					matrix.addNode(ret);
					entrypoints.add(ret);
				} else {
					// every non-throwing instruction that does not have a
					// follower
					// is ending the method
					Collection<Statement> preExits = new LinkedList<>();
					for (Statement st : matrix.getNodes())
						if (!st.stopsExecution() && matrix.followersOf(st).isEmpty())
							preExits.add(st);
					matrix.addNode(ret);
					for (Statement st : preExits)
						matrix.addEdge(new SequentialEdge(st, ret));
				}
			}

			for (Statement exit : matrix.getExits())
				if (!exit.stopsExecution()) {
					if (!matrix.containsNode(ret))
						matrix.addNode(ret);
					matrix.addEdge(new SequentialEdge(exit, ret));
				}
		}
	}

	private void parseAsNative(MethodDeclarationContext ctx, CodeMemberDescriptor descriptor) {
		String name = "Java_" + currentUnit.getName().replace('.', '_') + "_" + descriptor.getName();

		Parameter[] formals = descriptor.getFormals();
		// we add the env at the beginning, and the receiver of this call
		// becomes the second param
		Expression[] args = new Expression[1 + formals.length];
		CodeLocation loc = fromContext(file, ctx);
		args[0] = new JavaNewObj(currentCfg, loc, ClassType.search(JNIEnv.SHORT_NAME));
		for (int i = 0; i < formals.length; i++)
			args[i + 1] = new VariableRef(currentCfg, loc, formals[i].getName());

		UnresolvedCall call = new UnresolvedCall(
				currentCfg,
				fromContext(file, ctx),
				CallType.STATIC,
				program.getName(),
				name,
				args);
		if (!descriptor.getReturnType().isVoidType())
			currentCfg.addNode(new Return(currentCfg, loc, call), true);
		else {
			Ret ret = new Ret(currentCfg, loc);
			currentCfg.addNode(call, true);
			currentCfg.addNode(ret);
			currentCfg.addEdge(new SequentialEdge(call, ret));
		}
	}

	@Override
	public CodeMemberDescriptor visitMethodHeader(MethodHeaderContext ctx) {
		Type ret = visitResult(ctx.result());
		String name = ctx.methodDeclarator().Identifier().getText();

		List<Parameter> formals;
		if (ctx.methodDeclarator().formalParameterList() == null)
			formals = new LinkedList<>();
		else
			formals = visitFormalParameterList(ctx.methodDeclarator().formalParameterList());

		if (!lastParsedStaticFlag) {
			Parameter rec = new Parameter(fromContext(file, ctx), "this",
					new ReferenceType(ClassType.search(currentUnit.getName())));
			formals = new LinkedList<>(formals);
			formals.add(0, rec);
		}

		return new CodeMemberDescriptor(fromContext(file, ctx), currentUnit, !lastParsedStaticFlag, name, ret,
				formals.toArray(Parameter[]::new));
	}

	@Override
	public List<Parameter> visitFormalParameterList(FormalParameterListContext ctx) {
		if (ctx.receiverParameter() != null)
			throw notSupported(ctx);

		if (ctx.formalParameters() == null)
			return List.of(visitLastFormalParameter(ctx.lastFormalParameter()));

		List<Parameter> formals = visitFormalParameters(ctx.formalParameters());
		formals.add(visitLastFormalParameter(ctx.lastFormalParameter()));
		return formals;
	}

	@Override
	public Parameter visitLastFormalParameter(LastFormalParameterContext ctx) {
		if (ctx.formalParameter() != null)
			return visitFormalParameter(ctx.formalParameter());

		Type type = visitUnannType(ctx.unannType());
		String name = ctx.variableDeclaratorId().Identifier().getText();
		int dims = 1; // for the dots
		if (ctx.variableDeclaratorId().dims() != null)
			dims += ctx.variableDeclaratorId().dims().LBRACK().size();

		return new Parameter(fromContext(file, ctx.variableDeclaratorId()), name, ArrayType.lookup(type, dims));
	}

	@Override
	public List<Parameter> visitFormalParameters(FormalParametersContext ctx) {
		if (ctx.receiverParameter() != null)
			throw notSupported(ctx);

		List<Parameter> formals = new ArrayList<>(ctx.formalParameter().size());
		for (FormalParameterContext formal : ctx.formalParameter())
			formals.add(visitFormalParameter(formal));

		return formals;
	}

	@Override
	public Parameter visitFormalParameter(FormalParameterContext ctx) {
		Type type = visitUnannType(ctx.unannType());
		String name = ctx.variableDeclaratorId().Identifier().getText();
		if (ctx.variableDeclaratorId().dims() == null)
			return new Parameter(fromContext(file, ctx.variableDeclaratorId()), name, type);

		return new Parameter(fromContext(file, ctx.variableDeclaratorId()), name,
				ArrayType.lookup(type, ctx.variableDeclaratorId().dims().LBRACK().size()));
	}

	@Override
	public Type visitResult(ResultContext ctx) {
		if (ctx.VOID() != null)
			return VoidType.INSTANCE;

		return visitUnannType(ctx.unannType());
	}

	@Override
	public Type visitUnannType(UnannTypeContext ctx) {
		if (ctx.unannPrimitiveType() != null)
			return visitUnannPrimitiveType(ctx.unannPrimitiveType());

		return visitUnannReferenceType(ctx.unannReferenceType());
	}

	@Override
	public Type visitUnannReferenceType(UnannReferenceTypeContext ctx) {
		if (ctx.unannArrayType() != null)
			return visitUnannArrayType(ctx.unannArrayType());

		if (ctx.unannClassOrInterfaceType() != null)
			return visitUnannClassOrInterfaceType(ctx.unannClassOrInterfaceType());

		throw notSupported(ctx);
	}

	@Override
	public Type visitUnannArrayType(UnannArrayTypeContext ctx) {
		Type inner;
		if (ctx.unannPrimitiveType() != null)
			inner = visitUnannPrimitiveType(ctx.unannPrimitiveType());
		else if (ctx.unannClassOrInterfaceType() != null)
			inner = visitUnannClassOrInterfaceType(ctx.unannClassOrInterfaceType());
		else
			throw notSupported(ctx);

		int dims = ctx.dims().LBRACK().size();
		return ArrayType.lookup(inner, dims);
	}

	@Override
	public Type visitUnannClassOrInterfaceType(UnannClassOrInterfaceTypeContext ctx) {
		if (ctx.unannClassType_lfno_unannClassOrInterfaceType() != null)
			return visitUnannClassType_lfno_unannClassOrInterfaceType(
					ctx.unannClassType_lfno_unannClassOrInterfaceType());

		if (ctx.unannInterfaceType_lfno_unannClassOrInterfaceType() != null)
			return visitUnannClassType_lfno_unannClassOrInterfaceType(
					ctx.unannInterfaceType_lfno_unannClassOrInterfaceType()
							.unannClassType_lfno_unannClassOrInterfaceType());

		throw notSupported(ctx);
	}

	@Override
	public Type visitUnannClassType_lfno_unannClassOrInterfaceType(
			UnannClassType_lfno_unannClassOrInterfaceTypeContext ctx) {
		return ClassType.search(ctx.Identifier().getText());
	}

	@Override
	public Type visitUnannPrimitiveType(UnannPrimitiveTypeContext ctx) {
		if (ctx.BOOLEAN() != null)
			return BoolType.INSTANCE;

		return visitNumericType(ctx.numericType());
	}

	@Override
	public Type visitNumericType(NumericTypeContext ctx) {
		if (ctx.integralType() == null)
			throw notSupported(ctx);

		return visitIntegralType(ctx.integralType());
	}

	@Override
	public Type visitIntegralType(IntegralTypeContext ctx) {
		if (ctx.INT() != null)
			return Int32Type.INSTANCE;

		throw notSupported(ctx);
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitMethodBody(MethodBodyContext ctx) {
		if (ctx.block() == null)
			return fromSingle(new NoOp(currentCfg, fromContext(file, ctx)));
		return visitBlock(ctx.block());
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitBlock(BlockContext ctx) {
		if (ctx.blockStatements() == null)
			return fromSingle(new NoOp(currentCfg, fromContext(file, ctx)));
		return visitBlockStatements(ctx.blockStatements());
	}

	private static void addEdge(Edge e, NodeList<CFG, Statement, Edge> block) {
		if (!e.getSource().stopsExecution())
			block.addEdge(e);
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitBlockStatements(
			BlockStatementsContext ctx) {
		NodeList<CFG, Statement, Edge> block = new NodeList<>(ANTLRUtils.SEQUENTIAL_SINGLETON);

		Statement first = null, last = null;
		for (int i = 0; i < ctx.blockStatement().size(); i++) {
			Triple<Statement, NodeList<CFG, Statement, Edge>,
					Statement> st = visitBlockStatement(ctx.blockStatement(i));
			block.mergeWith(st.getMiddle());
			if (first == null)
				first = st.getLeft();
			if (last != null)
				addEdge(new SequentialEdge(last, st.getLeft()), block);
			last = st.getRight();
		}

		if (first == null && last == null)
			return fromSingle(new NoOp(currentCfg, fromContext(file, ctx)));

		return Triple.of(first, block, last);
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitBlockStatement(
			BlockStatementContext ctx) {
		if (ctx.classDeclaration() != null)
			throw notSupported(ctx);

		if (ctx.localVariableDeclarationStatement() != null)
			return visitLocalVariableDeclarationStatement(ctx.localVariableDeclarationStatement());

		return visitStatement(ctx.statement());
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitLocalVariableDeclarationStatement(
			LocalVariableDeclarationStatementContext ctx) {
		return fromSingle(visitLocalVariableDeclaration(ctx.localVariableDeclaration()));
	}

	@Override
	public Expression visitLocalVariableDeclaration(LocalVariableDeclarationContext ctx) {
		if (ctx.variableDeclaratorList().variableDeclarator().size() != 1)
			throw notSupported(ctx);

		VariableDeclaratorContext declarator = ctx.variableDeclaratorList().variableDeclarator().iterator().next();
		if (declarator.variableInitializer() == null)
			throw notSupported(ctx);

		return new Assignment(currentCfg, ANTLRUtils.fromToken(file, declarator.ASSIGN().getSymbol()),
				new VariableRef(currentCfg, fromContext(file, declarator.variableDeclaratorId()),
						declarator.variableDeclaratorId().Identifier().getText()),
				visitVariableInitializer(declarator.variableInitializer()));
	}

	@Override
	public Expression visitVariableInitializer(VariableInitializerContext ctx) {
		if (ctx.arrayInitializer() != null)
			throw notSupported(ctx);
		return visitExpression(ctx.expression());
	}

	@Override
	public Expression visitExpression(ExpressionContext ctx) {
		if (ctx.lambdaExpression() != null)
			throw notSupported(ctx);
		return visitAssignmentExpression(ctx.assignmentExpression());
	}

	@Override
	public Expression visitAssignmentExpression(AssignmentExpressionContext ctx) {
		if (ctx.conditionalExpression() != null)
			return visitConditionalExpression(ctx.conditionalExpression());
		throw notSupported(ctx);
	}

	@Override
	public Expression visitConditionalExpression(ConditionalExpressionContext ctx) {
		if (ctx.expression() != null)
			throw notSupported(ctx);
		return visitConditionalOrExpression(ctx.conditionalOrExpression());
	}

	@Override
	public Expression visitConditionalOrExpression(ConditionalOrExpressionContext ctx) {
		Expression inner = visitConditionalAndExpression(ctx.conditionalAndExpression());

		if (ctx.conditionalOrExpression() == null)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitConditionalAndExpression(ConditionalAndExpressionContext ctx) {
		Expression inner = visitInclusiveOrExpression(ctx.inclusiveOrExpression());

		if (ctx.conditionalAndExpression() == null)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitInclusiveOrExpression(InclusiveOrExpressionContext ctx) {
		Expression inner = visitExclusiveOrExpression(ctx.exclusiveOrExpression());

		if (ctx.inclusiveOrExpression() == null)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitExclusiveOrExpression(ExclusiveOrExpressionContext ctx) {
		Expression inner = visitAndExpression(ctx.andExpression());

		if (ctx.exclusiveOrExpression() == null)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitAndExpression(AndExpressionContext ctx) {
		Expression inner = visitEqualityExpression(ctx.equalityExpression());

		if (ctx.andExpression() == null)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitEqualityExpression(EqualityExpressionContext ctx) {
		Expression inner = visitRelationalExpression(ctx.relationalExpression());

		if (ctx.equalityExpression() == null)
			return inner;

		Expression left = visitEqualityExpression(ctx.equalityExpression());
		if (ctx.EQUAL() != null)
			return new Equal(currentCfg, fromToken(file, ctx.EQUAL().getSymbol()), left, inner);
		else
			return new NotEqual(currentCfg, fromToken(file, ctx.EQUAL().getSymbol()), left, inner);
	}

	@Override
	public Expression visitRelationalExpression(RelationalExpressionContext ctx) {
		Expression inner = visitShiftExpression(ctx.shiftExpression());

		if (ctx.relationalExpression() == null)
			return inner;

		Expression left = visitRelationalExpression(ctx.relationalExpression());
		if (ctx.LE() != null)
			return new LessOrEqual(currentCfg, fromToken(file, ctx.LE().getSymbol()), left, inner);
		else if (ctx.GE() != null)
			return new GreaterOrEqual(currentCfg, fromToken(file, ctx.GE().getSymbol()), left, inner);
		else if (ctx.LT() != null)
			return new LessThan(currentCfg, fromToken(file, ctx.LT().getSymbol()), left, inner);
		else if (ctx.GT() != null)
			return new GreaterThan(currentCfg, fromToken(file, ctx.GT().getSymbol()), left, inner);
		else
			throw notSupported(ctx);
	}

	@Override
	public Expression visitShiftExpression(ShiftExpressionContext ctx) {
		Expression inner = visitAdditiveExpression(ctx.additiveExpression());

		if (ctx.shiftExpression() == null)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitAdditiveExpression(AdditiveExpressionContext ctx) {
		Expression inner = visitMultiplicativeExpression(ctx.multiplicativeExpression());

		if (ctx.additiveExpression() == null)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		Expression inner = visitUnaryExpression(ctx.unaryExpression());

		if (ctx.multiplicativeExpression() == null)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitUnaryExpression(UnaryExpressionContext ctx) {
		if (ctx.unaryExpressionNotPlusMinus() != null)
			return visitUnaryExpressionNotPlusMinus(ctx.unaryExpressionNotPlusMinus());

		throw notSupported(ctx);
	}

	@Override
	public Expression visitUnaryExpressionNotPlusMinus(UnaryExpressionNotPlusMinusContext ctx) {
		if (ctx.postfixExpression() != null)
			return visitPostfixExpression(ctx.postfixExpression());

		throw notSupported(ctx);
	}

	@Override
	public Expression visitPostfixExpression(PostfixExpressionContext ctx) {
		if (ctx.primary() != null)
			return visitPrimary(ctx.primary());

		throw notSupported(ctx);
	}

	@Override
	public Expression visitPrimary(PrimaryContext ctx) {
		if (ctx.primaryNoNewArray_lfno_primary() != null)
			return visitPrimaryNoNewArray_lfno_primary(ctx.primaryNoNewArray_lfno_primary());

		throw notSupported(ctx);
	}

	@Override
	public Expression visitPrimaryNoNewArray_lfno_primary(PrimaryNoNewArray_lfno_primaryContext ctx) {
		if (ctx.classInstanceCreationExpression_lfno_primary() != null)
			return visitClassInstanceCreationExpression_lfno_primary(
					ctx.classInstanceCreationExpression_lfno_primary());

		if (ctx.literal() != null)
			return visitLiteral(ctx.literal());

		if (ctx.methodInvocation_lfno_primary() != null)
			return visitMethodInvocation_lfno_primary(ctx.methodInvocation_lfno_primary());

		throw notSupported(ctx);
	}

	@Override
	public Expression visitMethodInvocation_lfno_primary(MethodInvocation_lfno_primaryContext ctx) {
		if (ctx.SUPER() != null)
			throw notSupported(ctx);

		Expression[] args;
		if (ctx.argumentList() != null)
			args = visitArgumentList(ctx.argumentList());
		else
			args = new Expression[0];

		String target;
		if (ctx.Identifier() != null)
			target = ctx.Identifier().getText();
		else
			target = ctx.methodName().Identifier().getText();

		String innername = null;
		ParserRuleContext innerctx = null;
		if (ctx.typeName() != null) {
			innername = visitTypeName(ctx.typeName());
			innerctx = ctx.typeName();
		} else if (ctx.expressionName() != null) {
			innername = visitExpressionName(ctx.expressionName());
			innerctx = ctx.expressionName();
		}

		if (innername == null)
			return call(ctx, target, args);

		Expression[] args2 = new Expression[args.length + 1];
		args2[0] = new VariableRef(currentCfg, fromContext(file, innerctx), innername);
		for (int i = 0; i < args.length; i++)
			args2[i + 1] = args[i];
		return call(ctx, target, args2);
	}

	private UnresolvedCall call(ParserRuleContext ctx, String target, Expression... actuals) {
		return new UnresolvedCall(
				currentCfg,
				fromContext(file, ctx),
				CallType.UNKNOWN,
				null,
				target,
				actuals);
	}

	@Override
	public String visitExpressionName(ExpressionNameContext ctx) {
		if (ctx.ambiguousName() == null)
			return ctx.Identifier().getText();
		return visitAmbiguousName(ctx.ambiguousName()) + "." + ctx.Identifier().getText();
	}

	@Override
	public String visitAmbiguousName(AmbiguousNameContext ctx) {
		if (ctx.ambiguousName() == null)
			return ctx.Identifier().getText();
		return visitAmbiguousName(ctx.ambiguousName()) + "." + ctx.Identifier().getText();
	}

	@Override
	public String visitTypeName(TypeNameContext ctx) {
		if (ctx.packageOrTypeName() == null)
			return ctx.Identifier().getText();
		return visitPackageOrTypeName(ctx.packageOrTypeName()) + "." + ctx.Identifier().getText();
	}

	@Override
	public String visitPackageOrTypeName(PackageOrTypeNameContext ctx) {
		if (ctx.packageOrTypeName() == null)
			return ctx.Identifier().getText();
		return visitPackageOrTypeName(ctx.packageOrTypeName()) + "." + ctx.Identifier().getText();
	}

	@Override
	public Expression visitLiteral(LiteralContext ctx) {
		if (ctx.IntegerLiteral() != null)
			return new Int32Literal(currentCfg, fromContext(file, ctx),
					Integer.parseInt(ctx.IntegerLiteral().getText()));

		if (ctx.BooleanLiteral() != null)
			if (Boolean.parseBoolean(ctx.BooleanLiteral().getText()))
				return new TrueLiteral(currentCfg, fromContext(file, ctx));
			else
				return new FalseLiteral(currentCfg, fromContext(file, ctx));

		throw notSupported(ctx);
	}

	@Override
	public Expression visitClassInstanceCreationExpression_lfno_primary(
			ClassInstanceCreationExpression_lfno_primaryContext ctx) {
		Type target = ClassType.search(ctx.Identifier(0).getText());
		if (ctx.argumentList() == null)
			return new JavaNewObj(currentCfg, fromContext(file, ctx), target);
		return new JavaNewObj(currentCfg, fromContext(file, ctx), target, visitArgumentList(ctx.argumentList()));
	}

	@Override
	public Expression[] visitArgumentList(ArgumentListContext ctx) {
		List<ExpressionContext> expression = ctx.expression();
		Expression[] result = new Expression[expression.size()];
		for (int i = 0; i < expression.size(); i++)
			result[i] = visitExpression(ctx.expression(i));
		return result;
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitStatement(StatementContext ctx) {
		if (ctx.ifThenStatement() != null)
			return visitIfThenStatement(ctx.ifThenStatement());

		if (ctx.statementWithoutTrailingSubstatement() != null)
			return visitStatementWithoutTrailingSubstatement(ctx.statementWithoutTrailingSubstatement());

		if (ctx.whileStatement() != null)
			return visitWhileStatement(ctx.whileStatement());

		if (ctx.ifThenElseStatement() != null)
			return visitIfThenElseStatement(ctx.ifThenElseStatement());

		throw notSupported(ctx);
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitIfThenElseStatement(
			IfThenElseStatementContext ctx) {
		Expression condition = visitExpression(ctx.expression());

		Triple<Statement, NodeList<CFG, Statement, Edge>,
				Statement> trueBlock = visitStatementNoShortIf(ctx.statementNoShortIf());

		Triple<Statement, NodeList<CFG, Statement, Edge>,
				Statement> falseBlock = visitStatement(ctx.statement());

		NodeList<CFG, Statement, Edge> block = new NodeList<>(ANTLRUtils.SEQUENTIAL_SINGLETON);
		NoOp exit = new NoOp(currentCfg, fromToken(file, ctx.IF().getSymbol()));
		block.addNode(condition);
		block.addNode(exit);
		block.mergeWith(trueBlock.getMiddle());
		block.mergeWith(falseBlock.getMiddle());
		addEdge(new TrueEdge(condition, trueBlock.getLeft()), block);
		addEdge(new FalseEdge(condition, falseBlock.getLeft()), block);
		addEdge(new SequentialEdge(trueBlock.getRight(), exit), block);
		addEdge(new SequentialEdge(falseBlock.getRight(), exit), block);

		currentCfg.addControlFlowStructure(new IfThenElse(matrix, condition, exit, trueBlock.getMiddle().getNodes(),
				falseBlock.getMiddle().getNodes()));

		return Triple.of(condition, block, exit);
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitStatementNoShortIf(
			StatementNoShortIfContext ctx) {
		if (ctx.statementWithoutTrailingSubstatement() != null)
			return visitStatementWithoutTrailingSubstatement(ctx.statementWithoutTrailingSubstatement());

		throw notSupported(ctx);
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitWhileStatement(
			WhileStatementContext ctx) {
		NodeList<CFG, Statement, Edge> block = new NodeList<>(ANTLRUtils.SEQUENTIAL_SINGLETON);

		Statement condition = visitExpression(ctx.expression());

		Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> body = visitStatement(ctx.statement());

		NoOp exit = new NoOp(currentCfg, fromToken(file, ctx.RPAREN().getSymbol()));

		block.addNode(condition);
		block.mergeWith(body.getMiddle());
		addEdge(new TrueEdge(condition, body.getLeft()), block);
		addEdge(new SequentialEdge(body.getRight(), condition), block);
		block.addNode(exit);
		addEdge(new FalseEdge(condition, exit), block);

		currentCfg
				.addControlFlowStructure(new Loop(matrix, condition, exit, body.getMiddle().getNodes()));

		return Triple.of(condition, block, exit);
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>,
			Statement> visitStatementWithoutTrailingSubstatement(StatementWithoutTrailingSubstatementContext ctx) {
		if (ctx.expressionStatement() != null)
			return visitExpressionStatement(ctx.expressionStatement());

		if (ctx.returnStatement() != null)
			return visitReturnStatement(ctx.returnStatement());

		if (ctx.block() != null)
			return visitBlock(ctx.block());

		throw notSupported(ctx);
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitExpressionStatement(
			ExpressionStatementContext ctx) {
		return fromSingle(visitStatementExpression(ctx.statementExpression()));
	}

	@Override
	public Expression visitStatementExpression(StatementExpressionContext ctx) {
		if (ctx.methodInvocation() != null)
			return visitMethodInvocation(ctx.methodInvocation());

		throw notSupported(ctx);
	}

	@Override
	public Expression visitMethodInvocation(MethodInvocationContext ctx) {
		if (ctx.SUPER() != null)
			throw notSupported(ctx);

		Expression[] args;
		if (ctx.argumentList() != null)
			args = visitArgumentList(ctx.argumentList());
		else
			args = new Expression[0];

		String target;
		if (ctx.Identifier() != null)
			target = ctx.Identifier().getText();
		else
			target = ctx.methodName().Identifier().getText();

		String innername = null;
		ParserRuleContext innerctx = null;
		if (ctx.typeName() != null) {
			innername = visitTypeName(ctx.typeName());
			innerctx = ctx.typeName();
		} else if (ctx.expressionName() != null) {
			innername = visitExpressionName(ctx.expressionName());
			innerctx = ctx.expressionName();
		}

		if (innername != null) {
			Expression[] args2 = new Expression[args.length + 1];
			args2[0] = new VariableRef(currentCfg, fromContext(file, innerctx), innername);
			for (int i = 0; i < args.length; i++)
				args2[i + 1] = args[i];
			return call(ctx, target, args2);
		} else if (ctx.primary() != null) {
			Expression[] args2 = new Expression[args.length + 1];
			args2[0] = visitPrimary(ctx.primary());
			for (int i = 0; i < args.length; i++)
				args2[i + 1] = args[i];
			return call(ctx, target, args2);
		} else
			return call(ctx, target, args);
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitReturnStatement(
			ReturnStatementContext ctx) {
		if (ctx.expression() == null)
			return fromSingle(new Ret(currentCfg, fromContext(file, ctx)));
		return fromSingle(new Return(currentCfg, fromContext(file, ctx), visitExpression(ctx.expression())));
	}

	@Override
	public Triple<Statement, NodeList<CFG, Statement, Edge>, Statement> visitIfThenStatement(
			IfThenStatementContext ctx) {
		Expression condition = visitExpression(ctx.expression());

		Triple<Statement, NodeList<CFG, Statement, Edge>,
				Statement> trueBlock = visitStatement(ctx.statement());

		NodeList<CFG, Statement, Edge> block = new NodeList<>(ANTLRUtils.SEQUENTIAL_SINGLETON);
		NoOp exit = new NoOp(currentCfg, fromToken(file, ctx.IF().getSymbol()));
		block.addNode(condition);
		block.addNode(exit);
		block.mergeWith(trueBlock.getMiddle());
		addEdge(new TrueEdge(condition, trueBlock.getLeft()), block);
		addEdge(new FalseEdge(condition, exit), block);
		addEdge(new SequentialEdge(trueBlock.getRight(), exit), block);

		currentCfg.addControlFlowStructure(new IfThenElse(matrix, condition, exit, trueBlock.getMiddle().getNodes(),
				Collections.emptySet()));

		return Triple.of(condition, block, exit);
	}
}
