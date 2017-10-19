/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package auction.project;

import jade.Boot;
import jade.core.Profile;
import jade.wrapper.AgentController;
import jade.wrapper.ContainerController;

/**
 *
 * @author Baf
 */
public class AuctionProject {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
         String[] services = {"-gui", "-agents", "agent1:xy.zy.Agent1"};
         Boot.main(services);
    }

}
