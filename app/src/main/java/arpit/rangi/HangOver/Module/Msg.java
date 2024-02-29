package arpit.rangi.HangOver.Module;

public class Msg {
    private String uid;
    private Object msg;
    private String seen;
    private String msgId;
    private String replay;
    private String msgType;
    private String receiver;
    private Object time;

    public Msg(String uid, Object msg, Object time, String seen, String msgType) {
        this.uid = uid;
        this.msg = msg;
        this.time = time;
        this.seen = seen;
        this.msgType = msgType;
    }
    public Msg(String uid, Object msg, Object time, String seen, String msgType, String replay) {
        this.uid = uid;
        this.msg = msg;
        this.time = time;
        this.seen = seen;
        this.msgType = msgType;
        this.replay = replay;
    }
    public Msg(String uid, Object msg, Object time) {
        this.uid = uid;
        this.msg = msg;
        this.time = time;
    }
    public Msg(String uid, Object msg, Object time, String seen, String msgId, String msgType, String receiver) {
        this.uid = uid;
        this.msg = msg;
        this.time = time;
        this.seen = seen;
        this.msgId = msgId;
        this.msgType = msgType;
        this.receiver = receiver;

    }
    public Msg(String uid, Object msg, Object time, String seen, String msgId, String msgType, String receiver, String replay) {
        this.uid = uid;
        this.msg = msg;
        this.time = time;
        this.seen = seen;
        this.msgId = msgId;
        this.msgType = msgType;
        this.receiver = receiver;
        this.replay = replay;

    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getSeen() {
        return seen;
    }

    public void setSeen(String seen) {
        this.seen = seen;
    }

    public String getMsgId() {
        return msgId;
    }
    public void setReceiver(String receiver) {
        this.seen = receiver;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setMsgId(String msgId) {
        this.msgId = msgId;
    }
    public String getMsgType() {
        return msgType;
    }
    public void setReplay(String Replay) {
        this.replay = replay;
    }
    public String getReplay() {
        return replay;
    }

    public void setMsgType(String msgType) {
        this.msgType = msgType;
    }

    public Object getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public Object getTime() {
        return time;
    }

    public void setTime(Object time) {
        this.time = time;
    }
}
