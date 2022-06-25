
package de.labystudio.spotifyapi.open.model.track;

import com.google.gson.annotations.SerializedName;

public class Artist {

    @SerializedName("external_urls")
    public ExternalUrls externalUrls;
    public String href;
    public String id;
    public String name;
    public String type;
    public String uri;

}
