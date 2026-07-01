package SheriffsssPackage.level;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Dedicated thread for the training mode tutorial.
 *
 * Why it lives in its own thread:
 *  - Steps wait for minimum durations without blocking the game loop.
 *    Doing this inside the game loop would pollute update() with time-based state machines.
 *  - Keeps the simulation (game loop) free to run at 60 FPS without spending frames waiting.
 *  - Skip is resolved atomically with interrupt(): no polling in the loop.
 *
 * Threading rules (see THREADING.md):
 *  - The game loop owns ALL world state. This thread does NOT mutate world state.
 *  - Tutorial state is the exclusive property of this thread.
 *  - No shared locks.
 */
public final class TutorialThread extends Thread
{
  private static final String THREAD_NAME = "SheriffsssTutorial";
  private static final long MIN_WAIT_AFTER_MIN_DURATION_MS = 1L;

  private final List<TutorialStep> steps;
  private final AtomicBoolean skipRequested = new AtomicBoolean(false);
  private final AtomicBoolean finished = new AtomicBoolean(false);

  public TutorialThread(List<TutorialStep> steps)
  {
    super(THREAD_NAME);
    setDaemon(true);
    this.steps = steps;
  }

  @Override
  public void run()
  {
    try
    {
      runAllSteps();
    }
    catch (InterruptedException ignored)
    {
      // Skip via interrupt: simply terminate.
    }
    finally
    {
      this.finished.set(true);
    }
  }

  private void runAllSteps() throws InterruptedException
  {
    int total = this.steps.size();
    for (int i = 0; i < total; i++)
    {
      if (this.skipRequested.get())
      {
        return;
      }
      runStep(this.steps.get(i), i + 1, total);
    }
  }

  private void runStep(TutorialStep step, int stepNumber, int totalSteps) throws InterruptedException
  {
    if (step.getMinDurationMs() > 0)
    {
      Thread.sleep(step.getMinDurationMs());
    }
    if (this.skipRequested.get())
    {
      return;
    }
    TutorialEventType expected = step.getTriggerEvent();
    if (expected == null)
    {
      return;
    }
    Thread.sleep(remainingWaitMs(step));
  }

  private static long remainingWaitMs(TutorialStep step)
  {
    return Math.max(MIN_WAIT_AFTER_MIN_DURATION_MS, step.getMaxWaitMs() - step.getMinDurationMs());
  }

  /**
   * Called from the game loop or during shutdown. Idempotent.
   */
  public void skip()
  {
    if (this.skipRequested.compareAndSet(false, true))
    {
      interrupt();
    }
  }
}
