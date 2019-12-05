package timeboard.core.model;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class TASData {

    private String matriculeID;
    private String name;
    private String firstName;
    private int month;
    private int year;
    private String businessCode;

    private List<String> dayMonthNames;
    private List<Integer> dayMonthNums;
    private Map<Integer, BigDecimal> workedDays;
    private Map<Integer, BigDecimal> offDays;
    private Map<Integer, BigDecimal> otherDays;
    private Map<Integer, String> comments;

    public String getMatriculeID() {
        return this.matriculeID;
    }

    public void setMatriculeID(final String matriculeID) {
        this.matriculeID = matriculeID;
    }

    public String getName() {
        return this.name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getFirstName() {
        return this.firstName;
    }

    public void setFirstName(final String firstName) {
        this.firstName = firstName;
    }

    public int getMonth() {
        return this.month;
    }

    public void setMonth(final int month) {
        this.month = month;
    }

    public int getYear() {
        return this.year;
    }

    public void setYear(final int year) {
        this.year = year;
    }

    public String getBusinessCode() {
        return this.businessCode;
    }

    public void setBusinessCode(final String businessCode) {
        this.businessCode = businessCode;
    }

    public List<String> getDayMonthNames() {
        return this.dayMonthNames;
    }

    public void setDayMonthNames(final List<String> dayMonthNames) {
        this.dayMonthNames = dayMonthNames;
    }

    public List<Integer> getDayMonthNums() {
        return this.dayMonthNums;
    }

    public void setDayMonthNums(final List<Integer> dayMonthNums) {
        this.dayMonthNums = dayMonthNums;
    }

    public Map<Integer, BigDecimal> getWorkedDays() {
        return this.workedDays;
    }

    public void setWorkedDays(final Map<Integer, BigDecimal> workedDays) {
        this.workedDays = workedDays;
    }

    public Map<Integer, BigDecimal> getOffDays() {
        return this.offDays;
    }

    public void setOffDays(final Map<Integer, BigDecimal> offDays) {
        this.offDays = offDays;
    }

    public Map<Integer, BigDecimal> getOtherDays() {
        return this.otherDays;
    }

    public void setOtherDays(final Map<Integer, BigDecimal> otherDays) {
        this.otherDays = otherDays;
    }

    public Map<Integer, String> getComments() {
        return this.comments;
    }

    public void setComments(final Map<Integer, String> comments) {
        this.comments = comments;
    }

}