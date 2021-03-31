package me.rhys.backup.util.object;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter @AllArgsConstructor
public class RedisObject {
    private final String IP;
    private final int port;
    private final String password;
    private final int database;
}
