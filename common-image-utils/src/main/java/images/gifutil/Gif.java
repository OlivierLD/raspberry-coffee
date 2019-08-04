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

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StreamCorruptedException;

public class Gif {

	/**
	 * Signature of GIF files ('GIF').
	 */
	public final static byte[] SIGNATURE =
			{(byte) 'G', (byte) 'I', (byte) 'F'};

	/**
	 * Version information for files in GIF/87a format.
	 */
	public final static byte[] VERSION_87a =
			{(byte) '8', (byte) '7', (byte) 'a'};

	/**
	 * Version information for files in GIF/89a format.
	 */
	public final static byte[] VERSION_89a =
			{(byte) '8', (byte) '9', (byte) 'a'};

	public static class Header {
		/**
		 * The version of the GIF format '87a' and '89a' are known.
		 */
		public byte[] version = new byte[3];

		public void init(GIFInputStream in)
				throws IOException {
			//System.err.println("DEBUG: Header.init()"); // DEBUG

			in.checkBytes(SIGNATURE);
			version = in.readBytes(3);

			//System.err.println("version: '" + (char) version[0] + (char) version[1] + (char) version[2] + "'"); // DEBUG
		}

		public void write(GIFOutputStream out)
				throws IOException {
			out.write(SIGNATURE);
			out.write(version);
		}

		public String toString() {
			return "{haui.GIF.Header " + "version='" + new String(version) +
					"'" + "}";
		}

	}

	public static class ScreenDescriptor {
		public final static int MSK_colorTableFlag = 0x80;
		public final static int SHL_colorTableFlag = 7;

		public final static int MSK_colorResolution = 0x70;
		public final static int SHL_colorResolution = 4;

		public final static int MSK_sortFlag = 0x08;
		public final static int SHL_sortFlag = 3;

		public final static int MSK_colorTableSize = 0x07;
		public final static int SHL_colorTableSize = 0;

		public int width;
		public int height;

		public boolean colorTableFlag;
		public int colorResolution;
		public boolean sortFlag;
		public int colorTableSize;

		public int backgroundColorIndex;
		public int pixelAspectReatio;

		public ColorTable colorTable;

		public String toString() {
			return "{haui.GIF.ScreenDescriptor" + " width='" + width + "'" +
					" height='" + height + "'" + " colorResolution='" +
					colorResolution + "'" + " backgroundColorIndex='" +
					backgroundColorIndex + "'" + " pixelAspectReatio='" +
					pixelAspectReatio + "'" + " sort='" + sortFlag + "'" +
					(colorTableFlag ? " colorTableSize='" + colorTableSize + "'" : "") +
					"}";
		}

		public void init(GIFInputStream in)
				throws IOException {
			//System.err.println("DEBUG: ScreenDescriptor.init()"); // DEBUG

			width = in.readShort();
			height = in.readShort();

			in.readBits();
			colorTableFlag =
					in.getBoolean(MSK_colorTableFlag, SHL_colorTableFlag);
			colorResolution =
					in.getInt(MSK_colorResolution, SHL_colorResolution);
			sortFlag = in.getBoolean(MSK_sortFlag, SHL_sortFlag);
			colorTableSize = in.getInt(MSK_colorTableSize, SHL_colorTableSize);

			backgroundColorIndex = in.readByte();
			pixelAspectReatio = in.readByte();

			if (colorTableFlag) {
				colorTable = new ColorTable();
				colorTable.init(in, colorTableSize);
			}
		}

		public void write(GIFOutputStream out)
				throws IOException {
			out.writeShort(width);
			out.writeShort(height);

			out.clearBits();
			out.setBoolean(colorTableFlag, MSK_colorTableFlag,
					SHL_colorTableFlag);
			out.setInt(colorResolution, MSK_colorResolution,
					SHL_colorResolution);
			out.setBoolean(sortFlag, MSK_sortFlag, SHL_sortFlag);
			out.setInt(colorTableSize, MSK_colorTableSize, SHL_colorTableSize);
			out.writeBits();

			out.write(backgroundColorIndex);
			out.write(pixelAspectReatio);

			if (colorTableFlag) {
				colorTable.write(out);
			}
		}
	}

	public static class ImageDescriptor {
		public final static int SEPARATOR = 0x2C;

		public final static int MSK_colorTableFlag = 0x80;
		public final static int SHL_colorTableFlag = 7;

		public final static int MSK_interlacedFlag = 0x40;
		public final static int SHL_interlacedFlag = 6;

		public final static int MSK_sortFlag = 0x20;
		public final static int SHL_sortFlag = 5;

		public final static int MSK_reserved = 0x18;
		public final static int SHL_reserved = 3;

		public final static int MSK_colorTableSize = 0x07;
		public final static int SHL_colorTableSize = 0;

		public int leftPosition;
		public int topPosition;
		public int width;
		public int height;

		public boolean colorTableFlag;
		public boolean interlacedFlag;
		public boolean sortFlag;
		public int reserved;
		public int colorTableSize;

		public ColorTable colorTable;

		public ImageData data;
		public Extension extension;

		public ImageDescriptor next;

		public String toFullString() {
			StringBuffer result = new StringBuffer();
			result.append(toString());
			if (data != null) {
				result.append(" ");
				result.append(data.toString());
			}

			return result.toString();
		}

		public String toString() {
			return "{haui.GIF.ImageDescriptor" + " left='" + leftPosition + "'" +
					" top='" + topPosition + "'" + " width='" + width + "'" +
					" height='" + height + "'" + " interlaced='" + interlacedFlag +
					"'" + " sort='" + sortFlag + "'" +
					(colorTableFlag ? " colorTableSize='" + colorTableSize + "'" : "") +
					"}";
		}

		public void init(GIFInputStream in)
				throws IOException {
			//System.err.println("DEBUG: ImageDescriptor.init()"); // DEBUG

			leftPosition = in.readShort();
			topPosition = in.readShort();
			width = in.readShort();
			height = in.readShort();

			in.readBits();
			colorTableFlag =
					in.getBoolean(MSK_colorTableFlag, SHL_colorTableFlag);
			interlacedFlag =
					in.getBoolean(MSK_interlacedFlag, SHL_interlacedFlag);
			sortFlag = in.getBoolean(MSK_sortFlag, SHL_sortFlag);
			reserved = in.getInt(MSK_reserved, SHL_reserved);
			colorTableSize = in.getInt(MSK_colorTableSize, SHL_colorTableSize);

			if (colorTableFlag) {
				colorTable = new ColorTable();
				colorTable.init(in, colorTableSize);
			}
		}

		public void write(GIFOutputStream out)
				throws IOException {
			//System.err.println("DEBUG: ImageDescriptor.write()"); // DEBUG
			out.write(SEPARATOR);

			out.writeShort(leftPosition);
			out.writeShort(topPosition);
			out.writeShort(width);
			out.writeShort(height);

			out.clearBits();
			out.setBoolean(colorTableFlag, MSK_colorTableFlag,
					SHL_colorTableFlag);
			out.setBoolean(interlacedFlag, MSK_interlacedFlag,
					SHL_interlacedFlag);
			out.setBoolean(sortFlag, MSK_sortFlag, SHL_sortFlag);
			out.setInt(reserved, MSK_reserved, SHL_reserved);
			out.setInt(colorTableSize, MSK_colorTableSize, SHL_colorTableSize);
			out.writeBits();

			if (colorTableFlag) {
				colorTable.write(out);
			}
		}
	}

	public static class ColorTable {
		public int size;
		public byte[] data;

		public ColorTable init(GIFInputStream in, int sizeDescr)
				throws IOException {
			//System.err.println("DEBUG: ColorTable.init()"); // DEBUG

			this.size = 3 * (1 << (sizeDescr + 1));
			this.data = in.readBytes(size);

			return this;
		}

		public void write(GIFOutputStream out)
				throws IOException {
			out.write(data);
		}

		public String toString() {
			StringBuffer result = new StringBuffer();
			result.append("{GIF.ColorTable");
			for (int n = 0; n < data.length; n += 3) {
				result.append(" r" + n + "='" + (((int) data[n]) & 0xFF) + "'");
				result.append(" g" + n + "='" + (((int) data[n + 1]) & 0xFF) +
						"'");
				result.append(" b" + n + "='" + (((int) data[n + 2]) & 0xFF) +
						"'");
			}
			result.append("}");

			return result.toString();
		}

	}

	public static class Data {
		public byte[] data;
		public Data next;

		public void init(GIFInputStream in)
				throws IOException {
			//System.err.println("DEBUG: Data.init()"); // DEBUG

			int size = in.readByte();
			if (size > 0) {
				data = in.readBytes(size);
			} else {
				data = null;
			}
		}

		public void write(GIFOutputStream out)
				throws IOException {
			if (data != null) {
				out.write(data.length);
				out.write(data);
			} else {
				out.write(0);
			}
		}

		public String attrToString() {
			int size = 0;
			int cnt = 0;
			Data all = this;
			while (all != null) {
				cnt++;
				if (all.data != null)
					size += all.data.length;

				all = all.next;
			}

			return " blocks='" + cnt + "'" + " size='" + size + "'";
		}

		public String toString() {
			return "{GIF.Data" + attrToString() + "}";
		}
	}

	public static class ImageData
			extends Data {
		public int minimumCodeSize;

		public String attrToString() {
			return " minimumCodeSize='" + minimumCodeSize + "'" +
					super.attrToString();
		}

		public String toString() {
			return "{GIF.ImageData" + attrToString() + "}";
		}

		public void init(GIFInputStream in)
				throws IOException {
			//System.err.println("DEBUG: ImageData.init()"); // DEBUG

			minimumCodeSize = in.readByte();
			super.init(in);
		}

		public void write(GIFOutputStream out)
				throws IOException {
			out.write(minimumCodeSize);
			super.write(out);
		}
	}

	public static class Extension
			extends Data {
		public final static int SEPARATOR = 0x21;

		public final static int TERMINATOR = 0x00;

		public int label;
		public Extension next;

		public String attrToString() {
			return " label='" + label + "'" + super.attrToString();
		}

		public String toString() {
			return "{GIF.Extension" + attrToString() + "}";
		}

		public static Extension read(GIFInputStream in)
				throws IOException {
			Extension result;

			int label = in.readByte();
			switch (label) {
				case Gif.ControlExtension.LABEL:
					//System.err.println("ControlExtension"); // DEBUG
					result = new ControlExtension();
					break;

				case Gif.CommentExtension.LABEL:
					//System.err.println("CommentExtension"); // DEBUG
					result = new CommentExtension();
					break;

				case Gif.TextExtension.LABEL:
					//System.err.println("TextExtension"); // DEBUG
					result = new TextExtension();
					break;

				case Gif.ApplicationExtension.LABEL:
					//System.err.println("ApplicationExtension"); // DEBUG
					result = new ApplicationExtension();
					break;

				default:
					//System.err.println("Extension"); // DEBUG
					//System.err.println("DEBUG: unknown extension (" + label + ")"); // DEBUG
					result = new Extension();
			}

			result.init(in, label);

			if (result instanceof CommentExtension) {
				//System.err.println(result.toString()); // DEBUG
			}

			return result;
		}

		public void init(GIFInputStream in, int label)
				throws IOException {
			//System.err.println("DEBUG: Extension.init(" + label + ")"); // DEBUG
			this.label = label;
			super.init(in);

			((Data) this).next = readData(in);
		}

		public void write(GIFOutputStream out)
				throws IOException {
			//System.err.println("DEBUG: Extension.write()"); // DEBUG

			out.write(SEPARATOR);
			out.write(label);
			super.write(out);
		}
	}

	public static class ControlExtension
			extends Extension {
		public final static int SIZE = 4;
		public final static int LABEL = 0xF9;

		public final static int DISPOSAL_NONE = 0;
		public final static int DISPOSAL_NO = 1;
		public final static int DISPOSAL_BACKGROUND = 2;
		public final static int DISPOSAL_PREVIOUS = 3;

		public final static boolean USERINPUT_NO = false;
		public final static boolean USERINPUT_YES = true;

		public final static boolean TRANSPARENCY_NO = false;
		public final static boolean TRANSPARENCY_YES = true;

		int reserved;
		int disposalMethod;
		boolean userInputFlag;
		boolean transparentColorFlag;

		public final static int MSK_reserved = 0xE0;
		public final static int SHL_reserved = 5;

		public final static int MSK_disposalMethod = 0x1C;
		public final static int SHL_disposalMethod = 2;

		public final static int MSK_userInputFlag = 0x02;
		public final static int SHL_userInputFlag = 1;

		public final static int MSK_transparentColorFlag = 0x01;
		public final static int SHL_transparentColorFlag = 0;

		int delayTime;
		int transparentColorIndex;

		public String attrToString() {
			return " disposalMethod='" + disposalMethod + "'" + " userInput='" +
					userInputFlag + "'" +
					(transparentColorFlag ? " transparentColorIndex='" +
							transparentColorIndex + "'" : "") + " delayTime='" + delayTime +
					"'" + super.attrToString();
		}

		public String toString() {
			return "{GIF.ControlExtension" + attrToString() + "}";
		}

		public void init(GIFInputStream in, int label)
				throws IOException {
			//System.err.println("DEBUG: ControlExtension.init()"); // DEBUG

			// not: super.init(in), data will be consumed here!
			this.label = label;

			in.checkByte(SIZE);

			in.readBits();
			reserved = in.getInt(MSK_reserved, SHL_reserved);
			disposalMethod = in.getInt(MSK_disposalMethod, SHL_disposalMethod);
			userInputFlag = in.getBoolean(MSK_userInputFlag, SHL_userInputFlag);
			transparentColorFlag =
					in.getBoolean(MSK_transparentColorFlag, SHL_transparentColorFlag);

			delayTime = in.readShort();
			transparentColorIndex = in.readByte();

			in.checkByte(TERMINATOR);
		}

		public void write(GIFOutputStream out)
				throws IOException {
			//System.err.println("DEBUG: ControlExtension.write()"); // DEBUG

			out.write(SEPARATOR);
			out.write(LABEL);

			out.write(SIZE);

			out.clearBits();
			out.setInt(reserved, MSK_reserved, SHL_reserved);
			out.setInt(disposalMethod, MSK_disposalMethod, SHL_disposalMethod);
			out.setBoolean(userInputFlag, MSK_userInputFlag, SHL_userInputFlag);
			out.setBoolean(transparentColorFlag, MSK_transparentColorFlag,
					SHL_transparentColorFlag);
			out.writeBits();

			out.writeShort(delayTime);
			out.write(transparentColorIndex);
			out.write(TERMINATOR);
		}

	}

	public static class CommentExtension
			extends Extension {
		public final static int LABEL = 0xFE;

		public String toString() {
			StringBuffer comment = new StringBuffer();

			Data data = this;
			while (data.data != null) {
				comment.append(new String(data.data));
				data = data.next;
			}

			return "{GIF.CommentExtension" + " comment='" + comment + "'" +
					attrToString() + "}";
		}
	}

	public static class TextExtension
			extends Extension {
		public final static int LABEL = 0x01;

		public String toString() {
			return "{GIF.TextExtension" + attrToString() + "}";
		}
	}

	public static class ApplicationExtension
			extends Extension {
		public final static int LABEL = 0xFF;

		public String toString() {
			return "{GIF.ApplicationExtension" + attrToString() + "}";
		}
	}

	/**
	 * Convenience class for <b>generating</b> NETSCAPE2.0
	 * extensions. This such extensions are not yet parsed from a GIF
	 * stream. All parsed application extensions are of type
	 * <code>ApplicationExtension</code>
	 */
	public static class NetscapeExtension
			extends ApplicationExtension {
		public final static byte[] ID = "NETSCAPE2.0".getBytes();

		public int loops;

		public NetscapeExtension(int loops) {
			this.loops = loops;
		}

		public void write(GIFOutputStream out)
				throws IOException {
			//System.err.println("DEBUG: NetscapeExtension.write()"); // DEBUG

			out.write(SEPARATOR);
			out.write(LABEL);

			out.write(ID.length);
			out.write(ID);

			out.write(0x03);
			out.write(0x01);
			out.writeShort(loops);

			out.write(TERMINATOR);
		}
	}

	public final static class Trailer {
		public final static int SEPARATOR = 0x3B;

		/**
		 * no instances of Trailer exist
		 */
		private Trailer() {
		}

		public String toString() {
			return "{GIF.Trailer}";
		}

		public static void write(GIFOutputStream out)
				throws IOException {
			out.write(Gif.Trailer.SEPARATOR);
		}
	}

	public Header header;
	public ScreenDescriptor screen;
	public ImageDescriptor image;
	public Extension tail;

	public void init(GIFInputStream in)
			throws IOException {
		parse(in, new Creator(this));
	}

	public static void parse(GIFInputStream in, ParseCallback callback)
			throws IOException {
		{
			//System.err.println("Header"); // DEBUG
			Header header = new Header();
			header.init(in);
			callback.notifyHeader(header);
		}

		{
			//System.err.println("ScreenDescriptor"); // DEBUG
			ScreenDescriptor screen = new ScreenDescriptor();
			screen.init(in);
			callback.notifyScreen(screen);
		}

		while (true) {
			int separator = in.readByte();
			switch (separator) {
				case Gif.ImageDescriptor.SEPARATOR:
					//System.err.println("ImageDescriptor"); // DEBUG
					ImageDescriptor image = new ImageDescriptor();
					image.init(in);
					callback.notifyImage(image);

					//System.err.println("ImageData"); // DEBUG
					ImageData imageData = new ImageData();
					imageData.init(in);
					callback.notifyImageData(imageData);

					Data data = imageData;
					while (data.data != null) {
						//System.err.println("Data"); // DEBUG
						data = new Data();
						data.init(in);
						callback.notifyData(data);
					}
					break;

				case Gif.Extension.SEPARATOR:
					Extension extension = Gif.Extension.read(in);
					callback.notifyExtension(extension);
					break;

				case 0: // LAZY: some corrupted gifs end with wrong separator (0)
				case Gif.Trailer.SEPARATOR:
					//System.err.println("Trailer"); // DEBUG
					callback.notifyTrailer();
					return;

				default:
					//System.err.println("unknown separator: " + separator + " ignored."); // DEBUG
					throw new StreamCorruptedException("unknown separator: " +
							separator);
			}
		}
	}

	public static Data readData(GIFInputStream in)
			throws IOException {
		Data result = new Data();

		Data block = result;
		while (true) {
			//System.err.println("Data"); // DEBUG
			block.init(in);
			if (block.data == null)
				break;

			block = block.next = new Data();
		}

		return result;
	}

	public static void writeData(GIFOutputStream out, Data data)
			throws IOException {
		while (data != null) {
			data.write(out);
			data = data.next;
		}
	}

	public void write(GIFOutputStream out)
			throws IOException {
		header.write(out);
		screen.write(out);

		ImageDescriptor image = this.image;
		while (image != null) {
			Extension extension = image.extension;
			while (extension != null) {
				writeData(out, extension);
				extension = extension.next;
			}

			image.write(out);
			writeData(out, image.data);
			image = image.next;
		}

		Extension extension = tail;
		while (extension != null) {
			writeData(out, extension);
			extension = extension.next;
		}

		Gif.Trailer.write(out);
	}

	public static interface ParseCallback {
		public void notifyHeader(Header header)
				throws IOException;

		public void notifyScreen(ScreenDescriptor screen)
				throws IOException;

		public void notifyImage(ImageDescriptor image)
				throws IOException;

		public void notifyImageData(ImageData data)
				throws IOException;

		public void notifyData(Data data)
				throws IOException;

		public void notifyExtension(Extension extension)
				throws IOException;

		public void notifyTrailer()
				throws IOException;
	}

	public static class Creator
			implements ParseCallback {
		protected Gif target;

		protected ImageDescriptor prevImage = null;
		protected Data prevData = null;
		protected Extension firstExtension = null;
		protected Extension prevExtension = null;

		public Creator(Gif target) {
			this.target = target;
		}

		public void notifyHeader(Header _header) {
			target.header = _header;
		}

		public void notifyScreen(ScreenDescriptor _screen) {
			target.screen = _screen;
		}

		public void notifyImage(ImageDescriptor _image) {
			_image.extension = firstExtension;
			firstExtension = null;

			if (target.image == null) {
				target.image = prevImage = _image;
			} else {
				prevImage = prevImage.next = _image;
			}
		}

		public void notifyImageData(ImageData data)
				throws IOException {
			prevData = prevImage.data = data;
		}

		public void notifyData(Data data)
				throws IOException {
			prevData = prevData.next = data;
		}

		public void notifyExtension(Extension _extension) {
			if (firstExtension == null) {
				firstExtension = prevExtension = _extension;
			} else {
				prevExtension = prevExtension.next = _extension;
			}
		}

		public void notifyTrailer() {
			target.tail = firstExtension;
			firstExtension = null;
		}
	}

	public static class Filter
			implements Runnable, ParseCallback {
		private GIFInputStream in;
		protected GIFOutputStream out;

		private IOException ex;

		public Filter(GIFInputStream in, GIFOutputStream out) {
			this.in = in;
			this.out = out;
		}

		public boolean success() {
			return ex == null;
		}

		public IOException getIOException() {
			return ex;
		}

		public void run() {
			ex = null;
			try {
				parse(in, this);
			} catch (IOException ex) {
				this.ex = ex;
			}
		}

		/**
		 * overriding methods must call super.notifyHeader(header) before
		 * returning
		 */
		public void notifyHeader(Header header)
				throws IOException {
			header.write(out);
		}

		/**
		 * overriding methods must call super.notifyScreen(screen) before
		 * returning
		 */
		public void notifyScreen(ScreenDescriptor screen)
				throws IOException {
			screen.write(out);
		}

		/**
		 * overriding methods must call super.notifyImage(image) before
		 * returning
		 */
		public void notifyImage(ImageDescriptor image)
				throws IOException {
			image.write(out);
		}

		/**
		 * overriding methods must call super.notifyImageData(data)
		 * before returning
		 */
		public void notifyImageData(ImageData data)
				throws IOException {
			data.write(out);
		}

		/**
		 * overriding methods must call super.notifyData(data)
		 * before returning
		 */
		public void notifyData(Data data)
				throws IOException {
			data.write(out);
		}

		/**
		 * overriding methods must call super.notifyExtension(extension)
		 * before returning
		 */
		public void notifyExtension(Extension extension)
				throws IOException {
			extension.write(out);
		}

		/**
		 * overriding methods must call super.notifyTrailer() before
		 * returning
		 */
		public void notifyTrailer()
				throws IOException {
			Gif.Trailer.write(out);
		}
	}

	public String toFullString() {
		StringBuffer result = new StringBuffer();

		result.append("{haui.GIF ");
		result.append(header.toString());
		result.append(" ");
		result.append(screen.toString());
		ImageDescriptor image = this.image;
		while (image != null) {
			Extension extension = image.extension;
			while (extension != null) {
				result.append(" ");
				result.append(extension.toString());
				extension = extension.next;
			}
			result.append(" ");
			result.append(image.toString());
			image = image.next;
		}
		if (tail != null) {
			result.append(" ");
			result.append(tail.toString());
		}
		result.append("}");

		return result.toString();
	}

	public static void main(String... args)
			throws Exception {
		Gif image = new Gif();
		try {
			image.init(new GIFInputStream(new FileInputStream(args[0])));

			System.out.println(image.toFullString());

			image.write(new GIFOutputStream(new FileOutputStream(args[0] + ".new")));
		} catch (Exception ex) {
			//System.err.println(image.toFullString());
			throw ex;
		}
	}
}
