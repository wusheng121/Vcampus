package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * {@link Config} 配置加载器：默认值/系统属性回退逻辑测试。
 * 不依赖 config.properties 的具体内容，保证可重复性。
 */
class ConfigTest {

    @Test
    void missingKeyReturnsProvidedDefault() {
        assertEquals("fallback", Config.get("__definitely_missing_key__", "fallback"));
        assertEquals(42, Config.getInt("__definitely_missing_key__", 42));
    }

    @Test
    void missingKeyWithoutDefaultReturnsNull() {
        assertNull(Config.get("__another_missing_key__"));
    }

    @Test
    void systemPropertyOverridesConfig() {
        // 通过 -Dconfig.test.prop=abc 可覆盖；测试中以系统属性为准
        String key = "vcampus.test.override." + System.nanoTime();
        System.setProperty(key, "sysvalue");
        try {
            assertEquals("sysvalue", Config.get(key));
            assertEquals("sysvalue", Config.get(key, "ignored"));
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    void getIntFallsBackOnInvalidNumber() {
        String key = "vcampus.test.badint." + System.nanoTime();
        System.setProperty(key, "not-a-number");
        try {
            assertEquals(99, Config.getInt(key, 99));
        } finally {
            System.clearProperty(key);
        }
    }

    @Test
    void getIntFallsBackOnBlank() {
        String key = "vcampus.test.blankint." + System.nanoTime();
        System.setProperty(key, "   ");
        try {
            assertEquals(7, Config.getInt(key, 7));
        } finally {
            System.clearProperty(key);
        }
    }
}
