package server.dao;

import java.sql.Timestamp;

/**
 * 找回密码验证码 DAO：保存/查询/标记已用。
 * 验证码以 SHA-256 哈希存储，含过期时间与是否已用。
 */
public interface PasswordResetDAO {

    /** 保存一条验证码记录（codeHash=sha256(6位码)） */
    boolean save(String userId, String codeHash, Timestamp expireTime);

    /** 取该用户最新的、未使用、未过期的验证码记录 */
    ResetRecord findLatestUnused(String userId);

    /** 标记某条记录为已用 */
    boolean markUsed(int id);

    /** 删除某用户所有已过期/已用的旧记录（清理） */
    int deleteStale(String userId);

    /** 单条记录载体 */
    class ResetRecord {
        public final int id;
        public final String userId;
        public final String codeHash;
        public final Timestamp expireTime;

        public ResetRecord(int id, String userId, String codeHash, Timestamp expireTime) {
            this.id = id;
            this.userId = userId;
            this.codeHash = codeHash;
            this.expireTime = expireTime;
        }
    }
}
