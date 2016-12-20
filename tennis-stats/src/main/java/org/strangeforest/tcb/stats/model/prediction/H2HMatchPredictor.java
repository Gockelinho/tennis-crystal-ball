package org.strangeforest.tcb.stats.model.prediction;

import java.time.*;
import java.util.*;
import java.util.function.*;

import org.strangeforest.tcb.stats.model.*;

import static org.strangeforest.tcb.stats.model.prediction.H2HPredictionItem.*;
import static org.strangeforest.tcb.stats.model.prediction.MatchDataUtil.*;

public class H2HMatchPredictor implements MatchPredictor {

	private final List<MatchData> matchData1;
	private final List<MatchData> matchData2;
	private final int playerId1;
	private final int playerId2;
	private final Date date;
	private final Surface surface;
	private final TournamentLevel level;
	private final Round round;
	private final short bestOf;

	private static final Period RECENT_PERIOD = Period.ofYears(3);

	public H2HMatchPredictor(List<MatchData> matchData1, List<MatchData> matchData2, int playerId1, int playerId2, Date date, Surface surface, TournamentLevel level, Round round, short bestOf) {
		this.matchData1 = matchData1;
		this.matchData2 = matchData2;
		this.playerId1 = playerId1;
		this.playerId2 = playerId2;
		this.date = date;
		this.surface = surface;
		this.level = level;
		this.round = round;
		this.bestOf = bestOf;
	}

	@Override public PredictionArea area() {
		return PredictionArea.H2H;
	}

	@Override public MatchPrediction predictMatch() {
		MatchPrediction prediction = new MatchPrediction();
		addItemProbabilities(prediction, MATCH, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE, isSurface(surface));
		addItemProbabilities(prediction, LEVEL, isLevel(level));
		addItemProbabilities(prediction, ROUND, isRound(round));
		addItemProbabilities(prediction, RECENT, isRecent(date, RECENT_PERIOD));
		addItemProbabilities(prediction, SURFACE_RECENT, isSurface(surface).and(isRecent(date, RECENT_PERIOD)));
		addItemProbabilities(prediction, LEVEL_RECENT, isLevel(level).and(isRecent(date, RECENT_PERIOD)));
		addItemProbabilities(prediction, ROUND_RECENT, isRound(round).and(isRecent(date, RECENT_PERIOD)));
		addItemProbabilities(prediction, SET, ALWAYS_TRUE);
		addItemProbabilities(prediction, SURFACE_SET, isSurface(surface));
		addItemProbabilities(prediction, LEVEL_SET, isLevel(level));
		addItemProbabilities(prediction, ROUND_SET, isRound(round));
		addItemProbabilities(prediction, RECENT_SET, isRecent(date, RECENT_PERIOD));
		addItemProbabilities(prediction, SURFACE_RECENT_SET, isSurface(surface).and(isRecent(date, RECENT_PERIOD)));
		addItemProbabilities(prediction, LEVEL_RECENT_SET, isLevel(level).and(isRecent(date, RECENT_PERIOD)));
		addItemProbabilities(prediction, ROUND_RECENT_SET, isRound(round).and(isRecent(date, RECENT_PERIOD)));
		return prediction;
	}

	private void addItemProbabilities(MatchPrediction prediction, H2HPredictionItem item, Predicate<MatchData> filter) {
		if (item.weight() > 0.0) {
			ToIntFunction<MatchData> dimension = item.forSet() ? MatchData::getPSets : MatchData::getPMatches;
			long won1 = matchData1.stream().filter(filter.and(isOpponent(playerId2))).mapToInt(dimension).sum();
			long won2 = matchData2.stream().filter(filter.and(isOpponent(playerId1))).mapToInt(dimension).sum();
			long total = won1 + won2;
			if (total > 0) {
				double weight = item.weight() * weight(total);
				DoubleUnaryOperator probabilityTransformer = probabilityTransformer(item.forSet(), bestOf);
				prediction.addItemProbability1(area(), item, weight, probabilityTransformer.applyAsDouble(1.0 * won1 / total));
				prediction.addItemProbability2(area(), item, weight, probabilityTransformer.applyAsDouble(1.0 * won2 / total));
			}
		}
	}
}