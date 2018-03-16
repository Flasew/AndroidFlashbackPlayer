package Tests;

import android.content.Context;
import android.os.Looper;
import android.support.test.rule.ActivityTestRule;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.util.LinkedList;

import edu.ucsd.team6flashbackplayer.DownloadedFileHandlerStrategy;
import edu.ucsd.team6flashbackplayer.MainActivity;
import edu.ucsd.team6flashbackplayer.WebMusicDownloader;

import static org.junit.Assert.*;

public class WebDownloaderTest {

    @Rule
    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);


    @Test
    public void testDownloadMp3() throws Exception {

        String url = "https://www.mfiles.co.uk/mp3-downloads/chopin-nocturne-op9-no2.mp3";
        String fname = "chopin-nocturne-op9-no2.mp3";

        WebMusicDownloader wdl = new WebMusicDownloader(new DownloadedFileHandlerStrategy() {
            @Override
            public boolean checkExtension(String extension) {
                return extension.toLowerCase().equals("mp3");
            }
            @Override
            public Context getContext() {
                return mainActivity.getActivity();
            }
            @Override
            public LinkedList<String> process(String url, String filename) {
                return null;
            }
        });

        wdl.downloadFromUrl(url);

        // should be fast enough... file is small
        Thread.sleep(5000);

        File file = new File(WebMusicDownloader.DOWNLOAD_DIR + "/" + fname);
        assertTrue(file.exists());

    }

    @Test
    public void testDownloadNonMp3() throws Exception {
        String url = "http://cseweb.ucsd.edu/classes/wi18/cse140-a/lectures/sec5.pdf";
        String fname = "sec5.pdf";

        try {
            WebMusicDownloader wdl = new WebMusicDownloader(new DownloadedFileHandlerStrategy() {
                @Override
                public boolean checkExtension(String extension) {
                    return extension.toLowerCase().equals("mp3");
                }

                @Override
                public Context getContext() {
                    return mainActivity.getActivity();
                }

                @Override
                public LinkedList<String> process(String url, String filename) {
                    return null;
                }
            });

            wdl.downloadFromUrl(url);

            // should be fast enough... file is small
            Thread.sleep(5000);
            File file = new File(WebMusicDownloader.DOWNLOAD_DIR + "/" + fname);
            assertTrue(file.exists());

            // fail();
        }
        catch (RuntimeException e) {
            File file = new File(WebMusicDownloader.DOWNLOAD_DIR + "/" + fname);
            assertTrue(file.exists());
        }

    }

    @Test
    public void testDownloadZip() throws Exception {

        String url = "https://d1b10bmlvqabco.cloudfront.net/attach/jc2fhqnhbwl4ii/j85f4pwtei5258/jdhvwzg7m0f5/Take_Yourself_Too_Seriously.zip";
        String fname = "Take_Yourself_Too_Seriously.zip";

        WebMusicDownloader wdl = new WebMusicDownloader(new DownloadedFileHandlerStrategy() {
            @Override
            public boolean checkExtension(String extension) {
                return extension.toLowerCase().equals("zip");
            }
            @Override
            public Context getContext() {
                return mainActivity.getActivity();
            }
            @Override
            public LinkedList<String> process(String url, String filename) {
                return null;
            }
        });

        wdl.downloadFromUrl(url);

        // should be fast enough... file is small
        Thread.sleep(8000);

        File file = new File(WebMusicDownloader.DOWNLOAD_DIR + "/" + fname);
        assertTrue(file.exists());

    }

    @Test
    public void testDownloadInvalid() throws Exception {

        String url = "lol:!!code.tutsplus.com/tutorials/essential-textmate-shortcuts-tips-and-techniques--net-21168";
        String fname = "essential-textmate-shortcuts-tips-and-techniques--net-21168.html";

        try {
            WebMusicDownloader wdl = new WebMusicDownloader(new DownloadedFileHandlerStrategy() {
                @Override
                public boolean checkExtension(String extension) {
                    return extension.toLowerCase().equals("mp3");
                }

                @Override
                public Context getContext() {
                    return mainActivity.getActivity();
                }

                @Override
                public LinkedList<String> process(String url, String filename) {
                    return null;
                }
            });

            wdl.downloadFromUrl(url);

            // should be fast enough... file is small
            Thread.sleep(5000);

            File file = new File(WebMusicDownloader.DOWNLOAD_DIR + "/" + fname);
            assertFalse(file.exists());

        } catch (RuntimeException e) {

            File file = new File(WebMusicDownloader.DOWNLOAD_DIR + "/" + fname);
            assertFalse(file.exists());
        }

    }

    @Test
    public void testDownloadBadURL() throws Exception {


        String url = "sftp://wew168@ieng6.ucsd.edu/~/.bashrc";
        String fname = ".bashrc";

        try {

            WebMusicDownloader wdl = new WebMusicDownloader(new DownloadedFileHandlerStrategy() {
                @Override
                public boolean checkExtension(String extension) {
                    return true;
                }

                @Override
                public Context getContext() {
                    return mainActivity.getActivity();
                }

                @Override
                public LinkedList<String> process(String url, String filename) {
                    return null;
                }
            });

            wdl.downloadFromUrl(url);

            // should be fast enough... file is small
            Thread.sleep(5000);
            File file = new File(WebMusicDownloader.DOWNLOAD_DIR + "/" + fname);
            assertFalse(file.exists());

        } catch (RuntimeException e) {

            File file = new File(WebMusicDownloader.DOWNLOAD_DIR + "/" + fname);
            assertFalse(file.exists());
        }

    }

    @Test
    public void testDownloadImplicit() {


        String url = "https://yun.davidzz.me/index.php/s/5RjFh3jz9ENUIsn/download";
        String fname = "clannad.zip";

        try {

            WebMusicDownloader wdl = new WebMusicDownloader(new DownloadedFileHandlerStrategy() {
                @Override
                public boolean checkExtension(String extension) {
                    return true;
                }

                @Override
                public Context getContext() {
                    return mainActivity.getActivity();
                }

                @Override
                public LinkedList<String> process(String url, String filename) {
                    return null;
                }
            });

            wdl.downloadFromUrl(url);

            // should be fast enough... file is small
            Thread.sleep(10000);
            File file = new File(WebMusicDownloader.DOWNLOAD_DIR + "/" + fname);
            assertTrue(file.exists());

        } catch (RuntimeException | InterruptedException e) {

        }

    }

    @After
    public void clearDownload() {
        File file = new File(WebMusicDownloader.DOWNLOAD_DIR);
        String[] files;
        files = file.list();
        for (int i=0; i<files.length; i++) {
            File myFile = new File(file, files[i]);
            FileUtils.deleteQuietly(myFile);
        }
    }

}
