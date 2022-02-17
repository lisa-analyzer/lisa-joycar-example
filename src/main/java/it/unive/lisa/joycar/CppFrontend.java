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
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import org.antlr.v4.runtime.BailErrorStrategy;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.atn.PredictionMode;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;

import it.unive.lisa.joycar.antlr.CPP14Lexer;
import it.unive.lisa.joycar.antlr.CPP14Parser;
import it.unive.lisa.joycar.antlr.CPP14Parser.AdditiveExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.AdditiveOpContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.AndExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.AssignmentExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.BlockDeclarationContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.BraceOrEqualInitializerContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.CastExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ClassNameContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.CompoundStatementContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ConditionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ConditionalExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.DeclSpecifierContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.DeclSpecifierSeqContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.DeclarationContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.DeclarationseqContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.DeclaratorContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.DeclaratoridContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.EqualityExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.EqualityOpContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ExclusiveOrExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ExpressionListContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ExpressionStatementContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ForInitStatementContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.FunctionBodyContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.FunctionDefinitionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.IdExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.InclusiveOrExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.InitDeclaratorContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.InitDeclaratorListContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.InitializerClauseContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.InitializerListContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.IterationStatementContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.JumpStatementContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.LiteralContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.LogicalAndExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.LogicalOrExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.MultiplicativeExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.MultiplicativeOpContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.NoPointerDeclaratorContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ParameterDeclarationClauseContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ParameterDeclarationContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ParameterDeclarationListContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ParametersAndQualifiersContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.PointerDeclaratorContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.PointerMemberExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.PostfixExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.PrimaryExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.RelationalExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.RelationalOpContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.SelectionStatementContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.ShiftExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.SimpleDeclarationContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.SimpleTypeLengthModifierContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.SimpleTypeSpecifierContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.StatementContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.StatementSeqContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.TheTypeNameContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.TrailingTypeSpecifierContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.TranslationUnitContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.TypeSpecifierContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.UnaryExpressionContext;
import it.unive.lisa.joycar.antlr.CPP14Parser.UnqualifiedIdContext;
import it.unive.lisa.joycar.antlr.CPP14ParserBaseVisitor;
import it.unive.lisa.joycar.statements.Addition;
import it.unive.lisa.joycar.statements.Division;
import it.unive.lisa.joycar.statements.Multiplication;
import it.unive.lisa.joycar.statements.Negation;
import it.unive.lisa.joycar.statements.Remainder;
import it.unive.lisa.joycar.statements.Subtraction;
import it.unive.lisa.joycar.types.ClassType;
import it.unive.lisa.joycar.types.JNIExportType;
import it.unive.lisa.joycar.units.JavaObject;
import it.unive.lisa.program.Program;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CFGDescriptor;
import it.unive.lisa.program.cfg.CodeLocation;
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
import it.unive.lisa.program.cfg.statement.call.assignment.OrderPreservingAssigningStrategy;
import it.unive.lisa.program.cfg.statement.call.resolution.RuntimeTypesMatchingStrategy;
import it.unive.lisa.program.cfg.statement.call.traversal.SingleInheritanceTraversalStrategy;
import it.unive.lisa.program.cfg.statement.comparison.Equal;
import it.unive.lisa.program.cfg.statement.comparison.GreaterOrEqual;
import it.unive.lisa.program.cfg.statement.comparison.GreaterThan;
import it.unive.lisa.program.cfg.statement.comparison.LessOrEqual;
import it.unive.lisa.program.cfg.statement.comparison.LessThan;
import it.unive.lisa.program.cfg.statement.comparison.NotEqual;
import it.unive.lisa.program.cfg.statement.literal.Int32Literal;
import it.unive.lisa.program.cfg.statement.literal.TrueLiteral;
import it.unive.lisa.type.ReferenceType;
import it.unive.lisa.type.Type;
import it.unive.lisa.type.VoidType;
import it.unive.lisa.type.common.BoolType;
import it.unive.lisa.type.common.Int32;
import it.unive.lisa.type.common.Int64;
import it.unive.lisa.util.datastructures.graph.AdjacencyMatrix;

public class CppFrontend extends CPP14ParserBaseVisitor<Object> {

	public static Program parse(String file) throws IOException {
		CPP14Lexer lexer;
		try (InputStream stream = new FileInputStream(file)) {
			lexer = new CPP14Lexer(CharStreams.fromStream(stream, StandardCharsets.UTF_8));
		}
		CPP14Parser parser = new CPP14Parser(new CommonTokenStream(lexer));

		// this is needed to get an exception on malformed input
		// otherwise an error is dumped to stderr and the partial
		// parsing result is returned
		parser.setErrorHandler(new BailErrorStrategy());
		parser.getInterpreter().setPredictionMode(PredictionMode.SLL);
		parser.setBuildParseTree(true);

		TranslationUnitContext unit = parser.translationUnit();
		Program p = new CppFrontend(file).visitTranslationUnit(unit);

		return p;
	}

	private final String file;

	private Program program;

	private AdjacencyMatrix<Statement, Edge, CFG> matrix;

	private CFG currentCfg;

	private CppFrontend(String file) {
		this.file = file;
	}

	@Override
	public Program visitTranslationUnit(TranslationUnitContext ctx) {
		program = new Program();

		if (ctx.declarationseq() != null)
			visitDeclarationseq(ctx.declarationseq());

		return program;
	}

	@Override
	public Object visitDeclarationseq(DeclarationseqContext ctx) {
		for (DeclarationContext declaration : ctx.declaration())
			visitDeclaration(declaration);
		return null;
	}

	@Override
	public Object visitDeclaration(DeclarationContext ctx) {
		if (ctx.blockDeclaration() != null)
			return visitBlockDeclaration(ctx.blockDeclaration());
		else if (ctx.functionDefinition() != null)
			return visitFunctionDefinition(ctx.functionDefinition());

		throw notSupported(ctx);
	}

	@Override
	public CFG visitFunctionDefinition(FunctionDefinitionContext ctx) {
		if (ctx.declSpecifierSeq() == null)
			throw notSupported(ctx);

		List<Type> decls = visitDeclSpecifierSeq(ctx.declSpecifierSeq());
		Pair<String, Parameter[]> sig = visitDeclarator(ctx.declarator());

		Type returnType = null;
		for (Type decl : decls)
			if (decl instanceof JNIExportType) {
				// this is just to make the parsing work
			} else if (returnType == null)
				returnType = decl;
			else
				notSupported(ctx.declSpecifierSeq());

		CFGDescriptor descriptor = new CFGDescriptor(fromContext(file, ctx), program, false, sig.getLeft(),
				returnType, sig.getRight());

		Collection<Statement> entrypoints = new LinkedList<>();
		matrix = new AdjacencyMatrix<>();
		currentCfg = new CFG(descriptor, entrypoints, matrix);

		Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>,
				Statement> visited = visitFunctionBody(ctx.functionBody());
		entrypoints.add(visited.getLeft());
		matrix.mergeWith(visited.getMiddle());

		if (currentCfg.getAllExitpoints().isEmpty()) {
			Ret ret = new Ret(currentCfg, descriptor.getLocation());
			if (currentCfg.getNodesCount() == 0) {
				// empty method, so the ret is also the entrypoint
				matrix.addNode(ret);
				entrypoints.add(ret);
			} else {
				// every non-throwing instruction that does not have a follower
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

		currentCfg.simplify();

		program.addCFG(currentCfg);

		return currentCfg;
	}

	@Override
	public List<Type> visitDeclSpecifierSeq(DeclSpecifierSeqContext ctx) {
		if (ctx.declSpecifier() == null || ctx.declSpecifier().isEmpty())
			return Collections.emptyList();

		List<Type> result = new ArrayList<>(ctx.declSpecifier().size());
		for (DeclSpecifierContext decl : ctx.declSpecifier())
			result.add(visitDeclSpecifier(decl));

		return result;
	}

	@Override
	public Type visitDeclSpecifier(DeclSpecifierContext ctx) {
		if (ctx.typeSpecifier() != null)
			return visitTypeSpecifier(ctx.typeSpecifier());

		throw notSupported(ctx);
	}

	@Override
	public Type visitTypeSpecifier(TypeSpecifierContext ctx) {
		if (ctx.trailingTypeSpecifier() != null)
			return visitTrailingTypeSpecifier(ctx.trailingTypeSpecifier());

		throw notSupported(ctx);
	}

	@Override
	public Type visitTrailingTypeSpecifier(TrailingTypeSpecifierContext ctx) {
		if (ctx.simpleTypeSpecifier() != null)
			return visitSimpleTypeSpecifier(ctx.simpleTypeSpecifier());

		throw notSupported(ctx);
	}

	@Override
	public Type visitSimpleTypeSpecifier(SimpleTypeSpecifierContext ctx) {
		if (ctx.Void() != null)
			return VoidType.INSTANCE;

		if (ctx.Int() != null)
			return Int32.INSTANCE;

		if (ctx.simpleTypeLengthModifier() != null
				&& ctx.simpleTypeLengthModifier().size() == 1)
			return visitSimpleTypeLengthModifier(ctx.simpleTypeLengthModifier(0));

		if (ctx.theTypeName() != null) {
			String name = visitTheTypeName(ctx.theTypeName());
			if (name.equals("JNIEXPORT") || name.equals("JNICALL"))
				return JNIExportType.INSTANCE;
			else if (name.equals("jint"))
				return Int32.INSTANCE;
			else if (name.equals("jobject"))
				return ClassType.lookup(JavaObject.NAME, null);
			else if (name.equals("jboolean"))
				return BoolType.INSTANCE;
			return ClassType.lookup(name, null);
		}

		throw notSupported(ctx);
	}

	@Override
	public String visitTheTypeName(TheTypeNameContext ctx) {
		if (ctx.className() != null)
			return visitClassName(ctx.className());

		throw notSupported(ctx);
	}

	@Override
	public String visitClassName(ClassNameContext ctx) {
		if (ctx.Identifier() != null)
			return ctx.Identifier().getText();

		throw notSupported(ctx);
	}

	@Override
	public Type visitSimpleTypeLengthModifier(SimpleTypeLengthModifierContext ctx) {
		if (ctx.Long() != null)
			return Int64.INSTANCE;

		throw notSupported(ctx);
	}

	@Override
	public Pair<String, Parameter[]> visitDeclarator(DeclaratorContext ctx) {
		if (ctx.pointerDeclarator() != null)
			return visitPointerDeclarator(ctx.pointerDeclarator());

		throw notSupported(ctx);
	}

	@Override
	public Pair<String, Parameter[]> visitPointerDeclarator(PointerDeclaratorContext ctx) {
		boolean star = false;
		if (ctx.pointerOperator() != null && ctx.pointerOperator().size() == 1 && ctx.pointerOperator(0).Star() != null)
			star = true;
		if (ctx.noPointerDeclarator() != null) {
			Pair<String, Parameter[]> visited = visitNoPointerDeclarator(ctx.noPointerDeclarator());
			if (!star)
				return visited;
			else
				return Pair.of("*" + visited.getLeft(), visited.getRight());
		}

		throw notSupported(ctx);
	}

	@Override
	public Pair<String, Parameter[]> visitNoPointerDeclarator(NoPointerDeclaratorContext ctx) {
		if (ctx.pointerDeclarator() != null)
			return visitPointerDeclarator(ctx.pointerDeclarator());

		if (ctx.declaratorid() == null)
			throw notSupported(ctx);
		String name = visitDeclaratorid(ctx.declaratorid());
		if (ctx.parametersAndQualifiers() == null)
			return Pair.of(name, null);

		Parameter[] formals = visitParametersAndQualifiers(ctx.parametersAndQualifiers());
		return Pair.of(name, formals);
	}

	@Override
	public String visitDeclaratorid(DeclaratoridContext ctx) {
		if (ctx.idExpression() != null)
			return visitIdExpression(ctx.idExpression());

		throw notSupported(ctx);
	}

	@Override
	public String visitIdExpression(IdExpressionContext ctx) {
		if (ctx.unqualifiedId() != null)
			return visitUnqualifiedId(ctx.unqualifiedId());

		throw notSupported(ctx);
	}

	@Override
	public String visitUnqualifiedId(UnqualifiedIdContext ctx) {
		if (ctx.Identifier() != null)
			return ctx.Identifier().getText();

		throw notSupported(ctx);
	}

	@Override
	public Parameter[] visitParametersAndQualifiers(ParametersAndQualifiersContext ctx) {
		if (ctx.parameterDeclarationClause() == null)
			return new Parameter[0];

		return visitParameterDeclarationClause(ctx.parameterDeclarationClause());
	}

	@Override
	public Parameter[] visitParameterDeclarationClause(ParameterDeclarationClauseContext ctx) {
		return visitParameterDeclarationList(ctx.parameterDeclarationList());
	}

	@Override
	public Parameter[] visitParameterDeclarationList(ParameterDeclarationListContext ctx) {
		List<Parameter> formals = new LinkedList<>();
		for (ParameterDeclarationContext formalctx : ctx.parameterDeclaration())
			formals.add(visitParameterDeclaration(formalctx));
		return formals.toArray(Parameter[]::new);
	}

	@Override
	public Parameter visitParameterDeclaration(ParameterDeclarationContext ctx) {
		if (ctx.declarator() == null)
			throw notSupported(ctx);

		String name = visitDeclarator(ctx.declarator()).getLeft();
		List<Type> decls = visitDeclSpecifierSeq(ctx.declSpecifierSeq());
		if (decls.size() != 1)
			throw notSupported(ctx.declSpecifierSeq());

		Type type = decls.iterator().next();
		if (name.startsWith("*") || type.isUnitType()) {
			// pointer
			name = name.substring(1);
			type = new ReferenceType(type);
		}
		return new Parameter(fromContext(file, ctx), name, type);
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitFunctionBody(
			FunctionBodyContext ctx) {
		return visitCompoundStatement(ctx.compoundStatement());
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitCompoundStatement(
			CompoundStatementContext ctx) {
		if (ctx.statementSeq() == null)
			return fromSingle(new NoOp(currentCfg, fromToken(file, ctx.LeftBrace().getSymbol())));
		return visitStatementSeq(ctx.statementSeq());
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitStatementSeq(
			StatementSeqContext ctx) {
		AdjacencyMatrix<Statement, Edge, CFG> block = new AdjacencyMatrix<>();

		Statement first = null, last = null;
		for (int i = 0; i < ctx.statement().size(); i++) {
			Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>,
					Statement> st = visitStatement(ctx.statement(i));
			block.mergeWith(st.getMiddle());
			if (first == null)
				first = st.getLeft();
			if (last != null)
				block.addEdge(new SequentialEdge(last, st.getLeft()));
			last = st.getRight();
		}

		if (first == null && last == null)
			return fromSingle(new NoOp(currentCfg, fromContext(file, ctx)));

		return Triple.of(first, block, last);
	}

	private static void addEdge(Edge e, AdjacencyMatrix<Statement, Edge, CFG> block) {
		if (!e.getSource().stopsExecution())
			block.addEdge(e);
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitStatement(StatementContext ctx) {
		if (ctx.declarationStatement() != null)
			return visitBlockDeclaration(ctx.declarationStatement().blockDeclaration());
		if (ctx.selectionStatement() != null)
			return visitSelectionStatement(ctx.selectionStatement());
		if (ctx.jumpStatement() != null)
			return visitJumpStatement(ctx.jumpStatement());
		if (ctx.iterationStatement() != null)
			return visitIterationStatement(ctx.iterationStatement());
		if (ctx.compoundStatement() != null)
			return visitCompoundStatement(ctx.compoundStatement());

		throw notSupported(ctx);
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitIterationStatement(
			IterationStatementContext ctx) {
		AdjacencyMatrix<Statement, Edge, CFG> block = new AdjacencyMatrix<>();

		if (ctx.While() != null || ctx.Do() != null || ctx.forRangeDeclaration() != null)
			throw notSupported(ctx);

		Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>,
				Statement> init = visitForInitStatement(ctx.forInitStatement());

		Statement condition;
		if (ctx.condition() != null)
			condition = visitCondition(ctx.condition());
		else
			condition = new TrueLiteral(currentCfg, fromToken(file, ctx.For().getSymbol()));

		Statement post;
		if (ctx.expression() != null)
			post = visitExpression(ctx.expression());
		else
			post = new NoOp(currentCfg, fromToken(file, ctx.For().getSymbol()));

		Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> body = visitStatement(ctx.statement());

		NoOp exit = new NoOp(currentCfg, fromToken(file, ctx.RightParen().getSymbol()));

		block.mergeWith(init.getMiddle());
		block.addNode(condition);
		addEdge(new SequentialEdge(init.getRight(), condition), block);
		block.mergeWith(body.getMiddle());
		addEdge(new TrueEdge(condition, body.getLeft()), block);
		block.addNode(post);
		addEdge(new SequentialEdge(body.getRight(), post), block);
		addEdge(new SequentialEdge(post, condition), block);
		block.addNode(exit);
		addEdge(new FalseEdge(condition, exit), block);

		currentCfg
				.addControlFlowStructure(new Loop(matrix, condition, exit, append(body.getMiddle().getNodes(), post)));

		return Triple.of(init.getLeft(), block, exit);
	}

	private static Collection<Statement> append(Collection<Statement> source, Statement extra) {
		Collection<Statement> res = new HashSet<>(source);
		res.add(extra);
		return res;
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitForInitStatement(
			ForInitStatementContext ctx) {
		if (ctx.simpleDeclaration() != null)
			return visitSimpleDeclaration(ctx.simpleDeclaration());
		else
			return visitExpressionStatement(ctx.expressionStatement());
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitExpressionStatement(
			ExpressionStatementContext ctx) {
		if (ctx.expression() != null)
			return fromSingle(visitExpression(ctx.expression()));
		else
			return fromSingle(new NoOp(currentCfg, fromContext(file, ctx)));
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitJumpStatement(
			JumpStatementContext ctx) {
		if (ctx.Return() != null)
			if (ctx.expression() != null)
				return fromSingle(new Return(currentCfg, fromContext(file, ctx), visitExpression(ctx.expression())));
			else
				return fromSingle(new Ret(currentCfg, fromContext(file, ctx)));

		throw notSupported(ctx);
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitBlockDeclaration(
			BlockDeclarationContext ctx) {
		if (ctx.usingDirective() != null) {
			System.out.println("Ignoring '" + ctx.getText() + "'");
			return null;
		}

		if (ctx.simpleDeclaration() != null)
			return visitSimpleDeclaration(ctx.simpleDeclaration());

		throw notSupported(ctx);
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitSimpleDeclaration(
			SimpleDeclarationContext ctx) {
		if (ctx.initDeclaratorList() == null)
			throw notSupported(ctx);

		Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>,
				Statement> list = visitInitDeclaratorList(ctx.initDeclaratorList());
		String declListText = ctx.initDeclaratorList().getText();
		if (ctx.declSpecifierSeq() != null && declListText.startsWith("(") && declListText.endsWith(")")) {
			// this is likely a call that has been parsed ambiguously
			VariableRef variable = (VariableRef) list.getLeft();
			List<Type> seq = visitDeclSpecifierSeq(ctx.declSpecifierSeq());
			if (seq.size() != 1)
				throw notSupported(ctx);

			String name = seq.iterator().next().toString();
			return fromSingle(call(ctx, name, variable));
		}

		return list;
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitInitDeclaratorList(
			InitDeclaratorListContext ctx) {
		if (ctx.initDeclarator() == null || ctx.initDeclarator().size() != 1)
			throw notSupported(ctx);

		return visitInitDeclarator(ctx.initDeclarator(0));
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitInitDeclarator(
			InitDeclaratorContext ctx) {
		String id = visitDeclarator(ctx.declarator()).getLeft();

		VariableRef variable = new VariableRef(currentCfg, fromContext(file, ctx.declarator()), id);
		if (ctx.initializer() == null)
			return fromSingle(variable);

		if (ctx.initializer().expressionList() != null) {
			Expression[] actuals = visitExpressionList(ctx.initializer().expressionList());
			UnresolvedCall call = call(ctx, id, actuals);
			return fromSingle(call);
		} else {
			BraceOrEqualInitializerContext equal = ctx.initializer().braceOrEqualInitializer();
			if (equal.initializerClause() != null) {
				Expression value = visitInitializerClause(equal.initializerClause());
				return fromSingle(new Assignment(currentCfg,
						fromToken(file, equal.Assign().getSymbol()), variable,
						value));
			}
		}

		throw notSupported(ctx);
	}

	private UnresolvedCall call(ParserRuleContext ctx, String target, Expression... actuals) {
		return new UnresolvedCall(
				currentCfg,
				fromContext(file, ctx),
				OrderPreservingAssigningStrategy.INSTANCE,
				RuntimeTypesMatchingStrategy.INSTANCE,
				SingleInheritanceTraversalStrategy.INSTANCE,
				CallType.STATIC,
				null,
				target,
				actuals);
	}

	@Override
	public Expression[] visitExpressionList(ExpressionListContext ctx) {
		return visitInitializerList(ctx.initializerList());
	}

	@Override
	public Expression[] visitInitializerList(InitializerListContext ctx) {
		Expression[] expressions = new Expression[ctx.initializerClause().size()];

		for (int i = 0; i < expressions.length; i++)
			expressions[i] = visitInitializerClause(ctx.initializerClause(i));

		return expressions;
	}

	@Override
	public Expression visitInitializerClause(InitializerClauseContext ctx) {
		if (ctx.assignmentExpression() != null)
			return visitAssignmentExpression(ctx.assignmentExpression());

		throw notSupported(ctx);
	}

	@Override
	public Expression visitAssignmentExpression(AssignmentExpressionContext ctx) {
		if (ctx.conditionalExpression() != null)
			return visitConditionalExpression(ctx.conditionalExpression());
		if (ctx.assignmentOperator() != null) {
			Expression left = visitLogicalOrExpression(ctx.logicalOrExpression());
			Expression right = visitInitializerClause(ctx.initializerClause());
			if (ctx.assignmentOperator().Assign() != null)
				return new Assignment(currentCfg, fromContext(file, ctx.assignmentOperator()), left, right);
			throw notSupported(ctx);
		}

		throw notSupported(ctx);
	}

	@Override
	public Expression visitConditionalExpression(ConditionalExpressionContext ctx) {
		Expression inner = visitLogicalOrExpression(ctx.logicalOrExpression());
		if (ctx.Question() != null)
			throw notSupported(ctx);

		return inner;
	}

	@Override
	public Expression visitLogicalOrExpression(LogicalOrExpressionContext ctx) {
		Expression inner = visitLogicalAndExpression(ctx.logicalAndExpression(0));
		if (ctx.logicalAndExpression().size() == 1)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitLogicalAndExpression(LogicalAndExpressionContext ctx) {
		Expression inner = visitInclusiveOrExpression(ctx.inclusiveOrExpression(0));
		if (ctx.inclusiveOrExpression().size() == 1)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitInclusiveOrExpression(InclusiveOrExpressionContext ctx) {
		Expression inner = visitExclusiveOrExpression(ctx.exclusiveOrExpression(0));
		if (ctx.exclusiveOrExpression().size() == 1)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitExclusiveOrExpression(ExclusiveOrExpressionContext ctx) {
		Expression inner = visitAndExpression(ctx.andExpression(0));
		if (ctx.andExpression().size() == 1)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitAndExpression(AndExpressionContext ctx) {
		Expression inner = visitEqualityExpression(ctx.equalityExpression(0));
		if (ctx.equalityExpression().size() == 1)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitEqualityExpression(EqualityExpressionContext ctx) {
		Expression inner = visitRelationalExpression(ctx.relationalExpression(0));
		int size = ctx.relationalExpression().size();
		if (size == 1)
			return inner;

		for (int i = 1; i < size; i++) {
			EqualityOpContext operator = ctx.equalityOp(i - 1);
			Expression other = visitRelationalExpression(ctx.relationalExpression(i));
			CodeLocation loc = fromContext(file, operator);
			if (operator.Equal() != null)
				inner = new Equal(currentCfg, loc, inner, other);
			else
				inner = new NotEqual(currentCfg, loc, inner, other);
		}

		return inner;
	}

	@Override
	public Expression visitRelationalExpression(RelationalExpressionContext ctx) {
		Expression inner = visitShiftExpression(ctx.shiftExpression(0));
		int size = ctx.shiftExpression().size();
		if (size == 1)
			return inner;

		for (int i = 1; i < size; i++) {
			RelationalOpContext operator = ctx.relationalOp(i - 1);
			Expression other = visitShiftExpression(ctx.shiftExpression(i));
			CodeLocation loc = fromContext(file, operator);
			if (operator.Less() != null)
				inner = new LessThan(currentCfg, loc, inner, other);
			else if (operator.Greater() != null)
				inner = new GreaterThan(currentCfg, loc, inner, other);
			else if (operator.LessEqual() != null)
				inner = new LessOrEqual(currentCfg, loc, inner, other);
			else
				inner = new GreaterOrEqual(currentCfg, loc, inner, other);
		}

		return inner;
	}

	@Override
	public Expression visitShiftExpression(ShiftExpressionContext ctx) {
		Expression inner = visitAdditiveExpression(ctx.additiveExpression(0));
		if (ctx.additiveExpression().size() == 1)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitAdditiveExpression(AdditiveExpressionContext ctx) {
		Expression inner = visitMultiplicativeExpression(ctx.multiplicativeExpression(0));
		int size = ctx.multiplicativeExpression().size();
		if (size == 1)
			return inner;

		for (int i = 1; i < size; i++) {
			AdditiveOpContext operator = ctx.additiveOp(i - 1);
			Expression other = visitMultiplicativeExpression(ctx.multiplicativeExpression(i));
			CodeLocation loc = fromContext(file, operator);
			if (operator.Plus() != null)
				inner = new Addition(currentCfg, loc, inner, other);
			else
				inner = new Subtraction(currentCfg, loc, inner, other);
		}

		return inner;
	}

	@Override
	public Expression visitMultiplicativeExpression(MultiplicativeExpressionContext ctx) {
		Expression inner = visitPointerMemberExpression(ctx.pointerMemberExpression(0));
		int size = ctx.pointerMemberExpression().size();
		if (size == 1)
			return inner;

		for (int i = 1; i < size; i++) {
			MultiplicativeOpContext operator = ctx.multiplicativeOp(i - 1);
			Expression other = visitPointerMemberExpression(ctx.pointerMemberExpression(i));
			CodeLocation loc = fromContext(file, operator);
			if (operator.Star() != null)
				inner = new Multiplication(currentCfg, loc, inner, other);
			else if (operator.Div() != null)
				inner = new Division(currentCfg, loc, inner, other);
			else
				inner = new Remainder(currentCfg, loc, inner, other);
		}

		return inner;
	}

	@Override
	public Expression visitPointerMemberExpression(PointerMemberExpressionContext ctx) {
		Expression inner = visitCastExpression(ctx.castExpression(0));
		if (ctx.castExpression().size() == 1)
			return inner;

		throw notSupported(ctx);
	}

	@Override
	public Expression visitCastExpression(CastExpressionContext ctx) {
		if (ctx.unaryExpression() != null)
			return visitUnaryExpression(ctx.unaryExpression());

		throw notSupported(ctx);
	}

	@Override
	public Expression visitUnaryExpression(UnaryExpressionContext ctx) {
		if (ctx.postfixExpression() != null)
			return visitPostfixExpression(ctx.postfixExpression());

		if (ctx.unaryExpression() != null) {
			Expression inner = visitUnaryExpression(ctx.unaryExpression());
			if (ctx.unaryOperator() != null)
				if (ctx.unaryOperator().Minus() != null)
					return new Negation(currentCfg, fromContext(file, ctx.unaryOperator()), inner);

			throw notSupported(ctx);
		}

		throw notSupported(ctx);
	}

	@Override
	public Expression visitPostfixExpression(PostfixExpressionContext ctx) {
		if (ctx.primaryExpression() != null)
			return visitPrimaryExpression(ctx.primaryExpression());

		if (ctx.postfixExpression() != null) {
			Expression inner = visitPostfixExpression(ctx.postfixExpression());
			if (ctx.LeftParen() != null) {
				// call
				if (!(inner instanceof VariableRef))
					throw notSupported(ctx);

				return call(ctx, ((VariableRef) inner).getName(),
						ctx.expressionList() == null
								? new Expression[0]
								: visitExpressionList(ctx.expressionList()));
			} else if (ctx.PlusPlus() != null) {
				CodeLocation where = fromToken(file, ctx.PlusPlus().getSymbol());
				return new Assignment(currentCfg, where, inner,
						new Addition(currentCfg, where, inner, new Int32Literal(currentCfg, where, 1)));
			} else if (ctx.MinusMinus() != null) {
				CodeLocation where = fromToken(file, ctx.MinusMinus().getSymbol());
				return new Assignment(currentCfg, where, inner,
						new Subtraction(currentCfg, where, inner, new Int32Literal(currentCfg, where, 1)));
			}
		}

		throw notSupported(ctx);
	}

	@Override
	public Expression visitPrimaryExpression(PrimaryExpressionContext ctx) {
		if (ctx.idExpression() != null)
			return new VariableRef(currentCfg, fromContext(file, ctx), visitIdExpression(ctx.idExpression()));

		if (ctx.literal() != null && ctx.literal().size() == 1)
			return visitLiteral(ctx.literal(0));

		if (ctx.expression() != null)
			return visitExpression(ctx.expression());

		throw notSupported(ctx);
	}

	@Override
	public Expression visitLiteral(LiteralContext ctx) {
		if (ctx.IntegerLiteral() != null)
			return new Int32Literal(currentCfg, fromContext(file, ctx),
					Integer.parseInt(ctx.IntegerLiteral().getText()));

		throw notSupported(ctx);
	}

	@Override
	public Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> visitSelectionStatement(
			SelectionStatementContext ctx) {
		if (ctx.If() == null)
			throw notSupported(ctx);

		Expression condition = visitCondition(ctx.condition());

		Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>,
				Statement> trueBlock = visitStatement(ctx.statement(0));

		Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> falseBlock;
		if (ctx.Else() != null)
			falseBlock = visitStatement(ctx.statement(1));
		else
			falseBlock = fromSingle(new NoOp(currentCfg, fromToken(file, ctx.RightParen().getSymbol())));

		AdjacencyMatrix<Statement, Edge, CFG> block = new AdjacencyMatrix<>();
		NoOp exit = new NoOp(currentCfg, fromToken(file, ctx.If().getSymbol()));
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
	public Expression visitCondition(ConditionContext ctx) {
		if (ctx.expression() == null)
			throw notSupported(ctx);

		return visitExpression(ctx.expression());
	}

	@Override
	public Expression visitExpression(ExpressionContext ctx) {
		Expression inner = visitAssignmentExpression(ctx.assignmentExpression(0));
		if (ctx.assignmentExpression().size() == 1)
			return inner;

		throw notSupported(ctx);
	}
}
