package common;

/**
 * 权限点常量（单一来源），与 DB 表 `permission.code` 一一对应。
 * 用于 RBAC 查询：AuthService.hasPermission(role, Permissions.XXX)。
 */
public final class Permissions {

    // ---- user ----
    public static final String USER_CREATE   = "user:create";
    public static final String USER_UPDATE   = "user:update";
    public static final String USER_DELETE   = "user:delete";
    public static final String USER_READ_ALL = "user:read-all";

    // ---- student ----
    public static final String STUDENT_CREATE = "student:create";
    public static final String STUDENT_UPDATE = "student:update";
    public static final String STUDENT_DELETE = "student:delete";
    public static final String STUDENT_READ   = "student:read";
    public static final String STUDENT_AUDIT  = "student:audit";

    // ---- course ----
    public static final String COURSE_CREATE = "course:create";
    public static final String COURSE_UPDATE = "course:update";
    public static final String COURSE_DELETE = "course:delete";
    public static final String COURSE_READ   = "course:read";

    // ---- lesson ----
    public static final String LESSON_CREATE = "lesson:create";
    public static final String LESSON_UPDATE = "lesson:update";
    public static final String LESSON_DELETE = "lesson:delete";
    public static final String LESSON_READ   = "lesson:read";

    // ---- lessontime ----
    public static final String LESSONTIME_CREATE = "lessontime:create";
    public static final String LESSONTIME_UPDATE = "lessontime:update";
    public static final String LESSONTIME_DELETE = "lessontime:delete";

    // ---- enrollment ----
    public static final String ENROLLMENT_ENROLL        = "enrollment:enroll";
    public static final String ENROLLMENT_DROP          = "enrollment:drop";
    public static final String ENROLLMENT_READ_SELF     = "enrollment:read-self";
    public static final String ENROLLMENT_READ_BY_LESSON = "enrollment:read-by-lesson";

    // ---- book ----
    public static final String BOOK_CREATE = "book:create";
    public static final String BOOK_UPDATE = "book:update";
    public static final String BOOK_DELETE = "book:delete";
    public static final String BOOK_READ   = "book:read";
    public static final String BOOK_BORROW = "book:borrow";
    public static final String BOOK_RETURN = "book:return";

    // ---- borrowrecord ----
    public static final String BORROWRECORD_READ_SELF = "borrowrecord:read-self";
    public static final String BORROWRECORD_READ_ALL = "borrowrecord:read-all";

    // ---- journal ----
    public static final String JOURNAL_CREATE = "journal:create";
    public static final String JOURNAL_UPDATE = "journal:update";
    public static final String JOURNAL_DELETE = "journal:delete";
    public static final String JOURNAL_READ   = "journal:read";

    // ---- product ----
    public static final String PRODUCT_CREATE = "product:create";
    public static final String PRODUCT_UPDATE = "product:update";
    public static final String PRODUCT_DELETE = "product:delete";
    public static final String PRODUCT_READ   = "product:read";

    // ---- category ----
    public static final String CATEGORY_CREATE = "category:create";
    public static final String CATEGORY_DELETE = "category:delete";

    // ---- order ----
    public static final String ORDER_CREATE        = "order:create";
    public static final String ORDER_READ_SELF    = "order:read-self";
    public static final String ORDER_READ_ALL     = "order:read-all";
    public static final String ORDER_PAY           = "order:pay";
    public static final String ORDER_CANCEL        = "order:cancel";
    public static final String ORDER_UPDATE_STATUS = "order:update-status";
    public static final String ORDER_DELETE        = "order:delete";

    // ---- teacher ----
    public static final String TEACHER_READ = "teacher:read";

    private Permissions() {
    }
}
