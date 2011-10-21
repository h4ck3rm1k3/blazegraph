/*

Copyright (C) SYSTAP, LLC 2006-2011.  All rights reserved.

Contact:
     SYSTAP, LLC
     4501 Tower Road
     Greensboro, NC 27410
     licenses@bigdata.com

This program is free software; you can redistribute it and/or modify
it under the terms of the GNU General Public License as published by
the Free Software Foundation; version 2 of the License.

This program is distributed in the hope that it will be useful,
but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
GNU General Public License for more details.

You should have received a copy of the GNU General Public License
along with this program; if not, write to the Free Software
Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
*/

package com.bigdata.util;

/**
 * PseudoRandom is derived from an algorithm used to create a dissolve
 * graphics effect.
 * 
 * Given a maximum number it generates all numbers from 1 to that number
 * in a deterministic but pseudorandom order.
 * 
 * It is therefore particularly useful for test cases.
 * 
 * See: http://www.mactech.com/articles/mactech/Vol.06/06.12/SafeDissolve/index.html
 * 
 */

public class PseudoRandom {
	int m_mask = 0;
	int m_next = 1;
	final int m_max;
	
	static final int[] s_masks = {0x03, 0x06, 0x0C, 0x14, 0x30, 0x60, 0xB8, 0x0110, 0x0240, 
	0x0500, 0x0CA0, 0x1B00, 0x3500, 0x6000, 0xB400, 0x00012000, 0x00020400, 
	0x00072000, 0x00090000, 0x00140000, 0x00300000, 0x00400000, 0x00D80000, 
	0x01200000, 0x03880000, 0x07200000, 0x09000000, 0x14000000, 0x32800000, 
	0x48000000, 0xA3000000};
	
	public PseudoRandom(final int range, final int next) {
		this(range);
		
		m_next = next;
	}
	
	public PseudoRandom(final int range) {
		for (int m = 0; m < s_masks.length; m++) {
			if (s_masks[m] > range) {
				m_mask = s_masks[m];
				break;
			}
		}
		m_max = range;
	}
	
	public int next() {
		if ((m_next & 1) == 1)
			m_next = (m_next >> 1) ^ m_mask;
		else m_next = (m_next >> 1);
		
		if (m_next > m_max)
			return next();
		else			
			return m_next;
	}
	
	public int next(final int prev) {
		m_next = prev;
		
		return next();		
	}
}