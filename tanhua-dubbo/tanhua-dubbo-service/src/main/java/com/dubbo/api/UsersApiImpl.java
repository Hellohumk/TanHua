package com.dubbo.api;


import com.alibaba.dubbo.config.annotation.Service;
import com.dubbo.api.UsersApi;
import com.dubbo.pojo.Users;
import com.dubbo.pojo.vo.PageInfo;
import org.bson.types.ObjectId;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.util.List;

@Service(version = "1.0.0")
public class UsersApiImpl implements UsersApi {
    @Autowired
    private MongoTemplate mongoTemplate;

    @Override
    public String saveUsers(Users users) {
        //排错
        if(users.getFriendId() == null || users.getUserId() == null){
            return null;
        }

        //检查是否存在biaozhong
        Query query = Query.query(Criteria.where("userId").is(users.getUserId()).and("friendId").is(users.getFriendId()));

        Users oldUsers = mongoTemplate.findOne(query,Users.class);
        if(oldUsers != null){
            //已经存在了，不用做操作
            return null;
        }

        //添加
        users.setId(ObjectId.get());
        users.setDate(System.currentTimeMillis());
        this.mongoTemplate.save(users);
        return users.getId().toHexString();

    }

    @Override
    public List<Users> queryAllUsersList(Long userId) {
        Query query =Query.query(Criteria.where("userId").is(userId));

        return mongoTemplate.find(query, Users.class);
    }

    @Override
    public PageInfo<Users> queryUsersList(Long userId, Integer page, Integer pageSize) {
        PageRequest pageRequest = PageRequest.of(page -1,pageSize, Sort.by(Sort.Order.desc("created")));

        Query query = Query.query(Criteria.where("userId").is(userId)).with(pageRequest);

        List<Users> usersList = mongoTemplate.find(query, Users.class);

        PageInfo<Users> pageInfo = new PageInfo<>();
        pageInfo.setPageNum(page);
        pageInfo.setPageSize(pageSize);
        pageInfo.setRecords(usersList);
        pageInfo.setTotal(0); //不提供总数
        return pageInfo;

    }
}
