package com.supervision.user.shiro;

import com.supervision.user.model.User;
import com.supervision.user.service.UserService;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationException;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;

/*
 * @Project:SupervisionSystem
 * @Description:custom realm
 * @Author:TjSanshao
 * @Create:2019-02-19 01:00
 *
 **/
public class CustomRealm extends AuthorizingRealm {
    @Autowired
    UserService userService;

    //shiro授权验证
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {

        if (principalCollection == null) {
            throw new AuthorizationException("PrincipalCollection method argument cannot be null.");
        }

        User user = (User)getAvailablePrincipal(principalCollection);

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();

        authorizationInfo.setRoles(userService.getRolesStrByUserId(user.getId()));  //设置角色列表
        authorizationInfo.setStringPermissions(userService.getPermissionsStrByUserId(user.getId()));  //设置权限列表
       
        return authorizationInfo;
    }

    //shiro登录验证
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {

        UsernamePasswordToken token = (UsernamePasswordToken) authenticationToken;
        String workCode = token.getUsername();

        if (workCode == null) {
            throw new AccountException("Null usernames are not allowed by this realm.");
        }

        User userInDb = userService.getUserByWorkCode(workCode);  //查询数据库

        if (userInDb == null) {
            throw new UnknownAccountException("No account found for [" + workCode + "]");
        }

        SimpleAuthenticationInfo info = new SimpleAuthenticationInfo(userInDb, userInDb.getPassword(), getName());

        return info;
    }
}
