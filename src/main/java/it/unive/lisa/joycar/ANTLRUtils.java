package it.unive.lisa.joycar;

import java.util.List;

import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.misc.Utils;
import org.antlr.v4.runtime.tree.Tree;
import org.antlr.v4.runtime.tree.Trees;
import org.apache.commons.lang3.tuple.Triple;

import it.unive.lisa.program.SourceCodeLocation;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.edge.Edge;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.util.datastructures.graph.AdjacencyMatrix;

public class ANTLRUtils {

	static CodeLocation fromContext(String file, ParserRuleContext ctx) {
		return new SourceCodeLocation(file, getLine(ctx), getCol(ctx));
	}

	static CodeLocation fromToken(String file, Token tok) {
		return new SourceCodeLocation(file, getLine(tok), getCol(tok));
	}

	static int getLine(ParserRuleContext ctx) {
		return ctx.getStart().getLine();
	}

	static int getCol(ParserRuleContext ctx) {
		return ctx.getStop().getCharPositionInLine();
	}

	static int getLine(Token tok) {
		return tok.getLine();
	}

	static int getCol(Token tok) {
		return tok.getCharPositionInLine();
	}

	static Triple<Statement, AdjacencyMatrix<Statement, Edge, CFG>, Statement> fromSingle(Statement st) {
		AdjacencyMatrix<Statement, Edge, CFG> block = new AdjacencyMatrix<>();
		block.addNode(st);
		return Triple.of(st, block, st);
	}

	static UnsupportedOperationException notSupported(ParserRuleContext ctx) {
		return new UnsupportedOperationException("Not supported: " + ctx.getText());
	}

	public static final String Eol = System.lineSeparator();
	public static final String Indents = "  ";
	private static int level;

	public static String toPrettyTree(final Tree t, final List<String> ruleNames) {
		// to use this:
		// List<String> ruleNamesList = Arrays.asList(parser.getRuleNames());
		// String prettyTree = ANTLRUtils.toPrettyTree(ctx, ruleNamesList);
		// System.out.println(prettyTree);
		level = 0;
		return process(t, ruleNames).replaceAll("(?m)^\\s+$", "").replaceAll("\\r?\\n\\r?\\n", Eol);
	}

	private static String process(final Tree t, final List<String> ruleNames) {
		if (t.getChildCount() == 0)
			return Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false);
		StringBuilder sb = new StringBuilder();
		sb.append(lead(level));
		level++;
		String s = Utils.escapeWhitespace(Trees.getNodeText(t, ruleNames), false);
		sb.append(s + ' ');
		for (int i = 0; i < t.getChildCount(); i++) {
			sb.append(process(t.getChild(i), ruleNames));
		}
		level--;
		sb.append(lead(level));
		return sb.toString();
	}

	private static String lead(int level) {
		StringBuilder sb = new StringBuilder();
		if (level > 0) {
			sb.append(Eol);
			for (int cnt = 0; cnt < level; cnt++) {
				sb.append(Indents);
			}
		}
		return sb.toString();
	}
}
