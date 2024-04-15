package org.acme;

import io.quarkus.qute.Location;
import io.quarkus.qute.Template;
import io.quarkus.qute.TemplateInstance;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.acme.Button;

import java.sql.*;
import java.util.ArrayList;
import java.util.Random;


@Path("/escapeButton")
public class EscapeButton {
    private String postgresURL="jdbc:postgresql://localhost/";
    private String postgresUsername="postgres";
    private String postgresPassword="admin";
    @Location("homePage.html")
    private Template templateOfHomePage;
    @Location("informationPage.html")
    private Template templateOfInformationPage;
    private Button moveButton;
    private Connection connection;
    public EscapeButton(){
        connectToDatabase();
        moveButton=new Button(0, 1);
        insertValues(moveButton.getX(), moveButton.getY());
    }
    @GET
    @Produces(MediaType.TEXT_HTML)
    public TemplateInstance getHomePage() {
        return templateOfHomePage.data("moveButton",moveButton);
    }
    @POST
    public TemplateInstance moveButton(){
        int rightScreenBorder = 90;
        int lowerScreenBorder = 45;
        Random random=new Random();
        moveButton.setX(random.nextInt(0,rightScreenBorder));
        moveButton.setY(random.nextInt(0,lowerScreenBorder));
        insertValues(moveButton.getX(), moveButton.getY());
        return templateOfHomePage.data("moveButton",moveButton);
    }
    @Path("/Information")
    @POST
    public TemplateInstance getInformationPage() {
        ArrayList<Button> list = new ArrayList<>();
        try {
            Statement statement = connection.createStatement();
            String selectQuery = "SELECT x,y FROM Coordinates";
            ResultSet resultSet = statement.executeQuery(selectQuery);
            while(resultSet.next()){
                list.add(new Button(resultSet.getInt("x"), resultSet.getInt("y")));
            }

        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return templateOfInformationPage.data("buttons",list);
    }
    public void connectToDatabase()  {
        try {
            connection = DriverManager
                    .getConnection(postgresURL+"escapebutton", postgresUsername, postgresPassword);
            Statement statement = connection.createStatement();
            statement.executeUpdate("DELETE FROM coordinates");
        } catch (SQLException e) {
            try {
                connection = DriverManager
                        .getConnection(postgresURL, postgresUsername, postgresPassword);
                Statement statement = connection.createStatement();
                statement.executeUpdate("CREATE DATABASE escapebutton");
                connection = DriverManager
                        .getConnection(postgresURL+"escapebutton", postgresUsername, postgresPassword);
                statement= connection.createStatement();
                String createTable = "CREATE TABLE coordinates" +
                        "(id SERIAL PRIMARY KEY, " +
                        "x INTEGER, " +
                        "y INTEGER)";
                statement.executeUpdate(createTable);
            } catch (SQLException ex) {
                throw new RuntimeException(ex);
            }
        }
    }
    public void insertValues(int x, int y){
        try {
            Statement statement = connection.createStatement();
            String insertQuery = "INSERT INTO coordinates(x, y) " +
                    "VALUES(" + x + ", " + y+ ")";
            statement.executeUpdate(insertQuery);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
