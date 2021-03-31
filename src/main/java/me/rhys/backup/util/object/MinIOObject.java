package me.rhys.backup.util.object;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class MinIOObject {
    private final String host;
    private final String accessKey;
    private final String privateKey;
    private final String bucketName;
}
