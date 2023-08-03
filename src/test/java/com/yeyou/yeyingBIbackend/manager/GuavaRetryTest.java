package com.yeyou.yeyingBIbackend.manager;

import com.github.rholder.retry.*;
import com.google.common.base.Predicates;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.concurrent.*;

public class GuavaRetryTest {

    private static int invokeCount=0;

    public int realAction(int num) throws InterruptedException {
        System.out.println("任务:" + num);
        invokeCount++;
        Thread.sleep(2000);
        System.out.println("结束睡眠");
        return num;
    }
    @Test
    public void test() throws InterruptedException {
        Retryer<Integer> retryer = RetryerBuilder.<Integer>newBuilder()
                .retryIfRuntimeException()
                .withRetryListener(new MyRetryListener())
                .withBlockStrategy(BlockStrategies.threadSleepStrategy())
                .withAttemptTimeLimiter(AttemptTimeLimiters.fixedTimeLimit(1,TimeUnit.SECONDS))
                .withStopStrategy(StopStrategies.stopAfterAttempt(3)).build();
        invokeCount=0;
        try {
            retryer.call(()->this.realAction(0));
        } catch (Exception e) {
            System.out.println("执行0，异常：" + e.getMessage());
        }
        Thread.sleep(5000);
    }

    private static class MyRetryListener implements RetryListener{
        @Override
        public <V> void onRetry(Attempt<V> attempt) {
            System.out.println("第"+attempt.getAttemptNumber()+"次执行");
        }
    }


}

