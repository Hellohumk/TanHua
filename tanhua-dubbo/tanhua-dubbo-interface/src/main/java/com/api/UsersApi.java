package com.api;

import com.pojo.Users;
import com.pojo.vo.PageInfo;

import java.util.List;

public interface UsersApi {

    /**
     * 保存联系人 （点击聊一下即保存）
     * @param users
     * @return
     */
    String saveUsers(Users users);

    /**
     * 根据用户id查询Users列表
     *
     * @param userId
     * @return
     */
    List<Users> queryAllUsersList(Long userId);
    /**
     * 根据用户id查询Users列表(分页查询)
     *
     * @param userId
     * @return
     */
    PageInfo<Users> queryUsersList(Long userId, Integer page, Integer
            pageSize);
}
