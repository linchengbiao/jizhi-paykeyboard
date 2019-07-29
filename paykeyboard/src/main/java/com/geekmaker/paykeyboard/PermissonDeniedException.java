package com.geekmaker.paykeyboard;

/**
 * USB 权键错误类
 */
class PermissonDeniedException extends Exception {
    public PermissonDeniedException(String no_permisson) {
        super(no_permisson);
    }
}
