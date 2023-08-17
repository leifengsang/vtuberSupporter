package com.leilei.vtubersupporter.component;

import java.awt.*;
import java.awt.event.ActionListener;

/**
 * 系统托盘
 *
 * @author leifengsang
 */
public class MagicTrayIcon {

    private TrayIcon trayIcon;

    public MagicTrayIcon() throws AWTException {
        System.setProperty("java.awt.headless", "false");
        SystemTray systemTray = SystemTray.getSystemTray();
        Image image = Toolkit.getDefaultToolkit().createImage("res/icon.png");
        trayIcon = new TrayIcon(image, "vtuberSupporter");
        trayIcon.setImageAutoSize(true);
        PopupMenu popupMenu = new PopupMenu();
        trayIcon.setPopupMenu(popupMenu);
        systemTray.add(trayIcon);
    }

    public void addMenuItem(String label, ActionListener actionListener) {
        MenuItem menuItem = new MenuItem(label);
        menuItem.addActionListener(actionListener);
        trayIcon.getPopupMenu().add(menuItem);
    }

    /**
     * 显示一条托盘信息
     *
     * @param content
     */
    public void displayMassage(String content) {
        trayIcon.displayMessage("processMonitor", content, TrayIcon.MessageType.INFO);
    }
}
