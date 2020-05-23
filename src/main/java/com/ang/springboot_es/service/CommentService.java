package com.ang.springboot_es.service;

import com.ang.springboot_es.dao.CommentMapper;
import com.ang.springboot_es.dao.DiscussPostMapper;
import com.ang.springboot_es.entity.Comment;
import com.ang.springboot_es.util.DemoConstant;
import com.ang.springboot_es.util.SensitiveFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.HtmlUtils;

import java.util.List;


@Service
public class CommentService implements DemoConstant {

    @Autowired
    private CommentMapper commentMapper;

    @Autowired
    private SensitiveFilter sensitiveFilter;

    @Autowired
    private DiscussPostMapper discussPostMapper;

    public List<Comment> findCommentsByEntity(int entityType, int entityId, int offset, int limit) {
        return commentMapper.selectByEntity(entityType, entityId, offset, limit);
    }


    public int findCommentsRows(int entityType, int entityId) {
        return commentMapper.selectCountByEntity(entityType, entityId);
    }

    public Comment findCommentById(int id) {
        return commentMapper.selectCommentById(id);
    }

    //读提交
    @Transactional(isolation = Isolation.READ_COMMITTED, propagation = Propagation.REQUIRED)
    public int addComment(Comment comment) {
        //过滤一下内容
        if (comment == null) {
            throw new IllegalArgumentException("参数不能为空");
        }
        comment.setContent(HtmlUtils.htmlEscape(comment.getContent()));
        comment.setContent(sensitiveFilter.filter(comment.getContent()));
        int rows = commentMapper.insertComment(comment);

        //更新贴子的评论数量
        if (comment.getEntityType() == ENTITY_TYPE_DISCUSSPOST) {
            int count = commentMapper.selectCountByEntity(comment.getEntityType(), comment.getEntityId());
            discussPostMapper.updateCommentCount(comment.getEntityId(), count);
        }
        return rows;
    }
}
