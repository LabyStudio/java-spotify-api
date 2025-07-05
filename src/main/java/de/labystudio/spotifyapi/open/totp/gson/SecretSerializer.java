package de.labystudio.spotifyapi.open.totp.gson;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import de.labystudio.spotifyapi.open.totp.model.Secret;

import java.lang.reflect.Type;

/**
 * This class is used to serialize the TOTP secret to Spotify's TOTP storage format.
 *
 * @author LabyStudio
 */
public class SecretSerializer implements JsonSerializer<Secret> {

    @Override
    public JsonElement serialize(Secret secret, Type type, JsonSerializationContext context) {
        JsonObject obj = new JsonObject();
        obj.addProperty("version", secret.getVersion());
        obj.addProperty("secret", secret.getSecretAsString());
        return obj;
    }
}
