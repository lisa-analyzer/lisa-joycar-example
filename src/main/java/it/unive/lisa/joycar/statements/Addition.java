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
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.operator.binary.NumericNonOverflowingAdd;
import it.unive.lisa.type.Type;

public class Addition extends it.unive.lisa.program.cfg.statement.BinaryExpression {

	public Addition(CFG cfg, CodeLocation location, Expression left, Expression right) {
		super(cfg, location, "+", left, right);
	}

	@Override
	protected <A extends AbstractState<A, H, V, T>,
			H extends HeapDomain<H>,
			V extends ValueDomain<V>,
			T extends TypeDomain<T>> AnalysisState<A, H, V, T> binarySemantics(
					InterproceduralAnalysis<A, H, V, T> interprocedural,
					AnalysisState<A, H, V, T> state,
					SymbolicExpression left,
					SymbolicExpression right,
					StatementStore<A, H, V, T> expressions)
					throws SemanticException {
		// we allow untyped for the type inference phase
		if (left.getRuntimeTypes().noneMatch(Type::isNumericType)
				&& left.getRuntimeTypes().noneMatch(Type::isUntyped))
			return state.bottom();
		if (right.getRuntimeTypes().noneMatch(Type::isNumericType)
				&& right.getRuntimeTypes().noneMatch(Type::isUntyped))
			return state.bottom();

		return state.smallStepSemantics(
				new BinaryExpression(
						getStaticType(),
						left,
						right,
						NumericNonOverflowingAdd.INSTANCE,
						getLocation()),
				this);
	}
}
