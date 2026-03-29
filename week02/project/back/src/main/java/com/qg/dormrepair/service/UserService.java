package com.qg.dormrepair.service;

import com.qg.dormrepair.dto.BindDormDTO;
import com.qg.dormrepair.dto.LoginDTO;
import com.qg.dormrepair.dto.RegisterDTO;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.pojo.User;

import java.util.Map;

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
     * 验证账号和密码的合法性，登录成功后生成JWT令牌并封装返回，
     * 自动识别用户角色（学生/管理员）并返回对应权限信息
     * </p>
     *
     * @param loginDTO 登录请求参数，包含账号和密码
     * @return 登录结果，包含双Token、用户信息、过期时间等
     * @throws BusinessException 账号不存在/密码错误时抛出
     */
    Map<String, Object> login(LoginDTO loginDTO);

    /**
     * 用户注册
     * <p>
     * 校验账号唯一性，密码加密后保存用户信息，支持学生/管理员注册
     * </p>
     *
     * @param registerDTO 注册参数（账号、密码、角色）
     * @throws BusinessException 账号已存在/注册失败时抛出
     */
    void register(RegisterDTO registerDTO);

    /**
     * 修改用户登录密码
     * <p>
     * 验证旧密码正确性，验证通过后加密新密码并更新到数据库，
     * 仅允许修改当前登录用户的密码
     * </p>
     *
     * @param oldPwd 旧密码（明文）
     * @param newPwd 新密码（明文）
     * @throws BusinessException 旧密码错误/修改失败时抛出
     */
    void updatePassword(String oldPwd, String newPwd);

    /**
     * 学生绑定宿舍信息
     * <p>
     * 仅学生角色可绑定，绑定后可正常提交报修订单
     * </p>
     *
     * @param account 用户账号
     * @param bindDormDTO 宿舍信息（楼栋、房间号）
     * @throws BusinessException 用户不存在/非学生/绑定失败时抛出
     */
    void bindDorm(String account, BindDormDTO bindDormDTO);

    /**
     * 判断用户是否已绑定宿舍
     *
     * @param account 用户账号
     * @return true=已绑定，false=未绑定/用户不存在
     */
    boolean isDormBound(String account);

    /**
     * 根据账号查询用户完整信息
     * <p>
     * 用于登录验证、权限判断、宿舍信息校验等内部场景
     * </p>
     *
     * @param account 用户账号
     * @return 用户实体对象，不存在则返回null
     */
    User findByAccount(String account);

    /**
     * 获取用户宿舍信息（楼栋、房间、是否绑定）
     *
     * @param account 用户账号
     * @return 宿舍信息Map
     */
    Map<String, String> getDormInfo(String account);
}