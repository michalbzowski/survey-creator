package pl.bzowski.events.web;

public class EventDetails {

    public static class Stats {
        public final String tag;
        public final Long yes;
        public final Long no;
        public final Long later;

        public Stats(String tag, Long yes, Long no, Long later) {
            this.tag = tag;
            this.yes = yes;
            this.no = no;
            this.later = later;
        }
    }
}
