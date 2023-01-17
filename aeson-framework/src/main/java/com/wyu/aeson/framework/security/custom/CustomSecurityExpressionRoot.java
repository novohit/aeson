package com.wyu.aeson.framework.security.custom;

import com.wyu.aeson.framework.security.context.PermissionContextHolder;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.util.AntPathMatcher;

import java.util.Collection;

/**
 * 我们仿造SecurityExpressionRoot的子类MethodSecurityExpressionRoot去编写我们自己的ExpressionRoot
 *
 * @author zwx
 * @date 2023-01-12 22:09
 */
public class CustomSecurityExpressionRoot extends SecurityExpressionRoot implements MethodSecurityExpressionOperations {

    private Object filterObject;

    private Object returnObject;

    private AntPathMatcher antPathMatcher = new AntPathMatcher();

    private Collection<? extends GrantedAuthority> authorities;

    public CustomSecurityExpressionRoot(Authentication authentication) {
        super(authentication);
        authorities = authentication.getAuthorities();
    }

    /**
     * 自定义一个参数的hasPermission方法
     * 并添加通配符的判断
     *
     * @param permission
     * @return
     */
    public boolean hasPermission(String permission) {
        PermissionContextHolder.setContext(permission);
        return this.hasAnyPermission(permission);
    }

    /**
     * 具有权限表达式中任意一个权限
     *
     * @param permissions
     * @return
     */
    public boolean hasAnyPermission(String... permissions) {
        for (String permission : permissions) {
            for (GrantedAuthority authority : this.authorities) {
                // 匹配所需权限中任意一个
                // match参数顺序 第一个参数是pattern是短的那个
                if (antPathMatcher.match(authority.getAuthority(), permission)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 具有权限表达式中所有权限
     *
     * @param permissions
     * @return
     */
    public boolean hasAllPermission(String... permissions) {
        for (String permission : permissions) {
            boolean match = false;
            for (GrantedAuthority authority : this.authorities) {
                // 匹配所需权限全部
                if (antPathMatcher.match(permission, authority.getAuthority())) {
                    match = true;
                }
            }
            if (!match) {
                return false;
            }
        }
        return true;
    }

    @Override
    public void setFilterObject(Object filterObject) {
        this.filterObject = filterObject;
    }

    @Override
    public Object getFilterObject() {
        return filterObject;
    }

    @Override
    public void setReturnObject(Object returnObject) {
        this.returnObject = returnObject;
    }

    @Override
    public Object getReturnObject() {
        return returnObject;
    }

    @Override
    public Object getThis() {
        return this;
    }
}
