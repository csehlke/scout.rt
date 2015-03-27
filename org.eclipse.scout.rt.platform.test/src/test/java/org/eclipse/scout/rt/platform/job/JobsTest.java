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
package org.eclipse.scout.rt.platform.job;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.commons.holders.Holder;
import org.eclipse.scout.commons.nls.NlsLocale;
import org.eclipse.scout.rt.platform.context.RunContexts;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(PlatformTestRunner.class)
public class JobsTest {

  @Test
  public void testScheduleWithoutInput() throws ProcessingException {
    NlsLocale.set(Locale.CANADA_FRENCH);

    // Test schedule
    IFuture<?> actualFuture = Jobs.schedule(new ICallable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }).awaitDoneAndGet();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());

    // schedule with delay
    actualFuture = Jobs.schedule(new ICallable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, 0, TimeUnit.MILLISECONDS).awaitDoneAndGet();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());

    // schedule at fixed rate
    final Holder<IFuture<?>> actualFutureHolder = new Holder<IFuture<?>>();
    Jobs.scheduleAtFixedRate(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, 0, 0, TimeUnit.MILLISECONDS, Jobs.newInput(RunContexts.copyCurrent())).awaitDoneAndGet();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());

    // schedule with fixed delay
    actualFutureHolder.setValue(null);
    Jobs.scheduleWithFixedDelay(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, 0, 0, TimeUnit.MILLISECONDS, Jobs.newInput(RunContexts.copyCurrent())).awaitDoneAndGet();

    assertEquals(Locale.CANADA_FRENCH, actualFuture.getJobInput().getRunContext().getLocale());
  }

  @Test(expected = AssertionException.class)
  public void testScheduleWithoutRunContext() throws ProcessingException {
    NlsLocale.set(Locale.CANADA_FRENCH);

    // Test schedule
    IFuture<?> actualFuture = Jobs.schedule(new ICallable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, Jobs.newInput(null)).awaitDoneAndGet();

    assertNull(actualFuture.getJobInput().getRunContext().getLocale());

    // schedule with delay
    actualFuture = Jobs.schedule(new ICallable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, 0, TimeUnit.MILLISECONDS, Jobs.newInput(null)).awaitDoneAndGet();

    assertNull(actualFuture.getJobInput().getRunContext().getLocale());

    // schedule at fixed rate
    final Holder<IFuture<?>> actualFutureHolder = new Holder<IFuture<?>>();
    Jobs.scheduleAtFixedRate(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, 0, 0, TimeUnit.MILLISECONDS, Jobs.newInput(null)).awaitDoneAndGet();

    assertNull(actualFuture.getJobInput().getRunContext().getLocale());

    // schedule with fixed delay
    actualFutureHolder.setValue(null);
    Jobs.scheduleWithFixedDelay(new IRunnable() {

      @Override
      public void run() throws Exception {
        actualFutureHolder.setValue(IFuture.CURRENT.get());
        IFuture.CURRENT.get().cancel(false); // cancel periodic action
      }
    }, 0, 0, TimeUnit.MILLISECONDS, Jobs.newInput(null)).awaitDoneAndGet();

    assertNull(actualFuture.getJobInput().getRunContext().getLocale());
  }
}
