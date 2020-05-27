package com.yx.distributed.id.core.model;

import lombok.Data;

import java.util.concurrent.atomic.AtomicLong;

/**
 * @Auther: shiyongxi
 * @Date: 2020-05-27 17:10
 * @Description: SequenceRange
 */
@Data
public class SequenceRange {
    private AtomicLong value = new AtomicLong(0);
    private volatile long max;
    private volatile int step;
    private SequenceRangeBuffer buffer;

    public SequenceRange(SequenceRangeBuffer buffer) {
        this.buffer = buffer;
    }

    public long getIdle() {
        return this.getMax() - getValue().get();
    }

    @Override
    public String toString() {
        return "SequenceRange{" +
                "value=" + value +
                ", max=" + max +
                ", step=" + step +
                '}';
    }
}
