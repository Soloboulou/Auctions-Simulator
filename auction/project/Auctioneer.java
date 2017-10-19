package auction.project;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import jade.wrapper.AgentController;
import jade.wrapper.PlatformController;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that represents an Auctioneer.
 *
 * @author Kalliopi Malerou(2370),Nikolaos Mamais(2371),Nikolaos Bafatakis(2383)
 */
public class Auctioneer extends Agent {

    public final static String READY = "READY";
    public final static String BID = "BID";
    public final static String CLAIM = "CLAIM";
    public final static String CLOSING = "CLOSING";
    protected Vector bidders = new Vector();
    private int num;
    private double money; //current price
    private double price; //starting price
    private String type;

    //EnglishAuction
    private double startTime;
    private boolean proposed;
    private boolean closing;
    private int closingTime = 3;
    private String currentBidder;

    //FirtsPrice Auction
    private double bid1 = 0;
    private String bidder1;
    private double bid2 = 0;
    private String bidder2;

    public Auctioneer() {

    }

    protected void setup() {
        this.price = Double.parseDouble(String.valueOf(this.getArguments()[0]));
        this.type = String.valueOf(this.getArguments()[1]);
        money = 0;
        try {
            System.out.println(getLocalName() + " setting up");
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            try {
                DFService.register(this, dfd); //Registering the Agent
            } catch (Throwable ex) {
                Auctions_Simulator.exMessages(new Throwable("<html><b>Auctioneer Registration Error</b><br></html>" + ex.getMessage()));
            }
            /**
             * Behaviour for the Dutch Style Auction
             */
            TickerBehaviour dutch = new TickerBehaviour(this, 1000) {
                protected void onTick() {
                    ACLMessage msg = receive();
                    if (msg != null) {
                        if (msg.getContent().equals(Auctioneer.CLAIM)) {
                            AuctioneerFrame.setEvent("**********************");
                            AuctioneerFrame.setEvent("######################");
                            AuctioneerFrame.setEvent("€€€€€€€€€€€€€€€€€€€€€€");
                            AuctioneerFrame.setEvent(msg.getSender().getLocalName() + " WON with price " + price);
                            AuctioneerFrame.setEvent("0000");
                            agentDone();
                            removeBehaviour(this);
                        }
                    }
                    price -= 100;
                    AuctioneerFrame.setEvent("Proposed " + price + " €");
                    ACLMessage proposal = new ACLMessage(ACLMessage.INFORM);
                    for (int i = 0; i < bidders.size(); i++) {
                        proposal.addReceiver(new AID((String) bidders.get(i), AID.ISLOCALNAME)); //send the price
                    }
                    proposal.setPerformative(ACLMessage.PROPOSE);
                    proposal.setContent(String.valueOf(price));
                    send(proposal);
                }

            };
            /**
             * Closing tick behaviour is enabled when no bidder gives a new bid
             * and the auction is about to close. If the auctioneer enables the
             * current behaviour and a new bid comes the behaviour is deleted.
             */
            TickerBehaviour closingTick = new TickerBehaviour(this, 1200) {
                protected void onTick() {
                    try {
                        ACLMessage info = new ACLMessage(991);
                        for (int i = 0; i < bidders.size(); i++) {
                            info.addReceiver(new AID(bidders.get(i).toString(), AID.ISLOCALNAME));
                        }
                        info.setContent(Auctioneer.CLOSING + "_" + String.valueOf(closingTime));
                        System.out.println(closingTime);
                        send(info);
                        if (closingTime == 3) {
                            AuctioneerFrame.setEvent("Going once.....");
                        } else if (closingTime == 2) {
                            AuctioneerFrame.setEvent("Going twice.....");
                        } else if (closingTime == 0) {
                            AuctioneerFrame.setEvent("Auction Closed......");
                        }
                        closingTime--;
                        if (closingTime < 0) {
                            System.err.println(currentBidder + " WON with price " + money);
                            AuctioneerFrame.setEvent("**********************");
                            AuctioneerFrame.setEvent("######################");
                            AuctioneerFrame.setEvent("€€€€€€€€€€€€€€€€€€€€€€");
                            AuctioneerFrame.setEvent(currentBidder + " WON with price " + money);
                            AuctioneerFrame.setEvent("0000"); //sends an event to the GUI to tell that the auction is done.!
                            agentDone();
                        }
                    } catch (Throwable ex) {
                        Auctions_Simulator.exMessages(new Throwable("<html><b>Closing Tick Error</b><br></html>" + ex.getMessage()));
                    }

                }

            };
            /**
             * Behaviour that utilises the English Auction
             */
            CyclicBehaviour english = new CyclicBehaviour(this) {

                @Override
                public void action() {
                    try {
                        ACLMessage msg = receive();
                        if (msg != null) {

                            if (msg.getPerformative() == ACLMessage.PROPOSE) {
                                Price p = (Price) msg.getContentObject();
                                AuctioneerFrame.setEvent(p.getBidder() + " Proposed me:" + p.getPrice() + " €");
                                if (p.getPrice() > money) {
                                    removeBehaviour(closingTick);
                                    money = p.getPrice();
                                    currentBidder = p.getBidder();
                                    this.sendPriceInfo(p.getBidder());
                                    proposed = true; //Debug later!!!!!!
                                    closing = false;
                                    closingTime = 3;

                                }
                            }

                            startTime = System.currentTimeMillis();
                        } else {
                            if (!closing) {
                                if (((System.currentTimeMillis() - startTime) / 1000) > 5) {  //time for closing pulse
                                    System.err.println("STARTING COUNTDOWN");
                                    addBehaviour(closingTick);
                                    closing = true;

                                }
                            }
                        }
                    } catch (Exception ex) {
                        Auctions_Simulator.exMessages(new Throwable("<html><b>Auctioneer-English Auction Error</b><br></html>" + ex.getMessage()));
                    }

                }

                ;
                
                protected void sendPriceInfo(String bidder) {
                    try {
                        ACLMessage info = new ACLMessage(114);
                        System.out.println("sending infos");
                        for (int i = 0; i < bidders.size(); i++) {
                            System.out.println("sending to" + bidders.get(i));
                            info.addReceiver(new AID((String) bidders.get(i), AID.ISLOCALNAME));
                        }
                        info.setContentObject((Serializable) new Price(money, bidder));
                        send(info);
                    } catch (IOException ex) {
                        Auctions_Simulator.exMessages(new Throwable("<html><b>Send Info Auctioneer Error</b><br></html>" + ex.getMessage()));
                    }
                }

            ;

            };
            
            
            

            //o constructor dexetai enan agent kai xrono se ms
            TickerBehaviour japanese = new TickerBehaviour(this, 1000) {
                protected void onTick() {
                    //dexetai minima apo tous Bidders
                    ACLMessage msg = receive();

                    if (msg != null) {

                        //an o Auctioneer dextei minima aporripsis tis prosforas emfanizetai o nikitis tis dimoprasias kai to programma termatizei
                        if (msg.getContent().equals("Reject Proposal")) {
                            System.err.println(price);
                            bidders.remove(msg.getSender().getLocalName());
                            System.out.println(bidders.get(0).toString() + " won the auction!!!");
                            AuctioneerFrame.setEvent("**********************");
                            AuctioneerFrame.setEvent("######################");
                            AuctioneerFrame.setEvent("€€€€€€€€€€€€€€€€€€€€€€");
                            AuctioneerFrame.setEvent(bidders.get(0) + " WON with price " + price);
                            AuctioneerFrame.setEvent("0000");
                            System.out.println("Auction ended");
                            ACLMessage done = new ACLMessage(991);
                            done.addReceiver(new AID((String) bidders.get(0), AID.ISLOCALNAME)); //killing the remainiing bidders
                            send(done);
                            agentDone();
                            removeBehaviour(this);

                        }
                    } else {

                        //i timi auksanetai stadiaka
                        price += 200;
                        System.out.println("New price is " + price);
                        AuctioneerFrame.setEvent("Current Price is " + price + " €");
                        //afou exei auksithei i timi stelnetai i nea prosfora stous bidders 
                        ACLMessage proposal = new ACLMessage(ACLMessage.PROPOSE);
                        for (int i = 0; i < bidders.size(); i++) {
                            proposal.addReceiver(new AID((String) bidders.get(i), AID.ISLOCALNAME)); //!!!!!!!
                        }
                        proposal.setPerformative(ACLMessage.PROPOSE);
                        proposal.setContent(String.valueOf(price));
                        send(proposal);
                    }
                }

            };

            CyclicBehaviour firstPrice = new CyclicBehaviour(this) {
                @Override
                public void action() {
                    ACLMessage msg = receive();
                    if (msg != null && msg.getPerformative() == ACLMessage.PROPOSE) {
                        if (bid1 != 0) {
                            bid2 = Double.parseDouble(msg.getContent());
                        } else {
                            bid1 = Double.parseDouble(msg.getContent());
                        }
                        if (bid1 > bid2 && bid2 != 0) {
                            bidders.remove(1);
                            System.out.println(bidders.get(0).toString() + " won the auction!!!");
                            AuctioneerFrame.setEvent("**********************");
                            AuctioneerFrame.setEvent("######################");
                            AuctioneerFrame.setEvent("€€€€€€€€€€€€€€€€€€€€€€");
                            AuctioneerFrame.setEvent(bidders.get(0) + " WON with price " + bid1);
                            AuctioneerFrame.setEvent("0000");
                            System.out.println("Auction ended");
                            ACLMessage done = new ACLMessage(991);
                            done.addReceiver(new AID((String) bidders.get(0), AID.ISLOCALNAME)); //killing the remainiing bidders
                            send(done);
                            agentDone();
                            removeBehaviour(this);

                        }
                        if (bid2 > bid1 && bid1 != 0) {
                            bidders.remove(0);
                            System.out.println(bidders.get(0).toString() + " won the auction!!!");
                            AuctioneerFrame.setEvent("**********************");
                            AuctioneerFrame.setEvent("######################");
                            AuctioneerFrame.setEvent("€€€€€€€€€€€€€€€€€€€€€€");
                            AuctioneerFrame.setEvent(bidders.get(0) + " WON with price " + bid2);
                            AuctioneerFrame.setEvent("0000");
                            System.out.println("Auction ended");
                            ACLMessage done = new ACLMessage(991);
                            done.addReceiver(new AID((String) bidders.get(0), AID.ISLOCALNAME)); //killing the remainiing bidders
                            send(done);
                            agentDone();
                            removeBehaviour(this);
                        }

                        if (bid1 == bid2 && bid1 != 0 && bid2 != 0) {
                            bidders.removeAllElements();
                            AuctioneerFrame.setEvent("**********************");
                            AuctioneerFrame.setEvent("######################");
                            AuctioneerFrame.setEvent("€€€€€€€€€€€€€€€€€€€€€€");
                            AuctioneerFrame.setEvent(" ");
                            AuctioneerFrame.setEvent(" ");
                            AuctioneerFrame.setEvent("TIE......");
                            AuctioneerFrame.setEvent("0000");
                            System.out.println("Auction ended");
                            agentDone();
                            removeBehaviour(this);

                        }

                    }
                }
            };

            CyclicBehaviour secondPrice = new CyclicBehaviour(this) {
                @Override
                public void action() {
                    ACLMessage msg = receive();
                    if (msg != null && msg.getPerformative() == ACLMessage.PROPOSE) {
                        if (bid1 != 0) {
                            bid2 = Double.parseDouble(msg.getContent());
                            bidder2 = msg.getSender().getLocalName();
                        } else {
                            bid1 = Double.parseDouble(msg.getContent());
                            bidder1 = msg.getSender().getLocalName();
                        }
                        if (bid1 > bid2 && bid2 != 0) {
                            bidders.remove(1);
                            System.out.println(bidders.get(0).toString() + " won the auction!!!");
                            AuctioneerFrame.setEvent("**********************");
                            AuctioneerFrame.setEvent("######################");
                            AuctioneerFrame.setEvent("€€€€€€€€€€€€€€€€€€€€€€");
                            AuctioneerFrame.setEvent(bidder1 + " WON with price " + bid2);
                            AuctioneerFrame.setEvent("0000");
                            System.out.println("Auction ended");
                            bid1 = 0;
                            bid2 = 0;
                            agentDone();
                            removeBehaviour(this);

                        }
                        if (bid2 > bid1 && bid1 != 0) {
                            bidders.remove(0);
                            System.out.println(bidders.get(0).toString() + " won the auction!!!");
                            AuctioneerFrame.setEvent("**********************");
                            AuctioneerFrame.setEvent("######################");
                            AuctioneerFrame.setEvent("€€€€€€€€€€€€€€€€€€€€€€");
                            AuctioneerFrame.setEvent(bidder2 + " WON with price " + bid1);
                            AuctioneerFrame.setEvent("0000");
                            System.out.println("Auction ended");
                            bid1 = 0;
                            bid2 = 0;
                            agentDone();
                            removeBehaviour(this);
                        }

                        if (bid1 == bid2 && bid1 != 0 && bid2 != 0) {
                            bidders.removeAllElements();
                            System.out.println("Tie");
                            bid1 = 0;
                            bid2 = 0;
                            agentDone();
                            removeBehaviour(this);

                        }

                    }
                }
            };
            /**
             * Function for registring the bidders and choosing the appropriate
             * Auction type.
             */
            CyclicBehaviour c1 = new CyclicBehaviour(this) {
                public void action() {
                    ACLMessage msg = receive();
                    if (msg != null) {
                        if (msg.getContent().equals(Auctioneer.READY)) {
                            String sender = msg.getSender().getLocalName();
                            bidders.add(msg.getSender().getLocalName());
                            if (bidders.size() == 2) { //!!!!
                                if (type.equals("english")) {
                                    addBehaviour(english);
                                    System.err.println("ENLISH IS SELECTED AS THE CURRENT AUCTION!");
                                } else if (type.equals("dutch")) {
                                    addBehaviour(dutch);
                                    System.err.println("DUTCH IS SELECTED AS THE CURRENT AUCTION!");
                                } else if (type.equals("japanese")) {
                                    addBehaviour(japanese);
                                    System.err.println("JAPANESE IS SELECTED AS THE CURRENT AUCTION!");
                                } else if (type.equals("first-price")) {
                                    addBehaviour(firstPrice);
                                    System.err.println("FIRST-PRICE IS SELECTED AS THE CURRENT AUCTION!");
                                    AuctioneerFrame.setEvent("Waiting for the bids...");
                                } else if (type.equals("second-price")) {
                                    addBehaviour(secondPrice);
                                    System.err.println("SECOND-PRICE IS SELECTED AS THE CURRENT AUCTION!");
                                    AuctioneerFrame.setEvent("Waiting for the bids...");
                                }

                                removeBehaviour(this);
                            }
                            System.out.println(sender + " is ready for the Auction");
                        }
                    } else {
                        // if no message is arrived, block the behaviour
                        block();
                    }
                }

            };
            addBehaviour(c1);
        } catch (Throwable ex) {
            Auctions_Simulator.exMessages(new Throwable("<html><b>C1 Error</b><br></html>" + ex.getMessage()));
        }

    }

    private void agentDone() {
        try {
            DFService.deregister(this); //Deregistering the Agent
            doDelete();
        } catch (FIPAException ex) {
           Auctions_Simulator.exMessages(new Throwable("<html><b>Agent Done Error</b><br></html>" + ex.getMessage()));
        }
    }

}
