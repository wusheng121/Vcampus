package server.dao;

import java.util.List;

/**
 * RBAC 权限查询 DAO：判断某角色是否持有某权限点，或取某角色的全部权限。
 */
public interface PermissionDAO {

    /** 角色 role 是否拥有权限 permCode */
    boolean hasPermission(String role, String permCode);

    /** 取角色 role 的全部权限码 */
    List<String> getPermissions(String role);
}
