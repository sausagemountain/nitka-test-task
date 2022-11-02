package com.sausagemountain.processing;

import java.time.LocalDateTime;

public class Message {
    private final String id;
    private final long wait;
    private final String payload;
    private final LocalDateTime readTime;
    private final String threadId;

    public String getId() {
        return id;
    }

    public long getWait() {
        return wait;
    }

    public String getPayload() {
        return payload;
    }

    public LocalDateTime getReadTime() {
        return readTime;
    }

    public String getThreadId() {
        return threadId;
    }

    public Message(String id, long wait, String payload, LocalDateTime readTime, String threadId) {
        this.id = id;
        this.wait = wait;
        this.payload = payload;
        this.readTime = readTime;
        this.threadId = threadId;
    }

    public static Message from(String line, LocalDateTime time) {
        final String[] split = line.split("[|]");

        var id = split[0];
        var wait = Long.parseLong(split[1]);
        var payload = split.length > 2 ? split[2] : "";

        return new Message(id, wait, payload, time, String.valueOf(Thread.currentThread().getId()));
    }

    public static Message from(String line) {
        return from(line, LocalDateTime.now());
    }

    @Override
    public String toString() {
        return getId() + '|' + getWait() + '|' + getPayload();
    }
}
