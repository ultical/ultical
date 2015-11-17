package de.ultical.backend.data;

import java.util.Comparator;

import de.ultical.backend.model.Event;

class EventDateComparator implements Comparator<Event> {

	public int compare(Event arg0, Event arg1) {
		if (arg0 == null || arg0.getStartDate() == null) {
			return -1;
		} if (arg1 == null || arg1.getStartDate() == null) {
			return 1;
		}
		return arg0.getStartDate().compareTo(arg1.getStartDate());
	}

}
