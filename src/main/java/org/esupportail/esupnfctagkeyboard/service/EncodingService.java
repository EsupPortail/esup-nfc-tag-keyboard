package org.esupportail.esupnfctagkeyboard.service;

import java.io.IOException;

import javax.smartcardio.CardException;

import org.apache.log4j.Logger;
import org.esupportail.esupnfctagkeyboard.domain.CsnMessageBean;
import org.esupportail.esupnfctagkeyboard.domain.NfcResultBean;
import org.esupportail.esupnfctagkeyboard.service.pcsc.PcscException;
import org.esupportail.esupnfctagkeyboard.service.pcsc.PcscUsbService;
import org.esupportail.esupnfctagkeyboard.utils.Utils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("restriction")
public class EncodingService {

	private final static Logger log = Logger.getLogger(EncodingService.class);
	
	private String esupNfcTagServerUrl;
	public String numeroId;
	public String authType;
	
	private RestTemplate restTemplate =  new RestTemplate(Utils.clientHttpRequestFactory());
	
	private PcscUsbService pcscUsbService = new PcscUsbService();
	
	public EncodingService(String esupNfcTagServerUrl){
		this.esupNfcTagServerUrl = esupNfcTagServerUrl;
	}
	
	public boolean pcscConnection() throws PcscException{
		boolean isConn = false;
		try {
			String cardTerminalName = pcscUsbService.connection();
			log.debug("cardTerminal : " + cardTerminalName);
			isConn = true;
		} catch (CardException e) {
			throw new PcscException("pcsc connection error", e);
		}
		return isConn;
	}
	
	public String readCsn() throws PcscException{
		try {
			String csn = pcscUsbService.byteArrayToHexString(pcscUsbService.hexStringToByteArray(pcscUsbService.getCardId()));
			return csn;
		} catch (CardException e) {
			log.error("csn read error" + e);
			throw new PcscException("csn read error", e);
		}
	}
	
	public NfcResultBean desfireNfcComm(String cardId) throws EncodingException, PcscException {
		String urlTest = esupNfcTagServerUrl + "/desfire-ws/?result=&numeroId="+numeroId+"&cardId="+cardId;
		NfcResultBean nfcResultBean;
		try{
			nfcResultBean = restTemplate.getForObject(urlTest, NfcResultBean.class);
		}catch (Exception e) {
			throw new EncodingException("rest call error for : " + urlTest + " - " + e);
		}
		log.info("Rest call : " + urlTest);
		log.info("Result of rest call :" + nfcResultBean);
		if(nfcResultBean.getFullApdu()!=null) {
			log.info("Encoding : Start");
			String result = "";
			while(true){
				log.info("RAPDU : "+ result);
				String url = esupNfcTagServerUrl + "/desfire-ws/?result="+ result +"&numeroId="+numeroId+"&cardId="+cardId;
				nfcResultBean = restTemplate.getForObject(url, NfcResultBean.class);
				log.info("SAPDU : "+ nfcResultBean.getFullApdu());
				if(nfcResultBean.getFullApdu()!=null){
				if(!"END".equals(nfcResultBean.getFullApdu()) ) {
					try {
						result = pcscUsbService.sendAPDU(nfcResultBean.getFullApdu());
					} catch (CardException e) {
						throw new PcscException("pcsc send apdu error", e);
					}
				} else {
					log.info("Encoding  : OK");
					return nfcResultBean;
				}
				}else{
					throw new EncodingException("return is null");
				}
			}
		} else {
			return nfcResultBean;
		}
	}
	
	public NfcResultBean csnNfcComm(String cardId) throws EncodingException, PcscException {
		CsnMessageBean nfcMsg = new CsnMessageBean();
	    nfcMsg.setNumeroId(numeroId);
	    nfcMsg.setCsn(cardId);
	    ObjectMapper mapper = new ObjectMapper();
	    String jsonInString = null;
		String url = esupNfcTagServerUrl + "/csn-ws";
		String nfcComm;
		try{
			log.info("try tagging on nfc-tag : " + cardId);
			jsonInString = mapper.writeValueAsString(nfcMsg);
			HttpHeaders headers = new HttpHeaders();
			headers.setContentType(MediaType.APPLICATION_JSON);

			HttpEntity<String> entity = new HttpEntity<String>(jsonInString, headers);
			
			
			nfcComm = restTemplate.postForObject(url, entity, String.class);
		}catch (Exception e) {
		    throw new EncodingException("rest call error for : " + url, e);
		}
		NfcResultBean nfcresulbean = null;
		try {
			nfcresulbean = mapper.readValue(nfcComm, NfcResultBean.class);
		} catch (Exception e) {
			log.error("parse error" , e);
		} 
		return nfcresulbean;
	}
	
	public String getDisplay(long tagLogId){
		String display = null;
		String urlTest = esupNfcTagServerUrl + "/nfc-ws/display?id="+tagLogId;
		try{
			display = restTemplate.getForObject(urlTest, String.class);
		}catch (Exception e) {
			log.error("error get display", e);
		}
		return display;
	}

	public boolean pcscCardOnTerminal() {
		try {
			return pcscUsbService.isCardOnTerminal();
		} catch (CardException e) {
			return false;
		}
	}
	
	public boolean isCardPresent() throws CardException{
		return pcscUsbService.isCardPresent();
	}
	
	public void pcscDisconnect() throws PcscException{
		try {
			pcscUsbService.disconnect();
		} catch (PcscException e) {
			throw new PcscException(e.getMessage(), e);
		}
		Utils.sleep(1000);
	}
	
	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};
	
	public static String swapPairs(byte[] tagId) {
		String s = new StringBuilder(byteArrayToHexString(tagId)).reverse().toString();
		String even = "";
		String odd = "";
		int length = s.length();

		for (int i = 0; i <= length-2; i+=2) {
			even += s.charAt(i+1) + "" + s.charAt(i);
		}

		if (length % 2 != 0) {
			odd = even + s.charAt(length-1);
			return odd;
		} else {
			return even;
		}
	}
	
	public static String byteArrayToHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length*2];
		int v;

		for(int j=0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j*2] = hexArray[v>>>4];
			hexChars[j*2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}
	
	public static byte[] hexStringToByteArray(String s) {
		s = s.replace(" ", "");
	    int len = s.length();
	    byte[] data = new byte[len / 2];
	    for (int i = 0; i < len; i += 2) {
	        data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
	                             + Character.digit(s.charAt(i+1), 16));
	    }
	    return data;
	}
}
