package com.lzp.service;

import com.lzp.dao.UserRepository;
import com.lzp.po.User;
import com.lzp.util.MD5Utils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * @Author LZP
 * @Date 2021/5/1 22:02
 * @Version 1.0
 */
@Service
public class UserServiceImpl implements UserService{

    @Autowired
    private UserRepository userRepository;

    @Override
    public User checkUser(String username, String password) {
       return userRepository.findByUsernameAndPassword(username, MD5Utils.code(password));
    }
}
