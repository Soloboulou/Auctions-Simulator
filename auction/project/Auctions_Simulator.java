package auction.project;

import java.awt.LayoutManager;
import javafx.scene.layout.StackPane;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

/**
 *
 * @author Kalliopi Malerou(2370),Nikolaos Mamais(2371),Nikolaos Bafatakis(2383)
 */
public class Auctions_Simulator {
    public static JFrame temp = new JFrame();
    public static JProgressBar progressBar = new JProgressBar();

    public static void main(String[] args) {
        try {
            new Throwable();
            temp.setTitle("Auctions Simulator v1.0.01");
            JPanel tempan = new JPanel();
            temp.getContentPane().setLayout(new BoxLayout(temp.getContentPane(), BoxLayout.Y_AXIS));
            tempan.setName("Auctions Simulator");
            temp.setSize(1000, 485);
            JLabel lab = new JLabel(new ImageIcon("Resources/loadlogo.vgi"));
            tempan.add(lab);
            progressBar.setStringPainted(true);
            temp.getContentPane().add(tempan);
            temp.getContentPane().add(progressBar);
            temp.setLocationRelativeTo(null);
            temp.setResizable(false);
            temp.setVisible(true);
            temp.setAlwaysOnTop(true);
            AuctioneerFrame.main(args);
        } catch (Throwable ex) {
            exMessages(ex);
        }
    }

    public static void exMessages(Throwable ex) {
        JOptionPane.showMessageDialog(temp.getContentPane(), "<html><b>Oops...An Error Occured</b><br>If this problem persists contact one of the 3 SuperDuper Devs @:<br>"
                + "<a href='kmalerou@csd.auth.gr'>kmalerou@csd.auth.gr</a><br>"
                + "<a href='nikolmamai@csd.auth.gr'>nikolmamai@csd.auth.gr</a><br>"
                + "<a href='nikompaf@csd.auth.gr'>nikompaf@csd.auth.gr</a><br>"
                + "<br>Error Message:</html>" + "\n \n" + ex.getMessage(), "Sorry :(", JOptionPane.ERROR_MESSAGE);
        System.exit(-1);
    }

}
