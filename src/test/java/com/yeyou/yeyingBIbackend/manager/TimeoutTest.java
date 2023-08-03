package com.yeyou.yeyingBIbackend.manager;

import com.yeyou.yeyingBIbackend.common.ErrorCode;
import com.yeyou.yeyingBIbackend.exception.BusinessException;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class TimeoutTest {
    public static void main(String[] args) throws InterruptedException {
        CompletableFuture<Boolean> future = CompletableFuture.supplyAsync(() -> {
            try {
                Thread.sleep(2000);
                System.out.println("HEllo");
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
            return true;
        });
        Boolean aBoolean = null;
        try {
            aBoolean = future.get(1, TimeUnit.SECONDS);
        } catch (InterruptedException e) {
            System.out.println("InterruptedException");
        } catch (ExecutionException e) {
            System.out.println("ExecutionException");
        } catch (TimeoutException e) {
            System.out.println("TimeoutException");
        }

        System.out.println("Boolean="+aBoolean);
        Thread.sleep(2000);
    }
}
