package org.esupportail.esupnfctagkeyboard.service.trayicon;

import java.awt.AWTException;
import java.awt.Image;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;

public class TrayIconService {
	
	private static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	
	private static SystemTray tray;
	private static TrayIcon trayIcon;
	
	private static String quitCommand = "Exit";
	private static String serviceName;
	
    private static ActionListener listener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
        	if(quitCommand.equals(e.getActionCommand())){
        		System.exit(0);
        	}
        }
    };
    
	public TrayIconService(String service) throws IOException, AWTException {
		serviceName = service;
		tray = SystemTray.getSystemTray();
		changeIconOK();
	}
	
	public void setTrayIcon(String imageUrl, String msg) throws IOException{
		InputStream is = classLoader.getResourceAsStream(imageUrl);
		Image icon = ImageIO.read(is);
		PopupMenu popup = new PopupMenu();
	    MenuItem msgItem = new MenuItem(msg);
	    popup.add(msgItem);
		MenuItem exitItem = new MenuItem(quitCommand);
	    exitItem.addActionListener(listener);
	    popup.add(exitItem);
	    int iconWidth = new TrayIcon(icon).getSize().width;
	    trayIcon = new TrayIcon(icon.getScaledInstance(iconWidth, -1, Image.SCALE_SMOOTH), msg, popup);
	    trayIcon.addActionListener(listener);
	}
	
	public void changeIconKO(String msg) throws IOException, AWTException {
		tray.remove(trayIcon);
		setTrayIcon("images/iconko.png", msg);
		tray.add(trayIcon);
	}
	
	public void changeIconOK() throws IOException, AWTException {
		tray.remove(trayIcon);
		setTrayIcon("images/icon.png", "EsupSGCKeybEmu " + serviceName);
		tray.add(trayIcon);
	}

	public void displayMessage(String title, String msg, MessageType msgType ){
		trayIcon.displayMessage(title+" : EsupSGCKeybEmu", msg, msgType);
	}
}
