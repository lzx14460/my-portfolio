package com.example.system.service.impl;

import com.example.system.constant.ApplyStatus;
import com.example.system.constant.UserRole;
import com.example.system.entity.User;
import com.example.system.mapper.UserMapper;
import com.example.system.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.mindrot.jbcrypt.BCrypt;

import java.util.Date;
import java.util.List;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserMapper userMapper;

    /**
     * 用户登录验证
     */
    @Override
    public User login(String username, String password) {
        User user = userMapper.selectByUsername(username);
        if (user == null) {
            return null;
        }

        // 检查账号是否被禁用
        if (user.getStatus() != null && user.getStatus() == 0) {
            return null;  // 返回 null
        }

        if (BCrypt.checkpw(password, user.getPassword())) {
            log.info("用户登录成功: {}", username);
            return user;
        }

        return null;
    }
    /**
     * 根据ID获取用户
     */
    @Override
    public User getUserById(Long id) {
        return userMapper.selectById(id);
    }

    /**
     * 根据用户名获取用户
     */
    @Override
    public User getUserByUsername(String username) {
        return userMapper.selectByUsername(username);
    }

    /**
     * 获取所有用户列表
     */
    @Override
    public List<User> getAllUsers() {
        return userMapper.selectAll();
    }

    /**
     * 分页查询用户列表
     */
    @Override
    public List<User> getUsersByPage(int pageNum, int pageSize) {
        int start = (pageNum - 1) * pageSize;
        return userMapper.selectByPage(start, pageSize);
    }

    /**
     * 获取用户总数
     */
    @Override
    public int getTotalCount() {
        return userMapper.selectCount();
    }

//    @Override
//    public List<User> getUsersByCondition(User user) {
//        return userMapper.selectByCondition(user);
//    }

//    @Override
//    @Transactional
//    public int addUser(User user) {
//        User existUser = userMapper.selectByUsername(user.getUsername());
//        if (existUser != null) {
//            throw new RuntimeException("用户名已存在");
//        }
//
//        String encryptedPassword = DigestUtils.md5DigestAsHex(user.getPassword().getBytes());
//        user.setPassword(encryptedPassword);
//
//        if (user.getStatus() == null) {
//            user.setStatus(1);
//        }
//        if (user.getRole() == null) {
//            user.setRole(0);
//        }
//
//        return userMapper.insert(user);
//    }

    /**
     * 更新用户信息
     */
    @Override
    public int updateUser(User user) {
        return userMapper.update(user);
    }
    /**
     * 管理员更新用户信息
     */
    @Override
    public int updateUserByAdmin(User user) {
        return userMapper.updateByAdmin(user);
    }
    /**
     * 删除用户
     */
    @Override
    @Transactional
    public int deleteUser(Long id) {
        return userMapper.deleteById(id);
    }

    /**
     * 修改用户密码
     */
    @Override
    @Transactional
    public int changePassword(Long id, String oldPassword, String newPassword) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }

        if (!BCrypt.checkpw(oldPassword, user.getPassword())) {
            throw new RuntimeException("原密码错误");
        }

        String encryptedNewPassword = BCrypt.hashpw(newPassword, BCrypt.gensalt());
        user.setPassword(encryptedNewPassword);

        return userMapper.update(user);
    }
    /**
     * 根据手机号查询用户
     */
    @Override
    public User getUserByPhone(String phone) {
        return userMapper.selectByPhone(phone);
    }

    /**
     * 更新用户启用/禁用状态
     */
    @Override
    public int updateStatus(Long id, Integer status) {
        User user = new User();
        user.setId(id);
        user.setStatus(status);
        return userMapper.update(user);
    }


    /**
     * 用户注册（带角色申请）
     */
    @Override
    @Transactional
    public int registerWithRole(User user, Integer appliedRole) {
        // 检查用户名是否存在
        User existUser = userMapper.selectByUsername(user.getUsername());
        if (existUser != null) {
            throw new RuntimeException("用户名已存在");
        }

        // 检查手机号是否存在
        if (user.getPhone() != null && !user.getPhone().isEmpty()) {
            User existPhone = userMapper.selectByPhone(user.getPhone());
            if (existPhone != null) {
                throw new RuntimeException("手机号已被注册");
            }
        }

        // 密码加密（BCrypt）
        String encryptedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt());
        user.setPassword(encryptedPassword);

        // 处理角色申请
        if (appliedRole != null && appliedRole > 0) {
            // 申请管理员角色，需要审批
            user.setRole(0);                          // 先设为普通学生
            user.setAppliedRole(appliedRole);         // 记录申请的角色
            user.setApplyStatus(1);                   // 待审批
            user.setApplyTime(new Date());
            System.out.println("=== 管理员申请 === role=" + appliedRole);
        } else {
            // 普通注册，直接是学生
            user.setRole(0);
            user.setAppliedRole(null);
            user.setApplyStatus(0);                   // 未申请
            user.setApplyTime(null);
            System.out.println("=== 普通注册 ===");
        }

        user.setStatus(1);

        System.out.println("保存前 - role:" + user.getRole() +
                ", appliedRole:" + user.getAppliedRole() +
                ", applyStatus:" + user.getApplyStatus());

        return userMapper.insert(user);
    }

    /**
     * 获取待审批的管理员申请列表
     */
    @Override
    public List<User> getPendingApprovals() {
        return userMapper.selectPendingApprovals();
    }

    /**
     * 审批用户角色申请
     */
    @Override
    @Transactional
    public int approveUserApply(Long userId, Integer status, Long approverId, String remark) {
        // 验证审批人权限（调用方已经验证，这里不再重复）
        return userMapper.updateApplyStatus(userId, status, remark);
    }
    /**
     * 获取已审批的历史记录
     */
    @Override
    public List<User> getApprovedHistory() {
        return userMapper.selectApprovedHistory();
    }
}