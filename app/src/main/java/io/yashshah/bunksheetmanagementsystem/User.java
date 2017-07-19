package io.yashshah.bunksheetmanagementsystem;

/**
 * Created by yashshah on 19/07/17.
 */

public class User {

    public static final int PRIVILEGE_STUDENT = 0;
    public static final int PRIVILEGE_HEAD = 1;
    public static final int PRIVILEGE_TEACHER = 2;
    public static final int PRIVILEGE_HOD = 3;

    private String name;
    private String email;
    private String phoneNumber;
    private String year;
    private String division;
    private String rollNumber;
    private String classTeacher;
    private String teacherGuardian;
    private int bunksheetsRequested;
    private int privilegeLevel;

    public User() {
    }

    public User(String name, String email) {
        this.name = name;
        this.email = email;
        this.phoneNumber = "";
        this.year = "";
        this.division = "";
        this.rollNumber = "";
        this.classTeacher = "";
        this.teacherGuardian = "";
        this.bunksheetsRequested = 0;
        this.privilegeLevel = PRIVILEGE_STUDENT;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getYear() {
        return year;
    }

    public String getDivision() {
        return division;
    }

    public String getRollNumber() {
        return rollNumber;
    }

    public String getClassTeacher() {
        return classTeacher;
    }

    public String getTeacherGuardian() {
        return teacherGuardian;
    }

    public int getBunksheetsRequested() {
        return bunksheetsRequested;
    }

    public int getPrivilegeLevel() {
        return privilegeLevel;
    }
}
