package edu.upc.epsevg.prop.amazons;

import edu.upc.epsevg.prop.amazons.players.HumanPlayer;
import edu.upc.epsevg.prop.amazons.players.CarlinhosPlayer;
import edu.upc.epsevg.prop.amazons.players.RandomPlayer;
import edu.upc.epsevg.prop.amazons.players.chickentodinner;
import javax.swing.SwingUtilities;

/**
 *
 * @author bernat
 */
public class Amazons {
        /**
     * @param args
     */
    public static void main(String[] args) {
        
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                
                IPlayer player1 = new HumanPlayer("Snail");
                //IPlayer player1 = new CarlinhosPlayer();
                IPlayer player2 = new chickentodinner("Davis", 4);
                
                new AmazonsBoard(player1 , player2, 10, Level.HALF_BOARD);
                
            }
        });
    }
}
