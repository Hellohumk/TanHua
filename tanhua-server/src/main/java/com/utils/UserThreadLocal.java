package com.utils;

import com.pojo.User;

/**
 * 跟直接new一个后get set无区别，但这样不用new了，爽死
 */

public class UserThreadLocal{
    private static final ThreadLocal<User> LOCAL = new ThreadLocal<User>();

    private UserThreadLocal(){

    }

    public static void set(User user){
       LOCAL.set(user);
    }

    public static User get() {
        return LOCAL.get();
    }
}
