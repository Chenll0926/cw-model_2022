package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Move.*;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;

import java.util.*;


/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	@Nonnull @Override public GameState build(
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

			//Initialisation
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
		}

		//Getters
		@Nonnull @Override public GameSetup getSetup(){
			return setup;
		}
		@Nonnull @Override public ImmutableSet<Piece> getPlayers(){
			Set<Piece> playersSet = new HashSet<>();
			for(Player p : detectives){
				playersSet.add(p.piece());
			}
			playersSet.add(mrX.piece());
			return ImmutableSet.copyOf(playersSet);
		}
		@Nonnull @Override public Optional<Integer> getDetectiveLocation(Detective detective) {
			for(Player p : detectives){
				if(p.piece().webColour() == detective.webColour()){
					return Optional.of(p.location());
				}
			}
			return Optional.empty();
		}
		@Nonnull @Override public Optional<TicketBoard> getPlayerTickets(Piece piece){
			return Optional.empty();
		}
		@Nonnull @Override public ImmutableList<LogEntry> getMrXTravelLog(){
			return log;
		}
		@Nonnull @Override public ImmutableSet<Piece> getWinner(){
			return null;
		}
		@Nonnull @Override public ImmutableSet<Move> getAvailableMoves(){
			return null;
		}

		@Nonnull @Override public GameState advance(Move move) {
			return null;
		}
		@Override public GameState Advance(Move move) {
			return null;
		}
	}

}
