package com.supervision.user.model;

public class UserDetail {
    private String workCode;

    private Integer age;

    private String gender;

    private String email;

    private String iceName;

    private String icePhone;

    public String getWorkCode() {
        return workCode;
    }

    public void setWorkCode(String workCode) {
        this.workCode = workCode == null ? null : workCode.trim();
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender == null ? null : gender.trim();
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email == null ? null : email.trim();
    }

    public String getIceName() {
        return iceName;
    }

    public void setIceName(String iceName) {
        this.iceName = iceName == null ? null : iceName.trim();
    }

    public String getIcePhone() {
        return icePhone;
    }

    public void setIcePhone(String icePhone) {
        this.icePhone = icePhone == null ? null : icePhone.trim();
    }
}