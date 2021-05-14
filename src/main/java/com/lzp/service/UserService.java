package com.lzp.service;

import com.lzp.po.User;

public interface UserService {

    User checkUser(String username, String password);
}
