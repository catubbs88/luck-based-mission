package org.launchcode.luckbasedmission.models;

import javax.persistence.*;
import java.text.FieldPosition;
import java.text.MessageFormat;
import java.text.NumberFormat;
import java.text.ParsePosition;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Created by catub on 7/24/2017.
 */

@Entity
public abstract class ScratcherGame{
    @Id
    @GeneratedValue
    private int uidNumber;

    //name of the game
    private String name;

    //id number of the game itself, not the UID
    private int gameID;

    //price of each ticket, this will be used as the last step to calculate expected return.
    //when generating the new HashMap, this variable should also be substituted for the String "TICKET" if it appears
    private double ticketPrice;

    //for completeness of information and eventual sorting options
    private int startDay;

    private int startMonth;

    private int startYear;

    private int createdDay;

    private int createdMonth;

    private int createdYear;

    //this will be the denominator of the 1 in X odds.  estimatedWinningTicketsRemaining x averageWinLossChance = estimatedTotalTicketsRemaining
    //it may make sense to refactor this to have estimatedTotalTicketsRemaining be a get method rather than a field, but it may be useful for CustomGame
    private double averageWinLossChance;

    //CSV, ticket values are %2 == 0, ticket quantities are %2 == 1
    private String allPrizes;

    //the sum of numberOfTickets in allPrizes
    private double estimatedWinningTicketsRemaining;

    //the sum of numberOfTickets in allPrizes * averageWinLossChance
    private double estimatedTotalTicketsRemaining;

    //the sum of dollarsToWin * numberOfTickets
    private double estimatedTotalPrizeMoneyRemaining;

    /*
    //same as estimatedTotalPrizesRemaining but taken directly from the site, used for unit testing only
    // estimatedTotalPrizesRemaining is more important because it can be used for CustomGame, and this one cannot
    //implement later
    private double totalPrizeMoneyRemaining;
    */

    //this is just estimatedTotalPrizeMoneyRemaining divided by estimatedTotalTicketsRemaining minus ticketPrice
    //this is the most important part of the Object, short of the identifiers
    private double expectedReturn;

    @OneToMany
    @JoinColumn(name = "scratcher_game_uid_number")
    private List<ScratcherGameCustom> customGames = new ArrayList<>();

    //empty constructor
    public ScratcherGame() {
    }

    /*Not needed
    public ScratcherGame(int gameID, String name, double ticketPrice, int startMonth, int startDay, int startYear,
                         int createdMonth, int createdDay, int createdYear, double averageWinLossChance, String allPrizes) {
        this();
        this.gameID = gameID;
        this.name = name;
        this.ticketPrice = ticketPrice;
        this.startMonth = startMonth;
        this.startDay = startDay;
        this.startYear = startYear;
        this.createdMonth = createdMonth;
        this.createdDay = createdDay;
        this.createdYear = createdYear;
        this.averageWinLossChance = averageWinLossChance;
        this.allPrizes = allPrizes;
    }*/

    //setters start here, getters are below

    public void setGameID(int gameID) {
        this.gameID = gameID;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setTicketPrice(double ticketPrice) {
        this.ticketPrice = ticketPrice;
    }

    //mySQL did not like storing a Local Date, storing as individual ints
    public void setStartMonth(int startMonth) {
        this.startMonth = startMonth;
    }

    public void setStartDay(int startDay) {
        this.startDay = startDay;
    }

    public void setStartYear(int startYear) {
        this.startYear = startYear;
    }

    public void setCreatedMonth(int createdMonth) {
        this.createdMonth = createdMonth;
    }

    public void setCreatedDay(int createdDay) {
        this.createdDay = createdDay;
    }

    public void setCreatedYear(int createdYear) {
        this.createdYear = createdYear;
    }

    public void setAverageWinLossChance(double averageWinLossChance) {
        this.averageWinLossChance = averageWinLossChance;
    }

    //originally, recalculate odds was run within setAllPrizes, but this caused problems.  it is best to run it before saving each time.
    public void setAllPrizes(String allPrizes) {
        allPrizes = allPrizes.trim();
        this.allPrizes = allPrizes;
    }

    public void recalculateOdds() {
        //set accumulators
        double numberOfWinningTickets = 0;
        double totalPrizeMoney = 0;
        ArrayList <Double> ticketValues = new ArrayList<>();
        ArrayList <Double> ticketQuantities = new ArrayList<>();

        //convert allPrizes into a iterable
        String[] allPrizesArray = allPrizes.split(",");

        //iterate!
        for (int i = 0; i < (allPrizesArray.length); i++) {
            //if i is an even number, it is a ticket value
            if (i % 2 == 0) {
                //this takes the string at this index, parses it as an integer, and adds it to the ticketValues arraylist
                ticketValues.add(Double.parseDouble(allPrizesArray[i]));
            }
            //if i is an odd number, it is a ticket quantity
            else {
                //as above, but for ticketQuantities
                ticketQuantities.add(Double.parseDouble(allPrizesArray[i]));
            }
        }

        //now, each number should be in the appropriate list at the corresponding index
        for (int i = 0; i < (ticketQuantities.size()); i++) {
            numberOfWinningTickets += ticketQuantities.get(i);
            totalPrizeMoney += (ticketQuantities.get(i) * ticketValues.get(i));
        }

        //lastly, set the values
        this.estimatedWinningTicketsRemaining = numberOfWinningTickets;
        this.estimatedTotalTicketsRemaining = (this.averageWinLossChance * numberOfWinningTickets);
        this.estimatedTotalPrizeMoneyRemaining = totalPrizeMoney;
        if (this.getEstimatedTotalTicketsRemaining() != 0) {
            this.expectedReturn = (getEstimatedTotalPrizeMoneyRemaining() / this.getEstimatedTotalTicketsRemaining()) - this.getTicketPrice();
        } else {
            this.expectedReturn = 0;
        }
    }

    //getters start here, public getters for all fields

    public static String dollarFormat(double field) {
        NumberFormat dollars = NumberFormat.getCurrencyInstance();
        return dollars.format(field);
    }

    public static String integerFormat(double field) {
        NumberFormat integer = NumberFormat.getIntegerInstance();
        return integer.format(field);
    }

    public int getUidNumber() {
        return uidNumber;
    }

    public int getGameID() {
        return gameID;
    }

    public String getName() {
        return name;
    }

    public double getTicketPrice() {
        return ticketPrice;
    }

    public String getTicketPriceFormatted() {
        return dollarFormat(this.getTicketPrice());
    }

    public int getStartDay() {
        return startDay;
    }

    public int getStartMonth() {
        return startMonth;
    }

    public int getStartYear() {
        return startYear;
    }

    public LocalDate getStartDate() {
        return LocalDate.of(this.startYear, this.startMonth, this.startDay);
    }

    public int getCreatedDay() {
        return createdDay;
    }

    public int getCreatedMonth() {
        return createdMonth;
    }

    public int getCreatedYear() {
        return createdYear;
    }

    public LocalDate getCreatedDate() {
        return LocalDate.of(this.createdYear, this.createdMonth, this.createdDay);
    }

    public double getAverageWinLossChance() {
        return averageWinLossChance;
    }

    public String getAllPrizes() {
        return allPrizes;
    }

    public double getEstimatedWinningTicketsRemaining() {
        return estimatedWinningTicketsRemaining;
    }

    public String getEstimatedWinningTicketsFormatted() {
        return integerFormat(this.getEstimatedWinningTicketsRemaining());
    }

    public String getEstimatedLosingTicketsFormatted() {
        return integerFormat(this.getEstimatedTotalTicketsRemaining() - this.getEstimatedWinningTicketsRemaining());
    }

    public double getEstimatedTotalTicketsRemaining() {
        return estimatedTotalTicketsRemaining;
    }

    public String getEstimatedTotalTicketsRemainingFormatted() {
        return integerFormat(this.getEstimatedTotalTicketsRemaining());
    }

    public double getEstimatedTotalPrizeMoneyRemaining() {
        return estimatedTotalPrizeMoneyRemaining;
    }

    public String getEstimatedTotalPrizeMoneyRemainingFormatted() {
        return dollarFormat(this.getEstimatedTotalPrizeMoneyRemaining());
    }

    public double getExpectedReturn() {
        return expectedReturn;
    }

    public double getReturnPercentage() {
        return (this.expectedReturn / this.ticketPrice);
    }

    public String getReturnPercentageFormatted() {
        double fraction = this.getExpectedReturn() / this.ticketPrice;
        String returnPercentage = MessageFormat.format("{0,number,#.##%}", fraction);
        return returnPercentage;
    }

    //tells template whether return is negative or positive for css
    public String getSign() {
        if (this.getExpectedReturn() < 0) {
            return "negative";
        } else {
            return "positive";
        }
    }

    public String getExpectedReturnFormatted() {
        return dollarFormat(this.getExpectedReturn());
    }

}