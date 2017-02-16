package us.stump.imgurapitest;

/**
 * This is a "Sample" version of the Constants class.
 * Copy this "Constants.java" and rename the class to Constants.
 * Then implement getStoredSecret();
 *
 * "Static" Class to hold our constants/secrets
 *
 * Unfortunately, there isn't a great way to protect secrets in Android.
 * This class will abstract the actual storage of the secrets away from the rest of the application.
 * We'll
 */
final class ConstantsSample {
    public static final String SECRET_IMGUR_APP_ID = "imgurAppId";
    public static final String SECRET_IMGUR_APP_SECRET = "imgurAppSecret";
    public static final String SECRET_IMGUR_APP_CALLBACK_URL = "imgurAppCallbackUrl";

    /**
     * There is no reason to ever call this.
     *
     * @throws RuntimeException Always.
     */
    private ConstantsSample() {
        throw new RuntimeException("Constants is meant to be a static class");
    }

    /**
     * Returns the value of the given secret.
     *
     * @param secretName
     * @return
     */
    public static String getStoredSecret(String secretName)
    {
        switch (secretName)
        {
            case SECRET_IMGUR_APP_ID:
                return "";

            case SECRET_IMGUR_APP_SECRET:
                return "";

            case SECRET_IMGUR_APP_CALLBACK_URL:
                return "";
        }

        throw new IllegalArgumentException("Unknown secret '"+secretName+"'");
    }
}
