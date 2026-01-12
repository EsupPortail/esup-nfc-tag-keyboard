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
package org.esupportail.esupnfctagkeyboard.service.pcsc;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.smartcardio.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@SuppressWarnings("restriction")
public class PcscUsbService {

	private final static Logger log = LoggerFactory.getLogger(PcscUsbService.class);
	
	private Card card;
	private CardTerminal cardTerminal;
	private TerminalFactory context;
	public CardTerminals terminals;
	
	public PcscUsbService() {
		try {
			context = TerminalFactory.getInstance("PC/SC", null);
			terminals = context.terminals();
		} catch (Exception e) {
			log.error("Exception retrieving context", e);
		}
	}
	
	public boolean isCardOnTerminal() throws CardException{
		return cardTerminal.waitForCardAbsent(2);
	}
	
	public String connection() throws CardException{
		if(terminals.list().size()>0) {
			for (CardTerminal terminal : terminals.list()) {
				if(!terminal.getName().contains("6121") && !terminal.getName().contains("SAM Reader") && terminal.isCardPresent()){
					cardTerminal = terminal;
					try{
						card = cardTerminal.connect("*");
						return cardTerminal.getName();
					}catch(Exception e){
						log.error("pcsc connection error", e);
					}
				}
			}
		}
		throw new CardException("No NFC reader found with card on it - NFC reader found : " + getNamesOfTerminals(terminals));
	}

	private String getNamesOfTerminals(CardTerminals terminals) throws CardException {
		List<String> terminalsNames = new ArrayList<String>();  
		for (CardTerminal terminal : terminals.list()) {
			terminalsNames.add(terminal.getName());
		}
		return terminalsNames.toString();
	}

	public boolean isCardPresent() throws CardException{
		try {
			if(terminals.list().size() == 0) {
				throw new CardException("Pas de lecteur NFC");
			}
			for (CardTerminal terminal : terminals.list()) {
			try {
				if(!terminal.getName().contains("6121") && !terminal.getName().contains("SAM Reader") && terminal.isCardPresent()) return true; 
			} catch (CardException e) {
				log.info("Pas de carte");
			}
			}
		}catch (Exception e){
			throw new CardException(e);
		}
		return false;
	}
	
	public String sendAPDU(String apdu) throws CardException{
		ResponseAPDU answer = null;
		answer = card.getBasicChannel().transmit(new CommandAPDU(hexStringToByteArray(apdu)));
		return byteArrayToHexString(answer.getBytes());

	}

	public String getCardId() throws CardException{
		ResponseAPDU answer;
		answer = card.getBasicChannel().transmit(new CommandAPDU(hexStringToByteArray("FFCA000000")));
		byte[] uid = Arrays.copyOfRange(answer.getBytes(), 0, answer.getBytes().length-2);
		return byteArrayToHexString(uid);
	
	}
	
	public void disconnect() throws PcscException{
		try {
			card.disconnect(false);
		} catch (CardException e) {
			throw new PcscException("pcsc disconnect error : ", e);
		}
	}

	public byte[] hexStringToByteArray(String s) {
		s = s.replace(" ", "");
		int len = s.length();
		byte[] data = new byte[len / 2];
		for (int i = 0; i < len; i += 2) {
			data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4)
					+ Character.digit(s.charAt(i+1), 16));
		}
		return data;
	}

	final protected static char[] hexArray = {'0','1','2','3','4','5','6','7','8','9','A','B','C','D','E','F'};

	public String byteArrayToHexString(byte[] bytes) {
		char[] hexChars = new char[bytes.length*2];
		int v;

		for(int j=0; j < bytes.length; j++) {
			v = bytes[j] & 0xFF;
			hexChars[j*2] = hexArray[v>>>4];
			hexChars[j*2 + 1] = hexArray[v & 0x0F];
		}

		return new String(hexChars);
	}
	
}
