package common.net;

import common.model.User;

import java.io.Serializable;

public class Message implements Serializable {
    private static final long serialVersionUID = 1L;

    private String type;   // 消息类型，例如 "login"
    private Object data;   // 消息体，可放User对象或Map
    private Object extra;  // 额外数据（用于传递操作详情等）
    private User caller;   // 调用方身份（由 ClientSocket 自动注入登录用户，供服务端 RBAC）
    private String status; // 可选，返回状态
    private String msg;    // 可选，返回提示

    // 默认构造器
    public Message() {}

    // 带 type 和 data 的构造器
    public Message(String type, Object data) {
        this.type = type;
        this.data = data;
    }

    // 带 type, data, status, msg 的构造器
    public Message(String type, Object data, String status, String msg) {
        this.type = type;
        this.data = data;
        this.status = status;
        this.msg = msg;
    }

    // Getter 和 Setter 方法
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getExtra() { return extra; }
    public void setExtra(Object extra) { this.extra = extra; }

    public User getCaller() { return caller; }
    public void setCaller(User caller) { this.caller = caller; }

    @Override
    public String toString() {
        return "Message{" +
                "type='" + type + '\'' +
                ", data=" + data +
                ", status='" + status + '\'' +
                ", msg='" + msg + '\'' +
                '}';
    }
}
