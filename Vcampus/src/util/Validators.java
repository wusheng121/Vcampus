package util;

/**
 * 输入校验工具：必填、邮箱、身份证、手机、密码强度。供各表单在提交前调用。
 */
public final class Validators {
    private Validators() {
    }

    public static boolean nonEmpty(String s) {
        return s != null && !s.trim().isEmpty();
    }

    public static boolean isEmail(String s) {
        return nonEmpty(s) && s.trim().matches("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    }

    /** 18 位身份证（末位可为 X）。 */
    public static boolean isIdentity(String s) {
        return nonEmpty(s) && s.trim().matches("^\\d{17}[\\dXx]$");
    }

    /** 11 位大陆手机号。 */
    public static boolean isPhone(String s) {
        return nonEmpty(s) && s.trim().matches("^1\\d{10}$");
    }

    /** 密码强度：长度>=8 且含字母与数字。 */
    public static boolean passwordStrong(String s) {
        return s != null && s.length() >= 8
                && s.matches(".*[A-Za-z].*")
                && s.matches(".*\\d.*");
    }
}
