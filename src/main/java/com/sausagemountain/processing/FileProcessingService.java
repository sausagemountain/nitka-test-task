package com.sausagemountain.processing;

import com.sausagemountain.util.MultiTaskQueue;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.TimeUnit;

@Service
public class FileProcessingService {

    public void processFile(InputStream fileStream, String name, int consumers) throws Throwable {
        printStart(name, consumers);

        var queue = new MultiTaskQueue<String>(consumers);
        var reader = new BufferedReader(new InputStreamReader(fileStream));

        var t = new Thread(() -> {
            try {
                reader.lines().forEach(line -> {
                    final Message message = Message.from(line, LocalDateTime.now());

                    if (StringUtils.hasLength(message.getId())) {
                        queue.add(message.getId(), () -> {
                            try {
                                processMessage(message);
                            } catch (Throwable e) {
                                throw new RuntimeException(e);
                            }
                        });
                    } else {
                        try {
                            Thread.sleep(message.getWait());
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
            } finally {
                queue.addEndTask(this::printEnd);
                queue.stop();
                try {
                    reader.close();
                } catch (IOException ignore) {
                    //ignore
                }
            }
        });
        t.start();
        queue.runTasks();
    }

    private void processMessage(Message message) throws Throwable {
        final var processStartTime = LocalDateTime.now();
        TimeUnit.MILLISECONDS.sleep(message.getWait());

        final var printTime = LocalDateTime.now();
        System.out.println(MessageFormat.format("PID: {0}; {1}; Thread: {2}; Start: {3}; End: {4}; Wait Time (ms): {5}",
                message.getThreadId(),
                message.toString(),
                String.valueOf(Thread.currentThread().getId()),
                message.getReadTime().format(DateTimeFormatter.ISO_LOCAL_TIME),
                printTime.format(DateTimeFormatter.ISO_LOCAL_TIME),
                String.valueOf(ChronoUnit.MILLIS.between(message.getReadTime(), processStartTime))
                ));
    }

    private void printStart(String filename, int consumers) {
        System.out.println(MessageFormat.format("PID: {0};  START: {1};  Consumers: {2};  File: {3}",
                String.valueOf(Thread.currentThread().getId()),
                LocalDateTime.now().format(DateTimeFormatter.ISO_TIME),
                String.valueOf(consumers),
                filename
        ));
    }

    private void printEnd() {
        System.out.println(MessageFormat.format("PID: {0};  END: {1}",
                String.valueOf(Thread.currentThread().getId()),
                LocalDateTime.now().format(DateTimeFormatter.ISO_TIME)
        ));
    }
}
