package WhizBang;

import breakthrough.*;
import game.*;

/**
 * Whizbang is an AI breakthrough player for CSE 486.
 * 
 * @author Cooper Riley, Josh Ruebusch, Lauren Murray
 *
 */
public class WhizBang extends GamePlayer {

	public WhizBang(String nickname, boolean isDeterministic) {
		super(nickname, new BreakthroughState(), isDeterministic);
	}

	@Override
	public GameMove getMove(GameState state, String lastMv) {
		BreakthroughState board = (BreakthroughState)state;
		
		// TODO Auto-generated method stub
		return null;
	}

	public static void main(String[] args){
		GamePlayer player = new WhizBang("The WhizBanger", false);
		player.compete(args);
	}
}
