# Android Imgur API Test
Android app that connects to the Imgur API and manages a user's images

## Getting Started
1. [Register](https://api.imgur.com/oauth2/addclient) an Imgur API Application if you don't have one.
    * You should create your application with the "OAuth 2 authorization with a callback URL" Authorization type.
    * Setup an Authorization callback URL.  If possible, this should be a URL for a domain you control.  If not, using
    https://imgur.com/ or https://www.example.com should be fine.  The page doesn't have to do anything special as the
    app will catch the request and grab the access token from it.
2. Download or Clone this repository.
3. Make a copy of the [`ConstantsSample`](https://github.com/jimstump/android-imgur-api-test/blob/master/app/src/main/java/us/stump/imgurapitest/ConstantsSample.java)
Class and rename/refactor it to `Constants`.  Then update the `getStoredSecret` method to return the appropriate values
for your Imgur API Application.
4. Build and enjoy!

## Features
* Login to Imgur
* View all images associated with your Imgur account
* Upload new images to your Imgur account
* Delete images from your Imgur account

## Acknowledgements
* Icons
	* [Logout](https://materialdesignicons.com/icon/logout) by Austin Andrews
	* [Add a Photo](https://material.io/icons/#ic_add_a_photo) by material.io
* Libraries
	* [Retrofit](https://square.github.io/retrofit/) by Square
	* [GSON](https://github.com/google/gson) by Google
	* [Glide](https://github.com/bumptech/glide)
