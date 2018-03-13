package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Class that handles a downloaded album. A downloaded album is expected to be a zipped file,
 * which need to be unzipped and then copies over to the music directory.
 */
public class DownloadedAlbumHandler implements DownloadedFileHandlerStrategy {

    private static final String TAG = "DownloadedAlbumHandler";

    private Context context;
    /**
     * Constructor that takes a context.
     * @param c context
     */
    public DownloadedAlbumHandler(Context c) {
        this.context = c.getApplicationContext();
    }

    /**
     * Unzip the album and copy the musics over, if the music is not in the download directory
     * @return list of files that's copied over to the music directory
     */
    @Override
    public LinkedList<String> process(String url, String filename) {

        if (!checkExtension(FilenameUtils.getExtension(filename))) {
            Log.d(TAG, "Extension: " + FilenameUtils.getExtension(filename));
            FileUtils.deleteQuietly(new File(WebMusicDownloader.DOWNLOAD_DIR, filename));
            return null;
        }

        String unzipFolderStr = filename.replaceAll("\\.zip$", "");

        LinkedList<String> unzippedFiles = unpackZip(WebMusicDownloader.DOWNLOAD_DIR, filename);
        LinkedList<String> copiedFiles = new LinkedList<>();

        // make song objects, add them to the global song list.


        // copy over the files.
        for (String songFilename: unzippedFiles) {

            if (!FilenameUtils.getExtension(songFilename).toLowerCase().equals("mp3") ||
                    songFilename.matches(".*__MAXOSX.*"))
                continue;

            Log.d(TAG, "Handling unzipped file " + songFilename);

            File fileInMusicDir = new File(makeDirStr(MusicPlayerActivity.MUSIC_DIR, songFilename));
            File fileInDownloadDir = new File(makeDirStr(WebMusicDownloader.DOWNLOAD_DIR, songFilename));

            // if the same file (or at least with the same name) already exist in music dir,
            // ignore this file.
            // otherwise move the file
            Log.d(TAG, fileInMusicDir.getAbsolutePath()+".exists(): " +fileInMusicDir.exists());
            Log.d(TAG, fileInDownloadDir.getAbsolutePath()+".exists(): " +fileInDownloadDir.exists());

            if (!fileInMusicDir.exists()) {
                fileInMusicDir.getParentFile().mkdirs();
                if (fileInDownloadDir.renameTo(fileInMusicDir)) {
                    copiedFiles.add(songFilename);
                }
            }
        }

        // delete the unzipped folder and downloaded zip
        FileUtils.deleteQuietly(new File(makeDirStr(WebMusicDownloader.DOWNLOAD_DIR, unzipFolderStr)));
        FileUtils.deleteQuietly(new File(makeDirStr(WebMusicDownloader.DOWNLOAD_DIR, filename)));

        for (String s: copiedFiles) {
            Log.d(TAG, "Files copied over: " + s);
        }

        return copiedFiles;
    }

    /**
     * Check if the extension is zip.
     * @param ext file extension
     * @return true if is, false otherwise
     */
    @Override
    public boolean checkExtension(String ext) {
        return ext.toLowerCase().equals("zip");
    }

    @Override
    public Context getContext() {
        return context;
    }

    /**
     * Unzip a zipfile to path/zipname.strip("zip")
     * unzipping adapted from https://stackoverflow.com/questions/3382996/how-to-unzip-files-programmatically-in-android
     * @param path root path of the zip file
     * @param zipname file name of the zip file
     * @return List of unzipped file paths.
     */
    public LinkedList<String> unpackZip(String path, String zipname) {

        // folder to unzip to
        String unzipFolder = zipname.replaceAll("\\.zip$", "");
        File unzipFolderFile = new File(makeDirStr(path, unzipFolder));
        unzipFolderFile.mkdirs();

        LinkedList<String> result = new LinkedList<>();

        InputStream is;
        ZipInputStream zis;

        try {
            String filename;
            is = new FileInputStream(makeDirStr(path, zipname));
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {

                filename = ze.getName();

                Log.d(TAG, "Unzipping " + filename);
                // Need to create directories if not exists, or
                // it will generate an Exception...
                if (ze.isDirectory()) {
                    File fmd = new File(makeDirStr(path, unzipFolder, filename));
                    fmd.mkdirs();
                    continue;
                }

                FileOutputStream fout = new FileOutputStream(makeDirStr(path, unzipFolder, filename));

                // cteni zipu a zapis
                while ((count = zis.read(buffer)) != -1) {
                    fout.write(buffer, 0, count);
                }

                fout.close();

                result.add(makeDirStr(unzipFolder, filename));
                Log.d(TAG, makeDirStr(unzipFolder, filename) + " extracted.");
            }

            zis.close();
        }
        catch(IOException e)
        {
            e.printStackTrace();
            return new LinkedList<>();
        }

        return result;
    }



}
