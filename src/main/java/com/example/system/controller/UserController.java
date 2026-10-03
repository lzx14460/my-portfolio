package com.example.system.controller;

import com.example.system.common.PageResult;
import com.example.system.common.Result;
import com.example.system.constant.ApplyStatus;
import com.example.system.constant.UserRole;
import com.example.system.dto.ApproveApplyDTO;
import com.example.system.dto.LoginDTO;
import com.example.system.dto.RegisterDTO;
import com.example.system.dto.UserUpdateDTO;
import com.example.system.entity.User;
import com.example.system.service.UserService;
import com.example.system.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户登录
     */
    @PostMapping("/login")
    public Result<UserVO> login(@Valid @RequestBody LoginDTO loginDTO, HttpSession session) {
        // 先检查用户是否存在
        User existUser = userService.getUserByUsername(loginDTO.getUsername());

        // 用户不存在
        if (existUser == null) {
            return Result.error("用户名或密码错误");
        }

        // 检查账号是否被禁用
        if (existUser.getStatus() != null && existUser.getStatus() == 0) {
            return Result.error("您的账号已被禁用，请联系管理员");
        }

        // 正常登录验证
        User user = userService.login(loginDTO.getUsername(), loginDTO.getPassword());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }

        session.setAttribute("userId", user.getId());
        session.setAttribute("userRole", user.getRole());
        session.setAttribute("userName", user.getName());

        UserVO userVO = convertToVO(user);
        return Result.success(userVO);
    }

    /**
     * 用户登出
     */
    @PostMapping("/logout")
    public Result<Void> logout(HttpSession session) {
        session.invalidate();
        return Result.success();
    }

//    /**
//     * 用户注册
//     */
//    @PostMapping("/register")
//    public Result<Void> register(@RequestBody User user) {
//
//        log.info("注册收到原始密码: {}", user.getPassword());
//
//        String encrypted = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
//        log.info("MD5后: {}", encrypted);
//
//        user.setPassword(encrypted);
//        userService.addUser(user);
//        return Result.success();
//    }
    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/current")
    public Result<UserVO> getCurrentUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return Result.error("未登录");
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            return Result.error("用户不存在");
        }

        return Result.success(convertToVO(user));
    }
    /**
     * 获取用户总数
     */
    @GetMapping("/count")
    public Result<Integer> getUserCount(HttpSession session) {
        // 可选：添加权限控制，只有管理员可以查看
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == 0) {
            return Result.error("无权限查看");
        }

        int count = userService.getTotalCount();
        return Result.success(count);
    }


    /**
     * 获取用户列表
     */
    @GetMapping("/list")
    public Result<List<UserVO>> listUsers() {
        List<User> users = userService.getAllUsers();
        List<UserVO> voList = users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 分页获取用户列表
     */
    @GetMapping("/page")
    public Result<PageResult<UserVO>> getUsersByPage(
            @RequestParam(defaultValue = "1") int pageNum,
            @RequestParam(defaultValue = "10") int pageSize) {

        List<User> users = userService.getUsersByPage(pageNum, pageSize);
        int total = userService.getTotalCount();

        List<UserVO> voList = users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());

        PageResult<UserVO> pageResult = new PageResult<>(pageNum, pageSize, total, voList);
        return Result.success(pageResult);
    }

    /**
     * 根据ID获取用户
     */
    @GetMapping("/{id}")
    public Result<UserVO> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        return Result.success(convertToVO(user));
    }

    // 在 UserController.java 中添加

    /**
     * 用户注册（支持角色申请）
     */
    @PostMapping("/register")
    public Result<String> register(@RequestBody RegisterDTO dto) {
        try {
            User user = new User();
            user.setUsername(dto.getUsername());
            user.setPassword(dto.getPassword());
            user.setName(dto.getName());
            user.setPhone(dto.getPhone());
            user.setEmail(dto.getEmail());           // 邮箱
            user.setStudentId(dto.getStudentId());   // 学号
            user.setCollege(dto.getCollege());       // 学院
            user.setClassName(dto.getClassName());   // 班级

            // 调用带角色的注册方法
            userService.registerWithRole(user, dto.getRole());

            String roleName = dto.getRole() == 0 ? "普通用户" :
                    (dto.getRole() == 1 ? "场馆管理员" : "系统管理员");

            if (dto.getRole() > 0) {
                return Result.success("注册成功，已提交" + roleName + "申请，请等待管理员审批");
            } else {
                return Result.success("注册成功");
            }
        } catch (Exception e) {
            log.error("注册失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 获取待审批列表（仅系统管理员可访问）
     */
    @GetMapping("/pending-approvals")
    public Result<List<UserVO>> getPendingApprovals(HttpSession session) {
        // 权限校验：只有系统管理员可以查看
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole != UserRole.SYSTEM_ADMIN) {
            return Result.error("无权限访问");
        }

        List<User> users = userService.getPendingApprovals();




        List<UserVO> voList = users.stream()
                .map(this::convertToVO)
                .collect(Collectors.toList());
        return Result.success(voList);
    }

    /**
     * 获取已审批历史列表
     */
    @GetMapping("/approved-history")
    public Result<List<User>> getApprovedHistory() {
        List<User> users = userService.getApprovedHistory();

        return Result.success(users);
    }
    /**
     * 审批用户申请（仅系统管理员可访问）
     */
    @PostMapping("/approve-apply")
    public Result<String> approveApply(@RequestBody ApproveApplyDTO dto, HttpSession session) {
        // 权限校验：只有系统管理员可以审批
        Integer userRole = (Integer) session.getAttribute("userRole");
        Long approverId = (Long) session.getAttribute("userId");

        if (userRole == null || userRole != UserRole.SYSTEM_ADMIN) {
            return Result.error("无权限操作");
        }

        try {
            userService.approveUserApply(dto.getUserId(), dto.getStatus(), approverId, dto.getRemark());

            String resultMsg = dto.getStatus() == ApplyStatus.APPROVED ? "已通过" : "已拒绝";
            return Result.success("审批" + resultMsg);
        } catch (Exception e) {
            log.error("审批失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 更新用户状态

     */
    @PutMapping("/status/{id}")
    public Result<Void> updateUserStatus(@PathVariable Long id, @RequestParam Integer status) {
        userService.updateStatus(id, status);
        return Result.success();
    }

    /**
     * 更新用户
     */
    @PutMapping("/update")
    public Result<Void> updateUser(@RequestBody User user) {
        // 检查用户名是否已被其他用户使用
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            User existUser = userService.getUserByUsername(user.getUsername());
            if (existUser != null && !existUser.getId().equals(user.getId())) {
                return Result.error("用户名已被使用");
            }
        }

        // 检查手机号是否已被其他用户使用
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            User existPhone = userService.getUserByPhone(user.getPhone());
            if (existPhone != null && !existPhone.getId().equals(user.getId())) {
                return Result.error("手机号已被其他用户绑定");
            }
        }

        int result = userService.updateUser(user);
        if (result > 0) {
            return Result.success();
        }
        return Result.error("更新失败");
    }
    /**
     * 检查手机号是否已被注册
     */
    @GetMapping("/check-phone")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        User user = userService.getUserByPhone(phone);
        return Result.success(user != null);
    }
    /**
     * 检查用户名是否已被使用
     */
    @GetMapping("/check-username")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        User user = userService.getUserByUsername(username);
        return Result.success(user != null);
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        int result = userService.deleteUser(id);
        if (result > 0) {
            return Result.success();
        }
        return Result.error("删除失败");
    }

    /**
     * 修改密码
     */
    @PutMapping("/change-password")
    public Result<Void> changePassword(
            @RequestParam Long id,
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            HttpSession session) {

        Long currentUserId = (Long) session.getAttribute("userId");
        if (!currentUserId.equals(id)) {
            return Result.error("无权限修改他人密码");
        }

        try {
            userService.changePassword(id, oldPassword, newPassword);
            return Result.success();
        } catch (Exception e) {
            return Result.error(e.getMessage());
        }
    }
    /**
     * 管理员更新用户信息（系统管理员专用）
     * 可修改：姓名、学号、学院、班级、手机号、邮箱
     */
    @PutMapping("/admin/update")
    public Result<Void> adminUpdateUser(@Valid @RequestBody UserUpdateDTO updateDTO, HttpSession session) {
        // 权限检查：仅系统管理员
        Integer userRole = (Integer) session.getAttribute("userRole");
        if (userRole == null || userRole != UserRole.SYSTEM_ADMIN) {
            return Result.error("权限不足，仅系统管理员可操作");
        }

        // 检查用户是否存在
        User existUser = userService.getUserById(updateDTO.getId());
        if (existUser == null) {
            return Result.error("用户不存在");
        }





        try {
            User user = new User();
            user.setId(updateDTO.getId());
            user.setName(updateDTO.getName());
            user.setStudentId(updateDTO.getStudentId());
            user.setCollege(updateDTO.getCollege());
            user.setClassName(updateDTO.getClassName());


            int result = userService.updateUserByAdmin(user);
            if (result > 0) {
                log.info("管理员更新用户成功: id={}, name={}", updateDTO.getId(), updateDTO.getName());
                return Result.success();
            }
            return Result.error("更新失败");
        } catch (Exception e) {
            log.error("更新用户失败", e);
            return Result.error(e.getMessage());
        }
    }

    /**
     * 转换方法：User -> UserVO
     */
    private UserVO convertToVO(User user) {
        if (user == null) return null;

        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);

        //  确保 role 字段被赋值
        vo.setRole(user.getRole());

        // 性别转换
        if (user.getGender() != null) {
            vo.setGenderText(user.getGender() == 1 ? "男" : "女");
        }

        // 角色转换
        if (user.getRole() != null) {
            switch (user.getRole()) {
                case 0:
                    vo.setRoleText("学生");
                    break;
                case 1:
                    vo.setRoleText("场馆管理员");
                    break;
                case 2:
                    vo.setRoleText("系统管理员");
                    break;
                default:
                    vo.setRoleText("未知");
            }
        }

        // 状态转换
        if (user.getStatus() != null) {
            vo.setStatusText(user.getStatus() == 1 ? "启用" : "禁用");
        }

        return vo;
    }
}