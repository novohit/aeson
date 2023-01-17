package com.wyu.aeson.framework.security.custom;

import org.aopalliance.intercept.MethodInvocation;
import org.springframework.security.access.expression.method.DefaultMethodSecurityExpressionHandler;
import org.springframework.security.access.expression.method.MethodSecurityExpressionOperations;
import org.springframework.security.core.Authentication;

/**
 * @author zwx
 * @date 2023-01-12 22:57
 */
public class CustomMethodSecurityExpressionHandler extends DefaultMethodSecurityExpressionHandler {
    /**
     * 重写创建ExpressionRootObject的方法
     * @param authentication
     * @param invocation
     * @return
     */
    @Override
    protected MethodSecurityExpressionOperations createSecurityExpressionRoot(Authentication authentication, MethodInvocation invocation) {
        CustomSecurityExpressionRoot root = new CustomSecurityExpressionRoot(authentication);
        root.setTrustResolver(getTrustResolver());
        // 角色继承
        root.setRoleHierarchy(getRoleHierarchy());
        root.setDefaultRolePrefix(getDefaultRolePrefix());
        // 这里设置的是security的hasPermission两个参数那个权限评估器 我们已经自定义了一个参数的hasPermission 不需要设置了
        //root.setPermissionEvaluator(getPermissionEvaluator());
        return root;
    }
}
