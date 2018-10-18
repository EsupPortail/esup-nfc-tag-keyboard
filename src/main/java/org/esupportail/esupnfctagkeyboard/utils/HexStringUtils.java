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
package org.esupportail.esupnfctagkeyboard.utils;

public class HexStringUtils {

	public static String swapPairs(String s) {
		s = new StringBuilder(s).reverse().toString();
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

}