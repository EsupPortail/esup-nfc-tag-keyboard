package org.esupportail.esupnfctagkeyboard;

import java.io.IOException;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.esupportail.esupnfctagkeyboard.domain.NfcResultBean;
import org.esupportail.esupnfctagkeyboard.domain.NfcResultBean.CODE;
import org.esupportail.esupnfctagkeyboard.service.EncodingException;
import org.esupportail.esupnfctagkeyboard.service.EncodingService;
import org.esupportail.esupnfctagkeyboard.service.TypeException;
import org.esupportail.esupnfctagkeyboard.service.TypeService;
import org.esupportail.esupnfctagkeyboard.service.trayicon.TrayIconService;
import org.esupportail.esupnfctagkeyboard.utils.Utils;
import org.springframework.web.client.RestTemplate;

import dorkbox.systemTray.SystemTray;
import dorkbox.util.CacheUtil;

public class EsupNfcTagKeyboardApplication {

	private final static Logger log = Logger.getLogger(EsupNfcTagKeyboardApplication.class);

	private static RestTemplate restTemplate =  new RestTemplate(Utils.clientHttpRequestFactory());
	
	private static long TIME_BETWEEN_SAME_CARD = 3000; 
	
	private static TrayIconService trayIconService; 
	private static EncodingService encodingService;
	private static TypeService typeService = new TypeService();
	private static boolean success = false;
	
	private static String esupNfcTagServerUrl;
	private static String numeroIdsString;
	private static boolean emulateKeyboard = true;
	private static boolean redirect = false;
	private static String redirectUrlTemplate;
	private static String prefix = "";
	private static String suffix = "";
	
	public static void main(String... args) throws Exception {
		
		Properties defaultProperties = new Properties();
		InputStream in = EsupNfcTagKeyboardApplication.class.getResourceAsStream("/esupnfctagkeyboard.properties");
		try {
			defaultProperties.load(in);
		} catch (Exception e) {
			throw new TypeException("sgcUrl not found");
		}
		
		esupNfcTagServerUrl = System.getProperty("esupNfcTagKeyboard.esupNfcTagServerUrl", defaultProperties.getProperty("esupNfcTagServerUrl"));
		encodingService = new EncodingService(esupNfcTagServerUrl);
		numeroIdsString =  System.getProperty("esupNfcTagKeyboard.numeroId", defaultProperties.getProperty("numeroIds"));
		prefix =  System.getProperty("esupNfcTagKeyboard.prefix", defaultProperties.getProperty("prefix"));
		suffix =  System.getProperty("esupNfcTagKeyboard.suffix", defaultProperties.getProperty("suffix"));
		
		
		List<String> numeroIds = Arrays.asList(numeroIdsString.split("\\s*,\\s*"));;
		
		encodingService.numeroId =  numeroIds.get(0).trim();
		
		log.info("default numeroId : " + encodingService.numeroId);
		
		emulateKeyboard =  Boolean.parseBoolean(System.getProperty("esupNfcTagKeyboard.emulateKeyboard", defaultProperties.getProperty("emulateKeyboard")));
		
		log.info("emulateKeyboard : " + emulateKeyboard);
		
		redirect =  Boolean.parseBoolean(System.getProperty("esupNfcTagKeyboard.redirect", defaultProperties.getProperty("redirect")));
		redirectUrlTemplate =  System.getProperty("esupNfcTagKeyboard.redirectUrlTemplate", defaultProperties.getProperty("redirectUrlTemplate"));
		
		log.info("redirect : " + redirect + " to : " + redirectUrlTemplate);
		
		String urlAuthType = esupNfcTagServerUrl + "/nfc-ws/deviceAuthConfig/?numeroId=" + encodingService.numeroId;

		try{
			log.info("try to get auth type : " + urlAuthType);
			encodingService.authType = restTemplate.getForObject(urlAuthType, String.class);
			log.info("auth type is : " + encodingService.authType);
		}catch (Exception e) {
			log.error("authUrl access issue", e);
			throw new EncodingException("rest call error for : " + urlAuthType + " - " + e);
			}
		
        CacheUtil.clear();

	    SystemTray systemTray = SystemTray.get();
	    
		try {
			if (systemTray != null) {
				trayIconService = new TrayIconService();
				trayIconService.numeroIds = numeroIds;
				trayIconService.setServiceName(encodingService.numeroId);
				trayIconService.setTrayIcon("images/icon.png", "L'application est connectée sur : " + encodingService.numeroId);
			}
		} catch(Exception e) {
			log.error("SystemTray error", e);
			throw new Exception("SystemTray not supported", e);
		}	
		
		log.info("Startup OK");
		success = true;
		
		while(true) {
			try {
				run();
			} catch(Exception e) {
				if(success) {
					log.error(e.getMessage(), e);
					notifyError(e);
					Utils.sleep(3000);
				}
			}
			Utils.sleep(1500);
		}
	}

	protected static void run() throws Exception {
		
		String lastCsn = null;
		long time = System.currentTimeMillis();
		while(true) {
			try {
				while(!encodingService.isCardPresent()){
					notifySuccess();
					encodingService.numeroId = trayIconService.getServiceName();
					Utils.sleep(1000);
				}
				encodingService.pcscConnection();
				String csn = encodingService.readCsn();
				String display = null;
				NfcResultBean nfcResultBean = null;
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
				if(csn != null && !csn.equals("") && (!csn.equals(lastCsn) || System.currentTimeMillis()-time > TIME_BETWEEN_SAME_CARD)) {
					time = System.currentTimeMillis();
					lastCsn = csn;
					if(display != null && !display.equals("")) {
						if(emulateKeyboard){
							log.info("emulate display : " + display);
							typeService.type(prefix + display + suffix);
							typeService.typeEnter();
						}
						if(redirect){
							String redirectUrl = MessageFormat.format(redirectUrlTemplate, new String[] {display});
							log.info("try to redirect to : " + redirectUrl);
							Runtime runtime = Runtime.getRuntime();
				            try {
				                runtime.exec("xdg-open " + redirectUrl);
				            } catch (IOException e) {
				            	log.error("error openning browser");
				            }
						}
					}else {
						log.info("Carte inconnue");
						if(emulateKeyboard){
							typeService.writeMinus();
						}
					}
				}
				encodingService.pcscDisconnect();
				while(encodingService.isCardPresent()){
					Utils.sleep(1000);
				}
			} catch (Exception e) {
				log.debug("Pas de connexion PCSC", e);
				throw new EncodingException("Pas de connexion PCSC", e);
			}
			
		}
	}
	
	private static void notifyError(Exception e) {
		if(success) {
			success = false;
			try {
				trayIconService.changeIconKO("ERROR  : " + e.getMessage());
				//trayIconService.displayMessage("ERROR", e.getMessage(), TrayIcon.MessageType.ERROR);			
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		Utils.sleep(1000);
	}

	private static void notifySuccess() {
		if(!success) {
			success = true;
			try {
				log.info("Application is now OK");
				trayIconService.changeIconOK();
				//trayIconService.displayMessage("INFO", "L'application est connectée sur : " + encodingService.numeroId, TrayIcon.MessageType.INFO);
			 } catch (Exception e1) {
			    	log.warn(e1.getMessage(), e1);
			 }	
		}
	}
}
