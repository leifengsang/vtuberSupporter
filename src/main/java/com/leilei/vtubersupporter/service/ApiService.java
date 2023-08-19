package com.leilei.vtubersupporter.service;

import com.alibaba.fastjson.JSONObject;
import com.leilei.vtubersupporter.component.MagicTrayIcon;
import com.leilei.vtubersupporter.meta.Expression;
import com.leilei.vtubersupporter.meta.Model;
import com.leilei.vtubersupporter.meta.Motion;
import jakarta.annotation.PostConstruct;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.time.DateUtils;
import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URI;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author leifengsang
 */
@Component
@Log4j2
@EnableScheduling
public class ApiService {

    /**
     * 数据集合
     */
    @Autowired
    private Model model;

    /**
     * live2DViewerEx ws客户端
     */
    private WebSocketClient wsClient;

    /**
     * 系统托盘
     */
    private MagicTrayIcon trayIcon;

    public ApiService() {
    }

    @PostConstruct
    public void init() throws AWTException {
        trayIcon = new MagicTrayIcon();

        //加载所有表情
        List<Expression> expList = model.getExpList();
        for (Expression exp : expList) {
            trayIcon.addMenuItem(exp.getName(), new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    changeExp(exp.getId());
                }
            });
        }

        trayIcon.addMenuItem("motion:cancel", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                cancelAllMotion();
            }
        });

        //加载所有动作
        Map<Integer, Motion> motionDic = model.getMotionDic();
        for (Motion motion : motionDic.values()) {
            if (motion.getId() < 0) {
                //取消选项不显示
                continue;
            }
            trayIcon.addMenuItem(motion.getShowName(), new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    changeMotion(motion.getId());
                }
            });
        }

        //退出
        trayIcon.addMenuItem("exit", new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                close();
            }
        });


        try {
            wsClient = new WebSocketClient(new URI(model.getLive2DViewExApiPath())) {
                @Override
                public void onOpen(ServerHandshake serverHandshake) {
                    initModel();
                }

                @Override
                public void onMessage(String s) {

                }

                @Override
                public void onClose(int i, String s, boolean b) {

                }

                @Override
                public void onError(Exception e) {

                }

                @Override
                public void send(String text) {
                    super.send(text);

                    log.info("向live2DViewExApi发送数据：" + text);
                }
            };
            wsClient.connect();
        } catch (Exception e) {
            log.warn("连接wsClient失败");
            e.printStackTrace();
        }
    }

    /**
     * 初始化
     */
    private void initModel() {
        if (model.getModelId() == Model.MODEL_ISLAND) {
            //不带帽子好看一点
            doChangeMotion(Motion.MOTION_HAT * -1, false);
            checkWeather();
        }
    }

    /**
     * 切换表情
     *
     * @param exp
     */
    public void changeExp(int exp) {
        //表情一样，没必要切
        if (model.getCurrentExp() == exp) {
            return;
        }

        log.info("切换表情，id：" + exp);

        model.setCurrentExp(exp);
        JSONObject json = new JSONObject();
        json.put("msg", 13300);
        json.put("msgId", 1);
        JSONObject data = new JSONObject();
        data.put("id", model.getModelId());
        data.put("expId", exp);
        json.put("data", data);
        wsClient.send(json.toJSONString());
    }

    /**
     * 切换动作
     *
     * @param motion
     */
    public void changeMotion(int motion) {
        //取消上一个动作
        cancelMotion();
        addMotion2Queue(motion);
    }

    private void cancelMotion() {
        addMotion2Queue(Motion.MOTION_INIT);
    }

    private void cancelAllMotion() {
        addMotion2Queue(Motion.MOTION_INIT_ALL);
    }

    private void addMotion2Queue(int motion) {
        log.info("往动作队列加入动作：" + motion);

        int[] queue = model.getMotionQueue();
        queue = ArrayUtils.add(queue, motion);
        model.setMotionQueue(queue);

        checkMotionQueue();
    }

    public void doChangeMotion(int motionId) {
        doChangeMotion(motionId, true);
    }

    /**
     * 发送切换动作的指令
     *
     * @param motionId
     * @param logged   记录当前动作 初始化的时候可能需要默认执行一些动作，不被后续动作还原
     */
    public void doChangeMotion(int motionId, boolean logged) {
        Motion motion = model.getMotionDic().get(motionId);
        if (motion == null) {
            return;
        }

        if (logged) {
            if (motionId > 0) {
                model.addMotionFlag((int) Math.pow(2, motionId));
            } else {
                model.removeMotionFlag((int) Math.pow(2, -motionId));
            }
        }
        model.setLastMotionChangeTime(new Date().getTime());

        log.info("切换动作，id：" + motion.getId() + "，名称：" + motion.getShowName());
        JSONObject json = new JSONObject();
        json.put("msg", 13200);
        json.put("msgId", 1);
        JSONObject data = new JSONObject();
        data.put("id", model.getModelId());
        data.put("type", 0);
        data.put("mtn", motion.getName());
        json.put("data", data);
        wsClient.send(json.toJSONString());
    }

    public void close() {
        System.exit(0);
    }

    /**
     * 检查天气，每小时0分检查一次
     */
    @Scheduled(cron = "0 0 0/1 * * *")
    private void checkWeather() {
        //只有无人岛模型才检查天气
        if (model.getModelId() != Model.MODEL_ISLAND) {
            return;
        }

        Calendar zeroTime = Calendar.getInstance();
        zeroTime.set(Calendar.HOUR_OF_DAY, 0);
        zeroTime.set(Calendar.MINUTE, 0);
        zeroTime.set(Calendar.SECOND, 0);
        zeroTime.set(Calendar.MILLISECOND, 0);

        long time = new Date().getTime() - zeroTime.getTime().getTime();
        double hour = time * 1d / DateUtils.MILLIS_PER_HOUR;

        int exp;
        if (hour >= 7d && hour <= 18d) {
            exp = Expression.EXP_MORNING;
        } else {
            exp = Expression.EXP_EVENING;
        }

        changeExp(exp);
    }

    /**
     * 检查动作队列，每秒检查一次
     */
    @Scheduled(cron = "0/1 * * * * *")
    private void checkMotionQueue() {
        int[] queue = model.getMotionQueue();
        //队列中没有动作
        if (ArrayUtils.isEmpty(queue)) {
            return;
        }

        long lastMotionChangeTime = model.getLastMotionChangeTime();
        long now = new Date().getTime();
        //动作切换需要时间，不能重复切换
        if (now - lastMotionChangeTime < model.getMotionChangeWaitingTime()) {
            return;
        }
        int motionId = queue[0];
        log.info("动作队列当前执行动作：" + motionId);

        queue = ArrayUtils.remove(queue, 0);
        //初始化
        if (motionId == Motion.MOTION_INIT || motionId == Motion.MOTION_INIT_ALL) {
            boolean clearAll = motionId == Motion.MOTION_INIT_ALL;
            int clearLater = 0;
            for (int i = 1; i <= model.getMotionCount(); i++) {
                //当前没有这个动作
                if (!model.hasMotionFlag((int) Math.pow(2, i))) {
                    continue;
                }
                Motion motion = model.getMotionDic().get(i);
                //需要取消
                if (clearAll) {
                    if (motion.isCancellable()) {
                        //最后清
                        clearLater = i;
                    } else {
                        doChangeMotion(-i);
                    }
                } else if (motion.isCancellable()) {
                    doChangeMotion(-i);
                }
            }

            if (clearLater > 0) {
                doChangeMotion(-clearLater);
            }
        } else {
            doChangeMotion(motionId);
        }
        model.setMotionQueue(queue);
    }

    public void onDead() {
        logActAction("onDead");

        switch (model.getModelId()) {
            case Model.MODEL_ISLAND:
                changeMotion(Motion.MOTION_CRY);
                break;
            default:
                break;
        }
    }

    public void onWeakness() {
        logActAction("onWeakness");

        switch (model.getModelId()) {
            case Model.MODEL_ISLAND:
                changeMotion(Motion.MOTION_PIG);
                break;
            default:
                break;
        }
    }

    public void onDamageDown() {
        logActAction("onDamageDown");

        switch (model.getModelId()) {
            case Model.MODEL_ISLAND:
                changeMotion(Motion.MOTION_BLACK_FACE);
                break;
            default:
                break;
        }
    }

    public void onDeadExpired() {
        logActAction("onDeadExpired");

        switch (model.getModelId()) {
            case Model.MODEL_ISLAND:
                changeMotion(Motion.MOTION_CRY * -1);
                break;
            default:
                break;
        }
    }

    public void onDamageDownExpired() {
        logActAction("onDamageDownExpired");

        switch (model.getModelId()) {
            case Model.MODEL_ISLAND:
                changeMotion(Motion.MOTION_BLACK_FACE * -1);
                break;
            default:
                break;
        }
    }

    public void onWeaknessExpired() {
        logActAction("onWeaknessExpired");

        switch (model.getModelId()) {
            case Model.MODEL_ISLAND:
                changeMotion(Motion.MOTION_PIG * -1);
                break;
            default:
                break;
        }
    }

    public void onReset() {
        logActAction("onReset");

        switch (model.getModelId()) {
            case Model.MODEL_ISLAND:
                cancelAllMotion();
                break;
            default:
                break;
        }
    }

    private void logActAction(String action) {
        log.info("收到来自act的调用：" + action);
    }
}
