package com.atguigu.gulimallcart.interceptor;//package com.atguigu.gulimallcart.interceptor;
//
//import com.atguigu.common.constant.AuthServerConstant;
//import com.atguigu.common.constant.CartConstant;
//import com.atguigu.common.vo.MemberResponseVo;
//import com.atguigu.gulimallcart.to.UserInfoTo;
//import org.springframework.lang.Nullable;
//import org.springframework.stereotype.Component;
//import org.springframework.web.servlet.HandlerInterceptor;
//import org.springframework.web.servlet.ModelAndView;
//
//import javax.servlet.http.Cookie;
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import javax.servlet.http.HttpSession;
//
///**
// * 在执行目标之前，判断用户的登录状态，并封装传递给controller目标请求
// *
// */
////@Component
//public class CartInterceptor implements HandlerInterceptor {
//
//
//    /**
//     * 在同一线程下资源共享
//     */
//    public static ThreadLocal<UserInfoTo>  threadLocal = new ThreadLocal<>();
//
//    /**
//     * 目标方法执行之前
//     *
//     * @param request
//     * @param response
//     * @param handler
//     *
//     * @throws Exception
//     */
//
//    public boolean postHandle(HttpServletRequest request,
//                           HttpServletResponse response,
//                           Object handler
//                           ) throws Exception {
//
//
//
//        UserInfoTo userInfoTo = new UserInfoTo();
//        HttpSession session = request.getSession();
//        MemberResponseVo attribute = (MemberResponseVo) session.getAttribute(AuthServerConstant.LOGIN_USER);
//        if (attribute != null) {
//            //说明用户没登录
//            userInfoTo.setUserId(attribute.getId());
//        }
//
//        Cookie[] cookies = request.getCookies();
//        if (cookies != null && cookies.length > 0) {
//            for (Cookie cookie : cookies) {
//                String name = cookie.getName();
//                if (name.equals(CartConstant.TEMP_USER_COOKIE_NAME)) {
//                    userInfoTo.setUserKey(cookie.getValue());
//                }
//
//            }
//        }
//        threadLocal.set(userInfoTo);
//        return true;
//    }
//}
//
//


import com.atguigu.common.vo.MemberResponseVo;
import com.atguigu.gulimallcart.to.UserInfoTo;

import org.apache.commons.lang.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.UUID;

import static com.atguigu.common.constant.AuthServerConstant.LOGIN_USER;
import static com.atguigu.common.constant.CartConstant.TEMP_USER_COOKIE_NAME;
import static com.atguigu.common.constant.CartConstant.TEMP_USER_COOKIE_TIMEOUT;

/**
 * @Description: 在执行目标方法之前，判断用户的登录状态.并封装传递给controller目标请求
 * @Created: with IntelliJ IDEA.
 * @author: 夏沫止水
 * @createTime: 2020-06-30 17:31
 **/

public class CartInterceptor implements HandlerInterceptor {


    public static ThreadLocal<UserInfoTo> toThreadLocal = new ThreadLocal<>();

    /***
     * 目标方法执行之前
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        UserInfoTo userInfoTo = new UserInfoTo();

        HttpSession session = request.getSession();
        //获得当前登录用户的信息
        MemberResponseVo memberResponseVo = (MemberResponseVo) session.getAttribute("loginUser");

        if (memberResponseVo != null) {
            //用户登录了
            userInfoTo.setUserId(memberResponseVo.getId());
        }

        Cookie[] cookies = request.getCookies();
        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                //user-key
                String name = cookie.getName();
                if (name.equals(TEMP_USER_COOKIE_NAME)) {
                    userInfoTo.setUserKey(cookie.getValue());
                    //标记为已是临时用户
                    userInfoTo.setTempUser(true);
                }
            }
        }

        //如果没有临时用户一定分配一个临时用户
        if (StringUtils.isEmpty(userInfoTo.getUserKey())) {
            String uuid = UUID.randomUUID().toString();
            userInfoTo.setUserKey(uuid);
        }

        //目标方法执行之前
        toThreadLocal.set(userInfoTo);
        return true;
    }


    /**
     * 业务执行之后，分配临时用户来浏览器保存
     * @param request
     * @param response
     * @param handler
     * @param modelAndView
     * @throws Exception
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {

        //获取当前用户的值
        UserInfoTo userInfoTo = toThreadLocal.get();

        //如果没有临时用户一定保存一个临时用户
        if (!userInfoTo.getTempUser()) {
            //创建一个cookie
            Cookie cookie = new Cookie(TEMP_USER_COOKIE_NAME, userInfoTo.getUserKey());
            //扩大作用域
            cookie.setDomain("localhost");
            //设置过期时间
            cookie.setMaxAge(TEMP_USER_COOKIE_TIMEOUT);
            response.addCookie(cookie);
        }

    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {

    }
}