package me.rhys.backup.service.api;

import lombok.AllArgsConstructor;
import lombok.Getter;
import me.rhys.backup.util.object.MinIOObject;
import me.rhys.backup.util.object.RedisObject;
import me.rhys.backup.util.time.TimeUtil;

import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Getter @AllArgsConstructor
public class BackupObject implements BackupInterface {
    private final String name;
    private final RedisObject redisObject;
    private final MinIOObject minIOObject;

    private final ExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
    private final String UID = UUID.randomUUID().toString();

    @Override
    public void run() {
        //
    }

    protected void sendConsoleMessage(String string, LogType logType) {
        System.out.printf("[BACKUP] [%S] (%s # %s) - %s (%s)\n", logType.type, this.name, this.UID, string,
                TimeUtil.getSystemTime());
    }

    public enum LogType {
        INFORMATION("INFO"),
        ERROR("ERROR"),
        WARNING("WARNING");

        String type;
        LogType(String type) {
            this.type = type;
        }
    }
}
