package com.leilei.vtubersupporter.meta;

/**
 * @author leifengsang
 */
public class Expression {

    /**
     * 表情
     */
    public static final int EXP_EVENING = 0; //2-夜晚
    public static final int EXP_MORNING = 1; //2-白天

    private int id;

    private String name;

    public Expression(int id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
