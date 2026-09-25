/*******************************************************************************
 * Copyright (c) 2026 Karel Kuhpast and others.
 *
 * This program and the accompanying materials are made
 * available under the terms of the Eclipse Public License 2.0
 * which is available at https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
package org.eclipse.php.internal.core.typeinference.evaluators;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.dltk.ast.references.ConstantReference;
import org.eclipse.dltk.ti.GoalState;
import org.eclipse.dltk.ti.IContext;
import org.eclipse.dltk.ti.goals.GoalEvaluator;
import org.eclipse.dltk.ti.goals.IGoal;
import org.eclipse.dltk.ti.types.IEvaluatedType;
import org.eclipse.php.internal.core.typeinference.PHPTypeInferenceUtils;
import org.eclipse.php.internal.core.typeinference.context.INamespaceContext;
import org.eclipse.php.internal.core.typeinference.goals.ConstantDeclarationGoal;

/**
 * Resolves a bare PHP constant reference to its declaration so its value can
 * participate in normal expression type inference.
 */
public class ConstantReferenceEvaluator extends GoalEvaluator {

	private final ConstantReference constantReference;
	private final List<IEvaluatedType> evaluatedTypes = new LinkedList<>();

	public ConstantReferenceEvaluator(IGoal goal, ConstantReference constantReference) {
		super(goal);
		this.constantReference = constantReference;
	}

	@Override
	public IGoal[] init() {
		IContext context = goal.getContext();
		String constantName = constantReference.getName();
		String namespace = null;
		if (context instanceof INamespaceContext) {
			namespace = ((INamespaceContext) context).getNamespace();
		}

		List<IGoal> subGoals = new LinkedList<>();
		if (namespace != null && !namespace.isEmpty()) {
			subGoals.add(new ConstantDeclarationGoal(context, constantName, namespace));
		}
		subGoals.add(new ConstantDeclarationGoal(context, constantName, null));
		return subGoals.toArray(new IGoal[subGoals.size()]);
	}

	@Override
	public Object produceResult() {
		return PHPTypeInferenceUtils.combineTypes(evaluatedTypes);
	}

	@Override
	public IGoal[] subGoalDone(IGoal subgoal, Object result, GoalState state) {
		if (state != GoalState.RECURSIVE && result instanceof IEvaluatedType) {
			evaluatedTypes.add((IEvaluatedType) result);
		}
		return IGoal.NO_GOALS;
	}
}
