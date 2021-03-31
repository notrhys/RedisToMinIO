package me.rhys.backup.service;

import me.rhys.backup.service.api.BackupObject;
import me.rhys.backup.service.impl.BackupService;
import me.rhys.backup.util.object.MinIOObject;
import me.rhys.backup.util.object.RedisObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class Backup {
    private final List<BackupObject> backupObjectList = Collections.synchronizedList(new ArrayList<>());
    private final ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

    public void setup() {
        this.addBackupService(new BackupService(
                "Service-Name",
                new RedisObject("REDIS_IP", 1234,
                "REDIS_PASSWORD",
                0), new MinIOObject("MinIO_Host",
                        "MinIO_ACCESS",
                        "MinIO_PRIVATE",
                "MinIO_Bucket_Name")));

        this.startBackup();
        this.startTimer();
    }

    void startTimer() {
        this.executorService.scheduleAtFixedRate(this::startBackup, 5L, 5L,
                TimeUnit.HOURS);
    }

    void startBackup() {
        this.backupObjectList.forEach(backupObject -> backupObject.getExecutorService().execute(backupObject::run));
    }

    void addBackupService(BackupObject backupObject) {
        this.backupObjectList.add(backupObject);
    }
}
