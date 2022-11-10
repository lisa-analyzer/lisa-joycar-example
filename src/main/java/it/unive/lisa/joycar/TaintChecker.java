package it.unive.lisa.joycar;

import it.unive.lisa.analysis.CFGWithAnalysisResults;
import it.unive.lisa.analysis.SimpleAbstractState;
import it.unive.lisa.analysis.heap.MonolithicHeap;
import it.unive.lisa.analysis.nonrelational.value.TypeEnvironment;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.types.InferredTypes;
import it.unive.lisa.checks.semantic.CheckToolWithAnalysisResults;
import it.unive.lisa.checks.semantic.SemanticCheck;
import it.unive.lisa.program.Global;
import it.unive.lisa.program.Unit;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.Parameter;
import it.unive.lisa.program.cfg.edge.Edge;
import it.unive.lisa.program.cfg.statement.Statement;
import it.unive.lisa.program.cfg.statement.call.Call;

public class TaintChecker implements
		SemanticCheck<SimpleAbstractState<MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>>,
				MonolithicHeap,
				ValueEnvironment<Taint>,
				TypeEnvironment<InferredTypes>> {

	public static final Annotation SINK_ANNOTATION = new Annotation("lisa.taint.Sink");
	public static final AnnotationMatcher SINK_MATCHER = new BasicAnnotationMatcher(SINK_ANNOTATION);

	private static final String[] suffixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th",
			"th" };

	public static String ordinal(int i) {
		switch (i % 100) {
		case 11:
		case 12:
		case 13:
			return i + "th";
		default:
			return i + suffixes[i % 10];

		}
	}

	@Override
	public void beforeExecution(CheckToolWithAnalysisResults<
			SimpleAbstractState<MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>>, MonolithicHeap,
			ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>> tool) {
	}

	@Override
	public void afterExecution(CheckToolWithAnalysisResults<
			SimpleAbstractState<MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>>, MonolithicHeap,
			ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>> tool) {
	}

	@Override
	public boolean visitUnit(
			CheckToolWithAnalysisResults<
					SimpleAbstractState<MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>>,
					MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>> tool,
			Unit unit) {
		return true;
	}

	@Override
	public void visitGlobal(
			CheckToolWithAnalysisResults<
					SimpleAbstractState<MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>>,
					MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>> tool,
			Unit unit, Global global, boolean instance) {
	}

	@Override
	public boolean visit(CheckToolWithAnalysisResults<
			SimpleAbstractState<MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>>, MonolithicHeap,
			ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>> tool, CFG graph) {
		Parameter[] parameters = graph.getDescriptor().getFormals();
		for (int i = 0; i < parameters.length; i++)
			if (parameters[i].getAnnotations().contains(SINK_MATCHER))
				for (Call call : tool.getCallSites(graph))
					for (CFGWithAnalysisResults<
							SimpleAbstractState<MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>>,
							MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>> result : tool
									.getResultOf(call.getCFG()))
						if (result.getAnalysisStateAfter(call.getParameters()[i]).getState().getValueState()
								.getValueOnStack().isTainted())
							tool.warnOn(call, "The value passed for the " + ordinal(i + 1)
									+ " parameter of this call is tainted, and it reaches the sink at parameter '"
									+ parameters[i].getName() + "' of " + graph.getDescriptor().getFullName());

		return true;
	}

	@Override
	public boolean visit(
			CheckToolWithAnalysisResults<
					SimpleAbstractState<MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>>,
					MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>> tool,
			CFG graph, Statement node) {
		return true;
	}

	@Override
	public boolean visit(
			CheckToolWithAnalysisResults<
					SimpleAbstractState<MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>>,
					MonolithicHeap, ValueEnvironment<Taint>, TypeEnvironment<InferredTypes>> tool,
			CFG graph, Edge edge) {
		return true;
	}
}
