package com.mseei.myhealthcare;

public class ChildAccount {
    private int ChildAccountID;
    private String LoginEmail;
    private String FirstName;
    private String FirstLastName;
    private String SecondLastName;
    private boolean Status;
    private boolean Blocked;
    private int FailedLoginAttempts;
    private String CreatedOn;
    private boolean RealTimeMonitoring;
    private int Perimeter;
    private boolean PendingLocationConfig;

    // Getters and Setters
    public int getChildAccountID() {
        return ChildAccountID;
    }

    public void setChildAccountID(int childAccountID) {
        ChildAccountID = childAccountID;
    }

    public String getLoginEmail() {
        return LoginEmail;
    }

    public void setLoginEmail(String loginEmail) {
        LoginEmail = loginEmail;
    }

    public String getFirstName() {
        return FirstName;
    }

    public void setFirstName(String firstName) {
        FirstName = firstName;
    }

    public String getFirstLastName() {
        return FirstLastName;
    }

    public void setFirstLastName(String firstLastName) {
        FirstLastName = firstLastName;
    }

    public String getSecondLastName() {
        return SecondLastName;
    }

    public void setSecondLastName(String secondLastName) {
        SecondLastName = secondLastName;
    }

    public boolean isStatus() {
        return Status;
    }

    public void setStatus(boolean status) {
        Status = status;
    }

    public boolean isBlocked() {
        return Blocked;
    }

    public void setBlocked(boolean blocked) {
        Blocked = blocked;
    }

    public int getFailedLoginAttempts() {
        return FailedLoginAttempts;
    }

    public void setFailedLoginAttempts(int failedLoginAttempts) {
        FailedLoginAttempts = failedLoginAttempts;
    }

    public String getCreatedOn() {
        return CreatedOn;
    }

    public void setCreatedOn(String createdOn) {
        CreatedOn = createdOn;
    }

    public boolean isRealTimeMonitoring() {
        return RealTimeMonitoring;
    }

    public void setRealTimeMonitoring(boolean realTimeMonitoring) {
        RealTimeMonitoring = realTimeMonitoring;
    }

    public int getPerimeter() {
        return Perimeter;
    }

    public void setPerimeter(int perimeter) {
        Perimeter = perimeter;
    }

    public boolean isPendingLocationConfig() {
        return PendingLocationConfig;
    }

    public void setPendingLocationConfig(boolean pendingLocationConfig) {
        PendingLocationConfig = pendingLocationConfig;
    }
}
