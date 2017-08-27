package com.pym.numb.context.listener;

import com.pym.numb.context.core.NumbContainer;
import com.pym.numb.util.GlobalUtil;
import com.pym.numb.util.NumbConstants;
import com.pym.numb.util.StrUtils;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

/**
 * Created by Administrator on 2017/8/22.
 * 初始化容器
 */
public class NumbServletContextListener implements ServletContextListener {
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        ServletContext sc = sce.getServletContext();
       String factoryNames = sc.getInitParameter("factoryClasses");
        if (StrUtils.isEmpty(factoryNames)) {
            throw new RuntimeException("缺少工厂类");
        }
        NumbContainer container = new NumbContainer(factoryNames);
        container.init();
        GlobalUtil.init(sc,container);
        sc.setAttribute(NumbConstants.NUMB_GLOBAL_UTIL,GlobalUtil.getInstance());
    }

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        GlobalUtil globalUtil = GlobalUtil.getInstance();
        if (globalUtil != null) {
            globalUtil.destory();
        }
    }
}
