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

	protected ScoredBreakthroughMove[] moveStack;
	public static final double MAX_SCORE = Double.MAX_VALUE;
	public static final int MAX_DEPTH = 2; // TODO

	public WhizBang(String nickname, boolean isDeterministic) {
		super(nickname, new BreakthroughState(), isDeterministic);
	}

	@Override
	public GameMove getMove(GameState state, String lastMv) {
		initalizeStack();
		alphaBeta((BreakthroughState) state, 0, Double.NEGATIVE_INFINITY,
				Double.POSITIVE_INFINITY);
		return moveStack[0];

	}

	public void initalizeStack() {
		moveStack = new ScoredBreakthroughMove[MAX_DEPTH];
		for (int i = 0; i < MAX_DEPTH; i++) {
			moveStack[i] = new ScoredBreakthroughMove();
		}
	}

	/**
	 * The method that searches through the possible moves.
	 * 
	 * @param board
	 *            the board being analyzed
	 * @param currentDepth
	 *            the current depth of the search
	 * @param alpha
	 *            best Max value
	 * @param beta
	 *            best Min value
	 */
	public void alphaBeta(BreakthroughState board, int currentDepth,
			double alpha, double beta) {

		boolean toMaximize = (board.getWho() == GameState.Who.HOME);
		boolean isTerminal = terminalValue(board, moveStack, currentDepth);

		if (isTerminal) {
			;
		} else if (currentDepth == MAX_DEPTH - 1) {
			moveStack[currentDepth].score = evaluate(board);
		} else {
			double bestScore = (toMaximize ? Double.NEGATIVE_INFINITY
					: Double.POSITIVE_INFINITY);
			
			char whoseTurn;
			int dRow; // The change in row for a turn
			if (toMaximize) {
				whoseTurn = BreakthroughState.homeSym;
				dRow = 1;
			} else {
				whoseTurn = BreakthroughState.awaySym;
				dRow = -1;
			}

			ScoredBreakthroughMove tempMove = new ScoredBreakthroughMove();
			ScoredBreakthroughMove bestMove = moveStack[currentDepth];
			ScoredBreakthroughMove nextMove = moveStack[currentDepth + 1];
			bestMove.score = bestScore;
			// GameState.Who currentTurn = board.getWho();

			for (int i = 0; i < BreakthroughState.N; i++) {
				for (int j = 0; j < BreakthroughState.N; j++) {
					if (board.board[i][j] == whoseTurn) {
						tempMove.startRow = i;
						tempMove.startCol = j;
						tempMove.endingRow = i + dRow;

						for (int k = -1; k < 2; k++) {
							tempMove.endingCol = j + k;

							if (board.moveOK(tempMove)) {
								BreakthroughState tempBoard = (BreakthroughState) board.clone();
								tempBoard.makeMove(tempMove);
								alphaBeta(tempBoard, currentDepth + 1, alpha, beta);

								/*
								 * char currentlyThere =
								 * board.board[tempMove.endingCol
								 * ][tempMove.endingRow];
								 * board.makeMove(tempMove);
								 * 
								 * alphaBeta(board, currentDepth + 1, alpha,
								 * beta);
								 * 
								 * board.board[tempMove.endingCol][tempMove.
								 * endingRow] = currentlyThere;
								 * board.board[tempMove
								 * .startCol][tempMove.startRow] = whoseTurn;
								 * 
								 * board.status = GameState.Status.GAME_ON;
								 * board.who = currentTurn;
								 */
								
								// Updates the best score
								if (toMaximize && nextMove.score > bestMove.score) {
									bestMove = (ScoredBreakthroughMove) tempMove.clone();
									bestMove.score = nextMove.score;
								} else if (!toMaximize && nextMove.score < bestMove.score) {
									bestMove = (ScoredBreakthroughMove) tempMove.clone();
									bestMove.score = nextMove.score;
								}

								// Performing pruning
								if (toMaximize) {
									alpha = Math.max(bestMove.score, alpha);
									if (bestMove.score >= beta
											|| bestMove.score == MAX_SCORE) {
										return;
									}
								} else {
									beta = Math.min(bestMove.score, beta);
									if (bestMove.score <= alpha
											|| bestMove.score == -MAX_SCORE) {
										return;
									}
								}
							}
						}
					}
				}
			}
		}
	}

	/**
	 * Evaluation function used by a alpha-beta search method. it calls all
	 * scoring functions to calculate the score.
	 * 
	 * @param board
	 *            the board to evaluate
	 * @return the score of the board (+ home winning, - away winning)
	 */
	public double evaluate(BreakthroughState board) {
		// TODO a preliminary implementation just to do some testing.
		return forwardTriangleEval(board) + detectBlock(board);
	}

	/**
	 * Determines a score based on the number of opposing pieces in front and
	 * diagonal to the player's pieces. Will be run once.
	 * 
	 * Calculates the difference of home and away's scores internally.
	 * 
	 * @param board
	 *            the board to be evaluated
	 * @return the combined score of home and away
	 */
	public double forwardTriangleEval(BreakthroughState board) {
		double score = 0;
		for (int row = 0; row < BreakthroughState.N; row++) {
			for (int col = 0; col < BreakthroughState.N; col++) {
				if (board.board[row][col] != BreakthroughState.emptySym) {
					score += forwardTriAtPiece(board, row, col);
				}
			}
		}
		return score;
	}

	/**
	 * Used by forwardTriangleEval function, counts the opposing pieces in front
	 * of the piece.
	 * 
	 * @param board
	 *            to be examined
	 * @param row
	 *            of piece we are looking at
	 * @param col
	 *            of piece we are looking at
	 * @return number of opposing pieces in front of that piece
	 */
	public double forwardTriAtPiece(BreakthroughState board, int row, int col) {
		double count = 0;

		if (board.board[row][col] == BreakthroughState.homeSym) {
			for (int i = row + 1; i < BreakthroughState.N; i++) {
				for (int j = row - i; j < i - row; j++) {
					if (col + j >= 0
							&& col + j < BreakthroughState.N
							&& board.board[i][col + j] == BreakthroughState.awaySym) {
						count--;
					}
				}
			}
		} else {
			for (int i = row - 1; i >= 0; i--) {
				for (int j = i - row; j < row - i; j++) {
					if (col + j >= 0
							&& col + j < BreakthroughState.N
							&& board.board[i][col + j] == BreakthroughState.homeSym) {
						count++;
					}
				}
			}
		}
		return count;
	}

	/**
	 * Determines the score based on pieces that are blocked. Runs only once for
	 * the entire board
	 * 
	 * @param board
	 *            state is examined for scoring
	 * @return score positive if in home favor and negative if away favor
	 */
	public double detectBlock(BreakthroughState board) {
		double score = 0;
		for (int i = 0; i < BreakthroughState.N; i++) {
			for (int j = 0; j < BreakthroughState.N; j++) {
				if (board.board[i][j] == BreakthroughState.homeSym
						&& i + 2 <= BreakthroughState.N - 1) {
					if (board.board[i + 1][j] == BreakthroughState.awaySym
							&& board.board[i + 2][j] == BreakthroughState.awaySym) {
						score--;
					}
				} else if (board.board[i][j] == BreakthroughState.awaySym
						&& i - 2 >= 0) {
					if (board.board[i - 1][j] == BreakthroughState.homeSym
							&& board.board[i - 2][j] == BreakthroughState.homeSym) {
						score++;
					}
				}
			}
		}
		return score;
	}

	public boolean terminalValue(BreakthroughState board, ScoredBreakthroughMove [] mvStack, int depth) {
		GameState.Status status = board.getStatus();
		boolean isTerminal = true;

		if (status == GameState.Status.HOME_WIN) {
			mvStack[depth].score = MAX_SCORE;
		} else if (status == GameState.Status.AWAY_WIN) {
			mvStack[depth].score = -MAX_SCORE;
		} else if (status == GameState.Status.DRAW) {
			mvStack[depth].score = 0;
		} else {
			isTerminal = false;
		}

		return isTerminal;
	}

	public static void main(String[] args) {
		GamePlayer player = new WhizBang("The WhizBanger", false);
		player.compete(args);
	}

	/**
	 * Extends BreakthroughMove to allow for scoring of moves
	 * 
	 * @author Cooper
	 * 
	 */
	class ScoredBreakthroughMove extends BreakthroughMove {
		public double score;

		public ScoredBreakthroughMove(int r1, int c1, int r2, int c2,
				double score) {
			super(r1, c1, r2, c2);
			this.score = score;
		}

		public ScoredBreakthroughMove() {
			super();
			this.score = 0;
		}

		public Object clone() {
			return new ScoredBreakthroughMove(startRow, startCol, endingRow,
					endingCol, score);

		}
	}
}
