package common.net;

/**
 * 全部客户端/服务端通信消息类型的唯一来源（single source of truth）。
 * 客户端发送时使用常量，服务端 switch 匹配同名常量，杜绝字符串拼写漂移。
 *
 * 说明：值必须与历史线上字符串完全一致，保证新旧两端兼容。
 */
public final class MessageType {

    private MessageType() {
    }

    // ========================= 认证 / 用户 =========================
    public static final String LOGIN               = "login";
    public static final String ADD_USER            = "addUser";
    public static final String DELETE_USER         = "deleteUser";
    public static final String UPDATE_USER         = "updateUser";
    public static final String UPDATE_PASSWORD     = "updatePassword";
    public static final String GET_ALL_USERS        = "getAllUsers";
    public static final String SEARCH_USERS        = "searchUsers";
    public static final String FIND_USERS_BY_ROLE  = "findUsersByRole";
    /** 学生修改个人联系方式（phone/email/address），走 UserService.updateUserInfo */
    public static final String USER_UPDATE         = "user_update";
    /** 找回密码：请求发送验证码到邮箱（data=账号或邮箱） */
    public static final String REQUEST_PASSWORD_RESET = "requestPasswordReset";
    /** 找回密码：用验证码重置密码（data=[账号或邮箱, 验证码, 新密码]） */
    public static final String RESET_PASSWORD       = "resetPassword";

    // ========================= 教师 =========================
    public static final String TEACHER_ID_BY_USERID = "teacher_id_by_userid";
    public static final String TEACHER_GET          = "teacher_get";
    public static final String TEACHER_UPDATE       = "teacher_update";
    public static final String LIST_TEACHERS        = "listTeachers";
    public static final String GET_TEACHER_BY_ID    = "getTeacherById";

    // ========================= 学生 / 学籍 =========================
    public static final String STUDENT_LIST         = "student_list";
    public static final String STUDENT_GET          = "student_get";
    public static final String STUDENT_ADD          = "student_add";
    public static final String STUDENT_UPDATE       = "student_update";
    public static final String STUDENT_DELETE       = "student_delete";
    public static final String STUDENT_AUDIT_LIST   = "student_audit_list";
    public static final String STUDENT_AUDIT_PASS   = "student_audit_pass";
    public static final String STUDENT_AUDIT_REJECT = "student_audit_reject";
    public static final String STUDENT_AUDIT_SUBMIT = "student_audit_submit";
    public static final String STUDENT_AUDIT_STATUS = "student_audit_status";
    public static final String STUDENT_AUDIT_REMARK = "student_audit_remark";
    public static final String STUDENT_PERSONAL_SAVE = "student_personal_save";
    public static final String STUDENT_ID_BY_USERID = "student_id_by_userid";

    // ========================= 图书 / 借阅 =========================
    public static final String FIND_ALL_BOOKS      = "findAllBooks";
    public static final String FIND_BOOKS_BY_CATEGORY = "findBooksByCategory";
    public static final String FIND_BOOK_BY_NAME   = "findBookByName";
    public static final String FIND_BOOK_BY_AUTHOR = "findBookByAuthor";
    public static final String FIND_BOOK_BY_ID     = "findBookById";
    public static final String ADD_BOOK            = "addBook";
    public static final String DELETE_BOOK         = "deleteBook";
    public static final String UPDATE_BOOK         = "updateBook";
    public static final String BORROW_BOOK         = "borrowBook";
    public static final String RETURN_BOOK         = "returnBook";
    public static final String GET_ALL_RECORDS     = "getAllRecords";
    public static final String GET_USER_RECORDS    = "getUserRecords";
    public static final String UPDATE_BORROW_RECORD = "updateBorrowRecord";

    // ========================= 期刊 =========================
    public static final String ADD_JOURNAL         = "addJournal";
    public static final String UPDATE_JOURNAL       = "updateJournal";
    public static final String DELETE_JOURNAL       = "deleteJournal";
    public static final String GET_JOURNAL_BY_ID   = "getJournalById";
    public static final String GET_ALL_JOURNALS     = "getAllJournals";

    // ========================= 课程 / 排课 / 选课 =========================
    public static final String LIST_LESSONS        = "listLessons";
    public static final String LIST_LESSON_TIMES   = "listLessonTimes";
    /** 批量：一次返回全部 lesson_time 行，消除逐课 N+1 */
    public static final String LIST_ALL_LESSON_TIMES = "listAllLessonTimes";
    /** 批量：一次返回全部 lesson 的已选人数 Map<lessonId,count>，消除逐课 N+1 */
    public static final String COUNT_ENROLLED_ALL   = "countEnrolledAll";
    public static final String ENROLL               = "enroll";
    public static final String DROP                 = "drop";
    public static final String LIST_MY_ENROLLMENTS = "listMyEnrollments";
    public static final String GET_COURSE_BY_ID    = "getCourseById";
    public static final String GET_LESSON_BY_ID    = "getLessonById";
    public static final String LIST_COURSES        = "listCourses";
    public static final String CREATE_COURSE       = "createCourse";
    public static final String UPDATE_COURSE       = "updateCourse";
    public static final String DELETE_COURSE       = "deleteCourse";
    public static final String CREATE_LESSON       = "createLesson";
    public static final String UPDATE_LESSON       = "updateLesson";
    public static final String DELETE_LESSON       = "deleteLesson";
    public static final String COUNT_ENROLLED      = "countEnrolled";
    public static final String CREATE_LESSON_TIME  = "createLessonTime";
    public static final String UPDATE_LESSON_TIME  = "updateLessonTime";
    public static final String DELETE_LESSON_TIME  = "deleteLessonTime";
    public static final String LIST_ENROLLMENTS_BY_LESSON = "listEnrollmentsByLesson";

    // ========================= 商店 / 商品 / 订单 =========================
    public static final String ADD_PRODUCT         = "addProduct";
    public static final String GET_ALL_PRODUCTS    = "getAllProducts";
    public static final String GET_AVAILABLE_PRODUCTS = "getAvailableProducts";
    public static final String DELETE_PRODUCT      = "deleteProduct";
    public static final String SEARCH_PRODUCTS_BY_NAME = "searchProductsByName";
    public static final String GET_PRODUCT_BY_ID   = "getProductById";
    public static final String UPDATE_PRODUCT      = "updateProduct";
    public static final String ADD_PRODUCT_CATEGORY = "addProductCategory";
    public static final String DELETE_PRODUCT_CATEGORY = "deleteProductCategory";
    public static final String GET_ALL_CATEGORIES  = "getAllCategories";
    public static final String GET_PRODUCTS_BY_CATEGORY = "getProductsByCategory";
    public static final String GET_CATEGORY_BY_ID  = "getCategoryById";

    public static final String GET_ALL_ORDERS      = "getAllOrders";
    public static final String GET_USER_ORDERS     = "getUserOrders";
    public static final String GET_ORDER_DETAIL    = "getOrderDetail";
    public static final String CREATE_ORDER        = "createOrder";
    public static final String PAY_ORDER           = "payOrder";
    public static final String UPDATE_ORDER_STATUS = "updateOrderStatus";
    public static final String DELETE_ORDER       = "deleteOrder";
    public static final String CANCEL_ORDER        = "cancelOrder";
    public static final String IS_PRODUCT_IN_ORDERS = "isProductInOrders";
}
