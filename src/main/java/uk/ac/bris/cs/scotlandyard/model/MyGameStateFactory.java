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

			//Set all players
			List<Player> all = new ArrayList<>();
			all.add(mrX);
			all.addAll(detectives);

			//Initialisation
			this.setup = setup;
			this.remaining = remaining; //The players still in the game
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
			Set<Piece> winners = new HashSet<>();
			Set<Piece> detectivePieces = new HashSet<>();
			for(Player d : detectives){
				detectivePieces.add(d.piece());
			}

			//Detectives win
			for(Player d : detectives){ //A detective finish a move on the same station as Mr.X
				if(d.location() == mrX.location()){
					winners = detectivePieces;
					break;
				}
			}

			if(makeSingleMoves(setup, detectives, mrX, mrX.location()).isEmpty()
							&& makeDoubleMove(setup, detectives, mrX, mrX.location()).isEmpty()){ //There are no unoccupied station
				winners = detectivePieces;                                                        //for Mr.X to travel
			}

			//Mr.X win
			if(setup.moves.size() == log.size()){ //Mr.X fill the log and there is no rounds for detectives to catch him
				winners.add(mrX.piece());
			}

			Set<Piece> detectivesCannotMove = new HashSet<>();
			for(Player d : detectives){
				if(makeSingleMoves(setup, detectives, d, d.location()).isEmpty()){ //Detectives have no ticket
					detectivesCannotMove.add(d.piece());                           //to move to catch Mr.X
				}
			}
			if(detectivesCannotMove.equals(detectivePieces)){
				winners.add(mrX.piece());
			}

			if(!winners.isEmpty()){
				this.remaining = ImmutableSet.of();
			}

			return ImmutableSet.copyOf(winners);
		}

		@Nonnull @Override
		public ImmutableSet<Move> getAvailableMoves(){
			Set<Move> moves = new HashSet<>();
			if(remaining.contains(mrX.piece())){

				moves.addAll(makeSingleMoves(setup, detectives, mrX, mrX.location()));
				moves.addAll(makeDoubleMove(setup, detectives, mrX, mrX.location()));
			}else{

				for(Player detective : detectives){

					if(remaining.contains(detective.piece())){

						moves.addAll(makeSingleMoves(setup, detectives, detective, detective.location()));
					}
				}
			}

			return ImmutableSet.copyOf(moves);

		}

		//Methods
		@Nonnull @Override
		public GameState advance(Move move) {
			this.moves = getAvailableMoves();
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);

			updateRemaining(move);
			updateLog(move);
			updateTickets(move);
			updateLocation(move);

			return new MyGameState(setup, remaining, log, mrX, detectives);
		}

		@Override
		public GameState Advance(Move move) {
			this.moves = getAvailableMoves();
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: " + move);

			updateRemaining(move);
			updateLog(move);
			updateTickets(move);
			updateLocation(move);

			return new MyGameState(setup, remaining, log, mrX, detectives);
		}

		public ImmutableSet<SingleMove> makeSingleMoves(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source
		){
			Set<SingleMove> singleMoves = new HashSet<>();

			for(int destination : setup.graph.adjacentNodes(source)){
				if(!isOccupied(player, detectives, destination)){

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

		private ImmutableSet<DoubleMove> makeDoubleMove(
				GameSetup setup,
				List<Player> detectives,
				Player player,
				int source
		){
			Set<DoubleMove> doubleMoves = new HashSet<>();
			ImmutableSet<SingleMove> firstSingleMove = makeSingleMoves(setup, detectives, player, source);

			if(player.has(Ticket.DOUBLE) && this.setup.moves.size() > 1){
					for(SingleMove move1 : firstSingleMove){

						ImmutableSet<SingleMove> secondSingleMove = makeSingleMoves(
								setup, detectives,
								player, move1.destination);

						for(SingleMove move2 : secondSingleMove){

							if(move1.ticket != move2.ticket || player.hasAtLeast(move1.ticket, 2)){

								DoubleMove doubleMove = new DoubleMove(
										player.piece(), move1.source(),
										move1.ticket, move1.destination,
										move2.ticket, move2.destination);

								doubleMoves.add(doubleMove);
							}
						}
					}
				}
			return ImmutableSet.copyOf(doubleMoves);
		}

		private boolean isOccupied(Player player, List<Player> detectives, int destination){
			ArrayList<Integer> locations = new ArrayList<>();
			boolean isOccupied = false;

			for(Player detective : detectives){
				if(detective != player){
					locations.add(detective.location());
				}
			}

			for(int location : locations){
				if(location == destination){
					isOccupied = true;
					break;
				}
			}

			return isOccupied;
		}

		//Get the final destination
		private Integer getDestination(Move move){
			return move.accept(new Visitor<>() {
				@Override
				public Integer visit(SingleMove singleMove) {
					return singleMove.destination;
				}

				@Override
				public Integer visit(DoubleMove doubleMove) {
					return doubleMove.destination2;
				}
			});
		}

		//As double move has destination1 and destination2, this method is to get destination1
		private Integer getMiddleDestination(Move move){
			return move.accept(new Visitor<>() {
				@Override
				public Integer visit(SingleMove singleMove) {
					return -1;
				}

				@Override
				public Integer visit(DoubleMove doubleMove) {
					return doubleMove.destination1;
				}
			});
		}

		private ImmutableList<Ticket> getTickets(Move move){
			return move.accept(new Visitor<>() {
				@Override
				public ImmutableList<Ticket> visit(SingleMove singleMove) {
					return ImmutableList.copyOf(singleMove.tickets());
				}

				@Override
				public ImmutableList<Ticket> visit(DoubleMove doubleMove) {
					return ImmutableList.copyOf(doubleMove.tickets());
				}
			});
		}

		private void updateLog(Move move){
			List<LogEntry> newLog = new ArrayList<>(log);

			//Only MrX needs to update log
			if(move.commencedBy().isMrX()){

				if (isDoubleMove(move)) {

					//Double move update log
					boolean isFirst = true;
					for (Ticket ticket : move.tickets()) {

						if (ticket == Ticket.DOUBLE) continue;
						if (isFirst) {

							if (setup.moves.get(log.size())) {
								newLog.add(LogEntry.reveal(ticket, getMiddleDestination(move)));
							} else {
								newLog.add(LogEntry.hidden(ticket));
							}

							isFirst = false;
						} else {
							if (setup.moves.get(log.size() + 1)) {
								newLog.add(LogEntry.reveal(ticket, getDestination(move)));
							} else {
								newLog.add(LogEntry.hidden(ticket));
							}
						}

					}
				} else {

					//Single move update log
					for (Ticket ticket : move.tickets()) {
						if (setup.moves.get(log.size())) {
							newLog.add(LogEntry.reveal(ticket, getDestination(move)));
						} else {
							newLog.add(LogEntry.hidden(ticket));
						}
					}
				}
			}
			this.log = ImmutableList.copyOf(newLog);
		}

		private void updateTickets(Move move){
			ImmutableList<Ticket> tickets = getTickets(move);

			if(move.commencedBy().isMrX()){

				for(Ticket ticket : tickets){

					//Take the used ticket(s) away from MrX
					mrX = mrX.use(ticket);
				}
			}else{

				List<Player> temp = new ArrayList<>(detectives);
				for(Ticket ticket : tickets){

					for(Player detective : detectives){

						if(detective.piece().equals(move.commencedBy())){
							int indexOfDetective = detectives.indexOf(detective);
							temp.set(indexOfDetective, detective.use(ticket));
							mrX = mrX.give(ticket);
						}
					}
				}
				detectives = temp;
			}
		}

		private void updateLocation(Move move){
			Integer destination = getDestination(move);

			if(move.commencedBy().isMrX()){
				mrX = mrX.at(destination);
			}else{

				List<Player> temp = new ArrayList<>(detectives);
				for(Player detective : detectives){

					if(detective.piece().equals(move.commencedBy())){
						int indexOfDetective = detectives.indexOf(detective);
						temp.set(indexOfDetective, detective.at(destination));
					}
				}
				detectives = temp;
			}
		}

		private void updateRemaining(Move move){
			List<Piece> newRemaining;
			List<Piece> remainingList = new ArrayList<>(remaining);
			List<Piece> detectivePieces = new ArrayList<>();
			for(Player d : detectives){
				detectivePieces.add(d.piece());
			}

			if(move.commencedBy().isMrX()){
				newRemaining = detectivePieces;
			}else{
				remainingList.remove(move.commencedBy());
				newRemaining = remainingList;
			}

			boolean isAllDetectivesCannotMove = false;
			List<Piece> detectivesCannotMove = new ArrayList<>();
			for(Player d : detectives){
				if(makeSingleMoves(setup, detectives, d, d.location()).isEmpty()){
					detectivesCannotMove.add(d.piece());
				}
			}

			if(detectivesCannotMove.equals(remainingList)){
				isAllDetectivesCannotMove = true;
			}

			if(remainingList.isEmpty() || isAllDetectivesCannotMove){
				remaining = ImmutableSet.of(mrX.piece());
			}else{
				remaining = ImmutableSet.copyOf(newRemaining);
			}
		}

		private boolean isDoubleMove(Move move){
			return move.accept(new Visitor<>() {
				@Override
				public Boolean visit(SingleMove move1) {
					return false;
				}

				@Override
				public Boolean visit(DoubleMove move1) {
					return true;
				}
			});
		}

	}
}
