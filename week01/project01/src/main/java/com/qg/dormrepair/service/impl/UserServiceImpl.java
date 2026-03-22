package com.qg.dormrepair.service.impl;

import com.qg.dormrepair.dto.DormDTO;
import com.qg.dormrepair.dto.LoginDTO;
import com.qg.dormrepair.dto.RegisterDTO;
import com.qg.dormrepair.enums.Role;
import com.qg.dormrepair.exception.BusinessException;
import com.qg.dormrepair.mapper.UserDao;
import com.qg.dormrepair.pojo.User;
import com.qg.dormrepair.service.UserService;
import com.qg.dormrepair.util.CurrentHolder;
import com.qg.dormrepair.util.JwtUtils;
import com.qg.dormrepair.util.PasswordUtil;
import com.qg.dormrepair.vo.LoginResponse;
import com.qg.dormrepair.vo.UserVO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;
/**
 * 用户业务层实现类
 * <p>
 * 实现{@link UserService}接口定义的所有用户核心业务逻辑，
 * 依赖{@link UserDao}完成数据库操作，通过{@link PasswordUtil}处理密码加密/验证，
 * 通过{@link JwtUtils}生成登录令牌，所有数据库操作均添加事务控制
 * </p>
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
     * 用户登录的具体实现逻辑
     * <p>
     * 核心步骤：
     * 1. 提取登录DTO中的账号和密码；
     * 2. 根据账号查询用户信息，不存在则抛业务异常；
     * 3. 使用BCrypt算法验证密码（明文密码与数据库加密密码比对）；
     * 4. 密码验证通过后，组装JWT令牌（包含账号、角色信息）；
     * 5. 转换用户信息为VO对象，封装登录响应结果返回
     * </p>
     * @param loginDTO 登录请求参数
     * @return 包含JWT令牌和用户信息的登录响应VO
     * @throws BusinessException 账号不存在/密码错误时抛出
     */
    @Override
    public LoginResponse login(LoginDTO loginDTO) {
        String account = loginDTO.getAccount();
        String pwd = loginDTO.getPwd();

        log.info("用户登录尝试,账号{}", account);

        //校验
        // 1. 根据账号查询用户
        User user = userDao.findByAccount(account);
        if (user == null) {
            log.warn("登录失败，账号不存在：{}", account);
            throw new BusinessException("账号或密码错误");
        }

        // 2. 使用 BCrypt 的 matches() 方法验证密码
        if (!PasswordUtil.matches(pwd, user.getPwd())) {
            log.warn("登录失败，密码错误：{}", account);
            throw new BusinessException("账号或密码错误");
        }

        // 3. 组装JWT令牌（Claims包含账号、角色、宿舍楼栋、房间号，用于后续接口权限校验）
        Map<String, Object> claims = new HashMap<>();
        claims.put("account", user.getAccount());
        claims.put("role", user.getRole());
        claims.put("dormBuilding", user.getDormBuilding());
        claims.put("dormRoom", user.getDormRoom());
        String token = JwtUtils.generateJwt(claims);

        log.info("登录成功，账号：{}", account);
        // 4. 转换User为VO（隐藏密码等敏感信息）并返回
        return new LoginResponse(convertToVO(user), token);
    }
    /**
     * 用户注册的具体实现逻辑
     * <p>
     * 核心步骤：
     * 1. 提取注册DTO中的账号、密码、角色；
     * 2. 校验账号唯一性（已存在则抛异常）；
     * 3. 加密密码并组装User对象；
     * 4. 插入数据库，受事务控制，插入失败则回滚并抛异常
     * </p>
     * @param registerDTO 注册请求参数
     * @throws BusinessException 账号已存在/格式错误/插入失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void register(RegisterDTO registerDTO) {
        String account = registerDTO.getAccount();
        String pwd = registerDTO.getPwd();
        String role = registerDTO.getRole();

        log.info("用户注册尝试,账号:{}", account);
        // 1. 校验账号唯一性
        User existingUser = userDao.findByAccount(account);
        if (existingUser != null) {
            log.warn("注册失败,账号已存在:{}", account);
            throw new BusinessException("账号已存在");
        }
        // 2. 组装User对象，密码加密存储（BCrypt算法）
        User user = new User();
        user.setAccount(account);
        user.setPwd(PasswordUtil.encrypt(pwd));
        user.setRole(role.charAt(0));
        // 4. 插入数据库，受事务控制
        int result = userDao.insert(user);
        if (result <= 0) {
            log.error("注册失败,账号:{}", account);
            throw new BusinessException("注册失败");
        }
        log.info("注册成功,账号:{}", account);
    }
    /**
     * 修改密码的具体实现逻辑
     * <p>
     * 核心步骤：
     * 1. 从ThreadLocal获取当前登录用户账号；
     * 2. 验证旧密码正确性（加密后与数据库比对）；
     * 3. 加密新密码并更新用户信息；
     * 4. 受事务控制，更新失败则回滚并抛异常
     * </p>
     * @param oldPwd 原密码（明文）
     * @param newPwd 新密码（明文）
     * @throws BusinessException 旧密码错误/更新失败时抛出
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void updatePassword(String oldPwd, String newPwd) {
        // 1. 从CurrentHolder获取当前登录用户账号（ThreadLocal存储）
        String account = CurrentHolder.getCurrentUser().getAccount();
        log.info("用户修改密码尝试,账号:{}", account);

        // 2. 验证旧密码正确性（加密后比对）
        User user = userDao.login(account, PasswordUtil.encrypt(oldPwd));
        if (user == null) {
            log.warn("修改密码失败,账号或密码错误:{}", account);
            throw new BusinessException("账号或密码错误");
        }
        // 3. 加密新密码并更新
        user.setPwd(PasswordUtil.encrypt(newPwd));
        int result = userDao.update(user);
        if (result <= 0) {
            log.error("修改密码失败,账号:{}", account);
            throw new BusinessException("修改密码失败");
        }
        log.info("修改密码成功,账号:{}", account);
    }
    /**
     * 绑定宿舍信息的具体实现逻辑
     * <p>
     * 核心步骤：
     * 1. 从ThreadLocal获取当前登录用户的账号、楼栋、房间号；
     * 2. 校验用户是否存在；
     * 3. 更新用户的宿舍信息；
     * 4. 更新失败则抛业务异常
     * </p>
     * <b>注意</b>：当前逻辑依赖CurrentHolder存储楼栋/房间号，建议优化为方法参数传入
     * @throws BusinessException 用户不存在/更新失败时抛出
     */
    @Override
    public void bindDorm(DormDTO dormDTO) {
        // 1. 从CurrentHolder获取当前用户的账号和待绑定的宿舍信息
        String account = CurrentHolder.getCurrentUser().getAccount();
        log.info("绑定宿舍请求，账号:{}", account);

        // 2. 校验用户存在性
        User user = userDao.findByAccount(account);
        if (user == null) {
            log.warn("该用户不存在");
            throw new BusinessException("该用户不存在");
        }
        String dormBuilding = dormDTO.getDormBuilding();
        String dormRoom = dormDTO.getDormRoom();
        // 3. 更新宿舍信息
        user.setDormBuilding(dormBuilding);
        user.setDormRoom(dormRoom);
        int result = userDao.update(user);
        if (result <= 0) {
            log.error("绑定宿舍失败,账号:{}", account);
            throw new BusinessException("绑定宿舍失败");
        }
        log.info("绑定宿舍成功,账号:{}", account);
    }
    /**
     * 根据账号查询用户信息的实现逻辑
     * <p>
     * 简单转发调用DAO层方法，无额外业务逻辑，直接返回查询结果
     * </p>
     * @param account 用户账号
     * @return 用户完整信息，不存在则返回null
     */
    @Override
    public User findByAccount(String account) {
        return userDao.findByAccount(account);
    }
    /**
     * 私有工具方法：将User实体转换为UserVO（视图对象）
     * <p>
     * 隐藏密码等敏感字段，补充角色名称（如1→学生，2→管理员），适配前端展示
     * </p>
     * @param user 原始用户实体
     * @return 脱敏后的用户视图对象
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        vo.setId(user.getId());
        vo.setAccount(user.getAccount());
        vo.setRole(user.getRole());
        vo.setRoleName(Role.getRole(user.getRole()));
        vo.setDormBuilding(user.getDormBuilding());
        vo.setDormRoom(user.getDormRoom());
        return vo;
    }

}
