package csci310.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class EventTest {

    @Test
    public void testGetEventRating()
    {
        Vote v1 = new Vote(1, new User(1, "Sam"), CanAttend.YES, 5, false);
        Vote v2 = new Vote(2, new User(2, "Also Sam"), CanAttend.MAYBE, 2, false);
        Vote v3 = new Vote(3, new User(3, "Also also Sam"), CanAttend.NO, 100, false);
        Vote[] event1_votes = {v1, v2, v3};
        Event event1 = new Event(1, "tmKey1", event1_votes);
        assertEquals(event1.getEventRating(), 6);
    }

    @Test
    public void testGetNumYes()
    {
        Vote v1 = new Vote(1, new User(1, "Sam"), CanAttend.YES, 5, false);
        Vote v2 = new Vote(2, new User(2, "Also Sam"), CanAttend.MAYBE, 2, false);
        Vote v3 = new Vote(3, new User(3, "Also also Sam"), CanAttend.NO, 100, false);
        Vote[] event1_votes = {v1, v2, v3};
        Event event1 = new Event(1, "tmKey1", event1_votes);
        assertEquals(event1.getNumYes(), 1);
    }

    @Test
    public void testGetTotalEventScore()
    {
        Vote v1 = new Vote(1, new User(1, "Sam"), CanAttend.YES, 5, false);
        Vote v2 = new Vote(2, new User(2, "Also Sam"), CanAttend.MAYBE, 2, false);
        Vote v3 = new Vote(3, new User(3, "Also also Sam"), CanAttend.NO, 100, false);
        Vote[] event1_votes = {v1, v2, v3};
        Event event1 = new Event(1, "tmKey1", event1_votes);
        assertEquals(event1.getTotalEventScore(), 5);
    }

    @Test
    public void testGetNumMaybe()
    {
        Vote v1 = new Vote(1, new User(1, "Sam"), CanAttend.YES, 5, false);
        Vote v2 = new Vote(2, new User(2, "Also Sam"), CanAttend.MAYBE, 2, false);
        Vote v3 = new Vote(3, new User(3, "Also also Sam"), CanAttend.NO, 100, false);
        Vote[] event1_votes = {v1, v2, v3};
        Event event1 = new Event(1, "tmKey1", event1_votes);
        assertEquals(event1.getNumMaybe(), 1);
    }

}
