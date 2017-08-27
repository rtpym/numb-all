package com.pym.numb.util;

import com.pym.numb.context.core.NumbContainer;
import com.pym.numb.context.lifecycle.NumbLifecycle;

import javax.servlet.ServletContext;

/**
 * Created by Administrator on 2017/8/26.
 */
public class GlobalUtil implements NumbLifecycle{
    private static GlobalUtil globalUtil = null;
    private ServletContext servletContext;
    private NumbContainer numbContainer;

    public ServletContext getServletContext() {
        return servletContext;
    }

    public NumbContainer getNumbContainer() {
        return numbContainer;
    }

    private GlobalUtil(ServletContext servletContext, NumbContainer numbContainer) {
        this.numbContainer = numbContainer;
        this.servletContext = servletContext;
    }

    public static void init(ServletContext servletContext, NumbContainer numbContainer) {
        if (globalUtil == null) {
            synchronized (GlobalUtil.class) {
                if (globalUtil == null) {
                    globalUtil = new GlobalUtil(servletContext,numbContainer);
                }
            }
        }
    }
    public static  GlobalUtil getInstance() {
        return globalUtil;
    }

    @Override
    public void init() {

    }

    @Override
    public void destory() {
        if (this.numbContainer != null) {
            this.numbContainer.destory();
        }
        this.numbContainer = null;
        this.servletContext = null;
    }
}
