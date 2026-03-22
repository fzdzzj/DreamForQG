package com.qg.dormrepair.vo;

import lombok.Data;

@Data
public class UserVO {
    private Long id;
    private String account;
    private Character role;
    private String roleName;
    private String dormBuilding;
    private String dormRoom;
    //不要包含pwd，防止敏感信息泄露
}
