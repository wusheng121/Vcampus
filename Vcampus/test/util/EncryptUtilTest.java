package util;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

/**
 * {@link EncryptUtil} 单元测试：SHA-256 哈希的确定性与已知向量。
 */
class EncryptUtilTest {

    @Test
    void sha256OfKnownInputs() {
        assertEquals("e3b0c44298fc1c149afbf4c8996fb92427ae41e4649b934ca495991b7852b855",
                EncryptUtil.sha256(""));
        assertEquals("ba7816bf8f01cfea414140de5dae2223b00361a396177a9cb410ff61f20015ad",
                EncryptUtil.sha256("abc"));
        assertEquals("8d969eef6ecad3c29a3a629280e686cf0c3f5d5a86aff3ca12020c923adc6c92",
                EncryptUtil.sha256("123456"));
    }

    @Test
    void sha256IsDeterministicAndHex64() {
        String h1 = EncryptUtil.sha256("vcampus");
        String h2 = EncryptUtil.sha256("vcampus");
        assertEquals(h1, h2, "相同输入应产生相同哈希");
        assertNotNull(h1);
        assertEquals(64, h1.length(), "SHA-256 十六进制应为 64 字符");
        assertEquals(0, h1.replaceAll("[0-9a-f]", "").length(),
                "应仅含小写十六进制字符");
    }

    @Test
    void sha256IsCaseSensitiveByInput() {
        // 大小写不同输入产生不同哈希
        assertEquals(false, EncryptUtil.sha256("A").equals(EncryptUtil.sha256("a")));
    }
}
