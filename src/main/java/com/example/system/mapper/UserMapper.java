package com.example.system.mapper;

import com.example.system.entity.User;
import org.apache.ibatis.annotations.MapKey;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;
import java.util.Map;

@Mapper
public interface UserMapper {

    /**
     * 根据ID查询
     */
    User selectById(Long id);

    /**
     * 根据用户名查询
     */
    User selectByUsername(String username);

    /**
     * 查询所有用户
     */
    List<User> selectAll();

    /**
     * 分页查询
     */
    List<User> selectByPage(@Param("start") int start, @Param("size") int size);

    /**
     * 查询总数
     */
    int selectCount();

//    /**
//     * 条件查询
//     */
//    List<User> selectByCondition(User user);

    /**
     * 插入用户
     */
    int insert(User user);

    /**
     * 更新用户
     */
    int update(User user);
    /**
     * 管理员更新用户信息（可更新学院、班级等）
     */
    int updateByAdmin(User user);
    /**
     * 删除用户
     */
    int deleteById(Long id);
//    /**
//     * 统计用户活跃度 - 返回多个Map，需要用@MapKey指定key字段
//     */
//    @MapKey("userId")
//    List<Map<String, Object>> selectUserActivityStatistics(@Param("startDate") String startDate,
//                                                           @Param("endDate") String endDate);


    /**
     * 查询待审批的用户列表
     */
    List<User> selectPendingApprovals();

    /**
     * 审批历史
     * */
    List<User> selectApprovedHistory();

    /**
     * 更新用户的申请状态
     */
    int updateApplyStatus(@Param("id") Long id,
                          @Param("status") Integer status,
                          @Param("approveRemark") String approveRemark);

    /**
     * 根据手机号查询用户
     */
    User selectByPhone(String phone);
}