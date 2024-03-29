package com.ry.cbms.decision.server.dto;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import com.ry.cbms.decision.server.model.Permission;
import com.ry.cbms.decision.server.model.SysUser;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.annotation.JsonIgnore;

@ApiModel(value = "登陆用户类")
public class LoginUser extends SysUser implements UserDetails {

    private static final long serialVersionUID = -1379274258881257107L;
    @ApiModelProperty(value = "权限")
    private List<Permission> permissions;
    @ApiModelProperty(value = "token")
    private String token;
    @ApiModelProperty(value = "登陆时间戳（毫秒）")

    /** 登陆时间戳（毫秒） */
    private Long loginTime;
    @ApiModelProperty(value = "过期时间戳 ")
    /** 过期时间戳 */
    private Long expireTime;

    public List<Permission> getPermissions() {
        return permissions;
    }

    public void setPermissions(List<Permission> permissions) {
        this.permissions = permissions;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return permissions.parallelStream ().filter (p -> !StringUtils.isEmpty (p.getPermission ()))
                .map (p -> new SimpleGrantedAuthority (p.getPermission ())).collect (Collectors.toSet ());
    }

    public void setAuthorities(Collection<? extends GrantedAuthority> authorities) {
        // do nothing
    }

    // 账户是否未过期
    @JsonIgnore
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    // 账户是否未锁定
    @JsonIgnore
    @Override
    public boolean isAccountNonLocked() {
        return getStatus () != Status.LOCKED;
    }

    // 密码是否未过期
    @JsonIgnore
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    // 账户是否激活
    @JsonIgnore
    @Override
    public boolean isEnabled() {
        return true;
    }

    public Long getLoginTime() {
        return loginTime;
    }

    public void setLoginTime(Long loginTime) {
        this.loginTime = loginTime;
    }

    public Long getExpireTime() {
        return expireTime;
    }

    public void setExpireTime(Long expireTime) {
        this.expireTime = expireTime;
    }

}
