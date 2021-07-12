package com.wind.meditor.core;

import com.wind.meditor.property.ModificationProperty;
import com.wind.meditor.utils.Log;
import com.wind.meditor.utils.Utils;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import java.util.zip.ZipOutputStream;

import pxb.android.axml.Util;

/**
 * @author Windysha
 */
public class FileProcesserApkAndManifest {

    public static void processApkFile(String srcApkPath, String dstApkPath, String manifestFilePath) {
        FileOutputStream outputStream = null;
        ZipOutputStream zipOutputStream = null;
        FileInputStream manifestFileInputStream =null;
        ZipFile zipFile = null;

        long time = System.currentTimeMillis();

        try {
            outputStream = new FileOutputStream(dstApkPath);
            zipOutputStream = new ZipOutputStream(outputStream);
            manifestFileInputStream = new FileInputStream(manifestFilePath);

            try {
                zipFile = new ZipFile(srcApkPath, Charset.forName("gbk"));
            } catch (Throwable e) {
                zipFile = new ZipFile(srcApkPath);
            }

            for (Enumeration entries = zipFile.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();

//                Log.d(" zipEntryName = " + zipEntryName);

                // ignore signature files, we will resign it.
                if (zipEntryName.startsWith("META-INF")) {
                    continue;
                }

                InputStream zipInputStream = null;
                try {
                    zipInputStream = zipFile.getInputStream(entry);

                    ZipEntry zosEntry = new ZipEntry(entry.getName());
                    zosEntry.setComment(entry.getComment());
                    zosEntry.setExtra(entry.getExtra());

                    zipOutputStream.putNextEntry(zosEntry);
                    if ("AndroidManifest.xml".equals(zipEntryName)) {
                        Log.i(" add AndroidManifest.xml to apk --> " + (System.currentTimeMillis() - time) + " ms");
                        Utils.copyStream(manifestFileInputStream,zipOutputStream);
                        // if it is manifest file, modify it.
//                        new ManifestEditor(zipInputStream, zipOutputStream, property).processManifest();
                    } else if (!entry.isDirectory()) {
                        Utils.copyStream(zipInputStream, zipOutputStream);
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Utils.close(zipInputStream);
                }
                zipOutputStream.closeEntry();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.close(zipOutputStream);
            Utils.close(outputStream);
            Utils.close(zipFile);

            Log.i(" processApkFile time --> " + (System.currentTimeMillis() - time) + " ms");
        }
    }

    public static void processApkFileOutManifest(String srcApkPath,String manifestFilePath) {
        FileOutputStream outputStream = null;
        ZipFile zipFile = null;

        long time = System.currentTimeMillis();

        try {
            outputStream = new FileOutputStream(manifestFilePath);

            try {
                zipFile = new ZipFile(srcApkPath, Charset.forName("gbk"));
            } catch (Throwable e) {
                zipFile = new ZipFile(srcApkPath);
            }

            for (Enumeration entries = zipFile.entries(); entries.hasMoreElements(); ) {
                ZipEntry entry = (ZipEntry) entries.nextElement();
                String zipEntryName = entry.getName();

//                Log.d(" zipEntryName = " + zipEntryName);

                // ignore signature files, we will resign it.
                if (zipEntryName.startsWith("META-INF")) {
                    continue;
                }

                InputStream zipInputStream = null;
                try {
                    zipInputStream = zipFile.getInputStream(entry);

                    ZipEntry zosEntry = new ZipEntry(entry.getName());
                    zosEntry.setComment(entry.getComment());
                    zosEntry.setExtra(entry.getExtra());

                    if ("AndroidManifest.xml".equals(zipEntryName)) {
                        Log.i("get AndroidManifest.xml from apk --> " + (System.currentTimeMillis() - time) + " ms");
                        Utils.copyStream(zipInputStream,outputStream);
                        Log.i("write AndroidManifest.xml to " + manifestFilePath+" --> " + (System.currentTimeMillis() - time) + " ms");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                } finally {
                    Utils.close(zipInputStream);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.close(outputStream);
            Utils.close(zipFile);

            Log.i(" processApkFileOutputManifest time --> " + (System.currentTimeMillis() - time) + " ms");
        }
    }


}
