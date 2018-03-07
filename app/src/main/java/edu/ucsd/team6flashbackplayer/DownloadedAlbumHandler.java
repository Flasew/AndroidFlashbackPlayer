package edu.ucsd.team6flashbackplayer;

/**
 * Created by frankwang on 3/6/18.
 */

import android.content.Context;

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

        String unzipFolderStr = filename.replaceAll(".zip$", "");
        File albumFolder = new File(makeDirStr(MusicPlayerActivity.MUSIC_DIR, unzipFolderStr));
        albumFolder.mkdirs();

        LinkedList<String> unzippedFiles = unpackZip(WebMusicDownloader.DOWNLOAD_DIR, filename);
        LinkedList<String> copiedFiles = new LinkedList<>();

        // copy over the files.
        for (String songFilename: unzippedFiles) {
            File fileInMusicDir = new File(makeDirStr(MusicPlayerActivity.MUSIC_DIR, songFilename));
            File fileInDownloadDir = new File(makeDirStr(WebMusicDownloader.DOWNLOAD_DIR, songFilename));

            // if the same file (or at least with the same name) already exist in music dir,
            // ignore this file.
            // otherwise move the file
            if (!fileInMusicDir.exists()) {
                if (fileInDownloadDir.renameTo(fileInMusicDir)) {
                    copiedFiles.add(makeDirStr(songFilename));
                }
            }
        }

        // delete the unzipped folder
        (new File(makeDirStr(WebMusicDownloader.DOWNLOAD_DIR, unzipFolderStr))).delete();

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
     * @param path root path of the zip file
     * @param zipname file name of the zip file
     * @return List of unzipped file paths.
     */
    private LinkedList<String> unpackZip(String path, String zipname) {

        // folder to unzip to
        String unzipFolder = zipname.replaceAll(".zip$", "");
        LinkedList<String> result = new LinkedList<>();

        InputStream is;
        ZipInputStream zis;

        try {
            String filename;
            is = new FileInputStream(path + zipname);
            zis = new ZipInputStream(new BufferedInputStream(is));
            ZipEntry ze;
            byte[] buffer = new byte[1024];
            int count;

            while ((ze = zis.getNextEntry()) != null) {
                // zapis do souboru
                filename = ze.getName();

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
                zis.closeEntry();

                result.add(makeDirStr(unzipFolder, filename));
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
