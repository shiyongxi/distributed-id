package com.yx.distributed.id.core.model;

import lombok.Data;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @Auther: shiyongxi
 * @Date: 2020-05-27 17:09
 * @Description: SequenceRangeBuffer
 */
@Data
public class SequenceRangeBuffer {
    private String bizTag;
    /**
     * 双buffer
     */
    private SequenceRange[] ranges;
    /**
     * 当前使用sequence
     */
    private volatile int currentPos;
    /**
     * 下一个sequence是否已准备完毕
     */
    private volatile boolean nextReady;
    private volatile boolean initOk;
    private final AtomicBoolean threadRunning;
    private final ReadWriteLock lock;

    private volatile int step;
    private volatile int minStep;
    private volatile long updateTimestamp;

    public SequenceRangeBuffer() {
        ranges = new SequenceRange[] {new SequenceRange(this), new SequenceRange(this)};
        currentPos = 0;
        nextReady = false;
        initOk = false;
        threadRunning = new AtomicBoolean(false);
        lock = new ReentrantReadWriteLock();
    }

    public SequenceRange getCurrent() {
        return ranges[currentPos];
    }

    public Lock readLock() {
        return lock.readLock();
    }

    public Lock writeLock() {
        return lock.writeLock();
    }

    public int nextPos() {
        return (currentPos + 1) % 2;
    }

    public void switchPos() {
        currentPos = nextPos();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("SequenceRangeBuffer{");
        sb.append("bizTag='").append(bizTag).append('\'');
        sb.append(", ranges=").append(Arrays.toString(ranges));
        sb.append(", currentPos=").append(currentPos);
        sb.append(", nextReady=").append(nextReady);
        sb.append(", initOk=").append(initOk);
        sb.append(", threadRunning=").append(threadRunning);
        sb.append(", step=").append(step);
        sb.append(", minStep=").append(minStep);
        sb.append(", updateTimestamp=").append(updateTimestamp);
        sb.append('}');
        return sb.toString();
    }
}
