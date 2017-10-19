package auction.project;

import jade.core.AID;
import jade.core.Agent;
import jade.core.behaviours.CyclicBehaviour;
import jade.core.behaviours.OneShotBehaviour;
import jade.core.behaviours.TickerBehaviour;
import jade.domain.DFService;
import jade.domain.FIPAAgentManagement.DFAgentDescription;
import jade.domain.FIPAException;
import jade.lang.acl.ACLMessage;
import jade.lang.acl.MessageTemplate;
import java.io.IOException;
import java.io.Serializable;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Class that represents a Bidder.
 *
 * @author Kalliopi Malerou(2370),Nikolaos Mamais(2371),Nikolaos Bafatakis(2383)
 */
public class Bidder extends Agent {

    private double budget;
    private boolean currentBidder;
    private double currentPrice = 0;
    private double probability = 0;
    private int desire = 10;

    public void Bidder() {
        // Used only in English Auction
        currentBidder = false;
    }

    /**
     * Agent Setup
     */
    protected void setup() {
        /**
         * Parsing the arguments args[0]==Auction Type args[1]==Available Budget
         * args[2]==Bidder Desire(only in English Auction)
         */
        this.budget = Double.parseDouble(String.valueOf(this.getArguments()[0]));
        if (this.getArguments().length > 2) {
            this.desire = Integer.parseInt(String.valueOf(this.getArguments()[2]));
        }

        try {
            System.out.println(getLocalName() + " setting up");
            DFAgentDescription dfd = new DFAgentDescription();
            dfd.setName(getAID());
            DFService.register(this, dfd); //Registering the Agent

            ACLMessage ready = new ACLMessage(ACLMessage.INFORM);
            ready.setContent(Auctioneer.READY);
            ready.addReceiver(new AID("Auctioneer", AID.ISLOCALNAME));
            send(ready);

            //***DUTCH AUCTION******
            CyclicBehaviour dutch = new CyclicBehaviour(this) {
                public void action() {
                    ACLMessage msg = receive(MessageTemplate.MatchPerformative(ACLMessage.PROPOSE));
                    if (msg != null) {
                        if (Double.parseDouble(msg.getContent()) <= budget) { //if the budget is sufficient, claim the object.
                            ACLMessage accept = new ACLMessage(ACLMessage.ACCEPT_PROPOSAL);
                            accept.setContent(Auctioneer.CLAIM);
                            accept.addReceiver(new AID("Auctioneer", AID.ISLOCALNAME));
                            send(accept);
                        }
                    } else {
                        block();
                    }
                }

            };
            //***ENGLISH AUCTION******
            Random rand = new Random(System.currentTimeMillis());
            double increament = 100;
            System.out.println(getLocalName() + "_Desire_" + desire + "_Budget_" + budget);
            probability = computePropability(0);

            //Behaviour that handles the messages
            CyclicBehaviour englishMessages = new CyclicBehaviour(this) {
                public void action() {
                    try {
                        ACLMessage msg = receive();
                        if (msg != null) {
                            if (msg.getPerformative() == 114) {
                                Price temp = (Price) msg.getContentObject();
                                currentPrice = temp.getPrice();
                                probability = computePropability(0);
                                if (temp.getBidder().equals(getLocalName())) { //if this bidder is not the current Bidder
                                    currentBidder = true;
                                } else {
                                    currentBidder = false;

                                }
                            } else if (msg.getPerformative() == 991) { //if the Auctioneer is Closing the Auction
                                char timeLeft = msg.getContent().charAt(8);
                                probability = computePropability(Integer.parseInt(String.valueOf(timeLeft)));
                                if (Integer.parseInt(String.valueOf(timeLeft)) <= 0) {
                                    agentDone();
                                    doDelete();
                                }
                            }
                        } else {
                            block();
                        }

                    } catch (Throwable ex) {
                       Auctions_Simulator.exMessages(new Throwable("<html><b>English Messages Error</b><br></html>" + ex.getMessage()));

                    }
                }
            };
            //Main behaviour of english auction.
            TickerBehaviour english = new TickerBehaviour(this, 1000) {
                @Override
                protected void onTick() {
                    try {
                        if (!currentBidder) { //if the current holder is not this.bidder
                            //System.out.println(getLocalName() + " probability=" + probability);
                            if (rand.nextDouble() <= probability) { //sent a proposal with probability probability if only the budget is sufficient
                                currentPrice += increament;
                                if (currentPrice <= budget) {
                                    ACLMessage propose = new ACLMessage(ACLMessage.PROPOSE);
                                    Price p = new Price(currentPrice, getLocalName());
                                    //System.out.println(getLocalName() + " proposing= " + currentPrice);
                                    propose.setContentObject((Serializable) p);
                                    propose.addReceiver(new AID("Auctioneer", AID.ISLOCALNAME));
                                    send(propose);
                                } else {
                                    done();
                                }
                            }
                        }
                    } catch (Exception ex) {
                        Auctions_Simulator.exMessages(new Throwable("<html><b>English Bidder Error</b><br></html>" + ex.getMessage()));
                    }
                }
            };
            
            
            
            //***JAPANESE AUCTION******
            CyclicBehaviour japanese = new CyclicBehaviour(this) {
                public void action() {
                    ACLMessage msg = receive();
                    //o bidder dexetai tin nea prosfora tou Auctioneer
                    if (msg != null) {
                        if (msg.getPerformative() == 991) {
                            agentDone();
                            removeBehaviour(this);
                        }

                        //an i prosfora einai megaliteri apo tin timi pou mporei na prosferei o Bidder tote 
                        //stelnetai minima aporripsis tis prosforas kai kaleitai i doDelete() gia tin diagrafi tou praktora
                        if (msg.getPerformative() == ACLMessage.PROPOSE && (Double.parseDouble(msg.getContent()) > budget)) {
                            //System.out.println(Double.parseDouble(msg.getContent()));
                            ACLMessage rejectProposal = msg.createReply();
                            rejectProposal.setPerformative(ACLMessage.REJECT_PROPOSAL);
                            rejectProposal.setContent("Reject Proposal");
                            rejectProposal.addReceiver(new AID("Auctioneer", AID.ISLOCALNAME));
                            send(rejectProposal);
                            agentDone();

                        }
                    } else {
                        block();
                    }
                }

            };
            //***FIRST PRICE-SECOND PRICE AUCTION******
            /*The Bidder sends only one message to the Auctioneer with its bid, so an one shot behaviour was implemented.
                This behaviour implied to both first price and second price auction types.
            */
            OneShotBehaviour firstPrice = new OneShotBehaviour(this) {

                @Override
                public void action() {
                    ACLMessage newBid = new ACLMessage(ACLMessage.PROPOSE);
                    newBid.setContent(String.valueOf(budget));
                    newBid.addReceiver(new AID("Auctioneer", AID.ISLOCALNAME));
                    send(newBid);
                    agentDone();
                }
            };

            if (String.valueOf(this.getArguments()[1]).equals("english")) {
                currentPrice = Integer.parseInt(String.valueOf(this.getArguments()[3]));
                addBehaviour(english);
                addBehaviour(englishMessages);
            } else if (String.valueOf(this.getArguments()[1]).equals("dutch")) {
                addBehaviour(dutch);
            } else if (String.valueOf(this.getArguments()[1]).equals("japanese")) {
                addBehaviour(japanese);
            } else if (String.valueOf(this.getArguments()[1]).equals("first-price")) {
                try {
                    Thread.sleep(2000); //sleep the agent for a while in order to let the Auctioneer set up himself.!
                } catch (InterruptedException ex) {
                    Logger.getLogger(Bidder.class.getName()).log(Level.SEVERE, null, ex);
                }
                addBehaviour(firstPrice);
            } else if (String.valueOf(this.getArguments()[1]).equals("second-price")) {
                try {
                    Thread.sleep(2000); //sleep the agent for a while in order to let the Auctioneer set up himself.!
                } catch (InterruptedException ex) {
                    Logger.getLogger(Bidder.class.getName()).log(Level.SEVERE, null, ex);
                }
                addBehaviour(firstPrice);
            }

        } catch (FIPAException ex) {
            Auctions_Simulator.exMessages(new Throwable("<html><b>FIRST PRICE Error</b><br></html>" + ex.getMessage()));
        }

    }

    private double computePropability(int args) {
        double des = Double.parseDouble(String.valueOf(desire)) / 10;
        if (args == 3) {
            System.out.println("ONCE" + getLocalName() + "Probability" + 0.3);
            return 0.5 * des;//65 * des;
        } else if (args == 2) {
            System.out.println("TWICE_" + getLocalName() + "Probability " + String.valueOf(des*0.9));
            return  0.9 * des;//2 * 45 * des;
        } else {
            System.out.println("currentPrice==" + currentPrice);
            double perc = (currentPrice * 100) / budget;
            if (perc == 0.0) {
                return 99.9;
            }
            if (perc < 50) {
                return 0.4;//perc * des * 2;
            } else {
                //return perc * des;
                return 0.3;//100-perc*(1-des);
            }
        }
    }
    /**
     * When an agent finished his job, deregister him.
     */
    private void agentDone() {
        try {
            DFService.deregister(this); //Registering the Agent
            doDelete();
        } catch (FIPAException ex) {
            Auctions_Simulator.exMessages(new Throwable("<html><b>Agent Done</b><br></html>" + ex.getMessage()));
        }
    }
}
