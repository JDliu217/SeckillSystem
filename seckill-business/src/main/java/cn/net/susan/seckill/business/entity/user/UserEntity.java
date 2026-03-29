package cn.net.susan.seckill.business.entity.user;

import cn.net.susan.entity.auth.AuthUserEntity;
import lombok.Data;

/**
 * 用户信息
 *
 */
@Data
public class UserEntity extends AuthUserEntity {

    private Long id;
}
