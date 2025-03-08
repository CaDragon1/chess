package server;

import Models.AuthTokenData;
import Models.GameData;
import Models.UserData;
import com.google.gson.JsonSyntaxException;
import service.Service;
import spark.*;
import com.google.gson.Gson;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

public class Server {

    // Create a single Gson object for all Gson operations
    private final Gson gson = new Gson();
    private final Service service;

    public Server() {
        service = new Service();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::registerUser);
        Spark.post("/session", this::loginUser);
        Spark.delete("/session", this::logoutUser);
        Spark.get("/game", this::listGame);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.delete("/db", this::clearDatabase);

        Spark.exception(ServerException.class, this::handleException);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }


    /**
     * registerUser takes the request JSON object, then makes it usable for Java. It then registers the user
     * into the server's database.
     * @param request is the JSON request to register a user. Contains username, password, and email.
     * @param response is the resulting response JSON object along with the serialized return information
     * @return the AuthTokenData object, serialized as a JSON object.
     * @throws ServerException
     */
    private String registerUser(Request request, Response response) throws ServerException {
        try {
            // Store the user data from the request
            UserData submittedUser = new Gson().fromJson(request.body(), UserData.class);

            // Trim the username
            String trimmedUsername = submittedUser.username().trim();
            UserData user = new UserData(trimmedUsername, submittedUser.password(), submittedUser.email());

            // Verify inputs
            if (!validateUsername(user.username()) || !validatePassword(user.password()) || !validateEmail(user.email())) {
                throw new ServerException("bad request", 400);
            }

            // Register user data
            else {
                AuthTokenData authToken = service.register(user);

                response.status(200);
                return gson.toJson(authToken);
            }

        // Catch exception from bad request
        } catch (JsonSyntaxException e) {
            throw new ServerException("bad request", 400);
        }
    }

    /**
     * loginUser will attempt to log in the user given a username and password.
     * @param request is the JSON request to register a user. Contains username and password.
     * @param response is the resulting response JSON object along with the serialized return information
     * @return the AuthTokenData object, serialized as a JSON object.
     * @throws ServerException
     */
    private String loginUser(Request request, Response response) throws ServerException {
        /**
         * Small record class specifically for deserializing the login request
         */
        record UserLoginCredentials(String username, String password) {}

        // Store the credentials from the request
        UserLoginCredentials userLogin = new Gson().fromJson(request.body(), UserLoginCredentials.class);
        String username = userLogin.username;
        String password = userLogin.password;

        AuthTokenData authToken = service.login(username, password);
        response.status(200);
        return gson.toJson(authToken);
    }

    /**
     * logoutUser will attempt to log out the user given the session's authtoken.
     * @param request contains the authToken we are seeking to remove
     * @param response is the resulting response JSON object along with the serialized return information
     * @throws ServerException
     */
    private void logoutUser(Request request, Response response) throws ServerException {
        String authToken = new Gson().fromJson(request.body(), String.class);

        service.logOut(authToken);
        response.status(200);
    }

    /**
     * listGame will return a list of all current games in the database
     * @param request contains the current user's authToken
     * @param response is the resulting response JSON object along with the serialized return information
     * @return the list of games in the database
     * @throws ServerException 401
     */
    private String listGame(Request request, Response response) throws ServerException {
        String authToken = new Gson().fromJson(request.body(), String.class);

        Collection<GameData> gameList = service.listGames(authToken);
        response.status(200);
        Map<String, Object> jsonMap = Map.of("games", gameList);
        return gson.toJson(jsonMap);
    }

    /**
     * createGame will create a new GameData object in the database.
     * @param request contains the user's authToken and the name of the game object they want to create.
     * @param response is the resulting response JSON object along with the serialized return information
     * @return the gameID of the new game object
     * @throws ServerException 400, 401
     */
    private String createGame(Request request, Response response) throws ServerException {
        Map<String, String> requestBody = gson.fromJson(request.body(), Map.class);
        String authToken = requestBody.get("authToken");
        String gameName = requestBody.get("gameName");

        int gameID = service.createGame(authToken, gameName);
        response.status(200);
        Map<String, Integer> jsonMap = Map.of("gameID", gameID);
        return gson.toJson(jsonMap);
    }

    /**
     * joinGame will add a user to an existing GameData object in the database.
     * @param request contains the playerColor and gameID
     * @param response contains only the success code (or error message).
     */
    private void joinGame(Request request, Response response) {

    }

    /**
     * clearDatabase will clear the database. I'm sure that doesn't come as a shock to you.
     * @param request
     * @param response contains nothing but the success code, exception info, and my feelings of resignation at
     *                 having to make these large comment headers for every function (it's good practice)
     */
    private void clearDatabase(Request request, Response response) {

    }

    /**
     * Method to handle exceptions
     * @param e is the exception
     * @param request is the request
     * @param response is the response
     */
    private void handleException(Exception e, Request request, Response response) {
        int statusCode;
        String errorMessage;

        if (e instanceof ServerException serverException) {
            statusCode = serverException.getStatusCode();
            errorMessage = serverException.getMessage();
        }
        else {
            statusCode = 500;
            errorMessage = "Error: " + e.getMessage();
        }

        response.status(statusCode);
        response.type("application/json");
        // Map.of() creates a key-value pair, which Gson can take and turn into Json.
        gson.toJson(Map.of("message", errorMessage));
    }

    /**
     * validateUsername will check to see if username is null or empty.
     * If the user tried to register a username of nothing but spaces, then the input system will trim input.
     * Therefore, there doesn't need to be an "all spaces" check.
     * @param username The username to check validity of
     * @return true if the username is valid
     */
    private Boolean validateUsername(String username) {
        return username != null && !username.isEmpty();
    }

    /**
     * validatePassword will make sure that the password has SOMETHING contained in its string.
     * Additional parameters for the password can easiliy be added here.
     * @param password The password to check validity of
     * @return true if the password is valid
     */
    private Boolean validatePassword(String password) {
        return password != null && !password.isEmpty();
    }

    /**
     * validateEmail will make sure that the submitted email follows correct email format.
     * We use a regex function to accomplish this.
     * @param email is the email to check
     * @return true if the email is valid
     */
    private Boolean validateEmail(String email) {
        // EMAIL_REGEX constant was written by amittn on Stack Overflow, with minor changes
        // https://stackoverflow.com/questions/58189908/regex-for-email-validation-including-blank-field-valid-as-well
        final String EMAIL_REGEX = "^([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6})$";
        final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

        if (email != null) {
            return EMAIL_PATTERN.matcher(email).matches();
        }
        return false;
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }
}