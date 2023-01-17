package com.wyu.aeson.framework.web.service;

import javax.annotation.Resource;

import com.wyu.aeson.framework.manager.AsyncManager;
import com.wyu.aeson.framework.manager.factory.AsyncFactory;
import com.wyu.aeson.framework.security.context.AuthenticationContextHolder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import com.wyu.aeson.common.constant.CacheConstants;
import com.wyu.aeson.common.constant.Constants;
import com.wyu.aeson.common.core.domain.entity.SysUser;
import com.wyu.aeson.common.core.domain.model.LoginUser;
import com.wyu.aeson.common.core.redis.RedisCache;
import com.wyu.aeson.common.exception.ServiceException;
import com.wyu.aeson.common.exception.user.CaptchaException;
import com.wyu.aeson.common.exception.user.CaptchaExpireException;
import com.wyu.aeson.common.exception.user.UserPasswordNotMatchException;
import com.wyu.aeson.common.utils.DateUtils;
import com.wyu.aeson.common.utils.MessageUtils;
import com.wyu.aeson.common.utils.ServletUtils;
import com.wyu.aeson.common.utils.StringUtils;
import com.wyu.aeson.common.utils.ip.IpUtils;
import com.wyu.aeson.system.service.ISysConfigService;
import com.wyu.aeson.system.service.ISysUserService;

/**
 * 登录校验方法
 *
 * @author zwx
 */
@Component
public class SysLoginService {
    @Autowired
    private TokenService tokenService;

    @Resource
    private AuthenticationManager authenticationManager;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysUserService userService;

    @Autowired
    private ISysConfigService configService;

    /**
     * 登录验证
     *
     * @param username  用户名
     * @param password  密码
     * @param code      验证码
     * @param captchaId 验证码唯一标识
     * @return 结果
     */
    public String login(String username, String password, String code, String captchaId) {
        // 先去redis中查验证码是否开启
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        // 验证码开关
        if (captchaEnabled) {
            validateCaptcha(username, code, captchaId);
        }
        // 用户验证
        Authentication authentication = null;
        try {
            UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(username, password);
            AuthenticationContextHolder.setContext(authenticationToken);
            // 执行登录 该方法会去调用UserDetailsServiceImpl.loadUserByUsername
            authentication = authenticationManager.authenticate(authenticationToken);
        } catch (Exception e) {
            if (e instanceof BadCredentialsException) {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.password.not.match")));
                throw new UserPasswordNotMatchException();
            } else {
                AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, e.getMessage()));
                throw new ServiceException(e.getMessage());
            }
        } finally {
            AuthenticationContextHolder.clearContext();
        }
        AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_SUCCESS, MessageUtils.message("user.login.success")));
        LoginUser loginUser = (LoginUser) authentication.getPrincipal();
        recordLoginInfo(loginUser.getUserId());
        // 生成token
        return tokenService.createToken(loginUser);
    }

    /**
     * 校验验证码
     *
     * @param username  用户名
     * @param code      验证码
     * @param captchaId 唯一标识
     * @return 结果
     */
    public void validateCaptcha(String username, String code, String captchaId) {
        // key captcha_codes:385aa48888414f7285d8fbb04e7751b6 value 验证码答案
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + StringUtils.nvl(captchaId, "");
        String captcha = redisCache.getCacheObject(verifyKey);
        // 不管是否登录成功 key使用过一次就会删除
        redisCache.deleteObject(verifyKey);
        if (captcha == null) {
            // 验证码过期 为null
            // 开启一个异步任务记录日志
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.expire")));
            throw new CaptchaExpireException();
        }
        if (!code.equalsIgnoreCase(captcha)) {
            AsyncManager.me().execute(AsyncFactory.recordLogininfor(username, Constants.LOGIN_FAIL, MessageUtils.message("user.jcaptcha.error")));
            throw new CaptchaException();
        }
    }

    /**
     * 记录登录信息
     *
     * @param userId 用户ID
     */
    public void recordLoginInfo(Long userId) {
        SysUser sysUser = new SysUser();
        sysUser.setUserId(userId);
        sysUser.setLoginIp(IpUtils.getIpAddr(ServletUtils.getRequest()));
        sysUser.setLoginDate(DateUtils.getNowDate());
        userService.updateUserProfile(sysUser);
    }
}
