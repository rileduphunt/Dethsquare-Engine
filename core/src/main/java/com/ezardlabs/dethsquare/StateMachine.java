package com.ezardlabs.dethsquare;

import java.util.ArrayList;
import java.util.HashMap;

public class StateMachine<T extends Enum> {
	private final HashMap<T, Transition<T>[]> stateTransitions = new HashMap<>();
	private final ArrayList<Transition<T>> anyTransitions = new ArrayList<>();
	private T state;

	public void init(T initialState) {
		state = initialState;
	}

	@SafeVarargs
	public final void addState(T name, Transition<T>... transitions) {
		stateTransitions.put(name, transitions);
	}

	public final void addTransitionFromAnyState(Transition<T> transition) {
		anyTransitions.add(transition);
	}

	public void update() {
		for (Transition<T> transition : anyTransitions) {
			if (transition.getTargetState() != state && transition.isValid()) {
				state = transition.execute();
				return;
			}
		}
		for (Transition<T> transition : stateTransitions.get(state)) {
			if (transition.isValid()) {
				state = transition.execute();
				return;
			}
		}
	}

	public T getState() {
		return state;
	}

	public void setState(T state) {
		this.state = state;
	}

	public static class Transition<T> {
		private final T targetState;
		private final Condition condition;
		private final Action action;

		public Transition(T targetState, Condition condition) {
			this(targetState, condition, null);
		}

		public Transition(T targetState, Condition condition, Action action) {
			this.targetState = targetState;
			this.condition = condition;
			this.action = action;
		}

		T getTargetState() {
			return targetState;
		}

		boolean isValid() {
			return condition.check();
		}

		T execute() {
			if (action != null) {
				action.run();
			}
			return targetState;
		}
	}

	public interface Condition {
		boolean check();
	}

	public interface Action {
		void run();
	}
}
