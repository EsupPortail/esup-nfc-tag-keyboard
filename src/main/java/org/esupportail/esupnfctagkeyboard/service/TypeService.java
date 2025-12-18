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
package org.esupportail.esupnfctagkeyboard.service;

import java.awt.AWTException;
import java.awt.Robot;
import java.awt.event.KeyEvent;

import com.jgoodies.common.base.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TypeService {

	private final static Logger log = LoggerFactory.getLogger(TypeService.class);
	
	private static Robot robot;
	
	
	
	public TypeService() {
		try {
			robot = new Robot();
		} catch (AWTException e) {
			log.error("Exception on TypeService when construct java.awt.Robot", e);
		}
	}

	public void type(CharSequence characters) {
        int length = characters.length();
        for (int i = 0; i < length; i++) {
            char character = characters.charAt(i);
            type(character);
        }
    }
	
	public void typeEnter() {
		doType(KeyEvent.VK_ENTER);
	}

    public void type(char character) {
        switch (character) {
        case 'a': doType(KeyEvent.VK_A); break;
        case 'b': doType(KeyEvent.VK_B); break;
        case 'c': doType(KeyEvent.VK_C); break;
        case 'd': doType(KeyEvent.VK_D); break;
        case 'e': doType(KeyEvent.VK_E); break;
        case 'f': doType(KeyEvent.VK_F); break;
        case 'g': doType(KeyEvent.VK_G); break;
        case 'h': doType(KeyEvent.VK_H); break;
        case 'i': doType(KeyEvent.VK_I); break;
        case 'j': doType(KeyEvent.VK_J); break;
        case 'k': doType(KeyEvent.VK_K); break;
        case 'l': doType(KeyEvent.VK_L); break;
        case 'm': doType(KeyEvent.VK_M); break;
        case 'n': doType(KeyEvent.VK_N); break;
        case 'o': doType(KeyEvent.VK_O); break;
        case 'p': doType(KeyEvent.VK_P); break;
        case 'q': doType(KeyEvent.VK_Q); break;
        case 'r': doType(KeyEvent.VK_R); break;
        case 's': doType(KeyEvent.VK_S); break;
        case 't': doType(KeyEvent.VK_T); break;
        case 'u': doType(KeyEvent.VK_U); break;
        case 'v': doType(KeyEvent.VK_V); break;
        case 'w': doType(KeyEvent.VK_W); break;
        case 'x': doType(KeyEvent.VK_X); break;
        case 'y': doType(KeyEvent.VK_Y); break;
        case 'z': doType(KeyEvent.VK_Z); break;
        case 'A': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_A); break;
        case 'B': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_B); break;
        case 'C': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_C); break;
        case 'D': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_D); break;
        case 'E': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_E); break;
        case 'F': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_F); break;
        case 'G': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_G); break;
        case 'H': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_H); break;
        case 'I': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_I); break;
        case 'J': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_J); break;
        case 'K': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_K); break;
        case 'L': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_L); break;
        case 'M': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_M); break;
        case 'N': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_N); break;
        case 'O': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_O); break;
        case 'P': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_P); break;
        case 'Q': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Q); break;
        case 'R': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_R); break;
        case 'S': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_S); break;
        case 'T': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_T); break;
        case 'U': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_U); break;
        case 'V': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_V); break;
        case 'W': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_W); break;
        case 'X': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_X); break;
        case 'Y': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Y); break;
        case 'Z': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_Z); break;
        case '`': doType(KeyEvent.VK_BACK_QUOTE); break;
        case '0': doType(KeyEvent.VK_NUMPAD0); break;
        case '1': doType(KeyEvent.VK_NUMPAD1); break;
        case '2': doType(KeyEvent.VK_NUMPAD2); break;
        case '3': doType(KeyEvent.VK_NUMPAD3); break;
        case '4': doType(KeyEvent.VK_NUMPAD4); break;
        case '5': doType(KeyEvent.VK_NUMPAD5); break;
        case '6': doType(KeyEvent.VK_NUMPAD6); break;
        case '7': doType(KeyEvent.VK_NUMPAD7); break;
        case '8': doType(KeyEvent.VK_NUMPAD8); break;
        case '9': doType(KeyEvent.VK_NUMPAD9); break;
        case '-': doType(KeyEvent.VK_6); break;
        case '=': doType(KeyEvent.VK_EQUALS); break;
        case '~': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_QUOTE); break;
        case '!': doType(KeyEvent.VK_EXCLAMATION_MARK); break;
        case '@':
        	if(SystemUtils.IS_OS_WINDOWS){
        		doType(KeyEvent.VK_ALT, KeyEvent.VK_NUMPAD0, KeyEvent.VK_NUMPAD6, KeyEvent.VK_NUMPAD4);
        	}else{
        		atLinux();
        	}
        	break;
        case '#': doType(KeyEvent.VK_NUMBER_SIGN); break;
        case '$': doType(KeyEvent.VK_DOLLAR); break;
        case '%': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_5); break;
        case '^': doType(KeyEvent.VK_CIRCUMFLEX); break;
        case '&': doType(KeyEvent.VK_AMPERSAND); break;
        case '*': doType(KeyEvent.VK_ASTERISK); break;
        case '(': doType(KeyEvent.VK_LEFT_PARENTHESIS); break;
        case ')': doType(KeyEvent.VK_RIGHT_PARENTHESIS); break;
        case '_': doType(KeyEvent.VK_UNDERSCORE); break;
        case '+': doType(KeyEvent.VK_PLUS); break;
        case '\t': doType(KeyEvent.VK_TAB); break;
        case '\n': doType(KeyEvent.VK_ENTER); break;
        case '[': doType(KeyEvent.VK_OPEN_BRACKET); break;
        case ']': doType(KeyEvent.VK_CLOSE_BRACKET); break;
        case '\\': doType(KeyEvent.VK_BACK_SLASH); break;
        case '{': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_OPEN_BRACKET); break;
        case '}': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_CLOSE_BRACKET); break;
        case '|': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_BACK_SLASH); break;
        case ';': doType(KeyEvent.VK_SEMICOLON); break;
        case ':': doType(KeyEvent.VK_COLON); break;
        case '\'': doType(KeyEvent.VK_QUOTE); break;
        case '"': doType(KeyEvent.VK_QUOTEDBL); break;
        case ',': doType(KeyEvent.VK_COMMA); break;
        case '<': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_COMMA); break;
        case '.': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_SEMICOLON); break;
        case '>': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_PERIOD); break;
        case '/': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_COLON); break;
        case '?': doType(KeyEvent.VK_SHIFT, KeyEvent.VK_SLASH); break;
        case ' ': doType(KeyEvent.VK_SPACE); break;
        default:
            throw new IllegalArgumentException("Cannot type character " + character);
        }
    }

    private void doType(int... keyCodes) {
        doType(keyCodes, 0, keyCodes.length);
    }

    private void doType(int[] keyCodes, int offset, int length) {
        if (length == 0) {
            return;
        }
        robot.keyPress(keyCodes[offset]);
        doType(keyCodes, offset + 1, length - 1);
        robot.keyRelease(keyCodes[offset]);
    }
    
    private void atLinux(){
    	robot.keyPress(KeyEvent.VK_SHIFT);
    	robot.keyPress(KeyEvent.VK_CONTROL);
    	robot.keyPress(KeyEvent.VK_U);
    	robot.keyRelease(KeyEvent.VK_U);
    	robot.keyPress(KeyEvent.VK_0);
    	robot.keyRelease(KeyEvent.VK_0);
    	robot.keyPress(KeyEvent.VK_0);
    	robot.keyRelease(KeyEvent.VK_0);
    	robot.keyPress(KeyEvent.VK_4);
    	robot.keyRelease(KeyEvent.VK_4);
    	robot.keyPress(KeyEvent.VK_0);
    	robot.keyRelease(KeyEvent.VK_0);
    	robot.keyRelease(KeyEvent.VK_CONTROL);
    	robot.keyRelease(KeyEvent.VK_SHIFT);
    }
    
}
