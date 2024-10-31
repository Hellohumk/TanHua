package com.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.mapper.QuestionMapper;
import com.pojo.Question;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class QuestionService {

    @Autowired
    private QuestionMapper questionMapper;

    //根据条件找一个
    public Question queryQuestion(Long userId){
        QueryWrapper queryWrapper = new QueryWrapper();
        queryWrapper.eq("user_id",userId);
        return questionMapper.selectOne(queryWrapper);
    }
}
