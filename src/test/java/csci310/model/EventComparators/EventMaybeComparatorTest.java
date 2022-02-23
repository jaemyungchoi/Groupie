package csci310.model.EventComparators;

import csci310.model.CanAttend;
import csci310.model.Event;
import csci310.model.User;
import csci310.model.Vote;
import org.junit.Assert;
import org.junit.Test;

public class EventMaybeComparatorTest {

    @Test
    public void testCompare()
    {
        Vote v1 = new Vote(1, new User(1, "Sam"), CanAttend.YES, 5, false);
        Vote v2 = new Vote(2, new User(2, "Also Sam"), CanAttend.MAYBE, 2, false);
        Vote v3 = new Vote(3, new User(3, "Also also Sam"), CanAttend.NO, 100, false);
        Vote[] event1_votes = {v1, v2, v3};
        Event event1 = new Event(1, "tmKey1", event1_votes);
        Vote[] event2_votes = {v1, v3};
        Event event2 = new Event(2, "tmKey2", event2_votes);
        EventMaybeComparator c = new EventMaybeComparator();
        Assert.assertTrue(c.compare(event1, event2) > 0);
    }
}
