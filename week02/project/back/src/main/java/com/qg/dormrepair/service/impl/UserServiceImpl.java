package com.qg.dormrepair.service.impl;

import com.qg.dormrepair.config.JwtProperties;
import com.qg.dormrepair.constants.MessageConstant;
import com.qg.dormrepair.dto.BindDormDTO;
import com.qg.dormrepair.dto.LoginDTO;
import com.qg.dormrepair.dto.RegisterDTO;
import com.qg.dormrepair.enums.Role;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.mapper.UserDao;
import com.qg.dormrepair.pojo.User;
import com.qg.dormrepair.service.PermissionService;
import com.qg.dormrepair.service.UserService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.util.JwtUtils;
import com.qg.dormrepair.util.PasswordUtil;
import com.qg.dormrepair.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 用户业务层实现类
 * <p>
 * 实现{@link UserService}接口定义的所有用户核心业务逻辑，
 * 包含：用户登录、注册、密码修改、宿舍绑定、Token生成等功能
 * 所有数据库写操作均添加事务控制，保证数据一致性
 * </p>
 *
 * @author qg
 * @date 2026-03-28
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    /**
     * 用户数据访问层对象（构造器注入，不可变）
     */
    private final UserDao userDao;

    /**
     * 权限业务层对象（构造器注入，不可变）
     */
    private final PermissionService permissionService;

    /**
     * JWT 工具类
     */
    private final JwtUtils jwtUtils;

    /**
     * JWT 配置属性
     */
    private final JwtProperties jwtProperties;

    /**
     * 用户登录
     * <p>
     * 核心步骤：
     * 1. 账号、密码校验
     * 2. 查询用户信息，校验用户是否存在
     * 3. 密码比对验证
     * 4. 查询用户权限，生成 AccessToken + RefreshToken
     * 5. 封装用户信息、令牌信息返回
     * 6. 学生用户额外返回宿舍绑定状态
     * </p>
     *
     * @param loginDTO 登录请求参数
     * @return 登录结果（包含双Token、用户信息、过期时间）
     * @throws BusinessException 账号不存在/密码错误时抛出
     */
    @Override
    public Map<String, Object> login(LoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        String pwd = loginDTO.getPwd();

        log.info("用户发起登录请求，账号：{}", account);

        // 1. 根据账号查询用户
        User user = userDao.findByAccount(account);
        if (user == null) {
            log.warn("登录失败：账号不存在，账号：{}", account);
            throw new BusinessException(401,MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 2. 密码校验
        if (!PasswordUtil.matches(pwd, user.getPwd())) {
            log.warn("登录失败：密码错误，账号：{}", account);
            throw new BusinessException(401,MessageConstant.PASSWORD_ERROR);
        }
        log.info("用户密码校验通过，账号：{}", account);

        // 3. 查询权限并生成 Token
        Set<String> permissions = permissionService.getPermissionsByRole(Role.getRole(user.getRole())).stream()
                .map(String::trim)
                .collect(Collectors.toSet());
        log.info("用户权限查询完成，账号：{}，权限数量：{}", account, permissions.size());

        // 生成双 Token
        String accessToken = jwtUtils.generateAccessToken(user.getAccount(), user.getRole(), permissions);
        String refreshToken = jwtUtils.generateRefreshToken(user.getAccount(), user.getRole());

        // 4. 封装返回数据
        Map<String, Object> data = new HashMap<>();
        data.put("accessToken", accessToken);
        data.put("refreshToken", refreshToken);
        data.put("accessTokenExprie", jwtProperties.getExpire());
        data.put("refreshTokenExprie", jwtProperties.getRefreshExpire());

        // 封装用户基础信息
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("id", user.getId());
        userInfo.put("account", user.getAccount());
        userInfo.put("role", user.getRole());

        // 存入线程上下文
        CurrentHolder.setCurrentUser(user.getAccount(), user.getRole());
        log.info("用户登录成功，账号：{}", account);

        // 学生：额外封装宿舍信息
        if (user.getRole().equals(Role.STUDENT.getCode())) {
            userInfo.put("dormBuilding", user.getDormBuilding());
            userInfo.put("dormRoom", user.getDormRoom());

            boolean dormBound = user.getDormBuilding() != null && !user.getDormBuilding().isEmpty()
                    && user.getDormRoom() != null && !user.getDormRoom().isEmpty();
            userInfo.put("dormBound", dormBound);
            data.put("needBindDorm", !dormBound);
        }

        data.put("user", userInfo);
        return data;
    }

    /**
     * 用户注册
     * <p>
     * 核心步骤：
     * 1. 校验账号是否已存在
     * 2. 密码加密
     * 3. 插入用户数据
     * 4. 事务回滚保障
     * </p>
     *
     * @param registerDTO 注册请求参数
     * @throws BusinessException 账号已存在/注册失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        String account = registerDTO.getAccount();
        String pwd = registerDTO.getPwd();
        String role = registerDTO.getRole();

        log.info("用户发起注册请求，账号：{}，角色：{}", account, role);

        // 1. 校验账号唯一性
        User existingUser = userDao.findByAccount(account);
        if (existingUser != null) {
            log.warn("注册失败：账号已存在，账号：{}", account);
            throw new BusinessException(409,MessageConstant.ALREADY_EXISTS);
        }

        // 2. 密码加密并组装用户对象
        User user = new User();
        user.setAccount(account);
        user.setPwd(PasswordUtil.encrypt(pwd));
        user.setRole(role);

        // 3. 插入数据库
        int result = userDao.insert(user);
        if (result <= 0) {
            log.error("注册失败：数据库插入失败，账号：{}", account);
            throw new BusinessException(500,MessageConstant.REGISTER_FAILED);
        }

        log.info("用户注册成功，账号：{}", account);
    }

    /**
     * 修改登录用户密码
     * <p>
     * 1. 校验旧密码是否正确
     * 2. 新密码加密
     * 3. 更新数据库
     * </p>
     *
     * @param oldPwd 旧密码明文
     * @param newPwd 新密码明文
     * @throws BusinessException 旧密码错误/更新失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String oldPwd, String newPwd) {
        String account = CurrentHolder.getCurrentUser().getAccount();
        log.info("用户发起修改密码请求，账号：{}", account);

        // 校验旧密码
        User user = userDao.findByAccount(account);
        if (user == null || !PasswordUtil.matches(oldPwd, user.getPwd())) {
            log.warn("修改密码失败："+MessageConstant.OLD_PASSWORD_ERROR+"，账号：{}", account);
            throw new BusinessException(401,MessageConstant.OLD_PASSWORD_ERROR);
        }

        // 更新新密码
        user.setPwd(PasswordUtil.encrypt(newPwd));
        int result = userDao.update(user);

        if (result <= 0) {
            log.error(MessageConstant.PASSWORD_EDIT_FAILED+"：数据库更新失败，账号：{}", account);
            throw new BusinessException(500,MessageConstant.PASSWORD_EDIT_FAILED);
        }

        log.info("用户密码修改成功，账号：{}", account);
    }

    /**
     * 学生绑定宿舍信息
     * 仅允许学生角色绑定
     *
     * @param account     用户账号
     * @param bindDormDTO 宿舍信息
     * @throws BusinessException 用户不存在/非学生/绑定失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void bindDorm(String account, BindDormDTO bindDormDTO) {
        log.info("用户绑定宿舍，账号：{}，楼栋：{}，房间：{}",
                account, bindDormDTO.getDormBuilding(), bindDormDTO.getDormRoom());

        // 校验用户是否存在
        User user = userDao.findByAccount(account);
        if (user == null) {
            log.error("绑定宿舍失败："+MessageConstant.USER_NOT_EXIST+"，账号：{}", account);
            throw new BusinessException(400,MessageConstant.USER_NOT_EXIST);
        }

        // 仅学生可绑定宿舍
        if (!Role.STUDENT.getCode().equals(user.getRole())) {
            log.warn("绑定宿舍失败：非学生角色无法绑定，账号：{}", account);
            throw new BusinessException(403,"只有学生可以绑定宿舍");
        }

        // 更新宿舍信息
        user.setDormBuilding(bindDormDTO.getDormBuilding());
        user.setDormRoom(bindDormDTO.getDormRoom());

        int result = userDao.update(user);
        if (result <= 0) {
            log.error(MessageConstant.BIND_DORM_FAILED+"：数据库更新失败，账号：{}", account);
            throw new BusinessException(500,MessageConstant.BIND_DORM_FAILED);
        }

        log.info("宿舍绑定成功，账号：{}", account);
    }

    /**
     * 判断学生是否已绑定宿舍
     *
     * @param account 用户账号
     * @return true=已绑定，false=未绑定/用户不存在
     */
    @Override
    public boolean isDormBound(String account) {
        User user = userDao.findByAccount(account);
        if (user == null) {
            log.warn("查询宿舍绑定状态失败：用户不存在，账号：{}", account);
            return false;
        }

        boolean isBound = user.getDormBuilding() != null && !user.getDormBuilding().trim().isEmpty()
                && user.getDormRoom() != null && !user.getDormRoom().trim().isEmpty();

        log.info("查询宿舍绑定状态，账号：{}，是否绑定：{}", account, isBound);
        return isBound;
    }

    /**
     * 根据用户账号查询用户信息
     *
     * @param account 用户账号
     * @return 用户实体对象
     */
    @Override
    public User findByAccount(String account) {
        log.debug("根据账号查询用户信息，账号：{}", account);
        return userDao.findByAccount(account);
    }

    /**
     * 获取用户宿舍信息（楼栋、房间、是否绑定）
     *
     * @param account 用户账号
     * @return 宿舍信息Map
     */
    @Override
    public Map<String, String> getDormInfo(String account) {
        log.debug("获取用户宿舍信息，账号：{}", account);

        User user = userDao.findByAccount(account);
        if (user == null) {
            log.warn("获取宿舍信息失败：用户不存在，账号：{}", account);
            return null;
        }

        Map<String, String> info = new HashMap<>();
        info.put("dormBuilding", user.getDormBuilding());
        info.put("dormRoom", user.getDormRoom());
        info.put("isBound", (user.getDormBuilding() != null && user.getDormRoom() != null) ? "true" : "false");

        log.debug("获取宿舍信息成功，账号：{}", account);
        return info;
    }

    /**
     * User 实体转换为 VO 对象（脱敏返回）
     *
     * @param user 原始用户实体
     * @return 前端展示 VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setAccount(user.getAccount());
        vo.setRole(user.getRole());
        vo.setDormBuilding(user.getDormBuilding());
        vo.setDormRoom(user.getDormRoom());
        return vo;
    }
}