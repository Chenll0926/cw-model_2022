package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import java.util.*;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	@Nonnull @Override public Model build(GameSetup setup,
	                                      Player mrX,
	                                      ImmutableList<Player> detectives) {
		return new Model() {
			MyGameStateFactory myFactory = new MyGameStateFactory();

			Board.GameState gameState = myFactory.build(setup, mrX, detectives);

			Set<Observer> observerList = new HashSet<>();  //create a new list to store observers

			@Nonnull
			@Override
			public Board getCurrentBoard() {
				return gameState;
			}

			@Override
			public void registerObserver(@Nonnull Observer observer) {

				//if no observer need to add throw error

				if(observer == null) throw new NullPointerException();

				//If there is no same observer then add it, else throw error

				if (!observerList.contains(observer)) observerList.add(observer);//throw new IllegalArgumentException();
				else throw new IllegalArgumentException();

			}

			@Override
			public void unregisterObserver(@Nonnull Observer observer) {

				//if no observer need to remove throw error

				if(observer == null) throw new NullPointerException();

				//If there is a same observer then remove it, else throw error

				if (!observerList.contains(observer)) throw new IllegalArgumentException();

				observerList.remove(observer);

			}

			@Nonnull
			@Override
			public ImmutableSet<Observer> getObservers() {

				return ImmutableSet.copyOf(observerList);  //return the copy of the observer list

			}

			@Override
			public void chooseMove(@Nonnull Move move) {

				gameState = gameState.advance(move);  //get the next move

				Observer.Event event;

				//judge the game state over or continue?
				if (gameState.getWinner().isEmpty()) {

					event = Observer.Event.MOVE_MADE;

				} else event = Observer.Event.GAME_OVER;

				//tell all the observer the game state
				for (Observer observer : observerList) {

					observer.onModelChanged(gameState, event);

				}
			}
		};
	}
}
