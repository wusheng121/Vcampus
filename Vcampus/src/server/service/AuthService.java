package server.service;

import server.dao.PermissionDAO;
import server.dao.PermissionDAOImpl;

/**
 * 授权服务（RBAC）：按角色查询是否持有权限点。
 * <p>当前为"仅数据模型"交付——提供查询能力，尚未接入各 handler 的调用流程
 * （接入时在 handler 用 {@code authService.hasPermission(user.getType(), Permissions.XXX)} 拦截即可）。
 */
public class AuthService {
    private final PermissionDAO permissionDAO = new PermissionDAOImpl();

    /** 角色 role 是否拥有权限 permCode */
    public boolean hasPermission(String role, String permCode) {
        if (role == null || permCode == null) return false;
        return permissionDAO.hasPermission(role, permCode);
    }

    /** 取角色全部权限码 */
    public java.util.List<String> getPermissions(String role) {
        return permissionDAO.getPermissions(role);
    }
}
