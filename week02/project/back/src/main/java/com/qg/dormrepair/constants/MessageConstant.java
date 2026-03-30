package com.qg.dormrepair.constants;

public class MessageConstant {

    // 通用
    public static final String UNKNOWN_ERROR = "未知错误";
    public static final String USER_NOT_LOGIN = "用户未登录，请重新登录";
    public static final String ALREADY_EXISTS = "已存在";
    public static final String NO_PERMISSION = "无权限操作";
    public static final String DELETE_FAILED = "删除失败";

    // 分页参数
    public static final String PAGE_NUM_INVALID = "页码不能小于1";
    public static final String PAGE_SIZE_INVALID = "每页条数不能小于1";

    // Token 相关
    public static final String REFRESH_TOKEN_NOT_EMPTY = "refreshToken不能为空";
    public static final String REFRESH_TOKEN_INVALID = "Refresh Token 已失效";
    public static final String REFRESH_TOKEN_EXPIRED = "Refresh Token 已过期";
    public static final String TOKEN_TYPE_ILLEGAL = "非法 Token，类型错误";
    public static final String TOKEN_INVALID = "Token 无效，无法获取用户信息";

    // 账号密码
    public static final String ACCOUNT_NOT_FOUND = "账号不存在";
    public static final String USER_NOT_EXIST = "用户不存在";
    public static final String PASSWORD_ERROR = "密码错误";
    public static final String ACCOUNT_PASSWORD_ERROR = "账号或密码错误";
    public static final String OLD_PASSWORD_ERROR = "旧密码错误";
    public static final String PASSWORD_ENCRYPT_ERROR = "密码加密失败";
    public static final String PASSWORD_EDIT_FAILED = "密码修改失败";

    // 注册与绑定
    public static final String REGISTER_FAILED = "注册失败，请稍后重试";
    public static final String BIND_DORM_FAILED = "绑定宿舍失败，请稍后重试";

    // 参数校验
    public static final String USER_ACCOUNT_NOT_EMPTY = "用户账号不能为空";
    public static final String ROLE_CODE_NOT_EMPTY = "角色编码不能为空";
    public static final String MESSAGE_TITLE_NOT_EMPTY = "消息标题不能为空";
    public static final String TARGET_ROLE_NOT_EMPTY = "目标角色不能为空";
    public static final String MESSAGE_ID_ILLEGAL = "消息ID不合法";

    // 消息相关
    public static final String MESSAGE_SEND_FAILED = "消息发送失败，请稍后重试";
    public static final String MESSAGE_READ_FAILED = "标记消息已读失败";
    public static final String SELECT_DELETE_MESSAGE = "请选择需要删除的消息";
    public static final String SELECT_MARK_MESSAGE = "请选择需要标记的消息";

    // 报修单相关
    public static final String ORDER_NOT_EXIST = "报修单不存在";
    public static final String ORDER_SUBMIT_FAILED = "报修订单提交失败";
    public static final String ORDER_UPDATE_FAILED = "更新报修单失败";
    public static final String ORDER_STATUS_UPDATE_FAILED = "更新报修单状态失败";
    public static final String ORDER_CANCEL_FAILED = "取消报修单失败";

    // 上传与OSS
    public static final String UPLOAD_FAILED = "文件上传失败";
    public static final String IMAGE_UPLOAD_FAILED = "图片上传失败";
    public static final String OSS_SERVICE_ERROR = "OSS服务端异常";

    // 日志
    public static final String SELECT_DELETE_LOG = "请选择需要删除的日志";
}