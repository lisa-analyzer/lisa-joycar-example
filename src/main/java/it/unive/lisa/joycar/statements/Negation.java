package it.unive.lisa.joycar.statements;

import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.StatementStore;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.value.TypeDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.interprocedural.InterproceduralAnalysis;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.operator.unary.NumericNegation;
import it.unive.lisa.type.TypeSystem;

public class Negation extends it.unive.lisa.program.cfg.statement.UnaryExpression {

	public Negation(CFG cfg, CodeLocation location, Expression expression) {
		super(cfg, location, "-", expression);
	}

	@Override
	public <A extends AbstractState<A, H, V, T>,
			H extends HeapDomain<H>,
			V extends ValueDomain<V>,
			T extends TypeDomain<T>> AnalysisState<A, H, V, T> unarySemantics(
					InterproceduralAnalysis<A, H, V, T> interprocedural,
					AnalysisState<A, H, V, T> state,
					SymbolicExpression expr,
					StatementStore<A, H, V, T> expressions)
					throws SemanticException {
		TypeSystem types = getProgram().getTypes();
		// we allow untyped for the type inference phase
		if (expr.getRuntimeTypes(types).stream().noneMatch(t -> t.isNumericType() || t.isUntyped()))
			return state.bottom();

		return state.smallStepSemantics(
				new UnaryExpression(
						expr.getStaticType(),
						expr,
						NumericNegation.INSTANCE,
						getLocation()),
				this);
	}
}
