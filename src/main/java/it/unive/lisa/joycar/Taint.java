package it.unive.lisa.joycar;

import it.unive.lisa.analysis.Lattice;
import it.unive.lisa.analysis.SemanticDomain.Satisfiability;
import it.unive.lisa.analysis.SemanticException;
import it.unive.lisa.analysis.nonrelational.value.BaseNonRelationalValueDomain;
import it.unive.lisa.analysis.nonrelational.value.ValueEnvironment;
import it.unive.lisa.analysis.representation.DomainRepresentation;
import it.unive.lisa.analysis.representation.StringRepresentation;
import it.unive.lisa.program.annotations.Annotation;
import it.unive.lisa.program.annotations.Annotations;
import it.unive.lisa.program.annotations.matcher.AnnotationMatcher;
import it.unive.lisa.program.annotations.matcher.BasicAnnotationMatcher;
import it.unive.lisa.program.cfg.ProgramPoint;
import it.unive.lisa.symbolic.SymbolicExpression;
import it.unive.lisa.symbolic.value.BinaryExpression;
import it.unive.lisa.symbolic.value.Constant;
import it.unive.lisa.symbolic.value.Identifier;
import it.unive.lisa.symbolic.value.NullConstant;
import it.unive.lisa.symbolic.value.TernaryExpression;
import it.unive.lisa.symbolic.value.UnaryExpression;
import it.unive.lisa.symbolic.value.ValueExpression;
import it.unive.lisa.symbolic.value.operator.binary.BinaryOperator;
import it.unive.lisa.symbolic.value.operator.binary.LogicalAnd;
import it.unive.lisa.symbolic.value.operator.binary.LogicalOr;
import it.unive.lisa.symbolic.value.operator.ternary.TernaryOperator;
import it.unive.lisa.symbolic.value.operator.unary.LogicalNegation;
import it.unive.lisa.symbolic.value.operator.unary.UnaryOperator;

public class Taint implements BaseNonRelationalValueDomain<Taint> {

	public static final Annotation TAINTED_ANNOTATION = new Annotation("lisa.taint.Tainted");

	private static final AnnotationMatcher TAINTED_MATCHER = new BasicAnnotationMatcher(TAINTED_ANNOTATION);

	public static final Annotation CLEAN_ANNOTATION = new Annotation("lisa.taint.Clean");

	private static final AnnotationMatcher CLEAN_MATCHER = new BasicAnnotationMatcher(CLEAN_ANNOTATION);

	private static final Taint TAINTED = new Taint(true);

	private static final Taint CLEAN = new Taint(false);

	private static final Taint BOTTOM = new Taint(null);

	private final Boolean taint;

	public Taint() {
		this(true);
	}

	private Taint(Boolean taint) {
		this.taint = taint;
	}

	@Override
	public Taint variable(Identifier id, ProgramPoint pp) throws SemanticException {
		Annotations annots = id.getAnnotations();
		if (annots.isEmpty())
			return BaseNonRelationalValueDomain.super.variable(id, pp);

		if (annots.contains(TAINTED_MATCHER))
			return TAINTED;

		if (annots.contains(CLEAN_MATCHER))
			return CLEAN;

		return BaseNonRelationalValueDomain.super.variable(id, pp);
	}

	@Override
	public DomainRepresentation representation() {
		return this == BOTTOM ? Lattice.bottomRepresentation()
				: this == CLEAN ? new StringRepresentation("_") : new StringRepresentation("#");
	}

	@Override
	public Taint top() {
		return TAINTED;
	}

	@Override
	public Taint bottom() {
		return BOTTOM;
	}

	public boolean isTainted() {
		return this == TAINTED;
	}

	@Override
	public Taint evalNullConstant(ProgramPoint pp) throws SemanticException {
		return CLEAN;
	}

	@Override
	public Taint evalNonNullConstant(Constant constant, ProgramPoint pp) throws SemanticException {
		return CLEAN;
	}

	@Override
	public Taint evalUnaryExpression(UnaryOperator operator, Taint arg, ProgramPoint pp) throws SemanticException {
		return arg;
	}

	@Override
	public Taint evalBinaryExpression(BinaryOperator operator, Taint left, Taint right, ProgramPoint pp)
			throws SemanticException {
		return left.lub(right);
	}

	@Override
	public Taint evalTernaryExpression(TernaryOperator operator, Taint left, Taint middle, Taint right, ProgramPoint pp)
			throws SemanticException {
		return left.lub(middle).lub(right);
	}

	@Override
	public boolean tracksIdentifiers(Identifier id) {
		return true;
	}

	@Override
	public boolean canProcess(SymbolicExpression expression) {
		return !expression.getDynamicType().isPointerType();
	}

	@Override
	public Taint lubAux(Taint other) throws SemanticException {
		return TAINTED; // should never happen
	}

	@Override
	public Taint wideningAux(Taint other) throws SemanticException {
		return TAINTED; // should never happen
	}

	@Override
	public boolean lessOrEqualAux(Taint other) throws SemanticException {
		return false; // should never happen
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((taint == null) ? 0 : taint.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Taint other = (Taint) obj;
		if (taint == null) {
			if (other.taint != null)
				return false;
		} else if (!taint.equals(other.taint))
			return false;
		return true;
	}

	@Override
	public Satisfiability satisfies(ValueExpression expression, ValueEnvironment<Taint> environment,
			ProgramPoint pp) throws SemanticException {
		if (expression instanceof Identifier)
			return satisfiesAbstractValue(environment.getState((Identifier) expression), pp);

		if (expression instanceof NullConstant)
			return satisfiesNullConstant(pp);

		if (expression instanceof Constant)
			return satisfiesNonNullConstant((Constant) expression, pp);

		if (expression instanceof UnaryExpression) {
			UnaryExpression unary = (UnaryExpression) expression;

			if (unary.getOperator() == LogicalNegation.INSTANCE)
				return satisfies((ValueExpression) unary.getExpression(), environment, pp).negate();
			else {
				Taint arg = eval((ValueExpression) unary.getExpression(), environment, pp);
//				if (arg.isBottom())
//					return Satisfiability.BOTTOM;

				return satisfiesUnaryExpression(unary.getOperator(), arg, pp);
			}
		}

		if (expression instanceof BinaryExpression) {
			BinaryExpression binary = (BinaryExpression) expression;

			if (binary.getOperator() == LogicalAnd.INSTANCE)
				return satisfies((ValueExpression) binary.getLeft(), environment, pp)
						.and(satisfies((ValueExpression) binary.getRight(), environment, pp));
			else if (binary.getOperator() == LogicalOr.INSTANCE)
				return satisfies((ValueExpression) binary.getLeft(), environment, pp)
						.or(satisfies((ValueExpression) binary.getRight(), environment, pp));
			else {
				Taint left = eval((ValueExpression) binary.getLeft(), environment, pp);
//				if (left.isBottom())
//					return Satisfiability.BOTTOM;

				Taint right = eval((ValueExpression) binary.getRight(), environment, pp);
//				if (right.isBottom())
//					return Satisfiability.BOTTOM;

				return satisfiesBinaryExpression(binary.getOperator(), left, right, pp);
			}
		}

		if (expression instanceof TernaryExpression) {
			TernaryExpression ternary = (TernaryExpression) expression;

			Taint left = eval((ValueExpression) ternary.getLeft(), environment, pp);
//			if (left.isBottom())
//				return Satisfiability.BOTTOM;

			Taint middle = eval((ValueExpression) ternary.getMiddle(), environment, pp);
//			if (middle.isBottom())
//				return Satisfiability.BOTTOM;

			Taint right = eval((ValueExpression) ternary.getRight(), environment, pp);
//			if (right.isBottom())
//				return Satisfiability.BOTTOM;

			return satisfiesTernaryExpression(ternary.getOperator(), left, middle, right, pp);
		}

		return Satisfiability.UNKNOWN;
	}
}
