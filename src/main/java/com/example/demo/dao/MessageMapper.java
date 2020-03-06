package com.example.demo.dao;

import com.example.demo.entity.Message;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface MessageMapper {


    //查询用户的会话列表，显示每个会话的最新消息 分页显示
    List<Message> selectConversation(@Param("userId") int userId, @Param("offset") int offset, @Param("limit") int limit);

    //查询用户的会话数量，用于分页
    int selectConversationCount(@Param("userId") int userId);

    int selectConversationCount2(@Param("userId") int userId);

    //查询会话的消息列表
    // 用户0001与用户0002的之间的所有消息都由conversationId来指示
    //0001->00002
    //0001<-00002
    //00001_00002
    List<Message> selectMessageByConversationId(@Param("conversationId") String conversationId, @Param("offset") int offset, @Param("limit") int limit);


    //查询会话的消息数
    int selectMessageCount(@Param("conversationId") String conversationId);

    //查询某个会话未读的消息数
    int selectUnreadMessageCount(@Param("conversationId") String conversationId, @Param("userId") int userId);

    // 添加消息
    int insertMessage(Message message);


    // 修改消息状态
    // 一次可能改多个消息的状态，而且要看是谁来读的，如果用conversationId来指示，就要在sql里判断，感觉麻烦，
    // 所以直接传ids来改，删除消息也用这个函数
    int updateMessage(@Param("ids") List<Integer> ids, @Param("status") int status);

}
