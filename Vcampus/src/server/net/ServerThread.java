package server.net;

import common.model.*;
import common.net.Message;
import common.net.MessageType;
import server.dao.*;
import server.service.*;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ServerThread implements Runnable {
    private Socket socket;
    private UserService userService = new UserService();
    private BookService bookService = new BookServiceImpl();
    private BorrowRecordService borrowRecordService = new BorrowRecordService();
    private JournalService journalService = new JournalServiceImpl();
    private CourseService courseService = new CourseService();
    private StudentService studentService = new StudentService();
    private ProductService productService;
    private OrderService orderService = new OrderService();
    private final TeacherService teacherService = new TeacherServiceImpl();


    public ServerThread(Socket socket) {

        this.socket = socket;
        // 初始化新增的服务（产品、订单）
        ProductDAO productDAO = new ProductDAOImpl();
        OrderDAO orderDAO = new OrderDAOImpl();
        this.productService = new ProductService(productDAO, orderDAO);
    }

    @Override
    public void run() {
        System.out.println("[ServerThread] 客户端已连接: " + socket.getRemoteSocketAddress());

        try (
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
        ) {
            out.flush(); // 确保流头发送出去
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Message request = (Message) in.readObject();
                System.out.println("[ServerThread] 收到请求 type=" + request.getType());

                Message response = new Message();

                switch (request.getType()) {
                    case "login" -> {
                        User user = (User) request.getData();
                        User verified = userService.login(user.getUserId(), user.getPassword());
                        if (verified != null) {
                            response.setStatus("success");
                            response.setData(verified);
                            response.setMsg("登录成功");
                        } else {
                            response.setStatus("fail");
                            response.setMsg("账号或密码错误");
                        }
                    }
                    case "teacher_id_by_userid" -> {
                        String userId = (String) request.getData();
                        Teacher teacher = teacherService.getTeacherByUserId(userId);
                        if (teacher != null) {
                            response.setStatus("success");
                            response.setData(teacher.getTeacherId());
                            response.setMsg("查询教师ID成功");
                        } else {
                            response.setStatus("fail");
                            response.setMsg("该用户未绑定教师信息");
                        }
                    }

                    case "teacher_get" -> {
                        String teacherId = (String) request.getData();
                        Teacher teacher = teacherService.getTeacherById(teacherId);
                        if (teacher != null) {
                            response.setStatus("success");
                            response.setData(teacher);
                            response.setMsg("获取教师信息成功");
                        } else {
                            response.setStatus("fail");
                            response.setMsg("教师不存在");
                        }
                    }
                    case "teacher_update" -> {
                        Teacher t = (Teacher) request.getData();
                        boolean ok = teacherService.updateTeacher(t);
                        response.setStatus(ok ? "success" : "fail");
                        response.setMsg(ok ? "更新成功" : "更新失败");
                    }
                    case "user_update" -> {
                        User u = (User) request.getData();
                        boolean ok = userService.updateUserInfo(u);   // 只更新个人信息字段
                        response.setStatus(ok ? "success" : "fail");
                        response.setMsg(ok ? "更新成功" : "更新失败");
                    }
                    case "addUser" -> {
                        User newUser = (User) request.getData();
                        //String operatorRole = request.getData("role"); // 发送方需要传当前操作人角色
                        boolean ok = userService.registerUser(newUser);
                        response.setStatus(ok ? "success" : "fail");
                        response.setMsg(ok ? "用户添加成功" : "用户添加失败（权限不足或已存在）");
                    }

                    case "deleteUser" -> {
                        String userId = (String) request.getData();
                        //String operatorRole = request.getData("role");
                        boolean ok = userService.deleteUser(userId);
                        response.setStatus(ok ? "success" : "fail");
                        response.setMsg(ok ? "用户删除成功" : "用户删除失败（权限不足或不存在）");
                    }

                    case "updateUser" -> {
                        User updateUser = (User) request.getData();
                        //String operatorRole = request.getData("role");
                        boolean ok = userService.updateUser(updateUser);
                        response.setStatus(ok ? "success" : "fail");
                        response.setMsg(ok ? "用户更新成功" : "用户更新失败（权限不足或不存在）");
                    }

                    case "updatePassword" -> {
                        System.out.println("[RAW] 收到 updatePassword 请求");
                        System.out.println("[TYPE] 数据类型: " + request.getData().getClass().getName());// 必打印
                        try {
                            PasswordDTO dto = (PasswordDTO) request.getData();

                            // 添加详细的调试信息
                            System.out.println("[DEBUG] 接收到的 PasswordDTO:");
                            System.out.println("[DEBUG]   userId: " + dto.getUserId());
                            System.out.println("[DEBUG]   oldPassword: " + dto.getOldPassword());
                            System.out.println("[DEBUG]   newPassword: " + dto.getNewPassword());

                            boolean ok = userService.updatePassword(dto.getUserId(),
                                    dto.getOldPassword(),
                                    dto.getNewPassword());

                            response.setStatus(ok ? "success" : "fail");
                            response.setMsg(ok ? "密码修改成功" : "原密码错误");

                            // 添加响应状态日志
                            System.out.println("[DEBUG] 密码修改结果: " + (ok ? "成功" : "失败"));
                        } catch (Exception e) {
                            response.setStatus("fail");
                            response.setMsg("服务端异常：" + e.getMessage());
                            e.printStackTrace();
                        }
                    }

                    case "getAllUsers" -> {
                        response.setStatus("success");
                        response.setData(userService.getAllUsers());
                    }

                    case "searchUsers" -> {
                        String keyword = (String) request.getData();
                        response.setStatus("success");
                        response.setData(userService.searchUsers(keyword));
                    }

                    case "findUsersByRole" -> {
                        String role = (String) request.getData();
                        response.setStatus("success");
                        response.setData(userService.findUsersByRole(role));
                    }
                    case "findAllBooks" -> {
                        response.setData(bookService.getAllBooks());
                        response.setStatus("success");
                        response.setMsg("获取所有书籍成功");
                    }case "findBooksByCategory" -> {
                        Book book = (Book) request.getData();

                        response.setStatus("success");
                        response.setData(bookService.findBookByCategory(book.getTheme()));
                    }
                    case "addBook" -> {
                        Book book = (Book) request.getData();
                        boolean result = bookService.addBook(book);
                        response.setStatus(result ? "success" : "fail");
                        response.setMsg(result ? "新增书籍成功" : "新增书籍失败");
                    }
                    case "deleteBook" -> {
                        Book book = (Book) request.getData();
                        boolean result = bookService.deleteBook(book.getBookId());
                        response.setStatus(result ? "success" : "fail");
                        response.setMsg(result ? "删除成功" : "删除失败");
                    }
                    case "updateBook" -> {
                        Book book = (Book) request.getData();
                        boolean result = bookService.updateBook(book.getBookId(), book);
                        response.setStatus(result ? "success" : "fail");
                        response.setMsg(result ? "修改成功" : "修改失败");
                    }
                    case "borrowBook" -> {
                        BorrowRecord record = (BorrowRecord) request.getData();
                        boolean result = borrowRecordService.borrowBook(record.getUserId(), record.getBookId());
                        response.setStatus(result ? "success" : "fail");
                        response.setMsg(result ? "借书成功" : "借书失败");
                    }
                    case "returnBook" -> {
                        BorrowRecord record = (BorrowRecord) request.getData();
                        boolean result = borrowRecordService.returnBook(record);
                        response.setStatus(result ? "success" : "fail");
                        response.setMsg(result ? "还书成功" : "还书失败");
                    }
                    case "getAllRecords" -> {
                        //BorrowRecord record = (BorrowRecord) request.getData();
                        response.setData(borrowRecordService.getAllRecords());
                        response.setStatus("success");
                        response.setMsg("获取所有借阅记录成功");
                    }
                    case "getUserRecords" -> {
                        String userId = (String) request.getData();
                        response.setData(borrowRecordService.getUserRecords(userId));
                        response.setStatus("success");
                        response.setMsg("获取用户借阅记录成功");
                    }
                    case "addJournal" -> {
                        Journal journalToAdd = (Journal) request.getData();
                        boolean addResult = journalService.addJournal(journalToAdd);
                        response.setStatus(addResult ? "success" : "fail");
                        response.setData(addResult);
                        response.setMsg(addResult ? "新增期刊成功" : "新增期刊失败");
                    }
                    case "updateJournal" -> {
                        Journal journalToUpdate = (Journal) request.getData();
                        boolean updateResult = journalService.updateJournal(journalToUpdate);
                        response.setStatus(updateResult ? "success" : "fail");
                        response.setData(updateResult);
                        response.setMsg(updateResult ? "更新期刊成功" : "更新期刊失败");
                    }
                    case "deleteJournal" -> {
                        int journalIdToDelete = (Integer) request.getData();
                        boolean deleteResult = journalService.deleteJournal(journalIdToDelete);
                        response.setStatus(deleteResult ? "success" : "fail");
                        response.setData(deleteResult);
                        response.setMsg(deleteResult ? "删除期刊成功" : "删除期刊失败");
                    }
                    case "getJournalById" -> {
                        int journalId = (Integer) request.getData();
                        Journal journal = journalService.getJournalById(journalId);
                        response.setStatus(journal != null ? "success" : "fail");
                        response.setData(journal);
                        response.setMsg(journal != null ? "获取期刊成功" : "未找到期刊");
                    }
                    case "getAllJournals" -> {
                        List<Journal> journals = journalService.getAllJournals();
                        response.setStatus("success");
                        response.setData(journals);
                        response.setMsg("获取所有期刊成功");
                    }
                    case "listLessons" -> {
                        List<Lesson> lessons = courseService.listLessons();
                        response.setStatus("success");
                        response.setData(lessons);
                    }
                    case "listLessonTimes" -> {
                        int lessonId = (int) request.getData();
                        List<LessonTime> times = courseService.listLessonTimes(lessonId);
                        response.setStatus("success");
                        response.setData(times);
                    }
                    case "enroll" -> {
                        Object[] arr = (Object[]) request.getData();
                        String studentId = (String) arr[0];
                        int lessonId = (int) arr[1];
                        CourseService.Result result = courseService.enroll(studentId, lessonId);
                        response.setStatus(result.ok ? "success" : "fail");
                        response.setMsg(result.msg);
                    }
                    case "drop" -> {
                        Object[] arr = (Object[]) request.getData();
                        String studentId = (String) arr[0];
                        int lessonId = (int) arr[1];
                        CourseService.Result result = courseService.drop(studentId, lessonId);
                        response.setStatus(result.ok ? "success" : "fail");
                        response.setMsg(result.msg);
                    }
                    case "listMyEnrollments" -> {
                        String studentId = (String) request.getData();
                        List<Enrollment> enrollments = courseService.listEnrollmentsByStudent(studentId);
                        response.setStatus("success");
                        response.setData(enrollments);
                    }
                    case "getCourseById" -> {
                        int courseId = (int) request.getData();
                        Course course = courseService.getCourse(courseId);
                        response.setStatus("success");
                        response.setData(course);
                    }
                    case "getLessonById" -> {
                        int lessonId = (int) request.getData();
                        Lesson lesson = courseService.getLesson(lessonId);
                        response.setStatus("success");
                        response.setData(lesson);
                    }
                    case "listCourses" -> {
                        List<Course> courses = courseService.listCourses();
                        response.setStatus("success");
                        response.setData(courses);
                    }
                    case "createCourse" -> {
                        Course c = (Course) request.getData();                 // 传入：Course（不带/忽略 courseId）

                        CourseService.Result r = courseService.createCourse(c);
                        response.setStatus(r.ok ? "success" : "fail");
                        response.setMsg(r.msg);
                        if (r.ok) {
                            // DAO 已经把新生成的 courseId 回填到 c 里了
                            response.setData(c);                               // 可选：把创建后的课程（含新ID）回传
                        }
                    }
                    case "updateCourse" -> {
                        Course c = (Course) request.getData();                 // 传入：Course（需包含有效 courseId）

                        CourseService.Result r = courseService.updateCourse(c);
                        response.setStatus(r.ok ? "success" : "fail");
                        response.setMsg(r.msg);

                    }
                    case "deleteCourse" -> {
                        int courseId = (int) request.getData();                // 传入：Integer courseId

                        CourseService.Result r = courseService.deleteCourse(courseId);
                        response.setStatus(r.ok ? "success" : "fail");
                        response.setMsg(r.msg);
                    }
                    case "createLesson" -> {
                        Lesson l = (Lesson) request.getData();                 // 传入：Lesson（不带/忽略 lessonId）

                        CourseService.Result r = courseService.createLesson(l);
                        response.setStatus(r.ok ? "success" : "fail");
                        response.setMsg(r.msg);
                        if (r.ok) {
                            // DAO 已把新生成的 lessonId 回填到 l 里了（见 CourseDAOImpl.addLesson）
                            response.setData(l);                                // 可选：把创建后的 Lesson（含新ID）回传
                        }

                    }
                    case "updateLesson" -> {
                        Lesson l = (Lesson) request.getData();                 // 传入：Lesson（需包含有效 lessonId）

                        CourseService.Result r = courseService.updateLesson(l);
                        response.setStatus(r.ok ? "success" : "fail");
                        response.setMsg(r.msg);

                    }
                    case "deleteLesson" -> {
                        int lessonId = (int) request.getData();                // 传入：Integer lessonId

                        CourseService.Result r = courseService.deleteLesson(lessonId);
                        response.setStatus(r.ok ? "success" : "fail");
                        response.setMsg(r.msg);
                    }
                    case "countEnrolled" -> {
                        int lessonId = (int) request.getData();
                        int n = courseService.countEnrolled(lessonId);
                        response.setStatus("success");
                        response.setData(n);
                    }
                    case "createLessonTime" -> {
                        LessonTime t = (LessonTime) request.getData();
                        CourseService.Result r = courseService.createLessonTime(t);
                        response.setStatus(r.ok ? "success" : "fail");
                        response.setMsg(r.msg);
                        if (r.ok) response.setData(t);

                    }
                    case "updateLessonTime" -> {
                        LessonTime t = (LessonTime) request.getData();
                        CourseService.Result r = courseService.updateLessonTime(t);
                        response.setStatus(r.ok ? "success" : "fail");
                        response.setMsg(r.msg);

                    }
                    case "deleteLessonTime" -> {
                        int timeId = (int) request.getData();
                        CourseService.Result r = courseService.deleteLessonTime(timeId);
                        response.setStatus(r.ok ? "success" : "fail");
                        response.setMsg(r.msg);
                    }
                    case "listTeachers" -> {
                        List<Teacher> teachers = courseService.listTeachers();
                        response.setStatus("success");
                        response.setData(teachers);
                    }
                    case "getTeacherById" -> {
                        String id = (String) request.getData();
                        Teacher t = courseService.getTeacherById(id);
                        response.setStatus("success");
                        response.setData(t);
                    }
                    case "listEnrollmentsByLesson" -> {
                        int lessonId = (int) request.getData();
                        List<Enrollment> list = courseService.listEnrollmentsByLesson(lessonId);
                        response.setStatus("success");
                        response.setData(list);
                    }
                    // -------------------- 学生模块 --------------------
                    case MessageType.STUDENT_LIST -> {
                        response.setStatus("success");
                        response.setData(new ArrayList<>(studentService.getAllStudents()));
                    }
                    case MessageType.STUDENT_GET -> {
                        String id = (String) request.getData();
                        response.setStatus("success");
                        response.setData(studentService.getStudent(id));
                    }
                    case MessageType.STUDENT_ADD -> {
                        Student stu = (Student) request.getData();
                        String err = studentService.addStudentAndGetErr(stu);   // 新写法：返回错误信息

                        boolean ok = (err == null);

                        response.setStatus(ok ? "success" : "fail");
                        response.setMsg(ok ? "" : "插入失败：" + (err.isEmpty() ? "学号/身份证/一卡通重复" : err));
                    }
                    case MessageType.STUDENT_UPDATE -> {
                        boolean ok = studentService.updateStudent((Student) request.getData());
                        response.setStatus(ok ? "success" : "fail");
                    }
                    case MessageType.STUDENT_DELETE -> {
                        boolean ok = studentService.deleteStudent((String) request.getData());
                        response.setStatus(ok ? "success" : "fail");
                    }
                    case MessageType.STUDENT_AUDIT_LIST -> {
                        response.setStatus("success");
                        response.setData(new ArrayList<>(studentService.getPendingAudits()));
                    }
                    case MessageType.STUDENT_AUDIT_PASS -> {
                        Object[] arr = (Object[]) request.getData();  // [studentId, auditor, remark]

                        boolean ok = studentService.audit((String) arr[0], true, (String) arr[1], (String) arr[2]);
                        response.setStatus(ok ? "success" : "fail");
                    }
                    case MessageType.STUDENT_AUDIT_REJECT -> {
                        Object[] arr = (Object[]) request.getData();
                        boolean ok = studentService.audit((String) arr[0], false, (String) arr[1], (String) arr[2]);
                        response.setStatus(ok ? "success" : "fail");
                    }
                    case MessageType.STUDENT_AUDIT_SUBMIT -> {
                        StudentPersonal sp = (StudentPersonal) request.getData();
                        boolean ok = studentService.submitPersonalAudit(sp);
                        response.setStatus(ok ? "success" : "fail");
                    }
                    case MessageType.STUDENT_AUDIT_STATUS -> {
                        String studentId = (String) request.getData();
                        String status = studentService.getPersonalAuditStatus(studentId);
                        response.setStatus("success");
                        response.setData(status);
                    }
                    case MessageType.STUDENT_AUDIT_REMARK -> {
                        String studentId = (String) request.getData();
                        String remark = studentService.getPersonalAuditRemark(studentId);
                        response.setStatus("success");
                        response.setData(remark);
                    }
                    case MessageType.STUDENT_PERSONAL_SAVE -> {
                        StudentPersonal sp = (StudentPersonal) request.getData();
                        boolean ok = studentService.saveOrUpdatePersonal(sp);
                        response.setStatus(ok ? "success" : "fail");
                    }
                    case MessageType.STUDENT_ID_BY_USERID -> {
                        String userId = (String) request.getData();
                        System.out.println("[诊断] 收到反查请求 userId = " + userId);
                        String stuId = new StudentDAO().findStudentIdByUserId(userId);
                        System.out.println("[诊断] 反查结果 stuId = " + stuId);

                        // ✅ 显式设置 status 和 data
                        response = new Message();
                        response.setStatus("success");
                        response.setData(stuId);
                        // msg 可不给
                    }
                    case "addProduct" -> {
                        Product product = (Product) request.getData();
                        User user = (User) request.getExtra();
                        if (user == null) {
                            response.setStatus("fail");
                            response.setMsg("用户会话已过期，请重新登录");
                            break;
                        }
                        if (!"admin".equalsIgnoreCase(user.getType())) {
                            response.setStatus("fail");
                            response.setMsg("权限不足，需要管理员权限");
                            break;
                        }
                        if (product == null || product.getProductId() == null) {
                            response.setStatus("fail");
                            response.setMsg("商品信息不完整");
                            break;
                        }
                        if (productService.addProduct(product, user.getType())) {
                            response.setStatus("success");
                            response.setMsg("商品添加成功");
                        } else {
                            response.setStatus("fail");
                            response.setMsg("添加商品失败，可能是商品ID已存在");
                        }
                    }
                    case "getAllProducts" -> {
                        List<Product> products = productService.getAllProducts();
                        response.setStatus("success");
                        response.setData(products);
                    }
                    case "getAvailableProducts" -> {
                        List<Product> products = productService.getAvailableProducts();
                        response.setStatus("success");
                        response.setData(products);
                    }
                    case "deleteProduct" -> {
                        String productId = (String) request.getData();
                        User user = (User) request.getExtra();
                        if (productService.deleteProduct(productId, user.getType())) {
                            response.setStatus("success");
                            response.setMsg("商品删除成功");
                        } else {
                            response.setStatus("fail");
                            response.setMsg("商品删除失败，可能原因：商品不存在、有关联订单或权限不足");
                        }
                    }
                    case "searchProductsByName" -> {
                        String name = (String) request.getData();
                        List<Product> products = productService.searchProductsByName(name);
                        response.setStatus("success");
                        response.setData(products);
                    }
                    case "getProductById" -> {
                        String productId = (String) request.getData();
                        Product product = productService.getProductById(productId);
                        if (product != null) {
                            response.setStatus("success");
                            response.setData(product);
                        } else {
                            response.setStatus("fail");
                            response.setMsg("未找到指定商品");
                        }
                    }
                    case "updateProduct" -> {
                        Product product = (Product) request.getData();
                        User user = (User) request.getExtra();
                        if (user == null) {
                            response.setStatus("fail");
                            response.setMsg("用户会话已过期，请重新登录");
                            break;
                        }
                        if (!"admin".equalsIgnoreCase(user.getType())) {
                            response.setStatus("fail");
                            response.setMsg("权限不足，需要管理员权限");
                            break;
                        }
                        if (product == null || product.getProductId() == null) {
                            response.setStatus("fail");
                            response.setMsg("商品信息不完整");
                            break;
                        }
                        if (productService.updateProduct(product, user.getType())) {
                            response.setStatus("success");
                            response.setMsg("商品更新成功");
                        } else {
                            response.setStatus("fail");
                            response.setMsg("商品更新失败");
                        }
                    }
                    case "getAllOrders" -> {
                        User user = (User) request.getData();
                        if (user == null || !"admin".equals(user.getType())) {
                            response.setStatus("fail");
                            response.setMsg("权限不足，需要管理员权限");
                            break;
                        }
                        List<Order> orders = orderService.getAllOrders();
                        response.setStatus("success");
                        response.setData(orders);
                    }
                    case "getUserOrders" -> {
                        User user = (User) request.getData();
                        if (user == null) {
                            response.setStatus("fail");
                            response.setMsg("用户未登录");
                            break;
                        }
                        List<Order> orders = orderService.getUserOrders(user.getUserId());
                        response.setStatus("success");
                        response.setData(orders);
                    }
                    case "getOrderDetail" -> {
                        String orderId = (String) request.getData();
                        Order order = orderService.getOrderDetail(orderId);
                        if (order != null) {
                            response.setStatus("success");
                            response.setData(order);
                        } else {
                            response.setStatus("fail");
                            response.setMsg("未找到指定订单");
                        }
                    }
                    case "createOrder" -> {
                        List<OrderItem> items = (List<OrderItem>) request.getData();
                        User user = (User) request.getExtra();
                        if (user == null) {
                            response.setStatus("fail");
                            response.setMsg("用户未登录，请重新登录");
                            break;
                        }
                        if (items == null || items.isEmpty()) {
                            response.setStatus("fail");
                            response.setMsg("订单商品不能为空");
                            break;
                        }
                        for (OrderItem item : items) {
                            if (item.getQuantity() <= 0) {
                                response.setStatus("fail");
                                response.setMsg("商品数量必须大于0");
                                break;
                            }
                            if (!productService.exists(item.getProductId())) {
                                response.setStatus("fail");
                                response.setMsg("商品不存在: " + item.getProductId());
                                break;
                            }
                        }
                        Map<String, Integer> insufficientItems = new HashMap<>();
                        for (OrderItem item : items) {
                            Product product = productService.getProductById(item.getProductId());
                            if (product.getStock() < item.getQuantity()) {
                                insufficientItems.put(product.getName(), item.getQuantity() - product.getStock());
                            }
                        }
                        if (!insufficientItems.isEmpty()) {
                            StringBuilder sb = new StringBuilder("库存不足:\n");
                            insufficientItems.forEach((name, shortage) ->
                                    sb.append("- ").append(name).append(" 缺少").append(shortage).append("件\n"));
                            response.setStatus("fail");
                            response.setMsg(sb.toString());
                            break;
                        }
                        boolean success = orderService.createOrder(user.getUserId(), items);
                        if (success) {
                            response.setStatus("success");
                            response.setMsg("订单创建成功");
                        } else {
                            response.setStatus("fail");
                            response.setMsg("订单创建失败");
                        }
                    }
                    case "payOrder" -> {
                        String orderId = (String) request.getData();
                        String[] extra = (String[]) request.getExtra();
                        String userId = extra[0];
                        String userType = extra[1];
                        boolean success = orderService.payOrder(orderId, userId, userType);
                        response.setStatus(success ? "success" : "fail");
                        response.setMsg(success ? "订单支付成功" : "订单支付失败");
                    }
                    case "updateOrderStatus" -> {
                        String orderId = (String) request.getData();
                        String[] extra = (String[]) request.getExtra();
                        String status = extra[0];
                        String operatorRole = extra[1];
                        boolean success = orderService.updateOrderStatus(orderId, status, operatorRole);
                        if (success) {
                            response.setStatus("success");
                            response.setMsg("订单状态更新成功");
                        } else {
                            response.setStatus("fail");
                            response.setMsg("订单状态更新失败");
                        }
                    }
                    case "deleteOrder" -> {
                        String orderId = (String) request.getData();
                        User user = (User) request.getExtra();
                        boolean success = orderService.deleteOrder(orderId, user.getType());
                        if (success) {
                            response.setStatus("success");
                            response.setMsg("订单删除成功");
                        } else {
                            response.setStatus("fail");
                            response.setMsg("订单删除失败");
                        }
                    }
                    case "cancelOrder" -> {
                        String orderId = (String) request.getData();
                        String[] extra = (String[]) request.getExtra();
                        String userId = extra[0];
                        String operatorRole = extra[1];
                        boolean success = orderService.cancelOrder(orderId, userId);
                        if (success) {
                            response.setStatus("success");
                            response.setMsg("订单取消成功");
                        } else {
                            response.setStatus("fail");
                            response.setMsg("订单取消失败");
                        }
                    }
                    case "isProductInOrders" -> {
                        String productId = (String) request.getData();
                        boolean exists = orderService.isProductInOrders(productId);
                        response.setStatus("success");
                        response.setData(exists);
                    }
                    case "addProductCategory" -> {
                        ProductCategory category = (ProductCategory) request.getData();
                        User user = (User) request.getExtra();
                        if (!"admin".equals(user.getType())) {
                            response.setStatus("fail");
                            response.setMsg("需要管理员权限");
                            break;
                        }
                        boolean success = productService.addProductCategory(category);
                        response.setStatus(success ? "success" : "fail");
                    }
                    case "deleteProductCategory" -> {
                        String categoryId = (String) request.getData();
                        User user = (User) request.getExtra();
                        if (!"admin".equals(user.getType())) {
                            response.setStatus("fail");
                            response.setMsg("需要管理员权限");
                            break;
                        }
                        boolean success = productService.deleteProductCategory(categoryId);
                        response.setStatus(success ? "success" : "fail");
                    }
                    case null, default -> {
                        response.setStatus("fail");
                        response.setMsg("未知请求类型：" + request.getType());
                    }

                }

                out.writeObject(response);
                out.flush();
                System.out.println("[ServerThread] 响应已发送");
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close();
            } catch (Exception ignore) {
            }
        }
    }}

