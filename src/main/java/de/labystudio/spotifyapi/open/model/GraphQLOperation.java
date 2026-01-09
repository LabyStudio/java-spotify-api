package de.labystudio.spotifyapi.open.model;

/**
 * Represents a GraphQL operation with its persisted query hash.
 * Spotify uses persisted queries (APQ) where operations are identified by their SHA-256 hash.
 */
public class GraphQLOperation {

    public static final GraphQLOperation GET_TRACK = new GraphQLOperation(
            "getTrack",
            "612585ae06ba435ad26369870deaae23b5c8800a256cd8a57e08eddc25a37294"
    );

    private final String operationName;
    private final String sha256Hash;

    public GraphQLOperation(String operationName, String sha256Hash) {
        this.operationName = operationName;
        this.sha256Hash = sha256Hash;
    }

    public String getOperationName() {
        return this.operationName;
    }

    public String getSha256Hash() {
        return this.sha256Hash;
    }
}

