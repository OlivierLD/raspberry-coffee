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

import java.io.EOFException;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StreamCorruptedException;

public class GIFInputStream
		extends FilterInputStream {
	public GIFInputStream(InputStream in) {
		super(in);
	}

	public final int readByte()
			throws IOException {
		int data = read();
		if (data < 0)
			throw new EOFException();

		return data;
	}

	public final byte[] readBytes(int cnt)
			throws IOException {
		byte[] result = new byte[cnt];

		int pos = 0;
		while (cnt > 0) {
			int direct = read(result, pos, cnt);
			if (direct < 0)
				throw new EOFException();

			cnt -= direct;
			pos += direct;
		}

		return result;
	}

	public final void checkByte(int x)
			throws IOException {
		if (x != readByte())
			throw new StreamCorruptedException("Not a GIF!");
	}

	public final void checkBytes(byte[] value)
			throws IOException {
		int cnt = value.length;
		for (int n = 0; n < cnt; n++) {
			checkByte(value[n]);
		}
	}

	public final int readShort()
			throws IOException {
		int lo = readByte();
		int hi = readByte();

		return (hi << 8) | lo;
	}

	private int bits;

	public final void readBits()
			throws IOException {
		bits = readByte();
	}

	public final int getInt(int mask, int shr) {
		return (bits & mask) >>> shr;
	}

	public final boolean getBoolean(int mask, int shr) {
		return (bits & mask) != 0;
	}
}
