package com.pbad.customer;

/**
 * @author pangdi
 * @version 1.0
 * @date 2024年04月15日 15:20
 */
public class NotifyExample extends Thread{

    private static final Object lock = new Object();
    private static boolean isReady = false;

    public static void waitingThread() {
        synchronized (lock) {
            while (!isReady) {
                try {
                    System.out.println("Thread is waiting.");
                    lock.wait();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }
            System.out.println("Thread is now running.");
        }
    }

    public static void notifyingThread() {
        synchronized (lock) {
            isReady = true;
            System.out.println("Thread has set the condition and will notify.");
            lock.notify();
        }
    }

    public static void main(String[] args) {
        // 创建并启动等待线程
        Thread waitingThread = new Thread(NotifyExample::waitingThread);
        waitingThread.start();

        // 确保等待线程有时间启动并进入等待状态
        try {
            Thread.sleep(1000);  // 暂停1秒确保等待线程已经开始等待
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // 创建并启动通知线程
        Thread notifyingThread = new Thread(NotifyExample::notifyingThread);
        notifyingThread.start();
    }
}
