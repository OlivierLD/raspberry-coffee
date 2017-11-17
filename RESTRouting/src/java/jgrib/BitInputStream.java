/**
 * ===============================================================================
 * $Id: BitInputStream.java,v 1.4 2006/07/31 11:55:20 frv_peg Exp $
 * ===============================================================================
 * JGRIB library  
 *  
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *  
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 * 
 * Authors:
 * See AUTHORS file
 * ===============================================================================
 */

/**
 * BitInputStream.java original name:
 * GribRecordBMS.java  1.0  01/01/2001
 *
 * Original author:
 * (C) Benjamin Stark 
 */

package jgrib;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;


/**
 * This class is an input stream wrapper that can read a specific number of
 * bytes and bits from an input stream.
 *
 * @author  Benjamin Stark
 * @version 1.0
 */

public class BitInputStream extends FilterInputStream
{
   /**
    * Buffer for one byte which will be processed bit by bit.
    */
   protected int bitBuf = 0;

   /**
    * Current bit position in <tt>bitBuf</tt>.
    */
   protected int bitPos = 0;


   /**
    * Constructs a bit input stream from an <tt>InputStream</tt> object.
    *
    * @param in input stream that will be wrapped
    */
   public BitInputStream(InputStream in)
   {

      super(in);
      // System.out.println("Class initialized");
   }


   /**
    * Read an unsigned 8 bit value.
    *
    * @return unsigned 8 bit value as integer
    * @throws IOException 
    */
   public int readUI8() throws IOException
   {

      int ui8 = in.read();

      if (ui8 < 0)
         throw new IOException("End of input.");

      return ui8;
   }


   /**
    * Read specific number of unsigned bytes from the input stream.
    *
    * @param length number of bytes to read and return as integers
    *
    * @return unsigned bytes as integer values
    * @throws IOException 
    */
   public int[] readUI8(int length) throws IOException
   {

      int[] data = new int[length];
      int read = 0;

      for (int i = 0; i < length && read >= 0; i++)
         data[i] = read = this.read();

      if (read < 0)
         throw new IOException("End of input.");

      return data;
   }


   /**
    * Read specific number of bytes from the input stream.
    *
    * @param length number of bytes to read
    *
    * @return array of read bytes
    * @throws IOException 
    */
   public byte[] read(int length) throws IOException
   {

      byte[] data = new byte[length];

      int numRead = this.read(data);

      if (numRead < length)
      {

         // retry reading
         int numReadRetry = this.read(data, numRead, data.length - numRead);

         if (numRead + numReadRetry < length)
            throw new IOException("Unexpected end of input.");
      }

      return data;
   }


   /**
    * Read an unsigned value from the given number of bits.
    *
    * @param numBits number of bits used for the unsigned value
    *
    * @return value read from <tt>numBits</tt> bits as long
    * @throws IOException 
    */
   public long readUBits(int numBits) throws IOException
   {

      if (numBits == 0) return 0;

      int bitsLeft = numBits;
      long result = 0;

      if (this.bitPos == 0)
      {

         this.bitBuf = in.read();
         this.bitPos = 8;
      }

      while (true)
      {

         int shift = bitsLeft - this.bitPos;
         if (shift > 0)
         {

            // Consume the entire buffer
            result |= this.bitBuf << shift;
            bitsLeft -= this.bitPos;

            // Get the next byte from the input stream
            this.bitBuf = in.read();
            this.bitPos = 8;
         }
         else
         {

            // Consume a portion of the buffer
            result |= this.bitBuf >> -shift;
            this.bitPos -= bitsLeft;
            this.bitBuf &= 0xff >> (8 - this.bitPos);   // mask off consumed bits

            return result;
         }
      }
   }


   /**
    * Read a signed value from the given number of bits
    *
    * @param numBits number of bits used for the signed value
    *
    * @return value read from <tt>numBits</tt> bits as integer
    * @throws IOException 
    */
   public int readSBits(int numBits) throws IOException
   {

      // Get the number as an unsigned value.
      long uBits = readUBits(numBits);

      // Is the number negative?
      if ((uBits & (1L << (numBits - 1))) != 0)
      {

         // Yes. Extend the sign.
         uBits |= -1L << numBits;
      }

      return (int) uBits;
   }

}

