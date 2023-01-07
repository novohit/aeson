package com.wyu.aeson.web.controller.common;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;
import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;

import com.wyu.aeson.common.config.AesonConfig;
import com.wyu.aeson.common.constant.CacheConstants;
import com.wyu.aeson.common.constant.Constants;
import com.wyu.aeson.common.core.domain.AjaxResult;
import com.wyu.aeson.common.core.redis.RedisCache;
import com.wyu.aeson.common.utils.sign.Base64;
import com.wyu.aeson.common.utils.uuid.IdUtils;
import com.wyu.aeson.system.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.FastByteArrayOutputStream;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.code.kaptcha.Producer;

/**
 * 验证码操作处理
 *
 * @author zwx……
 */
@RestController
public class CaptchaController {
    @Resource(name = "captchaProducer")
    private Producer captchaProducer;

    @Resource(name = "captchaProducerMath")
    private Producer captchaProducerMath;

    @Autowired
    private RedisCache redisCache;

    @Autowired
    private ISysConfigService configService;

    /**
     * 生成验证码
     */
    @GetMapping("/captchaImage")
    public AjaxResult getCode(HttpServletResponse response) throws IOException {
        AjaxResult ajax = AjaxResult.success();
        // 去数据库中查询是否开启验证码功能
        boolean captchaEnabled = configService.selectCaptchaEnabled();
        ajax.put("captchaEnabled", captchaEnabled);
        if (!captchaEnabled) {
            return ajax;
        }

        // 保存验证码信息
        String captchaId = IdUtils.simpleUUID();
        String verifyKey = CacheConstants.CAPTCHA_CODE_KEY + captchaId;

        String capStr = null, code = null;
        BufferedImage image = null;

        // 生成验证码
        String captchaType = AesonConfig.getCaptchaType();
        if ("math".equals(captchaType)) {
            // 1+1=?@2
            String capText = captchaProducerMath.createText();
            System.out.println("capText = " + capText);
            capStr = capText.substring(0, capText.lastIndexOf("@"));
            code = capText.substring(capText.lastIndexOf("@") + 1);
            // 传算术表达式1+1=? 进行画图
            image = captchaProducerMath.createImage(capStr);
        } else if ("char".equals(captchaType)) {
            capStr = code = captchaProducer.createText();
            image = captchaProducer.createImage(capStr);
        }

        redisCache.setCacheObject(verifyKey, code, Constants.CAPTCHA_EXPIRATION, TimeUnit.MINUTES);
        // 转换流信息写出
        FastByteArrayOutputStream os = new FastByteArrayOutputStream();
        try {
            ImageIO.write(image, "jpg", os);
        } catch (IOException e) {
            return AjaxResult.error(e.getMessage());
        }

        ajax.put("captchaId", captchaId);
        ajax.put("img", Base64.encode(os.toByteArray()));
        return ajax;
    }
}
