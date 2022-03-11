package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.*;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket;

import java.util.*;


/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override
	public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {
		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}

	public final class MyGameState implements GameState{
		//Attributes
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		private ImmutableList<Player> allPlayers; //All players in the game

		//Constructor
		private MyGameState(final GameSetup setup, final ImmutableSet<Piece> remaining,
							final ImmutableList<LogEntry> log, final Player mrX,
							final List<Player> detectives){
			//Check parameters are not null
			if(setup.moves.isEmpty()) throw new IllegalArgumentException("Moves is empty!");
			if(remaining.isEmpty()) throw new IllegalArgumentException("Remaining is empty!");
			if(mrX.isDetective()) throw new IllegalArgumentException("MrX is empty!");
			if(detectives.isEmpty()) throw new IllegalArgumentException("Detectives is empty!");
			if(setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph is empty!");

			List<Player> copyDetectives = new ArrayList<>();
			List<Integer> copyDetectivesLocation = new ArrayList<>();
			for(Player p : detectives){
				if(p.isMrX()) throw new IllegalArgumentException("Detective is MrX!");
				if(copyDetectives.contains(p)) throw new IllegalArgumentException("Duplicate detective!");
				if(copyDetectivesLocation.contains(p.location())) throw new IllegalArgumentException("Location is overlap!");

				copyDetectives.add(p);
				copyDetectivesLocation.add(p.location());

				if(p.has(ScotlandYard.Ticket.SECRET)) throw new IllegalArgumentException("Detective has secret ticket!");
				if(p.has(ScotlandYard.Ticket.DOUBLE)) throw new IllegalArgumentException("Detective has double ticket!");
			}

			List<Player> all = new ArrayList<>();
			all.add(mrX);
			all.addAll(detectives);

			//Initialisation
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
			this.allPlayers = ImmutableList.copyOf(all);
		}

		//Getters
		@Nonnull @Override
		public GameSetup getSetup(){
			return setup;
		}

		@Nonnull @Override
		public ImmutableSet<Piece> getPlayers(){
			Set<Piece> playersSet = new HashSet<>();
			for(Player p : detectives){
				playersSet.add(p.piece());
			}
			playersSet.add(mrX.piece());
			return ImmutableSet.copyOf(playersSet);
		}

		@Nonnull @Override
		public Optional<Integer> getDetectiveLocation(Detective detective) {
			for(Player p : detectives){
				if(p.piece().webColour().equals(detective.webColour())){
					return Optional.of(p.location());
				}
			}
			return Optional.empty();
		}

		@Nonnull @Override
		public Optional<TicketBoard> getPlayerTickets(Piece piece){
			for(Player p : allPlayers){
				if(p.piece().webColour().equals(piece.webColour())){
					return Optional.of(ticket -> p.tickets().get(ticket));
				}
			}

			return Optional.empty();
		}

		@Nonnull @Override
		public ImmutableList<LogEntry> getMrXTravelLog(){
			return log;
		}

		@Nonnull @Override
		public ImmutableSet<Piece> getWinner(){
			return ImmutableSet.of();
		}

		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves(){
			return moves;
		}

		@Nonnull @Override
		public GameState advance(Move move) {
			return null;
		}
		@Override
		public GameState Advance(Move move) {
			return null;
		}

		private ImmutableSet<SingleMove> makeSingleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source
		){
			ArrayList<Integer> detectiveLocations = new ArrayList<>();
			Set<SingleMove> singleMoves = new HashSet<>();

			//Get the detectives' location
			for(Player detective : detectives){
				detectiveLocations.add(detective.location());
			}

			for(int destination : setup.graph.adjacentNodes(source)){
				if(!(detectiveLocations.contains(destination))){
					for(Transport t : setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of())){
						if(player.has(t.requiredTicket())){
							singleMoves.add(new SingleMove(player.piece(), source, t.requiredTicket(), destination));
						}
					}
					if(player.has(Ticket.SECRET)){
						singleMoves.add(new SingleMove(player.piece(), source, Ticket.SECRET, destination));
					}
				}
			}

			return ImmutableSet.copyOf(singleMoves);
		}
	}

	private ImmutableSet<DoubleMove> makeDoubleMove(
			GameSetup setup,
			List<Player> detectives,
			Player player,
			int source
	){
		Set<DoubleMove> doubleMoves = new HashSet<>();
		return ImmutableSet.copyOf(doubleMoves);
	}

	public ImmutableSet<Move> getAvailableMoves(){
		Set<Move> moves = new HashSet<>();
		return ImmutableSet.copyOf(moves);
	}

}
