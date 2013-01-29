/*
 * Funambol is a mobile platform developed by Funambol, Inc.
 * Copyright (C) 2011 Funambol, Inc.
 *
 * This program is free software; you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License version 3 as published by
 * the Free Software Foundation with the addition of the following permission
 * added to Section 15 as permitted in Section 7(a): FOR ANY PART OF THE COVERED
 * WORK IN WHICH THE COPYRIGHT IS OWNED BY FUNAMBOL, FUNAMBOL DISCLAIMS THE
 * WARRANTY OF NON INFRINGEMENT  OF THIRD PARTY RIGHTS.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program; if not, see http://www.gnu.org/licenses or write to
 * the Free Software Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston,
 * MA 02110-1301 USA.
 *
 * You can contact Funambol, Inc. headquarters at 643 Bair Island Road, Suite
 * 305, Redwood City, CA 94063, USA, or at email address info@funambol.com.
 *
 * The interactive user interfaces in modified source and object code versions
 * of this program must display Appropriate Legal Notices, as required under
 * Section 5 of the GNU Affero General Public License version 3.
 *
 * In accordance with Section 7(b) of the GNU Affero General Public License
 * version 3, these Appropriate Legal Notices must retain the display of the
 * "Powered by Funambol" logo. If the display of the logo is not reasonably
 * feasible for technical reasons, the Appropriate Legal Notices must display
 * the words "Powered by Funambol".
 */

package com.funambol.framework.tools;


import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;


/**
 * Container of utility methods for io access
 *
 *
 * @version $Id: IOTools.java,v 1.2 2006/08/07 21:09:23 nichele Exp $
 */
public class IOTools {

    /**
     * Reads a file into a byte array given its filename
     *
     * @param file the filename (as java.io.File)
     *
     * @return the content of the file as a byte array
     *
     * @throws java.io.IOException;
     */
    static public byte[] readFileBytes(File file)
    throws IOException {
        FileInputStream fis = null;

        byte[] buf = new byte[(int)file.length()];
        try {
            fis = new FileInputStream(file);
            fis.read(buf);
            fis.close();
        } finally {
            if (fis != null) {
                fis.close();
            }
        }

        return buf;
    }


    /**
     * Reads a file into a byte array given its filename
     *
     * @param filename the filename (as java.lang.String)
     *
     * @return the content of the file as a byte array
     *
     * @throws java.io.IOException;
     */
    static public byte[] readFileBytes(String filename)
    throws IOException {
        return readFileBytes(new File(filename));
    }

    /**
     * Reads a file into a String given its filename
     *
     * @param file the filename (as java.io.File)
     *
     * @return the content of the file as a string
     *
     * @throws java.io.IOException;
     */
    static public String readFileString(File file)
    throws IOException {
        return new String(readFileBytes(file));
    }

    /**
     * Reads a file into a String given its filename
     *
     * @param filename the filename (as java.lang.String)
     *
     * @return the content of the file as a string
     *
     * @throws java.io.IOException;
     */
    static public String readFileString(String filename)
    throws IOException {
        return readFileString(new File(filename));
    }

    /**
     * Writes the given string to the file with the given name
     *
     * @param str the string to write
     * @param file the file name as a java.io.File
     *
     * @throws java.io.IOException
     */
    static public void writeFile(String str, File file)
    throws IOException {
        writeFile(str.getBytes(), file);
    }

    /**
     * Writes the given string to the file with the given name
     *
     * @param str the string to write
     * @param filename the file name as a java.lang.String
     *
     * @throws java.io.IOException
     */
    static public void writeFile(String str, String filename)
    throws IOException {
        writeFile(str.getBytes(), new File(filename));
    }

    /**
     * Writes the given bytes to the file with the given name
     *
     * @param buf the bytes to write
     * @param filename the file name as a java.lang.String
     *
     * @throws java.io.IOException
     */
    static public void writeFile(byte[] buf, String filename)
    throws IOException {
        writeFile(buf, new File(filename));
    }

    /**
     * Writes the given bytes to the file with the given name
     *
     * @param buf the bytes to write
     * @param file the file name as a java.io.File
     *
     * @throws java.io.IOException
     */
    static public void writeFile(byte[] buf, File file)
    throws IOException {
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);
            fos.write(buf);
            fos.close();
        } finally {
            if (fos != null) {
                fos.close();
            }
        }
    }

    /**
     * Returns a <i>FilenameFilter</i> that accepts only the files of the given
     * type (extension).
     *
     * @param type the type (the file extension) of the files to select. NULL
     *             means all files, the empty string means files without extension
     *             The filtering is case-insensitive
     *
     * @return the filter
     */
    public static FilenameFilter getFileTypeFilter(String type) {
        return new FileTypeFilter(type);
    }

    // -------------------------------------------------------------------------

    /**
     * This class is a <i>FilenameFilter</i> that accepts only the files of the
     * specified type (extension). The filtering is case-insensitive,
     */
    public static class FileTypeFilter implements FilenameFilter {

        private String type;

        /**
         * Creates the filter on the given type.
         *
         * @param type the type (the file extension) of the files to select. NULL
         *             means all files, the empty string means files without
         *             extension. The filtering is case-insensitive
         */
        public FileTypeFilter(final String type) {
            this.type = type.toUpperCase();
        }

        public boolean accept(File dir, String name) {
            if (type == null) {
                return true;
            }

            if (type.length() == 0) {
                return (name.indexOf('.') < 0);
            }

            return (name.toUpperCase().endsWith(type));
        }
    }
}
