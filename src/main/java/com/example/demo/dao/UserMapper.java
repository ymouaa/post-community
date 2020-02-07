package com.example.demo.dao;


import com.example.demo.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

//@Repository

@Mapper
public interface UserMapper {

    /*
    * @Select("SELECT * FROM CITY WHERE state = #{state}")
            City findByState(@Param("state") String state);
    *
    * */
   // @Select("select * from user where id =#{id}")


    User selectById(int id);

    User selectByName(String username);

    User selectByEmail(String email);

    int insertUser(User user);

    int updateStatus(@Param("id")int id, @Param("status")int status);

    int updateHeader(@Param("id")int id,@Param("headerUrl")String headerUrl);

    int updatePassword(@Param("id")int id,@Param("password")String password);

}
