package com.example.user_service.entity;

/**
 * User roles in JLR Parts Ordering System
 * Defines authorization levels for different user types
 */
public enum UserRole {

    /**
     * Individual car owners who purchase parts for personal vehicles
     * Permissions: Order standard parts, view own orders, update profile
     */
    CUSTOMER,

    /**
     * JLR dealership managers with elevated privileges
     * Permissions: Emergency orders, bulk orders, manage dealership inventory,
     *             view dealership reports, manage dealership staff
     */
    DEALER_MANAGER,

    /**
     * JLR dealership employees who assist customers
     * Permissions: Place orders for customers, view dealership inventory,
     *             access basic dealership functions
     */
    DEALER_EMPLOYEE,

    /**
     * System administrators with full access
     * Permissions: User management, system configuration, all reports,
     *             emergency system functions
     */
    ADMIN
}