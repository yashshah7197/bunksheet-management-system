package io.yashshah.bunksheetmanagementsystem.data;

import java.util.HashMap;
import java.util.Map;

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

  public void setName(String name) {
    this.name = name;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhoneNumber() {
    return phoneNumber;
  }

  public void setPhoneNumber(String phoneNumber) {
    this.phoneNumber = phoneNumber;
  }

  public String getYear() {
    return year;
  }

  public void setYear(String year) {
    this.year = year;
  }

  public String getDivision() {
    return division;
  }

  public void setDivision(String division) {
    this.division = division;
  }

  public String getRollNumber() {
    return rollNumber;
  }

  public void setRollNumber(String rollNumber) {
    this.rollNumber = rollNumber;
  }

  public String getClassTeacher() {
    return classTeacher;
  }

  public void setClassTeacher(String classTeacher) {
    this.classTeacher = classTeacher;
  }

  public String getTeacherGuardian() {
    return teacherGuardian;
  }

  public void setTeacherGuardian(String teacherGuardian) {
    this.teacherGuardian = teacherGuardian;
  }

  public int getBunksheetsRequested() {
    return bunksheetsRequested;
  }

  public int getPrivilegeLevel() {
    return privilegeLevel;
  }

  public Map<String, Object> toMap() {
    Map<String, Object> hashMap = new HashMap<>();
    hashMap.put("name", name);
    hashMap.put("phoneNumber", phoneNumber);
    hashMap.put("year", year);
    hashMap.put("division", division);
    hashMap.put("rollNumber", rollNumber);
    hashMap.put("classTeacher", classTeacher);
    hashMap.put("teacherGuardian", teacherGuardian);

    return hashMap;
  }
}
