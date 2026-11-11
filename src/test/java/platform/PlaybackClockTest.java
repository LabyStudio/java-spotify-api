package platform;

import de.labystudio.spotifyapi.platform.PlaybackClock;

public class PlaybackClockTest {

    private static long nanos = 1_000_000_000L;

    public static void main(String[] args) {
        PlaybackClock clock = new PlaybackClock(() -> nanos);

        check(!clock.hasPosition(), "unknown at start");
        check(clock.getPosition() == -1, "unknown position is -1");

        check(clock.update(10_000, nanos, true), "first update reports");
        advance(500);
        check(clock.getPosition() == 10_500, "interpolates while playing, got " + clock.getPosition());

        // sampled 20ms ago, query jitter of +20ms -> nudged by a quarter, no jump
        check(!clock.update(10_500, nanos - ms(20), true), "jitter does not report");
        check(clock.getPosition() == 10_505, "jitter nudges by a quarter, got " + clock.getPosition());

        // 200ms off -> silent re-anchor
        check(!clock.update(10_300, nanos, true), "resync does not report");
        check(clock.getPosition() == 10_300, "resync re-anchors, got " + clock.getPosition());

        check(clock.update(60_000, nanos, true), "seek reports");
        advance(250);
        check(clock.getPosition() == 60_250, "seek re-anchors, got " + clock.getPosition());

        // pause noticed 400ms late, the measured position wins
        check(!clock.update(59_850, nanos, false), "pause within the seek threshold does not report");
        advance(3000);
        check(clock.getPosition() == 59_850, "paused holds, got " + clock.getPosition());

        check(!clock.update(59_850, nanos, true), "resume does not report");
        advance(100);
        check(clock.getPosition() == 59_950, "resume interpolates, got " + clock.getPosition());

        clock.resync();
        check(clock.update(59_960, nanos, true), "update after resync reports");

        clock.setLength(60_000);
        advance(5000);
        check(clock.getPosition() == 60_000, "clamped to the track length, got " + clock.getPosition());

        clock.reset();
        check(!clock.hasPosition(), "reset forgets the position");

        System.out.println("PlaybackClockTest passed");
    }

    private static void advance(long millis) {
        nanos += ms(millis);
    }

    private static long ms(long millis) {
        return millis * 1_000_000L;
    }

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new IllegalStateException(message);
        }
    }
}
