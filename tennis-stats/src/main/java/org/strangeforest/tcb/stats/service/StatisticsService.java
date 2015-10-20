package org.strangeforest.tcb.stats.service;

import java.sql.*;
import java.util.*;

import org.springframework.beans.factory.annotation.*;
import org.springframework.jdbc.core.*;
import org.springframework.stereotype.*;
import org.strangeforest.tcb.stats.model.*;

import static java.lang.String.*;

@Service
public class StatisticsService {

	@Autowired private JdbcTemplate jdbcTemplate;

	private static final String MATCH_STATS_QUERY =
		"SELECT pw.name AS winner, pl.name AS loser, minutes, 1 w_matches, 0 l_matches, w_sets, l_sets,\n" +
		"  w_ace, w_df, w_sv_pt, w_1st_in, w_1st_won, w_2nd_won, w_sv_gms, w_bp_sv, w_bp_fc,\n" +
		"  l_ace, l_df, l_sv_pt, l_1st_in, l_1st_won, l_2nd_won, l_sv_gms, l_bp_sv, l_bp_fc\n" +
		"FROM match_stats\n" +
		"LEFT JOIN match m USING (match_id)\n" +
		"LEFT JOIN player_v pw ON m.winner_id = pw.player_id\n" +
		"LEFT JOIN player_v pl ON m.loser_id = pl.player_id\n" +
		"WHERE match_id = ? AND set = 0";

	private static final String PLAYER_STATS_COLUMNS =
		"sum(p_matches) p_matches, sum(o_matches) o_matches, sum(p_sets) p_sets, sum(o_sets) o_sets,\n" +
		"sum(p_ace) p_ace, sum(p_df) p_df, sum(p_sv_pt) p_sv_pt, sum(p_1st_in) p_1st_in, sum(p_1st_won) p_1st_won, sum(p_2nd_won) p_2nd_won, sum(p_sv_gms) p_sv_gms, sum(p_bp_sv) p_bp_sv, sum(p_bp_fc) p_bp_fc,\n" +
		"sum(o_ace) o_ace, sum(o_df) o_df, sum(o_sv_pt) o_sv_pt, sum(o_1st_in) o_1st_in, sum(o_1st_won) o_1st_won, sum(o_2nd_won) o_2nd_won, sum(o_sv_gms) o_sv_gms, sum(o_bp_sv) o_bp_sv, sum(o_bp_fc) o_bp_fc";

	private static final String PLAYER_STATS_QUERY = //language=SQL
		"SELECT " + PLAYER_STATS_COLUMNS + "\n" +
		"FROM player_match_stats_v m%1$s\n" +
		"WHERE m.player_id = ?%2$s";

	private static final String TOURNAMENT_EVENT_JOIN = //language=SQL
	 	"\nLEFT JOIN tournament_event e USING (tournament_event_id)";

	private static final String PLAYER_SEASONS_STATS_QUERY =
		"SELECT season, " + PLAYER_STATS_COLUMNS + "\n" +
		"FROM player_match_stats_v\n" +
		"WHERE player_id = ?\n" +
		"GROUP BY season ORDER BY season";

	private static final String PLAYER_PERFORMANCE_COLUMNS =
		"matches_won, matches_lost, grand_slam_matches_won, grand_slam_matches_lost, masters_matches_won, masters_matches_lost, clay_matches_won, clay_matches_lost, grass_matches_won, grass_matches_lost, hard_matches_won, hard_matches_lost, carpet_matches_won, carpet_matches_lost,\n" +
		"deciding_sets_won, deciding_sets_lost, fifth_sets_won, fifth_sets_lost, finals_won, finals_lost, vs_top10_won, vs_top10_lost, after_winning_first_set_won, after_winning_first_set_lost, after_losing_first_set_won, after_losing_first_set_lost, tie_breaks_won, tie_breaks_lost";

	private static final String PLAYER_PERFORMANCE_QUERY =
		"SELECT " + PLAYER_PERFORMANCE_COLUMNS + "\n" +
		"FROM player_performance\n" +
		"WHERE player_id = ?";

	private static final String PLAYER_SEASONS_PERFORMANCE_QUERY =
		"SELECT season, " + PLAYER_PERFORMANCE_COLUMNS + "\n" +
		"FROM player_season_performance_v\n" +
		"WHERE player_id = ?";


	// Match statistics

	public MatchStats getMatchStats(long matchId) {
		return jdbcTemplate.query(
			MATCH_STATS_QUERY,
			rs -> rs.next() ? mapMatchStats(rs) : null,
			matchId
		);
	}

	private MatchStats mapMatchStats(ResultSet rs) throws SQLException {
		String winner = rs.getString("winner");
		String loser = rs.getString("loser");
		PlayerStats winnerStats = mapPlayerStats(rs, "w_");
		PlayerStats loserStats = mapPlayerStats(rs, "l_");
		int minutes = rs.getInt("minutes");
		return new MatchStats(winner, loser, winnerStats, loserStats, minutes);
	}


	// Player statistics

	public PlayerStats getPlayerStats(int playerId) {
		return getPlayerStats(playerId, MatchFilter.ALL);
	}

	public PlayerStats getPlayerStats(int playerId, MatchFilter filter) {
		String join = !filter.isEmpty() ? TOURNAMENT_EVENT_JOIN : "";
		String criteria = filter.getCriteria();
		Object[] params = playerStatsParams(playerId, filter);
		return jdbcTemplate.queryForObject(
			format(PLAYER_STATS_QUERY, join, criteria),
			(rs, rowNum) -> {
				return mapPlayerStats(rs);
			},
			params
		);
	}

	private Object[] playerStatsParams(int playerId, MatchFilter filter) {
		List<Object> params = new ArrayList<>();
		params.add(playerId);
		params.addAll(filter.getParamList());
		return params.toArray();
	}

	public Map<Integer, PlayerStats> getPlayerSeasonsStats(int playerId) {
		Map<Integer, PlayerStats> seasonsStats = new TreeMap<>();
		jdbcTemplate.query(
			PLAYER_SEASONS_STATS_QUERY,
			rs -> {
				int season = rs.getInt("season");
				PlayerStats stats = mapPlayerStats(rs);
				seasonsStats.put(season, stats);
			},
			playerId
		);
		return seasonsStats;
	}

	private PlayerStats mapPlayerStats(ResultSet rs) throws SQLException {
		PlayerStats playerStats = mapPlayerStats(rs, "p_");
		PlayerStats opponentStats = mapPlayerStats(rs, "o_");
		playerStats.setOpponentStats(opponentStats);
		return playerStats;
	}

	private PlayerStats mapPlayerStats(ResultSet rs, String prefix) throws SQLException {
		return new PlayerStats(
			rs.getInt(prefix + "matches"),
			rs.getInt(prefix + "sets"),
			rs.getInt(prefix + "ace"),
			rs.getInt(prefix + "df"),
			rs.getInt(prefix + "sv_pt"),
			rs.getInt(prefix + "1st_in"),
			rs.getInt(prefix + "1st_won"),
			rs.getInt(prefix + "2nd_won"),
			rs.getInt(prefix + "sv_gms"),
			rs.getInt(prefix + "bp_sv"),
			rs.getInt(prefix + "bp_fc")
		);
	}


	// Player performance

	public PlayerPerformance getPlayerPerformance(int playerId) {
		return jdbcTemplate.query(
			PLAYER_PERFORMANCE_QUERY,
			rs -> rs.next() ? mapPlayerPerformance(rs) : PlayerPerformance.EMPTY,
			playerId
		);
	}

	public Map<Integer, PlayerPerformance> getPlayerSeasonsPerformance(int playerId) {
		Map<Integer, PlayerPerformance> seasonsPerf = new TreeMap<>();
		jdbcTemplate.query(
			PLAYER_SEASONS_PERFORMANCE_QUERY,
			rs -> {
				int season = rs.getInt("season");
				seasonsPerf.put(season, mapPlayerPerformance(rs));
			},
			playerId
		);
		return seasonsPerf;
	}

	private PlayerPerformance mapPlayerPerformance(ResultSet rs) throws SQLException {
		PlayerPerformance perf = new PlayerPerformance();
		// Performance
		perf.setMatches(mapWonLost(rs, "matches"));
		perf.setGrandSlamMatches(mapWonLost(rs, "grand_slam_matches"));
		perf.setMastersMatches(mapWonLost(rs, "masters_matches"));
		perf.setClayMatches(mapWonLost(rs, "clay_matches"));
		perf.setGrassMatches(mapWonLost(rs, "grass_matches"));
		perf.setHardMatches(mapWonLost(rs, "hard_matches"));
		perf.setCarpetMatches(mapWonLost(rs, "carpet_matches"));
		// Pressure situations
		perf.setDecidingSets(mapWonLost(rs, "deciding_sets"));
		perf.setFifthSets(mapWonLost(rs, "fifth_sets"));
		perf.setFinals(mapWonLost(rs, "finals"));
		perf.setVsTop10(mapWonLost(rs, "vs_top10"));
		perf.setAfterWinningFirstSet(mapWonLost(rs, "after_winning_first_set"));
		perf.setAfterLosingFirstSet(mapWonLost(rs, "after_losing_first_set"));
		perf.setTieBreaks(mapWonLost(rs, "tie_breaks"));
		return perf;
	}

	private static WonLost mapWonLost(ResultSet rs, String name) throws SQLException {
		return new WonLost(rs.getInt(name + "_won"), rs.getInt(name + "_lost"));
	}
}
