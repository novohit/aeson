package com.wyu.aeson.web.controller.monitor;

import com.wyu.aeson.common.core.domain.AjaxResult;
import com.wyu.aeson.framework.web.domain.Server;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 服务器监控
 *
 * @author zwx
 */
@RestController
@RequestMapping("/monitor/server")
public class ServerController {
    @PreAuthorize("hasPermission('monitor:server:list')")
    @GetMapping()
    public AjaxResult getInfo() throws Exception {
        Server server = new Server();
        server.copyTo();
        return AjaxResult.success(server);
    }
}
