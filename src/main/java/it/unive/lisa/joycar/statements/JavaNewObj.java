package it.unive.lisa.joycar.statements;

import java.util.Collections;

import org.apache.commons.lang3.ArrayUtils;

import it.unive.lisa.analysis.AbstractState;
import it.unive.lisa.analysis.AnalysisState;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.StatementStore;
import it.unive.lisa.analysis.heap.HeapDomain;
import it.unive.lisa.analysis.lattices.ExpressionSet;
import it.unive.lisa.analysis.value.TypeDomain;
import it.unive.lisa.analysis.value.ValueDomain;
import it.unive.lisa.interprocedural.InterproceduralAnalysis;
import it.unive.lisa.program.cfg.CFG;
import it.unive.lisa.program.cfg.CodeLocation;
import it.unive.lisa.program.cfg.statement.Expression;
import it.unive.lisa.program.cfg.statement.NaryExpression;
import it.unive.lisa.program.cfg.statement.VariableRef;
import it.unive.lisa.program.cfg.statement.call.Call.CallType;
import it.unive.lisa.program.cfg.statement.call.UnresolvedCall;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.heap.HeapReference;
import it.unive.lisa.symbolic.heap.MemoryAllocation;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.type.ReferenceType;
import it.unive.lisa.type.Type;

public class JavaNewObj extends NaryExpression {

	public JavaNewObj(CFG cfg, CodeLocation location, Type type, Expression... parameters) {
		super(cfg, location, "new " + type, type, parameters);
	}

	@Override
	public <A extends AbstractState<A, H, V, T>,
			H extends HeapDomain<H>,
			V extends ValueDomain<V>,
			T extends TypeDomain<T>> AnalysisState<A, H, V, T> expressionSemantics(
					InterproceduralAnalysis<A, H, V, T> interprocedural,
					AnalysisState<A, H, V, T> state,
					ExpressionSet<SymbolicExpression>[] params,
					StatementStore<A, H, V, T> expressions)
					throws SemanticException {
		Type type = getStaticType();
		ReferenceType reftype = new ReferenceType(type);
		CodeLocation location = getLocation();

		MemoryAllocation created = new MemoryAllocation(type, location);
		HeapReference ref = new HeapReference(reftype, created, location);
		created.setRuntimeTypes(Collections.singleton(type));
		ref.setRuntimeTypes(Collections.singleton(reftype));

		// we need to add the receiver to the parameters
		VariableRef paramThis = new VariableRef(getCFG(), location, "$lisareceiver", type);
		Expression[] fullExpressions = ArrayUtils.insert(0, getSubExpressions(), paramThis);
		ExpressionSet<SymbolicExpression>[] fullParams = ArrayUtils.insert(0, params, new ExpressionSet<>(created));

		// we also have to add the receiver inside the state
		AnalysisState<A, H, V, T> callstate = paramThis.semantics(state, interprocedural, expressions);
		AnalysisState<A, H, V, T> tmp = state.bottom();
		for (SymbolicExpression v : callstate.getComputedExpressions())
			tmp = tmp.lub(callstate.assign(v, ref, paramThis));
		expressions.put(paramThis, tmp);

		UnresolvedCall call = new UnresolvedCall(
				getCFG(),
				location,
				CallType.INSTANCE,
				type.toString(),
				type.toString(),
				fullExpressions);
		AnalysisState<A, H, V, T> sem = call.expressionSemantics(interprocedural, tmp, fullParams, expressions);

		if (!call.getMetaVariables().isEmpty())
			sem = sem.forgetIdentifiers(call.getMetaVariables());

		// now remove the instrumented receiver
		expressions.forget(paramThis);
		for (SymbolicExpression v : callstate.getComputedExpressions())
			if (v instanceof Identifier)
				sem = sem.forgetIdentifier((Identifier) v);

		sem = sem.smallStepSemantics(created, this);

		AnalysisState<A, H, V, T> result = state.bottom();
		for (SymbolicExpression loc : sem.getComputedExpressions())
			result = result.lub(sem.smallStepSemantics(new HeapReference(reftype, loc, location), call));

		return result;
	}
}
