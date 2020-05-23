package com.ang.springboot_es.dao;

import com.ang.springboot_es.entity.Comment;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;


@Mapper
public interface CommentMapper {


    List<Comment> selectByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId, @Param("offset") int offset, @Param("limit") int limit);

    int selectCountByEntity(@Param("entityType") int entityType, @Param("entityId") int entityId);

    int insertComment(Comment comment);

    Comment selectCommentById(@Param("id") int id);

}
