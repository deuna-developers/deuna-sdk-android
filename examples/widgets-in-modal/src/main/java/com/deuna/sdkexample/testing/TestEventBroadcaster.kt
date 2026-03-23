package com.deuna.sdkexample.testing

import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * Enum defining all test events that can be broadcast from the app to instrumentation tests.
 * Add new events here as needed for different test scenarios.
 */
enum class TestEvent {
    PAYMENT_METHODS_ENTERED,
    PAYMENT_SUCCESS,
    PAYMENT_ERROR,
}

/**
 * A waiter instance for a specific event. Each test gets its own waiter.
 */
class EventWaiter(val event: TestEvent) {
    internal val latch = CountDownLatch(1)

    /**
     * Waits for the event to be broadcast.
     * @param timeoutSeconds The maximum time to wait in seconds.
     * @return true if the event was received, false if timeout occurred.
     */
    fun await(timeoutSeconds: Long = 15): Boolean {
        return latch.await(timeoutSeconds, TimeUnit.SECONDS)
    }
}

/**
 * Helper object for broadcasting test events from the app to instrumentation tests.
 * Thread-safe and supports multiple concurrent tests.
 */
object TestEventBroadcaster {

    private val waiters = CopyOnWriteArrayList<EventWaiter>()

    /**
     * Creates a new waiter for a specific event.
     * Each test should create its own waiter before triggering the action.
     * @param event The test event to wait for.
     * @return An EventWaiter that can be used to wait for the event.
     */
    fun createWaiter(event: TestEvent): EventWaiter {
        val waiter = EventWaiter(event)
        waiters.add(waiter)
        return waiter
    }

    /**
     * Broadcasts a test event, releasing all waiting threads for that event.
     * @param event The test event to broadcast.
     */
    fun broadcast(event: TestEvent) {
        waiters.filter { it.event == event }.forEach { waiter ->
            waiter.latch.countDown()
            waiters.remove(waiter)
        }
    }

    /**
     * Removes a waiter. Call this after waiting or in cleanup.
     */
    fun removeWaiter(waiter: EventWaiter) {
        waiters.remove(waiter)
    }

    /**
     * Clears all registered waiters. Call this in test teardown if needed.
     */
    fun clear() {
        waiters.clear()
    }
}
