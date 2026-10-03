package com.example.system.service;

import com.example.system.entity.User;
import java.util.List;

public interface UserService {

    User login(String username, String password);

    User getUserById(Long id);

    User getUserByUsername(String username);

    List<User> getAllUsers();

    List<User> getUsersByPage(int pageNum, int pageSize);

    int getTotalCount();

//    List<User> getUsersByCondition(User user);

//    int addUser(User user);

    int updateUser(User user);
    /**
     * 管理员更新用户信息
     */
    int updateUserByAdmin(User user);

    int deleteUser(Long id);

    int changePassword(Long id, String oldPassword, String newPassword);

    /**
     * 更新用户状态

     */
    int updateStatus(Long id, Integer status);


    /**
     * 用户注册（带角色申请）
     */
    int registerWithRole(User user, Integer appliedRole);

    /**
     * 获取待审批列表
     */
    List<User> getPendingApprovals();

    /**
     * 已审批历史
     */
    List<User> getApprovedHistory();

    /**
     * 审批用户申请
     */
    int approveUserApply(Long userId, Integer status, Long approverId, String remark);
    User getUserByPhone(String phone);
}