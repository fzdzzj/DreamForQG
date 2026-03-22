package com.qg.dormrepair.constants;

/**
 * 正则表达式常量类
 * 用于校验登录注册更新密码时的参数
 */
public class RegexConstants {




    // 私有构造函数
    private RegexConstants() {
    }

    /**
     * 学生学号正则：3125/3225开头 + 6位数字（总10位）
     * 示例：3125012345、3225987654
     */
    public static final String STUDENT_ID = "(3125|3225)\\d{6}";
    /**
     * 管理员ID正则：0025开头 + 6位数字（总10位）
     * 示例：0025000001、0025999999
     */
    public static final String ADMIN_ID = "0025\\d{6}";
    /**
     * 通用用户ID正则：包含学生/管理员ID所有情况
     */
    public static final String USER_ID = "(3125|3225|0025)\\d{6}";
    /**
     * 密码正则：6-10位字母/数字，无特殊字符
     */
    public static final String PASSWORD = "[a-zA-Z0-9]{6,10}";
    /**
     * 角色标识正则：1=学生，2=管理员
     */
    public static final String ROLE = "[12]";
    /**
     * 报修状态正则：1=待处理，2=已完成，3=已取消
     */
    public static final String ORDER_STATUS = "[1-3]";

    /**
     * 报修优先级正则：1=普通，2=紧急，3=非常紧急
     */
    public static final String PRIORITY = "[1-3]";
    /**
     * 设备类型正则：1=空调、2=水龙头、3=电灯、4=门锁、5=水槽、6=水表、7=电表、8=床、9=窗
     */
    public static final String DEVICE_TYPE ="[1-9]" ;
}
