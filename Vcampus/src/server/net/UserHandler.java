package server.net;

import common.model.PasswordDTO;
import common.model.Teacher;
import common.model.User;
import common.net.Message;
import common.net.MessageType;
import common.Permissions;
import server.service.AuthService;
import server.service.TeacherService;
import server.service.TeacherServiceImpl;
import server.service.UserService;

/**
 * 认证 / 用户 / 教师域
 */
public class UserHandler implements Handler {
    private final UserService userService = new UserService();
    private final TeacherService teacherService = new TeacherServiceImpl();
    private final AuthService auth = new AuthService();

    /** RBAC 拦截：调用方需持有 perm 权限，否则填 fail 响应并返回 true（已拦截） */
    private boolean denied(Message request, Message response, String perm) {
        User c = request.getCaller();
        if (c == null || !auth.hasPermission(c.getType(), perm)) {
            response.setStatus("fail");
            response.setMsg("无权限：" + perm);
            return true;
        }
        return false;
    }

    @Override
    public Message handle(Message request) {
        Message response = new Message();
        switch (request.getType()) {
            case MessageType.LOGIN -> {
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
            case MessageType.TEACHER_ID_BY_USERID -> {
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
            case MessageType.TEACHER_GET -> {
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
            case MessageType.TEACHER_UPDATE -> {
                Teacher t = (Teacher) request.getData();
                boolean ok = teacherService.updateTeacher(t);
                response.setStatus(ok ? "success" : "fail");
                response.setMsg(ok ? "更新成功" : "更新失败");
            }
            case MessageType.USER_UPDATE -> {
                User u = (User) request.getData();
                boolean ok = userService.updateUserInfo(u);   // 只更新个人信息字段
                response.setStatus(ok ? "success" : "fail");
                response.setMsg(ok ? "更新成功" : "更新失败");
            }
            case MessageType.ADD_USER -> {
                if (denied(request, response, Permissions.USER_CREATE)) break;
                User newUser = (User) request.getData();
                boolean ok = userService.registerUser(newUser);
                response.setStatus(ok ? "success" : "fail");
                response.setMsg(ok ? "用户添加成功" : "用户添加失败（权限不足或已存在）");
            }
            case MessageType.DELETE_USER -> {
                if (denied(request, response, Permissions.USER_DELETE)) break;
                String userId = (String) request.getData();
                boolean ok = userService.deleteUser(userId);
                response.setStatus(ok ? "success" : "fail");
                response.setMsg(ok ? "用户删除成功" : "用户删除失败（权限不足或不存在）");
            }
            case MessageType.UPDATE_USER -> {
                if (denied(request, response, Permissions.USER_UPDATE)) break;
                User updateUser = (User) request.getData();
                boolean ok = userService.updateUser(updateUser);
                response.setStatus(ok ? "success" : "fail");
                response.setMsg(ok ? "用户更新成功" : "用户更新失败（权限不足或不存在）");
            }
            case MessageType.UPDATE_PASSWORD -> {
                System.out.println("[RAW] 收到 updatePassword 请求");
                System.out.println("[TYPE] 数据类型: " + request.getData().getClass().getName());
                try {
                    PasswordDTO dto = (PasswordDTO) request.getData();
                    System.out.println("[DEBUG] 接收到的 PasswordDTO:");
                    System.out.println("[DEBUG]   userId: " + dto.getUserId());
                    System.out.println("[DEBUG]   oldPassword: " + dto.getOldPassword());
                    System.out.println("[DEBUG]   newPassword: " + dto.getNewPassword());

                    boolean ok = userService.updatePassword(dto.getUserId(),
                            dto.getOldPassword(),
                            dto.getNewPassword());

                    response.setStatus(ok ? "success" : "fail");
                    response.setMsg(ok ? "密码修改成功" : "原密码错误");
                    System.out.println("[DEBUG] 密码修改结果: " + (ok ? "成功" : "失败"));
                } catch (Exception e) {
                    response.setStatus("fail");
                    response.setMsg("服务端异常：" + e.getMessage());
                    e.printStackTrace();
                }
            }
            case MessageType.GET_ALL_USERS -> {
                response.setStatus("success");
                response.setData(userService.getAllUsers());
            }
            case MessageType.SEARCH_USERS -> {
                String keyword = (String) request.getData();
                response.setStatus("success");
                response.setData(userService.searchUsers(keyword));
            }
            case MessageType.FIND_USERS_BY_ROLE -> {
                String role = (String) request.getData();
                response.setStatus("success");
                response.setData(userService.findUsersByRole(role));
            }
            case MessageType.REQUEST_PASSWORD_RESET -> {
                String userIdOrEmail = (String) request.getData();
                String msg = userService.requestPasswordReset(userIdOrEmail);
                response.setStatus("success");
                response.setMsg(msg);
            }
            case MessageType.RESET_PASSWORD -> {
                Object[] arr = (Object[]) request.getData(); // [账号或邮箱, 验证码, 新密码]
                String userIdOrEmail = (String) arr[0];
                String code = (String) arr[1];
                String newPwd = (String) arr[2];
                String result = userService.resetPassword(userIdOrEmail, code, newPwd);
                if ("success".equals(result)) {
                    response.setStatus("success");
                    response.setMsg("密码重置成功，请用新密码登录");
                } else {
                    response.setStatus("fail");
                    response.setMsg(result);
                }
            }
            default -> {
                return null;
            }
        }
        return response;
    }
}
