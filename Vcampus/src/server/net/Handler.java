package server.net;

import common.net.Message;

/**
 * 请求处理器接口。各域（用户/学生/课程/图书/期刊/商店）分别实现。
 * 约定：若 {@code type} 不属于本处理器负责的域，返回 {@code null}，
 * 由 {@link ServerThread} 继续询问下一个处理器；全部不命中则回退为"未知请求类型"。
 */
public interface Handler {
    Message handle(Message request);
}
