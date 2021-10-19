package it.lucaneg.lisa.joycar.java.statements;

import org.apache.commons.lang3.ArrayUtils;

import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.lattices.ExpressionSet;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.interprocedural.InterproceduralAnalysis;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.call.NativeCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall.ResolutionStrategy;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.heap.HeapAllocation;
import it.unive.lisa.symbolic.heap.HeapReference;
import it.unive.lisa.type.Type;

public class NewObj extends NativeCall {

	public NewObj(CFG cfg, CodeLocation location, Type type, Expression... parameters) {
		super(cfg, location, "new " + type, type, parameters);
	}

	@Override
	public <A extends AbstractState<A, H, V>,
			H extends HeapDomain<H>,
			V extends ValueDomain<V>> AnalysisState<A, H, V> callSemantics(
					AnalysisState<A, H, V> entryState, 
					InterproceduralAnalysis<A, H, V> interprocedural,
					AnalysisState<A, H, V>[] computedStates,
					ExpressionSet<SymbolicExpression>[] params)
					throws SemanticException {
		HeapAllocation created = new HeapAllocation(getRuntimeTypes(), getLocation());

		// we need to add the receiver to the parameters
		VariableRef paramThis = new VariableRef(getCFG(), getLocation(), "this",
				getStaticType());
		Expression[] fullExpressions = ArrayUtils.insert(0, getParameters(), paramThis);
		ExpressionSet<SymbolicExpression>[] fullParams = ArrayUtils.insert(0, params, new ExpressionSet<>(created));

		UnresolvedCall call = new UnresolvedCall(getCFG(), getLocation(),
				ResolutionStrategy.FIRST_DYNAMIC_THEN_STATIC, 
				true, getStaticType().toString(), fullExpressions);
		call.inheritRuntimeTypesFrom(this);
		AnalysisState<A, H, V> sem = call.callSemantics(entryState, interprocedural, computedStates, fullParams);

		if (!call.getMetaVariables().isEmpty())
			sem = sem.forgetIdentifiers(call.getMetaVariables());

		sem = sem.smallStepSemantics(created, this);

		AnalysisState<A, H, V> result = entryState.bottom();
		for (SymbolicExpression loc : sem.getComputedExpressions())
			result = result.lub(sem.smallStepSemantics(new HeapReference(loc.getTypes(), loc, getLocation()), call));

		return result;
	}
}

