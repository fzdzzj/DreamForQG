package com.qg.dormrepair.vo;

import lombok.Data;
/**
 * 用户VO
 */
@Data
public class UserVO {
    //ID
    private Long id;
    //用户学号/工号
    private String account;
    //用户角色
    private Character role;
    //楼栋
    private String dormBuilding;
    private String dormRoom;
    //不要包含pwd，防止敏感信息泄露
}
