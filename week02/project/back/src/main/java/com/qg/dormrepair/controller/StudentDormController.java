// controller/StudentDormController.java
package com.qg.dormrepair.controller;

import com.qg.dormrepair.annotation.OperationLog;
import com.qg.dormrepair.dto.BindDormDTO;
import com.qg.dormrepair.pojo.Result;
import com.qg.dormrepair.pojo.User;
import com.qg.dormrepair.service.UserService;
import com.qg.dormrepair.util.CurrentHolder;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 宿舍信息功能控制器
 */
@Slf4j
@RestController
@RequestMapping("/api/student")
@RequiredArgsConstructor
@Tag(name = "学生宿舍管理接口")
@CrossOrigin(origins = "*")
public class StudentDormController {

    private final UserService userService;

    /**
     * 绑定宿舍
     */
    @PostMapping("/dorm")
    @OperationLog("绑定宿舍")
    @Operation(summary = "绑定宿舍")
    public Result<Void> bindDorm(@Valid @RequestBody BindDormDTO bindDormDTO) {
        String account = CurrentHolder.getCurrentUser().getAccount();
        log.info("用户绑定宿舍,账号{}", account);
        if (account == null) {
            log.warn("用户未登录");
            return Result.error(401, "未登录");
        }
        boolean isBound = userService.isDormBound(account);
        log.info("用户是否绑定了宿舍:{}", isBound);
        //检查是否已经绑定
        if (isBound) {
            log.warn("用户已绑定宿舍");
            return Result.error(400, "您已绑定宿舍，请使用修改功能");
        }
        userService.bindDorm(account, bindDormDTO);
        log.info("用户宿舍绑定成功");
        return Result.success();
    }

    @PutMapping("/dorm")
    @OperationLog("修改宿舍信息")
    @Operation(summary = "修改宿舍信息")
    public Result<Void> updateDorm(@Valid @RequestBody BindDormDTO bindDormDTO) {
        String account = CurrentHolder.getCurrentUser().getAccount();
        log.info("用户修改宿舍信息,账号{}", account);
        if (account == null) {
            log.warn("用户未登录");
            return Result.error(401, "未登录");
        }
        userService.bindDorm(account, bindDormDTO);  // 复用绑定方法
        log.info("用户宿舍修改成功");
        return Result.success();
    }

    /**
     * 检查宿舍绑定状态
     */
    @GetMapping("/dorm/status")
    @Operation(summary = "检查宿舍绑定状态")
    public Result<Map<String, Object>> getDormStatus() {
        String account = CurrentHolder.getCurrentUser().getAccount();
        log.info("用户检查宿舍绑定状态,账号{}", account);
        if (account == null) {
            log.warn("用户未登录");
            return Result.error(401, "未登录");
        }

        Map<String, Object> data = new HashMap<>();
        boolean isBound = userService.isDormBound(account);
        data.put("dormBound", isBound);
        if (isBound) {
            log.info("用户已绑定宿舍");
            // 已绑定，返回宿舍信息
            // 可以从 CurrentHolder 或重新查询获取
            data.put("message", "宿舍已绑定");
        } else {
            log.info("用户未绑定宿舍");
            data.put("message", "请先绑定宿舍");
        }
        return Result.success(data);
    }

    /**
     * 获取宿舍信息
     */
// controller/StudentDormController.java
    @GetMapping("/dorm/info")
    @Operation(summary = "获取宿舍信息")
    public Result<Map<String, String>> getDormInfo() {
        String account = CurrentHolder.getCurrentUser().getAccount();
        log.info("用户获取宿舍信息,账号{}", account);
        if (account == null) {
            log.warn("用户未登录");
            return Result.error(401, "未登录");
        }

        Map<String, String> data = userService.getDormInfo(account);
        if (data == null) {
            log.warn("用户不存在");
            return Result.error(404, "用户不存在");
        }
        log.info("用户宿舍信息获取成功");
        return Result.success(data);
    }

}
