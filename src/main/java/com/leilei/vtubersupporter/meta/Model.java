package com.leilei.vtubersupporter.meta;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.time.DateUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author leifengsang
 */
@ConfigurationProperties(prefix = "model")
@Component
@Log4j2
public class Model implements InitializingBean {

    public static final long MOTION_CHANGE_TIME_INTERVAL = DateUtils.MILLIS_PER_SECOND * 2;

    /**
     * 模型
     */
    public static final int MODEL_ISLAND = 2; //无人岛

    /**
     * live2DViewEx api地址
     */
    private String live2DViewExApiPath;

    /**
     * 当前使用的模型Id
     */
    private int modelId;

    /**
     * 表情配置json
     */
    private String expDicJson;

    /**
     * 动作配置json
     */
    private String motionDicJson;

    /**
     * 表情list
     */
    private List<Expression> expList;

    /**
     * 动作map
     */
    private Map<Integer, Motion> motionDic;

    /**
     * 当前表情
     */
    private int currentExp = -1;

    /**
     * 动作flag
     */
    private int motionFlag = 0;

    /**
     * 动作列表 -1表示初始化
     */
    private int[] motionQueue;

    /**
     * 上次切换的动作时间
     */
    private long lastMotionChangeTime;

    /**
     * 动作数量，用于处理flag
     */
    private int motionCount = 0;

    public String getLive2DViewExApiPath() {
        return live2DViewExApiPath;
    }

    public void setLive2DViewExApiPath(String live2DViewExApiPath) {
        this.live2DViewExApiPath = live2DViewExApiPath;
    }

    public int getModelId() {
        return modelId;
    }

    public void setModelId(int modelId) {
        this.modelId = modelId;
    }

    public String getExpDicJson() {
        return expDicJson;
    }

    public void setExpDicJson(String expDicJson) {
        this.expDicJson = expDicJson;
    }

    public String getMotionDicJson() {
        return motionDicJson;
    }

    public void setMotionDicJson(String motionDicJson) {
        this.motionDicJson = motionDicJson;
    }

    public List<Expression> getExpList() {
        return expList;
    }

    public void setExpList(List<Expression> expList) {
        this.expList = expList;
    }

    public Map<Integer, Motion> getMotionDic() {
        return motionDic;
    }

    public void setMotionDic(Map<Integer, Motion> motionDic) {
        this.motionDic = motionDic;
    }

    public int getCurrentExp() {
        return currentExp;
    }

    public void setCurrentExp(int currentExp) {
        this.currentExp = currentExp;
    }

    public int getMotionFlag() {
        return motionFlag;
    }

    public void setMotionFlag(int motionFlag) {
        this.motionFlag = motionFlag;
    }

    public int[] getMotionQueue() {
        return motionQueue;
    }

    public void setMotionQueue(int[] motionQueue) {
        this.motionQueue = motionQueue;
    }

    public long getLastMotionChangeTime() {
        return lastMotionChangeTime;
    }

    public void setLastMotionChangeTime(long lastMotionChangeTime) {
        this.lastMotionChangeTime = lastMotionChangeTime;
    }

    public int getMotionCount() {
        return motionCount;
    }

    public void setMotionCount(int motionCount) {
        this.motionCount = motionCount;
    }

    @Override
    public String toString() {
        return "Model{" +
                "live2DViewExApiPath='" + live2DViewExApiPath + '\'' +
                ", modelId=" + modelId +
                ", expDicJson='" + expDicJson + '\'' +
                ", motionDicJson='" + motionDicJson + '\'' +
                '}';
    }

    /**
     * 解析表情json
     */
    private void initExpList() {
        expList = new ArrayList<>();
        JSONArray jsonArray = JSONObject.parseObject(expDicJson).getJSONArray(modelId + "");
        if (jsonArray == null) {
            return;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            Expression exp = new Expression(object.getIntValue("id"), object.getString("name"));
            expList.add(exp.getId(), exp);
        }
    }

    /**
     * 解析动作json
     */
    private void initMotionDic() {
        motionDic = new HashMap<>();
        JSONArray jsonArray = JSONObject.parseObject(motionDicJson).getJSONArray(modelId + "");
        if (jsonArray == null) {
            return;
        }
        for (int i = 0; i < jsonArray.size(); i++) {
            JSONObject object = jsonArray.getJSONObject(i);
            Motion motion = new Motion();
            int id = object.getIntValue("id");
            String showName = object.getString("showName");
            String name = object.getString("name");
            boolean cancellable = object.getBooleanValue("cancellable");
            motion.setId(id);
            motion.setShowName(showName);
            motion.setName(name);
            motion.setCancellable(cancellable);
            motionDic.put(motion.getId(), motion);

            if (motion.getId() > 0) {
                motionCount++;
            }
        }
    }

    public boolean hasMotionFlag(int flag) {
        return (motionFlag & flag) == flag;
    }

    public void addMotionFlag(int flag) {
        if (!hasMotionFlag(flag)) {
            motionFlag = motionFlag | flag;
        }
    }

    public void removeMotionFlag(int flag) {
        if (hasMotionFlag(flag)) {
            motionFlag = motionFlag ^ flag;
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        initExpList();
        initMotionDic();
    }
}
