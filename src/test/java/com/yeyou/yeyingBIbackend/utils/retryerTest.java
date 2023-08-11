package com.yeyou.yeyingBIbackend.utils;

import com.github.rholder.retry.*;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

//@SpringBootTest
public class retryerTest {
    @Test
    public void testAsync() throws ExecutionException, RetryException {
        Retryer<Boolean> retryer = RetryerBuilder.<Boolean>newBuilder()
                .retryIfException()
                //阻塞等待
                .withBlockStrategy(BlockStrategies.threadSleepStrategy())
                //按照斐波那列的值来重试等待
                .withWaitStrategy(WaitStrategies.fibonacciWait(20,TimeUnit.SECONDS))
                //任务超时管理
                .withAttemptTimeLimiter(AttemptTimeLimiters.fixedTimeLimit(50, TimeUnit.SECONDS))
                //最多重试次数
                .withStopStrategy(StopStrategies.stopAfterAttempt(3)).build();
        retryer.call(() -> {
            Thread.sleep(9000L);
            return true;
        });
        CompletableFuture.runAsync(() -> {
            try {
                retryer.call(() -> {
                    Thread.sleep(9000L);
                    return true;
                });
            } catch (ExecutionException e) {
                throw new RuntimeException(e);
            } catch (RetryException e) {
                throw new RuntimeException(e);
            }
        });

        System.out.println("yes");
    }
}
