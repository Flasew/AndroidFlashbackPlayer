package edu.ucsd.team6flashbackplayer;

import com.google.android.gms.maps.model.LatLng;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.HashSet;

/**
 * Created by alice on 2/18/18.
 */

public class JUnitJson {

    Song song1;
    String jsonString1;
    ZoneId zone;

    Song song2;
    String jsonString2;


    @Before
    public void initialize() {
        song1 = new Song("song1-id", "A", "A", "A");
        jsonString1 = "{\"Path\":\"sunrise-id\",\"Title\":\"Sunrise\",\"Album\":\"Feels Like Home\",\"Artist\":\"Norah Jones\",\"LatestLocation\":[-39.3484,175.376],\"LatestTime\":[2018,2,19,1,53,35,448000000],\"Liked\":true,\"Disliked\":false,\"TimeOfDay\":[false,false,true],\"DayOfWeek\":[false,false,false,false,false,false,true,true],\"LocationHistory\":[[-39.3484,175.376],[16.816431,-4.876562]]}";
        zone = ZoneId.systemDefault();

        song2 = new Song("www.google.com","song1","Song One", "A", "A", "123");
        jsonString2 = "{\"Path\":\"song1\",\"Title\":\"Song One\",\"Album\":\"A\",\"Artist\":\"A\",\"Url\":\"www.google.com\",\"Id\":\"123\",\"LastPlayedUserUid\":\"---\",\"LatestLocation\":[-39.3484,175.376],\"LocationHistory\":[[-39.3484,175.376],[16.816431,-4.876562]],\"LatestTime\":[2018,2,19,1,53,35,448000000],\"Liked\":false,\"Disliked\":false}";
    }

    /**
     * Tests the jsonParse method for when a song object is created for the first time
     */
    @Test
    public void testInitJsonParse() {
        String jsonParsed = SongJsonParser.jsonParse(song1);
        String correctJson = "{\"Path\":\"song1-id\",\"Title\":\"A\",\"Album\":\"A\",\"Artist\":\"A\",\"LatestLocation\":[1000],\"LatestTime\":[10000],\"Liked\":false,\"Disliked\":false,\"TimeOfDay\":[false,false,false],\"DayOfWeek\":[false,false,false,false,false,false,false,false],\"LocationHistory\":[1000]}";
        Assert.assertEquals(correctJson,jsonParsed);
        // The json string of the Song object should be set correctly to the string above on the constructor
        Assert.assertEquals(correctJson,song1.getJsonString());
    }

    /**
     *  Given a json string test if the object's fields are all populated correctly
     */
    @Test
    public void testJsonPopulate() {
        SongJsonParser.jsonPopulate(song1, jsonString1);

        // Check the fields - these stay the same as the original
        Assert.assertEquals("song1-id",song1.getPath());
        Assert.assertEquals("A", song1.getTitle());
        Assert.assertEquals("A", song1.getArtist());
        Assert.assertEquals("A", song1.getAlbum());

        // Check the fields that are supposed to have changed
        ZonedDateTime correctLatestTime = ZonedDateTime.of(2018,2,19,1,53,35,448000000,zone);
        Assert.assertEquals(correctLatestTime,song1.getLatestTime());
        // Latest Location
        LatLng correctLatLng = new LatLng(-39.3484,175.376);
        Assert.assertEquals(correctLatLng, song1.getLatestLoc());
        //Set of Locations
        HashSet<LatLng> correctSet = new HashSet<LatLng>();
        LatLng loc1 = new LatLng(-39.3484,175.376);
        LatLng loc2 = new LatLng(16.816431,-4.876562);
        correctSet.add(loc1);
        correctSet.add(loc2);
        Assert.assertEquals(correctSet, song1.getLocHist());

        Assert.assertEquals(true, song1.isLiked());
        Assert.assertEquals(false, song1.isDisliked());

        Assert.assertArrayEquals(new boolean[] {false,false,true}, song1.getTimeHist());
        Assert.assertArrayEquals(new boolean[] {false,false,false,false,false,false,true,true}, song1.getDayHist());
    }

    @Test
    public void testUpdateLocTime() {
        ZonedDateTime newTime = ZonedDateTime.of(2017, 2, 12, 3, 12, 32, 555000000, zone);
        LatLng newLocation = new LatLng(-3.862823, -67.982031);
        SongJsonParser.updateSongLocTime(song1, newTime, newLocation);

        boolean[] correct = {false, false, false};
        int time = Song.timeOfDay(3);
        correct[time] = true;

        boolean[] correctDay = {false, false, false, false, false, false, false, false};
        int dayOfWeek = newTime.getDayOfWeek().getValue();
        correctDay[dayOfWeek] = true;

        Assert.assertEquals(newTime, song1.getLatestTime());
        Assert.assertEquals(newLocation, song1.getLatestLoc());

        // Check the dayHist and timeHist arrays
        Assert.assertArrayEquals(correct, song1.getTimeHist());
        Assert.assertArrayEquals(correctDay, song1.getDayHist());
    }

    @Test
    public void testRefreshJson() {
        //If you just set the values seperately it won't refresh the json string of the song
        song1.setLike(false);
        song1.setDislike(true);

        String newJson = SongJsonParser.jsonParse(song1);
        // Check that the json is refreshed after the call to refresh is made only
        Assert.assertNotEquals(newJson, song1.getJsonString());

        SongJsonParser.refreshJson(song1);
        Assert.assertEquals(newJson, song1.getJsonString());
    }

    @Test
    public void testInitJsonParseFirebase() {
        String jsonParsed = SongJsonParser.jsonParseFirebase(song2);
        String correctJson = "{\"Path\":\"song1\",\"Title\":\"Song One\",\"Album\":\"A\",\"Artist\":\"A\",\"Url\":\"www.google.com\",\"Id\":\"123\",\"LastPlayedUserUid\":\"---\",\"LatestLocation\":[1000],\"LocationHistory\":[1000],\"LatestTime\":[10000],\"Liked\":false,\"Disliked\":false}";
        Assert.assertEquals(correctJson,jsonParsed);
        // The json string of the Song object should be set correctly to the string above on the constructor
        Assert.assertEquals(correctJson,song2.getJsonString());
    }

    @Test
    public void testJsonPopulateFirebase() {
        Song songTest = SongJsonParser.jsonPopulateFromFirebase(jsonString2);

        // Check all the fields of the song generated from the json string
        Assert.assertEquals("song1",songTest.getPath());
        Assert.assertEquals("Song One", songTest.getTitle());
        Assert.assertEquals("A", songTest.getArtist());
        Assert.assertEquals("A", songTest.getAlbum());
        Assert.assertEquals("123", songTest.getId());
        Assert.assertEquals("www.google.com", songTest.getUrl());
        Assert.assertEquals("---", songTest.getLastPlayedUserUid()); // last played user

        // Location and time history
        ZonedDateTime correctLatestTime = ZonedDateTime.of(2018,2,19,1,53,35,448000000,zone);
        Assert.assertEquals(correctLatestTime,songTest.getLatestTime());
        // Latest Location
        LatLng correctLatLng = new LatLng(-39.3484,175.376);
        Assert.assertEquals(correctLatLng, songTest.getLatestLoc());
        //Set of Locations
        HashSet<LatLng> correctSet = new HashSet<LatLng>();
        LatLng loc1 = new LatLng(-39.3484,175.376);
        LatLng loc2 = new LatLng(16.816431,-4.876562);
        correctSet.add(loc1);
        correctSet.add(loc2);
        Assert.assertEquals(correctSet, songTest.getLocHist());

        // like and dislike don't matter here but can check anyways
        Assert.assertEquals(false, songTest.isLiked());
        Assert.assertEquals(false, songTest.isDisliked());

    }


}
