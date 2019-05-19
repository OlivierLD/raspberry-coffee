package images.gifutil;

/*
   Copyright (C) 2000 Bernhard Haumacher <haui@haumacher.de>

   This file is part of the haui.gif package.

   haui.gif is a Java package that allows manipulations of GIF images
   that do not need a LZW decoding or encoding algorithm.

   You can redistribute it and/or modify it under the terms of the GNU
   General Public License as published by the Free Software
   Foundation; either version 2 of the License, or (at your option)
   any later version.

   haui.gif is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.

   You should have received a copy of the GNU General Public License
   along with haui.gif; see the file GPL.  If not, write to the
   Free Software Foundation, Inc., 59 Temple Place, Suite 330, Boston,
   MA 02111-1307, USA.
*/

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class GIFOutputStream
		extends FilterOutputStream {
	public GIFOutputStream(OutputStream out) {
		super(out);
	}

	public final void writeShort(int value)
			throws IOException {
		int lo = value & 0xFF;
		int hi = (value >>> 8) & 0xFF;

		write(lo);
		write(hi);
	}

	private int bits;

	public final void writeBits()
			throws IOException {
		write(bits);
	}

	public final void clearBits()
			throws IOException {
		bits = 0;
	}

	public final void setInt(int value, int mask, int shl) {
		bits |= (value << shl) & mask;
	}

	public final void setBoolean(boolean value, int mask, int shl) {
		if (value) {
			bits |= (1 << shl);
		} else {
			bits &= ~(1 << shl);
		}
	}
}
