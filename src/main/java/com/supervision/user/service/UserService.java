package com.supervision.user.service;

import com.supervision.user.model.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface UserService {

    Object getUserByPage(int currentPage, int pageSize);

    String addUser(Map<String, String> user,int length);

    String delUser(Integer id);

    String getUserInfo(HttpServletRequest request, String workoCde, String password);

    String disableAccount(String userState);

    String enableAccount(String workCode);

    String transferUser(String workCode, Integer departmentId);

    User getUser(int userId);

    User getUserByWorkCode(String workCode);

    Set<String> getRolesStrByUserId(int userId);

    Set<String> getPermissionsStrByUserId(int userId);

    boolean setUserDirector(Integer userId);

    boolean setUserDelegate(Integer userId);

    boolean setUserNormal(Integer userId);

    String delUserOfRole(String workCode);

    List<User> getAllDirector();

    List<User> getAllDelegates();

    List<User> getAllEngineers();

    String modifyPwd(String workCode, String password);

    String getUserInfoByWorkCode(String workCode);

    String getUserListOfRole(Integer roleId);

    String checkUserInfoByWorkCode(String workCode);

    String getUserInfoByCompanyId(Integer deptId);

    String modifyUserInfoDept(String workCode, String userName, Integer deptId, String phone);

    String modifyUserInfoCompany(String workCode, String userName, String phone);

    void updateUserLoginOutState(String workCode);

    String delUserFromDept(String workCode);

    String updateUserInfo(String workCode, String mobile, Integer age, String email, String iceName, String icePhone);

    String getUserInfoByDeptId(Integer deptId);
}
