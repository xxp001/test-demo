package com.supervision.user.mapper;

import com.supervision.user.model.UserDetail;
import org.apache.ibatis.annotations.Param;

public interface UserDetailMapper {
    int insert(UserDetail record);

    int insertSelective(UserDetail record);

    void updateUserDetailInfo(@Param("workCode") String workCode, @Param("age")Integer age, @Param("email")String email, @Param("iceName")String iceName, @Param("icePhone")String icePhone);

    UserDetail selectUserDetailInfo(String workCode);
}