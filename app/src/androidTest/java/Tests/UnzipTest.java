package Tests;

import android.content.Context;
import android.os.Environment;
import android.os.Looper;
import android.support.test.rule.ActivityTestRule;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import edu.ucsd.team6flashbackplayer.ControlButtons;
import edu.ucsd.team6flashbackplayer.DownloadedAlbumHandler;
import edu.ucsd.team6flashbackplayer.DownloadedFileHandlerStrategy;
import edu.ucsd.team6flashbackplayer.MainActivity;
import edu.ucsd.team6flashbackplayer.WebMusicDownloader;

import static org.junit.Assert.*;


/**
 * Unit test for unzip in downloaded Album.
 */

public class UnzipTest {

    private static String TEST_DIR = Environment.getExternalStorageDirectory().getAbsolutePath() + "/Test";

    private static final List<String> TEST_FILES = Arrays.asList(
            "chopin-nocturne-op9-no2.mp3",
            "Homeward_Bound.mp3",
            "phys_hw.zip",
            "Take_Yourself_Too_Seriously.zip",
            "take_yourself_nested.zip"
    );

    @Rule
    public ActivityTestRule<MainActivity> mainActivity = new ActivityTestRule<MainActivity>(MainActivity.class);


    public void test(String filename, List<String> expected) {

        DownloadedAlbumHandler handler = new DownloadedAlbumHandler(mainActivity.getActivity());
        String folderName = filename.replaceAll("\\.zip$", "");

        LinkedList<String> result = handler.unpackZip(TEST_DIR, filename);

        File extractDir = new File(TEST_DIR, folderName);;
        assertTrue(extractDir.exists());

        for (String s: result) {
            System.err.println(s);
        }

        for (String s: expected) {

            assertTrue(result.indexOf(folderName + "/" +s) != -1);
        }
    }

    @Test
    public void testPureMp3FileZipped() {

        List<String> expected = Arrays.asList(
                "dead-dove-do-not-eat.mp3",
                "sisters-of-the-sun.mp3",
                "dreamatorium.mp3",
                "sky-full-of-ghosts.mp3",
                "i-just-want-to-tell-you-both-good-luck.mp3",
                "windows-are-the-eyes-to-the-house.mp3"
        );

        test("Take_Yourself_Too_Seriously.zip", expected);
    }

    @Test
    public void testNonMp3FilesZipped() {
        List<String> expected = Arrays.asList(
                "08-HW5.pdf", "12-HW8_with_attach.pdf",
                "04-HW2_PlusFigures.pdf", "10-HW6.pdf",
                "05-HW3.pdf", "11-HW7.pdf", "12-HW8_with_attach.pdf"
        );

        test("phys_hw.zip", expected);
    }

    @Test
    public void testNestedMp3File() {
        List<String> expected = Arrays.asList(
                "nested/dead-dove-do-not-eat.mp3",
                "nested/sisters-of-the-sun.mp3",
                "nested/dreamatorium.mp3",
                "nested/sky-full-of-ghosts.mp3",
                "nested/i-just-want-to-tell-you-both-good-luck.mp3",
                "nested/windows-are-the-eyes-to-the-house.mp3"
        );

        test("take_yourself_nested.zip", expected);
    }

    @After
    public void clearTestDir() {
        System.err.println("Cleaning directory");
        File testDir = new File(TEST_DIR);
        File[] list = testDir.listFiles();
        for (File f: list) {
            System.err.println(f.getName());
            if (TEST_FILES.indexOf(f.getName()) == -1) {
                System.err.println("Should be deleted.");
                FileUtils.deleteQuietly(f);
            }

        }
    }

}
