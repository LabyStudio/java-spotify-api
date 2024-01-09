import de.labystudio.spotifyapi.platform.linux.api.Variant;

public class SpotifyDBusParserTest {

    public static void main(String[] args) {
        String metadata = "   variant       array [\n" +
                "         dict entry(\n" +
                "            string \"mpris:trackid\"\n" +
                "            variant                string \"/com/spotify/track/0r1kH7SIkkPP9W7mUknObF\"\n" +
                "         )\n" +
                "         dict entry(\n" +
                "            string \"mpris:length\"\n" +
                "            variant                uint64 172000000\n" +
                "         )\n" +
                "         dict entry(\n" +
                "            string \"mpris:artUrl\"\n" +
                "            variant                string \"https://i.scdn.co/image/ab67616d0000b27397c097afa44e5cdb38a03d4f\"\n" +
                "         )\n" +
                "         dict entry(\n" +
                "            string \"xesam:album\"\n" +
                "            variant                string \"Raop\"\n" +
                "         )\n" +
                "         dict entry(\n" +
                "            string \"xesam:albumArtist\"\n" +
                "            variant                array [\n" +
                "                  string \"CRO\"\n" +
                "               ]\n" +
                "         )\n" +
                "         dict entry(\n" +
                "            string \"xesam:artist\"\n" +
                "            variant                array [\n" +
                "                  string \"CRO\"\n" +
                "               ]\n" +
                "         )\n" +
                "         dict entry(\n" +
                "            string \"xesam:autoRating\"\n" +
                "            variant                double 0.01\n" +
                "         )\n" +
                "         dict entry(\n" +
                "            string \"xesam:discNumber\"\n" +
                "            variant                int32 1\n" +
                "         )\n" +
                "         dict entry(\n" +
                "            string \"xesam:title\"\n" +
                "            variant                string \"Easy\"\n" +
                "         )\n" +
                "         dict entry(\n" +
                "            string \"xesam:trackNumber\"\n" +
                "            variant                int32 3\n" +
                "         )\n" +
                "         dict entry(\n" +
                "            string \"xesam:url\"\n" +
                "            variant                string \"https://open.spotify.com/track/0r1kH7SIkkPP9W7mUknObF\"\n" +
                "         )\n" +
                "      ]";

        String playing = "   variant       string \"Playing\"";

        Variant response = Variant.parse(metadata);
        for (Variant entry : response.<Variant[]>getValue()) {
            System.out.println(entry.getSig() + "|" + entry.getValue());
        }

        Variant response2 = Variant.parse(playing);
        if (!response2.getSig().equals("variant")) {
            throw new IllegalStateException("Invalid sig key: " + response2.getSig());
        }
        if (!response2.<String>getValue().equals("Playing")) {
            throw new IllegalStateException("Invalid value: " + response2.<String>getValue());
        }
    }

}
