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
package org.esupportail.esupnfctagkeyboard;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import javax.smartcardio.CardException;
import javax.swing.SwingUtilities;

import javafx.application.Application;
import javafx.stage.Stage;
import org.apache.log4j.Logger;
import org.esupportail.esupnfctagkeyboard.domain.NfcResultBean;
import org.esupportail.esupnfctagkeyboard.domain.NfcResultBean.CODE;
import org.esupportail.esupnfctagkeyboard.service.EncodingException;
import org.esupportail.esupnfctagkeyboard.service.EncodingService;
import org.esupportail.esupnfctagkeyboard.service.TypeException;
import org.esupportail.esupnfctagkeyboard.service.TypeService;
import org.esupportail.esupnfctagkeyboard.service.pcsc.PcscException;
import org.esupportail.esupnfctagkeyboard.service.trayicon.TrayIconService;
import org.esupportail.esupnfctagkeyboard.utils.HexStringUtils;
import org.esupportail.esupnfctagkeyboard.utils.Utils;
import org.springframework.web.client.RestTemplate;

import dorkbox.systemTray.SystemTray;

public class EsupNfcTagKeyboardApplication extends Application {

	private final static Logger log = Logger.getLogger(EsupNfcTagKeyboardApplication.class);

	private static RestTemplate restTemplate =  new RestTemplate(Utils.clientHttpRequestFactory());

	private static long timeBetweenSameCard = 3000;
	private static long cardReadSleepTime = 1000;
	private static long onErrorSleepTime = 3000;
	private static long beforeNextCardSleepTime = 1500;

	private static TrayIconService trayIconService; 
	private static EncodingService encodingService;
	private static TypeService typeService = new TypeService();
	private static boolean success = false;

	private static String esupNfcTagServerUrl;
	private static String noResponseMessage = "-";
	private static String numeroIdsString;
	private static boolean emulateKeyboard = true;
	private static boolean lineFeed = true;
	private static boolean redirect = false;
	private static boolean forceCsn = false;
	private static String redirectUrlTemplate;
	private static String prefix = "";
	private static String suffix = "";
	private static String urlAuthType;

	private static int port = 33333;
	private static String title = "esupNfcTagKeyboard";
	private static String message = "Impossible d'ouvrir une deuxième instance de l'application";
	private static Runnable runOnReceive = new Runnable() {
	    public void run() {
	        SwingUtilities.invokeLater(new Runnable() {
	            public void run() {
	            	trayIconService.displayMessage(title, message);
	            }
	        });
	    }                   
	};

	public void start(final Stage primaryStage)  throws Exception {

		Properties defaultProperties = new Properties();
		InputStream in = EsupNfcTagKeyboardApplication.class.getResourceAsStream("/esupnfctagkeyboard.properties");
		try {
			defaultProperties.load(in);
		} catch (Exception e) {
			throw new TypeException("sgcUrl not found");
		}

		String strPort = System.getProperty("esupNfcTagKeyboard.port", defaultProperties.getProperty("port"));
		if(strPort != null){
			port = Integer.valueOf(strPort);
		}
		log.info("port : " + port);
		UniqueInstance uniqueInstance = new UniqueInstance(port, title, runOnReceive);
		
		if(!uniqueInstance.launch()) {
			log.error(message);
			System.exit(0);			
		}

		timeBetweenSameCard = Long.parseLong(System.getProperty("esupNfcTagKeyboard.timeBetweenSameCard", defaultProperties.getProperty("timeBetweenSameCard")));
		cardReadSleepTime = Long.parseLong(System.getProperty("esupNfcTagKeyboard.cardReadSleepTime", defaultProperties.getProperty("cardReadSleepTime")));
		onErrorSleepTime = Long.parseLong(System.getProperty("esupNfcTagKeyboard.onErrorSleepTime", defaultProperties.getProperty("onErrorSleepTime")));
		beforeNextCardSleepTime = Long.parseLong(System.getProperty("esupNfcTagKeyboard.beforeNextCardSleepTime", defaultProperties.getProperty("beforeNextCardSleepTime")));

		esupNfcTagServerUrl = System.getProperty("esupNfcTagKeyboard.esupNfcTagServerUrl", defaultProperties.getProperty("esupNfcTagServerUrl"));
		noResponseMessage = System.getProperty("esupNfcTagKeyboard.noResponseMessage", defaultProperties.getProperty("noResponseMessage"));
		encodingService = new EncodingService(esupNfcTagServerUrl);
		numeroIdsString =  System.getProperty("esupNfcTagKeyboard.numeroId", defaultProperties.getProperty("numeroIds"));
		prefix =  System.getProperty("esupNfcTagKeyboard.prefix", defaultProperties.getProperty("prefix"));
		suffix =  System.getProperty("esupNfcTagKeyboard.suffix", defaultProperties.getProperty("suffix"));

		List<String> numeroIds = Arrays.asList(numeroIdsString.split("\\s*,\\s*"));;
		encodingService.numeroId =  numeroIds.get(0).trim();
		log.info("default numeroId : " + encodingService.numeroId);
		emulateKeyboard =  Boolean.parseBoolean(System.getProperty("esupNfcTagKeyboard.emulateKeyboard", defaultProperties.getProperty("emulateKeyboard")));
		log.info("emulateKeyboard : " + emulateKeyboard);
		forceCsn =  Boolean.parseBoolean(System.getProperty("esupNfcTagKeyboard.forceCsn", defaultProperties.getProperty("forceCsn")));
		log.info("forceCsn : " + forceCsn);
		lineFeed =  Boolean.parseBoolean(System.getProperty("esupNfcTagKeyboard.lineFeed", defaultProperties.getProperty("lineFeed")));
		log.info("lineFeed : " + lineFeed);
		redirect =  Boolean.parseBoolean(System.getProperty("esupNfcTagKeyboard.redirect", defaultProperties.getProperty("redirect")));
		redirectUrlTemplate =  System.getProperty("esupNfcTagKeyboard.redirectUrlTemplate", defaultProperties.getProperty("redirectUrlTemplate"));
		log.info("redirect : " + redirect + " to : " + redirectUrlTemplate);
		urlAuthType = esupNfcTagServerUrl + "/nfc-ws/deviceAuthConfig/?numeroId=" + encodingService.numeroId;

		try{
			log.info("try to get auth type : " + urlAuthType);
			encodingService.authType = restTemplate.getForObject(urlAuthType, String.class);
			if(encodingService.authType == null) {
				log.error("unable to get authType, please check configuration");
				throw new EncodingException("unable to get authType");
			}
			log.info("auth type is : " + encodingService.authType);
		}catch (Exception e) {
			log.error("authUrl access issue", e);
			throw new EncodingException("rest call error for : " + urlAuthType + " - " + e);
		}

		SystemTray systemTray = SystemTray.get(TrayIconService.TRAY_NAME);

		if (systemTray != null) {
			trayIconService = new TrayIconService(systemTray);
			trayIconService.numeroIds = numeroIds;
			trayIconService.forceCsn = forceCsn;
			trayIconService.setServiceName(encodingService.numeroId);
			trayIconService.setTrayIcon("images/icon.png", "L'application est connectée sur : " + encodingService.numeroId);
		}

		log.info("Startup OK");
		success = true;

		new Thread(() -> {
			while (true) {
				try {
					run();
				} catch (Exception e) {
					if (success) {
						log.error(e.getMessage());
						notifyError(e);
						Utils.sleep(onErrorSleepTime);
					}
				}
				Utils.sleep(1500);
			}
		}).start();

	}

	protected static void run() throws Exception {

		String lastCsn = null;
		long time = System.currentTimeMillis();
		while(true) {
			try {
				while(!encodingService.isCardPresent()){
					notifySuccess();
					encodingService.numeroId = trayIconService.getServiceName();
					Utils.sleep(cardReadSleepTime);
				}
				encodingService.pcscConnection();
				String csn = encodingService.readCsn();
				if(csn != null && !csn.equals("") && (!csn.equals(lastCsn) || System.currentTimeMillis()-time > timeBetweenSameCard)) {
					if(!encodingService.numeroId.equals("forceCsn") && !encodingService.numeroId.equals("forceReverseCsn")) {
						String display = null;
						NfcResultBean nfcResultBean = null;
						encodingService.authType = restTemplate.getForObject(urlAuthType, String.class);
						if (encodingService.authType.equals("CSN")) {
							nfcResultBean = encodingService.csnNfcComm(csn);
						} else {
							try {
								nfcResultBean = encodingService.desfireNfcComm(csn);						
							} catch (Exception e) {
								log.error("desfire error", e);
							}
						}
						if(nfcResultBean != null && (CODE.END.equals(nfcResultBean.getCode()) || CODE.OK.equals(nfcResultBean.getCode()))){
							display = encodingService.getDisplay(nfcResultBean.getTaglogId());

						}

						time = System.currentTimeMillis();
						lastCsn = csn;
						if(display != null && !display.equals("")) {
							if(emulateKeyboard){
								log.info("emulate display : " + display);
								typeService.type(prefix + display + suffix);
								if(lineFeed) {
									typeService.typeEnter();
								}
							}
							if(redirect){
								String redirectUrl = MessageFormat.format(redirectUrlTemplate, display);
								log.info("try to redirect to : " + redirectUrl);
								Runtime runtime = Runtime.getRuntime();
								try {
									runtime.exec("xdg-open " + redirectUrl);
								} catch (IOException e) {
									log.error("error openning browser");
								}
							}
						} else {
							log.info("Carte inconnue");
							if(emulateKeyboard){
								typeService.type(noResponseMessage);
							}		
						}
					} else {
						if(encodingService.numeroId.equals("forceCsn")) {
							log.info("emulate csn : " + csn);
							typeService.type(csn);
						} else {
							log.info("emulate reverseCsn: " + HexStringUtils.swapPairs(csn));
							typeService.type(HexStringUtils.swapPairs(csn));
						}
						if(lineFeed) {
							typeService.typeEnter();
						}
					}
				} else {
					log.warn("Erreur lecture CSN");
					emulateError();

				}
				encodingService.pcscDisconnect();

				while(encodingService.isCardPresent()) {
					Utils.sleep(cardReadSleepTime);
				}
				
			} catch (CardException e) {
				log.error("Pas de connexion PCSC", e);
				throw new Exception(e);
			} catch (PcscException e) {
				log.error("Erreur PCSC", e);
			} catch (EncodingException e) {
				log.warn("Erreur controle carte");
				emulateError();
			}
		}
	}

	private static void emulateError() {
		if(emulateKeyboard){
			typeService.type(noResponseMessage);
		}
		Utils.sleep(onErrorSleepTime);
	}
	
	private static void notifyError(Exception e) {
		if(success) {
			success = false;
			try {
				trayIconService.changeIconKO("ERROR  : " + e.getMessage());
				System.err.println("FALSE");
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
	}

	private static void notifySuccess() {
		if(!success) {
			success = true;
			try {
				log.info("Application is now OK");
				trayIconService.changeIconOK();
			} catch (Exception e1) {
				log.warn(e1.getMessage(), e1);
			}	
		}
	}
}
