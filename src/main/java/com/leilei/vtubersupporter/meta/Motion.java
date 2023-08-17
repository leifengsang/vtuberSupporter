package com.leilei.vtubersupporter.meta;

/**
 * @author leifengsang
 */
public class Motion {

    /**
     * 动作
     */
    public static final int MOTION_INIT = -1; //初始化
    public static final int MOTION_INIT_ALL = -2; //初始化（包含可同时存在的动作）

    public static final int MOTION_CRY = 4; //哭
    public static final int MOTION_BLACK_FACE = 5; //黑脸
    public static final int MOTION_HAT = 6; //帽子
    public static final int MOTION_PIG = 7; //猪

    /**
     * id
     */
    private int id;

    /**
     * 展示名称
     * 用于系统托盘
     */
    private String showName;

    /**
     * 实际名称
     * 用于api调用
     */
    private String name;

    /**
     * 是否需要调用取消动作
     */
    private boolean cancellable;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getShowName() {
        return showName;
    }

    public void setShowName(String showName) {
        this.showName = showName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isCancellable() {
        return cancellable;
    }

    public void setCancellable(boolean cancellable) {
        this.cancellable = cancellable;
    }
}
