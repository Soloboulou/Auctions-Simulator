package auction.project;
import java.util.Random;
/**
 * A class that shuffles cool(!) names to be used for the Bidders. 
 * @author Kalliopi Malerou(2370),Nikolaos Mamais(2371),Nikolaos Bafatakis(2383)
 */
public abstract class NameGenerator {

   private static String[] male = {"John Marston", "Neo Cortex","William Blazkowicz","Nick Reyes" ,
       "John Price","Booker DeWitt","Ethan Mars","Isaac Clarke","Corvo Attano","Edward Kenway","Nathan Drake"};
   
   private static String[] female = {"Judith Mossman","Katherine Marlowe","Alex Vance","Rosalind Lutece",
       "Jill Valentine","Sofia Lamb","Emily Kaldwin","Chloe Frazer","Lunafreya Nox Fleuret" ,"Elena Fisher"};
   
   private static Random rand = new Random();
   /**
    * 
    * @param type male/female i=as a String in order to get the given  gender name.
    * @return a string representation of a Name with first and last name.
    */
   public static String generateName(String type) {

       if(type.equals("male")){
           return male[rand.nextInt(male.length)];
       }else{
           return female[rand.nextInt(female.length)];
       }

   }

}