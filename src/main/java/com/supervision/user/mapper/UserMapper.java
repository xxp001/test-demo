package com.supervision.user.mapper;

import com.supervision.user.model.User;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    List<User> findAll();

    List<User> selectNotLogicDelete();

    int countUser();

    User selectUserByWorkcode(@Param("workCode") String workCode, @Param("password") String password);

    int countUserByWorkcode(String workCode);

    void disableAccount(String workCode);

    void enableAccount(String workCode);

    void transferUser(@Param("workCode")String workCode, @Param("departmentId") Integer departmentId);

    List<String> getOrganizationInfo(Integer roleId);

    List<User> getUserInfoByWorkcode(String workCode);

    List<User> getDepartmentId(@Param("workCode")String workCode, @Param("userRealname")String userRealname);

    List<User> getUserInfoByRoleId(Integer roleId);

    User selectByWorkCodeOnly(String workCode);

    void delUserOfRole(String workCode);

    void insertList(@Param("user")User user);

    List<User> selectDirectors();

    List<User> selectDelegates();

    List<User> selectEngineers();

    void modifyPwd(@Param("workCode")String workCode, @Param("password")String password);

    List<User> getUserByDeptId(Integer deptId);

    int countUserByDeptId(Integer deptId);

    int modifyDeptOfUserInfo(@Param("workCode")String workCode,@Param("userRealname") String userRealname, @Param("departmentId") Integer departmentId, @Param("mobile")String mobile);

    void modifyUserInfoCompany(@Param("workCode")String workCode,@Param("userRealname") String userRealname,@Param("mobile")String mobile);

    void updateUserState(@Param("workCode")String workCode);

    void updateUserLoginOutState(@Param("workCode")String workCode);

    void updateUserPhone(@Param("workCode")String workCode, @Param("mobile") String mobile);
}