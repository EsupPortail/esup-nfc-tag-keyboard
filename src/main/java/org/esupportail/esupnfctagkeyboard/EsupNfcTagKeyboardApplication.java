package org.esupportail.esupnfctagkeyboard;

import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.io.InputStream;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.esupportail.esupnfctagkeyboard.domain.NfcResultBean;
import org.esupportail.esupnfctagkeyboard.domain.NfcResultBean.CODE;
import org.esupportail.esupnfctagkeyboard.service.EncodingException;
import org.esupportail.esupnfctagkeyboard.service.EncodingService;
import org.esupportail.esupnfctagkeyboard.service.TypeException;
import org.esupportail.esupnfctagkeyboard.service.TypeService;
import org.esupportail.esupnfctagkeyboard.service.pcsc.PcscException;
import org.esupportail.esupnfctagkeyboard.service.trayicon.TrayIconService;
import org.esupportail.esupnfctagkeyboard.utils.Utils;
import org.springframework.web.client.RestTemplate;

public class EsupNfcTagKeyboardApplication {

	private final static Logger log = Logger.getLogger(EsupNfcTagKeyboardApplication.class);

	private static RestTemplate restTemplate =  new RestTemplate(Utils.clientHttpRequestFactory());
	
	private static long TIME_BETWEEN_SAME_CARD = 3000; 
	
	private static TrayIconService trayIconService; 
	private static EncodingService encodingService;
	private static TypeService typeService = new TypeService();
	private static 	boolean success = true;
	
	
	public static void main(String... args) throws Exception {
		
		Properties defaultProperties = new Properties();
		InputStream in = EsupNfcTagKeyboardApplication.class.getResourceAsStream("/esupnfctagkeyboard.properties");
		try {
			defaultProperties.load(in);
		} catch (Exception e) {
			throw new TypeException("sgcUrl not found");
		}
		
		String esupNfcTagServerUrl = System.getProperty("esupNfcTagKeyboard.url", defaultProperties.getProperty("url"));
		encodingService = new EncodingService(esupNfcTagServerUrl);
		
		encodingService.numeroId =  System.getProperty("esupNfcTagKeyboard.numeroId", defaultProperties.getProperty("numeroId"));
		
		String urlAuthType = esupNfcTagServerUrl + "/nfc-ws/deviceAuthConfig/?numeroId=" + encodingService.numeroId;

		try{
			encodingService.authType = restTemplate.getForObject(urlAuthType, String.class);
		}catch (Exception e) {
			throw new EncodingException("rest call error for : " + urlAuthType + " - " + e);
		}
		
		encodingService.authType = System.getProperty("esupNfcTagKeyboard.authType", defaultProperties.getProperty("authType"));

		
		log.info("Startup OK");
		
		try {
			if (SystemTray.isSupported()) {
				trayIconService = new TrayIconService(encodingService.numeroId);
			}
		} catch(Exception e) {
			log.warn("SystemTray error", e);
		}	
		
		while(true) {
			try {
				notifySuccess();
				run();
			} catch(Exception e) {
				notifyError(e);
			}
		}
	}

	protected static void run() throws Exception {
		
		String lastCsn = null;
		long time = System.currentTimeMillis();
		while(true) {
			try {
				while(!encodingService.isCardPresent()){
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
						log.debug("desfire error", e);
					}
				}
				if(nfcResultBean != null && (CODE.END.equals(nfcResultBean.getCode()) || CODE.OK.equals(nfcResultBean.getCode()))){
					display = encodingService.getDisplay(nfcResultBean.getTaglogId());
					
				}
				System.err.println(display);
				if(csn != null && !csn.equals("") && (!csn.equals(lastCsn) || System.currentTimeMillis()-time > TIME_BETWEEN_SAME_CARD)) {
					time = System.currentTimeMillis();
					lastCsn = csn;
					if(display != null && !display.equals("")) {
						log.info("emulate display : " + display);
						typeService.type(display);
						typeService.typeEnter();
					}else {
						log.info("Carte inconnue");
						typeService.writeMinus();
					}
				}
				encodingService.pcscDisconnect();
				while(encodingService.isCardPresent()){
					Utils.sleep(1000);
				}
			} catch (PcscException e) {
				log.debug("Pas de connexion PCSC");
			}
			
		}
	}
	
	private static void notifyError(Exception e) {
		if(success) {
			success = false;
			log.error(e.getMessage(), e);
			try {
				trayIconService.changeIconKO("ERROR  : " + e.getMessage());
				trayIconService.displayMessage("ERROR", e.getMessage(), TrayIcon.MessageType.ERROR);			
			} catch (Exception e1) {
				log.error(e1.getMessage(), e1);
			}
		}
		Utils.sleep(1000);
	}

	private static void notifySuccess() {
		if(!success) {
			success = true;
			log.info("Application is now OK");
			try {
				trayIconService.changeIconOK();
				trayIconService.displayMessage("INFO", "L'application est de nouveau accessible", TrayIcon.MessageType.INFO);
			 } catch (Exception e1) {
			    	log.warn(e1.getMessage(), e1);
			 }	
		}
	}
}
