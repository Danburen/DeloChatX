# WaterApi 需要补充的接口规划

> 此文档规划 WaterApi 需要补充的接口，VeloChatX 重构依赖这些接口。
> 实现由 WaterApi 维护者手动完成。

## 1. Server 接口扩展

### 当前状态
```java
// WaterAPI/src/main/java/org/nitrowater/waterapi/domain/model/Server.java
public interface Server {
    String getName();
    String getDisplay();
    List<UPlayer> getOnlinePlayers();
}
```

### 需要补充的方法

```java
public interface Server {
    // 现有方法
    String getName();
    String getDisplay();
    List<UPlayer> getOnlinePlayers();

    // 新增方法
    /**
     * Get the raw platform server object.
     * Used for platform-specific operations that cannot be abstracted.
     */
    Object getNativeServer();

    /**
     * Check if this server is connected to the proxy.
     */
    boolean isConnected();
}
```

---

## 2. UPlayer 接口扩展

### 当前状态
```java
// WaterAPI/src/main/java/org/nitrowater/waterapi/domain/model/UPlayer.java
public interface UPlayer {
    String getLoginName();
    String getNickname();
    UUID getUuid();
    Server getCurrentServer();
    void updateCurrentServer(Server server);
}
```

### 需要补充的方法

```java
public interface UPlayer {
    // 现有方法
    String getLoginName();
    String getNickname();
    UUID getUuid();
    Server getCurrentServer();
    void updateCurrentServer(Server server);

    // 新增方法
    /**
     * Get the player's display name for chat output.
     * May differ from getLoginName() if custom display name is set.
     */
    String getDisplayName();

    /**
     * Get the player's current network latency in milliseconds.
     */
    int getPing();

    /**
     * Get the player's effective locale setting.
     */
    Locale getLocale();

    /**
     * Check if the player is currently online on the proxy.
     */
    boolean isOnline();

    /**
     * Get the raw platform player object.
     */
    Object getNativePlayer();
}
```

---

## 3. ConfigurableService 增强

### 当前状态
```java
// WaterAPI/src/main/java/org/nitrowater/waterapi/domain/infra/service/ConfigurableService.java
public interface ConfigurableService<K extends ConfigKey> extends Service {
    Config getConfig();
    default <T> T getConfig(K key) { return getConfig().get(key); }
    default Boolean getBooleanConfig(K key) { return getConfig(key); }
}
```

### 需要补充的方法

```java
public interface ConfigurableService<K extends ConfigKey> extends Service {
    // 现有方法
    Config getConfig();
    default <T> T getConfig(K key) { return getConfig().get(key); }
    default Boolean getBooleanConfig(K key) { return getConfig(key); }

    // 新增方法
    /**
     * Get a list config value.
     */
    default <T> List<T> getList(K key) {
        return getConfig().getList(key);
    }

    /**
     * Get a string config value with default.
     */
    default String getString(K key) {
        return getConfig().get(key);
    }

    /**
     * Get an integer config value.
     */
    default Integer getInteger(K key) {
        return getConfig().get(key);
    }

    /**
     * Get a long config value.
     */
    default Long getLong(K key) {
        return getConfig().get(key);
    }

    /**
     * Reload configuration from disk.
     */
    void reload();
}
```

---

## 4. Config 接口增强

### 当前状态
```java
// WaterAPI/src/main/java/org/nitrowater/waterapi/domain/infra/config/Config.java
public class Config {
    public <T> T get(ConfigKey key) { ... }
    public <T> T get(String key, T defaultValue) { ... }
    public Boolean is(ConfigKey key) { ... }
    public <T> List<T> getList(ConfigKey key) { ... }
}
```

### 需要补充的方法

```java
public class Config {
    // 现有方法保持不变

    // 新增方法
    /**
     * Get a string value by ConfigKey.
     */
    public String getString(ConfigKey key) {
        return get(key);
    }

    /**
     * Get an integer value by ConfigKey.
     */
    public Integer getInteger(ConfigKey key) {
        return get(key);
    }

    /**
     * Get a long value by ConfigKey.
     */
    public Long getLong(ConfigKey key) {
        return get(key);
    }

    /**
     * Get a double value by ConfigKey.
     */
    public Double getDouble(ConfigKey key) {
        return get(key);
    }
}
```

---

## 5. ConfigKey 接口保持不变

当前接口已满足需求：

```java
// WaterAPI/src/main/java/org/nitrowater/waterapi/domain/infra/config/ConfigKey.java
public interface ConfigKey {
    String getKey();
    default List<String> getFriendlyKeys() { return List.of(getKey()); }
    Object getDefaultValue();
}
```

VeloChatX 通过枚举实现此接口：

```java
public enum ChatConfigKey implements ConfigKey {
    FORMAT("chat-format", "{channel}{Group}{Server}{Prefix}{Player}{Suffix} §8:§r {Message}"),
    FORMAT_MODEL("chat-format-model", "none"),
    CROSSING_CHAT_ENABLE("crossing-chat-enable", true);

    private final String key;
    private final Object defaultValue;

    ChatConfigKey(String key, Object defaultValue) {
        this.key = key;
        this.defaultValue = defaultValue;
    }

    @Override public String getKey() { return key; }
    @Override public Object getDefaultValue() { return defaultValue; }
}
```

---

## 6. 实现优先级

| 优先级 | 接口 | 说明 |
|-------|------|------|
| P0 | UPlayer 扩展 | VeloChatX 需要 getPing(), getLocale() 等 |
| P0 | Server 扩展 | VeloChatX 需要 getNativeServer() |
| P1 | ConfigurableService 增强 | 提供更多类型安全的 getter |
| P1 | Config 增强 | 提供类型安全的 getter |
| P2 | ConfigKey 保持不变 | 当前已满足需求 |

---

## 7. 属性下沉分析

### VPlayer → UPlayer 下沉

| VPlayer 方法 | 建议 | 理由 |
|-------------|------|------|
| `getDisplayName()` | → UPlayer | 所有平台都有显示名 |
| `getPing()` | → UPlayer | 在线玩家通用属性 |
| `getLocale()` | → UPlayer | i18n 通用需求 |
| `isOnline()` | → UPlayer | 基础状态 |
| `getNativePlayer()` | → UPlayer | 平台抽象通用模式 |
| `getTabListEntries()` | 保留 VPlayer | VeloChatX 特有 |
| `getCurrentServerName()` | 保留 VPlayer | 便捷方法 |

### VServer → Server 下沉

| VServer 方法 | 建议 | 理由 |
|-------------|------|------|
| `getPlayerCount()` | → Server | 基础属性 |
| `getNativeServer()` | → Server | 平台抽象通用模式 |
| `getChannels()` 系列 | 保留 VServer | 频道管理特有 |
| `getVPlayers()` 系列 | 保留 VServer | 玩家管理特有 |

---

## 8. VeloChatX DDD 架构

### 目录结构

```
org.nitrowater.velochatx/
├── domain/
│   ├── channel/
│   │   ├── Channel.java              (实体，使用 UPlayer)
│   │   ├── ChannelConfigKey.java     (枚举，仅 config.yml)
│   │   ├── ChannelService.java       (接口)
│   │   └── ChannelServiceImpl.java   (实现)
│   ├── broadcast/
│   │   ├── BroadcastConfigKey.java   (枚举，仅 broadcast.yml)
│   │   ├── BroadcastService.java     (接口)
│   │   └── BroadcastServiceImpl.java (实现)
│   ├── server/
│   │   ├── ServerInfo.java           (实体，实现 Server 接口，使用 UPlayer)
│   │   ├── ServerConfigKey.java      (枚举，仅 config.yml)
│   │   ├── ServerService.java        (接口)
│   │   └── ServerServiceImpl.java    (实现)
│   ├── chat/
│   │   ├── ChatConfigKey.java        (枚举，仅 config.yml)
│   │   ├── ChatService.java          (接口，使用 UPlayer)
│   │   └── ChatServiceImpl.java      (实现)
│   └── tablist/
│       ├── TabListConfigKey.java     (枚举，仅 config.yml)
│       ├── TabListService.java       (接口，使用 UPlayer)
│       └── TabListServiceImpl.java   (实现)
└── infrastructure/
    └── strategy/
        ├── VelocityPlayerStrategy.java
        └── VelocityServerStrategy.java
```

### 设计模式

#### 1. Service 实现模式

```java
public class ChannelServiceImpl implements ChannelService, 
        EventListener<KernelConfigUpdateEvent> {
    
    private final ServerService serverService;  // 构造器注入
    private volatile Config config;              // final Config

    public ChannelServiceImpl(Config config, ServerService serverService) {
        this.config = config;
        this.serverService = serverService;
        loadChannels();  // 构造时加载
    }

    @Override
    public Config getConfig() { return config; }

    @Override
    public void onEvent(KernelConfigUpdateEvent event) {
        this.config = event.getConfig();  // 配置重载
        loadChannels();
    }
}
```

#### 2. 配置读取模式

```java
// ✅ 正确方式
boolean enabled = getConfig().get(ChannelConfigKey.BROADCAST_ENABLED);
String prefix = getConfig().get(BroadcastConfigKey.JOIN_LEAVE_JOIN_PREFIX);

// ❌ 错误方式
boolean enabled = getConfigs().getBoolean("broadcast-enable", false);
```

#### 3. 配置分离

| Domain | 配置源 | 说明 |
|-------|-------|------|
| Channel | config.yml | `broadcast-enable`, `channels.global`, `channels.channel-list.*` |
| Broadcast | broadcast.yml | `random`, `global.*`, `locale.*`, `welcome-broadcast.*`, `join-leave-*` |
| Server | config.yml | `server-display.enable`, `server-display.proxy` |
| Chat | config.yml | `crossing-chat-enable`, `chat-format`, `ban-words.*` |
| TabList | config.yml | `tab-list.enable`, `tab-list.interval`, `tab-list.format` |
| Log | kernel | 委托给 `messageLoggerService` / `playerMessageService` |

### 已创建组件

| Domain | 实体 | ConfigKey | Service 接口 | Service 实现 |
|-------|------|----------|-------------|-------------|
| channel | `Channel` | `ChannelConfigKey` | `ChannelService` | `ChannelServiceImpl` |
| broadcast | `BroadcastModel` | `BroadcastConfigKey` | `BroadcastService` | `BroadcastServiceImpl` |
| server | `ServerInfo` | `ServerConfigKey` | `ServerService` | `ServerServiceImpl` |
| chat | - | `ChatConfigKey` | `ChatService` | `ChatServiceImpl` |
| tablist | - | `TabListConfigKey` | `TabListService` | `TabListServiceImpl` |
| log | - | - | 委托 kernel | - |

### 类型使用

| 组件 | 旧类型 | 新类型 |
|-----|-------|-------|
| 玩家 | `Object` / `Player` | `UPlayer` / `UniversalPlayer` |
| 服务器 | `Object` / `RegisteredServer` / `SubServer` | `ServerInfo` (implements `Server`) |

---

## 9. 配置读取对照表

| 旧 Manager | 旧配置读取方式 | 新 Service | 新配置读取方式 |
|-----------|--------------|-----------|--------------|
| `BasicMethods` | `getConfigs().getBoolean("server-display.enable")` | `ServerService` | `getConfig().get(ServerConfigKey.DISPLAY_ENABLED)` |
| `ChannelManager` | `getConfigs().getBoolean("broadcast-enable")` | `ChannelService` | `getConfig().get(ChannelConfigKey.BROADCAST_ENABLED)` |
| `ChannelManager` | `broadCastConfig.get("global.enable")` | `BroadcastService` | `getConfig().get(BroadcastConfigKey.GLOBAL_ENABLED)` |
| `ChatManager` | `getConfigs().getBoolean("crossing-chat-enable")` | `ChatService` | `getConfig().get(ChatConfigKey.CROSSING_CHAT_ENABLED)` |
| `TabListManager` | `getConfigs().getBoolean("tab-list.enable")` | `TabListService` | `getConfig().get(TabListConfigKey.ENABLED)` |
| `LogManager` | `getConfigs().getBoolean("log-text.enable")` | kernel | `messageLoggerService` |

---

## 10. 后续实现步骤

### WaterApi 侧（手动实现）

1. **UPlayer 接口扩展**
   - 添加 `getDisplayName()`, `getPing()`, `getLocale()`, `isOnline()`, `getNativePlayer()`
   - 更新 `UniversalPlayer` 实现

2. **Server 接口扩展**
   - 添加 `getPlayerCount()`, `getNativeServer()`, `isConnected()`

3. **ConfigurableService 保持不变**
   - 已有 `getConfig()` 方法满足需求

### VeloChatX 侧

1. **DDD Domain 层** ✅ 已完成
   - channel, broadcast, server, chat, tablist 各自独立
   - Log 委托给 kernel 服务

2. **Service 实现** ✅ 已完成
   - 各 Service 通过构造器注入 Config
   - 实现 `EventListener<KernelConfigUpdateEvent>` 处理配置重载
   - 持有 `volatile Config config` 字段
   - 使用 `UPlayer` 替代 `Object`

3. **基础设施层** ✅ 已完成
   - `VelocityPlayerStrategy`
   - `VelocityServerStrategy`

4. **应用层** （下一步）
   - 创建 VeloChatX 主类初始化
   - 注册 EventListener
   - 协调各 Service

---

*最后更新: 2026-09-05*
