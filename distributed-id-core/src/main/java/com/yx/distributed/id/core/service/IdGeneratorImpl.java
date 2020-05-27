package com.yx.distributed.id.core.service;

import com.yx.distributed.id.core.mapper.SequenceMapper;
import com.yx.distributed.id.core.model.Sequence;
import com.yx.distributed.id.core.model.SequenceRange;
import com.yx.distributed.id.core.model.SequenceRangeBuffer;
import com.yx.distributed.id.dto.GenerateResult;
import com.yx.distributed.id.dto.GenerateStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @Auther: shiyongxi
 * @Date: 2020-05-27 17:07
 * @Description: IdGeneratorImpl
 */
@Slf4j
@Service
public class IdGeneratorImpl implements IdGenerator {

    @Autowired
    private SequenceMapper sequenceMapper;

    /**
     * IDCache未初始化成功时的异常码
     */
    private static final long EXCEPTION_ID_INIT_FALSE = -1;
    /**
     * key不存在时的异常码
     */
    private static final long EXCEPTION_ID_KEY_NOT_EXISTS = -2;
    /**
     * SegmentBuffer中的两个Segment均未从DB中装载时的异常码
     */
    private static final long EXCEPTION_ID_TWO_SEQUENCE_ARE_NULL = -3;
    /**
     * 最大步长不超过100,0000
     */
    private static final int MAX_STEP = 1000000;
    /**
     * 一个Sequence维持时间为15分钟
     */
    private static final long SEGMENT_DURATION = 15 * 60 * 1000L;
    private ExecutorService service =
            new ThreadPoolExecutor(5, Integer.MAX_VALUE, 60L, TimeUnit.SECONDS, new SynchronousQueue<Runnable>(),
                    new UpdateThreadFactory());

    private final Lock lock = new ReentrantLock();

    private final long lockTimeout = 5l;
    /**
     * 获取sequence重试次数
     */
    private final int retryTimes = 10;

    private Map<String, SequenceRangeBuffer> cacheMap = new ConcurrentHashMap<>();

    public static class UpdateThreadFactory implements ThreadFactory {

        private static AtomicInteger threadInitNumber = new AtomicInteger(0);

        @Override
        public Thread newThread(Runnable r) {
            return new Thread(r, "thread-sequence-update-" + threadInitNumber.getAndIncrement());
        }
    }

    @Override
    public GenerateResult get(String bizTag) {
        if (StringUtils.isEmpty(bizTag)) {
            return new GenerateResult(EXCEPTION_ID_KEY_NOT_EXISTS, GenerateStatus.EXCEPTION);
        }

        //1. 查看缓存map中是否有bizTag对应buffer，无则初始化
        SequenceRangeBuffer buffer = null;
        if ((buffer = cacheMap.get(bizTag)) == null) {
            try {
                if (lock.tryLock(lockTimeout, TimeUnit.SECONDS)) {
                    if ((buffer = cacheMap.get(bizTag)) == null) {
                        buffer = new SequenceRangeBuffer();
                        buffer.setBizTag(bizTag);
                        SequenceRange range = buffer.getCurrent();
                        range.setValue(new AtomicLong(0));
                        range.setMax(0);
                        cacheMap.put(bizTag, buffer);
                    }
                }
            } catch (InterruptedException e) {
                log.error("try lock timeout when try to load sequence from db", e);
                return new GenerateResult(EXCEPTION_ID_INIT_FALSE, GenerateStatus.EXCEPTION);
            } finally {
                lock.unlock();
            }
        }
        if (buffer == null) {
            return new GenerateResult(EXCEPTION_ID_INIT_FALSE, GenerateStatus.EXCEPTION);
        }
        //2. 从区段获取id
        if (!buffer.isInitOk()) {
            synchronized (buffer) {
                if (!buffer.isInitOk()) {
                    try {
                        updateSequenceFromDb(bizTag, buffer.getCurrent());
                        log.info("init buffer. update sequence {} {} from db", bizTag, buffer.getCurrent());
                        buffer.setInitOk(true);
                    } catch (Exception e) {
                        log.warn("init buffer {} exception", buffer.getCurrent(), e);
                    }
                }
            }
        }
        return getIdFromSequenceBuffer(buffer);
    }

    private void updateSequenceFromDb(String bizTag, SequenceRange sequenceRange) {
        for (int i = 0; i < retryTimes; i++) {
            log.info("update sequence from db, bizTag: {}", bizTag);
            SequenceRangeBuffer buffer = sequenceRange.getBuffer();
            Sequence sequence;
            if (!buffer.isInitOk()) {
                sequence = update(bizTag);
                if (sequence != null) {
                    buffer.setStep(sequence.getStep());
                    buffer.setMinStep(sequence.getStep());
                }
            } else if (buffer.getUpdateTimestamp() == 0) {
                sequence = update(bizTag);
                if (sequence != null) {
                    buffer.setUpdateTimestamp(System.currentTimeMillis());
                    buffer.setStep(sequence.getStep());
                    buffer.setMinStep(sequence.getStep());
                }
            } else {
                long duration = System.currentTimeMillis() - buffer.getUpdateTimestamp();
                int nextStep = buffer.getStep();
                if (duration < SEGMENT_DURATION) {
                    if (nextStep * 2 > MAX_STEP) {
                        //do nothing
                    } else {
                        nextStep = nextStep * 2;
                    }
                } else if (duration < SEGMENT_DURATION * 2) {
                    //do nothing with nextStep
                } else {
                    nextStep = nextStep / 2 >= buffer.getMinStep() ? nextStep / 2 : nextStep;
                }
                sequence = updateByCustomStep(bizTag, nextStep);
                if (sequence != null) {
                    buffer.setUpdateTimestamp(System.currentTimeMillis());
                    buffer.setStep(nextStep);
                    buffer.setMinStep(sequence.getStep());
                }
            }
            if (sequence != null) {
                long value = sequence.getSeqValue() - buffer.getStep();
                sequenceRange.getValue().set(value);
                sequenceRange.setMax(sequence.getSeqValue());
                sequenceRange.setStep(buffer.getStep());
                if (sequenceRange.getMax() <= sequence.getMaxValue()) {
                    return;
                } else {
                    //超出设置的允许最大值，重置seq_value为0
                    sequenceMapper.resetWhenOverflow(bizTag);
                    log.info("biz_tag: {} ,seq_value {} greater than max_value {}, reset seq_value to 0",
                            new Object[]{sequenceRange.getMax(), sequence.getMaxValue(), bizTag});
                }
            }
        }
    }

    private Sequence update(String bizTag) {
        sequenceMapper.update(bizTag);
        return sequenceMapper.getByBizTag(bizTag);
    }

    private Sequence updateByCustomStep(String bizTag, int customStep) {
        sequenceMapper.updateByCustomStep(bizTag, customStep);
        return sequenceMapper.getByBizTag(bizTag);
    }

    private GenerateResult getIdFromSequenceBuffer(final SequenceRangeBuffer buffer) {
        for (; ; ) {
            try {
                buffer.readLock().lock();
                final SequenceRange range = buffer.getCurrent();
                if (!buffer.isNextReady() && (range.getIdle() < 0.9 * range.getStep()) && buffer.getThreadRunning()
                        .compareAndSet(false, true)) {
                    service.execute(() -> {
                        SequenceRange next = buffer.getRanges()[buffer.nextPos()];
                        boolean updateOk = false;
                        try {
                            updateSequenceFromDb(buffer.getBizTag(), next);
                            updateOk = true;
                            log.info("update sequence {} from db {}", buffer.getBizTag(), next);
                        } catch (Exception e) {
                            log.warn(buffer.getBizTag() + " update sequence from db exception", e);
                        } finally {
                            if (updateOk) {
                                buffer.writeLock().lock();
                                buffer.setNextReady(true);
                                buffer.getThreadRunning().set(false);
                                buffer.writeLock().unlock();
                            } else {
                                buffer.getThreadRunning().set(false);
                            }
                        }
                    });
                }
                long value = range.getValue().getAndIncrement();
                if (value < range.getMax()) {
                    return new GenerateResult(value, GenerateStatus.SUCCESS);
                }
            } finally {
                buffer.readLock().unlock();
            }

            waitAndSleep(buffer);
            try {
                buffer.writeLock().lock();
                final SequenceRange range = buffer.getCurrent();
                long value = range.getValue().getAndIncrement();
                if (value < range.getMax()) {
                    return new GenerateResult(value, GenerateStatus.SUCCESS);
                }
                if (buffer.isNextReady()) {
                    buffer.switchPos();
                    buffer.setNextReady(false);
                } else {
                    log.error("both two sequence ranges in {} are not ready!", buffer);
                    return new GenerateResult(EXCEPTION_ID_TWO_SEQUENCE_ARE_NULL, GenerateStatus.EXCEPTION);
                }
            } finally {
                buffer.writeLock().unlock();
            }
        }
    }

    private void waitAndSleep(SequenceRangeBuffer buffer) {
        int roll = 0;
        while (buffer.getThreadRunning().get()) {
            roll += 1;
            if (roll > 10000) {
                try {
                    Thread.sleep(10);
                    break;
                } catch (InterruptedException e) {
                    log.warn("thread {} interrupted", Thread.currentThread().getName());
                    break;
                }
            }
        }
    }
}
