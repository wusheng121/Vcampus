package util;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Properties;

/**
 * 统一配置加载器：从 classpath 读取 config.properties。
 * 优先级：系统属性 (-Dkey=value) > config.properties > 代码默认值。
 * <p>用 UTF-8 Reader 加载（Properties.load(InputStream) 默认 ISO-8859-1，会把中文读成乱码）。
 */
public final class Config {
    private static final Properties PROPS = new Properties();

    static {
        try (InputStream in = Config.class.getResourceAsStream("/config.properties")) {
            if (in != null) {
                PROPS.load(new InputStreamReader(in, StandardCharsets.UTF_8));
            } else {
                System.err.println("[Config] 未在 classpath 找到 /config.properties，将使用默认值/系统属性");
            }
        } catch (Exception e) {
            throw new RuntimeException("加载 config.properties 失败", e);
        }
    }

    private Config() {
    }

    /** 取配置值，缺失返回 null */
    public static String get(String key) {
        String v = System.getProperty(key);
        return (v != null) ? v : PROPS.getProperty(key);
    }

    /** 取配置值，缺失返回默认值 */
    public static String get(String key, String def) {
        String v = System.getProperty(key);
        if (v != null) return v;
        return PROPS.getProperty(key, def);
    }

    /** 取整型配置，缺失/解析失败返回默认值 */
    public static int getInt(String key, int def) {
        String v = get(key);
        if (v == null || v.isBlank()) return def;
        try {
            return Integer.parseInt(v.trim());
        } catch (NumberFormatException e) {
            return def;
        }
    }
}
