package com.freightflow.users;

/**
 * Application roles used for RBAC. Authorities are emitted as ROLE_<name>.
 */
public enum UserRole {
    ADMIN,
    MANAGER,
    DISPATCHER,
    DRIVER
}
