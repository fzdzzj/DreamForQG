package com.qg.dormrepair.service;

import com.qg.dormrepair.dto.DormDTO;
import com.qg.dormrepair.dto.LoginDTO;
import com.qg.dormrepair.dto.RegisterDTO;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.pojo.User;
import com.qg.dormrepair.vo.LoginResponse;
/**
 * 用户业务层核心接口
 * <p>
 * 定义用户模块的核心业务逻辑规范，包括登录、注册、密码修改、宿舍绑定、账号查询等功能，
 * 所有方法均围绕用户身份验证、信息管理场景设计，异常统一抛出{@link BusinessException}
 * </p>
 */
public interface UserService {
    /**
     * 用户登录验证
     * <p>
     * 验证账号和密码的合法性，登录成功后生成JWT令牌并封装为登录响应VO返回，
     * 自动识别用户角色（学生/管理员）并关联到响应结果中
     * </p>
     * @param loginDTO 登录请求参数，包含账号（学生/管理员ID）和密码
     * @return 登录响应VO，包含JWT令牌、用户账号、角色等信息
     * @throws BusinessException 账号不存在/密码错误时抛出对应业务异常
     */
     LoginResponse login(LoginDTO loginDTO);


    void register(RegisterDTO registerDTO);
    /**
     * 修改用户登录密码
     * <p>
     * 验证旧密码的正确性，验证通过后加密新密码并更新到数据库，
     * 仅允许修改当前登录用户的密码，不支持跨账号修改
     * </p>
     * @param oldPwd 原登录密码（明文，后端会加密后与数据库存储的密码比对）
     * @param newPwd 新登录密码（需符合密码正则规则，后端加密后存储）
     * @throws BusinessException 旧密码错误/新密码格式错误抛出对应业务异常
     */
    void updatePassword(String oldPwd,String newPwd);
    /**
     * 绑定当前登录学生的宿舍信息
     * @throws BusinessException 非学生角色/楼栋/房间号为空时抛出对应业务异常
     */
    void bindDorm(DormDTO dormDTO);
    /**
     * 根据账号查询用户信息
     * <p>
     * 查询用户的完整信息（账号、角色、宿舍信息、密码加密串等），
     * 主要用于登录验证、权限判断、宿舍信息校验等场景
     * </p>
     * @param account 用户账号（学生学号/管理员ID）
     * @return 用户完整信息对象，账号不存在时返回null
     */
    User findByAccount(String account);

}
