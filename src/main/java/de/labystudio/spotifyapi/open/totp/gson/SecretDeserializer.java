package de.labystudio.spotifyapi.open.totp.gson;

import com.google.gson.*;
import de.labystudio.spotifyapi.open.totp.model.Secret;

import java.lang.reflect.Type;

/**
 * This class is used to deserialize the TOTP secret from Spotify's TOTP storage format.
 *
 * @author LabyStudio
 */
public class SecretDeserializer implements JsonDeserializer<Secret> {

    @Override
    public Secret deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject obj = json.getAsJsonObject();

        int version = obj.get("version").getAsInt();
        JsonElement secret = obj.get("secret");

        // Check if the secret is an integer array
        if (secret.isJsonArray()) {
            JsonArray array = secret.getAsJsonArray();
            int[] numbers = new int[array.size()];
            for (int i = 0; i < array.size(); i++) {
                numbers[i] = array.get(i).getAsInt();
            }
            return Secret.fromNumbers(numbers, version);
        }

        // Check if the secret is a string
        if (secret.isJsonPrimitive() && secret.getAsJsonPrimitive().isString()) {
            String secretString = secret.getAsString();
            return Secret.fromString(secretString, version);
        }

        throw new JsonParseException("Invalid secret format: " + secret);
    }
}
