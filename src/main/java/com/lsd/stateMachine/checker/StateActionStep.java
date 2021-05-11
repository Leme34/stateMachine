package com.lsd.stateMachine.checker;

import com.lsd.stateMachine.context.StateContext;

public interface StateActionStep<T, C> {

    Checkable getCheckable(StateContext<C> context);

}
