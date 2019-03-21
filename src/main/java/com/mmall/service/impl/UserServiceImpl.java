package com.mmall.service.impl;

import com.mmall.common.Const;
import com.mmall.common.ServerResponse;
import com.mmall.common.TokenCache;
import com.mmall.dao.UserMapper;
import com.mmall.pojo.User;
import com.mmall.service.IUserService;
import com.mmall.util.MD5Util;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
public class UserServiceImpl implements IUserService {
    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int result = userMapper.checkUsername(username);
        if (result == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }
        String md5Password = MD5Util.MD5EncodeUtf8(password);
        System.out.println("login:"+md5Password);
        User user = userMapper.selectLogin(username, md5Password);
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess("登录成功", user);
    }

    public ServerResponse<String> register(User user) {
        ServerResponse valid = checkValid(user.getUsername(), Const.USERNAME);
        if (!valid.isSuccess()) {
            return valid;
        }
        valid = checkValid(user.getEmail(), Const.EMAIL);
        if (!valid.isSuccess()) {
            return valid;
        }
        user.setRole(Const.Role.ROLE_CUSTOMER);
        user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int result = userMapper.insert(user);
        if (result == 0) {
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccess("注册成功");
    }

    public ServerResponse<String> checkValid(String str, String type) {
        if (StringUtils.isNotBlank(type)) {
            if (Const.USERNAME.equals(type)) {
                int result = userMapper.checkUsername(str);
                if (result > 0) {
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if (Const.EMAIL.equals(type)) {
                int result = userMapper.checkEmail(str);
                if (result > 0) {
                    return ServerResponse.createByErrorMessage("邮箱已存在");
                }
            }
        } else {
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验通过");
    }

    public ServerResponse<String> selectQuestion(String username) {
        ServerResponse valid = checkValid(username, Const.USERNAME);
        if (valid.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String question = userMapper.selectQuestionByUsername(username);
        if (StringUtils.isNotBlank(question)) {
            return ServerResponse.createBySuccess(question);
        }
        return ServerResponse.createByErrorMessage("未找到密保问题");
    }

    public ServerResponse<String> checkAnswer(String username, String question, String answer) {
        int result = userMapper.checkAnswer(username, question, answer);
        if (result > 0) {
            String forgetToken = UUID.randomUUID().toString();
            TokenCache.setKey(TokenCache.TOKEN_PREFIX + username, forgetToken);
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("答案不正确");
    }

    public ServerResponse<String> forgetResetPassword(String username, String newPassword, String forgetToken) {
        System.out.println("new:"+newPassword);
        if (StringUtils.isBlank(forgetToken)) {
            return ServerResponse.createByErrorMessage("参数错误，需要传递token");
        }
        ServerResponse valid = checkValid(username, Const.USERNAME);
        if (valid.isSuccess()) {
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX + username);
        if (StringUtils.isBlank(token)) {
            return ServerResponse.createByErrorMessage("token无效或过期");
        }
        if (StringUtils.equals(forgetToken, token)) {
            String md5Password = MD5Util.MD5EncodeUtf8(newPassword);
            System.out.println("md5:"+md5Password);
            int row = userMapper.updatePasswordByUsername(username, md5Password);
            if (row > 0) {
                return ServerResponse.createBySuccessMessage("密码修改成功");
            }
        } else {
            return ServerResponse.createByErrorMessage("token错误，请重新获取");
        }
        return ServerResponse.createByErrorMessage("密码修改失败");
    }

    @Override
    public ServerResponse<String> resetPassword(String oldPassword, String newPassword, User user) {
        //防止横向越权，校验此用户的旧密码
        int result = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(oldPassword), user.getId());
        if (result == 0) {
            return ServerResponse.createByErrorMessage("旧密码错误");
        }
        user.setPassword(MD5Util.MD5EncodeUtf8(newPassword));
        result = userMapper.updateByPrimaryKeySelective(user);
        if (result > 0) {
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    public ServerResponse<User> updateInformation(User user) {
        //username不可更新
        //新email也要进行校验，如果已存在，不可以时当前用户的
        int result = userMapper.checkEmailByUserId(user.getEmail(), user.getId());
        if (result > 0) {
            return ServerResponse.createByErrorMessage("email已存在，请重新输入");
        }
        User updateUser = new User();
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int row = userMapper.updateByPrimaryKeySelective(user);
        if (row > 0) {
            return ServerResponse.createBySuccess("个人信息更新成功", user);
        }
        return ServerResponse.createByErrorMessage("个人信息更新失败");
    }

    public ServerResponse<User> getInformation(Integer userId) {
        User user = userMapper.selectByPrimaryKey(userId);
        if (user == null) {
            return ServerResponse.createByErrorMessage("未找到该用户");
        }
        user.setPassword(StringUtils.EMPTY);
        return ServerResponse.createBySuccess(user);
    }

    //backend
    public ServerResponse checkAdminRole(User user) {
        if (user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN) {
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }
}
