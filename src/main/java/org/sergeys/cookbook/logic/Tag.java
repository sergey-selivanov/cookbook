package org.sergeys.cookbook.logic;

public class Tag {

    public static final int SPECIAL_OTHER = 1;

    private long id;
    private long parentid;
    private String val;
    private Integer specialid;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getParentid() {
        return parentid;
    }

    public void setParentid(long parentid) {
        this.parentid = parentid;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    public Integer getSpecialid() {
        return specialid;
    }

    public void setSpecialid(Integer specialid) {
        this.specialid = specialid;
    }

}
