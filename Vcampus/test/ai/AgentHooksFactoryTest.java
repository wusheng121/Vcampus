package ai;

import client.ai.AgentHooksFactory;
import client.ai.CourseAgentHooks;
import client.ai.GeneralAgentHooks;
import client.ai.LibraryAgentHooks;
import client.ai.ShopAgentHooks;
import client.ai.TeacherAgentHooks;
import common.model.User;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * {@link AgentHooksFactory#forContext} 分发逻辑测试（纯逻辑，不依赖服务端）。
 */
class AgentHooksFactoryTest {

    private User user(String role) {
        User u = new User();
        u.setType(role);
        u.setUserId(role + "1");
        return u;
    }

    @Test
    void studentContext() {
        User s = user("student");
        assertTrue(AgentHooksFactory.forContext(s, "图书馆") instanceof LibraryAgentHooks);
        assertTrue(AgentHooksFactory.forContext(s, "选课系统") instanceof CourseAgentHooks);
        assertTrue(AgentHooksFactory.forContext(s, "商店") instanceof ShopAgentHooks);
        assertTrue(AgentHooksFactory.forContext(s, "首页") instanceof GeneralAgentHooks);
        assertTrue(AgentHooksFactory.forContext(s, "Welcome") instanceof GeneralAgentHooks);
    }

    @Test
    void teacherContext() {
        User t = user("teacher");
        assertTrue(AgentHooksFactory.forContext(t, "选课系统") instanceof TeacherAgentHooks);
        assertTrue(AgentHooksFactory.forContext(t, "图书馆") instanceof LibraryAgentHooks);
        assertTrue(AgentHooksFactory.forContext(t, "商店") instanceof ShopAgentHooks);
    }

    @Test
    void adminContext() {
        User a = user("admin");
        // 管理员的"选课系统"是课程管理，无个人选课，走通用兜底
        assertTrue(AgentHooksFactory.forContext(a, "选课系统") instanceof GeneralAgentHooks);
        assertTrue(AgentHooksFactory.forContext(a, "图书馆") instanceof LibraryAgentHooks);
        assertTrue(AgentHooksFactory.forContext(a, "用户管理") instanceof GeneralAgentHooks);
    }

    @Test
    void titleFor() {
        assertTrue(AgentHooksFactory.titleFor("图书馆").contains("图书馆"));
        assertTrue(AgentHooksFactory.titleFor("选课系统").contains("选课"));
        assertTrue(AgentHooksFactory.titleFor("商店").contains("商店"));
        assertTrue(AgentHooksFactory.titleFor("首页").contains("校园"));
    }
}
