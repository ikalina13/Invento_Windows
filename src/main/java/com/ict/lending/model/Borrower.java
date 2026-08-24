package com.ict.lending.model;

/**
 * Person borrowing ICT equipment.
 */
public class Borrower {
    private int borrowerId;
    private String fullName;
    private String position;
    private String gradeLevel;
    private String section;
    private String purpose;

    public Borrower() {
    }

    public Borrower(int borrowerId, String fullName, String position,
                    String gradeLevel, String section, String purpose) {
        this.borrowerId = borrowerId;
        this.fullName = fullName;
        this.position = position;
        this.gradeLevel = gradeLevel;
        this.section = section;
        this.purpose = purpose;
    }

    public int getBorrowerId() {
        return borrowerId;
    }

    public void setBorrowerId(int borrowerId) {
        this.borrowerId = borrowerId;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public String getGradeLevel() {
        return gradeLevel;
    }

    public void setGradeLevel(String gradeLevel) {
        this.gradeLevel = gradeLevel;
    }

    public String getSection() {
        return section;
    }

    public void setSection(String section) {
        this.section = section;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
    }
}
