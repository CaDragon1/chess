package dataaccess;
import models.UserData;
import models.AuthTokenData;
import models.GameData;
import org.junit.jupiter.api.*;
import passoff.model.TestUser;

import java.sql.Connection;
import java.sql.Statement;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SQLGameDataTests {
}
