package com.pym.numb.web.handler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 视图解析器，暂时只支持jsp转发
 */
public final class NumbViewHandler {

    private String prefix = "/WEB-INF/";
    private String suffix =".jsp";

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    public String getSuffix() {
        return suffix;
    }

    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    public void handler (String path, HttpServletRequest request, HttpServletResponse response) {
        String resultPath = path;
        if (resultPath.startsWith("redirect:")) {
            try {
                String fp = resultPath.replaceFirst("(redirect)\\s*:", "").trim();
                if (!fp.matches("^((http)|(https))\\s*:.*$")) {
                    if (!fp.startsWith(request.getContextPath())) {
                        fp = request.getContextPath() + fp;
                    }
                }
                response.sendRedirect(fp);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            resultPath = getPrefix() + resultPath + getSuffix();
            try {
                request.getRequestDispatcher(resultPath).forward(request,response);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
