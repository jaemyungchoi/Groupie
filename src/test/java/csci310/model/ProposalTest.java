package csci310.model;

import org.junit.Test;

import static org.junit.Assert.*;

public class ProposalTest {

    @Test
    public void testCanAccess() {
        User owner = new User(1, "owner");
        User user1 = new User(2, "user1");
        User user2 = new User(3, "user2");
        Proposal proposal = new Proposal(1, owner, "test", new Event[0], new User[]{user1}, null, new User[0], new User[0], new User[0], null);
        assertTrue(proposal.canAccess(owner.getId()));
        assertTrue(proposal.canAccess(user1.getId()));
        assertFalse(proposal.canAccess(user2.getId()));
    }

    @Test
    public void testHasEvent() {
        User owner = new User(1, "owner");
        Event event1 = new Event(1, "tmKey1", new Vote[0]);
        Event event2 = new Event(2, "tmKey2", new Vote[0]);
        Proposal proposal = new Proposal(1, owner, "test", new Event[]{event1}, new User[]{owner}, null, new User[0], new User[0], new User[0], null);
        assertTrue(proposal.hasEvent(event1.getId()));
        assertFalse(proposal.hasEvent(event2.getId()));
    }

    @Test
    public void testGetBestEvent()
    {
        Vote v1 = new Vote(1, new User(1, "Sam"), CanAttend.YES, 5, false);
        Vote v2 = new Vote(2, new User(2, "Also Sam"), CanAttend.MAYBE, 2, false);
        Vote v3 = new Vote(3, new User(3, "Also also Sam"), CanAttend.NO, 5, true);
        Vote v4 = new Vote(4, new User(4, "Sam #4 (this is getting out of hand)"), CanAttend.YES, 4, false);
        Vote v5 = new Vote(5, new User(5, "Sam #5 (this is getting very out of hand)"), CanAttend.YES, 3, false);

        // TEST #1: Get best event based on # yes votes
        Vote[] event1_votes = {v1, v3};
        Event event1 = new Event(1, "tmKey1", event1_votes);
        Vote[] event2_votes = {v1, v4};
        Event event2 = new Event(2, "tmKey2", event2_votes);
        Proposal p1  = new Proposal(1, new User(1, "Sam"), "event test", new Event[]{event1, event2}, new User[]{new User(1, "Sam")}, null, new User[0], new User[0], new User[0], null);
        // Event 2 should be chosen
        assertEquals(event2.getId(), p1.getBestEvent().getId());

        // TEST #2: Get best event based on excitement score
        Vote[] event3_votes = {v1, v5, v3};
        Event event3 = new Event(3, "tmKey3", event3_votes);
        Proposal p2  = new Proposal(2, new User(1, "Sam"), "event test", new Event[]{event2, event3}, new User[]{new User(1, "Sam")}, null, new User[0], new User[0], new User[0], null);
        // Event 2 should be chosen
        assertEquals(event2.getId(), p2.getBestEvent().getId());

        // TEST #3: Get best event based on # maybe
        Vote[] event4_votes = {v1, v5, v2, v3};
        Event event4 = new Event(4, "tmKey4", event4_votes);
        Proposal p3  = new Proposal(3, new User(1, "Sam"), "event test", new Event[]{event3, event4}, new User[]{new User(1, "Sam")}, null, new User[0], new User[0], new User[0], null);
        // Event 4 should be chosen
        assertEquals(event4.getId(), p3.getBestEvent().getId());

        // TEST #4: In case of tie in other categories, just make sure an event is returned
        Event event5 = new Event(5, "tmKey5", event4_votes);
        Proposal p4  = new Proposal(4, new User(1, "Sam"), "event test", new Event[]{event4, event5}, new User[]{new User(1, "Sam")}, null, new User[0], new User[0], new User[0], null);
        assertNotNull(p4.getBestEvent());
    }

    @Test
    public void testGetBestEventNoEvents()
    {
        // No event proposal - no event should be reported as the best event
        Proposal p  = new Proposal(1, new User(1, "Sam"), "event test", new Event[]{}, new User[]{new User(1, "Sam")}, null, new User[0], new User[0], new User[0], null);
        assertNull(p.getBestEvent());
    }

    @Test
    public void testConstructor()
    {
        Proposal p  = new Proposal(1, null, "event test", new Event[]{}, new User[]{new User(1, "Sam")}, null, new User[0], new User[0], new User[0], null);
        assertNull(p.getPendingUsers());
    }

    @Test
    public void testConstructor2()
    {
        Proposal p  = new Proposal(1, new User(1, "Sam"), "event test", new Event[]{}, null, null, new User[0], new User[0], new User[0], null);
        assertNull(p.getPendingUsers());
    }

    @Test
    public void testConstructor3()
    {
        Proposal p  = new Proposal(1, new User(1, "Sam"), "event test", new Event[]{}, new User[]{new User(1, "Sam")}, null, null, new User[0], new User[0], null);
        assertNull(p.getPendingUsers());
    }

    @Test
    public void testConstructor4()
    {
        Proposal p  = new Proposal(1, new User(1, "Sam"), "event test", new Event[]{}, new User[]{new User(1, "Sam")}, null, new User[0], null, new User[0], null);
        assertNull(p.getPendingUsers());
    }
}