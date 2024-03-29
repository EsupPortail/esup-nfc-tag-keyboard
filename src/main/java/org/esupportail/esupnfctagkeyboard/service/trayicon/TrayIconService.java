/**
 * Licensed to ESUP-Portail under one or more contributor license
 * agreements. See the NOTICE file distributed with this work for
 * additional information regarding copyright ownership.
 *
 * ESUP-Portail licenses this file to you under the Apache License,
 * Version 2.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at:
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.esupportail.esupnfctagkeyboard.service.trayicon;

import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

import org.apache.log4j.Logger;

import dorkbox.systemTray.MenuItem;
import dorkbox.systemTray.SystemTray;

public class TrayIconService {

	public static String TRAY_NAME = "esupnfctagkeyboard";
	private static ClassLoader classLoader = ClassLoader.getSystemClassLoader();
	private final static Logger log = Logger.getLogger(TrayIconService.class);
	private static SystemTray tray;

	private List<MenuItem> menuDeviceList = new ArrayList<MenuItem>();
	private MenuItem exitItem = new MenuItem(quitCommand, new ActionListener() {
		public void actionPerformed(final ActionEvent e) {
			exit();
		}
	}); 

	private static String quitCommand = "Quit";
	private static String serviceName;

	public List<String> numeroIds = new ArrayList<String>();
	public boolean forceCsn = false;

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String service) {
		serviceName = service;
	}

	public TrayIconService(SystemTray tray) throws IOException {
		this.tray = tray;
	}

	public void refreshTrayIcon(String imageUrl, String msg) throws IOException {
		displayMessage("INFO", msg);
		InputStream is = classLoader.getResourceAsStream(imageUrl);
		Image icon = ImageIO.read(is);
		tray.setImage(icon);
	}

	public void setTrayIcon(String imageUrl, String msg) throws IOException {
		displayMessage("INFO", msg);
		InputStream is = classLoader.getResourceAsStream(imageUrl);
		Image icon = ImageIO.read(is);
		tray.setImage(icon);
		for(final String numeroId : numeroIds){
			menuDeviceList.add(new MenuItem(numeroId.trim(), new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					serviceName = numeroId.trim();
					try {
						changeIconOK();
					} catch (IOException e1) {
						log.error("error change tray");
					}
					log.info("Switch to : " + serviceName);
					displayMessage("INFO", "L'application est connectée sur : " + serviceName);
				}

			}));
		}
		if(forceCsn){
			menuDeviceList.add(new MenuItem("forceCsn", new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					serviceName = "forceCsn";
					try {
						changeIconOK();
					} catch (IOException e1) {
						log.error("error change tray");
					}
					log.info("Switch to : " + serviceName);
					displayMessage("INFO", "L'application est connectée sur : " + serviceName);
				}

			}));
			menuDeviceList.add(new MenuItem("forceReverseCsn", new ActionListener() {
				public void actionPerformed(final ActionEvent e) {
					serviceName = "forceReverseCsn";
					try {
						changeIconOK();
					} catch (IOException e1) {
						log.error("error change tray");
					}
					log.info("Switch to : " + serviceName);
					displayMessage("INFO", "L'application est connectée sur : " + serviceName);
				}

			}));
		}
		for (MenuItem menuItem : menuDeviceList){
			tray.getMenu().add(menuItem);
		}
		tray.getMenu().add(exitItem).setShortcut('q'); // case does not matter
	}

	public void changeIconKO(String msg) throws IOException {
		refreshTrayIcon("images/iconko.png", msg);
	}

	public void changeIconOK() throws IOException {
		refreshTrayIcon("images/icon.png", "L'application est connectée sur : " + serviceName);
	}

	public void displayMessage(String title, String msg){
		tray.setStatus(msg);
		tray.setTooltip(msg);
	}
	
	public void exit() {
		tray.shutdown();
		System.exit(0);
	}
}
