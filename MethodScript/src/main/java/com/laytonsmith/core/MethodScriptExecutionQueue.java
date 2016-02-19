
package com.laytonsmith.core;

import com.laytonsmith.PureUtilities.ExecutionQueue;
import com.laytonsmith.core.constructs.CString;
import com.laytonsmith.core.constructs.CVoid;
import com.laytonsmith.core.constructs.Target;
import com.laytonsmith.core.environments.Environment;
import com.laytonsmith.core.environments.GlobalEnv;
import com.laytonsmith.core.exceptions.CancelCommandException;
import com.laytonsmith.core.exceptions.ConfigRuntimeException;
import com.laytonsmith.core.exceptions.FunctionReturnException;
import com.laytonsmith.core.functions.Echoes;

/**
 * A subclass of ExecutionQueue, which knows how to handle uncaught exceptions
 * in a MethodScript specific way.
 * 
 */
public class MethodScriptExecutionQueue extends ExecutionQueue {
	
	GlobalEnv env;
	public MethodScriptExecutionQueue(String threadPrefix, String defaultQueueName) {
		super(threadPrefix, defaultQueueName, null);
	}
	
	public void setEnvironment(GlobalEnv env){
		this.env = env;
		super.setUncaughtExceptionHandler(getExceptionHandler());
	}
	
	private Thread.UncaughtExceptionHandler getExceptionHandler(){
		Thread.UncaughtExceptionHandler uceh = new Thread.UncaughtExceptionHandler() {

			@Override
			public void uncaughtException(Thread t, Throwable e) {
				Environment env = Environment.createEnvironment(MethodScriptExecutionQueue.this.env);
				if(e instanceof ConfigRuntimeException){
					//This should be handled by the default UEH
					ConfigRuntimeException.HandleUncaughtException(((ConfigRuntimeException)e), env);
				} else if(e instanceof FunctionReturnException){
					//If they return void, fine, but if they return any other value, it will be
					//ignored, so we want to warn them, but not trigger a flat out error.
					if(!(((FunctionReturnException)e).getReturn() instanceof CVoid)){
						ConfigRuntimeException.DoWarning("Closure is returning a value in an execution queue task,"
								+ " which is unexpected behavior. It may return void however, which will"
								+ " simply stop that one task. " + ((FunctionReturnException)e).getTarget().toString());
					}
				} else if(e instanceof CancelCommandException){
					//Ok. If there's a message, echo it to console.
					String msg = ((CancelCommandException)e).getMessage().trim();
					if(!"".equals(msg)){
						Target tt = ((CancelCommandException)e).getTarget();
						new Echoes.console().exec(tt, env, new CString(msg, tt));
					}
				} else {
					//Well, we tried to deal with it, but this is beyond our ability to
					//handle, so let it bubble up further.
					throw new RuntimeException(e);
				}
			}
		};
		return uceh;
	}
	
}
