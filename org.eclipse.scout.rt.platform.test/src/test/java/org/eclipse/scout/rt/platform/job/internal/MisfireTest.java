/*******************************************************************************
 * Copyright (c) 2010-2017 BSI Business Systems Integration AG.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     BSI Business Systems Integration AG - initial API and implementation
 ******************************************************************************/
package org.eclipse.scout.rt.platform.job.internal;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.eclipse.scout.rt.platform.job.IFuture;
import org.eclipse.scout.rt.platform.job.Jobs;
import org.eclipse.scout.rt.platform.util.SleepUtil;
import org.eclipse.scout.rt.platform.util.concurrent.IRunnable;
import org.eclipse.scout.rt.platform.util.concurrent.TimedOutError;
import org.eclipse.scout.rt.testing.platform.runner.PlatformTestRunner;
import org.eclipse.scout.rt.testing.platform.runner.Times;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.quartz.SimpleScheduleBuilder;

@RunWith(PlatformTestRunner.class)
public class MisfireTest {

  /**
   * Tests that a last round is scheduled upon a 'misfire' with the end-time arrived.
   */
  @Test
  @Times(10) // regression
  public void testFinalRunAfterMisfire() {
    final int endsIn = 20;
    final int schedulingInterval = 1;

    final List<String> protocol = Collections.synchronizedList(new ArrayList<String>()); // synchronized because modified/read by different threads.
    final AtomicInteger roundCounter = new AtomicInteger();

    IFuture<Void> future = Jobs.schedule(new IRunnable() {

      @Override
      public void run() throws Exception {
        int round = roundCounter.incrementAndGet();
        writeProtocol("BEFORE-SLEEP", (JobFutureTask<?>) IFuture.CURRENT.get(), round, protocol);

        if (round == 1) {
          SleepUtil.sleepSafe(endsIn + 50, TimeUnit.MILLISECONDS);
        }
        writeProtocol("AFTER-SLEEP", (JobFutureTask<?>) IFuture.CURRENT.get(), round, protocol);
      }
    }, Jobs.newInput()
        .withExecutionTrigger(Jobs.newExecutionTrigger()
            .withEndIn(endsIn, TimeUnit.MILLISECONDS)
            .withSchedule(SimpleScheduleBuilder.simpleSchedule()
                .withIntervalInMilliseconds(schedulingInterval)
                .repeatForever())));

    // Wait until done
    try {
      future.awaitDone(10, TimeUnit.SECONDS);
    }
    catch (TimedOutError e) {
      future.cancel(true);
      fail("Job is hanging because no last round scheduled upon misfire with end-time arrived");
    }

    // Verify
    List<String> expectedProtocol = new ArrayList<>();
    expectedProtocol.add("BEFORE-SLEEP: has-next-execution [round=1]");
    expectedProtocol.add("AFTER-SLEEP: has-next-execution [round=1]");

    expectedProtocol.add("BEFORE-SLEEP: final-run [round=2]");
    expectedProtocol.add("AFTER-SLEEP: final-run [round=2]");

    assertEquals(expectedProtocol, protocol);
    assertTrue(((JobFutureTask<?>) future).isFinalRun());
    assertFalse(((JobFutureTask<?>) future).hasNextExecution());
  }

  private void writeProtocol(String prefix, JobFutureTask<?> future, int round, List<String> protocol) {
    if (future.isFinalRun()) {
      protocol.add(prefix + ": final-run [round=" + round + "]");
    }

    if (future.hasNextExecution()) {
      protocol.add(prefix + ": has-next-execution [round=" + round + "]");
    }
  }
}
