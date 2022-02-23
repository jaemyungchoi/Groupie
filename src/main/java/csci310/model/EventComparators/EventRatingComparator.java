package csci310.model.EventComparators;

import csci310.model.Event;

import java.util.Comparator;

public class EventRatingComparator implements Comparator<Event>
{
    public int compare(Event o1, Event o2)
    {
        return Integer.compare(o1.getTotalEventScore(), o2.getTotalEventScore());
    }
}
