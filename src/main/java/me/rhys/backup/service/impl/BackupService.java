package me.rhys.backup.service.impl;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.UploadObjectArgs;
import io.minio.errors.MinioException;
import me.rhys.backup.service.api.BackupObject;
import me.rhys.backup.util.file.FileUtil;
import me.rhys.backup.util.file.ZIPUtil;
import me.rhys.backup.util.math.MathUtil;
import me.rhys.backup.util.object.MinIOObject;
import me.rhys.backup.util.object.RedisObject;
import me.rhys.backup.util.time.TimeUtil;
import org.springframework.util.FileSystemUtils;
import redis.clients.jedis.Jedis;

import java.io.File;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.*;

public class BackupService extends BackupObject {
    public BackupService(String name, RedisObject redisObject, MinIOObject minIOObject) {
        super(name, redisObject, minIOObject);
    }

    private Jedis jedis;
    private File folder, zipFile;
    private final Map<String, String> data = new HashMap<>();

    @Override
    public void run() {
        this.data.clear();
        this.createFolder();
        this.connectToRedis();
        this.grabRedisKeys();
        this.downloadKeys();
        this.zipFolder();
        try {
            this.uploadToCloud();
        } catch (IOException | NoSuchAlgorithmException | InvalidKeyException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
        this.cleanUp();
    }

    void cleanUp() {
        this.jedis.disconnect();
        this.data.clear();
        this.folder.delete();
        this.zipFile.delete();
        FileSystemUtils.deleteRecursively(this.folder);
        FileSystemUtils.deleteRecursively(this.zipFile);

        this.sendConsoleMessage("Finished Backup.", LogType.INFORMATION);
    }

    void uploadToCloud() throws IOException, NoSuchAlgorithmException, InvalidKeyException {
        /*
            Note:
             - MinIO Bucket names must be fully lower-case
         */
        try {
            this.sendConsoleMessage("Setting up MinIO Client... ", LogType.INFORMATION);
            MinioClient minioClient =
                    MinioClient.builder()
                            .endpoint(this.getMinIOObject().getHost())
                            .credentials(this.getMinIOObject().getAccessKey(),
                                    this.getMinIOObject().getPrivateKey())
                            .build();

            if (!minioClient.bucketExists(BucketExistsArgs.builder()
                    .bucket(this.getMinIOObject().getBucketName()
                            .toLowerCase(Locale.ROOT)).build())) {
                minioClient.makeBucket(MakeBucketArgs.builder().bucket(this.getMinIOObject().getBucketName()
                        .toLowerCase(Locale.ROOT)).build());
            } else {
                this.sendConsoleMessage("Bucket with the name '"
                        + this.getMinIOObject().getBucketName() + "' already exits", LogType.WARNING);
            }

            this.sendConsoleMessage("Uploading to MinIO...", LogType.INFORMATION);
            minioClient.uploadObject(
                    UploadObjectArgs.builder()
                            .bucket(this.getMinIOObject().getBucketName()
                                    .toLowerCase(Locale.ROOT))
                            .object(zipFile.getName())
                            .filename(this.zipFile.getAbsolutePath())
                            .build());
        } catch (MinioException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        }
    }

    void zipFolder() {
        this.zipFile = new File("cache/" + this.getUID() + "-" + TimeUtil.getSystemTime() + ".zip");
        try {
            new ZIPUtil().zip(Collections.singletonList(this.folder), this.zipFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void downloadKeys() {
        if (this.jedis.isConnected()) {
            float amount = this.data.size();
            float completed = 0;

            for (Map.Entry<String, String> entry : this.data.entrySet()) {
                FileUtil.write(new File(this.folder.getAbsolutePath() + "/" + entry.getKey()
                        .replace("*", ""))
                        .getAbsolutePath(), entry.getValue());

                if (completed % 60 == 0) {
                    this.sendConsoleMessage("Saved " + MathUtil.trimFloat(1,
                            (completed * 100.0f) / amount) + "% of keys", LogType.INFORMATION);
                }
                completed++;
            }
        } else {
            this.sendConsoleMessage("Error when trying to download keys from redis database.", LogType.ERROR);
        }
    }

    void grabRedisKeys() {
        if (this.jedis.isConnected()) {
            this.sendConsoleMessage("Grabbing redis keys...", LogType.INFORMATION);

            Set<String> toSearch = jedis.keys("*");
            float amount = toSearch.size();
            float completed = 0;

            for (String s : toSearch) {
                String data = this.jedis.get(s);

                if (data != null) {
                    this.data.put(s, data);
                }

                if (completed % 60 == 0) {
                    this.sendConsoleMessage("Downloaded " + MathUtil.trimFloat(1,
                            (completed * 100.0f) / amount) + "% of keys", LogType.INFORMATION);
                }
                completed++;
            }
        } else {
            this.sendConsoleMessage("Error when trying to grab keys from redis database.", LogType.ERROR);
        }
    }

    void connectToRedis() {
        this.jedis = new Jedis(this.getRedisObject().getIP(), this.getRedisObject().getPort());
        this.jedis.auth(this.getRedisObject().getPassword());
        this.jedis.select(this.getRedisObject().getDatabase());
    }

    void createFolder() {
        this.folder = new File("cache/" + this.getUID());
        if (this.folder.mkdirs()) {
            this.sendConsoleMessage("Created temp folder for " + this.getUID(), LogType.INFORMATION);
        } else {
            this.sendConsoleMessage("Unable to create temp folder for " + this.getUID(), LogType.ERROR);
        }
    }
}
