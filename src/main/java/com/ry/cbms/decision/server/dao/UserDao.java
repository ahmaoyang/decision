package com.ry.cbms.decision.server.dao;

import java.util.List;
import java.util.Map;

import com.ry.cbms.decision.server.model.SysUser;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface UserDao {

    @Options(useGeneratedKeys = true, keyProperty = "id")
    @Insert("insert into sys_user(username, password, headImgUrl, phone, email, birthday, sex, status, createTime, updateTime) values(#{username}, #{password}, #{headImgUrl}, #{phone}, #{email}, #{birthday}, #{sex}, #{status}, #{createTime}, #{updateTime})")
    int save(SysUser user);

    @Select("select * from sys_user t where t.id = #{id}")
    SysUser getById(String id);

    @Select("select * from sys_user t where  t.username=#{username} or t.phone=#{username} or t.email=#{username}")
    SysUser getUser(String username);

    @Select("select * from sys_user t where   t.username=#{username} and t.phone=#{phone} and t.email=#{email}")
    SysUser getUserByUserNameAndPhoneAndEmail(String username, String phone, String email);


    @Select("select * from sys_user t where   t.username=#{username} or t.phone=#{phone} or t.email=#{email}")
    List<SysUser> getUserByUserNameOrPhoneOrEmail(String username, String phone, String email);

    @Update("update sys_user t set t.password = #{password} where t.id = #{id}")
    int changePassword(@Param("id") Long id, @Param("password") String password);

    Integer count(@Param("params") Map<String, Object> params);

    List<SysUser> list(@Param("params") Map<String, Object> params, @Param("offset") Integer offset,
                       @Param("limit") Integer limit);

    @Delete("delete from sys_role_user where userId = #{userId}")
    int deleteUserRole(Long userId);

    int saveUserRoles(@Param("userId") Long userId, @Param("roleIds") List<Long> roleIds);

    int update(SysUser user);

    @Delete("delete from sys_user where id = #{id}")
    int deleteUser(String id);


    List<SysUser> getUserByCondition(String email, String phone, String name);
}
