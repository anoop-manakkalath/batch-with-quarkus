package com.example.batch.partition;

import jakarta.enterprise.context.ApplicationScoped;
import lombok.extern.jbosslog.JBossLog;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@ApplicationScoped
@JBossLog
public class PartitionStepBarrier {

    private final Lock lock;
    private final Condition turnCondition;
    private final AtomicInteger currentTurn;

    public PartitionStepBarrier() {
        this.lock = new ReentrantLock();
        this.turnCondition = this.lock.newCondition();
        this.currentTurn = new AtomicInteger(0);
    }

    /**
     * Blocks execution until the previous partition has completed its ENTIRE file segment.
     * Guarantees strictly one partition active in memory at a time.
     */
    public void awaitTurn(int partitionId) {
        lock.lock();
        try {
            while (currentTurn.get() != partitionId) {
                turnCondition.awaitUninterruptibly();
            }
            log.infof("Partition %d UNLOCKED: Starting execution", partitionId);
        } finally {
            lock.unlock();
        }
    }

    /**
     * Signals that the partition has completely finished all its chunks.
     * Unlocks Partition N + 1.
     */
    public void completeTurn(int partitionId) {
        lock.lock();
        try {
            var nextTurn = currentTurn.incrementAndGet();
            log.infof("Partition %d FINISHED COMPLETELY. Unlocking Partition %d", partitionId, nextTurn);
            turnCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void deregister(int partitionId) {
        lock.lock();
        try {
            var nextTurn = currentTurn.incrementAndGet();
            log.warnf("Partition %d failed! Skipping turn to %d to prevent deadlock.", partitionId, nextTurn);
            turnCondition.signalAll();
        } finally {
            lock.unlock();
        }
    }

    public void reset() {
        lock.lock();
        try {
            currentTurn.set(0);
            turnCondition.signalAll();
            log.info("PartitionStepBarrier turn counter reset to 0.");
        } finally {
            lock.unlock();
        }
    }
}
