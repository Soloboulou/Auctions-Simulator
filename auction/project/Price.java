package auction.project;

import java.io.Serializable;

/**
 *Class that represents a Proposal that contains the price and the name of the Bidder(This class is only udes in the English Auction).
 * The class also implements Serializable in order to be able to be send in an ACLMessage.
 * @author Kalliopi Malerou(2370),Nikolaos Mamais(2371),Nikolaos Bafatakis(2383)
 */
public class Price implements Serializable{

    private double price;
    private String bidder;

    public Price(double price, String bidder) {
        this.price = price;
        this.bidder = bidder;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public String getBidder() {
        return bidder;
    }

    public void setBidder(String bidder) {
        this.bidder = bidder;
    }

}
