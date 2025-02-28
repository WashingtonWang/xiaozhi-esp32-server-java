package com.xiaozhi.common.exception;

/**
 * 用户名没有找到异常
 * 
 * @author Joey
 */
@SuppressWarnings("serial")
public class UsernameNotFoundException extends Exception {
  public UsernameNotFoundException() {
  }

  public UsernameNotFoundException(String msg) {
    super(msg);
  }
}