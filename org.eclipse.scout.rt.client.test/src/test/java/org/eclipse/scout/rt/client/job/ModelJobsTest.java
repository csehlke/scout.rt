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
package org.eclipse.scout.rt.client.job;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.concurrent.TimeUnit;

import org.eclipse.scout.commons.Assertions.AssertionException;
import org.eclipse.scout.commons.ICallable;
import org.eclipse.scout.commons.IRunnable;
import org.eclipse.scout.commons.exception.ProcessingException;
import org.eclipse.scout.rt.client.IClientSession;
import org.eclipse.scout.rt.client.context.ClientRunContext;
import org.eclipse.scout.rt.platform.context.RunContext;
import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.JobInput;
import org.eclipse.scout.rt.shared.ISession;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

@RunWith(PlatformTestRunner.class)
public class ModelJobsTest {

  @Mock
  private IClientSession m_clientSession;

  @Before
  public void before() {
    MockitoAnnotations.initMocks(this);
  }

  @Test
  public void testIsModelJob() {
    assertFalse(ModelJobs.isModelJob(null));

    IFuture<?> future = mock(IFuture.class);
    when(future.getJobInput()).thenReturn(new JobInput());
    assertFalse(ModelJobs.isModelJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new RunContext()));
    assertFalse(ModelJobs.isModelJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ClientRunContext()));
    assertFalse(ModelJobs.isModelJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ClientRunContext()).mutex(new Object()));
    assertFalse(ModelJobs.isModelJob(future));

    when(future.getJobInput()).thenReturn(new JobInput().runContext(new ClientRunContext()).mutex(mock(IClientSession.class)));
    assertTrue(ModelJobs.isModelJob(future));
  }

  @Test
  public void testScheduleWithoutInput() throws ProcessingException {
    ISession.CURRENT.set(m_clientSession);

    // Test schedule
    IFuture<?> actualFuture = ModelJobs.schedule(new ICallable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }).awaitDoneAndGet();

    assertTrue(ModelJobs.isModelJob(actualFuture));
    assertFalse(ClientJobs.isClientJob(actualFuture));

    // Test schedule with delay
    actualFuture = ModelJobs.schedule(new ICallable<IFuture<?>>() {

      @Override
      public IFuture<?> call() throws Exception {
        return IFuture.CURRENT.get();
      }
    }, 0, TimeUnit.MILLISECONDS).awaitDoneAndGet();

    assertTrue(ModelJobs.isModelJob(actualFuture));
    assertFalse(ClientJobs.isClientJob(actualFuture));
  }

  @Test(expected = AssertionException.class)
  public void testScheduleWithoutInputWithoutSession() throws ProcessingException {
    ISession.CURRENT.set(null);
    ModelJobs.schedule(mock(IRunnable.class));
  }
}
