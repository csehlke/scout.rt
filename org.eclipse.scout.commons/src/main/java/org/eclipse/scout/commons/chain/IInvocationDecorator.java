/*******************************************************************************
 * Copyright (c) 2015 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.commons.chain;

import java.util.concurrent.Callable;

/**
 * A <code>Decorator</code> is an object to be used in {@link InvocationChain} to decorate the execution of a
 * {@link Callable} with some <i>before</i>- and <i>after</i> actions, e.g. to log execution or to set some
 * thread-locals.
 *
 * @see IInvocationInterceptor
 * @since 5.2
 */
public interface IInvocationDecorator<RESULT> extends IChainable {

  /**
   * Method invoked prior to executing the {@link Callable command}.
   *
   * @return the {@link IUndecorator} to be invoked after execution in order to revert decoration, or <code>null</code>
   *         to do nothing upon return of the command.
   * @throws Exception
   *           throw exception to stop chain processing. The exception is propagated to the caller.
   */
  IUndecorator<RESULT> decorate() throws Exception;

  /**
   * Undecorator to roll back decoration.
   */
  public interface IUndecorator<RESULT> {

    /**
     * Method invoked after executed the {@link Callable}, and is invoked regardless of success or failure.
     *
     * @param invocationResult
     *          the result returned by the {@link Callable}, or <code>null</code> if returned with an exception, or if
     *          returned <code>null</code> as the result.
     * @param invocationException
     *          the exception if the callable returned with an exception.
     */
    void undecorate(RESULT invocationResult, Throwable invocationException);
  }
}
