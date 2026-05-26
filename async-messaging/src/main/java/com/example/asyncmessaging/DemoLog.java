package com.example.asyncmessaging;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

final class DemoLog {

    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm:ss.SSS");
    private static final String RED = "\u001B[31m";
    private static final String RESET = "\u001B[0m";

    private DemoLog() {
    }

    static void title(String message) {
        System.out.println();
        System.out.println("============================================================");
        System.out.println(message);
        System.out.println("============================================================");
    }

    static void section(String message) {
        System.out.println();
        System.out.println("------------------------------------------------------------");
        System.out.println(message);
        System.out.println("------------------------------------------------------------");
    }

    static void info(String message) {
        print(message);
    }

    static void focus(String message) {
        print(RED + "[집중] " + message + RESET);
    }

    static void sleep(String message, Duration duration) {
        info(message + " 시작");
        pause(duration);
        info(message + " 종료");
    }

    static void pause(Duration duration) {
        try {
            Thread.sleep(duration.toMillis());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IllegalStateException("데모 대기 중 인터럽트가 발생했습니다.", e);
        }
    }

    private static void print(String message) {
        System.out.printf("[%s] [%-12s] %s%n", LocalTime.now().format(TIME_FORMATTER), Thread.currentThread().getName(), message);
    }
}
